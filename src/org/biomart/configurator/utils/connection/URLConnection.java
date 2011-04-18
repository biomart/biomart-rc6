package org.biomart.configurator.utils.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class URLConnection {
	
	private static URLConnection instance; 

	
	public static URLConnection getInstance() {
		if(instance == null)
			instance = new URLConnection();
		return instance;
	}
	
	private URLConnection() {
	}
	
	/**
	 * 
	 * @param url example: http://www.biomart.org:80/biomart/martservice
	 * @return
	 */
	public List<MartInVirtualSchema> getMartsFromURL(String url) {
		List<MartInVirtualSchema> martList = new ArrayList<MartInVirtualSchema>();
		String martUrl = url+"?type=registry";
		URL registryURL = null;
		try {
			registryURL = new URL(martUrl);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		SAXBuilder builder = new SAXBuilder();
		Document registryDocument = null;
		try {
			registryDocument = builder.build(registryURL.openStream());
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(registryDocument==null) {
			return martList;
		}
		Element rootElement = registryDocument.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> virtualSchemaList = rootElement.getChildren();
		
		for (Element virtualSchema : virtualSchemaList) {
			MartInVirtualSchema mart = new MartInVirtualSchema.URLBuilder().database(virtualSchema.getAttributeValue("database"))
				.defaultValue(virtualSchema.getAttributeValue("default"))
				.displayName(virtualSchema.getAttributeValue("displayName"))
				.host(virtualSchema.getAttributeValue("host"))
				.includeDatasets(virtualSchema.getAttributeValue("includeDatasets"))
				.martUser(virtualSchema.getAttributeValue("martUser"))
				.name(virtualSchema.getAttributeValue("name"))
				.path(virtualSchema.getAttributeValue("path"))
				.port(virtualSchema.getAttributeValue("port"))
				.serverVirtualSchema(virtualSchema.getAttributeValue("serverVirtualSchema"))
				.visible(virtualSchema.getAttributeValue("visible"))
				.build();
			martList.add(mart);	
		}
		return martList;
	}

}