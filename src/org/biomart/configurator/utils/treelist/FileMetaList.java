package org.biomart.configurator.utils.treelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biomart.backwardCompatibility.DatasetFromUrl;
import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.biomart.common.resources.Resources;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.utils.McGuiUtils;


public class FileMetaList extends LeafCheckBoxList implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileMetaPanel parentPanel;
	
	public FileMetaList(FileMetaPanel parent) {
		this.parentPanel = parent;
		this.addListSelectionListener(this);
	}



	@Override
	public void valueChanged(ListSelectionEvent event) {
		boolean adjust = event.getValueIsAdjusting();
		if (!adjust) {
			final FileCheckBoxNode obj = (FileCheckBoxNode)this.getSelectedValue();
			if(!obj.isSelected())
				return;
			if(obj.hasTables()) {
				this.restoreTables(obj);
			} else {
				//set progressbar
				final ProgressDialog progressMonitor = ProgressDialog.getInstance();				

				final SwingWorker worker = new SwingWorker() {
					public Object construct() {
						try {
							setItem(obj);
						} catch (final Throwable t) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									StackTrace.showStackTrace(t);
								}
							});
						}finally {
							progressMonitor.setVisible(false);			
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
	
	private void restoreTables(FileCheckBoxNode node) {
		this.parentPanel.getCheckBoxList().setItems(node.getSubNodes());
	}

	private void setItem(FileCheckBoxNode node) {
		this.parentPanel.setItems(node);
	}
}