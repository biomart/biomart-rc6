package org.biomart.configurator.controller.dialects;



import java.util.Set;
import org.biomart.configurator.model.MartConstructorAction.UpdateOptimiser;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.type.JdbcType;


/**
 * Must be mart within a schema (same or not) in same database, otherwise get message: 
 * 		"ERROR:  cross-database references are not implemented: ..."
 */	
class PgDialect extends DatabaseDialect {


	@Override
	public Set<String> getPartitionedTables(JdbcLinkObject conObj,
			String partitionBase) {
		// TODO Auto-generated method stub
		return null;
	}
	public PgDialect() {
		super(JdbcType.PostGreSQL);
	}
	@Override
	protected String buildGroupConcat(UpdateOptimiser action, String fromTable) throws Exception {
		throw new Exception("group_concat not implemented in MartConfigurator and for postgres");	//TODO
	}
	@Override
	protected final String addRenameStatement(final String schemaName, final String oldTableName, final String newTableName) {
		return ALTER + " " + TABLE + " " + schemaName + "." + oldTableName + " " + RENAME + " " + TO + " " + newTableName + "";
	}
	
}