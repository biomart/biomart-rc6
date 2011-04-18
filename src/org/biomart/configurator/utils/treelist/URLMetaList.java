package org.biomart.configurator.utils.treelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biomart.backwardCompatibility.DatasetFromUrl;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McGuiUtils;


public class URLMetaList extends LeafCheckBoxList implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URLMetaPanel parentPanel;
	
	public URLMetaList(URLMetaPanel parent) {
		this.parentPanel = parent;
		this.addListSelectionListener(this);
	}



	@Override
	public void valueChanged(ListSelectionEvent event) {
		boolean adjust = event.getValueIsAdjusting();
		if (!adjust) {
			DBCheckBoxNode obj = (DBCheckBoxNode)this.getSelectedValue();
			if(!obj.isSelected())
				return;
			if(obj.hasTables()) {
				this.parentPanel.restoreDataSets(obj);
			} else {			
				List<LeafCheckBoxNode> tables = this.getDataSets(obj.getText(),true);
				this.parentPanel.getCheckBoxList().setItems(tables);
				for(LeafCheckBoxNode table: tables) {
					obj.addTable(table);
				}
			}
		}
	}
	
	
	
	public List<LeafCheckBoxNode> getDataSets(String martName, boolean isSelected) {
		List<LeafCheckBoxNode> dsList = new ArrayList<LeafCheckBoxNode>();
		List<DatasetFromUrl> dss;
		try {
			dss = McGuiUtils.INSTANCE.getDatasetsFromUrlForMart(this.parentPanel.getMartInVirtualSchema(martName));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, Resources.get("NODATASETS"), "error", JOptionPane.ERROR_MESSAGE);
			return dsList;
		}
		for(DatasetFromUrl ds: dss) {
			LeafCheckBoxNode cbn = new LeafCheckBoxNode(ds.getName(),isSelected);
			cbn.setUserObject(ds);
			dsList.add(cbn);
		}
		return dsList;
	}

}