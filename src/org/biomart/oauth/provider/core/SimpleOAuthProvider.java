/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.biomart.oauth.provider.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import org.biomart.common.resources.Log;
import org.biomart.oauth.persist.Consumer;

/**
 * Utility methods for providers that store consumers, tokens and secrets in 
 * local cache (HashSet). Consumer key is used as the name, and its credentials are 
 * stored in HashSet.
 *
 * @author Praveen Alavilli
 */
public class SimpleOAuthProvider {

    public static final SimpleOAuthValidator VALIDATOR = new SimpleOAuthValidator();

    private static final Map<String, OAuthConsumer> ALL_CONSUMERS 
                    = Collections.synchronizedMap(new HashMap<String,OAuthConsumer>(10));
    
    private static final Collection<OAuthAccessor> ALL_TOKENS = new HashSet<OAuthAccessor>();

    public static synchronized void loadConsumers() throws IOException {
        ALL_CONSUMERS.clear();
        Set<Consumer> consumers = Consumer.all();
        for (Consumer c : consumers) {
            Log.info("Loading OAuth consumer: " + c.oauthConsumer.getProperty("name"));
            ALL_CONSUMERS.put(c.oauthConsumer.consumerKey, c.oauthConsumer);
        }
    }

    public static synchronized OAuthConsumer getConsumer(
            OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {
        
        OAuthConsumer consumer = null;
        // try to load from local cache if not throw exception
        String consumer_key = requestMessage.getConsumerKey();
        
        consumer = SimpleOAuthProvider.ALL_CONSUMERS.get(consumer_key);
        
        if(consumer == null) {
            OAuthProblemException problem = new OAuthProblemException("token_rejected");
            throw problem;
        }
        
        return consumer;
    }
    
    /**
     * Get the access token and token secret for the given oauth_token. 
     */
    public static synchronized OAuthAccessor getAccessor(OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {
        
        // try to load from loyal cache if not throw exception
        String oauth_token = requestMessage.getParameter("oauth_token");
        String consumer_key = requestMessage.getParameter("oauth_consumer_key");
        OAuthAccessor accessor = null;
        for (OAuthAccessor a : SimpleOAuthProvider.ALL_TOKENS) {
            if(a.requestToken != null) {
                if (a.requestToken.equals(oauth_token)) {
                    accessor = a;
                    break;
                }
            } else if(a.accessToken != null){
                if (a.accessToken.equals(oauth_token) && a.consumer.consumerKey.equals(consumer_key)) {
                    accessor = a;
                    break;
                }
            }
        }
        
        if(accessor == null){
            OAuthProblemException problem = new OAuthProblemException("token_expired");
            throw problem;
        }
        
        return accessor;
    }

    public static synchronized void removeAuthorization(OAuthAccessor accessor) {
        accessor.setProperty("user", null);
        accessor.setProperty("authorized", Boolean.FALSE);
        ALL_TOKENS.remove(accessor);
    }

    /**
     * Set the access token 
     */
    public static synchronized void markAsAuthorized(OAuthAccessor accessor, String userId)
            throws OAuthException {
        
        
        // first remove the accessor from cache
        ALL_TOKENS.remove(accessor);
        
        accessor.setProperty("user", userId);   
        accessor.setProperty("authorized", Boolean.TRUE);
        
        // update token in local cache
        ALL_TOKENS.add(accessor);
    }
    

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateRequestToken(
            OAuthAccessor accessor)
            throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        String secret_data = consumer_key + System.nanoTime() + token;
        String secret = DigestUtils.md5Hex(secret_data);
        
        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;
        
        // add to the local cache
        ALL_TOKENS.add(accessor);
        
    }
    
    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateAccessToken(OAuthAccessor accessor)
            throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // first remove the accessor from cache
        ALL_TOKENS.remove(accessor);
        
        accessor.requestToken = null;
        accessor.accessToken = token;
        
        // update token in local cache
        ALL_TOKENS.add(accessor);
    }

    public static void handleException(Exception e, HttpServletRequest request,
            HttpServletResponse response, boolean sendBody)
            throws IOException, ServletException {
        String realm = (request.isSecure())?"https://":"http://";
        OutputStream out = response.getOutputStream();
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody); 
        out.close();
    }

}
