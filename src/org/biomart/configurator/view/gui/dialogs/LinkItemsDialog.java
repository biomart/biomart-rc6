package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.PortableType;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.ElementList;
import org.biomart.objects.objects.Filter;


public class LinkItemsDialog extends JDialog implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final private PortableType type;
	private JTextField nameField;
	private JTextField versionField;
	private JTextField typeField;
	private String defaultName;
	private ElementList elementList;
	final private Config config;
	private JList targetList;
	private JList sourceList;
	
	
	// 0 imp 1 exp
	public LinkItemsDialog(Config config, PortableType type, String defaultName) {
		this.config = config;
		this.type = type;
		if(type.equals(PortableType.IMPORTABLE))
			this.setTitle("create importable");
		else
			this.setTitle("create exportable");
		this.defaultName = defaultName;
		this.setModal(true);
		this.add(this.createGui());
		this.pack();
		this.setLocationRelativeTo(null);
	}
		
	private JPanel createGui() {
		JPanel content = new JPanel(new BorderLayout());
		JPanel inputPanel = new JPanel(new GridBagLayout());
		JPanel elementPanel = new JPanel(new FlowLayout());
		
		JLabel nameLabel = new JLabel(XMLElements.NAME.toString());			
		nameField = new JTextField(20);
		nameField.setText(this.defaultName);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		
		inputPanel.add(nameLabel, c);
		c.gridx = 1;
		inputPanel.add(nameField, c);
		
		JLabel versionLabel = new JLabel(XMLElements.VERSION.toString());
		c.gridx = 0; 
		c.gridy = 1;
		inputPanel.add(versionLabel,c);
		
		versionField = new JTextField(20);
		c.gridx = 1;
		inputPanel.add(versionField,c);
		
		JLabel typeLabel = new JLabel(XMLElements.TYPE.toString());
		c.gridx = 0;
		c.gridy = 2;
		inputPanel.add(typeLabel,c);
		
		typeField = new JTextField(20);
		typeField.setText(XMLElements.LINK.toString());
		c.gridx = 1;
		inputPanel.add(typeField, c);
		
		//get all filter/attribute
		if(this.type.equals(PortableType.IMPORTABLE)) {
			List<Filter> filterList = config.getFilters(new ArrayList<String>(), true,true);
			sourceList = new JList(filterList.toArray());
		}else {
			List<Attribute> attributeList = config.getAttributes(new ArrayList<String>(), true, true);
			sourceList = new JList(attributeList.toArray());
		}
		
		JScrollPane listScroller = new JScrollPane(sourceList);
		listScroller.setPreferredSize(new Dimension(250, 250));
		elementPanel.add(listScroller);
		
		JPanel movePanel = new JPanel(new GridBagLayout());
		JButton addButton = new JButton(">");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
		JButton removeButton = new JButton("<");
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);
		
		c.gridx = 0;
		c.gridy = 0;
		movePanel.add(addButton,c);
		c.gridy = 1;
		movePanel.add(removeButton,c);
		elementPanel.add(movePanel);
		
		DefaultListModel listModel = new DefaultListModel();
		targetList = new JList(listModel);
		JScrollPane tlistScroller = new JScrollPane(targetList);
		tlistScroller.setPreferredSize(new Dimension(250, 250));
		elementPanel.add(tlistScroller);
		
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton(Resources.get("OK"));
		okButton.setActionCommand(Resources.get("OK"));
		buttonPanel.add(okButton);
		okButton.addActionListener(this);
		content.add(inputPanel, BorderLayout.NORTH);
		content.add(elementPanel,BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);
		
		return content;
	}
	
	private void createPortable() {
		if(!McUtils.isStringEmpty(this.nameField.getText()) && ((DefaultListModel)this.targetList.getModel()).getSize()>0) {
			this.elementList = new ElementList(this.config,this.nameField.getText(),this.type);
			//check version
			String versionStr = this.versionField.getText();
			if(!McUtils.isStringEmpty(versionStr)) {
				this.elementList.setProperty(XMLElements.VERSION, versionStr);
			}
			this.elementList.setProperty(XMLElements.TYPE,this.typeField.getText());
			//check filters/attributes

			DefaultListModel model = (DefaultListModel)this.targetList.getModel();
			if(this.type.equals(PortableType.IMPORTABLE)) {
				for(int i=0; i<model.getSize(); i++) {
					this.elementList.addFilter(((Filter)model.get(i)));
				}
			}else {
				for(int i=0; i<model.getSize(); i++) {
					this.elementList.addAttribute(((Attribute)model.get(i)));
				}
			}
			this.setVisible(false);
		}
	}
	
	public ElementList getElementList() {
		return this.elementList;
	}

	private void add() {
		DefaultListModel model = (DefaultListModel)this.targetList.getModel();
		for(Object obj: this.sourceList.getSelectedValues()) {
			//check if it exist already
			boolean found = false;
			for(int i=0; i<model.getSize(); i++) {
				Object obj2 = model.get(i);
				if(obj2.equals(obj)) {
					found = true;
					break;
				}
			}
			if(!found)
				model.addElement(obj);
		}
	}
	
	private void remove() {
		for(Object obj: this.targetList.getSelectedValues())
			((DefaultListModel)this.targetList.getModel()).removeElement(obj);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(Resources.get("OK"))) {
			this.createPortable();
		}else if(e.getActionCommand().equals("add")) {
			this.add();
		}else if(e.getActionCommand().equals("remove")) {
			this.remove();
		}
		
	}
	
}