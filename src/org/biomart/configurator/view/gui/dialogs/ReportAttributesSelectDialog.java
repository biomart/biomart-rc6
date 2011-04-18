/**
 * 
 */
package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.controller.ObjectController;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViewPortal;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.objects.enums.FilterType;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.portal.GuiContainer;
import org.biomart.objects.portal.UserGroup;

/**
 * @author lyao
 * 
 */
public class ReportAttributesSelectDialog extends JDialog implements
		ActionListener {
	private ObjectController objectCtl;
	private Mart mart;
	private String configName;
	private JList attributeList;
	private List<Attribute> atts;
	private GuiContainer gc;

	public ReportAttributesSelectDialog(ObjectController oc, Mart mart,
			String configName, GuiContainer gc) {

		this.objectCtl = oc;
		this.mart = mart;
		this.configName = configName;
		atts = ObjectController.getAttributesInMain(mart);
		this.gc = gc;
		init();

	}

	public void init() {
		this.setTitle("Please select the identifier field for the report");
		this.setContentPane(this.createAttSlectPanel());
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public JPanel createAttSlectPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel centerPanel = new JPanel();
		centerPanel.setPreferredSize(new Dimension(400, 600));
		JPanel buttonPanel = new JPanel();

		JButton okButton = new JButton("Select");
		okButton.setActionCommand("select");
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		DefaultListModel model = new DefaultListModel();

		for (Attribute a : atts) {
			if(a.isHidden())
				continue;
			//if mart is url based drop attributes that dont have a filter
			if(mart.isURLbased()){
				if(a.getReferenceFilters().isEmpty())
					continue;
			}				
			model.addElement(a);
			
		}
		this.attributeList = new JList(model){
			public String getToolTipText(MouseEvent evt) {
		        // Get item index
		        int index = locationToIndex(evt.getPoint());

		        // Get item
		        Attribute item = (Attribute)getModel().getElementAt(index);

		        // Return the tool tip text
		        return item.getName();
		    }
		};
		this.attributeList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane attsp = new JScrollPane(this.attributeList);
		attsp.setPreferredSize(new Dimension(380, 550));

		centerPanel.add(attsp);		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		panel.add(centerPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals("select")){
			final UserGroup user = ((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getUser();
			//Config newConfig = this.objectCtl.addConfigFromMaster(mart, configName,user);
			Config newConfig = this.objectCtl.addReportConfigFromMaster(mart, configName,user);
			
			int sel = this.attributeList.getSelectedIndex();
			if(sel < 0){
				JOptionPane.showMessageDialog(this, "Please select a attribute first");
				return;
			}else{				
				Container attrCon = newConfig.getContainerByName("Attributes");
				Attribute att = (Attribute) this.attributeList.getSelectedValue();
				
				//move the selected attribute to the first col
				List<Attribute> attributes = attrCon.getAttributeList();
				for(Attribute a: attributes){
					if(a.getName().equals(att.getName())){
						Collections.swap(attributes, 0, attributes.indexOf(a));
					}
				}
				
				//add filter
				Filter newfilter = null;
				//if no filter, create a filter in master and copy to report config under root container
				if(att.getReferenceFilters().isEmpty()){
					if(this.mart.isURLbased()){
						JOptionPane.showMessageDialog(this, "The chosen attribute can not be used as a filtering criterion for the report");
						return;
					}else{
						String name = McGuiUtils.INSTANCE.getUniqueFilterName(newConfig, att.getName());
						newfilter = new Filter(att,name);
						Container reportCon = newConfig.getRootContainer().getContainerByNameResursively("report");
						if(reportCon != null)
							reportCon.addFilter(newfilter);
					}
				}
				//if has filter found, copy it to report config
				else{
					for(Filter filter : att.getReferenceFilters()){
						if(filter.getFilterType().equals(FilterType.TEXT))
							newfilter = new Filter(filter.generateXml());						
					}
					if(newfilter == null && !att.getReferenceFilters().isEmpty()){
						newfilter = new Filter(att.getReferenceFilters().get(0).generateXml());						
					}
					//newConfig.getRootContainer().addFilter(newfilter);
					Container reportCon = newConfig.getRootContainer().getContainerByNameResursively("report");
					if(reportCon != null)
						reportCon.addFilter(newfilter);
					
					newfilter.synchronizedFromXML();
				}
				if(newfilter == null)
					return;
				else if(newfilter.isHidden())
					newfilter.setHideValue(false);
				//create meta info for report config
				/*
				{ 
					"layout": {
						"gene_info":{"rendering":"list","options":{"breakAt":2}}, 
						"ped":{"rendering":"heatmap","options":{"heatColumn":0,"displayColumns":[2,3,4],"fallbackColumn":1}}, 
						"prot_domain_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"prot_family_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"prot_interpro_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"tm_and_signal_1_gene_report":{"rendering":"list"}, 
						"go_biological_process_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"go_cellular_component_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"go_molecular_function_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"xrefs_1_gene_report":{"rendering":"list","options":{"grouped":false}}, 
						"microarray_1_gene_report":{"rendering":"list","options":{"grouped":false}}  
						} 
				}
				*/
				
				if(attrCon != null){
					StringBuilder metaInfo = new StringBuilder();
					metaInfo.append("{"+'"'+"layout"+'"'+":");
						metaInfo.append("{"+'"'+attrCon.getName()+'"'+":");
							metaInfo.append("{"+'"'+"rendering"+'"'+":"+'"'+"list"+'"');
							metaInfo.append("}");
						metaInfo.append("}");
					metaInfo.append("}");
					newConfig.setProperty(XMLElements.METAINFO, metaInfo.toString());
				}
				//create linkout url for that chosen attribute in all other configs
				StringBuilder linkOutURL = new StringBuilder();
				linkOutURL.append("/martreport/?report=");
				linkOutURL.append(gc.getName());
				linkOutURL.append("&");
				linkOutURL.append("mart=");
				linkOutURL.append(newConfig.getName());
				linkOutURL.append("&");
				linkOutURL.append(newfilter.getName());
				linkOutURL.append("=%s%");
				linkOutURL.append("&datasets=%dataset%");
				
				ObjectController.addLinkURLtoAttribute(att, linkOutURL.toString());
				//create a container and copy all main table attributes from master to this container
				Container container = new Container("mainTableAttributes");
				for(Attribute attribute: ObjectController.getAttributesInMain(mart.getMasterConfig())){
					org.jdom.Element elem = attribute.generateXml();
					Attribute newAtt = new Attribute(elem);
					container.addAttribute(newAtt);
				}
				//create a datasets for link out url for all other configs
				for(Config config: newConfig.getMart().getConfigList()){
					Attribute datasetAttr = new Attribute("dataset", "Dataset");
					datasetAttr.setConfig(newConfig);
					datasetAttr.setHideValue(true);
					datasetAttr.setProperty(XMLElements.VALUE, "(p0c5)");
					config.getRootContainer().addAttribute(datasetAttr);
				}
			}		
			
			this.setVisible(false);
		}else if(e.getActionCommand().equals("cancel")){
			this.setVisible(false);
		}
	}
}
