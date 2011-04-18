/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biomart.api.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.biomart.api.Jsoml;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.objects.portal.UserGroup;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author jhsu
 */
@XmlRootElement(name="processorGroup")
public class ProcessorGroup extends LiteMartConfiguratorObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private org.biomart.processors.ProcessorGroup processorGroupObject;
	private UserGroup currentUser;

    public ProcessorGroup() {}

    public ProcessorGroup(org.biomart.processors.ProcessorGroup processorGroupObject, UserGroup user) {
        super(processorGroupObject);
        this.processorGroupObject = processorGroupObject;
        this.currentUser = user;
    }

    @JsonIgnore
    public List<Processor> getProcessorList() {
        List<Processor> list = new ArrayList<Processor>();
        for (org.biomart.processors.Processor p : this.processorGroupObject.getProcessorList()) {
           list.add(new Processor(p,this.currentUser)); 
        }
        return list;
    }

    
    @JsonIgnore
    public Processor getDefaultProcessor() {
    	org.biomart.processors.Processor defaultProcessor = this.processorGroupObject.getDefaultProcessor();
    	if(defaultProcessor == null)
    		return null;
    	
    	return new Processor(defaultProcessor,this.currentUser);
    }
    

    
    @Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.getXMLElementName());

		jsoml.setAttribute("name", super.getName());
		jsoml.setAttribute("displayName", super.getDisplayName());
		return jsoml;
    }
}
