package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.UrlLinkObject;
import org.biomart.configurator.utils.connection.URLConnection;
import org.biomart.configurator.utils.type.DataLinkType;
import org.biomart.configurator.view.gui.dialogs.DataLinkDialog;
import org.jdom.Document;
import org.jdom.Element;

public class URLDataLinkPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField urlTF;
	public JTextField getUrlTF() {
		return urlTF;
	}

	private JTextField portTF;
	private JTextField pathTF;
	private JTextField keyTF;
	private JCheckBox groupCB;
	private JCheckBox checkAllCB;
	private URLMetaPanel urlMetaPanel;
	private JCheckBox version8CB;
	private boolean version8 = false;
	private JTextField urlUserTF;
	private JPasswordField urlPwTF;
	
	public URLDataLinkPanel() {
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		JPanel northPanel = new JPanel(new GridLayout(0,1));
		JPanel n1panel = new JPanel();
		JPanel n2panel = new JPanel();
		JPanel n3panel = new JPanel();
		northPanel.add(n1panel);
		northPanel.add(n2panel);
		northPanel.add(n3panel);
		urlTF = new JTextField(30);
		urlTF.setText(Resources.get("biomartUrl"));
		portTF = new JTextField(3);
		portTF.setText("80");
		pathTF = new JTextField(15);
		pathTF.setText(Resources.get("biomartPath"));
		JLabel hostLabel = new JLabel("Host:");
		JLabel pathLabel = new JLabel("Path:");
		JLabel portLabel = new JLabel("Port:");
		this.groupCB = new JCheckBox("Group");
		this.checkAllCB = new JCheckBox("CheckAll");
		this.checkAllCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				URLDataLinkPanel.this.urlMetaPanel.checkAll();
			}			
		});
		this.checkAllCB.setEnabled(false);
		JButton fetchButton = new JButton(Resources.get("getButton"));
		fetchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(URLDataLinkPanel.this.version8) {
					String url = urlTF.getText();
					if(!McUtils.isStringEmpty(portTF.getText()))
						url = url + ":" + portTF.getText();
					//getMartsFromURL8(url,urlUserTF.getText(),new String(urlPwTF.getPassword()));
					getMartsFromURL8OAuth(url, keyTF.getText(),urlUserTF.getText(),new String(urlPwTF.getPassword()));
				}
				else {
					String url = "";
					if(portTF.getText() == null || portTF.getText().equals(""))
						url = urlTF.getText()+pathTF.getText();
					else
						url = urlTF.getText()+":"+portTF.getText()+pathTF.getText();
					setUrlMetaList(url);
				}
			}			
		});
		
		this.version8CB = new JCheckBox("version 0.8");
		this.version8CB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URLDataLinkPanel.this.version8 = URLDataLinkPanel.this.version8CB.isSelected();
			}			
		});
		this.version8CB.setVisible(Boolean.parseBoolean(System.getProperty("showadvancedmenu")));
		
		JLabel userLabel = new JLabel("user:");
		JLabel passwordLabel = new JLabel("password:");
		this.urlUserTF = new JTextField(10);
		this.urlPwTF = new JPasswordField(10);
		//disable gui controls
		userLabel.setEnabled(false);
		passwordLabel.setEnabled(false);
		this.urlUserTF.setEnabled(false);
		this.urlPwTF.setEnabled(false);
		
		n1panel.add(hostLabel);
		n1panel.add(urlTF);
		n1panel.add(portLabel);
		n1panel.add(portTF);
		n1panel.add(pathLabel);
		n1panel.add(pathTF);
		n2panel.add(userLabel);
		n2panel.add(this.urlUserTF);
		n2panel.add(passwordLabel);
		n2panel.add(this.urlPwTF);
		//n2panel.add(this.checkAllCB);
		//n2panel.add(this.groupCB);
		n2panel.add(this.version8CB);
		
		JLabel keyLabel = new JLabel("key:");
		this.keyTF = new JTextField(50);
		n3panel.add(keyLabel);
		n3panel.add(this.keyTF);
		n3panel.add(fetchButton);
		this.add(northPanel, BorderLayout.NORTH);

		this.urlMetaPanel = new URLMetaPanel(this);
		this.add(urlMetaPanel,BorderLayout.CENTER);
	}
	
	private void setUrlMetaList(String url){
		List<MartInVirtualSchema> martList = URLConnection.getInstance().getMartsFromURL(url);
		List<String> martNameList = new ArrayList<String>();
		for(MartInVirtualSchema mart: martList) {
			martNameList.add(mart.getName());
		}
		if(martNameList.isEmpty()) 
			this.checkAllCB.setEnabled(false);
		else {
			this.urlMetaPanel.setBaseUrlString(url);
			this.urlMetaPanel.setMartList(martList);
			this.urlMetaPanel.updateList(martNameList,false);
			this.checkAllCB.setEnabled(true);
		}
	}

	
	private void getMartsFromURL8OAuth(String url, String keys, String userName, String password){
		Document registryDocument = null;
		String martUrl = url+"/martservice/marts";
		if(McUtils.isStringEmpty(keys))
			registryDocument = McUtils.getDocumentFromUrl(martUrl, userName, password);
		else
			registryDocument = McUtils.buildDocument(McUtils.getUrlContentFromOAuth(martUrl, keys));
		if(registryDocument==null) {
			return;
		}
		Element rootElement = registryDocument.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> virtualSchemaList = rootElement.getChildren();
		Map<String,List<String>> martMpMap = new HashMap<String,List<String>>();
//		List<MartInVirtualSchema> martList = new ArrayList<MartInVirtualSchema>();
		for (Element virtualSchema : virtualSchemaList) {
			String mpName = virtualSchema.getAttributeValue("name");
			String martName = virtualSchema.getAttributeValue("mart");
			List<String> mpList = martMpMap.get(martName);
			if(mpList == null) {
				mpList = new ArrayList<String>();
				martMpMap.put(martName,mpList);
			}
			mpList.add(mpName);
		}
		if(martMpMap.isEmpty()) 
			this.checkAllCB.setEnabled(false);
		else {
			this.urlMetaPanel.setBaseUrlString(url);
			this.urlMetaPanel.updateList(martMpMap);
			this.checkAllCB.setEnabled(true);
		}
	}

	public URLMetaPanel getMetaPanel() {
		return this.urlMetaPanel;
	}
	
	public DataLinkInfo getDataLinkInfo() {
		UrlLinkObject conObject = new UrlLinkObject();
		String host = this.urlTF.getText();
		int index = host.indexOf("://");
		host = host.substring(index+3);
	//	conObject.setBaseUrl(this.urlMeta.getBaseUrl());
		conObject.setHost(host);
		conObject.setFullHost(this.urlTF.getText());
		conObject.setPort(this.portTF.getText());
		conObject.setPath(this.pathTF.getText());
		conObject.setVersion8(this.version8);
		conObject.setUserName(this.urlUserTF.getText());
		conObject.setPassword(new String(this.urlPwTF.getPassword()));
		conObject.setKeys(this.keyTF.getText());
		if(this.version8) {
			conObject.setMpList(this.urlMetaPanel.getMpList(false));
		} else
			conObject.setDsInfoMap(this.urlMetaPanel.getDBInfo(false));


		conObject.setGrouped(this.groupCB.isSelected());
		DataLinkInfo dlinkInfo = new DataLinkInfo(DataLinkType.URL);
		dlinkInfo.setUrlLinkObject(conObject);

		return dlinkInfo;
	}
	
	public void setPredefineValues() {
		this.urlTF.setText(Resources.get("biomartUrl"));
		this.pathTF.setText(Resources.get("biomartPath"));
		this.portTF.setText("80");
		this.urlMetaPanel.updateList(null,false);
	}
	
	public void copySettingsForUrl(Properties properties) {
		this.urlTF.setText(properties.getProperty("host"));
		this.portTF.setText(properties.getProperty("port"));
		this.pathTF.setText(properties.getProperty("path"));
		this.urlUserTF.setText(properties.getProperty("user"));
		this.urlPwTF.setText(properties.getProperty("password"));
		this.version8 = "0.8".equals(properties.getProperty("version"));
		this.version8CB.setSelected(this.version8);
	}
}