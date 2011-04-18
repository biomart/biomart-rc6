package org.biomart.objects.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.PartitionUtils;
import org.biomart.common.utils.XMLElements;

import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.McNodeType;

public abstract class Column extends MartConfiguratorObject implements Comparable<Column>, Comparator<Column> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> existInPts;
	
	/**
	 * need to check for name conflict, so the columnName will changed after constructed, 
	 * but the id will not TODO, rethink id
	 * @param table
	 * @param name
	 */
	public Column(Table table, String name) {
		super(name);
		this.parent = table;
		this.existInPts = new ArrayList<String>();
		this.setNodeType(McNodeType.COLUMN);
		Log.debug("Creating column " + name + " on table " + table.getName());
		// First we need to find out the base name, ie. the bit
		// we append numbers to make it unique, but before any
		// key suffix. If we appended numbers after the key
		// suffix then it would confuse MartEditor.
		String suffix = "";
		String baseName = name;
		if (name.endsWith(Resources.get("keySuffix"))) {
			suffix = Resources.get("keySuffix");
			baseName = name.substring(0, name.indexOf(suffix));
		}
		// Now simply check to see if the name is used, and
		// then add an incrementing number to it until it is unique.
		int k = 1;
		for(Column cc: table.getColumnList()) {
			if(cc.getName().equals(name)) {
				name = baseName + "_"+k+suffix;
				k++;
			}
		}
//		for (int i = 1; table.getColumns().containsKey(name); name = baseName
//				+ "_" + i++ + suffix)
//			;
		// Return it.
		Log.debug("Unique name is " + name);
		this.setName(name);
	}
	
	public Column(org.jdom.Element element) {
		super(element);
		this.setNodeType(McNodeType.COLUMN);
		this.existInPts = new ArrayList<String>();
	}

	public Table getTable() {
		return (Table)this.parent;
	}

	public int compare(Column column1, Column column2) {
		if (column1==null && column2!=null) {
			return -1;
		} else if (column1!=null && column2==null) {
			return 1;
		}
		return column1.getPropertyValue(XMLElements.NAME).toLowerCase().compareTo(
				column2.getPropertyValue(XMLElements.NAME).toLowerCase());
	}

	@Override
	public int compareTo(Column column) {
		return compare(this, column);
	}

	public void removeFromPartitions(String value) {
//		int oldcount = this.existInPts.size();
		if(this.existInPts.remove(value))
			this.setVisibleModified(true);
/*		int newcount = this.existInPts.size();
		if(oldcount == 1 && newcount == 0) {
			((Table)this.getParent()).removeColumn(this);
		} */
	}
	
	/**
	 * 
	 */
	public void renameInPartition(String oldvalue, String newvalue) {
		if(this.existInPts.remove(oldvalue)) {
			this.existInPts.add(newvalue);
			this.setProperty(XMLElements.INPARTITIONS, McUtils.StrListToStr(this.existInPts, ","));
		}
	}
	
	public void cleanInPartitions() {
		this.existInPts.clear();
	}
	
	public boolean inPartition(String value) {
		if(McUtils.isStringEmpty(value))
			return true;
		List<String> values = new ArrayList<String>();
		values.add(value);
		return this.inPartition(values);
	}
	
	public boolean inPartition(Collection<String> values) {
		if(McUtils.isCollectionEmpty(values))
			return true;
/*		if(this.existInPts.isEmpty()) {
			//check if it only has one dataset
			if(this.getParentMart().getSchemaPartitionTable().getTotalRows() == 1) {
				//add the dataset
				this.existInPts.add(this.getParentMart().getSchemaPartitionTable().getValue(0, PartitionUtils.DATASETNAME));
			}
		}
		if(this.existInPts.isEmpty()) {
			if(this.isVisibleModified())
				return false;
			else
				return true;
		}*/
		return this.existInPts.containsAll(values);
	}

	public List<String> getRange() {
		return this.existInPts;
	}
	
	public void addInPartitions(String value) {
		if(!this.existInPts.contains(value) && (!McUtils.isStringEmpty(value))) {
			this.existInPts.add(value);
			this.setProperty(XMLElements.INPARTITIONS, McUtils.StrListToStr(this.existInPts, ","));
		}
	}


	private Mart getParentMart() {
		Table table = this.getTable();
		if(table instanceof SourceTable) {
			return (Mart)table.getParent().getParent();
		}else {
			return (Mart)table.getParent();
		}
	}
}