/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biomart.api;

import java.util.List;
import org.biomart.api.lite.Attribute;
import org.biomart.api.lite.Container;
import org.biomart.api.lite.Dataset;
import org.biomart.api.lite.Filter;
import org.biomart.api.lite.FilterData;
import org.biomart.api.lite.GuiContainer;
import org.biomart.api.lite.Mart;
import org.biomart.api.lite.Processor;
import org.biomart.api.lite.ProcessorGroup;

/**
 *
 * @author jhsu
 */
public interface PortalService {
    public GuiContainer getRootGuiContainer(String guiType);

    public List<Mart> getMarts(String guiContainerName);

    public List<Dataset> getDatasets(String martName);

    public List<Filter> getFilters(String datasets, String config, String containerName);

    public List<FilterData> getFilterValues(String datasets, String filter, String value);

    public List<Attribute> getAttributes(String datasets, String config, String containerName);

    public Container getContainers(String datasets, String config, Boolean withattributes, Boolean withfilters);

    public List<Dataset> getLinkables(String datasets);

    public String getResults(String xml);
}
