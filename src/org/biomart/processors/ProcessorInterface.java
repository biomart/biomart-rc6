package org.biomart.processors;

import java.io.IOException;
import java.io.OutputStream;
import org.biomart.queryEngine.Query;
import org.jdom.Document;

/**
 *
 * @author Syed Haider
 *
 * This class is the entry point to query engine. All processors implements the
 * printResults method to comply with ONE generic way of returning the results
 * Processor specific logic of reorganising the results sit within each processor
 */

public interface ProcessorInterface {

    public String getContentType();
    public OutputStream getOutputStream() throws IOException;
    public void preprocess(Document queryXML);
    public void beforeQuery(Query query, OutputStream out) throws IOException;
    public void afterQuery() throws IOException;

    public boolean accepts(String[] accepts);
    public String getDefaultValueForField(String name);
    public boolean isUserDefined(String name);
    public boolean isRequired(String name);
    public String[] getFieldNames();
    public void setFieldValue(String name, String value);
}
