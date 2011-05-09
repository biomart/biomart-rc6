package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.biomart.backwardCompatibility.MartInVirtualSchema;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.controller.ObjectController;
import org.biomart.configurator.utils.McUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
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
		ObjectController oc = new ObjectController();
		List<MartInVirtualSchema> martList;
		try {
			martList = oc.getURLMartFromFile(file);
			this.martMetaPanel.setMartList(martList);
		} catch (JDOMException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Not a valid BioMart 0.7 registry file");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Not a valid BioMart 0.7 registry file");
		}

	}
	
	
	public FileMetaPanel getFileMetaPanel() {
		return this.martMetaPanel;
	}
}