package org.mortbay.jetty.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.codehaus.jackson.map.ObjectMapper;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.VoldemortSessionManager.SessionData;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.versioning.Versioned;

/**
 *
 * @author jhsu
 *
 */
public class VoldemortSessionIdManager extends AbstractSessionIdManager {
    private final String __sessionIds = "sessionIds";

    private final String __storeName;
    private final String __connectionUrl;

    private final ObjectMapper _mapper = new ObjectMapper();

    private VoldemortDataStore<String,Object> _dataStore;

    protected Timer _timer; //scavenge timer
    protected TimerTask _task; //scavenge task
    protected long _lastScavengeTime;
    protected long _scavengeIntervalMs = 1000 * 60 * 10; //10mins

    protected class VoldemortDataStore<K,V> {
        private final StoreClient<K,V> client;

        public VoldemortDataStore() {
            String[] urls = __connectionUrl.split(",");
            StoreClientFactory factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(urls));
            client = factory.getStoreClient(__storeName);
        }

        public V get(K k) {
            return client.getValue(k);
        }

        public Set<V> getAll(Iterable<K> keys) {
            Set<V> set = new HashSet<V>();
            Map<K, Versioned<V>> map = client.getAll(keys);
            for (Entry<K,Versioned<V>> entry : map.entrySet()) {
                set.add(entry.getValue().getValue());
            }
            return set;
        }

        public void put(K k, V v) {
            client.put(k, v);
        }

        public boolean delete(K k) {
            return client.delete(k);
        }
    }

    public VoldemortSessionIdManager(Server server) {
        this(server, System.getProperty("datastore.name"), System.getProperty("datastore.url"));
    }
   
    public VoldemortSessionIdManager(Server server, String storeName, String connectionUrl) {
        super(server);
        __storeName = storeName;
        __connectionUrl = connectionUrl;
    }

    public VoldemortSessionIdManager(Server server, Random random) {
       super(server, random);
        __storeName = System.getProperty("datastore.name");
        __connectionUrl = System.getProperty("datastore.url");
    }

    @Override
    public void doStart() {
        _dataStore = new VoldemortDataStore<String,Object>();
        synchronized (_dataStore) {
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);
            if (ids == null) {
                ids = new ArrayList<String>();
                _dataStore.put(__sessionIds, ids);
            }
        }
        super.doStart();
        _timer = new Timer("VoldemortSessionScavenger", true);
        setScavengeInterval(getScavengeInterval());
    }

    @Override
    public void doStop() throws Exception {
        synchronized(this) {
            if (_task!=null)
                _task.cancel();
            if (_timer!=null)
                _timer.cancel();
            _timer=null;
        }
        super.doStop();
    }

    @Override
    public boolean idInUse(String id) {
        synchronized (_dataStore) {
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);
            return ids.contains(id);
        }
    }

    public void setScavengeInterval (long sec) {
        if (sec<=0)
            sec=60;

        long old_period=_scavengeIntervalMs;
        long period=sec*1000;

        _scavengeIntervalMs=period;

        //add a bit of variability into the scavenge time so that not all
        //nodes with the same scavenge time sync up
        long tenPercent = _scavengeIntervalMs/10;
        if ((System.currentTimeMillis()%2) == 0)
            _scavengeIntervalMs += tenPercent;

        if (Log.isDebugEnabled()) Log.debug("Scavenging every "+_scavengeIntervalMs+" ms");
        if (_timer!=null && (period!=old_period || _task==null)) {
            synchronized (this) {
                if (_task!=null)
                    _task.cancel();
                _task = new TimerTask() {
                    public void run()
                    {
                        scavenge();
                    }
                };
                _timer.schedule(_task,_scavengeIntervalMs,_scavengeIntervalMs);
            }
        }
    }

    public long getScavengeInterval () {
        return _scavengeIntervalMs/1000;
    }

    @Override
    public void addSession(HttpSession session) {
        if (session == null) return;
        String id = ((VoldemortSessionManager.Session)session).getClusterId();
        synchronized (_dataStore) {
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);
            ids.add(id);
            _dataStore.put(__sessionIds, ids);
        }
    }

    @Override
    public void removeSession(HttpSession session) {
        if (session == null) return;
        String id = ((VoldemortSessionManager.Session)session).getClusterId();
        synchronized (_dataStore) {
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);
            ids.remove(id);
            _dataStore.put(__sessionIds, ids);
        }
    }

    @Override
    public void invalidateAll(String id) {
        synchronized (_dataStore) {
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);
            ids.remove(id);
            _dataStore.put(__sessionIds, ids);
            _dataStore.delete(id);
        }
    }

    /**
     * Get the session id without any node identifier suffix.
     *
     * @see org.mortbay.jetty.SessionIdManager#getClusterId(java.lang.String)
     */
    @Override
    public String getClusterId(String nodeId) {
        int dot = nodeId.lastIndexOf('.');
        return (dot > 0) ? nodeId.substring(0,dot) : nodeId;
    }

    /**
     * Get the session id, including this node's id as a suffix.
     *
     * @see org.mortbay.jetty.SessionIdManager#getNodeId(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getNodeId(String clusterId, HttpServletRequest request) {
        if (_workerName != null)
            return clusterId + '.' + _workerName;

        return clusterId;
    }

    protected VoldemortDataStore<String,Object> getConnection() {
        return _dataStore;
    }

    /**
     * Look for sessions in the database that have expired.
     *
     * We do this in the SessionIdManager and not the SessionManager so
     * that we only have 1 scavenger, otherwise if there are n SessionManagers
     * there would be n scavengers, all contending for the database.
     *
     * We look first for sessions that expired in the previous interval, then
     * for sessions that expired previously - these are old sessions that no
     * node is managing any more and have become stuck in the database.
     */
    private void scavenge () {
        List expiredSessionIds = new ArrayList();
        List<String> validIds = new ArrayList<String>();
        try {
            if (Log.isDebugEnabled()) Log.debug("Scavenge sweep started at "+System.currentTimeMillis());
            List<String> ids = (List<String>)_dataStore.get(__sessionIds);

            if (_lastScavengeTime > 0) {
                //"select sessionId from JettySessions where expiryTime > (lastScavengeTime - scanInterval) and expiryTime < lastScavengeTime";
                long lowerBound = (_lastScavengeTime - _scavengeIntervalMs);
                long upperBound = _lastScavengeTime;
                long upperBound2 =  _lastScavengeTime - (2 * _scavengeIntervalMs);
                if (Log.isDebugEnabled()) Log.debug("Searching for sessions expired between "+lowerBound + " and "+upperBound);
                for (String id : ids) {
                    String str = (String)_dataStore.get(VoldemortSessionManager.__sessionPrefix + id);
                    if (str != null) {
                        SessionData data = (SessionData)_mapper.readValue(str, SessionData.class);
                        long exp = data.getExpiryTime();

                        //find all sessions that have expired at least a couple of scanIntervals ago and just delete them
                        if (upperBound2 > 0 && exp > 0 && exp <= upperBound2) {
                            _dataStore.delete(VoldemortSessionManager.__sessionPrefix + id + VoldemortSessionManager.__attributesSuffix);
                            _dataStore.delete(VoldemortSessionManager.__sessionPrefix + id);
                        } else {
                            if (exp >= lowerBound && exp <= upperBound) {
                                expiredSessionIds.add(id);
                                if (Log.isDebugEnabled()) Log.debug("Found expired sessionId="+id);
                            }
                            validIds.add(id);
                        }
                    } else {
                        _dataStore.delete(VoldemortSessionManager.__sessionPrefix + id);
                        _dataStore.delete(VoldemortSessionManager.__sessionPrefix + id + VoldemortSessionManager.__attributesSuffix);
                    }
                }

                // Update with new values
                synchronized(_dataStore) {
                    _dataStore.put(__sessionIds, validIds);
                }

                //tell the SessionManagers to expire any sessions with a matching sessionId in memory
                Handler[] contexts = _server.getChildHandlersByClass(WebAppContext.class);
                for (int i=0; contexts!=null && i<contexts.length; i++) {
                    AbstractSessionManager manager = ((AbstractSessionManager)((WebAppContext)contexts[i]).getSessionHandler().getSessionManager());
                    if (manager instanceof VoldemortSessionManager) {
                        ((VoldemortSessionManager)manager).expire(expiredSessionIds);
                    }
                }
            }
        } catch (Exception e) {
            Log.warn("Problem selecting expired sessions", e);
        } finally {
            _lastScavengeTime=System.currentTimeMillis();
            if (Log.isDebugEnabled()) Log.debug("Scavenge sweep ended at "+_lastScavengeTime);
        }
    }
}
