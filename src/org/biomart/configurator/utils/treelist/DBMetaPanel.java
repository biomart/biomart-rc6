package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.controller.dialects.DialectFactory;
import org.biomart.configurator.controller.dialects.McSQL;
import org.biomart.configurator.utils.ConnectionPool;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.DataLinkType;

public class DBMetaPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBMetaList dbmetaList;
	private LeafCheckBoxList checkBoxList;
	private boolean useOldConfig = false;
	private DataLinkType dlt;
	private JdbcLinkObject conObject;
	private Map<String, List<String>> treelistInfoMap;
	
	public DBMetaPanel() {
		init();
	}
	
	private void init() {
		this.setLayout(new GridLayout());
		JPanel martsPanel = new JPanel(new BorderLayout());
		JLabel martLabel = new JLabel(Resources.get("SCHEMASLABEL"));
	    this.checkBoxList = new LeafCheckBoxList();	
		dbmetaList = new DBMetaList(this);
	    JScrollPane scrollPane = new JScrollPane(dbmetaList);
	    martsPanel.add(scrollPane,BorderLayout.CENTER);
	    martsPanel.add(martLabel,BorderLayout.NORTH);
	    martsPanel.setBorder(BorderFactory.createEtchedBorder());
	    this.add(martsPanel);
	    
		JPanel dsPanel = new JPanel(new BorderLayout());
		JLabel dsLabel = new JLabel(Resources.get("TABLESLABEL"));
    
	    JScrollPane listScrollPane = new JScrollPane(checkBoxList);
	    //listScrollPane.setSize(scrollPane.getSize());
	    dsPanel.add(listScrollPane,BorderLayout.CENTER);
	    dsPanel.add(dsLabel,BorderLayout.NORTH);
	    dsPanel.setBorder(BorderFactory.createEtchedBorder());
	    this.treelistInfoMap = new HashMap<String,List<String>>();
	    this.add(dsPanel);
	}

	public void updateList(List<String> list) {
		List<DBCheckBoxNode> nodeList = new ArrayList<DBCheckBoxNode>();
		if(!McUtils.isCollectionEmpty(list)) {
			for(String item:list) {
				DBCheckBoxNode node = new DBCheckBoxNode(item,false);
				nodeList.add(node);
			}
		}
		
		//FIXME check here, why a new model
		CheckBoxListModel model = new CheckBoxListModel();
		//CheckBoxListModel model = (CheckBoxListModel)this.dbmetaList.getModel();
		model.clear();
		for(DBCheckBoxNode node: nodeList) {
			model.addElement(node);
		}	
		
		this.dbmetaList.setModel(model);
	}
	
	public boolean getUseOldConfigFlag() {
		return this.useOldConfig;
	}

	public void setDataLinkType(DataLinkType type){
		this.dlt = type;
	}
	
	public DataLinkType getDataLinkType() {
		return this.dlt;
	}
	
	public void updateList(JdbcLinkObject conObject) {
		this.conObject = conObject;
		List<String> databases = this.getDatabases();		
		this.updateList(databases);
	}

	public List<String> getDatabases() {
		List<String> result = new ArrayList<String>();
		Pattern p = null;
		if(this.conObject.getPartitionRegex()!=null && this.conObject.getPtNameExpression()!=null)
		try {
			p = Pattern.compile(this.conObject.getPartitionRegex());
		} catch (final PatternSyntaxException e) {
			// Ignore and return if invalid.
			return new ArrayList<String>();
		}

		Connection con = ConnectionPool.Instance.getConnection(this.conObject);
		if(con==null)
			return new ArrayList<String>();
		try {
			DatabaseMetaData dmd = con.getMetaData();			
			ResultSet rs2 = this.conObject.useSchema()? dmd.getSchemas(): dmd.getCatalogs();
			//ResultSet rs2 = dmd.getSchemas();
			//clean all
			//check the first one is information_schema
//			rs2.next();
//			String schemaName = rs2.getString(1);
			
//			if(!"information_schema".equals(rs2.getString(1)))
//				this.treeItemStrList.add(rs2.getString(1));
			while (rs2.next()) {
				String schemaName = rs2.getString(1);
				if(this.conObject.getPartitionRegex()!=null && this.conObject.getPtNameExpression()!=null) {
					Matcher m = p.matcher(schemaName);
					if (m.matches()) {
						result.add(schemaName);
					}
				}else
					result.add(schemaName);
			}					
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		ConnectionPool.Instance.releaseConnection(this.conObject);
		return result;
	}

	/**
	 * table info in the current selected node is not stored, need special handle. 
	 */
	public Map<String, List<String>> getDBInfo(boolean all) {
		this.treelistInfoMap = new LinkedHashMap<String, List<String>>();

		for(Object obj: this.dbmetaList.getCheckedValues()) {	
			DBCheckBoxNode cbn = (DBCheckBoxNode)obj;
			this.treelistInfoMap.put(cbn.getText(),cbn.getTable(all));		
		}
		return this.treelistInfoMap;
	}

	public LeafCheckBoxList getCheckBoxList() {
		return this.checkBoxList;
	}
	
	public void setConnectionObject(JdbcLinkObject object) {
		this.conObject = object;
	}
	
	public void setItems(String schemaName, boolean isSelected) {
		List<LeafCheckBoxNode> tables = getTables(this.conObject,schemaName,isSelected);
		this.checkBoxList.setItems(tables);			
	}
	
	public List<LeafCheckBoxNode> getTables(JdbcLinkObject conObject, String schemaName, boolean isSelected) {
		if(this.dlt.equals(DataLinkType.SOURCE)) {
			McSQL mcsql = new McSQL();
			return mcsql.getTablesNodeFromSource(conObject, schemaName, isSelected);
		}
		else if(this.dlt.equals(DataLinkType.TARGET)) {
			McSQL mcsql = new McSQL();
			if(mcsql.hasOldConfigInTarget(conObject, schemaName)) {
				int option = JOptionPane.showConfirmDialog(null,
					    "Do you want to use the existing config?", "",
					    JOptionPane.YES_NO_OPTION);
				if(option == 0) {
					this.useOldConfig = true;
				}else {
					this.useOldConfig = false;
				}
			}else {
				this.useOldConfig = false;
			}
			
			if(this.useOldConfig) {
				isSelected = true;
				return DialectFactory.getDialect(this.conObject.getJdbcType()).
					getMetaTablesFromOldConfig(conObject, schemaName, isSelected);
			}else {
				return mcsql.getTablesNodeFromTarget(conObject, schemaName, isSelected);
			}			
		}
		return new ArrayList<LeafCheckBoxNode>();
	}

}