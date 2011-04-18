package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.utils.McUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class FileDataLinkPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileMetaPanel martMetaPanel;
	
	public FileDataLinkPanel() {
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();

		
		JButton fileButton = new JButton("choose file");
		fileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadFile();
			}			
		});
		
		martMetaPanel = new FileMetaPanel();		
		northPanel.add(fileButton);

		
		this.add(northPanel,BorderLayout.NORTH);
		this.add(martMetaPanel,BorderLayout.CENTER);
	}
	
	private void loadFile() {
		final String currentDir = Settings.getProperty("currentOpenDir");
		File file = null;
		final JFileChooser xmlFileChooser = new JFileChooser();
		xmlFileChooser.setCurrentDirectory(currentDir == null ? new File(".")
				: new File(currentDir));
		if (xmlFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// Update the load dialog.
			Settings.setProperty("currentOpenDir", xmlFileChooser
					.getCurrentDirectory().getPath());

			file = xmlFileChooser.getSelectedFile();
		}
		if(file == null)
			return;
		List<MartInVirtualSchema> martList = this.getURLMartFromFile(file);
		if(martList.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Not a valid BioMart 0.7 registry file");
		}
		this.martMetaPanel.setMartList(martList);
	}
	
	private List<MartInVirtualSchema> getURLMartFromFile(File file) {
		List<MartInVirtualSchema> result = new ArrayList<MartInVirtualSchema>();
		Document document = null;
		try {
			SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
			document = saxBuilder.build(file);
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		Element rootElement = document.getRootElement();
		//has virtualschemas?
		//get all real mart list
		List<Element> martElementList = new ArrayList<Element>();
		@SuppressWarnings("unchecked")
		List<Element> virtuals = rootElement.getChildren();
		for(Element vss: virtuals) {
			//virtual schema?
			if(vss.getName().equals("virtualSchema")) {
				List<Element> subElements = vss.getChildren();
				for(Element subE: subElements) {
					martElementList.add(subE);
				}
			} else if(vss.getName().equals("MartURLLocation") || vss.getName().equals("MartDBLocation")){				
				martElementList.add(vss);
			}
		}

		for(Element virtualSchema: martElementList) {
			//db or url
			if(McUtils.isStringEmpty(virtualSchema.getAttributeValue("databaseType"))) {
				MartInVirtualSchema mart = new MartInVirtualSchema.URLBuilder().database(virtualSchema.getAttributeValue("database"))
				.defaultValue(virtualSchema.getAttributeValue("default"))
				.displayName(virtualSchema.getAttributeValue("displayName"))
				.host(virtualSchema.getAttributeValue("host"))
				.includeDatasets(virtualSchema.getAttributeValue("includeDatasets"))
				.martUser(virtualSchema.getAttributeValue("martUser"))
				.name(virtualSchema.getAttributeValue("name"))
				.path(virtualSchema.getAttributeValue("path"))
				.port(virtualSchema.getAttributeValue("port"))
				.serverVirtualSchema(virtualSchema.getAttributeValue("serverVirtualSchema"))
				.visible(virtualSchema.getAttributeValue("visible"))
				.build();
				result.add(mart);
			} else {
				//db
				MartInVirtualSchema mart = new MartInVirtualSchema.DBBuilder().database(virtualSchema.getAttributeValue("database"))
				.defaultValue(virtualSchema.getAttributeValue("default"))
				.displayName(virtualSchema.getAttributeValue("displayName"))
				.host(virtualSchema.getAttributeValue("host"))
				.includeDatasets(virtualSchema.getAttributeValue("includeDatasets"))
				.martUser(virtualSchema.getAttributeValue("martUser"))
				.name(virtualSchema.getAttributeValue("name"))
				.port(virtualSchema.getAttributeValue("port"))
				.visible(virtualSchema.getAttributeValue("visible"))
				.schema(virtualSchema.getAttributeValue("schema"))
				.username(virtualSchema.getAttributeValue("user"))
				.password(virtualSchema.getAttributeValue("password"))
				.type(virtualSchema.getAttributeValue("databaseType"))
				.build();
				result.add(mart);}
		}
		
		return result;
	}
	
	public FileMetaPanel getFileMetaPanel() {
		return this.martMetaPanel;
	}
}