package org.biomart.api.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

import org.biomart.api.Jsoml;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.objects.portal.UserGroup;
import org.codehaus.jackson.annotate.JsonIgnore;


@XmlRootElement(name="processor")
public class Processor extends LiteMartConfiguratorObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private org.biomart.processors.Processor processorObject; 
    private boolean includeContainers;
    private UserGroup currentUser;

    public Processor() {}

    public Processor(org.biomart.processors.Processor processorObject, UserGroup user) {
        super(processorObject);
        this.processorObject = processorObject;
        this.currentUser = user;
    }
	   
    @JsonIgnore
	public org.biomart.api.lite.Container getContainer() {
		org.biomart.objects.objects.Container c =  this.processorObject.getContainer(); 
		
		if(c.isEmpty() || c.isHidden())
			return null;
		//get all datasets
		List<org.biomart.objects.objects.Dataset> dsList = ((org.biomart.objects.portal.MartPointer)this.processorObject.
				getParent().getParent()).getDatasetList(false);
		List<String> dsNames = new ArrayList<String>();
		for(org.biomart.objects.objects.Dataset ds: dsList)
			dsNames.add(ds.getName());
		return (new org.biomart.api.lite.Container(c, dsNames, true, true,this.currentUser, true));
	}
	
	public org.biomart.api.lite.Container getContainer(List<String> datasets, boolean includeAttributes, boolean includeFilters) {
		org.biomart.objects.objects.Container c =  this.processorObject.getContainer(); 
		
		if(c.isEmpty() || c.isHidden())
			return null;
		return (new org.biomart.api.lite.Container(c, datasets, includeAttributes, includeFilters,this.currentUser, true));
	}

    @Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.getXMLElementName());

		jsoml.setAttribute("name", super.getName());
		jsoml.setAttribute("displayName", super.getDisplayName());
        if (this.includeContainers && this.getContainer()!=null) {

            jsoml.addContent(this.getContainer().generateExchangeFormat(xml));
    		
        }
		return jsoml;
    }
}