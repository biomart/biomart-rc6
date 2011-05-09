/**
 * 
 */
package org.biomart.configurator.utils;

import org.biomart.common.resources.ErrorMessage;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.object.PartitionColumn;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Column;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.DatasetColumn;
import org.biomart.objects.objects.DatasetTable;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.ForeignKey;
import org.biomart.objects.objects.Key;
import org.biomart.objects.objects.Link;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Relation;
import org.biomart.objects.portal.GuiContainer;
import org.biomart.objects.portal.MartPointer;
import org.biomart.objects.portal.Portal;
import org.biomart.processors.Processor;
import org.biomart.processors.ProcessorGroup;


public class Validation {
	private static Validation instance;
	
	private Validation() {};
	
	public static ValidationStatus validateObject(MartConfiguratorObject mcObj) {
		if(instance == null)
			instance = new Validation();
		
		ValidationStatus vs = ValidationStatus.VALID;
		if(mcObj instanceof MartRegistry) {
			vs = instance.validateMartRegistry((MartRegistry)mcObj);
		} else if(mcObj instanceof Portal) {
			vs = instance.validatePortal((Portal)mcObj);
		} else if(mcObj instanceof GuiContainer) {
			vs = instance.validateGuiContainer((GuiContainer)mcObj);
		} else if(mcObj instanceof Processor) {
			vs = instance.validateProcessor((Processor)mcObj);
		} else if(mcObj instanceof ProcessorGroup) {
			vs = instance.validateProcessorGroup((ProcessorGroup)mcObj);
		} else if(mcObj instanceof MartPointer) {
			vs = instance.validateMartPointer((MartPointer)mcObj);
		}
		else if(mcObj instanceof Mart) {
			vs = instance.validateMart((Mart)mcObj);
		} else if(mcObj instanceof Config) {
			vs = instance.validateConfig((Config)mcObj);
		} else if(mcObj instanceof Link) {
			vs = instance.validateLink((Link)mcObj);
		} else if(mcObj instanceof PartitionTable) {
			vs = instance.validatePartitionTable((PartitionTable)mcObj);
		} else if(mcObj instanceof Relation) {
			vs = instance.validateRelation((Relation)mcObj);
		} else if(mcObj instanceof Column) {
			vs = instance.validateColumn((Column)mcObj);
		} else if(mcObj instanceof DatasetTable) {
			vs = instance.validateDatasetTable((DatasetTable)mcObj);
		} else if(mcObj instanceof Key) {
			vs = instance.validateKey((Key)mcObj);
		}
		else if(mcObj instanceof Container) {
			vs = instance.validateContainer((Container)mcObj);
		} 
/*		else if(mcObj instanceof Attribute) {
			vs = instance.validateAttribute((Attribute)mcObj);
		} 
		else if(mcObj instanceof Filter) {
			vs = instance.validateFilter((Filter)mcObj);
		}*/
		return vs;
	}

	private ValidationStatus validateContainer(Container con){
		ValidationStatus validate = ValidationStatus.VALID;
		String errorMessage = null;
		for(Attribute attribute: con.getAttributeList()) {
			if(!attribute.isValid()) {
				validate = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("60001");
			}
		}
		
		for(Filter filter: con.getFilterList()) {
			if(!filter.isValid()) {
				validate = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("60001");
			}
		}
		
		for(Container c: con.getContainerList()) {
			ValidationStatus tmp = this.validateContainer(c);
			if(tmp.compareTo(validate)>0) {
				validate = tmp;
				errorMessage = ErrorMessage.get("60001");
			}
		}

		con.setObjectStatus(validate);
		con.setProperty(XMLElements.ERROR,errorMessage);
		return validate;
	}
	
	private ValidationStatus validateConfig(Config config){
		ValidationStatus validate =  this.validateContainer(config.getRootContainer());
		config.setObjectStatus(validate);
		if(validate!=ValidationStatus.VALID)
			config.setProperty(XMLElements.ERROR, ErrorMessage.get("60001"));
		else
			config.setProperty(XMLElements.ERROR, "");
		return validate;
	}
	
	private ValidationStatus validateAttribute(Attribute attr){
		ValidationStatus result = ValidationStatus.VALID;
		String errorMessage = null;
		//check for existence in source first
		if(!attr.getParentConfig().isMasterConfig() &&
				attr.getParentConfig().getMart().getMasterConfig().getAttributeByName(attr.getName(), null) == null) {
			result = ValidationStatus.INVALID;
			errorMessage = ErrorMessage.get("10005");
		}
		//check pointer
		//if it is a pointer, the pointedElement should not be null
		if(attr.isPointer()) {
			if(attr.getPointedAttribute()== null) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10004");
			}
			
/*			if(!McUtils.isStringEmpty(attr.getPropertyValue(XMLElements.COLUMN)) ||
				(!McUtils.isStringEmpty(attr.getPropertyValue(XMLElements.TABLE)))) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10006");
			}*/
		} else if(attr.isAttributeList()) {
			//it is valid if one of the attribute in the list is valid
/*			boolean b = false;
			for(Attribute att: attr.getAttributeList()) {
				if(this.validateAttribute(att)==ValidationStatus.VALID)
					b = true;
			}
			if(!b) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10007");
			}*/
		} else if(attr.isPseudoAttribute()) {
			
		} //normal attribute
		else {
			//normal attribute, check table and column
			if(attr.getDatasetTable() == null || attr.getDataSetColumn() == null) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10008");
			}
		}
		
		attr.setObjectStatus(result);
		attr.setProperty(XMLElements.ERROR, errorMessage);
		return result;
	}
	
	private  ValidationStatus validateDatasetTable(DatasetTable datasetTable){
		ValidationStatus result = ValidationStatus.VALID;
		for(Column col: datasetTable.getColumnList()) {
			ValidationStatus tmp = this.validateColumn((DatasetColumn)col);
			if(tmp.compareTo(result)>0)
				result = tmp;
		}
		if(datasetTable.getPrimaryKey()!=null) {
			ValidationStatus tmp = this.validateKey(datasetTable.getPrimaryKey());
			if(tmp.compareTo(result)>0)
				result = tmp;			
		}
		//fk
		for(ForeignKey fk: datasetTable.getForeignKeys()) {
			ValidationStatus tmp = this.validateKey(fk);
			if(tmp.compareTo(result)>0)
				result = tmp;						
		}
		datasetTable.setObjectStatus(result);
		return result;
	}
	
	private ValidationStatus validateFilter(Filter filter){
		ValidationStatus result = ValidationStatus.VALID;
		String errorMessage = null;
		if(!filter.getParentConfig().isMasterConfig() && 
				filter.getParentConfig().getMart().getMasterConfig().getFilterByName(filter.getName(), null) == null)
		{
			result = ValidationStatus.INVALID;
			errorMessage = ErrorMessage.get("10005");
		}
		if(filter.isPointer()) {
			if(filter.getPointedFilter()== null) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10004");
			}
		} else if(filter.isFilterList()) {
			//it is valid if one of the filter in the list is valid
			boolean b = false;
			for(Filter fil: filter.getFilterList()) {
				if(this.validateFilter(fil)==ValidationStatus.VALID)
					b = true;
			}
			if(!b) {
				result = ValidationStatus.INVALID;
				errorMessage = ErrorMessage.get("10007");
			}
		} else {
			//normal filter
			if(filter.getAttribute() == null ||
					this.validateAttribute(filter.getAttribute())!=ValidationStatus.VALID) {
				result = ValidationStatus.VALID;
				errorMessage = ErrorMessage.get("10009");
			}
			
			//check for qualifiers
			if(filter.getQualifier() == null){
				errorMessage = ErrorMessage.get("10010");
				result = ValidationStatus.INVALID;
			}

		}				
		filter.setObjectStatus(result);
		filter.setProperty(XMLElements.ERROR, errorMessage);
		return result;
	}
	
	private ValidationStatus validateKey(Key key){
		ValidationStatus result = ValidationStatus.VALID;
		return result;
	}
	
	private ValidationStatus validateLink(Link link){
		String errorMessage = null;
		if(link.getPointedConfig() == null) {
			errorMessage = ErrorMessage.get("10011");
			link.setObjectStatus(ValidationStatus.INVALID);
			link.setProperty(XMLElements.ERROR,errorMessage);
			return link.getObjectStatus();
		}
		//synch pointed dataset, for now just set the name,
				
		String attributes = link.getPropertyValue(XMLElements.ATTRIBUTES);
		String[] _atts = attributes.split(",");
		for(String attStr: _atts) {
			Attribute att = ((Config)link.getParentConfig()).getAttributeByName(attStr,null);
			if(att == null) {
				link.setObjectStatus(ValidationStatus.INVALID);
				link.setProperty(XMLElements.ERROR, "attributes invalid");
				return link.getObjectStatus();
			}
		}
		
		String filters = link.getPropertyValue(XMLElements.FILTERS);
		String[] _filters = filters.split(",");
		for(String filStr: _filters) {
			Filter filter = ((Config)link.getParentConfig()).getFilterByName(filStr, null);
			if(filter == null) {
				link.setObjectStatus(ValidationStatus.INVALID);
				link.setProperty(XMLElements.ERROR, "filters invalid");
				return link.getObjectStatus();				
			}
		}
		link.setObjectStatus(ValidationStatus.VALID);
		return link.getObjectStatus();
	}
	
	private ValidationStatus validateMart(Mart mart){
		ValidationStatus result = ValidationStatus.VALID;
		//validate datasettable
		for(DatasetTable dst: mart.getDatasetTables()) {
			ValidationStatus tmp = this.validateDatasetTable(dst);
			if(tmp.compareTo(result)>0)
				result = tmp;
		}
		//validate relation
		for(Relation relation: mart.getRelations()) {
			ValidationStatus tmp = this.validateRelation(relation);
			if(tmp.compareTo(result)>0)
				result = tmp;
		}
		//validate configs
		for(Config conf: mart.getConfigList()) {
			ValidationStatus tmp = this.validateConfig(conf);
			if(tmp.compareTo(result)>0)
				result = tmp;
		}
		mart.setObjectStatus(result);
		if(result!=ValidationStatus.VALID)
			mart.setProperty(XMLElements.ERROR,ErrorMessage.get("60001"));
		else
			mart.setProperty(XMLElements.ERROR, "");
		return result;
	}
	
	/**
	 * in a martregistry validation, portal part is indepent with marts part, which means
	 * if a mart is invalid, a martpointer pointing to the mart can be valid.
	 * @param mr
	 * @return
	 */
	private ValidationStatus validateMartRegistry(MartRegistry mr){
		ValidationStatus validate = this.validatePortal(mr.getPortal());
    	for(Mart mart: mr.getMartList()) {
    		ValidationStatus tmp = this.validateMart(mart);
    		if(tmp.compareTo(validate)>0)
    			validate = tmp;
    	}
    	mr.setObjectStatus(validate);
    	if(validate!=ValidationStatus.VALID)
    		mr.setProperty(XMLElements.ERROR,ErrorMessage.get("60001"));
		else
			mr.setProperty(XMLElements.ERROR, "");
    	return validate;
	}
	
	private ValidationStatus validatePartitionTable(PartitionTable pt){
		ValidationStatus result = ValidationStatus.VALID;
		int totalrow = pt.getTotalRows();
		for(PartitionColumn pc: pt.getPartitionColumns()) {
			if(pc.getColumnList().size()!=totalrow) {
				result = ValidationStatus.INVALID;
				break;
			}
		}

		pt.setObjectStatus(result);
		return result;
	}
	
	
	private ValidationStatus validateGuiContainer(GuiContainer gc){
		ValidationStatus result = ValidationStatus.VALID;
		if(gc.isLeaf()) {
			for(MartPointer mp: gc.getMartPointerList()) {
				ValidationStatus tmp = this.validateMartPointer(mp);
				if(tmp.compareTo(result)>0)
					result = tmp;
			}			
		}else {
			for(GuiContainer guic: gc.getGuiContainerList()) {
				ValidationStatus tmp = this.validateGuiContainer(guic);
				if(tmp.compareTo(result)>0)
					result = tmp;
			}
		}
		gc.setObjectStatus(result);
		if(result!=ValidationStatus.VALID)
			gc.setProperty(XMLElements.ERROR, ErrorMessage.get("60001"));
		else
			gc.setProperty(XMLElements.ERROR, "");
		return result;
	}
	
	private ValidationStatus validateMartPointer(MartPointer mp){
		ValidationStatus result = ValidationStatus.VALID;
		String errorMessage = null;
		for(ProcessorGroup pg: mp.getProcessorGroupList()) {
			ValidationStatus tmp = this.validateProcessorGroup(pg);
			if(tmp.compareTo(result)>0) {
				result = tmp;
				errorMessage = ErrorMessage.get("60001");
			}
		}
		
		if(mp.getMart()==null || mp.getConfig() == null) {
			result = ValidationStatus.INVALID;
			//override 60001 
			errorMessage = ErrorMessage.get("10001");
		}		

		//check link		
		mp.setObjectStatus(result);
		mp.setProperty(XMLElements.ERROR, errorMessage);
		return result;
	}
	
	private ValidationStatus validatePortal(Portal portal){
		ValidationStatus result = this.validateGuiContainer(portal.getRootGuiContainer());
		portal.setObjectStatus(result);
		if(result!=ValidationStatus.VALID)
			portal.setProperty(XMLElements.ERROR, ErrorMessage.get("60001"));
		else
			portal.setProperty(XMLElements.ERROR, "");
		return result;
	}
	
	private ValidationStatus validateProcessor(Processor processor){
		ValidationStatus result = ValidationStatus.VALID;
		if(processor.getContainer() == null) {
			result = ValidationStatus.INVALID;
		}
		processor.setObjectStatus(result);
		if(result!=ValidationStatus.VALID)
			processor.setProperty(XMLElements.ERROR, ErrorMessage.get("10002"));
		else
			processor.setProperty(XMLElements.ERROR, "");
		
		return result;
	}
	
	private ValidationStatus validateProcessorGroup(ProcessorGroup pg){
		ValidationStatus result = ValidationStatus.VALID;
		String errorMessage = null;
		for(Processor p: pg.getProcessorList()) {
			ValidationStatus tmp = this.validateProcessor(p);
			if(tmp.compareTo(result)>0) {
				result = tmp;
				errorMessage = ErrorMessage.get("60001");
			}
		}
		if(pg.getDefaultProcessor() ==  null) {
			result = ValidationStatus.INVALID;
			errorMessage = ErrorMessage.get("10003");
		}	
		pg.setObjectStatus(result);
		pg.setProperty(XMLElements.ERROR, errorMessage);		
		return result;
	}

	private ValidationStatus validateRelation(Relation r) {
		return ValidationStatus.VALID;
	}
	
	private ValidationStatus validateColumn(Column c) {
		return ValidationStatus.VALID;
	}

	/**
	 * second round of validation. some object's validation are based on the result of first round validation.
	 * such as pointer.
	 */
	private void validation2() {
		
	}
}
