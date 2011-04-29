package org.biomart.api.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.biomart.api.Jsoml;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.common.utils.PartitionUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.PartitionType;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.portal.UserGroup;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@XmlRootElement(name="container")
@JsonPropertyOrder({"name", "displayName", "description", "maxContainers", "maxAttributes", "independent"})
public class Container extends LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 5317562972868527144L;

	private org.biomart.objects.objects.Container containerObject;
	//TODO document
	private List<Container> containerList;
	private List<Attribute> attributeList;
	private List<Filter> filterList;
	private boolean containersOnly;
	private UserGroup currentUser;
	

    public Container() {}

	public Container(org.biomart.objects.objects.Container container, List<String> datasetList, boolean includeAttributes,
			boolean includeFilters, UserGroup user) {
        this(container, datasetList, includeAttributes, includeFilters, false, user);
    }

	public Container(org.biomart.objects.objects.Container container, List<String> datasetList, boolean includeAttributes, 
			boolean includeFilters, boolean containersOnly, UserGroup user) {
		super(container);
		this.currentUser = user;
		this.containerList = new ArrayList<Container>();
		this.attributeList = new ArrayList<Attribute>();
		this.filterList = new ArrayList<Filter>();
		this.containerObject = container;
		this.containersOnly = containersOnly;
		//get the sub containers/filters/attributes;
		this.createSubComponents(container, datasetList, includeAttributes, includeFilters, containersOnly, this);
	}
	
	private void createSubComponents(org.biomart.objects.objects.Container container, List<String> datasetList, 
			boolean includeAttributes, boolean includeFilters, boolean containersOnly, Container parent) {
		if(container.isHidden())
			return;
		if(includeAttributes) {
			for(org.biomart.objects.objects.Attribute attribute: container.getAttributeList()) {
				if(!attribute.isValid())
					continue;
				if(!attribute.isHidden() && attribute.inPartition(datasetList) && attribute.inUser(this.currentUser.getName(),datasetList)) {		
					//create multiple one if it is partitioned
					if(McUtils.hasPartitionBinding(attribute.getName())) {
						List<String> ptRefList = McUtils.extractPartitionReferences(attribute.getName());
						//assume only one partition for now
						String ptRef = ptRefList.get(1);
						String ptName = McUtils.getPartitionTableName(ptRef);
						PartitionTable pt = this.containerObject.getParentConfig().getMart().getPartitionTableByName(ptName);
						//use first available row
						for(int i=0; i<pt.getTotalRows(); i++) {
							String dsName = null;
							if(pt.getPartitionType() == PartitionType.SCHEMA) 
								dsName = pt.getValue(i, PartitionUtils.DATASETNAME);
							else
								dsName = pt.getValue(i, 0);
							if(!datasetList.contains(dsName))
								continue;

							String realName = McUtils.replacePartitionReferences(pt, i, ptRefList);
							if(realName !=null) {
								org.biomart.api.lite.Attribute liteAttribute = new org.biomart.api.lite.Attribute(parent,attribute);
								liteAttribute.setRange(datasetList);
								this.attributeList.add(liteAttribute);
								break;
							}
						}
					}else {
						if(attribute.inPartition(datasetList)) {
							Attribute liteAtt = new Attribute(parent,attribute);
							liteAtt.setRange(datasetList);
							this.attributeList.add(liteAtt);
						}
					}
				}
			}
		}
		
		if(includeFilters) {
			for(org.biomart.objects.objects.Filter filter: container.getFilterList()) {
				if(!filter.isValid())
					continue;
				if(!filter.isHidden() && filter.inPartition(datasetList) && filter.inUser(this.currentUser.getName(),datasetList)) {
					//create multiple one if it is partitioned
					if(McUtils.hasPartitionBinding(filter.getName())) {
						List<String> ptRefList = McUtils.extractPartitionReferences(filter.getName());
						//assume only one partition for now
						String ptRef = ptRefList.get(1);
						String ptName = McUtils.getPartitionTableName(ptRef);
						PartitionTable pt = this.containerObject.getParentConfig().getMart().getPartitionTableByName(ptName);
						//add all rows
						for(int i=0; i<pt.getTotalRows(); i++) {
							String dsName = null;
							if(pt.getPartitionType() == PartitionType.SCHEMA) 
								dsName = pt.getValue(i, PartitionUtils.DATASETNAME);
							else
								dsName = pt.getValue(i, 0);
							if(!datasetList.contains(dsName))
								continue;

							String realName = McUtils.replacePartitionReferences(pt, i, ptRefList);
							if(realName !=null) {
								org.biomart.api.lite.Filter liteFilter = new org.biomart.api.lite.Filter(parent,filter);
								liteFilter.setRange(datasetList);
								this.filterList.add(liteFilter);
								break;
							}
						}
					}else {
						Filter liteFilter = new Filter(parent, filter);
						liteFilter.setRange(datasetList);
						this.filterList.add(liteFilter);
					}
				}
			}
		}
		
		for(org.biomart.objects.objects.Container subConObject: container.getContainerList()) {
			if(!subConObject.isHidden()) {
				Container subCon = new Container(subConObject,datasetList,includeAttributes,includeFilters,containersOnly,this.currentUser);
				if(!subCon.isEmpty())
					this.containerList.add(subCon);			
			}
		}
	}

    @XmlElementWrapper(name="containers")
    @XmlElement(name="container")
    @JsonProperty("containers")
	public List<Container> getContainerList() {
		return this.containerList;
	}

    @XmlElementWrapper(name="filters")
    @XmlElement(name="filter")
    @JsonProperty("filters")
	public List<Filter> getFilterList() {
		return this.filterList;
	}
    @XmlElementWrapper(name="attributes")
    @XmlElement(name="attribute")
    @JsonProperty("attributes")
	public List<Attribute> getAttributeList() {
		return this.attributeList;
	}

    @XmlAttribute(name="maxContainers")
    @JsonProperty("maxContainers")
	public int getMaxContainers() {
		return this.containerObject.getMaxContainers();
	}
	
    @XmlAttribute(name="maxAttributes")
    @JsonProperty("maxAttributes")
	public int getMaxAttributes() {
		return this.containerObject.getMaxAttributes();
	}

    @XmlAttribute(name="independent")
    @JsonProperty("independent")
	public boolean isIndependentQuerying() {
		return this.containerObject.isIndependentQuerying();
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
    @JsonIgnore
	public boolean isEmpty() {
		for(Container subContainer: this.containerList) {
			if(!subContainer.isEmpty())
				return false;
		}
		
		if(this.filterList.isEmpty() && this.attributeList.isEmpty()) 
			return true;
		else
			return false;
	}
	
    @JsonIgnore
	public boolean isHidden() {
		return this.containerObject.isHidden();
	}
	
    @JsonIgnore
	public boolean isLeaf() {
		if(this.getContainerList().isEmpty())
			return true;
		else
			return false;
	}
	public boolean hasAttributes() {
		return !getAttributeList().isEmpty();
	}
	public boolean hasFilters() {
		return !getFilterList().isEmpty();
	}


	/**
	 * get a container by name recursively, assuming that the container name is unique for now
	 * @param name
	 * @return
	 */
	public Container getContainerByName(String name) {
		if(this.getName().equals(name))
			return this;
		else {
			for(Container c: this.getContainerList()) {
				Container tmpC = c.getContainerByName(name);
				if(tmpC!=null)
					return tmpC;
			}
		} 
		return null;
	}
	@Override
	protected Jsoml generateExchangeFormat(boolean xml)
			throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.getXMLElementName());
        boolean leaf = isLeaf();

		jsoml.setAttribute("name", super.getName());
		jsoml.setAttribute("displayName", super.getDisplayName());
		jsoml.setAttribute("description", super.getDescription());
        jsoml.setAttribute("isLeaf", leaf);
        jsoml.setAttribute("independent", this.containerObject.isIndependentQuerying());
        jsoml.setAttribute("hasAttributes", hasAttributes());
        jsoml.setAttribute("hasFilters", hasFilters());
        jsoml.setAttribute("maxAttributes", getMaxAttributes());
        jsoml.setAttribute("maxContainers", getMaxContainers());

        // If not leaf then add container names
        if (!leaf) {
            for (Container container : getContainerList()) {
                jsoml.addContent(container.generateExchangeFormat(xml));
            }
        }

        if (!this.containersOnly) {
            if (this.hasAttributes()) {
                for (Attribute a : this.getAttributeList()) {
                    jsoml.addContent(a.generateExchangeFormat(xml));
                }
            }

            if (this.hasFilters()) {
                for (Filter a : this.getFilterList()) {
                    jsoml.addContent(a.generateExchangeFormat(xml));
                }
            }
        }

		return jsoml;
	}

}
