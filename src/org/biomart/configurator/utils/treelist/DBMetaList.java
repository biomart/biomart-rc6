package org.biomart.configurator.utils.treelist;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;

public class DBMetaList extends LeafCheckBoxList implements ListSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBMetaPanel parentPanel;
	
	public DBMetaList(DBMetaPanel parent) {
		this.parentPanel = parent;
		this.addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		boolean adjust = event.getValueIsAdjusting();
		if (!adjust) {
			final DBCheckBoxNode obj = (DBCheckBoxNode)this.getSelectedValue();
			if(obj == null)
				return;
			if(obj.hasTables()) {
				this.restoreTables(obj);
			} else {	
				//set progressbar
				final ProgressDialog progressMonitor = ProgressDialog.getInstance();				

				final SwingWorker worker = new SwingWorker() {
					public Object construct() {
						try {
							parentPanel.setItems(obj.getText(),false);
							saveCurrentSelectedTables(obj);
						} catch (final Throwable t) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									StackTrace.showStackTrace(t);
								}
							});
						}finally {
							progressMonitor.setVisible(false);
						//	progressMonitor.dispose();					
						}
						return null;
					}

					public void finished() {
						// Close the progress dialog.
						progressMonitor.setVisible(false);
						//progressMonitor.dispose();
					}
				};
				
				worker.start();
				progressMonitor.start("processing ...");

			}

		}		
	}
	

	
	private void restoreTables(DBCheckBoxNode node) {
		this.parentPanel.getCheckBoxList().setItems(node.getSubNodes());
	}
	
	private void saveCurrentSelectedTables(DBCheckBoxNode node) {
		node.clearTables();
		for(int i=0; i<this.parentPanel.getCheckBoxList().getModel().getSize(); i++) {
			node.addTable((LeafCheckBoxNode)this.parentPanel.getCheckBoxList().getModel().getElementAt(i));
		}
	}

}