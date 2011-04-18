package org.biomart.configurator.utils;

import java.util.HashMap;
import java.util.Map;

public class MessageConfig {
	private static MessageConfig instance;
	
	/*
	 * 0 yes 
	 * 1 no 
	 * 2 no more for 0 
	 * 3 no more for 1
	 */
	private Map<String,Integer> messageConfig;
	
	public static MessageConfig getInstance() {
		if(instance == null) {
			instance = new MessageConfig();
		}
		return instance;
	}
	
	private MessageConfig() {
		this.messageConfig = new HashMap<String,Integer>();
		this.messageConfig.put("rebuildlink", 1);
	}
	
	public int showDialog(String messageType) {
		if("2".equals(System.getProperty("api"))) //ignore testing
			return 0;
		return messageConfig.get(messageType);
	}
	
	public void put(String key, int value) {
		this.messageConfig.put(key, value);
	}
	
	public int get(String key) {
		return this.messageConfig.get(key);
	}
	
}