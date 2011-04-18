package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.biomart.backwardCompatibility.DatasetFromUrl;
import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.controller.dialects.DialectFactory;
import org.biomart.configurator.controller.dialects.McSQL;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.DataLinkType;
import org.biomart.configurator.utils.type.JdbcType;

public class FileMetaPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileMetaList urlMetaList;
	private LeafCheckBoxList checkBoxList;
	
	private List<MartInVirtualSchema> martList;
	private LeafCheckBoxList subList;

	
	public FileMetaPanel() {
		init();
	}
	
	private void init() {
		this.setLayout(new GridLayout());
		JPanel martsPanel = new JPanel(new BorderLayout());
		JLabel martLabel = new JLabel(Resources.get("MARTS"));
	    this.checkBoxList = new LeafCheckBoxList();	
		urlMetaList = new FileMetaList(this);
	    JScrollPane scrollPane = new JScrollPane(urlMetaList);
	    martsPanel.add(scrollPane,BorderLayout.CENTER);
	    martsPanel.add(martLabel,BorderLayout.NORTH);
	    martsPanel.setBorder(BorderFactory.createEtchedBorder());
	    this.add(martsPanel);
	    
		JPanel dsPanel = new JPanel(new BorderLayout());
		JLabel dsLabel = new JLabel(Resources.get("DATASETS"));
    
	    JScrollPane listScrollPane = new JScrollPane(checkBoxList);
	    listScrollPane.setSize(scrollPane.getSize());
	    dsPanel.add(listScrollPane,BorderLayout.CENTER);
	    dsPanel.add(dsLabel,BorderLayout.NORTH);
	    dsPanel.setBorder(BorderFactory.createEtchedBorder());
	    this.add(dsPanel);
	}
		
	public void setMartList(List<MartInVirtualSchema> list) {
		this.martList = list;
		this.updateList(list, false);
	}
	
	private void updateList(List<MartInVirtualSchema> list, boolean withDataset) {
		List<LeafCheckBoxNode> nodeList = new ArrayList<LeafCheckBoxNode>();
		if(list!=null) {
			for(MartInVirtualSchema item:list) {
				FileCheckBoxNode node = new FileCheckBoxNode(item,false);
				nodeList.add(node);
			}
		}
		CheckBoxListModel model = (CheckBoxListModel)this.urlMetaList.getModel();
		model.clear();
		for(LeafCheckBoxNode node: nodeList) {
			model.addElement(node);
		}	
	}
	
	
	public List<MartInVirtualSchema> getMartList() {
		return this.martList;
	}
	
	public LeafCheckBoxList getCheckBoxList() {
		return this.checkBoxList;
	}
	
	/**
	 * if all=false, only selected tables return;
	 * @param all
	 * @return
	 */
	public Map<MartInVirtualSchema, List<DatasetFromUrl>> getDatasetsInfo07(boolean all) {
		Map<MartInVirtualSchema, List<DatasetFromUrl>>dbInfo = new LinkedHashMap<MartInVirtualSchema, List<DatasetFromUrl>>();
		for(Object selected: this.urlMetaList.getCheckedValues()) {
			FileCheckBoxNode cbn = (FileCheckBoxNode)selected;
			MartInVirtualSchema ms = (MartInVirtualSchema)cbn.getUserObject();
			dbInfo.put(ms,cbn.getDatasetsForUrl(all));			
		}
		return dbInfo;
	}

	private void saveCurrentSelectedDataSets(FileCheckBoxNode node) {
		node.clearTables();
		for(int i=0; i<this.getCheckBoxList().getModel().getSize(); i++) {
			node.addTable((LeafCheckBoxNode)this.getCheckBoxList().getModel().getElementAt(i));
		}
	}
	
	public void restoreDataSets(FileCheckBoxNode node) {		
		this.getCheckBoxList().setItems(node.getSubNodes());
	}

	public void setItems(FileCheckBoxNode node) {
		MartInVirtualSchema martV = (MartInVirtualSchema)node.getUserObject();
		List<LeafCheckBoxNode> tables = null;
		if(martV.isURLMart()) {
			tables = this.getSubNodesFromUrl(martV);
		} else {
			//db
			JdbcLinkObject conObject = martV.getJdbcLinkObject();
			tables = getSubNodesFromDB(conObject,martV.getSchema());					
		}
		this.checkBoxList.setItems(tables);	
		//save
		this.saveCurrentSelectedDataSets(node);
	}
	
	
	
	
	
	private List<LeafCheckBoxNode> getSubNodesFromDB(JdbcLinkObject conObject, String schemaName) {
			return DialectFactory.getDialect(conObject.getJdbcType()).
				getMetaTablesFromOldConfig(conObject, schemaName, true);
	
	}
	
	private List<LeafCheckBoxNode> getSubNodesFromUrl(MartInVirtualSchema martV){
		List<LeafCheckBoxNode> dsList = new ArrayList<LeafCheckBoxNode>();
		List<DatasetFromUrl> dss;
		try {
			dss = McGuiUtils.INSTANCE.getDatasetsFromUrlForMart(martV);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, Resources.get("NODATASETS"), "error", JOptionPane.ERROR_MESSAGE);
			return dsList;
		}
		for(DatasetFromUrl ds: dss) {
			LeafCheckBoxNode cbn = new LeafCheckBoxNode(ds.getName(),true);
			cbn.setUserObject(ds);
			dsList.add(cbn);
		}
		return dsList;
	}

}