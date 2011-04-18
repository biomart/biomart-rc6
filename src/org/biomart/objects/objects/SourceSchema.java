package org.biomart.objects.objects;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.biomart.common.exceptions.FunctionalException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.JDBCDataLink;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.type.McNodeType;
import org.jdom.Element;

public class SourceSchema extends MartConfiguratorObject implements JDBCDataLink {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Table> tableList;
	private JdbcLinkObject conObj;
	/*
	 * for reading from element only
	 */
	private List<Relation> tmpRelations;
	
	public SourceSchema(JdbcLinkObject conObj) {
		super(conObj.getDatabaseName());
		//this.parent = mart;
		this.conObj = conObj;
		tableList = new ArrayList<Table>();
		this.setNodeType(McNodeType.SCHEMA);
	}

	/*
	 * should be used by fake schema only
	 */
	public SourceSchema(String name) {
		super(name);
		//this.parent = mart;
		this.conObj = conObj;
		tableList = new ArrayList<Table>();
		this.setNodeType(McNodeType.SCHEMA);		
	}

	public SourceSchema(Element element) {
		super(element);
		tableList = new ArrayList<Table>();
		this.setNodeType(McNodeType.SCHEMA);
		
		//tables
		List<Element> tableElements = element.getChildren(XMLElements.TABLE.toString());
		for(Element tableElement: tableElements) {
			SourceTable st = new SourceTable(tableElement);
			this.addTable(st);		
		}
		//relation
		@SuppressWarnings("unchecked")
		List<Element> relationElementList = element.getChildren(XMLElements.RELATION.toString());
		this.tmpRelations = new ArrayList<Relation>();
		for(Element relationElement: relationElementList) {
			RelationSource relation = new RelationSource(relationElement);
			relation.setParent(this);
			this.tmpRelations.add(relation);
		}
	}
	
	@Override
	public Element generateXml() throws FunctionalException {
		Element element =new Element(XMLElements.SOURCESCHEMA.toString());
		element.setAttribute(XMLElements.NAME.toString(),this.getName());
		element.setAttribute(XMLElements.INTERNALNAME.toString(),this.getInternalName());
		element.setAttribute(XMLElements.DISPLAYNAME.toString(),this.getDisplayName());
		element.setAttribute(XMLElements.DESCRIPTION.toString(),this.getDescription());
		element.setAttribute(XMLElements.ID.toString(),this.getPropertyValue(XMLElements.ID));

		for(Table table: this.tableList) {
			element.addContent(table.generateXml());
		}		
		for(Relation relation: this.getRelations()) {
			element.addContent(relation.generateXml());
		}
		return element;
	}
	
	public void addTable(Table table) {
		this.tableList.add(table);
		table.setParent(this);
	}

	public List<Table> getTables() {
		return this.tableList;
	}

	public int hashCode() {
		return this.getName().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof SourceSchema) {
			final SourceSchema t = (SourceSchema) o;
			return (this.getName())
					.equals(t.getName());
		} else
			return false;
	}
	
	public Set<Relation> getRelations() {
		Set<Relation> result = new TreeSet<Relation>();	// Tree for order
		for(Table table:this.getTables()) {
			result.addAll(table.getRelations());
		}
		return result;
	}

	public Table getTableByName(String name) {
		for(Table table: this.tableList) {
			if(table.getName().equals(name))
				return table;
		}
		return null;
	}
	
	public void removeTableByName(String name) {
		Table table = this.getTableByName(name);
		if(table!=null)
			this.tableList.remove(table);
	}

	@Override
	public void synchronizedFromXML() {
		for(Table table: this.getTables()) {
			table.synchronizedFromXML();
		}
		
		for(Relation r: this.tmpRelations  ) {
			r.synchronizedFromXML();
		}
		Collections.sort(this.tmpRelations);
		
		//update connectionObj
		Dataset ds = ((Mart)this.getParent()).getDatasetByName(this.getName());
		if(ds!=null)
			this.conObj = ds.getDataLinkInfoNonFlip().getJdbcLinkObject();
	}

	public JdbcLinkObject getJdbcLinkObject() {
		return this.conObj;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Log.debug("Establishing JDBC connection");
		// Start out with no driver at all.
		Class loadedDriverClass = null;

		// Try the system class loader instead.
		try {
			loadedDriverClass = Class.forName(this.conObj.getJdbcType().getDriverClassName());
		} catch (final ClassNotFoundException e) {
			final SQLException e2 = new SQLException();
			e2.initCause(e);
			throw e2;
		}

		// Check it really is an instance of Driver.
		if (!Driver.class.isAssignableFrom(loadedDriverClass))
			throw new ClassCastException(Resources
					.get("driverClassNotJDBCDriver"));

		// Connect!
		final Properties properties = new Properties();
		properties.setProperty("user", this.conObj.getUserName());
		if (!this.conObj.getPassword().equals(""))
			properties.setProperty("password", this.conObj.getPassword());
		properties.setProperty("nullCatalogMeansCurrent", "false");
		/*this.connection = DriverManager.getConnection(
				overrideDataLinkSchema == null ? this.conObj.getJdbcUrl(): 
					(this.conObj.getJdbcUrl())
						.replaceAll(this.getDataLinkSchema(),
								overrideDataLinkSchema), properties);*/
		
		Connection connection = DriverManager.getConnection(this.conObj.getJdbcUrl(),
				conObj.getUserName(),conObj.getPassword());
		return connection;
	}

	@Override
	public String getDataLinkDatabase() {
		return this.conObj.getDatabaseName();
	}

	@Override
	public String getDataLinkSchema() {
		return this.conObj.getSchemaName();
	}

	@Override
	public String getDriverClassName() {
		return this.conObj.getJdbcType().getDriverClassName();
	}

	@Override
	public String getPassword() {
		return this.conObj.getPassword();
	}

	@Override
	public String getUrl() {
		return this.conObj.getJdbcUrl();
	}

	@Override
	public String getUsername() {
		return this.conObj.getUserName();
	}

	@Override
	public void setDataLinkDatabase(String databaseName) {
		
	}

	@Override
	public void setDataLinkSchema(String schemaName) {

		
	}
	
	public int getUniqueId() {
		String id = this.getPropertyValue(XMLElements.ID);
		if("".equals(id))
			return 0;
		else
			return Integer.parseInt(id);
	}
	
	public void setUniqueId(int id) {
		this.setProperty(XMLElements.ID, ""+id);
	}

	/**
	 * create a copy of myself.
	 * @return
	 */
	public SourceSchema cloneMyself() {
		
		return null;
	}
}