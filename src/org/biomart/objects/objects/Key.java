package org.biomart.objects.objects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.ComponentStatus;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.jdom.Element;

public abstract class Key extends MartConfiguratorObject implements Comparable<Key>, Comparator<Key>{

	private ComponentStatus status;
	private Set<Relation> relations;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Key(final List<Column> columns, String name) {
		//default use the first column as name
		super(name);
		this.status = ComponentStatus.INFERRED;
		this.relations = new TreeSet<Relation>();
		this.parent = columns.get(0).getTable();	
		this.setColumns(columns);
	}
	
	protected Key(final Column column, String name) {
		super(name);
		this.status = ComponentStatus.INFERRED;
		this.relations = new TreeSet<Relation>();
		this.parent = column.getTable();
		List<Column> cols = new ArrayList<Column>();
		cols.add(column);
		this.setColumns(cols);
	}
	
	public Key(org.jdom.Element element) {
		super(element);
		this.relations = new TreeSet<Relation>();
		this.status = ComponentStatus.INFERRED;
		this.setInternalName(this.getName());
		this.setDisplayName(this.getName());
	}
	
	/**
	 * Replaces the set of columns this key is formed over with a new set.
	 * 
	 * @param columns
	 *            the replacement columns, in order.
	 */
	private void setColumns(final List<Column> columns) {
		Log.debug("Creating key over " + columns);
		this.setProperty(XMLElements.COLUMN, McUtils.listToStr(columns, ","));
	}
	
	/**
	 * Sets the status of this key.
	 * 
	 * @param status
	 *            the new status of this key.
	 */
	public void setStatus(final ComponentStatus status) {
		Log.debug("Changing status for " + this + " to " + status);
		if (this.status == status || this.status != null
				&& this.status.equals(status))
			return;
		this.status = status;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("["+this.getPropertyValue(XMLElements.COLUMN)+"]");
		return sb.toString();
	}
	
	public Table getTable() {
		return(Table)this.parent;
	}

	void addRelation(Relation newRelation) {
		this.relations.add(newRelation);
	}
	
	public void removeRelation(Relation relation) {
		this.relations.remove(relation);
	}

	public ComponentStatus getStatus() {
		return this.status;
	}
	
	public List<Column> getColumns() {
		List<Column> result = new ArrayList<Column>();
		String[] cols = this.getPropertyValue(XMLElements.COLUMN).split(",");
		for(String col: cols) {
			Column column = this.getTable().getColumnByName(col);
			if(column == null)
				Log.error("null column");
			else
				result.add(column);
		}
		return result;
	}

	/**
	 * 
	 * @return 
	 */
	public Set<Relation> getRelations() {
/*		Set<Relation> results = new HashSet<Relation>();
		for(Relation r: this.relations) {
			if(r.isHidden())
				continue;
			results.add(r);
		}
		return results;*/
		return this.relations;
	}
	
	/**
	 * remove all relations on this key, and also the relations on the other sides
	 */
	public void removeAllRelations() {
		for (Relation rel: this.relations) {
			Key otherKey = rel.getOtherKey(this);
			otherKey.getRelations().remove(rel);
		}
		this.relations.clear();
	}
	
	public void clearRelations() {
		this.relations.clear();
	}
	
	/**
	 * Assumes key not null
	 */
	@Override
	public int compareTo(Key object) {
		boolean equals = this.equals(object);
		if (equals) {
			return 0;
		} else {
			Integer compare = null;
			compare = this.getClass().toString().compareTo(object.getClass().toString());
			if (compare==0) {
				compare = this.getFullIdentifier().compareTo(object.getFullIdentifier());
			}
			return compare;	
		}		
	}
	@Override
	public int compare(Key key1, Key key2) {
		return key1.compareTo(key2);	// assumes not null
	}
	
	/**
	 * only at table level
	 * @param key
	 * @return
	 */
	public boolean isKeyEquals(Key key) {
		if(!this.getTable().getName().equals(key.getTable().getName()))
			return false;
		//TODO column order?
		Set<String> sSet = new HashSet<String>();
		Set<String> tSet = new HashSet<String>();
		for(Column c: this.getColumns())
			sSet.add(c.getName());
		for(Column c: key.getColumns())
			tSet.add(c.getName());
		if(sSet.size() == tSet.size() && sSet.containsAll(tSet))
			return true;
		else
			return false;
	}
	
	public int hashCode() {
		// The hash code is only against the table itself, in
		// case our column names change. This is to ensure
		// that we stay in the same hash buckets.
		return this.getHashIdentifier().hashCode();
	}
	
	public String getFullIdentifier() {
		String identifier = this.getIdentifier() + Resources.get("IDENTIFIER_SEPARATOR") + getCommaSeparatedColumnNameList(); 	// FIXME columnList should never really be necessary...
		return identifier.toLowerCase(); // toLowerCase: see DCCTEST-491
	}
	private String getIdentifier() {
		Table parentTable = this.getTable();
		String identifier = this.getClass().getName() + Resources.get("IDENTIFIER_SEPARATOR") + 
		(parentTable!=null ? parentTable.getName() + Resources.get("IDENTIFIER_SEPARATOR") : Resources.get("UNRESOLVED_PARENT")) + 
		this.getName();
		return identifier.toLowerCase(); // toLowerCase: see DCCTEST-491
	}
	public String getHashIdentifier() {
		String identifier = this.getClass().getName() + Resources.get("IDENTIFIER_SEPARATOR") + 
			/*this.getTable().getName() + IDENTIFIER_SEPARATOR + */
			this.getName();
		return identifier.toLowerCase(); // toLowerCase: see DCCTEST-491s
	}
	private String getCommaSeparatedColumnNameList() {
		StringBuffer columnListSb = new StringBuffer();
		columnListSb.append('['+this.getPropertyValue(XMLElements.COLUMN)+']');
		return columnListSb.toString();
	}
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Key) {
			final Key k = (Key) o;
			return this.getFullIdentifier().equals(k.getFullIdentifier());
		} else
			return false;
	}

	public boolean equalsByName(final Object o) {
		if(o == this)
			return true;
		else if(o==null)
			return false;
		else if(o instanceof Key) {
			final Key k = (Key) o;
			if(k.getColumns().size() !=this.getColumns().size())
				return false;
			else {
				//assume only one column for now
				return (k.getColumns().get(0).getName().equals(this.getColumns().get(0).getName()));
			}
		}else
			return false;
	}
	
	@Override
	public Element generateXml() throws FunctionalException {
		Element element = new Element(this.getNodeType().toString());
		element.setAttribute(XMLElements.NAME.toString(),this.getName());
		element.setAttribute(XMLElements.INTERNALNAME.toString(),this.getInternalName());
		element.setAttribute(XMLElements.DISPLAYNAME.toString(),this.getDisplayName());
		element.setAttribute(XMLElements.DESCRIPTION.toString(),this.getDescription());
		element.setAttribute(XMLElements.COLUMN.toString(),this.getPropertyValue(XMLElements.COLUMN));
		
		return element;
	}
	
	@Override
	public void synchronizedFromXML() {
		//set column
		this.setObjectStatus(ValidationStatus.VALID);
		String[] columns = this.getPropertyValue(XMLElements.COLUMN).split(",");
		List<Column> colArray = new ArrayList<Column>();
		for(int i=0; i<columns.length; i++) {
			Column column = this.getTable().getColumnByName(columns[i]);
			if(column==null)
				this.setObjectStatus(ValidationStatus.INVALID);
			else
				colArray.add(column);
		}
		this.setColumns(colArray);
	} 
	
	
}