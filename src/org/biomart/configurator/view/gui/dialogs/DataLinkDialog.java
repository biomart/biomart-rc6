/*
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the itmplied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.utils.FileLinkObject;
import org.biomart.configurator.utils.treelist.DbDataLinkPanel;
import org.biomart.configurator.utils.treelist.FileDataLinkPanel;
import org.biomart.configurator.utils.treelist.URLDataLinkPanel;
import org.biomart.configurator.utils.type.DataLinkType;
import org.biomart.objects.objects.MartRegistry;

public class DataLinkDialog extends JDialog implements ItemListener{
	private static final long serialVersionUID = 1;

	/**
	 * Pop up a dialog asking the user for details for a new schema, then create
	 * and return that schema.
	 * 
	 * 
	 * @return the newly created schema, or null if it was cancelled.
	 */
	public static DataLinkInfo showDialog() {
		final DataLinkDialog dialog = new DataLinkDialog(
				Resources.get("newMartDialogTitle"));
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		DataLinkType type = dialog.getDataLinkType();
		if(type.equals(DataLinkType.RDBMS)) {
			final DataLinkInfo linkInfo = dialog.getDataLinkInfo();
			if(linkInfo!=null) {
				dialog.dispose();
				return linkInfo;
			}				
		}else if(type.equals(DataLinkType.URL)) {
			return dialog.getDataLinkInfo();
		}else if(type.equals(DataLinkType.FILE)) {
			return dialog.getDataLinkInfo();
		}
		dialog.dispose();
		return null;
	}

	private JButton cancel;
	private DbDataLinkPanel dbLinkPanel;
	private JButton execute;	
	private JComboBox name;
	private JComboBox type;	
	private DataLinkInfo dlinkInfo = null;	
	private JPanel typePanel;
	//private JPanel connectionPanelHolder;
	//default is URL
	private DataLinkType martType = DataLinkType.URL;
	private FileDataLinkPanel filePanel;
	private URLDataLinkPanel urlPanel;
	private JCheckBox sourceGroupCB;
	private JCheckBox includePortalCB;
	private JCheckBox version8CB;
	private JTextField urlUserTF;
	private JPasswordField urlPwTF;
	private boolean version8 = false;

	//set loaddata status for detecting name combobox selection ignoring additem and removeitem
	private boolean loadData = true;

	private DataLinkDialog(final String title) {
		// Create the basic dialog centred on the main mart builder window.
		this.dlinkInfo = null;
		this.setTitle(title);
		this.setModal(true);
		Toolkit toolkit = Toolkit.getDefaultToolkit();  
		Dimension screenSize = toolkit.getScreenSize(); 
		this.setMinimumSize(new Dimension(screenSize.width - 400, screenSize.height - 200));

		// Create the content pane for the dialog, ie. the bit that will hold
		// all the various questions and answers.
		final JPanel content = new JPanel(new BorderLayout());
		this.setContentPane(content);
		
		final JPanel northPanel = new JPanel();
		
		typePanel = new JPanel(new CardLayout());
		final JPanel southPanel = new JPanel(new FlowLayout());
		
		content.add(northPanel,BorderLayout.NORTH);
		content.add(typePanel,BorderLayout.CENTER);
		content.add(southPanel, BorderLayout.SOUTH);
		
		// Create some constraints for labels, except those on the last row
		// of the dialog.
		final GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridwidth = GridBagConstraints.RELATIVE;
		labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		labelConstraints.anchor = GridBagConstraints.LINE_END;
		labelConstraints.insets = new Insets(0, 2, 0, 0);
		// Create some constraints for fields, except those on the last row
		// of the dialog.
		final GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
		fieldConstraints.fill = GridBagConstraints.NONE;
		fieldConstraints.anchor = GridBagConstraints.LINE_START;
		fieldConstraints.insets = new Insets(0, 1, 0, 2);
		// Create some constraints for labels on the last row of the dialog.
		final GridBagConstraints labelLastRowConstraints = (GridBagConstraints) labelConstraints
				.clone();
		labelLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;
		// Create some constraints for fields on the last row of the dialog.
		final GridBagConstraints fieldLastRowConstraints = (GridBagConstraints) fieldConstraints
				.clone();
		fieldLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;

		// Create the input fields for the type, and the
		// holder for the connection panel details.
		this.type = new JComboBox(new DataLinkType[] { DataLinkType.URL, DataLinkType.RDBMS, DataLinkType.FILE });
		//connectionPanelHolder = new JPanel();
		this.type.addItemListener(this);

		this.dbLinkPanel = new DbDataLinkPanel(DataLinkType.SOURCE);
		//connectionPanelHolder.add(this.dbLinkPanel);
		// Make a default selection for the connection panel holder. Use JDBC
		// as it is the most obvious choice. We have to do something here else
		// the box won't size properly without one.
		this.type.setSelectedItem(DataLinkType.URL);

		// Build a combo box that allows the user to change the name
		// of a schema, or select one from history to copy settings from.
		this.name = new JComboBox();
		this.name.setEditable(true);
		this.updateNameReference(DataLinkType.URL);

		// Create buttons in dialog.
		this.cancel = new JButton(Resources.get("cancelButton"));
		this.execute = new JButton(Resources.get("addButton"));

		// In the name field, also include the type label and field, to save
		// space.
		JPanel field = new JPanel();
		
		JLabel typelabel = new JLabel(Resources.get("typeLabel"));
		this.sourceGroupCB = new JCheckBox(Resources.get("GROUPTITLE"));
		field.add(typelabel);
		field.add(this.type);
				
		// Add the name label and name field.
		JLabel namelabel = new JLabel(Resources.get("nameLabel"));
		field.add(namelabel);
		field.add(this.name);
		field.add(this.sourceGroupCB);
		northPanel.add(field);

		
		this.includePortalCB = new JCheckBox("include portal");
		//this.includePortalCB.setSelected(true);
		//set default unchecked
		this.includePortalCB.setSelected(false);
		
//		southPanel.add(this.test);
		southPanel.add(this.cancel);
		southPanel.add(this.execute);
		//southPanel.add(this.includePortalCB);
		// Intercept the cancel button, which closes the dialog
		// without taking any action.
		this.cancel.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				DataLinkDialog.this.dlinkInfo = null;
				DataLinkDialog.this.setVisible(false);
			}
		});

		// Intercept the execute button, which causes the
		// schema to be created as a temporary schema object. If
		// successful, the dialog closes.
		this.execute.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				execute();
			}
		});
		
		this.name.addItemListener(this);
		urlPanel = new URLDataLinkPanel();
		//JPanel xmlPanel = this.createXMLPanel();
		filePanel = new FileDataLinkPanel();
				
		typePanel.add(urlPanel,DataLinkType.URL.toString());
		typePanel.add(dbLinkPanel,DataLinkType.RDBMS.toString());
		//typePanel.add(xmlPanel,DataLinkType.XML.toString());
		typePanel.add(filePanel,DataLinkType.FILE.toString());
		
		this.loadData = false;
		// Make the execute button the default button.
		this.getRootPane().setDefaultButton(this.execute);

		// Reset the fields to their default values.

		// Pack and resize the window.
		this.pack();

		// Move ourselves.
		this.setLocationRelativeTo(null);
	}
	
	private void execute() {
		
		if(((DataLinkType)this.type.getSelectedItem()).equals(DataLinkType.RDBMS)) {
			// Assume we've failed.
			boolean passedTest = true;
	
			if (passedTest) {
				this.setVisible(false);
				boolean isNameEmpty = false;
				if(this.name.getSelectedItem() == null || (this.isEmpty(this.name.getSelectedItem().toString())))
					isNameEmpty = true;

				this.dlinkInfo = this.dbLinkPanel.getDataLinkInfo(this.isSourceSchema()? DataLinkType.SOURCE: DataLinkType.TARGET);
				if(this.dlinkInfo == null || this.dlinkInfo.getSelectedTablesMap().isEmpty()) {
					JOptionPane.showMessageDialog(this, Resources.get("EMPTYDATABASE"), Resources
							.get("testTitle"), JOptionPane.ERROR_MESSAGE);	
					return;
				}
				if(!this.isSourceSchema() && this.dbLinkPanel.getUseOldConfigFlag())
					this.dlinkInfo.setUseOldConfigFlag(true);
				this.dlinkInfo.setSourceGrouped(this.sourceGroupCB.isSelected());
				//this.dlinkInfo.setIncludePortal(this.includePortalCB.isSelected());
				this.dlinkInfo.setIncludePortal(this.sourceGroupCB.isSelected());
				this.dlinkInfo.setTargetTableNamePartitioned(this.dbLinkPanel.isTablePartitioned());
				if(!isNameEmpty)
					this.storeInHistory();
			} else
				JOptionPane.showMessageDialog(this, Resources
						.get("schemaTestFailed"), Resources
						.get("testTitle"), JOptionPane.ERROR_MESSAGE);	
		}else if(((DataLinkType)this.type.getSelectedItem()).equals(DataLinkType.URL)) {
			this.setVisible(false);	
			this.dlinkInfo = this.urlPanel.getDataLinkInfo();
			
			if(this.name.getSelectedItem()!=null && !this.name.getSelectedItem().equals("")) {
				String nameStr = this.name.getSelectedItem().toString();
				final Properties history = new Properties();
				history.setProperty("host", dlinkInfo.getUrlLinkObject().getHost());
				history.setProperty("path", dlinkInfo.getUrlLinkObject().getPath());
				history.setProperty("port", dlinkInfo.getUrlLinkObject().getPort());
				history.setProperty("user", this.urlUserTF.getText());
				history.setProperty("password", new String(this.urlPwTF.getPassword()));
				history.setProperty("version", this.version8?"0.8":"0.7");
				Settings.saveHistoryProperties(DataLinkType.class, nameStr, history);							
			} 
			this.dlinkInfo.setSourceGrouped(this.sourceGroupCB.isSelected());
			this.dlinkInfo.setUseOldConfigFlag(!this.version8);
			//this.dlinkInfo.setIncludePortal(this.includePortalCB.isSelected());
			this.dlinkInfo.setIncludePortal(this.sourceGroupCB.isSelected());
			this.storeInHistory();
			
		} else if(((DataLinkType)this.type.getSelectedItem()).equals(DataLinkType.FILE)) {
			FileLinkObject linkObject = new FileLinkObject();
			linkObject.setDsInfoMap(this.filePanel.getFileMetaPanel().getDatasetsInfo07(false));
			this.dlinkInfo = new DataLinkInfo(DataLinkType.FILE);
			this.dlinkInfo.setFileLinkObject(linkObject);
			this.dlinkInfo.setSourceGrouped(this.sourceGroupCB.isSelected());
			//this.dlinkInfo.setIncludePortal(this.includePortalCB.isSelected());
			this.dlinkInfo.setIncludePortal(this.sourceGroupCB.isSelected());
			this.setVisible(false);	
		}
	}	
	
		


	private boolean isEmpty(final String string) {
		// Return true if the string is null or empty.
		return string == null || string.trim().length() == 0;
	}


	private boolean validateFields() {
		// Make a list to hold messages.
		final List<String> messages = new ArrayList<String>();

//		if(this.name.getSelectedItem() == null || (this.isEmpty(this.name.getSelectedItem().toString())))
		// We don't like missing names.
//			messages.add(Resources.get("fieldIsEmpty", Resources.get("name")));

		// We don't like missing types either.
		if (this.type.getSelectedIndex() == -1)
			messages.add(Resources.get("fieldIsEmpty", Resources.get("type")));

		// If we have any messages, show them.
		if (!messages.isEmpty())
			JOptionPane.showMessageDialog(this,
					messages.toArray(new String[0]), Resources
							.get("validationTitle"),
					JOptionPane.INFORMATION_MESSAGE);

		// If there were no messages, then validated OK if the connection
		// panel also validated OK.
		return messages.isEmpty() && this.dbLinkPanel.validateFields(true);
	}

	
	public DataLinkInfo getDataLinkInfo() {
		return this.dlinkInfo;
	}

	public boolean isSourceSchema() {
		if(this.martType!=DataLinkType.RDBMS)
			return false;
		return this.dbLinkPanel.isSource();
	}

	public DataLinkType getDataLinkType() {
		return this.martType;
	}
	

	/**
	 * reset the cached history items for the name combobox
	 * @param type
	 */
	private void updateNameReference(DataLinkType type) {
		this.loadData = true;
		this.name.removeAllItems();
		List<String> names = null;
		if(type.equals(DataLinkType.RDBMS)) {
			names = Settings.getHistoryNamesForClass(MartRegistry.class);
		}else if(type.equals(DataLinkType.URL))
			names = Settings.getHistoryNamesForClass(DataLinkType.class);
			
		if(names==null)
			return;
		Collections.reverse(names);
		for (final Iterator<String> i = names.iterator(); i.hasNext();)
			this.name.addItem(i.next());

		this.name.setSelectedIndex(-1);	
		this.loadData = false;
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource().equals(this.type)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				CardLayout cl = (CardLayout)(typePanel.getLayout());
				DataLinkType type = (DataLinkType)this.type.getSelectedItem();
				this.updateNameReference(type);
				// JDBC specific stuff.
				if (type.equals(DataLinkType.RDBMS)) {
					this.martType = type;
					cl.show(typePanel, DataLinkType.RDBMS.toString());
/*					if (!(this.dbLinkPanel instanceof DbDataLinkPanel)) {
						connectionPanelHolder.removeAll();
						this.dbLinkPanel = new DbDataLinkPanel(type);
						connectionPanelHolder.add(this.dbLinkPanel);
					}else*/
						this.dbLinkPanel.setDefaultValue(true);
					this.dbLinkPanel.clearDataBasesList();
				} else {
					this.martType = type;
					if(type.equals(DataLinkType.URL)) {
						this.urlPanel.setPredefineValues();
					}
					cl.show(typePanel, type.toString());
				} 
			}
			this.pack();
		}else if(e.getSource() == this.name) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final String obj = (String)this.name.getSelectedItem();
				if(obj!=null && !obj.toString().equals("")) {
						// Load the schema settings from our history.
					switch(this.martType) {
					case RDBMS:{
						final Properties historyProps = Settings.getHistoryProperties(
								MartRegistry.class, obj.toString());				
										// Copy the settings, if we found any that matched.
						if (historyProps != null && !this.loadData) {
							this.dbLinkPanel.copySettingsFromProperties(historyProps);							
							this.dbLinkPanel.updateDataBasesList();
							this.pack();
						}
						break;
					}
					case URL: {
						final Properties historyProps = Settings.getHistoryProperties(
								DataLinkType.class, obj.toString());				
										// Copy the settings, if we found any that matched.
						if (historyProps != null && !this.loadData) {
							this.urlPanel.copySettingsForUrl(historyProps);							
							this.pack();
						}						
						break;
					}
					}
				}					
			}
		}
	}

	private void storeInHistory() {
		final Properties history = new Properties();
		if(this.dlinkInfo == null || this.name.getSelectedItem() == null)
			return;
		if(this.martType == DataLinkType.RDBMS) {
			history.setProperty("driverClass", this.dlinkInfo.getJdbcLinkObject().getJdbcType().getDriverClassName());
			history.setProperty("jdbcURL", this.dlinkInfo.getJdbcLinkObject().getConnectionBase());
			history.setProperty("username", this.dlinkInfo.getJdbcLinkObject().getUserName());
			history.setProperty("password", this.dlinkInfo.getJdbcLinkObject().getPassword());
			history.setProperty("keyguessing", "" + this.dlinkInfo.getJdbcLinkObject().isKeyGuessing());
			history.setProperty("regex", this.dlinkInfo.getJdbcLinkObject().getPartitionRegex() == null?"":
				this.dlinkInfo.getJdbcLinkObject().getPartitionRegex());
			history.setProperty("expression", this.dlinkInfo.getJdbcLinkObject().getPtNameExpression()==null?"":
				this.dlinkInfo.getJdbcLinkObject().getPtNameExpression());
			history.setProperty("source", this.isSourceSchema()?"true":"false");
			history.setProperty("database", this.dlinkInfo.getJdbcLinkObject().getDatabaseName());
			Settings.saveHistoryProperties(MartRegistry.class, 
					this.name.getSelectedItem().toString(), history);
		}else if(this.martType.equals(DataLinkType.URL)) {
			history.setProperty("host", this.dlinkInfo.getUrlLinkObject().getHost());
			history.setProperty("port", this.dlinkInfo.getUrlLinkObject().getPort());
			history.setProperty("path", this.dlinkInfo.getUrlLinkObject().getPath());
			Settings.saveHistoryProperties(MartRegistry.class, 
					this.name.getSelectedItem().toString(), history);
			
		}
	}

}
