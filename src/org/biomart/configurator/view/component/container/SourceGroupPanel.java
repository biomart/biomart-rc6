package org.biomart.configurator.view.component.container;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.biomart.common.utils.PartitionUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.controller.ObjectController;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.MessageConfig;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.gui.dialogs.DataLinkDialog;
import org.biomart.configurator.view.gui.dialogs.DatasourceDialog;
import org.biomart.configurator.view.gui.dialogs.WarningDialog;
import org.biomart.configurator.view.idwViews.McViewPortal;
import org.biomart.configurator.view.idwViews.McViewSourceGroup;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.SourceContainer;
import org.biomart.objects.portal.UserGroup;


class SourceGroupPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SourceContainer sourceContainer;
	private final boolean isgrouped;
	
	SourceGroupPanel(SourceContainer sc, MouseListener ml, boolean selected, boolean isgrouped) {
		this.sourceContainer = sc;
		this.isgrouped = isgrouped;
		this.setLayout(new BorderLayout());
		ActionPanel ap = new ActionPanel(sc,ml,selected);
		this.setBackground(ActionPanel.lightblue);
		this.add(ap,BorderLayout.CENTER);
		this.add(getToolBar(),BorderLayout.EAST);
	    this.setBorder(BorderFactory.createEtchedBorder());
	}
	
	private JToolBar getToolBar() {
		JToolBar panel = new JToolBar();
		panel.setBackground(ActionPanel.lightblue);
		if(this.isgrouped) {
			JButton gpButton = new JButton("G");
			gpButton.setToolTipText("show group data source management");
			gpButton.setActionCommand("groupsourcemanagement");
			gpButton.addActionListener(this);
			panel.add(gpButton);
		}
		
		panel.setFloatable(false);
		return panel;
	}

	 private boolean isCreateLink(){
	    int value = MessageConfig.getInstance().showDialog("rebuildlink");
		if(value == 3) //no and don't warn me anymore
			return false;
		boolean create = false;
		if(value == 2)
			create = true; //yes and don't warn me anymore
		else {
			WarningDialog dialog = new WarningDialog("rebuildlink");
			dialog.showDialogFor("rebuildlink",	"create link from Backward Compatibility?");
			value = dialog.getResult();
			if(value == 0 || value == 2)
				create = true;
			else
				create = false;
		}
		return create;
    }
 
	public boolean addMarts(final UserGroup user, final String group) {
		final DataLinkInfo dlinkInfo = DataLinkDialog.showDialog();
		
		if(dlinkInfo==null)
			return false;
	
		final ProgressDialog progressMonitor = ProgressDialog.getInstance();
		final ObjectController oc = new ObjectController();
	
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					if(dlinkInfo.getUseOldConfigFlag())
						oc.initMarts(dlinkInfo,user,group, SourceGroupPanel.this.isCreateLink());
					else
						oc.initMarts(dlinkInfo,user,group, true);
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
			}
		};
		
		worker.start();
		progressMonitor.start("processing ...");
		return true;
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("groupsourcemanagement")) {
			// added cols orders to a new PtModel
			ArrayList<Integer> cols = new ArrayList<Integer>();
			cols.add(PartitionUtils.DATASETNAME);
			cols.add(PartitionUtils.DISPLAYNAME);
			cols.add(PartitionUtils.CONNECTION);
			cols.add(PartitionUtils.DATABASE);
			cols.add(PartitionUtils.SCHEMA);
			cols.add(PartitionUtils.HIDE);
			cols.add(PartitionUtils.KEY);

			List<Mart> marts = McGuiUtils.INSTANCE.getRegistryObject().getMartsInGroup(this.sourceContainer.getName());
			new DatasourceDialog(marts, cols,true);
		}
		
	}
	
}