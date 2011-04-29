/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biomart.api.rdf;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbaran
 */
public class RDFClass extends RDFObject {
    public final List<RDFProperty> properties = new LinkedList<RDFProperty>();
    public final List<String> uriAttributes = new LinkedList<String>();

    public RDFClass(String name, String[] uriAttributes, Map<String, String> namespaces) {
        super(name, namespaces);

        if (uriAttributes != null && uriAttributes.length > 0)
            this.uriAttributes.addAll(Arrays.asList(uriAttributes));
    }

    public void addProperty(RDFProperty newProperty) {
        for (int i = 0; i < properties.size(); i++) {
            RDFProperty currentProperty = properties.get(i);

            if (currentProperty.isSimilar(newProperty)) {
                if (currentProperty.getFilter().isEmpty()) {
                    properties.remove(i);
                    properties.add(newProperty);
                }
                return;
            }
        }
        properties.add(newProperty);
    }

    public List<RDFProperty> getProperties() { return Collections.unmodifiableList(properties); }
    public List<String> getURIAttributes() { return Collections.unmodifiableList(uriAttributes); }
}