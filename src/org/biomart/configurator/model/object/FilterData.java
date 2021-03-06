package org.biomart.configurator.model.object;

import org.biomart.common.utils.AlphanumComparator;


public class FilterData implements Comparable<FilterData> {
	private String name;
	private String displayName;
	private boolean isSelected;
	
	public FilterData(String name, String displayName, boolean isSelected) {
		this.name = name;
		this.displayName = displayName;
		this.isSelected = isSelected;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isSelected() {
		return isSelected;
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
	public boolean equals(Object object) {
		if(this == object)
			return true;
		if(!(object instanceof FilterData))
			return false;
		else {
			return ((FilterData)object).getName().equals(this.getName());
		}
	}

	public String toString() {
		return this.displayName;
	}

	public String toSavedFormat() {
		String tmpname = this.name;
		String tmpdisplayname = this.displayName;
		if(this.name.indexOf("|")>=0) {
			tmpname = tmpname.replaceAll("\\|", "\\\\|");
		}
		if(this.displayName.indexOf("|")>=0) {
			tmpdisplayname = tmpdisplayname.replaceAll("\\|", "\\\\|");
		}
		return tmpname+"|"+tmpdisplayname+"|"+Boolean.toString(isSelected);
	}


	public int compareTo(FilterData arg0) {
		AlphanumComparator alphacomp = new AlphanumComparator();
		return alphacomp.compare(this.getName(), arg0.getName());
	}
}