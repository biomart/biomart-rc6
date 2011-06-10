package org.biomart.objects.objects;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.MartConfiguratorUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.utils.type.ComponentStatus;
import org.biomart.configurator.utils.type.DatasetTableType;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.configurator.utils.type.ValidationStatus;
import org.jdom.Element;


public abstract class Relation extends MartConfiguratorObject implements Comparable<Relation>, Comparator<Relation> {
	
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.firstKeyObj == null ? "<undef>" : this.firstKeyObj.getParent().getName()+"."+this.firstKeyObj.toString());
		sb.append(" -> ");
		sb.append(this.secondKeyObj == null ? "<undef>" : this.secondKeyObj.getParent().getName()+"."+this.secondKeyObj
				.toString());
		return sb.toString();
	}

	private String getIdentifier() {
		String identifier = this.getClass().getName() + Resources.get("IDENTIFIER_SEPARATOR") + 
			this.firstKeyObj.getFullIdentifier() +	// already includes table name 
			Resources.get("IDENTIFIER_SEPARATOR") + 
			this.secondKeyObj.getFullIdentifier(); 	// getName() is irrelevant here
		return identifier.toLowerCase(); // toLowerCase: see DCCTEST-491s
	}
	private String getHashIdentifier() {	// does not include columns nor table
		String identifier = this.getClass().getName() + Resources.get("IDENTIFIER_SEPARATOR") + 
			this.firstKeyObj.getHashIdentifier() + 
			Resources.get("IDENTIFIER_SEPARATOR") + 
			this.secondKeyObj.getHashIdentifier();
		return identifier.toLowerCase(); // toLowerCase: see DCCTEST-491s
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Relation) {
			final Relation r = (Relation) o;
			// Check that the same keys are involved.
			return this.getIdentifier().equals(r.getIdentifier());
		} else
			return false;
	}

	@Override
	public int compareTo(Relation object) {	// assumes not null
		boolean equals = this.equals(object);
		if (equals) {
			return 0;
		} else {
			Integer compare = null;
			compare = this.getClass().toString().compareTo(object.getClass().toString());
			if (compare==0) {
				compare = this.getIdentifier().compareTo(object.getIdentifier());
			}
			return compare;	
		}		
	}
	@Override
	public int compare(Relation relation1, Relation relation2) {
		return relation1.compareTo(relation2);	// assumes not null
	}

	public int hashCode() {
		return this.getHashIdentifier().hashCode();
	}

	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() throws FunctionalException {
		Element element = new Element(XMLElements.RELATION.toString());
		element.setAttribute(XMLElements.NAME.toString(),this.getName());
		element.setAttribute(XMLElements.INTERNALNAME.toString(),this.getPropertyValue(XMLElements.INTERNALNAME));
		element.setAttribute(XMLElements.DISPLAYNAME.toString(),this.getPropertyValue(XMLElements.DISPLAYNAME));
		element.setAttribute(XMLElements.DESCRIPTION.toString(),this.getPropertyValue(XMLElements.DESCRIPTION));
		
		MartConfiguratorUtils.addAttribute(element, XMLElements.FIRSTTABLE.toString(), 
				(this.firstKeyObj!=null ? this.firstKeyObj.getTable().getName() : null));
		MartConfiguratorUtils.addAttribute(element, XMLElements.SECONDTABLE.toString(), 
				(this.secondKeyObj!=null ? this.secondKeyObj.getTable().getName() : null));
		MartConfiguratorUtils.addAttribute(element, XMLElements.FIRSTKEY.toString(), this.firstKeyObj.getName());
		MartConfiguratorUtils.addAttribute(element, XMLElements.SECONDKEY.toString(), this.secondKeyObj.getName());
		element.setAttribute(XMLElements.TYPE.toString(), (this.cardinality!=null ? this.cardinality.toString() : null));
		
		return element;
	}

	protected Key firstKeyObj;
	protected Key secondKeyObj;
	private Cardinality cardinality;
	private Cardinality originalCardinality;
	private ComponentStatus status;

	//TODO: may have different objects for source and target
	protected Set<String> dsForSubRelation;
	public static String DATASET_WIDE = "__DATASET_WIDE__";

	protected Relation(Key firstkey, Key secondkey, Cardinality cardinality) throws AssociationException {
		super(firstkey.getParent().getName()+"__"+
				secondkey.getParent().getName());
		Log.debug("Creating relation between " + firstkey + " and " + secondkey
				+ " with cardinality " + cardinality);

		this.firstKeyObj = firstkey;
		this.secondKeyObj = secondkey;
		this.setOriginalCardinality(cardinality);
		this.setCardinality(cardinality);
		this.setStatus(ComponentStatus.INFERRED);
		this.dsForSubRelation = new HashSet<String>();
		// Check the keys have the same number of columns.
		if (firstKeyObj.getColumns().size() != secondKeyObj.getColumns().size()) {
			throw new AssociationException(Resources
					.get("keyColumnCountMismatch"));
		}
		// Check the relation doesn't already exist.
		if (firstkey.getRelations().contains(this)) {
			Log.debug("duplicated relation "+this);
			throw new AssociationException(Resources
					.get("relationAlreadyExists"));
		}
		// Cannot place a relation on an FK to this table if it
		// already has relations.
		if (firstKeyObj.getTable().equals(secondKeyObj.getTable())
				&& (firstKeyObj instanceof ForeignKey
						&& firstKeyObj.getRelations().size() > 0 || secondKeyObj instanceof ForeignKey
						&& secondKeyObj.getRelations().size() > 0))
			throw new AssociationException(Resources
					.get("fkToThisOnceOrOthers"));
		// Cannot place a relation on an FK to another table if
		// it already has a relation to this table (it will have
		// only one due to previous check).
		if (!firstKeyObj.getTable().equals(secondKeyObj.getTable())
				&& ((firstKeyObj instanceof ForeignKey
						&& firstKeyObj.getRelations().size() == 1 && ( firstKeyObj
						.getRelations().iterator().next())
						.getOtherKey(firstKeyObj).getTable().equals(
								firstKeyObj.getTable())) || (secondKeyObj instanceof ForeignKey
						&& secondKeyObj.getRelations().size() == 1 && ((Relation) secondKeyObj
						.getRelations().iterator().next()).getOtherKey(
						secondKeyObj).getTable().equals(secondKeyObj.getTable()))))
			throw new AssociationException(Resources
					.get("fkToThisOnceOrOthers"));

		// Update flags.
		
		this.firstKeyObj.addRelation(this);
		this.secondKeyObj.addRelation(this);
		//set parent
		if(this.firstKeyObj.getTable().getParent()!=null)
			this.parent = this.firstKeyObj.getTable().getParent();
		this.setProperty(XMLElements.FIRSTKEY, this.firstKeyObj.getName());
		this.setProperty(XMLElements.FIRSTTABLE, this.firstKeyObj.getTable().getName());
		this.setProperty(XMLElements.SECONDKEY, this.secondKeyObj.getName());
		this.setProperty(XMLElements.SECONDTABLE, this.secondKeyObj.getTable().getName());
	}
	
	protected Relation(Element element) {
		super(element);
		this.dsForSubRelation = new HashSet<String>();
		this.setProperty(XMLElements.FIRSTKEY, element.getAttributeValue(XMLElements.FIRSTKEY.toString()));
		this.setProperty(XMLElements.FIRSTTABLE, element.getAttributeValue(XMLElements.FIRSTTABLE.toString()));
		this.setProperty(XMLElements.SECONDKEY, element.getAttributeValue(XMLElements.SECONDKEY.toString()));
		this.setProperty(XMLElements.SECONDTABLE, element.getAttributeValue(XMLElements.SECONDTABLE.toString()));
	}
	
	/**
	 * Sets the original cardinality of the foreign key end of this relation, in
	 * a 1:M relation. If used on a 1:1 or M:M relation, then specifying M makes
	 * it M:M and specifying 1 makes it 1:1.
	 * 
	 * @param originalCardinality
	 *            the originalCardinality.
	 */
	public void setOriginalCardinality(Cardinality originalCardinality) {
		Log.debug("Changing original cardinality of " + this + " to "
				+ originalCardinality);
		if (this.originalCardinality == originalCardinality
				|| this.originalCardinality != null
				&& this.originalCardinality.equals(originalCardinality))
			return;
		this.originalCardinality = originalCardinality;
	}

	/**
	 * Sets the cardinality of the foreign key end of this relation, in a 1:M
	 * relation. If used on a 1:1 or M:M relation, then specifying M makes it
	 * M:M and specifying 1 makes it 1:1.
	 * 
	 * @param cardinality
	 *            the cardinality.
	 */
	public void setCardinality(Cardinality cardinality) {
		Log.debug("Changing cardinality of " + this + " to " + cardinality);
		if (this.firstKeyObj instanceof PrimaryKey
				&& this.secondKeyObj instanceof PrimaryKey) {
			Log.debug("Overriding cardinality change to ONE");
			cardinality = Cardinality.ONE;
		}

//		final Cardinality oldValue = this.cardinality;
		if (this.cardinality == cardinality || this.cardinality != null
				&& this.cardinality.equals(cardinality))
			return;
		this.cardinality = cardinality;		
		this.setProperty(XMLElements.TYPE, cardinality.toString());
	}

	/**
	 * Returns the status of this relation. The default value, unless otherwise
	 * specified, is {@link ComponentStatus#INFERRED}.
	 * 
	 * @return the status of this relation.
	 */
	public ComponentStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets the status of this relation.
	 * 
	 * @param status
	 *            the new status of this relation.
	 * @throws AssociationException
	 *             if the keys at either end of the relation are incompatible
	 *             upon attempting to mark an
	 *             {@link ComponentStatus#INFERRED_INCORRECT} relation as
	 *             anything else.
	 */
	public void setStatus(final ComponentStatus status)
			throws AssociationException {
		Log.debug("Changing status of " + this + " to " + status);
		// If the new status is not incorrect, we need to make sure we
		// can legally do this, ie. the two keys have the same number of
		// columns each.
		if (!status.equals(ComponentStatus.INFERRED_INCORRECT))
			// Check both keys have same cardinality.
			if (this.firstKeyObj.getColumns().size() != this.secondKeyObj
					.getColumns().size())
				throw new AssociationException(Resources
						.get("keyColumnCountMismatch"));
		if (this.status == status || this.status != null
				&& this.status.equals(status))
			return;
		// Make the change.
		this.status = status;
	}
	
	
	/**
	 * Given a key that is in this relationship, return the other key.
	 * 
	 * @param key
	 *            the key we know is in this relationship.
	 * @return the other key in this relationship, or <tt>null</tt> if the key
	 *         specified is not in this relationship.
	 */
	public Key getOtherKey(final Key key) {
		return this.firstKeyObj.equals(key) ? this.secondKeyObj : this.firstKeyObj;
	}

	public Key getFirstKey() {
		if(this.firstKeyObj!=null)
			return this.firstKeyObj;
		else {
			if(this.parent instanceof Mart)
				return ((Mart)this.parent).getTableByName(this.getPropertyValue(XMLElements.FIRSTTABLE)).getPrimaryKey();
			else if(this.parent instanceof SourceSchema)
				return ((SourceSchema)this.parent).getTableByName(this.getPropertyValue(XMLElements.FIRSTTABLE)).getPrimaryKey();
			else
				return null;
		}			
	}
	
	public Key getSecondKey() {
		if(this.secondKeyObj!=null)
			return this.secondKeyObj;
		else {
			if(this.parent instanceof Mart)
				return ((Mart)this.parent).getTableByName(this.getPropertyValue(XMLElements.SECONDTABLE))
					.getForeignKeyByName(this.getPropertyValue(XMLElements.SECONDKEY));
			else if(this.parent instanceof SourceSchema)
				return ((SourceSchema)this.parent).getTableByName(this.getPropertyValue(XMLElements.SECONDTABLE))
					.getForeignKeyByName(this.getPropertyValue(XMLElements.SECONDKEY));
			else
				return null;
		}			
	}
	
	/**
	 * Returns the cardinality of the foreign key end of this relation, in a 1:M
	 * relation. In 1:1 relations this will always return 1, and in M:M
	 * relations it will always return M.
	 * 
	 * @return the cardinality of the foreign key end of this relation, in 1:M
	 *         relations only. Otherwise determined by the relation type.
	 */
	public Cardinality getCardinality() {
		return this.cardinality;
	}

	/**
	 * Returns the original cardinality of the foreign key end of this relation,
	 * in a 1:M relation. In 1:1 relations this will always return 1, and in M:M
	 * relations it will always return M.
	 * 
	 * @return the original cardinality of the foreign key end of this relation,
	 *         in 1:M relations only. Otherwise determined by the relation type.
	 */
	public Cardinality getOriginalCardinality() {
		return this.originalCardinality;
	}

	
	/**
	 * Returns <tt>true</tt> if this is a 1:1 relation.
	 * 
	 * @return <tt>true</tt> if this is a 1:1 relation, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isOneToOne() {
		return this.cardinality == Cardinality.ONE;
	}
	
	/**
	 * Returns <tt>true</tt> if this is either kind of 1:M relation.
	 * 
	 * @return <tt>true</tt> if this is either kind of 1:M relation,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean isOneToMany() {
		return this.cardinality == Cardinality.MANY_A;
	}
	
	/**
	 * In a 1:M relation, this will return the M end of the relation. In all
	 * other relation types, this will return <tt>null</tt>.
	 * 
	 * @return the key at the many end of the relation, or <tt>null</tt> if
	 *         this is not a 1:M relation.
	 */
	public Key getManyKey() {
		return this.secondKeyObj;
	}

	public static boolean isRelationExist(Key firstKey, Key secondKey) {
		for (final Iterator<Relation> f = secondKey.getRelations().iterator(); f
				.hasNext();) {
			// Obtain the next relation.
			final Relation candidateRel =  f.next();

			// a) a relation already exists between the FK
			// and the PK.
			//if (candidateRel.getOtherKey(secondKey).isKeyEquals(firstKey)) {
			if(candidateRel.getOtherKey(secondKey).equals(firstKey)) {
				return true;
			}
		}
		for (final Iterator<Relation> f = firstKey.getRelations().iterator(); f
				.hasNext();) {
			// Obtain the next relation.
			final Relation candidateRel =  f.next();
		
			// a) a relation already exists between the FK
			// and the PK.
			//if (candidateRel.getOtherKey(secondKey).isKeyEquals(firstKey)) {
			if(candidateRel.getOtherKey(firstKey).equals(secondKey)) {
				return true;
			}
		}
		/*
		 * FIXME need to remove the column objects in key
		 * 
		 */
		if(firstKey.getColumns().size() != secondKey.getColumns().size())
			return true; 
		return false;
	}
	
	
	/**
	 * Subclass this relation.
	 * just set the value, doesn't fire anything
	 */
	public void setSubclassRelation(final boolean subclass, final String inMart) throws ValidationException {
		String tmp = inMart;
		if(McUtils.isStringEmpty(inMart))
			tmp = Relation.DATASET_WIDE;
		final boolean oldValue = this.isSubclassRelation(tmp);
		if (subclass == oldValue)
			return;
		if (subclass) {
			// Work out the child end of the relation - the M end. The parent is
			// the 1 end.
			final Table parentTable = this.getFirstKey().getTable();
			final Table childTable = this.getSecondKey().getTable();
			if (parentTable.equals(childTable))
				throw new ValidationException(Resources
						.get("subclassNotBetweenTwoTables"));
			if (parentTable.getPrimaryKey() == null
					|| childTable.getPrimaryKey() == null)
				throw new ValidationException(Resources
						.get("subclassTargetNoPK"));

			// We need to test if the selected relation links to
			// a table which itself has subclass relations, or
			// is the central table, and has not got an
			// existing subclass relation in the direction we
			// are working in.
			boolean hasConflict = false;
			final Set<Relation> combinedRels = new HashSet<Relation>();
			combinedRels.addAll(parentTable.getRelations());
			combinedRels.addAll(childTable.getRelations());
			for (final Iterator<Relation> i = combinedRels.iterator(); i.hasNext()
					&& !hasConflict;) {
				final Relation rel =  i.next();
				if (!rel.isSubclassRelation(tmp))
					continue;
				else if (rel.getFirstKey().getTable().equals(parentTable)
						|| rel.getSecondKey().getTable().equals(childTable))
					hasConflict = true;
			}
			// If child has M:1 or parent has 1:M, we cannot do this.
			if (hasConflict)
				throw new ValidationException(Resources
						.get("mixedCardinalitySubclasses"));

			// Now do it.
			this.dsForSubRelation.add(tmp);
		} else {
			// Break the chain first.
			this.dsForSubRelation.remove(tmp);
/*			final Key key = this.getManyKey();
			if (key != null) {
				final Table target = key.getTable();
				Mart mart = ((DatasetTable)target).getMart();
				if (!target.equals(mart.getMainTable()))
					if (target.getPrimaryKey() != null)
						for (final Iterator<Relation> i = target.getPrimaryKey()
								.getRelations().iterator(); i.hasNext();) {
							final Relation rel = i.next();
							if (rel.isOneToMany())
								rel.setSubclassRelation(false,tmp);
						}
			}*/


		}
	}


	/**
	 * for a source relation, need a mart parameter because a source can be used in 
	 * multiple mart. no needed in target relation.
	 * @param dataset
	 * @return
	 */
	public boolean isSubclassRelation(String mart) {
		String inMart = mart;
		if(McUtils.isStringEmpty(mart))
			inMart = Relation.DATASET_WIDE;
		return this.dsForSubRelation.contains(inMart);
	}


	/**
	 * Returns the key in this relation associated with the given table. 
	 * 
	 * @param table
	 *            the table to get the key for.
	 * @return the key for that table. <tt>null</tt> if neither key is from
	 *         that table.
	 */
	public Key getKeyForTable(final Table table) {
		return this.getFirstKey().getTable().equals(table) ? this.getFirstKey()
				: this.getSecondKey();
	}
	
	
	public Column getFirstKeyColumnForTable(Table table) {
		Key key = this.getKeyForTable(table);
		return key.getColumns().get(0);
	}
	
	public Column getFirstKeyColumnForOtherTable(Table table) {
		Key key = this.getKeyForTable(table);
		Key otherKey = this.getOtherKey(key);
		return otherKey.getColumns().get(0);
	}

	@Override
	public  void synchronizedFromXML() {
		DatasetTable ft = ((Mart)this.getParent()).getTableByName(this.getPropertyValue(XMLElements.FIRSTTABLE));
		DatasetTable st = ((Mart)this.getParent()).getTableByName(this.getPropertyValue(XMLElements.SECONDTABLE));
		if(ft==null) {
			Log.debug("cannot find table "+this.getPropertyValue(XMLElements.FIRSTTABLE));
			this.setObjectStatus(ValidationStatus.INVALID);
			return;
		}
		if(st == null) {
			Log.debug("relation error: "+this.getPropertyValue(XMLElements.FIRSTTABLE)+
					" "+this.getPropertyValue(XMLElements.SECONDTABLE));
			this.setObjectStatus(ValidationStatus.INVALID);
			return;			
		}
			
		boolean isSubRelation = (!(ft.getType().
				equals(DatasetTableType.DIMENSION)) && (!st.getType().
						equals(DatasetTableType.DIMENSION)));

		PrimaryKey fKey = ft.getPrimaryKey();
		ForeignKey sKey = st.getForeignKeyByName(this.getPropertyValue(XMLElements.SECONDKEY));
		
		String firstKeyName = this.getPropertyValue(XMLElements.FIRSTKEY);
		if (fKey == null || !fKey.getName().equals(firstKeyName) || sKey == null) {
			//should not go here
			Log.debug("*** relation error: CHECK in mart "+this.getParent().getName() +  
					" ft "+this.getPropertyValue(XMLElements.FIRSTTABLE) + " fk "+
					firstKeyName + " st "+st.getName() + " sk " +
					this.getPropertyValue(XMLElements.SECONDKEY) + 
					" (" + (fKey == null) + ", " + (sKey == null) + ", " + (fKey==null ? null : fKey.getName()) + ")"
					);
			JOptionPane.showMessageDialog(null, "invalid relation found");
		} else {	
			this.firstKeyObj = fKey;
			this.secondKeyObj = sKey;
			try {
				this.setOriginalCardinality(Cardinality.valueFrom(this.getPropertyValue(XMLElements.TYPE)));
				this.setCardinality(Cardinality.valueFrom(this.getPropertyValue(XMLElements.TYPE)));
				this.setStatus(ComponentStatus.INFERRED);
				if(isSubRelation)  {
					this.setSubclassRelation(true,Relation.DATASET_WIDE);
				}
			} catch (AssociationException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fKey.addRelation(this);
			sKey.addRelation(this);
		}
		
		this.setObjectStatus(ValidationStatus.VALID);
	}

	public String toToolTipString() {
		return this.toString()+"("+this.cardinality.toString()+")";
	}

	
}
