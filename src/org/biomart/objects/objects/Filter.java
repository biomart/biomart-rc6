package org.biomart.objects.objects;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.biomart.common.exceptions.MartBuilderException;
import org.biomart.common.resources.ErrorMessage;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.MartConfiguratorUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.object.FilterData;
import org.biomart.configurator.utils.ConnectionPool;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.Validation;
import org.biomart.configurator.utils.type.DataLinkType;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.enums.FilterOperation;
import org.biomart.objects.enums.FilterType;
import org.biomart.queryEngine.OperatorType;


public class Filter extends Element	{
			
	public Filter(org.jdom.Element element) {
		super(element);
		this.setNodeType(McNodeType.FILTER);
	}

	public void setPointedInfo(String pointedFilterName, String pointedDatasetName,
			String pointedConfigName, String pointedMartName) {
		this.setPointer(true);
		this.setProperty(XMLElements.POINTEDDATASET, pointedDatasetName);
		this.setProperty(XMLElements.POINTEDFILTER, pointedFilterName);
		this.setProperty(XMLElements.POINTEDCONFIG, pointedConfigName);
		this.setProperty(XMLElements.POINTEDMART, pointedMartName);
	}
	
	public String getPointedFilterName() {
		return this.getPropertyValue(XMLElements.POINTEDFILTER);
	}
	
	public String getPointedDatasetName() {
		return this.getPropertyValue(XMLElements.POINTEDDATASET);
	}

	


	public void addFilter(Filter filter)  {
		if(McUtils.isStringEmpty(this.getPropertyValue(XMLElements.FILTERLIST).trim())) {
			this.setProperty(XMLElements.FILTERLIST, filter.getName());
		} else {
			String filterName = filter.getName();
			String[] existingFilters = this.getPropertyValue(XMLElements.FILTERLIST).split(",");
			List<String> filterList = new ArrayList<String>(Arrays.asList(existingFilters));
			if(!filterList.contains(filterName)) {
				filterList.add(filterName);
				this.setProperty(XMLElements.FILTERLIST, McUtils.StrListToStr(filterList, ","));
			}
		}	
	}

	public List<Filter> getFilterList() {
		List<Filter> filterList = new ArrayList<Filter>();
		String filterListStr = this.getPropertyValue(XMLElements.FILTERLIST);
		if(McUtils.isStringEmpty(filterListStr))
			return filterList;
		String[] list = filterListStr.split(",");
		List<String> range = new ArrayList<String>();
		for(String att: list) {
			Filter filter = this.getParentConfig().getFilterByName(att, range);
			if(filter!=null)
				filterList.add(filter);
		}
		return filterList;
	}
	
	public List<Filter> getFilterList(Collection<String> dss) {
		List<Filter> filterList = new ArrayList<Filter>();
		String filterListStr = this.getPropertyValue(XMLElements.FILTERLIST);
		if(McUtils.isStringEmpty(filterListStr))
			return filterList;
		String[] list = filterListStr.split(",");
		for(String att: list) {
			Filter filter = this.getParentConfig().getFilterByName(att, dss);
			if(filter!=null)
				filterList.add(filter);
		}
		return filterList;
	}
	
	public boolean isFilterList() {
		if(!McUtils.isStringEmpty(this.getPropertyValue(XMLElements.FILTERLIST)))
			return true;
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Filter filter=(Filter)object;
		boolean sameconfig = true;
		Config con1 = filter.getParentConfig();
		Config con2 = this.getParentConfig();
		if(null!=con1) {
			if(null!=con2)
				sameconfig = con1.getName().equals(con2.getName());
			else
				sameconfig = false;
		}else {
			if(null!=con2)
				sameconfig = false;
			else
				sameconfig = true;
		}

		return filter.getName().equals(this.getName()) && sameconfig;		
	}
	
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}
	

	
	/**
	 * skip the range if it is a pointer, otherwise return the intersection
	 * @param datasets
	 * @return
	 */
	public List<FilterData> getFilterDataList(Collection<String> datasets) {
		if(this.getObjectStatus().equals(ValidationStatus.VALID)) {
			if(this.isPointer()) 
				return this.getFilterDataListPointer(datasets);
			else
				return this.getFilterDataListNonPointer(datasets);			
		}
		return new ArrayList<FilterData>();
	}

	/**
	 * skip the range if it is a pointer, otherwise return the intersection
	 * @param datasets
	 * @return
	 */
	private List<FilterData> getFilterDataListPointer(Collection<String> datasets) {

		List<FilterData> filterDataList = new ArrayList<FilterData>();
		Set<String> newdsList = new HashSet<String>(datasets);
		
		if(!this.getObjectStatus().equals(ValidationStatus.VALID)) 
			return filterDataList;
		
		Filter currentFilter = this;
		//pointer skip the range
		// see the kegg example
		if(this.isPointer())  {
			newdsList.clear();
			currentFilter = this.getPointedFilter();
		}
		
		String pointedDsName = this.getPointedDatasetName();
		if(McUtils.isStringEmpty(pointedDsName))
			return filterDataList;
		
		if(McUtils.hasPartitionBinding(pointedDsName)) {
			//get new dataset list based on the pointed column
			Mart mart = this.getParentConfig().getMart();
			PartitionTable schemaPt = mart.getSchemaPartitionTable();
			newdsList.addAll(McUtils.getOtherDatasets(schemaPt, new ArrayList<String>(datasets), pointedDsName));
		}else {
			//skip the range check, see kegg example
			newdsList.add(pointedDsName.split(",")[0]);
		}
				
		//return the intersection
		if(currentFilter == null || currentFilter.getObjectStatus()!=ValidationStatus.VALID)
			return filterDataList;
		//if a pointer filter has dependson, don't go to the pointedfilter
		if(!McUtils.isStringEmpty(this.getPropertyValue(XMLElements.DEPENDSON))) 
			//don't use the newdsList in this case
			return this.getDependentFilterDataList(datasets);

		return currentFilter.getFilterDataListNonPointer(newdsList);
	}
	
	private List<FilterData> getDependentFilterDataList(Collection<String> datasets) {
		List<FilterData> fds = new ArrayList<FilterData>();
		Filter pFilter = this.getParentConfig().getFilterByName(this.getPropertyValue(XMLElements.DEPENDSON), datasets);
		if(pFilter == null)
			return fds;
		return McUtils.getSubFilterData(this.getParentConfig(), pFilter, "", datasets, this);
	}

	private List<FilterData> getFilterDataListNonPointer(Collection<String> datasets) {
		//is it a dependent filter
		if(!McUtils.isStringEmpty(this.getPropertyValue(XMLElements.DEPENDSON))) 
			return this.getDependentFilterDataList(datasets);
		
		
		List<FilterData> filterDataList = new ArrayList<FilterData>();		
		//if it is a filterlist, need to check if all of it's child in the datasets
		boolean allInRange = true;
		if(this.isFilterList()) {
			for(Filter filter: this.getFilterList()) {
				if(!filter.inPartition(datasets)) {
					allInRange = false;
					break;
				}
			}
		}
		if(!allInRange)
			return filterDataList;
		//master config only
		Config config = this.getParentConfig().getMart().getMasterConfig();
		Mart mart = config.getMart();

		List<FilterData> tmpFilterDataList = new ArrayList<FilterData>();

		
		org.jdom.Element martElement = Options.getInstance().getMartElement(mart);
		if(martElement == null)
			return filterDataList;
		
		org.jdom.Element configElement = Options.getInstance().getConfigElement(config, martElement);

		org.jdom.Element filterElement = null;
		if(configElement == null) {
			filterElement = Options.getInstance().getFilterOptionElementInFirstConfig(martElement, this);
		} else {
			filterElement = Options.getInstance().getFilterOptionElement(config, this,true);
			if(filterElement == null) 
				filterElement = Options.getInstance().getFilterOptionElementInFirstConfig(martElement, this);
		}
				
		if(filterElement!=null) {
			//check if it exist in all datasets
			int i=-1;
			for(String dsName: datasets) {
				i++;
				tmpFilterDataList.clear();
				@SuppressWarnings("unchecked")
				List<org.jdom.Element> childElementList = filterElement.getChildren();
				//the children can have three cases
				//1. row
				//2. dataset
				//3. row->filter->row
				//check if can find the dataset
				org.jdom.Element child = McUtils.findChildElementByAttribute(filterElement, XMLElements.NAME.toString(), dsName);
				if(child == null && childElementList.size()==0) {
					filterDataList.clear();
					return filterDataList;						
				} else if(child == null) { //check if the first child is dataset 
					if(XMLElements.DATASET.toString().equals(childElementList.get(0).getName())) {
						//it is dataset but not contains the dsName
						filterDataList.clear();
						return filterDataList;	
					} else 
						child = filterElement;
				}
				//the children of child is <row> now
				
				@SuppressWarnings("unchecked")
				List<org.jdom.Element> dataElementList = child.getChildren();
				for(org.jdom.Element dataElement: dataElementList) {
					String data = dataElement.getAttributeValue("data");
					if(McUtils.isStringEmpty(data)) {
//						Log.error("data is null ");
						continue;
					}
					String[] dataArray = McUtils.getOptionsDataFromString(data);
					//remove all legacy null row
					if(dataArray[1].equals("null")|| dataArray[1].equals("NULL"))
						continue;
					FilterData fd = new FilterData(dataArray[0],
							dataArray[1],new Boolean(dataArray[2]));
					if(!tmpFilterDataList.contains(fd))
						tmpFilterDataList.add(fd);						
				}
				
				if(i==0) 
					filterDataList.addAll(tmpFilterDataList);
				else {//do the intersection 
					filterDataList.retainAll(tmpFilterDataList);
				}
			}
		} else { //intersection == null
			filterDataList.clear();
			return filterDataList;					
		}			
		

		return filterDataList;
	}

 	public OperatorType getQualifier() {
		return OperatorType.valueFrom(this.getPropertyValue(XMLElements.QUALIFIER));
	}
	
	public void setQualifier(OperatorType ot) {
		this.setProperty(XMLElements.QUALIFIER, ot.toString());
	}

	public Filter getPointedFilter() {
		Config pointedConfig = this.getPointedConfing();
		if(pointedConfig == null || McUtils.isStringEmpty(this.getPropertyValue(XMLElements.POINTEDFILTER)))
			return null;
		return pointedConfig.getFilterByName(this.getPropertyValue(XMLElements.POINTEDFILTER), new ArrayList<String>());
	}
	
		
	public Filter(Attribute attribute, String name) {
		//check unique name
		super(name);
		this.setProperty(XMLElements.DISPLAYNAME, attribute.getDisplayName());
		this.setProperty(XMLElements.INTERNALNAME, attribute.getInternalName());
		this.setProperty(XMLElements.DESCRIPTION, attribute.getDescription());
		this.setProperty(XMLElements.ATTRIBUTE, attribute ==null?"":attribute.getName());
		this.setNodeType(McNodeType.FILTER);
		this.setObjectStatus(ValidationStatus.VALID);  
	}
	
	public DatasetColumn getDatasetColumn() {
		if(this.getAttribute() == null)
			return null;
		return this.getAttribute().getDataSetColumn();
	}
	
	/**
	 * need to handle the partition
	 * @return
	 */
	public DatasetTable getDatasetTable() {
		return this.getAttribute().getDatasetTable();		
	}
	
	
	/**
	 * for filter list
	 * by default, use singleselect type
	 * @param name
	 * @param displayName
	 */
	public Filter(String name, String displayName) {
		super(name);
		this.setDisplayName(displayName);
		this.setNodeType(McNodeType.FILTER);
	}
	
	public Filter(String name, String pointedFilterName, String pointedDatasetName) {
		super(name);
		this.setPointer(true);
		this.setNodeType(McNodeType.FILTER);
		//it is not valid for now because the pointedFilter and pointedDataset may not be valid
		this.setObjectStatus(ValidationStatus.POINTERINCOMPLETE);
		this.setProperty(XMLElements.POINTEDDATASET, pointedDatasetName);
		this.setProperty(XMLElements.POINTEDFILTER, pointedFilterName);
		this.setObjectStatus(ValidationStatus.POINTERINCOMPLETE);
	}
	
	public Filter(FilterType type, String name) {
		super(name);
		this.setNodeType(McNodeType.FILTER);
		if(type==null)
			this.setFilterType(FilterType.TEXT);
		else
			this.setFilterType(type);
		this.setObjectStatus(ValidationStatus.VALID);
	}
	
	public void setAttribute(Attribute attribute) {
		this.setProperty(XMLElements.ATTRIBUTE, attribute.getName());
	}
	
	/*
	 * used by editing the attribute from the gui
	 */
	public void setAttribute(String attName) {
		//find attribute by name
		Attribute att = this.getParentConfig().getAttributeByName(null, 
				attName, true);
		if(att!=null)
			this.setAttribute(att);
		else 
			JOptionPane.showMessageDialog(null, "attribute name error", "error",JOptionPane.ERROR_MESSAGE);
	}

    public String getRDF() {
        return this.getPropertyValue(XMLElements.RDF);
    }

    public void setRDF(String rdf) {
        this.setProperty(XMLElements.RDF, rdf);
    }

	/**
	 * return the attribute object. if the filter is pointer, return the pointed filter's attribute
	 * @return
	 */
	public Attribute getAttribute() {
		if(this.isPointer() && this.getPointedFilter() !=null)
			return this.getPointedFilter().getAttribute();
		return this.getParentConfig().getAttributeByName(this.getPropertyValue(XMLElements.ATTRIBUTE), new ArrayList<String>());
	}

	public void setFilterType(FilterType type) {
		this.setProperty(XMLElements.TYPE, type.toString());
	}
	
	public FilterType getFilterType () {
		if(this.isPointer() && this.getPointedFilter() !=null) {
			return this.getPointedFilter().getFilterType();
		}
		return FilterType.valueFrom(this.getPropertyValue(XMLElements.TYPE));
	}

	public void setSplitOnValue(String value) {
		this.setProperty(XMLElements.SPLITON, value);
	}
	
	public void setFilterOperation(FilterOperation value) {
		this.setProperty(XMLElements.OPERATION, value.toString());
	}
	
	public void setDataFileUrl(String path) {
		this.setProperty(XMLElements.DATAFILE, path);
	}
	
	public void setOnlyValue(String value) {
		this.setProperty(XMLElements.ONLY, value);
	}
	
	public String getOnlyValue() {
		return this.getPropertyValue(XMLElements.ONLY);
	}
	
	public void setExcludedValue(String value) {
		this.setProperty(XMLElements.EXCLUDED, value);
	}
	
	public String getExcludedValue() {
		return this.getPropertyValue(XMLElements.EXCLUDED);
	}
	
	public boolean inPartition(String value) {
		List<String> l = new ArrayList<String>();
		l.add(value);
		return this.inPartition(l);
	}

	public boolean inPartition(Collection<String> values) {
		if(values == null || values.isEmpty())
			return true;
		//is it a pointer?
		if(this.isPointer() && this.getPointedFilter()!=null)  {
			if(McUtils.hasPartitionBinding(this.getPointedDatasetName())) {
				Filter pointedFilter = this.getPointedFilter();
				if(pointedFilter.getObjectStatus()!=ValidationStatus.VALID)
					return false;
				Set<String> targetPartitions = new HashSet<String>();
				//assume they are in schemapartitiontable
				PartitionTable pt = this.getParentConfig().getMart().getSchemaPartitionTable();
				for(String sourceDsStr: values) {
					int row = pt.getRowNumberByDatasetName(sourceDsStr);
					if(row>=0) {
						String realName = McUtils.getRealName(pt, row, this.getPointedDatasetName());
						if(McUtils.isStringEmpty(realName))
							continue;
						String[] _newDsStrs = realName.split(",");
						for(String item: _newDsStrs) {
							targetPartitions.add(item);
						}
					}
				}
				/*
				 * no value for the dataset rows
				 */
				if(targetPartitions.isEmpty())
					return false;
				else
					return pointedFilter.inPartition(targetPartitions);
			}else
				return true;
		}
		if(values == null || values.isEmpty())
			return true;
		//TODO Figure out how to make this work for both ICGC and Ensembl
		// The problem is that, in the commented code below, a check is being made to see if the pointer dataset is valid.
		// For some reason, though, this is returning "false" for ICGC Kegg Pathway. Need to better understand the logic
		// before I can make a fix.
		/*	String pointeddatasets = this.getPointedDatasetName();

			String[] dss = pointeddatasets.split(",");
			if(values.containsAll(Arrays.asList(dss))) 
				return this.getPointedFilter().inPartition(values);
			else {
				//check the name convention
				boolean found = true;
				for(String item: dss) {
					String[] _names = item.split("_");
					for(String ptStr: values) {
						if(ptStr.indexOf(_names[0])<0) {
							found = false;
							break;
						}
					}
				}
				return found;
//				return false;
			}
			//return this.getPointedFilter().inPartition(values);
		}*/
		//is it a filterlist?		
		if(!this.getFilterList().isEmpty()) {
			return true; //FIXME return true for now
		}
		
		if(this.getAttribute() == null)
			return false;
		return this.getAttribute().inPartition(values);		
	}


	//if the filter's status is incomplete, need to store this information for future reference
	public void setFilterListString(String filterListString) {
		this.setProperty(XMLElements.FILTERLIST, filterListString);
	}
	
	public void updateFilterList(String listStr) {
		this.setProperty(XMLElements.FILTERLIST, listStr);
	}
	



	//if the filter's status is incomplete, need to store this information for future reference
	public String getFilterListString() {
		return this.getPropertyValue(XMLElements.FILTERLIST);
	}

	public FilterOperation getFilterOperation() {
		return FilterOperation.valueFrom(this.getPropertyValue(XMLElements.OPERATION));
	}
	
	public String getSplitOnValue() {
		return this.getPropertyValue(XMLElements.SPLITON);
	}

	public String getRealName(int row) {
		if(row==-1 || (!McUtils.hasPartitionBinding(this.getName())))
			return this.getName();
		else {
			List<String> ptRefList = McUtils.extractPartitionReferences(this.getName());
			//if has partition references
			if(ptRefList.size()>1) {
				//assume only one partition for now
				String ptRef = ptRefList.get(1);
				String ptName = McUtils.getPartitionTableName(ptRef);
				PartitionTable pt = this.getParentConfig().getMart().getPartitionTableByName(ptName);
				if(pt == null) {
					Log.debug(Resources.get("INVALIDOBJECT",this.getName()));
					return this.getName();
				} else 
					return McUtils.replacePartitionReferences(pt, row, ptRefList);
			}
			return null;
		}
	}
	
	public String getRealDisplayName(int row) {
		if(row==-1 || (!McUtils.hasPartitionBinding(this.getName())))
			return this.getDisplayName();
		else {
			List<String> ptRefList = McUtils.extractPartitionReferences(this.getDisplayName());
			//if has partition references
			if(ptRefList.size()>1) {
				//assume only one partition for now
				String ptRef = ptRefList.get(1);
				String ptName = McUtils.getPartitionTableName(ptRef);
				PartitionTable pt = this.getParentConfig().getMart().getPartitionTableByName(ptName);
				if(pt == null) {
					Log.debug(Resources.get("INVALIDOBJECT",this.getDisplayName()));
					return this.getName();
				} else 
					return McUtils.replacePartitionReferences(pt, row, ptRefList);
			}
			return null;
		}
	}

	public void setPointedDatasetName(String value) {
		if(McUtils.isStringEmpty(this.getPropertyValue(XMLElements.POINTEDMART))) {
			JOptionPane.showMessageDialog(null,
				    "The pointedMart is empty.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);

			return;
		}
		if(McUtils.hasPartitionBinding(value)) {
			this.setProperty(XMLElements.POINTEDDATASET, value);
		}else {
			String[] pointedDsNames = value.split(",");
			//check if all datasets are valid
			Mart pointedMart = this.getParentConfig().getMart().getMartRegistry().getMartByName(this.getPropertyValue(XMLElements.POINTEDMART));
			
			for(String dsName : pointedDsNames) {
				if(pointedMart.getDatasetByName(dsName)==null) {
					JOptionPane.showMessageDialog(null,
						    "Dataset is not valid.",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);		
					return;
				}				
			}
		
			this.setProperty(XMLElements.POINTEDDATASET, value);
		}
	}

	public void setPointedMartName(String value) {
		//check parent config
		if(this.getParentConfig() == null)
			return;
		//check if the pointedMartName is valid
		if(this.getParentConfig().getMart().getMartRegistry().getMartByName(value)==null) {
			JOptionPane.showMessageDialog(null,
				    "Mart is not valid.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.setProperty(XMLElements.POINTEDMART, value);
	}

	


	/*
	 * filterpointer, filterList, importable
	 */
	public List<Filter> getReferences() {
		List<Filter> references = new ArrayList<Filter>();
		Mart currentMart = this.getParentConfig().getMart();
/*		for(Config config: currentMart.getConfigList()) {
			for(ElementList imp: config.getImportableList()) {
				for(Filter fil: imp.getFilterList()) {
					if(fil.equals(this)) {
						references.add(imp);
						break;
					}
				}
			}
		}*/
		for(Mart mt: currentMart.getMartRegistry().getMartList()) {
			for(Config config: mt.getConfigList()) {
				List<Filter> ftList = config.getFilters(new ArrayList<String>(), true, true);
				for(Filter tmpFilter :ftList) {
					if(tmpFilter.isPointer() && tmpFilter.getPointedFilter()!=null && tmpFilter.getPointedFilter().equals(this))
						references.add(tmpFilter);
					else if((tmpFilter.isFilterList()) && tmpFilter.getFilterList().contains(this)) {
						references.add(tmpFilter);
					}
				}
			}
		}
		return references;
	}

	public void setPointedConfigName(String value) {
		this.setProperty(XMLElements.POINTEDCONFIG, value);
	}

	public org.jdom.Element generateXml() {
		org.jdom.Element element = new org.jdom.Element(XMLElements.FILTER.toString());
		super.saveConfigurableProperties(element);
		element.setAttribute(XMLElements.NAME.toString(),this.getPropertyValue(XMLElements.NAME));
		MartConfiguratorUtils.addAttribute(element, XMLElements.DISPLAYNAME.toString(), this.getPropertyValue(XMLElements.DISPLAYNAME));
		MartConfiguratorUtils.addAttribute(element, XMLElements.INTERNALNAME.toString(), this.getPropertyValue(XMLElements.INTERNALNAME));
		MartConfiguratorUtils.addAttribute(element, XMLElements.DESCRIPTION.toString(), this.getPropertyValue(XMLElements.DESCRIPTION));

		MartConfiguratorUtils.addAttribute(element, XMLElements.MART.toString(), this.getParentConfig().getMart().getName());
		MartConfiguratorUtils.addAttribute(element, XMLElements.CONFIG.toString(), this.getParentConfig().getName());
		MartConfiguratorUtils.addAttribute(element, XMLElements.DEFAULT.toString(), this.getPropertyValue(XMLElements.DEFAULT));
		MartConfiguratorUtils.addAttribute(element, XMLElements.POINTER.toString(), this.isPointer().toString());
		if (this.isPointer()) {
			element.setAttribute(XMLElements.POINTEDDATASET.toString(),this.getPropertyValue(XMLElements.POINTEDDATASET));
			element.setAttribute(XMLElements.POINTEDFILTER.toString(),this.getPropertyValue(XMLElements.POINTEDFILTER));
			element.setAttribute(XMLElements.POINTEDMART.toString(),this.getPropertyValue(XMLElements.POINTEDMART));
			element.setAttribute(XMLElements.POINTEDCONFIG.toString(),this.getPropertyValue(XMLElements.POINTEDCONFIG));				
		}

		element.setAttribute(XMLElements.HIDE.toString(), this.isHidden() ? 
				XMLElements.TRUE_VALUE.toString() : XMLElements.FALSE_VALUE.toString());

		element.setAttribute(XMLElements.TYPE.toString(), this.getPropertyValue(XMLElements.TYPE));
		element.setAttribute(XMLElements.ATTRIBUTE.toString(), this.getPropertyValue(XMLElements.ATTRIBUTE));
		element.setAttribute(XMLElements.SPLITON.toString(), this.getPropertyValue(XMLElements.SPLITON));
		element.setAttribute(XMLElements.OPERATION.toString(), this.getPropertyValue(XMLElements.OPERATION));
		element.setAttribute(XMLElements.DATAFILE.toString(), this.getPropertyValue(XMLElements.DATAFILE));
		element.setAttribute(XMLElements.FILTERLIST.toString(), this.getPropertyValue(XMLElements.FILTERLIST));
		element.setAttribute(XMLElements.QUALIFIER.toString(), this.getPropertyValue(XMLElements.QUALIFIER));
		element.setAttribute(XMLElements.POINTER.toString(), this.isPointer().toString());

		element.setAttribute(XMLElements.REFCONTAINER.toString(), this.getPropertyValue(XMLElements.REFCONTAINER));
		


		
		
		if(this.getFilterType().equals(FilterType.BOOLEAN)) {
			element.setAttribute(XMLElements.ONLY.toString(), this.getPropertyValue(XMLElements.ONLY));
			element.setAttribute(XMLElements.EXCLUDED.toString(), this.getPropertyValue(XMLElements.EXCLUDED));
		}
		element.setAttribute(XMLElements.INUSERS.toString(),this.getPropertyValue(XMLElements.INUSERS));
		element.setAttribute(XMLElements.DEPENDSON.toString(),this.getPropertyValue(XMLElements.DEPENDSON));
		element.setAttribute(XMLElements.RDF.toString(),this.getPropertyValue(XMLElements.RDF));
		return element;
	}

	@Override
	@Deprecated
	public  void synchronizedFromXML() {
		this.setObjectStatus(ValidationStatus.VALID);
	}

	@Override
	public boolean isHidden() {
		return super.isHidden();
	}
	
	public boolean hasDropDown() {
		return (this.getFilterType() == FilterType.SINGLESELECT || 
				this.getFilterType() == FilterType.MULTISELECT);
	}
	
	public void updateDropDown(List<Dataset> dsList) throws MartBuilderException {
		if(!hasDropDown())
			return;
		if(this.isPointer())
			return;
		for(Dataset ds: dsList) {
			if(ds.hideOnMaster())
				continue;
			if(!this.inPartition(ds.getName()))
				continue;
			if(ds.getDataLinkType() == DataLinkType.URL) {
				//do nothing for now
			}else {
				List<FilterData> fdList = this.getOptionDataForDataset(ds);
				Options.getInstance().updateFilterOptionElement(this, ds, fdList);
			}
		}
	}
	
	public List<FilterData> getOptionDataForDataset(Dataset ds) throws MartBuilderException {
		List<FilterData> fdl = new ArrayList<FilterData>();
		if(ds.getDataLinkType() == DataLinkType.SOURCE ||
				ds.getDataLinkType() == DataLinkType.TARGET) {
			JdbcLinkObject jdbcObj = null;
			boolean materialized = ds.isMaterialized();
			if(materialized) 
				jdbcObj = ds.getDataLinkInfoForTarget().getJdbcLinkObject();
			else
				jdbcObj = ds.getDataLinkInfoForSource().getJdbcLinkObject();
			
			String tableName = null;
			String colName = null;
			
			if(this.getDatasetTable() == null || this.getDatasetColumn()==null)
				return fdl;

			//get the tablename and columnname
			if(materialized) {
				tableName = this.getDatasetTable().getName(ds.getName());
				colName = this.getDatasetColumn().getName();
			} else {
				SourceColumn sc = this.getDatasetColumn().getSourceColumn();
				if(sc == null)
					return fdl;
				colName = sc.getName();
				tableName = sc.getTable().getName();
			}
			
			StringBuffer sqlBuilder = new StringBuffer("select distinct ");			
			sqlBuilder.append(colName+" from "+tableName);
			
			List<Map<String,String>> rs = ConnectionPool.Instance.query(jdbcObj, sqlBuilder.toString());
			for(Map<String,String> mapItem: rs) {
				String value = (String)mapItem.get(colName);
				if(McUtils.isStringEmpty(value) || value.equals("null") || value.equals("NULL"))
					continue;
				FilterData fd = new FilterData(value,value,false);
				fdl.add(fd);
			}
			Collections.sort(fdl);
			return fdl;
		}
		else
			return fdl;
		//return fdl;
	}

	public boolean inUser(String user, Collection<String> dss) {
		String userStr = this.getPropertyValue(XMLElements.INUSERS);
		if(McUtils.hasPartitionBinding(this.getPropertyValue(XMLElements.INUSERS)) && dss!=null && dss.size()>0) {			
			PartitionTable pt = this.getParentConfig().getMart().getSchemaPartitionTable();
			
			for(String ds: dss) {
				int row = pt.getRowNumberByDatasetName(ds);
				String tmpStr = McUtils.getRealName(pt, row, userStr).trim();
				String[] users = tmpStr.split(",");
				if(!Arrays.asList(users).contains(user))
					return false;
			}
			return true;
		} else {
			userStr = userStr.trim();
			if("".equals(userStr))
				userStr = "anonymous,privileged";
			String[] users = userStr.split(",");
			return Arrays.asList(users).contains(user);
		}
	}

	/**
	 * create a pointer of myself
	 * @return
	 */
	public Filter createPointer() {
		org.jdom.Element e = this.generateXml();
		Filter f = new Filter(e);
		f.setPointer(true);
		
		return f;
	}

	/**
	 * called by reflection
	 * @return
	 */
	public List<String> getMartsDropDown() {
		List<String> result = new ArrayList<String>();
		if(!this.isPointer())
			return result;
		List<Mart> martList = this.getParentConfig().getMart().getMartRegistry().getMartList();
		for(Mart item: martList)
			result.add(item.getName());
		return result;
	}

	public Filter cloneMyself() {
		org.jdom.Element e = this.generateXml();
		return new Filter(e);
	}

	public void clearFilterList() {
		this.setProperty(XMLElements.FILTERLIST, "");
	}

	@Override
	public boolean isValid() {
		ValidationStatus result = ValidationStatus.VALID;
		String errorMessage = null;
		//ignore validation if is hidden
		/*if(this.isHidden())
			return true;*/
		if(!this.getParentConfig().isMasterConfig() && 
				this.getParentConfig().getMart().getMasterConfig().getFilterByName(this.getName(), null) == null)
		{
			this.setObjectStatus(ValidationStatus.INVALID);
			this.setProperty(XMLElements.ERROR, ErrorMessage.get("10005"));
			return false;
		}
		if(this.isPointer()) {
			if(this.getPointedFilter()== null || !this.getPointedFilter().isValid()) {
				this.setObjectStatus(ValidationStatus.INVALID);
				this.setProperty(XMLElements.ERROR, ErrorMessage.get("10004"));
				return false;
			}
			if(!McUtils.hasLink(getParentConfig(), getPointedConfing())) {
				this.setObjectStatus(ValidationStatus.INVALID);
				this.setProperty(XMLElements.ERROR, ErrorMessage.get("10004"));
				return false;
			}
		} else if(this.isFilterList()) {
			//it is valid if one of the filter in the list is valid
			boolean b = false;
			for(Filter fil: this.getFilterList()) {
				if(fil.isValid())
					b = true;
			}
			if(!b) {
				this.setObjectStatus(ValidationStatus.INVALID);
				this.setProperty(XMLElements.ERROR, ErrorMessage.get("10007"));
				return false;
			}
		} else {
			//normal filter
			if(this.getAttribute() == null || !this.getAttribute().isValid()) {
				this.setObjectStatus(ValidationStatus.INVALID);
				this.setProperty(XMLElements.ERROR,ErrorMessage.get("10009"));
				return false;
			}
			
			//check for qualifiers
			if(this.getQualifier() == null){
				this.setProperty(XMLElements.ERROR,ErrorMessage.get("10010"));
				this.setObjectStatus(ValidationStatus.INVALID);
				return false;
			}

		}				
		this.setObjectStatus(ValidationStatus.VALID);
		this.setProperty(XMLElements.ERROR, "");
		return true;
	}

	@Override
	public String getDisplayName() {
		if(this.isPointer() && this.getPointedFilter()!=null)
			return this.getPointedFilter().getDisplayName();
		else
			return super.getDisplayName();
	}

}
