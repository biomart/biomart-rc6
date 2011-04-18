package org.biomart.configurator.utils.treelist;

import java.util.ArrayList;
import java.util.List;

import org.biomart.backwardCompatibility.DatasetFromUrl;

public class DBCheckBoxNode extends LeafCheckBoxNode {

	
	  public DBCheckBoxNode(String text, boolean selected) {
		  super(text, selected);
	  }
	
	  

	  /**
	   * if all = false, only selected tables return
	   * @param all
	   * @return
	   */
	  public List<DatasetFromUrl> getDatasetsForUrl(boolean all) {
		  List<DatasetFromUrl> sl = new ArrayList<DatasetFromUrl>();
		  for(LeafCheckBoxNode node:this.tableList) {
			  if(all)
				  sl.add((DatasetFromUrl)node.getUserObject());
			  else if(node.isSelected())
				  sl.add((DatasetFromUrl)node.getUserObject());
		  }
		  return sl;
	  }

}