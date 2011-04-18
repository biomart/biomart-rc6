/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biomart.api.rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jbaran
 */
public class Ontology {
    private Map<String, String> namespaces = new HashMap<String, String>(); // Prefix -> Namespace URI
    private Map<String, RDFClass> classes = new HashMap<String, RDFClass>(); // Class name -> Class

    public void addNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
    }

    public void addClass(String className, String[] uriAttributes) {
        classes.put(className, new RDFClass(className, uriAttributes, namespaces));
    }

    public void addProperty(String attributeName, String filterName, String className, String property, String range) {
        RDFClass rdfClass = classes.get(className);

        if (rdfClass == null)
            return; // TODO: Exception? Config is missing class definition...

        setNamespace(rdfClass);

        RDFProperty rdfProperty = new RDFProperty(attributeName, filterName, property, range, namespaces);

        setNamespace(rdfProperty);
        rdfClass.addProperty(rdfProperty);
        classes.put(className, rdfClass);
    }

    public Collection<RDFClass> getRDFClasses() { return Collections.unmodifiableCollection(classes.values()); }

    private void setNamespace(RDFObject ro) {
        for (String prefix : namespaces.keySet())
            if (ro.getName().startsWith(prefix))
                ro.setNamespace(prefix, namespaces.get(prefix));
    }
}