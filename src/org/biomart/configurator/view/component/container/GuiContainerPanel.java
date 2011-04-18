package org.biomart.configurator.view.component.container;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.xml.bind.annotation.XmlElements;

import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.utils.ModifiedFlowLayout;
import org.biomart.common.utils.WrapLayout;
import org.biomart.common.utils.XMLElements;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.controller.MartController;
import org.biomart.configurator.controller.ObjectController;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.component.ConfigComponent;
import org.biomart.configurator.view.dnd.ConfigDropTargetListener;
import org.biomart.configurator.view.dnd.ConfigDnDTransferHandler;
import org.biomart.configurator.view.gui.dialogs.AddConfigDialog;
import org.biomart.configurator.view.gui.dialogs.AddConfigFromMartDialog;
import org.biomart.configurator.view.gui.dialogs.ConfigDialog;
import org.biomart.configurator.view.gui.dialogs.ReportAttributesSelectDialog;
import org.biomart.configurator.view.idwViews.McViewPortal;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.configurator.view.menu.ContextMenuConstructor;
import org.biomart.objects.enums.GuiType;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.portal.GuiContainer;
import org.biomart.objects.portal.MartPointer;
import org.biomart.objects.portal.UserGroup;

import javax.swing.table.*;
import java.awt.*;

import e.gui.ETable;

public class GuiContainerPanel extends JPanel implements MouseListener,ClipboardOwner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GuiContainer gc;
	//private JPanel configPanel;
	private JPanel checkboxPanel;
	private JCheckBox hideCheckBox;
	private JCheckBox listViewCheckBox;
	private JButton addButton;
	private JButton rdfButton;
	private JList configList;
	private ETable configTable;
	
	public JTable getConfigTable() {
		return configTable;
	}

	public JList getConfigList() {
		return configList;
	}

	public GuiContainerPanel(GuiContainer gc) {
		this.gc = gc;
		init();
		//this.setTransferHandler(new ConfigDnDTransferHandler());
		this.setDropTarget(new DropTarget(GuiContainerPanel.this, new ConfigDropTargetListener(GuiContainerPanel.this)));
	}

	public GuiContainer getGuiContainer() {
		return this.gc;
	}
	
	public void regenerateRDF() {
		if(this.getSelectedMPs().size() == 0){
			JOptionPane.showMessageDialog(this, "Please select configs to generate RDF");
			return;
		}
		Object[] options = {"Yes", "No"};

		int n = JOptionPane.showOptionDialog(this,
			    Resources.get("RDFWARNING"),
			    "Warning",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[1]);
		if(n==0) {
			ObjectController oc = new ObjectController();
			for( MartPointer mp : this.getSelectedMPs()) {
				Config config = mp.getConfig();
				oc.generateRDF(MartController.getInstance().getMainAttribute(config.getMart()), config);
			}
		}
	}
	
	public void init() {
		this.setLayout(new BorderLayout());
		
		//configPanel = new JPanel();
		checkboxPanel = new JPanel();
		
		WrapLayout flo = new WrapLayout();
		//configPanel.setLayout(flo);
		flo.setAlignment(FlowLayout.LEFT);
		
		addButton = new JButton(Resources.get("ADDCONFIG"),McUtils.createImageIcon("images/add_group.gif"));
		//addButton.setToolTipText(Resources.get("ADDCONFIG"));
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addConfig();
			}			
		});
		
		this.rdfButton = new JButton(McUtils.createImageIcon("images/rdf.gif"));
        this.rdfButton.setText("Generate RDF");
		this.rdfButton.setActionCommand("rdf");
		this.rdfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				regenerateRDF();
			}			
		});
		
		checkboxPanel.add(addButton);
		//checkboxPanel.add(rdfButton);
		checkboxPanel.addMouseListener(this);
		
		//DefaultListModel model = new DefaultListModel();
		String[] colNames = {	
								"Name",
								"Description",
								"Source",								
								"Group"
							};
		SharedDataModel model = new SharedDataModel(colNames);
		for(MartPointer mp :this.gc.getMartPointerList()){
			if(Boolean.parseBoolean(Settings.getProperty("hidemaskedmp"))){
				if(mp.getConfig().isHidden())
					continue;
			}
			model.addElement(mp);
		}
		
		configList = new JList(model);
		configList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		configList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		configList.setVisibleRowCount(-1);
		configList.setDragEnabled(true);
		configList.setDropMode(DropMode.INSERT);
		//configList.setDropTarget(new DropTarget(configList, new ConfigDropTargetListener(GuiContainerPanel.this)));
		ConfigListTransferHandler listHandler = new ConfigListTransferHandler(this);
		configList.setTransferHandler(listHandler);
		this.setMappings(configList);
		configList.setAutoscrolls(true);
		ConfigListCellRenderer renderer = new ConfigListCellRenderer(true);
		configList.setCellRenderer(renderer);
		configList.addMouseListener(this);
		
		
		JScrollPane listsp = new JScrollPane(configList);
		//listsp.setPreferredSize(new Dimension(1050,750));
		//listsp.setAlignmentX(LEFT_ALIGNMENT);
		
		configTable = new ETable(model);
		configTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		configTable.setDragEnabled(true);
		configTable.setDropMode(DropMode.INSERT);
		//configTable.setDropTarget(new DropTarget(configTable, new ConfigDropTargetListener(GuiContainerPanel.this)));
		ConfigTableTransferHandler tableHandler = new ConfigTableTransferHandler(this);
		configTable.setTransferHandler(tableHandler);
		this.setMappings(configTable);
		configTable.addMouseListener(this);
		ConfigTableCellRenderer tableRenderer = new ConfigTableCellRenderer();
		configTable.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
		configTable.setRowHeight(20);
		configTable.setFont(new Font(this.getFont().getFamily(), Font.PLAIN, 14));		
		configTable.setAutoCreateRowSorter(false);
		
		
		JScrollPane tablesp = new JScrollPane(configTable);
		//tablesp.setPreferredSize(new Dimension(1050,750));
		
		//configPanel.setLayout(new BorderLayout());
		
		if(Boolean.parseBoolean(Settings.getProperty("portal.listview")))
			this.add(tablesp,BorderLayout.CENTER);
		else
			this.add(listsp,BorderLayout.CENTER);
		
		
		//this.add(configPanel,BorderLayout.CENTER);
		
		//add another panel for checkbox group at right
		
		//BoxLayout mflo = new BoxLayout(checkboxPanel,BoxLayout.Y_AXIS);
		
		addShowHideCheckBox(checkboxPanel);
		addListViewCheckBox(checkboxPanel);
		//checkboxPanel.setLayout(mflo);
		this.add(checkboxPanel,BorderLayout.NORTH);
		
	}
	
	/**
	 * @param configPanel
	 */
	private void addShowHideCheckBox(JPanel configPanel) {
		hideCheckBox = new JCheckBox("Show hidden configs");
  		hideCheckBox.setSelected(!Boolean.parseBoolean(Settings.getProperty("hidemaskedmp")));
  		hideCheckBox.setActionCommand("hidemaskedmp");
  		hideCheckBox.addActionListener(new ActionListener(){
  			@Override
			public void actionPerformed(ActionEvent e) {
				//addConfig();
  				showHideItems();
			}
  		});
  		configPanel.add(hideCheckBox);
	}
	
	private void addListViewCheckBox(JPanel configPanel){
		listViewCheckBox = new JCheckBox("List View");
		listViewCheckBox.setSelected(Boolean.parseBoolean(Settings.getProperty("portal.listview")));
		listViewCheckBox.setActionCommand("showlistview");
		listViewCheckBox.addActionListener(new ActionListener(){
  			@Override
			public void actionPerformed(ActionEvent e) {
				//addConfig();
  				showListView();
			}
  		});
  		configPanel.add(listViewCheckBox);
	}
	
	private void showListView(){
		String showFlag = Settings.getProperty("portal.listview");
		boolean show = Boolean.parseBoolean(showFlag);
		show = !show;
		Settings.setProperty("portal.listview", Boolean.toString(show));
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).refreshRootPane();
		
	}
	
	public void addConfig(ConfigComponent config) {
		//int count = this.configPanel.getComponentCount() - 1;
		//this.configPanel.add(config,count);
		
		//TODO
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).addConfigComponent(config);
		Set<Mart> linkedMart = this.getLinkedMartForConfig(config.getMartPointer().getConfig());
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getMPMartMap().put(
				config.getMartPointer(), linkedMart);
	}
	
	public void removeConfig(ConfigComponent config) {
		//this.configPanel.remove(config);
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).removeConfigComponent(config);
	}

	private void showHideItems() {
		String hideFlag = Settings.getProperty("hidemaskedmp");
		boolean iHide = Boolean.parseBoolean(hideFlag);
		iHide = !iHide;
		Settings.setProperty("hidemaskedmp", Boolean.toString(iHide));
		((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).refreshRootPane();
		
	}

	private void addConfig() {
		if(this.gc.getGuiType().equals(GuiType.get("martreport"))) {
			this.addReportConfig();
		}else
			this.addNaiveConfig();
	}
	
	private void addReportConfig() {
		List<MartPointer> marts = this.getGuiContainer().getAllMartPointerListResursively();
		
		AddConfigFromMartDialog dialog = new AddConfigFromMartDialog(McGuiUtils.INSTANCE.getRegistryObject(),true, marts);
		
		final List<Mart> selectedmarts = dialog.getSelectedMart();
		if(McUtils.isCollectionEmpty(selectedmarts))
			return;
		final Mart mart = selectedmarts.get(0);
    	this.addReportConfig(mart);	
    			
	}
	
	public void addReportConfigs(List<Mart> martList) {
		for(final Mart mart: martList) {
			this.addReportConfig(mart);
		}
	}
	
	public void addReportConfig(final Mart mart) {
		// should be 1 master config only
		if(mart.getConfigList().size() <= 1){
			JOptionPane.showMessageDialog(this, "Please create a new configuration for data source "+mart.getDisplayName()+" before building a report");
			return;
		}
		
		
    	final AddConfigDialog acd = new AddConfigDialog(mart,true);
    	final ObjectController oc = new ObjectController();
    	if(acd.getConfigInfo()!=null) {
    		final ProgressDialog progressMonitor = ProgressDialog.getInstance();				
    		
    		final SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
    					progressMonitor.setStatus("creating config ...");
    					ReportAttributesSelectDialog rasd = new ReportAttributesSelectDialog(oc, mart,acd.getConfigInfo().getName(),getGuiContainer());
    					  
    					((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).refreshPanel(GuiContainerPanel.this);
    					
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
    		//request update gui
    	}
	}
	
	private void addNaiveConfig() {
		
		AddConfigFromMartDialog dialog = new AddConfigFromMartDialog(McGuiUtils.INSTANCE.getRegistryObject(),false,null);
		final UserGroup user = ((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getUser();
		final List<Mart> marts = dialog.getSelectedMart();
		if(McUtils.isCollectionEmpty(marts))
			return;
		if(marts.size() == 1)
			this.addSingleConfig(marts.get(0), user);
		else
			this.addMultipleConfigs(marts,user);
	}
	
	public void addMultipleConfigs(final List<Mart> marts, final UserGroup user) {
    	
    	final ObjectController oc = new ObjectController();
    	//after the first config added, enable report tab
    	DnDTabbedPane tp = (DnDTabbedPane)SwingUtilities.getAncestorOfClass(DnDTabbedPane.class, this);
		if(tp != null){
			for(int i=0; i<tp.getComponentCount(); i++) {
				if(tp.getComponent(i) instanceof GuiContainerPanel)
				{
					GuiContainerPanel gcp = (GuiContainerPanel)tp.getComponent(i);
					if(gcp.getGuiContainer().getName().equals("report"))
					{
						gcp.enableControls(true);
						
						ButtonTabComponent btc = (ButtonTabComponent)tp.getTabComponentAt(i-1);
						btc.setEnableTitle(true);
					}
				}
			}
		}
		

    		final ProgressDialog progressMonitor = ProgressDialog.getInstance();			
    		
    		final SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
    					progressMonitor.setStatus("creating config ...");
    					for(Mart mart: marts) {
    						String defaultName = McGuiUtils.INSTANCE.getUniqueConfigName(mart, mart.getName()+ Resources.get("CONFIGSUFFIX"));
    						GuiContainer gc = ((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getSelectedGuiContainer();
    						oc.addConfigFromMaster(mart, defaultName,user,false, gc);  
    					}
    					((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).refreshPanel(GuiContainerPanel.this);   					
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
    		//request update gui
    		
    					
	}
	
	public void addSingleConfig(final Mart mart, final UserGroup user) {
    	final AddConfigDialog acd = new AddConfigDialog(mart,false);
    	final ObjectController oc = new ObjectController();
    	//after the first config added, enable report tab
    	DnDTabbedPane tp = (DnDTabbedPane)SwingUtilities.getAncestorOfClass(DnDTabbedPane.class, this);
		if(tp != null){
			for(int i=0; i<tp.getComponentCount(); i++) {
				if(tp.getComponent(i) instanceof GuiContainerPanel)
				{
					GuiContainerPanel gcp = (GuiContainerPanel)tp.getComponent(i);
					if(gcp.getGuiContainer().getName().equals("report"))
					{
						gcp.enableControls(true);
					
						ButtonTabComponent btc = (ButtonTabComponent)tp.getTabComponentAt(i-1);
						btc.setEnableTitle(true);
					}
				}
			}
		}
		
    	if(acd.getConfigInfo()!=null) {
    		final ProgressDialog progressMonitor = ProgressDialog.getInstance();			
    		
    		final SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {   
    					progressMonitor.setStatus("creating config ...");
    					GuiContainer gc = ((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getSelectedGuiContainer();
    					if(acd.getConfigInfo().isDoNaive())
    						oc.addConfigFromMaster(mart, acd.getConfigInfo().getName(),user, true, gc);  
    					else
    						oc.addEmptyConfig(mart, acd.getConfigInfo().getName(), user, gc);
    					((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).refreshPanel(GuiContainerPanel.this);
    					
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
    		//request update gui
    		
    	}		
	}

	public void addTabbedPane(DnDTabbedPane tp) {
		this.removeAll();
		this.add(tp,BorderLayout.CENTER);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			int index = 0;
			if(this.configList.getSelectedValue() != null)
				index = this.configList.getSelectedIndex();
			else if(this.configTable.getSelectedRowCount() > 0)
				index = this.configTable.getSelectedRow();
			
			MartPointer mp = (MartPointer)this.configList.getModel().getElementAt(index);
			new ConfigDialog(mp.getConfig());
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		boolean outside = true;
		if(this.configList.getModel().getSize() == 0)
			outside = true;
		else if(Boolean.parseBoolean(Settings.getProperty("portal.listview")))
			outside =  this.configTable.rowAtPoint(e.getPoint()) < 0;
		else
			outside = !this.configList.getCellBounds(0, this.configList.getModel().getSize()-1).contains(e.getPoint());
		if(!outside){
			if(e.isPopupTrigger()) {
				JPopupMenu menu = ContextMenuConstructor.getInstance().getContextMenu(this,"config",
							this.getSelectedMPs().size()>1);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}	
		}else{
			if(e.isPopupTrigger()) {
				JPopupMenu menu = ContextMenuConstructor.getInstance().getContextMenu(this, "guipanel",false);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
			this.configList.clearSelection();
			this.configTable.clearSelection();
			
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		boolean outside = true;
		if(this.configList.getModel().getSize() == 0)
			outside = true;
		else if(Boolean.parseBoolean(Settings.getProperty("portal.listview")))
			outside =  this.configTable.rowAtPoint(e.getPoint()) < 0;
		else
			outside = !this.configList.getCellBounds(0, this.configList.getModel().getSize()-1).contains(e.getPoint());
		if(!outside){
			if(e.isPopupTrigger()) {
				JPopupMenu menu = ContextMenuConstructor.getInstance().getContextMenu(this,"config",
							this.getSelectedMPs().size()>1);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}	
		}else{
			if(e.isPopupTrigger()) {
				JPopupMenu menu = ContextMenuConstructor.getInstance().getContextMenu(this, "guipanel",false);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
			this.configList.clearSelection();
			this.configTable.clearSelection();
			
		}		
	}

	public Set<Mart> getLinkedMartForConfig(Config config) {
		Set<Mart> mpSet = new HashSet<Mart>();
		List<Attribute> attList = config.getAttributes(null, true, true);
		for(Attribute att: attList) {
			if(att.isPointer() && !McUtils.isStringEmpty(att.getPointedMartName())) {
				String martName = att.getPointedMartName();
				Mart mart = config.getMart().getMartRegistry().getMartByName(martName);
				if(mart!=null)
					mpSet.add(mart);
			}
		}
		List<Filter> filterList = config.getRootContainer().getAllFilters(null, true, true);
		for(Filter fil: filterList) {
			if(fil.isPointer() && !McUtils.isStringEmpty(fil.getPointedMartName())) {
				String martName = fil.getPointedMartName();
				Mart mart = config.getMart().getMartRegistry().getMartByName(martName);
				if(mart!=null)
					mpSet.add(mart);
			}
		}

		return mpSet;
	}

	public List<ConfigComponent> getConfigComponents() {
		List<ConfigComponent> cl = new ArrayList<ConfigComponent>();
		/*Component[] cs = this.configPanel.getComponents();
		for(Component c: cs) {
			if(c instanceof ConfigComponent) {
				cl.add((ConfigComponent)c);
			}
		}*/
		return cl;
	}
	
	// enable or disable controls in the panel
	public void enableControls(boolean enable) {
		this.addButton.setEnabled(enable);
		this.listViewCheckBox.setEnabled(enable);
		this.hideCheckBox.setEnabled(enable);
		this.rdfButton.setEnabled(enable);
	}

	public void clean() {
		/*for(Component cc: this.configPanel.getComponents()) {
			if(cc instanceof ConfigComponent) {
				((ConfigComponent)cc).clean();
				cc = null;
			}
		}*/
			
		this.gc = null;
	}
	
	public List<MartPointer> getSelectedMPs() {
		int[] indexs = {};
		if(this.getConfigList().getSelectedIndices().length > 0)
			indexs = this.getConfigList().getSelectedIndices();
		else if(this.getConfigTable().getSelectedRowCount() > 0)
			indexs = this.getConfigTable().getSelectedRows();
		
		List<MartPointer> mps = new ArrayList<MartPointer>();
		for(int index : indexs){
			MartPointer mp = (MartPointer) ((SharedDataModel)this.getConfigList().getModel()).elementAt(index);
			mps.add(mp);
		}
		
		return mps;
	}
	
	public int[] getSelectedIndexes() {
		int[] indexs = {};
		String showFlag = Settings.getProperty("portal.listview");
		boolean show = Boolean.parseBoolean(showFlag);
		if(show)
			indexs = this.getConfigList().getSelectedIndices();
		else
			indexs = this.getConfigTable().getSelectedRows();
		
		return indexs;
	}
	
	public void setSelectedIndexes(int[] selIndexes) {
		String showFlag = Settings.getProperty("portal.listview");
		boolean show = Boolean.parseBoolean(showFlag);
		if(!show)
			this.getConfigList().setSelectedIndices(selIndexes);
		else{
			for(int selIndex : selIndexes)
				this.getConfigTable().addRowSelectionInterval(selIndex, selIndex);
		}
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}

    private void setMappings(JList list) {
        ActionMap map = list.getActionMap();
        map.put(ConfigListTransferHandler.getCutAction().getValue(Action.NAME),
        		ConfigListTransferHandler.getCutAction());
        map.put(ConfigListTransferHandler.getCopyAction().getValue(Action.NAME),
        		ConfigListTransferHandler.getCopyAction());
        map.put(ConfigListTransferHandler.getPasteAction().getValue(Action.NAME),
        		ConfigListTransferHandler.getPasteAction());
    }
    
    private void setMappings(JTable table) {
    	ActionMap map = table.getActionMap();
        map.put(ConfigTableTransferHandler.getCutAction().getValue(Action.NAME),
        		ConfigTableTransferHandler.getCutAction());
        map.put(ConfigTableTransferHandler.getCopyAction().getValue(Action.NAME),
        		ConfigTableTransferHandler.getCopyAction());
        map.put(ConfigTableTransferHandler.getPasteAction().getValue(Action.NAME),
        		ConfigTableTransferHandler.getPasteAction());
    }
}