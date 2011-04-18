
package org.biomart.configurator.jdomUtils;


import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.biomart.common.resources.Log;
import org.biomart.common.resources.Settings;
import org.biomart.common.utils.McFont;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.DatasetTableType;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.biomart.configurator.utils.type.PartitionType;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.DatasetColumn;
import org.biomart.objects.objects.DatasetTable;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.PartitionTable;

/**
 * Changes how the tree displays elements.
 */
public class XMLTreeCellRenderer extends DefaultTreeCellRenderer {
     
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean validate = false;
	//remove icons
    public XMLTreeCellRenderer() {
    	validate = Boolean.parseBoolean(System.getProperty("guivalidation"));
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    	McTreeNode node = (McTreeNode)value;
        MartConfiguratorObject mcObject = (MartConfiguratorObject)node.getUserObject();

    	this.setMcIcon(mcObject);
    	if(tree instanceof MartConfigTree) {
	    	if(McUtils.isStringEmpty(mcObject.getDisplayName()))
	    		this.setText(mcObject.toString());
	    	else {
	    		MartConfigTree mcTree = (MartConfigTree)tree;
	    		this.setText(node.toString(mcTree.getSelectedDataset()));
	    	}
    	}else
    		this.setText(mcObject.toString());
    	if(!mcObject.isValid())
    		this.setToolTipText(mcObject.getPropertyValue(XMLElements.ERROR));
    	else
    		this.setToolTipText(mcObject.getName());
    	this.setColor(mcObject);

        return this;
        
    }
    
    /**
     * should replaced by setIcon
     * @param mcObject
     */
    private void setColor(MartConfiguratorObject mcObject) {
    	//color for dataset
    	this.setFont(McFont.getInstance().getDefaultFont());
    	switch(mcObject.getNodeType()) {
    	case TABLE:    		
        	DatasetTable dst = (DatasetTable) mcObject;
        	if(dst.getType().equals(DatasetTableType.MAIN) || dst.getType().equals(DatasetTableType.MAIN_SUBCLASS) ) {
        		this.setFont(McFont.getInstance().getBoldFont());
        	}else
        		this.setFont(McFont.getInstance().getDefaultFont());
        	break;
    	case PARTITIONTABLE:
    		PartitionTable pt = (PartitionTable) mcObject;
    		if(pt.getPartitionType().equals(PartitionType.DATASET)) {
    			setForeground(Color.RED);
    		}else if(pt.getPartitionType().equals(PartitionType.DIMENSION)) {
    			setForeground(Color.ORANGE);
    		}else
    			setForeground(Color.BLACK);
    		
    		break;
    	case ATTRIBUTE:
    	case FILTER:
    		
    	}
    	if(mcObject.isHidden())
    		setForeground(Color.LIGHT_GRAY);
    	else if(!mcObject.isValid() && mcObject.getNodeType() != McNodeType.CONTAINER)
    		setForeground(Color.LIGHT_GRAY);
    	else if(mcObject.isVisibleModified()) {
    		setForeground(Color.GREEN);
    	} else 
    		setForeground(Color.BLACK);
    		
    }
    
    private void setMcIcon(MartConfiguratorObject object) {
    	if(object == null) {
    		Log.error("object is null");
    		return;
    	}
    	if(object.getNodeType() == null) {
    		return;
    	}
/*    	if(!validate)
    		object.setObjectStatus(ValidationStatus.VALID);
    	
    	StringBuilder path = new StringBuilder("images/");
    	switch(object.getNodeType()) {
    	case MARTREGISTRY:
    		if(object.getObjectStatus().equals(ValidationStatus.VALID))
    			path.append("registry");
    		else
    			path.append("registry_error");
    		break;
    	case MART:
    		if(object.getObjectStatus().equals(ValidationStatus.VALID))
    			path.append("mart");
    		else
    			path.append("mart_error");
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");
    		break;
    	case TABLE:
    		path.append("table");
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");
    		else if(((DatasetTable)object).isOrphan())
    			path.append("_warning");
    		break;   		
    	case SOURCETABLE:
    		path.append("table");
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");
    		break;
    	case COLUMN:
    		path.append("column");
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");
    		else if(object instanceof DatasetColumn) {
    			if(((DatasetColumn)object).isOrphan()) {
    				path.append("_warning");
    			}
    		}
    		break;
    	case RELATION:
    	case SOURCERELATION:
    		path.append("relation");
    		if(!object.getObjectStatus().equals(ValidationStatus.VALID))
    			path.append("_error");
    		break;
    	case SCHEMA:
    		path.append("schema");
    		break;
    	case CONFIG:
    		if(object.getObjectStatus().equals(ValidationStatus.VALID))
    			path.append("config");
//    		else if(object.getObjectStatus().equals(ValidateStatus.POINTERINCOMPLETE))
//    			path.append("config_warning");
    		else
    			path.append("config_error");
    		
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");
    		break;
    	case CONTAINER:   		
			switch (object.getObjectStatus()) {
			case VALID:
				path.append("container");
				break;
//			case POINTERINCOMPLETE:
//				path.append("container_warning");
//				break;
			default:
				path.append("container_error");
				break;
			} 		
    		if(object.isHidden()) {
    			path.append("_hide");
    		}
    		if(object.getUpdateType()!=UpdateType.NOCHANGE)
    			path.append("_dirty");  
    		break;
    	case FILTER:
    		path.append(this.getFilterImagePath((Filter)object));
    		break;
    	case ATTRIBUTE:
    		path.append(this.getAttributeImagePath((Attribute)object));
     		break;
    	case PARTITIONTABLE:
    		path.append("partitionTable");
    		break;
    	case PORTAL:
    		path.append("portal");
    		break;
    	case USERS:
    	case GROUP:
    		path.append("users.gif");
    		break;
    	case USER:
    		path.append("user.gif");
    		break;
    	case LINKINDEXES:
    		path.append("links");
    		break;
    	case LINKINDEX:
    		path.append("link");
    		break;
    	case GUICONTAINER:
    		path.append("guiContainer");
    		break;
    	case MARTPOINTER:
    		path.append("martpointer");
    		break;
    	case PROCESSORS:
    	case PROCESSOR:
    		path.append("processor");
    		break;
    	case LINK:
    		path.append("link");
    		if(object.getObjectStatus()!=ValidationStatus.VALID)
    			path.append("_error");
    		break;
    	case IMPORTABLE:
    		path.append("importable");
    		break;
    	case EXPORTABLE:
    		path.append("exportable");
    		break;
    	case PRIMARYKEY:
    		path.append("pk");
    		break;
    	case FOREIGNKEY:
    		path.append("fk");
    		break;
    	default:
    		path.append("cut");
    	}

   		
		path.append(".gif");*/
		Icon icon = McUtils.createImageIcon(this.getImageName(object));
		this.setIcon(icon);    	
    }
    
    private String getImageName(MartConfiguratorObject obj) {
    	StringBuilder sb = new StringBuilder("images/");
    	
    	sb.append(this.getObjectImageBase(obj)).append(this.getDirtyExt(obj)).
    	append(this.getHideExt(obj)).append(this.getWarningExt(obj)).append(this.getValidExt(obj)).append(".gif");
    	
    	return sb.toString();
    }
    
    private String getObjectImageBase(MartConfiguratorObject obj) {
    	String result = obj.getNodeType().toString();
    	if(obj instanceof Attribute) {
    		Attribute att = (Attribute)obj;
    		if(att.isAttributeList())
    			result = "attribute_l";
    		else if(att.isPointer())
    			result = "attribute_p";
    	} else if(obj instanceof Filter) {
    		Filter fil = (Filter)obj;
    		if(fil.isFilterList())
    			result = "filter_l";
    		else if(fil.isPointer())
    			result = "filter_p";
    	} 
    	return result;
    }
    
    private String getWarningExt(MartConfiguratorObject obj) {
    	return "";
    }
    
    private String getDirtyExt(MartConfiguratorObject obj) {
    	return "";
    }
    
    private String getHideExt(MartConfiguratorObject obj) {
    	if(obj.isHidden())
    		return "_h";
    	else
    		return "";
    }
    
    private String getValidExt(MartConfiguratorObject obj) {
    	//skip config validation
    	if(obj instanceof Config)
    		return "";
    	if(!validate)
    		return "";
    	else if(!obj.isValid())
    		return "_w";
    	return "";
    }
        
}

