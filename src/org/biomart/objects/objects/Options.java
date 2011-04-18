package org.biomart.objects.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.object.FilterData;
import org.biomart.configurator.utils.McGuiUtils;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

public class Options {
	private org.jdom.Element optionRoot;
	
	private static Options instance;
	
	public static Options getInstance() {
		if(instance == null)
			instance = new Options();
		return instance;
	}
	
	public void setOptions(org.jdom.Element root) {
		this.optionRoot = root;
		if(this.optionRoot == null)
			this.optionRoot = new Element(XMLElements.OPTIONS.toString());;
	}
	
	private Options() {
	}
	
	public Element getMartElementByName(String martName) {
		if(this.optionRoot == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Element> martElementList = this.optionRoot.getChildren();
		//find mart element
		Element martElement = null;
		for(Element element: martElementList) {
			if(martName.equals(element.getAttributeValue(XMLElements.NAME.toString()))) {
				martElement = element;
				break;
			}
		}
		return martElement;
	}
	
	public Element getMartElement(Mart mart) {
		return this.getMartElementByName(mart.getName());
	}
	
	public Element getConfigElement(Config config, Element martElement) {
		if(martElement == null)
			return null;
		//find config element
		@SuppressWarnings("unchecked")
		List<Element> configElementList = martElement.getChildren();
		Element configElement = null;
		for(Element element: configElementList) {
			if(config.getName().equals(element.getAttributeValue(XMLElements.NAME.toString()))) {
				configElement = element;
				break;
			}
		}
		return configElement;
	}
	
	public Element getFilterOptionElement(Config config, Filter filter, boolean useMaster) {
		Mart mart = config.getMart();
		//find mart element
		Element martElement = getMartElement(mart);
		if(martElement == null)
			return null;
		//find config element

		Element configElement = getConfigElement(config, martElement);
		if(configElement == null) {
			if(!useMaster)
				return null;
			else {
				configElement = getConfigElement(mart.getMasterConfig(), martElement);
				if(configElement == null)
					return null;
			}
		}
			
		@SuppressWarnings("unchecked")
		List<Element> filterElementList = configElement.getChildren();
		Element filterElement = null;
		for(Element element: filterElementList) {
			if(filter.getName().equals(element.getAttributeValue(XMLElements.NAME.toString()))) {
				filterElement = element;
			}
		}
		return filterElement;
	}
	
	public Element getFilterOptionElementIncludeOtherConfigs(Config config, Filter filter) {
		Mart mart = config.getMart();
		//find mart element
		Element martElement = getMartElement(mart);
		if(martElement == null)
			return null;
		//find config element

		Element configElement = getConfigElement(config, martElement);
		if(configElement == null) {
			//get first configElement;
			
		}else {
			
		}
		@SuppressWarnings("unchecked")
		List<Element> filterElementList = configElement.getChildren();
		Element filterElement = null;
		for(Element element: filterElementList) {
			if(filter.getName().equals(element.getAttributeValue(XMLElements.NAME.toString()))) {
				filterElement = element;
			}
		}
		return filterElement;
	}
	
	public Element getFilterOptionElementInFirstConfig(Element martElement, Filter filter) {
		@SuppressWarnings("unchecked")
		List<Element> configElementList = martElement.getChildren();
		for(Element configElement: configElementList) {
			@SuppressWarnings("unchecked")
			List<Element> filterElementList = configElement.getChildren();
			for(Element filterElement: filterElementList) {
				if(filter.getName().equals(filterElement.getAttributeValue(XMLElements.NAME.toString()))) {
					return filterElement;
				}
			}
		}
		return null;
	}

	public void updateFilterOptionElement(Filter filter, Dataset ds, List<FilterData> fdList) {
		//update the master config
		Config config = filter.getParentConfig().getMart().getMasterConfig();
		Mart mart = config.getMart();
		//get mart element
		Element martElement = getMartElement(mart);
		if(martElement == null) {
			//create a martElement, configElement, filterElement
			martElement = new Element(XMLElements.MART.toString());
			martElement.setAttribute(XMLElements.NAME.toString(),mart.getName());
			this.optionRoot.addContent(martElement);
		} 
		//get master config element
		Element configElement = getConfigElement(config, martElement);
		if(configElement == null) {
			configElement = new Element(XMLElements.CONFIG.toString());
			configElement.setAttribute(XMLElements.NAME.toString(),config.getName());
			martElement.addContent(configElement);
		}
		//get filter element
		Element filterElement = getFilterOptionElement(config, filter,false);
		if(filterElement == null) {
			filterElement = new Element(XMLElements.FILTER.toString());
			filterElement.setAttribute(XMLElements.NAME.toString(),filter.getName());
			configElement.addContent(filterElement);
			//add dataset element
			Element datasetElement = new Element(XMLElements.DATASET.toString());
			datasetElement.setAttribute(XMLElements.NAME.toString(),ds.getName());
			filterElement.addContent(datasetElement);
			for(FilterData fd: fdList) {
				Element valueElement = new Element(XMLElements.ROW.toString());
				String dataStr = fd.getName()+"|"+fd.getDisplayName()+"|"+((Boolean)fd.isSelected()).toString();
				valueElement.setAttribute(XMLElements.DATA.toString(),dataStr);
				datasetElement.addContent(valueElement);
			}			
		} else {
			//keep the order for the old options
			List<String> dss = new ArrayList<String>();
			dss.add(ds.getName());
			List<FilterData> oldfd = filter.getFilterDataList(dss);
			oldfd.retainAll(fdList);
			fdList.removeAll(oldfd);
			List<FilterData> newFdList = new ArrayList<FilterData>();
			newFdList.addAll(oldfd);
			newFdList.addAll(fdList);
			//get the datasetelement of this filter
			Element datasetElement = getFilterOptionDatasetElement(ds.getName(), filterElement);
			if(null!=datasetElement) {
				datasetElement.removeContent();
			}else {
				datasetElement = new Element(XMLElements.DATASET.toString());
				datasetElement.setAttribute(XMLElements.NAME.toString(),ds.getName());
				filterElement.addContent(datasetElement);
			}	
			for(FilterData fd: newFdList) {
				Element valueElement = new Element(XMLElements.ROW.toString());
				String dataStr = fd.getName()+"|"+fd.getDisplayName()+"|"+((Boolean)fd.isSelected()).toString();
				valueElement.setAttribute(XMLElements.DATA.toString(),dataStr);
				datasetElement.addContent(valueElement);
			}						
		}
	}
	
	
	public Element getFilterOptionDatasetElement(String ds, Element filterElement) {
		Element datasetElement = null;
		@SuppressWarnings("unchecked")
		List<Element>elementList = filterElement.getChildren();
		for(Element element: elementList) {
			if(ds.equals(element.getAttributeValue(XMLElements.NAME.toString()))) {
				datasetElement = element;
				return datasetElement;
			}
		}
		return datasetElement;
	}

	public void addMartElement(org.jdom.Element martElement) {
		//check if it already exist
		if(this.getMartElementByName(martElement.getAttributeValue(XMLElements.NAME.toString())) !=null)
			return;
		this.optionRoot.addContent(martElement);
	}
	
	public Element getOptionRootElement() {
		if(this.optionRoot!=null)
			this.optionRoot.detach();
		else
			this.optionRoot = new Element(XMLElements.OPTIONS.toString());
		return this.optionRoot;
	}
	
	public void renameDataset(String oldvalue, String newvalue) {
		Element root = this.getOptionRootElement();
		@SuppressWarnings("unchecked")
		Iterator<Element> it = root.getDescendants(new ElementFilter(XMLElements.DATASET.toString()));
		while(it.hasNext()) {
			Element e = it.next();
			if(oldvalue.equals(e.getAttributeValue(XMLElements.NAME.toString()))) {
				e.setAttribute(XMLElements.NAME.toString(), newvalue);
			}
		}
	}

	/*
	 * remove non master config options
	 */
	public void clean() {
		MartRegistry registry = McGuiUtils.INSTANCE.getRegistryObject();
		@SuppressWarnings("unchecked")
		List<Element> martElementList = this.optionRoot.getChildren();
		List<Element> droppedMarts = new ArrayList<Element>();
		List<Element> droppedConfigs = new ArrayList<Element>();
		//find mart element
		for(Element martElement: martElementList) {
			Mart mart = registry.getMartByName(martElement.getAttributeValue(XMLElements.NAME.toString()));
			if(mart == null) {
				droppedMarts.add(martElement);
			} else {
				@SuppressWarnings("unchecked")
				List<Element> configElementList = martElement.getChildren();
				//if only one element and it is not master, rename it to master
				String masterName = mart.getMasterConfig().getName();
				if(configElementList.size()==1 && !masterName.equals(configElementList.get(0).
						getAttributeValue(XMLElements.NAME.toString()))) {
					configElementList.get(0).setAttribute(XMLElements.NAME.toString(), masterName);
				}
				for(Element configElement: configElementList) {
					if(!masterName.equals(configElement.getAttributeValue(XMLElements.NAME.toString()))) {
						droppedConfigs.add(configElement);
					}
				}
			}
		}
		//remove
		for(Iterator<Element> it = droppedMarts.iterator(); it.hasNext();) {
			Element e = it.next();
			e.getParentElement().removeContent(e);
		}
		for(Iterator<Element> it = droppedConfigs.iterator(); it.hasNext();) {
			Element e = it.next();
			e.getParentElement().removeContent(e);
		}
	}
}