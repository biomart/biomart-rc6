package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.biomart.configurator.utils.MessageConfig;


public class WarningDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox cb;
	private String messageType;
	
	public WarningDialog(String messageType) {
		this.messageType = messageType;
	}
	
	
	public void showDialogFor(String messageType, String message) {
		//default is 1
		this.messageType = messageType;
		MessageConfig.getInstance().put(messageType, 1);
		this.setLayout(new BorderLayout());
		JPanel centralPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JLabel messageLabel = new JLabel(message);
		JButton okButton = new JButton("YES");
		okButton.setActionCommand("yes");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("NO");
		cancelButton.setActionCommand("no");
		cancelButton.addActionListener(this);
		
		cb = new JCheckBox("don't warn me anymore");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(cb);
		
		centralPanel.add(messageLabel);
		
		this.add(centralPanel,BorderLayout.CENTER);
		this.add(buttonPanel,BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.pack();
		this.setModal(true);
		this.setVisible(true);
	}
	
	public int getResult() {
		return MessageConfig.getInstance().get(messageType);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("yes")) {
			if(cb.isSelected())
				MessageConfig.getInstance().put(this.messageType, 2);
			else
				MessageConfig.getInstance().put(this.messageType, 0);
		}else if(e.getActionCommand().equals("no")) {
			if(cb.isSelected())
				MessageConfig.getInstance().put(this.messageType, 3);
			else
				MessageConfig.getInstance().put(this.messageType, 1);			
		}	
		this.setVisible(false);
		this.dispose();
	}
}