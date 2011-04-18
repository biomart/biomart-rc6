package org.biomart.objects.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biomart.api.enums.Operation;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.DisplayNameComparator;
import org.biomart.common.utils.PartitionUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.object.FilterData;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.Validation;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.processors.ProcessorGroup;
import org.jdom.Element;


public class MartPointer extends MartConfiguratorObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<ProcessorGroup> processorGroupList;
	private Mart mart;
	private Operation queryOperation;
	private Config config;
	private List<UserGroup> inUsers;
	//attributes that in MartPointer but not in Mart
	private boolean independentQuery;
	
	
	public MartPointer(Config config, String name) {
		super(name);
		this.setConfig(config);
		this.mart = config.getMart();
		this.setProperty(XMLElements.MART, mart.getName());
		this.setNodeType(McNodeType.MARTPOINTER);
		this.processorGroupList = new ArrayList<ProcessorGroup>();
		this.inUsers = new ArrayList<UserGroup>();
		//set default values
		this.independentQuery = false;
		this.setOperation(Operation.SINGLESELECT);
	}
	
	public MartPointer(Element element) {
		super(element);
		this.setNodeType(McNodeType.MARTPOINTER);
		this.processorGroupList = new ArrayList<ProcessorGroup>();
		this.inUsers = new ArrayList<UserGroup>();

		//processgroup
		@SuppressWarnings("unchecked")
		List<Element> pgElementList = element.getChildren(XMLElements.PROCESSORGROUP.toString());
		for(Element pgElement: pgElementList) {
			ProcessorGroup pg = new ProcessorGroup(pgElement);
			this.addProcessorGroup(pg);
		}
	}
	
	public List<ProcessorGroup> getProcessorGroupList() {
		return this.processorGroupList;
	}
	
	public Element generateXml() {
		Element element = new Element(XMLElements.MARTPOINTER.toString());
		super.saveConfigurableProperties(element);
		element.setAttribute(XMLElements.NAME.toString(), this.getName());
		element.setAttribute(XMLElements.INTERNALNAME.toString(), this.getInternalName());
		element.setAttribute(XMLElements.DISPLAYNAME.toString(), this.getDisplayName());
		element.setAttribute(XMLElements.DESCRIPTION.toString(), this.getDescription());
		
		element.setAttribute(XMLElements.MART.toString(), this.mart.getName());
		element.setAttribute(XMLElements.CONFIG.toString(),this.config.getName());
		element.setAttribute(XMLElements.OPERATION.toString(),this.queryOperation.toString());
		element.setAttribute(XMLElements.ICON.toString(),this.getPropertyValue(XMLElements.ICON));
		element.setAttribute(XMLElements.INDEPENDENTQUERYING.toString(),this.independentQuery?
				XMLElements.TRUE_VALUE.toString():XMLElements.FALSE_VALUE.toString());
		element.setAttribute(XMLElements.INUSERS.toString(), McUtils.listToStr(this.inUsers, ","));

		for(ProcessorGroup pg: this.processorGroupList) {
			element.addContent(pg.generateXml());
		}
		
		return element;
	}
	
	public void addProcessorGroup(ProcessorGroup pg) {
		this.processorGroupList.add(pg);
		pg.setParent(this);
	}
		
	public Mart getMart() {
		return this.mart;
	}
	
	public void setIconName(String value) {
		this.setProperty(XMLElements.ICON, value);
	}
	
	public String getIconName() {
		return this.getPropertyValue(XMLElements.ICON);
	}
	
	public void setOperation(Operation o) {
		this.queryOperation = o;
		this.setProperty(XMLElements.OPERATION, o.toString());
	}
	
	public Operation getOperation() {
		return this.queryOperation;
	}
	
		
	public void setMart(Mart mart) {
		this.mart = mart;
		this.setProperty(XMLElements.MART, mart.getName());
	}
	
	public String getGroupName() {
		return this.getPropertyValue(XMLElements.GROUP);
	}
	
	public void setGroupName(String groupName) {
		this.setProperty(XMLElements.GROUP, groupName);
	}
	
 	public void setConfig(String configName) {
		this.config = mart.getConfigByName(configName);
		if(this.config!=null)
			this.setProperty(XMLElements.CONFIG, configName);
		else
			this.setProperty(XMLElements.CONFIG, "");
	}
	
	public void setConfig(Config config) {
		this.config = config;
		this.setProperty(XMLElements.CONFIG,config.getName());
	}
	
	public Config getConfig() {
		return this.config;
	}
	
	public GuiContainer getGuiContainer() {
		return (GuiContainer) this.parent;
	}

	
	/**
	 * return the datasetlist in the config
	 * @return the non-hidden dataset list
	 */
	public List<Dataset> getDatasetList(boolean includeHiddenDs) {
		List<Dataset> result = new ArrayList<Dataset>();
		Config config = this.getConfig();
		String hidecol = config.getPropertyValue(XMLElements.DATASETHIDEVALUE);
		if(McUtils.isStringEmpty(hidecol))
			hidecol = ""+PartitionUtils.HIDE;
		int col = McUtils.getPartitionColumnValue(hidecol);
		for(int row = 0; row< this.mart.getSchemaPartitionTable().getTotalRows(); row ++) {
			boolean b = Boolean.parseBoolean(this.mart.getSchemaPartitionTable().getValue(row, col));
			if(!b || includeHiddenDs) {
				String dsName = this.mart.getSchemaPartitionTable().getValue(row, PartitionUtils.DATASETNAME);
				result.add(this.mart.getDatasetByName(dsName));
			}
		}
		//reorder by displayname
		//Collections.sort(result, new DisplayNameComparator());
		return result;
	}
	
	public List<FilterData> getFilterDataList(String value, Collection<String> datasets, String subFilter) {
		List<FilterData> fds = new ArrayList<FilterData>();
		Filter subFil = this.config.getFilterByName(subFilter, datasets);
		if(subFil == null)
			return fds;
		String parentFilName = subFil.getPropertyValue(XMLElements.DEPENDSON);
		if(McUtils.isStringEmpty(parentFilName))
			return fds;
		Filter pfilter = this.config.getFilterByName(parentFilName, datasets);
		if(pfilter==null) 
			return fds;
				
		return McUtils.getSubFilterData(this.getConfig(),pfilter, value, datasets, subFil);
	}


	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;
		else if(!(o instanceof MartPointer)) {
			return false;
		} else if(((MartPointer)o).getName().equals(this.getName()) && 
				((MartPointer)o).getMart().equals(this.getMart()) &&
				((MartPointer)o).getConfig().equals(this.getConfig()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	public boolean getIndependentQuery() {
		return this.independentQuery;
	}
	
	public void setIndependentQuery(boolean b) {
		this.independentQuery = b;
	}

	@Override
	public void synchronizedFromXML() {
		//set mart
		MartRegistry mr = (MartRegistry)this.getGuiContainer().getRootGuiContainer().getParent().getParent();
		Mart mart = mr.getMartByName(this.getPropertyValue(XMLElements.MART));
		if(mart == null) {
			this.setObjectStatus(ValidationStatus.INVALID);
			return;
		}
		this.setMart(mart);
		
		Config con = mart.getConfigByName(this.getPropertyValue(XMLElements.CONFIG));
		if(con == null) {
			this.setObjectStatus(ValidationStatus.INVALID);
			return;			
		}
		this.setConfig(con);
		this.queryOperation = Operation.valueFrom(this.getPropertyValue(XMLElements.OPERATION));
		this.independentQuery = Boolean.parseBoolean(this.getPropertyValue(XMLElements.INDEPENDENTQUERYING));

		String inUserStr = this.getPropertyValue(XMLElements.INUSERS);
		String[] _inUser = inUserStr.split(",");
		for(String userStr: _inUser) {
			UserGroup user = mr.getPortal().getUsers().getUserGroupByName(userStr);
			if(user == null)
				this.setObjectStatus(ValidationStatus.INVALID);
			else
				this.addUser(user);
		}
				
		this.setObjectStatus(ValidationStatus.VALID);
	}
	
	public ValidationStatus synchronizeSubComponentFromXML() {

		//processorgroup
		for(ProcessorGroup pg: this.processorGroupList) {
			pg.synchronizedFromXML();
		}
		return ValidationStatus.VALID;
	}

	public void setPublic(boolean b) {
		//find the public user
/*		Portal portal = this.findPortal();
		List<User> pubUsers = portal.getUsers().getPublicUsers();
		for(User user: pubUsers) {
			user.addMartPointer(this);
		}*/
	}
	
	public Portal findPortal() {
		MartConfiguratorObject parent = this.getParent();
		if(parent == null)
			return null;
		while(parent instanceof GuiContainer) {
			parent = parent.getParent();
		}
		return (Portal)parent;
	}
	
	public void addUser(UserGroup user) {
		if(!this.inUsers.contains(user)) {
			this.inUsers.add(user);
			this.setProperty(XMLElements.INUSERS, McUtils.listToStr(this.inUsers, ","));
		}
	}
	
	public void removeUser(UserGroup user) {
		this.inUsers.remove(user);
		this.setProperty(XMLElements.INUSERS, McUtils.listToStr(this.inUsers, ","));
	}
	
	public boolean isActivatedInUser(UserGroup user) {
		if(user == null || user.isHidden()) {
			return false;
		}
		if(!this.inUsers.contains(user))
			return false;
		//check parent guicontainer till root
		boolean check = true;
		GuiContainer parent = this.getGuiContainer();
		
		while(check && !parent.isRoot()) {
			if(!parent.isActivatedInUser(user))
				check = false;
			parent = (GuiContainer)parent.getParent();
		}
		return check;
	}
	
}