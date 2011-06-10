package org.biomart.configurator.jdomUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.utils.McEventBus;
import org.biomart.common.utils.PartitionUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.controller.MartController;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.model.object.ObjectCopy;
import org.biomart.configurator.update.UpdateMart;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.ComponentStatus;
import org.biomart.configurator.utils.type.DatasetTableType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.McEventProperty;
import org.biomart.configurator.utils.type.PortableType;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.configurator.utils.type.PartitionType;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.gui.dialogs.AddPseudoAttributeDialog;
import org.biomart.configurator.view.gui.dialogs.AddAttributeListDialog;
import org.biomart.configurator.view.gui.dialogs.AddDsToMartPointerDialog;
import org.biomart.configurator.view.gui.dialogs.AddFilterListDialog;
import org.biomart.configurator.view.gui.dialogs.AddMartPointerDialog;
import org.biomart.configurator.view.gui.dialogs.AddProcessorDialog;
import org.biomart.configurator.view.gui.dialogs.CreateLinkIndexDialog;
import org.biomart.configurator.view.idwViews.McViewPortal;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.objects.enums.FilterOperation;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Column;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.DatasetColumn;
import org.biomart.objects.objects.DatasetTable;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.ElementList;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.ForeignKey;
import org.biomart.objects.objects.Key;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.PrimaryKey;
import org.biomart.objects.objects.Relation;
import org.biomart.objects.objects.RelationTarget;
import org.biomart.objects.objects.SourceSchema;
import org.biomart.objects.objects.Table;
import org.biomart.objects.portal.GuiContainer;
import org.biomart.objects.portal.LinkIndex;
import org.biomart.objects.portal.LinkIndices;
import org.biomart.objects.portal.MartPointer;
import org.biomart.objects.portal.Portal;
import org.biomart.objects.portal.UserGroup;
import org.biomart.objects.portal.Users;
import org.biomart.processors.Processor;
import org.biomart.processors.ProcessorGroup;

public class McTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//save a copy of parent dialog in root node
	private JDialog parent = null;
	
    public McTreeNode(MartConfiguratorObject userObject) {
    	super(userObject);
    }
    
    public void setParentDialog(JDialog parent){
    	this.parent = parent;
    }
    
    public JDialog getParentDialog(){
    	return this.parent;
    }
    
    public MartConfiguratorObject getObject() {
    	return (MartConfiguratorObject)this.userObject;
    }
       
 
    public Config createNaiveConfig(Mart mart, String configName) {
    	Config config = new Config(configName);
    	mart.addConfig(config);
    	config.setProperty(XMLElements.DATASETDISPLAYNAME, "(p0c"+PartitionUtils.DISPLAYNAME+")");
    	config.setProperty(XMLElements.DATASETHIDEVALUE, "(p0c"+PartitionUtils.HIDE+")");
    	//martController.addConfig(configObject);
    	//TODO should combine the creation of two
    	//add root container object
    	Container tmpContainer = new Container("root");
    	config.addRootContainer(tmpContainer);
      	//add attribute container object
    	Container attributeRootContainer = new Container(XMLElements.ATTRIBUTE.toString());
    	tmpContainer.addContainer(attributeRootContainer);
     	//add filter container object
    	Container filterRootContainer = new Container(XMLElements.FILTER.toString());
    	tmpContainer.addContainer(filterRootContainer);
    	//need to order the DataSetTable 
    	List<DatasetTable> mainList = new ArrayList<DatasetTable>();
    	List<DatasetTable> subList = new ArrayList<DatasetTable>();
    	List<DatasetTable> dmList = new ArrayList<DatasetTable>();
    	for(Iterator<DatasetTable> it = mart.getDatasetTables().iterator(); it.hasNext(); ) {
    		DatasetTable dsTable = (DatasetTable)it.next();
    		if(dsTable.getType().equals(DatasetTableType.MAIN))
    			mainList.add(dsTable);
    		else if(dsTable.getType().equals(DatasetTableType.MAIN_SUBCLASS))
    			subList.add(dsTable);
    		else
    			dmList.add(dsTable);
    	}

    	//order sublist
    	while(subList.size()>0) {
    		DatasetTable lastDst = mainList.get(mainList.size()-1);
    		//make sure the loop ends
    		boolean found = false;
    		for(DatasetTable dst:subList) {
    			if(lastDst.equals(dst.getParentMainTable())) {
    				mainList.add(dst);
    				subList.remove(dst);
    				found = true;
    				break;
    			}
    		}
    		if(!found)
    			break;
    		//should not go here, hack TODO check
    	//	subList.remove(lastDst);
    	}
    	//only keep the last sub main, since it includes all columns
    	while(mainList.size()>1) 
    		mainList.remove(0);
    	mainList.addAll(dmList);

    	//now mainList should have all tables with main tables in the begining
     	for (DatasetTable dsTable:mainList) {
     		//add filter and attribute container for each dataset table
     		String tbName = dsTable.getName();

     		Container attributeContainer = new Container(tbName+Resources.get("ATTRIBUTESUFFIX"));
     		attributeRootContainer.addContainer(attributeContainer);
     		
    		Container filterContainer = new Container(tbName+Resources.get("FILTERSUFFIX"));
    		filterRootContainer.addContainer(filterContainer);

     		this.doNaiveTable(config, dsTable, attributeContainer, filterContainer);
    	}
    	return config;
    }
    

    private void doNaiveTable(Config mart, DatasetTable dst, Container attributeContainer,  Container filterContainer) {
		for(final Iterator<Column> ci=dst.getColumnList().iterator(); ci.hasNext();) {
			DatasetColumn col =(DatasetColumn) ci.next();
			//check name
			String attName = McUtils.getUniqueAttributeName(mart, dst.getName()+Resources.get("columnnameSubSep")+col.getName());
			Attribute attribute = new Attribute(col,attName);
			//use the column name as display name and capitalize first letter
			String displayname = col.getName();
			displayname = (displayname.substring(0, 1).toUpperCase() + displayname.substring(1)).replace("_", " ");
			attribute.setDisplayName(displayname);
			String filterName = McGuiUtils.INSTANCE.getUniqueFilterName(mart, attName);
			Filter filter = new Filter(attribute, filterName);
			filter.setDisplayName(displayname);
			attributeContainer.addAttribute(attribute);
			filterContainer.addFilter(filter);	
		}
    }
        
    public void addMartNodes(List<Mart> marts) {
    	for(Mart mart: marts) {
    		if(mart.isHidden() && "1".equals(Settings.getProperty("hidemaskedcomponent")))
    			continue;
    		McTreeNode martNode = new McTreeNode(mart);
    		this.add(martNode);
    		martNode.synchronizeMart();
    	}
    }
        
    private void addContainerNode(McTreeNode currentNode, Container container) {
    	for(Container subContainer: container.getContainerList()) {
    		if(subContainer.isHidden() && "1".equals(Settings.getProperty("hidemaskedcomponent")))
    			continue;
    		McTreeNode conNode = new McTreeNode(subContainer);
    		currentNode.add(conNode);
    		addContainerNode(conNode, subContainer);
    	}
    	
    	for(Attribute attribute: container.getAttributeList()) {
    		if(attribute.isHidden() && "1".equals(Settings.getProperty("hidemaskedcomponent")))
    			continue;
    		McTreeNode attNode = new McTreeNode(attribute);
    		currentNode.add(attNode);
    	}
    	
    	for(Filter filter: container.getFilterList()) {
    		if(filter.isHidden() && "1".equals(Settings.getProperty("hidemaskedcomponent")))
    			continue;
    		McTreeNode filterNode = new McTreeNode(filter);
    		currentNode.add(filterNode);
    	}
    }

    public void addPortalNode(Portal portal) {
    	McTreeNode portalNode =  new McTreeNode(portal);
	    this.add(portalNode);
    	
    	//add users
    	Users users = portal.getUsers();
    	if(users!=null) {
	    	McTreeNode usersNode = new McTreeNode(users);
	    	portalNode.add(usersNode);
	    	for(UserGroup user: users.getUserList()) {
	    		McTreeNode userNode = new McTreeNode(user);
	    		usersNode.add(userNode);
	    	}
    	}
    	LinkIndices linkIndexes = portal.getLinkIndices();
    	if(linkIndexes!=null) {
	    	McTreeNode linkIndexesNode = new McTreeNode(linkIndexes);
	    	portalNode.add(linkIndexesNode);
	    	for(LinkIndex linkIndex: linkIndexes.getLinkIndexList()) {
	    		McTreeNode linkIndexNode = new McTreeNode(linkIndex);
	    		linkIndexesNode.add(linkIndexNode);
	    	}
    	}
    	//root guiContainer
    	GuiContainer rootGuiContainer = portal.getRootGuiContainer();
    	McTreeNode rootGuiContainerNode = new McTreeNode(rootGuiContainer);
    	portalNode.add(rootGuiContainerNode);
    	//add subcontainer
    	this.addSubGuiContainer(rootGuiContainer, rootGuiContainerNode);
    }
    
    private void addSubGuiContainer(GuiContainer guiContainer, McTreeNode currentNode) {
    	for(GuiContainer subGuiContainer: guiContainer.getGuiContainerList()) {
    		McTreeNode subGCNode = new McTreeNode(subGuiContainer);
    		currentNode.add(subGCNode);
    		this.addSubGuiContainer(subGuiContainer, subGCNode);
    	}
    	
    	for(MartPointer martPointer: guiContainer.getMartPointerList()) {
    		McTreeNode martPointNode = new McTreeNode(martPointer);
    		currentNode.add(martPointNode);

    		//add processorgroups
    		for(ProcessorGroup pg: martPointer.getProcessorGroupList()) {
        		McTreeNode processorsNode = new McTreeNode(pg);
        		martPointNode.add(processorsNode);
        		//add processes
        		List<Processor> processorList = pg.getProcessorList();
        		for(Processor processor: processorList) {
        			McTreeNode processorNode = new McTreeNode(processor);
        			processorsNode.add(processorNode);
        		}   			
    		}
    	}
    }

    public int hashCode() {
    	return this.userObject.hashCode();
    }
    
    public boolean equals(Object o) {
		if (this==o) {
			return true;
		}else if((o==null) || (o.getClass()!= this.getClass())) {
			return false;
		}
		return this.getObject().equals(((McTreeNode)o).getObject());		
    }

    
    /**
     * copy itself and store it in the McViewSchema object
     */
    public void copy(List<McTreeNode> nodeList) {
    	List<ObjectCopy> ocList = new ArrayList<ObjectCopy>();
    	for(McTreeNode tmpNode: nodeList) {
			ObjectCopy object = new ObjectCopy(tmpNode,0);
			ocList.add(object);
    	}
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).setObjectCopies(ocList);
    }
    
    /**
     * cut itself and store it in the McViewSchema object
     */
    public void cut(List<McTreeNode> nodeList) {
    	List<ObjectCopy> ocList = new ArrayList<ObjectCopy>();
    	for(McTreeNode tmpNode: nodeList) {
			ObjectCopy object = new ObjectCopy(tmpNode,1);			
			ocList.add(object);
    	}
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).setObjectCopies(ocList);
   }
    
    /**
     * paste the objects from the memory. if cut, the object memory will be clear.
     * @return
     */
    public boolean paste(MartConfigTree tree) {
    	//validate if the paste operation is good
    	McViewPortal schemaView = (McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL);
    	//check if the parent is a container
    	if(!this.getObject().getNodeType().equals(McNodeType.CONTAINER)) {
			JOptionPane.showMessageDialog(schemaView,
					"cannot copy");
			return false;
    	}
    	//check if the objectcopy is null
    	List<ObjectCopy> ocList = schemaView.getObjectCopies();
    	if(ocList.isEmpty())
    		return false;
    	Container targetContainer = (Container)this.getObject();

    	Config targetConfig = targetContainer.getParentConfig();   	
    	boolean isCrossConfig = false;
    	//check all
    	for(ObjectCopy oc: ocList) {
    		if(!((Element)oc.getObject()).getParentConfig().equals(targetConfig)) {
    			isCrossConfig = true;
    			break;
    		}
    	}
    	//cannot do cut/paste cross config
    	if(isCrossConfig && !ocList.get(0).isCopy()) {
			JOptionPane.showMessageDialog(schemaView,
					"cannot cut and paste between configs ");
			return false;    		
    	}
    	
    	
    	//do the paste
    	//if cut, needs to remove the old one and put it in new container, also clean the ObjectCopy
    	//needs to do a deep clone
    	if(ocList.get(0).isCopy()) {
    		List<MartConfiguratorObject> objectList = new ArrayList<MartConfiguratorObject>();
    		for(ObjectCopy oc:ocList) {
    			objectList.add(oc.getObject());
    		}
    		return this.doCopyPaste(tree,objectList,targetContainer, isCrossConfig);
    	}else {
    		Set<McTreeNode> parentSet = new HashSet<McTreeNode>();
    		for(ObjectCopy oc: ocList) {
	    		//remove the tree node and paste it to the new container
	    		//remove the object copy from schemaview
	    		McTreeNode cuttedNode = oc.getSourceNode();
	    		McTreeNode oldParent = (McTreeNode)cuttedNode.getParent();
	    		parentSet.add(oldParent);
	    		cuttedNode.removeFromParent();
		    	if(cuttedNode.getObject().getNodeType().equals(McNodeType.ATTRIBUTE)) {
		    		((Container)oldParent.getObject()).removeAttribute((Attribute)cuttedNode.getObject());
		    		targetContainer.addAttribute((Attribute)cuttedNode.getObject());
		    	}
		    	else {
		    		((Container)oldParent.getObject()).removeFilter((Filter)cuttedNode.getObject());
		    		targetContainer.addFilter((Filter)cuttedNode.getObject());
		    	}
    		}
	    	//synchronize new parent node
	    	this.synchronizeNode();
	    	for(McTreeNode changedParent: parentSet)
	    		((McTreeModel)tree.getModel()).nodeStructureChanged(changedParent);	 
	    	((McTreeModel)tree.getModel()).nodeStructureChanged(this);	    	
			//((McTreeModel)target.getModel()).insertNodeInto(draggedNode,newParentNode,newParentNode.getChildCount());
			TreePath treePath = new TreePath(this.getPath());
			tree.scrollPathToVisible(treePath);
			tree.setSelectionPath(treePath);

    		schemaView.getObjectCopies().clear();
			//McGuiUtils.refreshGui(this);
    	}
    	return true;    	
    }
    
    /**
     * if isCrossConfig=true, the new object will be set as a pointer
     * @param mcObjectList
     * @param targetContainer
     * @param isCrossConfig
     * @return
     */
    private boolean doCopyPaste(MartConfigTree tree, List<MartConfiguratorObject> mcObjectList, Container targetContainer,  boolean isCrossConfig) {
    	for(MartConfiguratorObject oldObj: mcObjectList) {
    		MartConfiguratorObject newObj = this.deepClone(oldObj, isCrossConfig);
    		//add it to the object model and tree
    		if(newObj.getNodeType().equals(McNodeType.ATTRIBUTE))
    			targetContainer.addAttribute((Attribute)newObj);
    		else
    			targetContainer.addFilter((Filter)newObj);
    		McTreeNode newTreeNode = new McTreeNode(newObj);
    		this.add(newTreeNode);      		
    	}
		
		((McTreeModel)tree.getModel()).nodeStructureChanged(this);
		return true;
    }
    
    /**
     * create a new object including all objects under it recursively, if isCrossConfig, 
     * @param source
     * @param isCrossConfig
     * @return
     */
    private MartConfiguratorObject deepClone(MartConfiguratorObject source, boolean isCrossConfig) {
    	//source should be an attribute or filter
    	//get the next copy name
    	//this is a container
    	String sourceName = source.getName();
    	String tmpName = sourceName + Resources.get("replicateSuffix");
    	int i=1;
    	if(source.getNodeType().equals(McNodeType.ATTRIBUTE)) {
    		//get the next copy name
    		Attribute nextAtt = ((Element)source).getParentConfig().getRootContainer().getAttributeRecursively(tmpName);
    		//if the attribute already exist, change name by adding 1;
    		while(null!=nextAtt) {
    			tmpName = sourceName + Resources.get("replicateSuffix") + i++;
    			nextAtt = ((Element)source).getParentConfig().getRootContainer().getAttributeRecursively(tmpName);
    		}
    		//tmpName is name we want
    		if(isCrossConfig) {
    			nextAtt = new Attribute(tmpName,(Attribute)source);
    			//set all datasets by default
    			for(Dataset ds: ((Attribute)source).getParentConfig().getMart().getDatasetList()) {
    				nextAtt.addPointedDataset(ds);
    			}
    			//set pointeddataset, pointedmart, pointedconfig
    		}
    		else {
    			nextAtt = ((Attribute)source).cloneMyself();
    			nextAtt.setName(tmpName);
    			//nextAtt = new Attribute(((Attribute)source).getDataSetColumn(),tmpName);
    		}
    		nextAtt.setDisplayName(source.getDisplayName());
    		nextAtt.setObjectStatus(((Attribute)source).getObjectStatus());
    		//nextAtt.setConfig(currentConfig);
    		return nextAtt;
    	}else {
    		Filter nextFilter = ((Element)source).getParentConfig().getRootContainer().getFilterRecursively(tmpName);
    		//if the attribute already exist, change name by adding 1;
    		while(null!=nextFilter) {
    			tmpName = sourceName + Resources.get("replicateSuffix") + i++;
    			nextFilter = ((Element)source).getParentConfig().getRootContainer().getFilterRecursively(tmpName);
    		}
    		//tmpName is name we want
      		if(isCrossConfig) {
      			//do pointer
      			nextFilter = new Filter(((Filter)source).getFilterType(),tmpName);
          		nextFilter.setPointedElement((Filter)source);
          		nextFilter.setPointer(true);
          		//set pointeddataset, pointedmart, pointedconfig
          		for(Dataset ds: ((Filter)source).getParentConfig().getMart().getDatasetList()) {
    				nextFilter.addPointedDataset(ds);
    			}
      		}else {
      			nextFilter = ((Filter)source).cloneMyself();
      			nextFilter.setName(tmpName);
      			//just make a copy
      			/*if(sourceFilter.isPointer())
      				nextFilter.setPointedElement(sourceFilter.getPointedFilter());
      			nextFilter.setPointer(sourceFilter.isPointer());
      			nextFilter.setHideValue(sourceFilter.isHidden());
      			nextFilter.setQualifier(sourceFilter.getQualifier());
      			for(Filter subFilter: sourceFilter.getFilterList()) {
      				nextFilter.addFilter(subFilter);
      			}
      			nextFilter.setOnlyValue(sourceFilter.getOnlyValue());
      			nextFilter.setExcludedValue(sourceFilter.getExcludedValue());
      			if(sourceFilter.getAttribute()!=null) {
      				nextFilter.setAttribute(sourceFilter.getAttribute());
      			}*/
      		}

      		//nextFilter.setConfig(config);
      		nextFilter.setDisplayName(source.getDisplayName());
      		nextFilter.setObjectStatus(((Filter)source).getObjectStatus());
      		//nextFilter.setConfig(currentConfig);
      		return nextFilter;	
    	}
    }
  
    public boolean addGuiContainer() {
		String s = (String)JOptionPane.showInputDialog(null,"Input guicontainer name:",null);
		if(!McUtils.isStringEmpty(s)) {
			GuiContainer parent = (GuiContainer)this.getObject();
			GuiContainer gc = new GuiContainer(s);
			parent.addGuiContainer(gc);
			//add tree node
			McTreeNode gcNode = new McTreeNode(gc);
			this.add(gcNode);
			return true;
		}
    	return false;
    }
    
    public boolean addContainer(MartConfigTree tree) {
    	JDialog parentDlg = tree.getParentDialog();
		String s = (String)JOptionPane.showInputDialog(parentDlg,"Input container name:",null);
		if(s!=null && !s.trim().equals("")) {
			Container parent = (Container)this.getObject();
			Container c = new Container(s);
			parent.addContainer(c);
			//add tree node
			McTreeNode gcNode = new McTreeNode(c);
			this.add(gcNode);
			return true;
		}
    	return false;    	
    }
    
    public boolean addMartPointer() {
    	AddMartPointerDialog ampd = new AddMartPointerDialog();
    	MartPointer mp = ampd.getMartPointer();
    	if(mp!=null) {
    		GuiContainer parent = (GuiContainer) this.getObject();
    		parent.addMartPointer(mp);
    		//add tree node
    		McTreeNode mpNode = new McTreeNode(mp);
    		//createlinks
    		List<MartPointer> mpList = new ArrayList<MartPointer>();
    		mpList.add(mp);
    		//this.createLinks(parent, mpList);
    		this.add(mpNode);
    		return true;
    	}
    	return false;
    }
    
    public boolean addProcessor() {
    	ProcessorGroup pg = (ProcessorGroup)this.getObject().getParent();
    	AddProcessorDialog apd = new AddProcessorDialog(pg);
    	Processor processor = apd.getProcessor();
    	if(processor!=null) {
    		McTreeNode processNode = new McTreeNode(processor);
    		this.add(processNode);
    		return true;
    	}
    	return false;
    }
    
    public boolean setDefaultProcessor() {
    	ProcessorGroup ps = ((Processor)this.getObject()).getProcessors();
    	ps.setDefaultProcessor((Processor)this.getObject());
    	return true;
    }

    public McTreeNode findNode(MartConfiguratorObject mcObject) {
    	for(int i=0; i<this.getChildCount(); i++) {
    		McTreeNode node = (McTreeNode)this.getChildAt(i);
    		if(node.getObject().equals(mcObject)) 
    			return node;
    		else {
    			McTreeNode tmpNode = node.findNode(mcObject);
    			if(tmpNode!=null && tmpNode.getObject().equals(mcObject))
    				return tmpNode;
    		}
    	}
    	return null;
    }

   
    public List<Mart> groupMarts(String martName, List<Mart> martList) {
    	List<Mart> newMartList = new ArrayList<Mart>();
    	if(martList.isEmpty())
    		return newMartList;
    	MartRegistry registry = (MartRegistry)this.getObject();
    	DatasetTable centralTable = martList.get(0).getMainTable();
    	String centralTableName = centralTable.getName();
    	Mart newMart = new Mart(registry,martName,null);
    	boolean isNewMartVisible = false;
    	//create a new schema partition table
    	PartitionTable ptTable = new PartitionTable(newMart,PartitionType.SCHEMA);
    	//each existing mart as a partition row
    	for(Mart mart: martList) {
    		if(!mart.isHidden()) 
    			isNewMartVisible = true;
    		else
    			Log.debug("mart is hidden "+mart.getName());
    		//handle partition table
    		PartitionTable existingPt = mart.getSchemaPartitionTable();
    		//assume only one row for now TODO
    		String partitionValue = existingPt.getTable().get(0).get(PartitionUtils.DATASETNAME);
    		for(List<String> rowList : existingPt.getTable()) {
    			//add the visible, display name to it
    			//String visible = mart.isHidden()? XMLElements.FALSE_VALUE: XMLElements.TRUE_VALUE;
    			//rowList.add(rowList.size()-1,visible);
    			//rowList.add(rowList.size()-1,mart.getDisplayName());
    			ptTable.addNewRow(rowList);
    		}
    		
    		//construct dataset table
    		for(DatasetTable dst: mart.getDatasetTables()) {
    			//find if the datasetTable already exist
    			DatasetTable datasetTable = newMart.getTableByName(dst.getName());
    			if(datasetTable == null) {
    				datasetTable = new DatasetTable(newMart,dst.getName(),dst.getType());
    				newMart.addTable(datasetTable);
    			}
    			datasetTable.addInPartitions(partitionValue);
    			
    			//construct dataset column
    			for(Column column: dst.getColumnList()) {
    				DatasetColumn newColumn = datasetTable.getColumnByName(column.getName());
    				if(newColumn==null) {
    					newColumn = new DatasetColumn(datasetTable,column.getName());
    					datasetTable.addColumn(newColumn);
    				}
    				newColumn.addInPartitions(partitionValue);
    			}
    			
    			//pk, fk TODO pk,fk equals
    			if(datasetTable.getPrimaryKey()==null && dst.getPrimaryKey()!=null) {
    				PrimaryKey srcpk = dst.getPrimaryKey();
    				List<Column> srcColumns = srcpk.getColumns();
    				List<Column> dscs = new ArrayList<Column>();
    				for(Column col : srcColumns) {
    					dscs.add(datasetTable.getColumnByName(col.getName()));
    				}
    				PrimaryKey pk = new PrimaryKey(dscs);
    				datasetTable.setPrimaryKey(pk);
    			}
    			
    			for(ForeignKey srcFk: dst.getForeignKeys()){
    				List<Column> srcColumns = srcFk.getColumns();
    				List<Column> dscs = new ArrayList<Column>();
    				for(Column col: srcColumns) {
    					dscs.add(datasetTable.getColumnByName(col.getName()));
    				}
    				if(dscs.size() ==0)
    					continue;
    				ForeignKey fk = new ForeignKey(dscs);
    				//check if fk exist
    				boolean fkAlreadyExists = false;
    				for (ForeignKey candidateFk : datasetTable.getForeignKeys()) {
						if (candidateFk.equals(fk)) {
							fkAlreadyExists = true;
							break;
						}
    				}
    				if(!fkAlreadyExists) {
    					datasetTable.getForeignKeys().add(fk);
    				}
    			}   			   			
    		}
    		//relations
    		final List<DatasetTable> mainTables = new ArrayList<DatasetTable>();
    		mainTables.add(mart.getMainTable());
    		for (int i = 0; i < mainTables.size(); i++) {
    			final DatasetTable sourceMtable = mainTables.get(i);
    			final DatasetTable targetTable = (DatasetTable)newMart.getTableByName(sourceMtable.getName());
    			if(sourceMtable.getPrimaryKey()!=null) {
    				for (final Relation relation : sourceMtable.getPrimaryKey().getRelations()) {
    					final DatasetTable target = (DatasetTable) relation
    							.getManyKey().getTable();
    					//find the same keys in the target dataset
    					
						Key targetfk = null;
						Key targetsk = null;
						Key fk = relation.getFirstKey();
						Key sk = relation.getSecondKey();
						for(Key tfk:targetTable.getKeys()) {
							if(tfk.isKeyEquals(fk)) {
								targetfk = tfk;
								break;
							}
						}
						final DatasetTable secondTable = (DatasetTable)newMart.getTableByName(
								relation.getSecondKey().getTable().getName());
						for(Key tfk:secondTable.getKeys()) {
							if(tfk.isKeyEquals(sk)) {
								targetsk = tfk;
								break;
							}
						}
						
						if(targetfk!=null && targetsk!=null && !Relation.isRelationExist(targetfk, targetsk)) {
						
							try {
								Relation relObj = new RelationTarget(targetfk,targetsk,relation.getCardinality());
	
								relObj.setOriginalCardinality(relation.getOriginalCardinality());
								//targetfk.addRelation(relObj);
								//targetsk.addRelation(relObj);
								relObj.setStatus(ComponentStatus.INFERRED);
								if (target.getType().equals(DatasetTableType.MAIN_SUBCLASS)) {
									mainTables.add(target);
									relObj.setSubclassRelation(true,Relation.DATASET_WIDE);
								}									
							} catch (AssociationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ValidationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else if(target.getType().equals(DatasetTableType.MAIN_SUBCLASS)){ 
							//even we don't add the relation, still need to put the table in loop							
							mainTables.add(target);
						}
    					
    				}
    			}
    		}
    		//add config
    		for(Config config: mart.getConfigList()) {
    			if(newMart.getConfigList().isEmpty()) {
    				Container oldRootContainer = config.getRootContainer();
    				Config newConfig = new Config(newMart.getName());
    				newMart.addConfig(newConfig);
    				String configName = oldRootContainer.getName();
    				//if the configName has partition info, remove it
    				String[] tmpName = configName.split("_");
    				StringBuffer sb = new StringBuffer(tmpName[0]);
    				for(int k = 1; k<tmpName.length-1; k++) {
    					sb.append("_");
    					sb.append(tmpName[k]);
    				}
    				//create root container and merge root container
    				Container rootContainer = new Container(sb.toString());
    				newConfig.addRootContainer(rootContainer);
    				rootContainer.mergeContainer(oldRootContainer);
    				//merge importable/exportable
    				for(ElementList imps: config.getImportableList()) {
    					ElementList newImp = new ElementList(newConfig,imps.getName(),PortableType.IMPORTABLE);
    					newImp.addLinkVersion(mart.getName(), imps.getLinkVersion());
    					for(Filter filter: imps.getFilterList()) {
    						Filter newFilter = newConfig.getRootContainer().getFilterRecursively(filter.getName());
    						if(newFilter == null) {
    							Log.debug("merge importable: could not find filter "+filter.getName());
    							continue;
    						}
    						newImp.addFilter(newFilter);
    					}
    					newImp.setProperty(XMLElements.TYPE,imps.getPropertyValue(XMLElements.TYPE));
    					newConfig.addElementList(newImp);
    				}
    				for(ElementList exps: config.getExportableList()) {
    					ElementList newExp = new ElementList(newConfig,exps.getName(),PortableType.EXPORTABLE);
    					newExp.addLinkVersion(mart.getName(), exps.getLinkVersion());
    					for(Attribute attribute: exps.getAttributeList()) {
    						Attribute newAttribute = newConfig.getRootContainer().getAttributeRecursively(attribute.getName());
    						if(newAttribute == null) {
    							Log.debug("merge exportable: could not find attribute "+attribute.getName());
    							continue;
    						}
    						newExp.addAttribute(newAttribute);
    					}
    					newExp.setDefaultState(exps.isDefault());
    					newExp.setProperty(XMLElements.TYPE,exps.getPropertyValue(XMLElements.TYPE));
    					newConfig.addElementList(newExp);    					
    				}
    			}
    			else {
    				//merge config
    				Config newConfig = newMart.getConfigList().get(0);
    				//merge rootcontainer
    				Container newRootContainer = newConfig.getRootContainer();
    				Container oldRootContainer = config.getRootContainer();
    				newRootContainer.mergeContainer(oldRootContainer);
    				//merge importable/exportable
    				for(ElementList imps: config.getImportableList()) {
    					ElementList newImp = newConfig.getImportableByInternalName(imps.getName());
    					if(newImp==null) {
    						newImp = new ElementList(newConfig,imps.getName(),PortableType.IMPORTABLE);
        					for(Filter filter: imps.getFilterList()) {
        						Filter newFilter = newConfig.getRootContainer().getFilterRecursively(filter.getName());
        						if(newFilter == null) {
        							Log.debug("merge importable: could not find filter "+filter.getName());
        							continue;
        						}
        						newImp.addFilter(newFilter);
        					}
        					newConfig.addElementList(newImp);
    					}
    					newImp.addLinkVersion(mart.getName(), imps.getLinkVersion());
    				}
    				for(ElementList exps: config.getExportableList()) {
    					ElementList newExp = newConfig.getExportableByInternalName(exps.getName());
    					if(newExp==null) {
    						newExp = new ElementList(newConfig,exps.getName(),PortableType.EXPORTABLE);
        					for(Attribute attribute: exps.getAttributeList()) {
        						Attribute newAttribute = newConfig.getRootContainer().getAttributeRecursively(attribute.getName());
        						if(newAttribute == null) {
        							Log.debug("merge exportable: could not find attribute "+attribute.getName());
        							continue;
        						}
        						newExp.addAttribute(newAttribute);
        					}
        					newConfig.addElementList(newExp);    					
         				}
    					newExp.addLinkVersion(mart.getName(), exps.getLinkVersion());
    				}
    			}
    		}
    		//remove the old mart objects
    		registry.getMartList().remove(mart);
    	}
    	if(!isNewMartVisible)
    		newMart.setHideValue(true);
    	else
    		newMart.setHideValue(false);
    	newMart.setCentralTable(newMart.getTableByName(centralTableName));
    	newMart.addPartitionTable(ptTable);
    	newMartList.add(newMart);
    	
		//add tree nodes
//		this.addMartNodes(newMartList);
//		List<MartPointer> mpList = this.doNaivePortal(newMartList);
//		this.addPortalNodes(mpList);
		
		return newMartList;
    }


    /**
     * synchronized the treenode with object model
     */
    public void synchronizeNode() {
    	//remove all children, then recreate them according to the object model
    	this.removeAllChildren();
    	if(this.userObject instanceof Container) {
    		this.synchronizeContainer();
    	}else if(this.userObject instanceof GuiContainer) {
    		this.synchronizeGuiContainer();
    	}else if(this.userObject instanceof MartPointer) {
    		this.synchronizeMartPointer();
    	}else if(this.userObject instanceof Mart) {
    		this.synchronizeMart();
    	}else if(this.userObject instanceof Config) {
    		this.synchronizeConfig();
    	}
    }
    
    private void synchronizeMartPointer() {
    	MartPointer mp = (MartPointer)this.userObject;

		//add processorgroup
		for(ProcessorGroup pg: mp.getProcessorGroupList()) {
			McTreeNode pgNode = new McTreeNode(pg);
			this.add(pgNode);
			//add processor
			for(Processor p: pg.getProcessorList()) {
				McTreeNode pNode = new McTreeNode(p);
				pgNode.add(pNode);
			}
		}
    }
    
    private void synchronizeContainer() {
    	Container container = (Container)this.userObject;
    	for(Attribute attribute: container.getAttributeList()) {
    		if(attribute.isHidden() && "1".equals(Settings.getProperty("hidehiddenattribute"))) 
    			continue;
    		McTreeNode attNode = new McTreeNode(attribute);
    		this.add(attNode);	    	
    	}
    	
    	for(Filter filter: container.getFilterList()) {
    		if(filter.isHidden() && "1".equals(Settings.getProperty("hidehiddenfilter"))) 
    			continue;
    		McTreeNode filterNode = new McTreeNode(filter);
    		this.add(filterNode);	    	
    	}
    	
    	for(Container subContainer: container.getContainerList()) {
    		if(subContainer.isHidden() && "1".equals(Settings.getProperty("hidehiddencontainer"))) 
    			continue;
    		McTreeNode subConNode = new McTreeNode(subContainer);
    		this.add(subConNode);
    		this.addContainerNode(subConNode,subContainer);
    	}
    }
    
    private void synchronizeMart() {
    	Mart mart = (Mart)this.userObject;
		//add partitiontable
		for(PartitionTable pt: mart.getPartitionTableList()) {
			McTreeNode ptNode = new McTreeNode(pt);
			this.add(ptNode);
		}
		//add datasetTable
		Collection<DatasetTable> dstList = mart.getDatasetTables();
//		Collections.sort(dstList, new DisplayNameComparator());
		for(DatasetTable dst: dstList) {
			McTreeNode dsNode = new McTreeNode(dst);
			this.add(dsNode);
			//add column
			for(Column column:dst.getColumnList()) {
				McTreeNode colNode = new McTreeNode(column);
				dsNode.add(colNode);
			}
			//add keys
			if(dst.getPrimaryKey()!=null) {
				McTreeNode pkNode = new McTreeNode(dst.getPrimaryKey());
				dsNode.add(pkNode);
			}
			//fks
			Set<ForeignKey> fks = dst.getForeignKeys();
			for(ForeignKey fk: fks) {
				McTreeNode fkNode = new McTreeNode(fk);
				dsNode.add(fkNode);
			}
		}
		//add relation
		//order relation
		List<Relation> relationList = new ArrayList<Relation>(mart.getRelations());
		Collections.sort(relationList/*,new RelationComparator()*/); // uses Relation.compare()
		for(Relation dsRelation: relationList) {
			McTreeNode dsrNode = new McTreeNode(dsRelation);
			this.add(dsrNode);
		}
		//add config
		for(Config config: mart.getConfigList()) {
			if(config.isHidden() && "1".equals(Settings.getProperty("hidemaskedcomponent")))
				continue;
			McTreeNode configNode = new McTreeNode(config);
			this.add(configNode);
			//add portable
			for(ElementList imp: config.getImportableList()) {
				McTreeNode impNode = new McTreeNode(imp);
				configNode.add(impNode);
			}
			for(ElementList exp: config.getExportableList()) {
				McTreeNode expNode = new McTreeNode(exp);
				configNode.add(expNode);
			}

			//add rootContainer   			
			Container rootContainer = config.getRootContainer();
			McTreeNode rcNode = new McTreeNode(rootContainer);
			configNode.add(rcNode);
			//add containers, filters, attributes
			this.addContainerNode(rcNode, rootContainer);
		}
		//add source schema
		for(SourceSchema ss: mart.getIncludedSchemas()) {
			McTreeNode ssNode = new McTreeNode(ss);
			this.add(ssNode);
			//add table
			for(Table sourceTable: ss.getTables()) {
				McTreeNode stNode = new McTreeNode(sourceTable);
				ssNode.add(stNode);
				//add column
				for(Column column: sourceTable.getColumnList()) {
					McTreeNode columnNode = new McTreeNode(column);
					stNode.add(columnNode);
				}
			}
			//add relation
			for(Relation relation: ss.getRelations() ) {
				McTreeNode relationNode = new McTreeNode(relation);
				ssNode.add(relationNode);
			}
		}

    }
 
    private void synchronizeConfig() {
    	Config config = (Config)this.userObject;

		//add portable
		for(ElementList imp: config.getImportableList()) {
			McTreeNode impNode = new McTreeNode(imp);
			this.add(impNode);
		}
		for(ElementList exp: config.getExportableList()) {
			McTreeNode expNode = new McTreeNode(exp);
			this.add(expNode);
		}
		for(org.biomart.objects.objects.Link link: config.getLinkList()) {
			McTreeNode linkNode = new McTreeNode(link);
			this.add(linkNode);
		}
		for(org.biomart.objects.objects.RDFClass rdf: config.getRDFClasses()) {
			McTreeNode rdfNode = new McTreeNode(rdf);
			this.add(rdfNode);
		}

		//add rootContainer   			
		Container rootContainer = config.getRootContainer();
		McTreeNode rcNode = new McTreeNode(rootContainer);
		this.add(rcNode);
		//add containers, filters, attributes
		this.addContainerNode(rcNode, rootContainer);
		

    }

/*    private void addContainerNode(Container container) {
    	McTreeNode containerNode = new McTreeNode(container);
    	this.add(containerNode);
    	for(Attribute attribute: container.getAttributeList()) {
    		McTreeNode attNode = new McTreeNode(attribute);
    		containerNode.add(attNode);
    	}
    	for(Filter filter: container.getFilterList()) {
    		McTreeNode filterNode = new McTreeNode(filter);
    		containerNode.add(filterNode);
    	}
    	for(Container subContainer: container.getContainerList()) {
    		containerNode.addContainerNode(subContainer);
    	}    	
    }*/
    
    private void synchronizeGuiContainer() {
    	GuiContainer gc = (GuiContainer)this.userObject;
    	for(MartPointer mp: gc.getMartPointerList()) {
    		McTreeNode mpNode = new McTreeNode(mp);
    		this.add(mpNode);

    		//add processorgroup
    		for(ProcessorGroup pg: mp.getProcessorGroupList()) {
    			McTreeNode pgNode = new McTreeNode(pg);
    			mpNode.add(pgNode);
    			//add processor
    			for(Processor p: pg.getProcessorList()) {
    				McTreeNode pNode = new McTreeNode(p);
    				pgNode.add(pNode);
    			}
    		}
    	}
    	for(GuiContainer subgc: gc.getGuiContainerList()) {
    		this.addGuiContainerNode(subgc);
    	}
    }
    
    private void addGuiContainerNode(GuiContainer gc) {
    	McTreeNode gcNode = new McTreeNode(gc);
    	this.add(gcNode);
    	for(MartPointer mp: gc.getMartPointerList()) {
    		McTreeNode mpNode = new McTreeNode(mp);
    		gcNode.add(mpNode);

    		//add processorgroup
    		for(ProcessorGroup pg: mp.getProcessorGroupList()) {
    			McTreeNode pgNode = new McTreeNode(pg);
    			mpNode.add(pgNode);
    			//add processor
    			for(Processor p: pg.getProcessorList()) {
    				McTreeNode pNode = new McTreeNode(p);
    				pgNode.add(pNode);
    			}
    		}
    	}
    	for(GuiContainer subgc: gc.getGuiContainerList()) {
    		gcNode.addGuiContainerNode(subgc);
    	}
    }

    public boolean createLink() {
    	//CreateLinkDialog cld = new CreateLinkDialog((MartPointer)this.userObject);
    	
    	return false;
    }
    
    public void createLinkIndex() {
    	CreateLinkIndexDialog cld = new CreateLinkIndexDialog((Mart)this.userObject);
    	
    }

    public boolean createAttributeList() {
    	//this is a container
    	Container container = (Container)this.getUserObject();
    	JDialog parentDlg = ((McTreeNode)this.getRoot()).getParentDialog();
    	AddAttributeListDialog ald = new AddAttributeListDialog(container.getParentConfig(),parentDlg);  
    	Attribute al = ald.getCreatedAttributeList();
    	if(al!=null) {
    		Container c = (Container) this.userObject;
    		c.addAttribute(al);
    		//create all sub attributes if they don't exist in the config
    		Config parentConfig = c.getParentConfig();
    		if(!parentConfig.isMasterConfig()) {
    			Config masterConfig = parentConfig.getMart().getMasterConfig();
    			String listStr = al.getAttributeListString();
    			String[] atts = listStr.split(",");
    			for(String att: atts) {
    				if(null==parentConfig.getAttributeByName(att, new ArrayList<String>())){
    					Attribute attInSource = masterConfig.getAttributeByName(att, new ArrayList<String>());
    					if(attInSource!=null) {
    						Attribute copy = attInSource.cloneMyself();
    						c.addAttribute(copy);
    					}
    				}
    			}
    		}
    		McEventBus.getInstance().fire(McEventProperty.SYNC_NEW_LIST.toString(), al);
    		McTreeNode alTreeNode = new McTreeNode(al);
    		this.add(alTreeNode);
    		return true;
    	}
    	return false;
    }
    
    public boolean createFilterList() {
    	//this is a container
    	Container container = (Container)this.getUserObject();
    	JDialog parentDlg = ((McTreeNode)this.getRoot()).getParentDialog();
    	AddFilterListDialog ald = new AddFilterListDialog(container.getParentConfig(),parentDlg);  
    	Filter flFilter = ald.getCreatedFilterList();
    	if(flFilter!=null) {
    		Container c = (Container) this.userObject;
    		c.addFilter(flFilter);
    		//create all sub filters in they don't exist in the config
    		Config parentConfig = c.getParentConfig();
    		if(!parentConfig.isMasterConfig()) {
    			Config masterConfig = parentConfig.getMart().getMasterConfig();
    			String listStr = flFilter.getFilterListString();
    			String[] atts = listStr.split(",");
    			for(String att: atts) {
    				if(null == parentConfig.getFilterByName(att, new ArrayList<String>())) {
    					Filter filterInSource = masterConfig.getFilterByName(att, new ArrayList<String>());
    					if(filterInSource !=null) {
    						Filter copy = filterInSource.cloneMyself();
    						c.addFilter(copy);
    					}
    				}
    			}
    		}
    		McEventBus.getInstance().fire(McEventProperty.SYNC_NEW_LIST.toString(), flFilter);
     		McTreeNode alTreeNode = new McTreeNode(flFilter);
    		this.add(alTreeNode);
    		return true;
    	}
    	return false;
    }

    public boolean createAttribute() {
    	//this is a container
    	Container container = (Container)this.getUserObject();
    	JDialog parentDlg = ((McTreeNode)this.getRoot()).getParentDialog();
    	AddPseudoAttributeDialog aad = new AddPseudoAttributeDialog(container.getParentConfig(),parentDlg);
    	Attribute attribute = aad.getCreatedAttribute();
    	if(attribute!=null) {
    		((Container) this.userObject).addAttribute(attribute);
    		McEventBus.getInstance().fire(McEventProperty.SYNC_NEW_PSEUDO.toString(), attribute);
    		McTreeNode alTreeNode = new McTreeNode(attribute);
    		this.add(alTreeNode);
    		return true;
    	}
    	return false;
    }

    public boolean replaceMart(Mart oldMart, Mart newMart) {
    	//remove the oldmart from martregistry
    	//add newmart to martregistry
    	//remove oldmart treenode
    	//add newmart to tree
    	//need to add back to the same index
    	MartRegistry mr = oldMart.getMartRegistry();
    	mr.removeMart(oldMart);
    	mr.addMart(newMart);
    	McTreeNode parent = (McTreeNode)this.getParent();
    	this.removeFromParent();  
    	List<Mart> newMartList = new ArrayList<Mart>();
    	newMartList.add(newMart);
    	parent.addMartNodes(newMartList);
    	return true;
    }
    
    public boolean refleshMart(Mart mart) {
    	McTreeNode parent = (McTreeNode)this.getParent();
    	int index = parent.getIndex(this);
    	this.removeFromParent();  
    	McTreeNode martNode = new McTreeNode(mart);
    	parent.insert(martNode, index);
    	return true;
    }
        
  
    public boolean addDsInPartition() {
/*    	final DataLinkInfo dlinkInfo = AddDsPartitionDialog.showDialog();
    	final Mart mart = (Mart)this.getUserObject(); 
    	if(dlinkInfo==null)
    		return false;
		final ProgressDialog progressMonitor = ProgressDialog.getInstance();				

		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					addNaiveDatasetInMart(dlinkInfo, mart);
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							StackTrace.showStackTrace(t);
						}
					});
				}finally {
					progressMonitor.setVisible(false);
				}
				return true;
			}

			public void finished() {
				// Close the progress dialog.
				progressMonitor.setVisible(false);
			}
		};
		
		worker.start();
		progressMonitor.start("processing ...");*/
    	return true;
    }
    
    public boolean mergeFilters(JDialog parent, List<Filter> filterList) {
    	String commonStr = null;
    	Set<String> fieldSet = new HashSet<String>();
    	for(Filter filter: filterList) {
    		for(Filter subFil: filter.getFilterList()) {
	    		Attribute attribute = subFil.getAttribute();
	    		if(attribute == null) {
	    			JOptionPane.showMessageDialog(parent, "no attribute for filter "+subFil.getName());
	    			return false;
	    		}
	    		DatasetColumn dsc = attribute.getDataSetColumn();
	    		if(dsc == null) {
	    			JOptionPane.showMessageDialog(parent, "no field for filter "+subFil.getName());
	    			return false;  			
	    		}
	    		String fieldName = dsc.getName();
	    		String[] __names = fieldName.split("__");
	    		StringBuilder sb = new StringBuilder(__names[0]);
	    		for(int i=1; i<__names.length-1; i++) {
	    			sb.append("__"+__names[i]);
	    		}
	    		if(commonStr==null)
	    			commonStr = sb.toString();
	    		else {
	    			if(!commonStr.equals(sb.toString())) {
	        			JOptionPane.showMessageDialog(parent, "please check the filter list name convention");
	        			return false;
	    			}
	    		}
	    		fieldSet.add(fieldName);
    		}
    	}
    	Container mergedContainer = new Container("merged_"+commonStr);
		mergedContainer.setHideValue(true);
    	for(String field: fieldSet) {
    		Filter fl = new Filter("merged_"+field, "merged_"+field);
        	for(Filter filter: filterList) {
        		for(Filter subFil: filter.getFilterList()) {
        			Attribute attribute = subFil.getAttribute();
        			DatasetColumn dsc = attribute.getDataSetColumn();
        			if(dsc.getName().equals(field)){
        				fl.addFilter(subFil);
        				fl.setDisplayName(subFil.getDisplayName());
        			}
        		}
        	}
    		mergedContainer.addFilter(fl);
    		fl.setFilterOperation(FilterOperation.OR);
    	}
    	if(!mergedContainer.isEmpty()) {
    		((Filter)this.getObject()).getParentContainer().getParentContainer().addContainer(mergedContainer);
    	}
    	return true;
    }

    public boolean updateTargetRelations() {
    	//this is a mart node
    	Mart mart = (Mart)this.getUserObject();
    	int count = this.getChildCount();
    	
    	//remove all relations
    	for(int i=count-1; i>=0; i--) {
    		McTreeNode node = (McTreeNode)this.getChildAt(i);
    		if(node.getUserObject() instanceof Relation) {
    			this.remove(i);
    		}
    	}
    	//add sortted relations
    	mart.updateRelations();
    	List<Relation> list = new ArrayList<Relation>(mart.getRelations());
    	Collections.sort(list/*, new RelationComparator()*/); // Relation.compare()
		for(Relation dsRelation: list) {
			McTreeNode dsrNode = new McTreeNode(dsRelation);
			this.add(dsrNode);
		}

    	return true;
    }

    public McTreeNode getChildByObject(MartConfiguratorObject mcObject) {
    	for(int i=0; i<this.getChildCount(); i++) {
    		McTreeNode node = (McTreeNode)this.getChildAt(i);
    		if(node.getObject().equals(mcObject)) 
    			return node;
    	}
    	return null;
    }

    public String toString(Dataset ds) {
    	if(ds==null)
    		return super.toString();
    	else if(McUtils.hasPartitionBinding(this.userObject.toString())) {
    		String value =  McUtils.getRealName(this.userObject.toString(), ds);
    		if(value==null)
    			return super.toString();
    		else
    			return value;
    	}
    	return super.toString();    		
    }
}