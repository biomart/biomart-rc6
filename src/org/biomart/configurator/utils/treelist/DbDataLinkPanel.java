/*
 Copyright (C) 2006 EBI
 
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

package org.biomart.configurator.utils.treelist;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.biomart.common.resources.Resources;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.controller.TargetSchema;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.utils.JdbcLinkObject;
import org.biomart.configurator.utils.type.JdbcType;
import org.biomart.configurator.utils.type.DataLinkType;


/**
 * This connection panel implementation allows a user to define some JDBC
 * connection parameters, such as hostname, username, driver class and if
 * necessary the location where the driver can be found. It uses this to
 * construct a JDBC URL, dynamically, and ultimately creates a
 * ConnectionObject implementation which represents the connection.
 */
public class DbDataLinkPanel extends JPanel implements DocumentListener, ActionListener {

	private static final long serialVersionUID = 1;	
	private String currentJDBCURLTemplate;
	private JTextField host;
	private JTextField jdbcURL;
	private JPasswordField password;
	private JButton getButton;
	private JFormattedTextField port;
	private JComboBox predefinedDriverClass;
	private JTextField username;
	//private DBMetaTree dbPreview;
	private DBMetaPanel dbMetaPanel;
	private JCheckBox keyguessing;
	private JdbcLinkObject conObject;
	private JTextField dbTField;
	private JTextField regexTF;
	private JTextField expressionTF;
	private JCheckBox partitionedCB;
	private boolean sourceMode = true;
	private JRadioButton sourceRB;
	private JRadioButton martRB;

	/**
	 * This constructor creates a panel with all the fields necessary to
	 * construct a JDBCSchema instance, save the name which will be
	 * passed in elsewhere.
	 * <p>
	 * You must call {@link #copySettingsFromSchema(SchemaController)} before
	 * displaying this panel, otherwise the values displayed are not defined
	 * and may result in unpredictable behaviour. Or, call
	 * {@link #copySettingsFromProperties(Properties)} to achieve the same
	 * results.
	 * 
	 * @param mart
	 *            the mart this schema will belong to.
	 */

	public DbDataLinkPanel(DataLinkType type) {
		super();
		this.sourceMode = true;
		// Create the layout manager for this panel.
		this.setLayout(new BorderLayout());
		// Create all the useful fields in the dialog box.
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		JPanel northPanel = new JPanel(new GridLayout(0,1));
		
		this.jdbcURL = new JTextField(40);
		this.host = new JTextField(20);
		this.port = new JFormattedTextField(new DecimalFormat("0"));
		this.port.setColumns(4);
		this.dbTField = new JTextField(15);
		this.username = new JTextField(10);
		this.password = new JPasswordField(10);
		this.keyguessing = new JCheckBox(Resources.get("myISAMLabel"));

		this.updateJDBCURL();
		// The predefined driver class box displays everything we know
		// about by default, as defined by the map at the start of this
		// class.
		this.predefinedDriverClass = new JComboBox(JdbcType.values());

		this.predefinedDriverClass.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				// This method is called when the driver class field is
				// changed, either by the user typing in it, or using
				// the drop-down to select a predefine value.

				// Work out which database type was selected.
				final JdbcType classType = (JdbcType) DbDataLinkPanel.this.predefinedDriverClass
						.getSelectedItem();

				// Use it to look up the default class for that database
				// type, then reset the drop-down to nothing-selected.
			
				final String driverClassName = classType.getDriverClassName();
				DbDataLinkPanel.this.currentJDBCURLTemplate = classType.getUrlTemplate();
				DbDataLinkPanel.this.host.setText(classType.getDefaultHost());
				DbDataLinkPanel.this.port.setText(classType.getDefaultPort());
				DbDataLinkPanel.this.keyguessing
							.setVisible(driverClassName
									.indexOf("mysql") >= 0);
				DbDataLinkPanel.this.keyguessing
							.setSelected(driverClassName
									.indexOf("mysql") >= 0);
				if(driverClassName.indexOf("mysql")>=0)
					DbDataLinkPanel.this.dbTField.setText("");
				//clear Tree
				DbDataLinkPanel.this.dbMetaPanel.updateList(new ArrayList<String>());
				
			}				
		});

		// Create a listener that listens for changes on the host, port
		// and database fields, and uses this to automatically update
		// and construct a JDBC URL based on their contents.
		this.host.getDocument().addDocumentListener(this);
		this.port.getDocument().addDocumentListener(this);
		this.dbTField.getDocument().addDocumentListener(this);


		// On-change listener for regex+expression to update panel of
		// matches
		// by creating a temporary dummy schema with the specified regexes
		// and
		// seeing what it produces. Alerts if nothing produced.
		// Add the driver class label and field.
		JLabel label = new JLabel(Resources.get("driverClassLabel"));
		panel1.add(label);
		panel1.add(this.predefinedDriverClass);
		panel1.add(this.keyguessing);
		
		sourceRB = new JRadioButton(Resources.get("SOURCE"));
		sourceRB.setActionCommand("sourcebutton");
		sourceRB.addActionListener(this);
		sourceRB.setSelected(true);
		martRB = new JRadioButton(Resources.get("TARGET"));
		martRB.setActionCommand("targetbutton");
		martRB.addActionListener(this);
		ButtonGroup bg = new ButtonGroup();
		bg.add(sourceRB);
		bg.add(martRB);
		
		panel1.add(sourceRB);
		panel1.add(martRB);
		northPanel.add(panel1);

		// Add the host label, and the host field, port label, port field.
		label = new JLabel(Resources.get("hostLabel"));
		panel2.add(label);
		panel2.add(this.host);
		label = new JLabel(Resources.get("portLabel"));
		panel2.add(label);
		panel2.add(this.port);
		label = new JLabel("Database");
		panel2.add(label);
		panel2.add(this.dbTField);
		northPanel.add(panel2);
		

		// Add the JDBC URL label and field.
		label = new JLabel(Resources.get("jdbcURLLabel"));
		panel3.add(label);
		panel3.add(this.jdbcURL);
		northPanel.add(panel3);

		// Add the username label, and the username field, password
		// label and password field across the username field space
		// in order to save space.
		label = new JLabel(Resources.get("usernameLabel"));
		panel4.add(label);
		panel4.add(this.username);
		label = new JLabel(Resources.get("passwordLabel"));
		panel4.add(label);
		panel4.add(this.password);
		getButton = new JButton(Resources.get("getButton"));
		panel4.add(this.getButton);
		this.partitionedCB = new JCheckBox("partitioned");
		this.partitionedCB.setEnabled(Boolean.parseBoolean(System.getProperty("showadvancedmenu")));
		panel4.add(this.partitionedCB);
		getButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDataBasesList();
			}				
		});
		
		northPanel.add(panel4);

		// Add the partition stuff.
		this.regexTF = new JTextField(50);
		this.expressionTF = new JTextField(20);
//		JLabel regexLabel = new JLabel("Regex:");
//		JLabel expressionLabel = new JLabel("Expression:");
//		this.add(regexLabel,labelConstraints);
//		this.add(regexTF,fieldConstraints);
//		this.add(expressionLabel,labelConstraints);
//		this.add(expressionTF,fieldConstraints);
		// Two-column string/string panel of matches
//		label = new JLabel(Resources.get("databasesLabel"));
//		this.add(label, labelLastRowConstraints);
		//JPanel field = this.dbPreview.createGUI();
		dbMetaPanel = new DBMetaPanel();
		this.add(dbMetaPanel, BorderLayout.CENTER);
		this.add(northPanel,BorderLayout.NORTH);
		
		//set default value
		this.predefinedDriverClass.setSelectedItem(JdbcType.MySQL);
		if(this.sourceMode) {
			this.host.setText(Resources.get("ensemblSourceHost"));
			this.port.setText(Resources.get("ensemblSourcePort"));
		}else {
			this.jdbcURL.setText(Resources.get("ensemblMartHost"));
			this.port.setText(Resources.get("ensemblMartPort"));
		}
	}

	public void copySettingsFromProperties(final Properties template) {
		// Copy the driver class.

		// Make sure the right fields get enabled.
		this.driverClassChanged(template.getProperty("driverClass"));

		// Carry on copying.
		final String jdbcURL = template.getProperty("jdbcURL");
		this.jdbcURL.setText(jdbcURL);
		this.username.setText(template.getProperty("username"));
		this.password.setText(template.getProperty("password"));
		this.keyguessing.setSelected(Boolean.valueOf(
				template.getProperty("keyguessing")).booleanValue());
		if(Boolean.parseBoolean(template.getProperty("source"))) {
			sourceRB.setSelected(true);
			this.sourceMode = true;
		}else {
			martRB.setSelected(true);
			this.sourceMode = false;
		}
		this.dbTField.setText(template.getProperty("database"));
		// Parse the JDBC URL into host, port and database, if the
		// driver is known to us (defined in the map at the start
		// of this class).
		String regexURL = this.currentJDBCURLTemplate;
		if (regexURL != null) {
			// Replace the three placeholders in the JDBC URL template
			// with regex patterns. Obviously, this depends on the
			// three placeholders appearing in the correct order.
			// If they don't, then you're stuffed.
			regexURL = regexURL.replaceAll("<HOST>", "(.*)");
			regexURL = regexURL.replaceAll("<PORT>", "(.*)");
			regexURL = regexURL.replaceAll("<DATABASE>", "(.*)");

			// Use the regex to parse out the host, port and database
			// from the JDBC URL.
			final Pattern regex = Pattern.compile(regexURL);
			final Matcher matcher = regex.matcher(jdbcURL);
			if (matcher.matches()) {
				this.host.setText(matcher.group(1));
				this.port.setText(matcher.group(2));
			}
		}
	}

	private void documentEvent(final DocumentEvent e) {
		this.updateJDBCURL();
	}

	private void driverClassChanged(String className) {
		// Work out which class we should try out.
		// If it's not empty...
		if (!this.isEmpty(className)) {
			this.keyguessing.setVisible(className.indexOf("mysql") >= 0);
			//this.predefinedDriverClass.setSelectedItem(JdbcType.valueOf(className));
			if(className.indexOf("mysql")>=0)
				this.dbTField.setText("");
			// Do we know about this, as defined in the map at the start
			// of this class?
			JdbcType jdbcType = JdbcType.valueFrom(className);
				
			if (jdbcType!=null) {
				this.predefinedDriverClass.setSelectedItem(jdbcType);
				// Yes, so we can use the map to construct a JDBC URL
				// template, into which host, port, and database can be
				// placed as required.


				// The second part is the JDBC URL template itself. Remember
				// which template was selected, then disable the JDBC URL
				// field in the interface as its contents will now be
				// computed automatically. Enable the host/database/port
				// fields instead.
				this.currentJDBCURLTemplate = jdbcType.getUrlTemplate();
				this.jdbcURL.setEnabled(false);
				this.host.setEnabled(true);
				this.port.setEnabled(true);

				// The first part of the template is the default port
				// number, so set the port field to that number.
				this.port.setText(jdbcType.getDefaultPort());
			}

			// This else statement deals with JDBC drivers that we do not
			// have a template for.
			else {
				// Blank out our current template, so that we don't try
				// and use it by accident.
				this.currentJDBCURLTemplate = null;

				// Enable the user-specified JDBC URL field, and disable
				// the host/port/database fields as they're no longer
				// required.
				this.jdbcURL.setEnabled(true);
				this.host.setEnabled(false);
				this.port.setEnabled(false);
			}
		}

		// If it's empty, disable the fields that depend on it.
		else {
			this.keyguessing.setVisible(false);
			this.host.setEnabled(false);
			this.port.setEnabled(false);
			this.jdbcURL.setEnabled(false);
		}
	}


	private boolean isEmpty(final String string) {
		// Strings are empty if they are null or all whitespace.
		return string == null || string.trim().length() == 0;
	}

	private void updateJDBCURL() {
		// If we don't have a current template, we can't parse it,
		// so don't even attempt to do so.
		if (this.currentJDBCURLTemplate == null)
			return;

		// Update the JDBC URL based on our current settings. Do this
		// by replacing the placeholders in the template with the
		// current values of the host/port/database fields. If there
		// are no values in these fields, leave the placeholders
		// as they are.
		String newURL = this.currentJDBCURLTemplate;
		if (!this.isEmpty(this.host.getText()))
			newURL = newURL.replaceAll("<HOST>", this.host.getText());
		if (!this.isEmpty(this.port.getText()))
			newURL = newURL.replaceAll("<PORT>", this.port.getText());
		if(!this.isEmpty(this.dbTField.getText())) {
			newURL = newURL.replaceAll("<DATABASE>", this.dbTField.getText());
		}
//		newURL = newURL + this.dbTField.getText()+"/";
		// Set the JDBC URL field to contain the URL we constructed.
		this.jdbcURL.setText(newURL);
	}

	public void changedUpdate(final DocumentEvent e) {
		this.documentEvent(e);
	}

	public TargetSchema createSchemaFromSettings(final String name) {
		// If the fields aren't valid, we can't create it.
		if (!this.validateFields(true))
			return null;

		try {
			// Return that schema.
			return this.privateCreateSchemaFromSettings(name);
		} catch (final Throwable t) {
			StackTrace.showStackTrace(t);
		}

		// If we got here, something went wrong, so behave
		// as though validation failed.
		return null;
	}

	private TargetSchema privateCreateSchemaFromSettings(final String name)
			throws Exception {
		// Record the user's specifications.
		final String url = this.jdbcURL.getText();
		final String username = this.username.getText();
		final String password = new String(this.password.getPassword());
		// Construct a JDBCSchema based on them.
		JdbcLinkObject conObj = new JdbcLinkObject(url,"","",username,password,
				(JdbcType)this.predefinedDriverClass.getSelectedItem(),this.regexTF.getText(),this.expressionTF.getText(),this.keyguessing.isSelected());
		final TargetSchema schema = new TargetSchema(null, conObj);

		// Return that schema.
		return schema;
	}

	public void insertUpdate(final DocumentEvent e) {
		this.documentEvent(e);
	}


	public void removeUpdate(final DocumentEvent e) {
		this.documentEvent(e);
	}


	public boolean validateFields(final boolean report) {
		// Make a list to hold any validation messages that may occur.
		final List<String> messages = new ArrayList<String>();


		// If the user had to specify their own JDBC URL, make sure
		// they have done so.
		if (this.jdbcURL.isEnabled()) {
			if (this.isEmpty(this.jdbcURL.getText()))
				messages.add(Resources.get("fieldIsEmpty", Resources
						.get("jdbcURL")));
		}

		// Otherwise, make sure they have specified all three of host, port
		// and database.
		else {
			if (this.isEmpty(this.host.getText()))
				messages.add(Resources.get("fieldIsEmpty", Resources
						.get("host")));
			if (this.isEmpty(this.port.getText()))
				messages.add(Resources.get("fieldIsEmpty", Resources
						.get("port")));
		}

		// Make sure they have given a schema name.

		// Make sure they have given a username. (Password is optional as
		// not all databases require one).
		if (this.isEmpty(this.username.getText()))
			messages.add(Resources.get("fieldIsEmpty", Resources
					.get("username")));

		// If there any messages to show the user, show them.
		if (report && !messages.isEmpty())
			JOptionPane.showMessageDialog(this, messages
					.toArray(new String[0]), Resources
					.get("validationTitle"),
					JOptionPane.INFORMATION_MESSAGE);

		// Validation succeeds if there were no messages.
			return messages.isEmpty();
		}

	public boolean getUseOldConfigFlag() {
		return this.dbMetaPanel.getUseOldConfigFlag();
	}
	
	public boolean isTablePartitioned() {
		return this.partitionedCB.isSelected();
	}
	
	public void updateDataBasesList() {
		if (!this.validateFields(true))
			return;

		conObject = new JdbcLinkObject(this.jdbcURL.getText(),this.dbTField.getText(),"",
					this.username.getText(),
					new String(this.password.getPassword()),
					(JdbcType)this.predefinedDriverClass.getSelectedItem(),
					this.regexTF.getText(),this.expressionTF.getText(),this.keyguessing.isSelected());
			final ProgressDialog progressMonitor = ProgressDialog.getInstance();
			
 
    		final SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
    					progressMonitor.setStatus("getting data source...");
    					dbMetaPanel.setDataLinkType(sourceMode?DataLinkType.SOURCE:DataLinkType.TARGET);
		        		dbMetaPanel.updateList(conObject);
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
		//	progressMonitor.dispose();
		}
	};
	worker.start();
	progressMonitor.start("testing");
}
	
	public void clearDataBasesList() {
		this.dbMetaPanel.updateList(new ArrayList<String>());
	}
	

	
	public boolean test() {
		boolean res = this.validateFields(true);
		return res;
	}

	
	public DataLinkInfo getDataLinkInfo(DataLinkType type) {

		Map<String, List<String>> selectedTablesMap = this.dbMetaPanel.getDBInfo(false);
		if(selectedTablesMap.size()<=0) 
			return null;
		
		DataLinkInfo dlinkInfo = new DataLinkInfo(type);
		dlinkInfo.setType(type);
		
		if(isPartitionApplied()) {
			if(selectedTablesMap.size()>0) {
				Iterator<String> it = selectedTablesMap.keySet().iterator();
				String dbName = it.next();
				//assume the mart is unique for now TODO, find a name for mart

				JdbcLinkObject conObj = new JdbcLinkObject(this.jdbcURL.getText(),dbName,
						dbName,this.username.getText(),
						new String(this.password.getPassword()),
						(JdbcType)this.predefinedDriverClass.getSelectedItem(),
						this.regexTF.getText(),this.expressionTF.getText(),this.keyguessing.isSelected());

				dlinkInfo.setJdbcLinkObject(conObj);
				dlinkInfo.setSelectedTables(selectedTablesMap);
				dlinkInfo.setAllTables(this.dbMetaPanel.getDBInfo(true));
			}
		}else {
			JdbcLinkObject conObj = new JdbcLinkObject(this.jdbcURL.getText(),this.dbTField.getText(),"",
					this.username.getText(),
					new String(this.password.getPassword()),
					(JdbcType)this.predefinedDriverClass.getSelectedItem(),
					this.regexTF.getText(),this.expressionTF.getText(),this.keyguessing.isSelected());
			dlinkInfo.setJdbcLinkObject(conObj);		
			dlinkInfo.setSelectedTables(selectedTablesMap);
			dlinkInfo.setAllTables(this.dbMetaPanel.getDBInfo(true));
		}
		return dlinkInfo;
	}

	private boolean isPartitionApplied() {
		if(!"".equals(this.regexTF.getText()) && !"".equals(this.expressionTF.getText()))
			return true;
		else
			return false;
	}

	public String getHostPortValue() {
		return this.host.getText()+":"+this.port.getText();
	}
	
	public void setDefaultValue(boolean isSource) {
		this.predefinedDriverClass.setSelectedItem(JdbcType.MySQL);
		if(isSource) {
			this.host.setText(Resources.get("ensemblSourceHost"));
			this.port.setText(Resources.get("ensemblSourcePort"));
		}else {
			this.host.setText(Resources.get("ensemblMartHost"));
			this.port.setText(Resources.get("ensemblMartPort"));
		}
		this.username.setText(Resources.get("ANONYMOUSUSER"));
		this.updateJDBCURL();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("sourcebutton")) {
			this.sourceMode = true;
			this.updateDataBasesList();
		}else if(e.getActionCommand().equals("targetbutton")) {
			this.sourceMode = false;
			this.updateDataBasesList();
		}
		
	}
	
	public boolean isSource() {
		return this.sourceMode;
	}
}

