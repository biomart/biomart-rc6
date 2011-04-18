package org.biomart.configurator.view.component.container;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViewPortal;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.objects.portal.MartPointer;
import org.biomart.objects.portal.UserGroup;

public class ConfigTableCellRenderer extends DefaultTableCellRenderer {
	

	@Override
	protected void setValue(Object obj) {
		// TODO Auto-generated method stub
		super.setValue(obj);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object obj,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component c = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);

		//this.setToolTipText(obj.toString());
		final UserGroup user = ((McViewPortal)McViews.getInstance().getView(IdwViewType.PORTAL)).getUser();
		
		StringBuffer iconPath = new StringBuffer("images/");
		MartPointer mp = (MartPointer)((SharedDataModel) table.getModel()).elementAt(row);
		if(mp.isActivatedInUser(user)) {
			iconPath.append("config");
		}
		else {
			iconPath.append("config_h");
		}
		iconPath.append(".gif");
		this.setIcon(McUtils.createImageIcon(iconPath.toString()));
			
		
		
		return c;
	}

}
