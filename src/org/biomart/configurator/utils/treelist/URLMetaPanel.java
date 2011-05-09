package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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

public class URLMetaPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URLMetaList urlMetaList;
	private LeafCheckBoxList checkBoxList;
	
	private String baseUrl;
	private List<MartInVirtualSchema> martList;
	private LeafCheckBoxList subList;
	private URLDataLinkPanel parentPanel;
	
	public URLDataLinkPanel getParentPanel() {
		return parentPanel;
	}

	public URLMetaPanel(URLDataLinkPanel parent) {
		parentPanel = parent;
		init();
	}
	
	private void init() {
		this.setLayout(new GridLayout());
		JPanel martsPanel = new JPanel(new BorderLayout());
		JLabel martLabel = new JLabel(Resources.get("MARTS"));
	    this.checkBoxList = new LeafCheckBoxList();	
		urlMetaList = new URLMetaList(this);
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
	
	public void setBaseUrlString(String value) {
		this.baseUrl = value;
	}
	
	public void setMartList(List<MartInVirtualSchema> list) {
		this.martList = list;
	}
	
	public void updateList(List<String> list, boolean withDataset) {
		List<DBCheckBoxNode> nodeList = new ArrayList<DBCheckBoxNode>();
		if(list!=null) {
			for(String item:list) {
				DBCheckBoxNode node = new DBCheckBoxNode(item,false);
				nodeList.add(node);
			}
		}
		CheckBoxListModel model = (CheckBoxListModel)this.urlMetaList.getModel();
		model.clear();
		for(DBCheckBoxNode node: nodeList) {
			model.addElement(node);
		}	
	}
	
	public void updateList(Map<String,List<String>> map) {
		List<DBCheckBoxNode> nodeList = new ArrayList<DBCheckBoxNode>();
		if(map!=null) {
			for(Map.Entry<String, List<String>> entry: map.entrySet()) {
				String martName = entry.getKey();
				DBCheckBoxNode node = new DBCheckBoxNode(martName, false);
				nodeList.add(node);
				for(String mpName: entry.getValue()) {
					LeafCheckBoxNode mpNode = new LeafCheckBoxNode(mpName,false);
					node.addTable(mpNode);
				}
			}
		}
		
		CheckBoxListModel model = (CheckBoxListModel)this.urlMetaList.getModel();
		model.clear();

		//JTree.DynamicUtilTreeNode.createChildren(dbnode, nodesVector);
		for(DBCheckBoxNode cbn: nodeList) {
			model.addElement(cbn);
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
	public Map<MartInVirtualSchema, List<DatasetFromUrl>> getDBInfo(boolean all) {

		Map<MartInVirtualSchema, List<DatasetFromUrl>>dbInfo = new LinkedHashMap<MartInVirtualSchema, List<DatasetFromUrl>>();
		for(Object selected: this.urlMetaList.getCheckedValues()) {
			DBCheckBoxNode cbn = (DBCheckBoxNode)selected;
			MartInVirtualSchema ms = this.getMartInVirtualSchema(cbn.getText());
			dbInfo.put(ms,cbn.getDatasetsForUrl(all));			
		}
		return dbInfo;
	}

	public MartInVirtualSchema getMartInVirtualSchema(String name) {
		for(MartInVirtualSchema ms: this.martList) {
			if(ms.getName().equals(name))
				return ms;
		}
		return null;
	}
	
	public Map<String,List<String>> getMpList(boolean all) {
		Map<String,List<String>> mpList = new HashMap<String,List<String>>();
		for(Object obj: this.urlMetaList.getCheckedValues()) {
			DBCheckBoxNode cbn = (DBCheckBoxNode)obj;

			List<String> list = new ArrayList<String>();
			for(LeafCheckBoxNode sub: cbn.getSubNodes()) {
				if(sub.isSelected()) {
					list.add(sub.getText());
				}
			}
			if(!list.isEmpty())
				mpList.put(cbn.getText(), list);
					
		}
		return mpList;
	}

	public void checkAll() {
		for(Object obj: this.urlMetaList.getSelectedValues()) {
			DBCheckBoxNode cbn = (DBCheckBoxNode)obj;
			cbn.setSelected(true);
			if(cbn.hasTables()) {
				this.getCheckBoxList().setItems(cbn.getSubNodes());
				for(int j=0; j<this.checkBoxList.getItems(true).size(); j++) {
					((DBCheckBoxNode)this.checkBoxList.getModel().getElementAt(j)).setSelected(true);
				}
			} else {			
				List<LeafCheckBoxNode> tables = this.urlMetaList.getDataSets(cbn.getText(),true);
				this.checkBoxList.setItems(tables);			
			}		
			this.saveCurrentSelectedDataSets(cbn);
		}
	}

	public void saveCurrentSelectedDataSets(DBCheckBoxNode node) {
		node.clearTables();
		for(int i=0; i<this.getCheckBoxList().getModel().getSize(); i++) {
			node.addTable((DBCheckBoxNode)this.getCheckBoxList().getModel().getElementAt(i));
		}
	}
	
	public void restoreDataSets(DBCheckBoxNode node) {		
		this.getCheckBoxList().setItems(node.getSubNodes());
	}

}