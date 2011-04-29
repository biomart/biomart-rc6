package org.biomart.api.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.ArrayUtils;
import org.biomart.api.Authenticator;
import org.biomart.api.BioMartApiException;
import org.biomart.api.BioMartUnauthorizedException;
import org.biomart.api.Portal;
import org.biomart.api.PortalService;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.api.lite.Attribute;
import org.biomart.api.lite.Container;
import org.biomart.api.lite.Dataset;
import org.biomart.api.lite.Filter;
import org.biomart.api.lite.FilterData;
import org.biomart.api.lite.GuiContainer;
import org.biomart.api.lite.Mart;
import org.biomart.api.lite.Processor;
import org.biomart.api.lite.ProcessorGroup;
import org.biomart.api.rest.annotations.Cache;
import org.biomart.common.exceptions.FunctionalException;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;

/**
 *
 * @author jhsu
 */
@Singleton
@Path("/martservice")
public class PortalResource implements PortalService {
    private static final int MAX_AGE = 3600;

    private MartRegistryFactory factory;

    @Context HttpServletRequest servletRequest;
    @Context HttpServletResponse servletResponse;

    @Inject
    public PortalResource(MartRegistryFactory factory) {
        this.factory = factory;
    }

    @Path("portal")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public GuiContainer getRootGuiContainer(@QueryParam("guitype") String guitype) {
        return getPortal().getRootGuiContainer(guitype);
    }

    @Path("gui")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    public GuiContainer getGuiContainer(@QueryParam("name") String name) {
        return getPortal().getGuiContainer(name);
    }

    @Path("marts")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Override
    @Cache(maxAge = MAX_AGE)
    public List<Mart> getMarts( @QueryParam("guicontainer") String guiContainerName) {
        return getPortal().getMarts(guiContainerName);
    }

    @Path("datasets")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<Dataset> getDatasets(@QueryParam("mart") String martName) {
        return getPortal().getDatasets(martName);
    }

    @Path("filters")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<Filter> getFilters(
            @QueryParam("datasets") String datasets,
            @QueryParam("config") String config,
            @QueryParam("container") String container) {
        Portal portal = getPortal();
        List<Filter> filters;
        return portal.getFilters(datasets, config, container);
    }

    @Path("attributes")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<Attribute> getAttributes(
            @QueryParam("datasets") String datasets,
            @QueryParam("config") String config,
            @QueryParam("container") String container) {
        Portal portal = getPortal();
        return portal.getAttributes(datasets, config, container);
    }

    @Path("containers")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public Container getContainers(
            @QueryParam("datasets") String datasets,
            @QueryParam("config") String config,
            @QueryParam("withattributes") @DefaultValue("true") Boolean withattributes,
            @QueryParam("withfilters") @DefaultValue("true") Boolean withfilters) {
        return getPortal().getContainers(datasets, config, withattributes, withfilters);
    }

    @Path("linkables")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<Dataset> getLinkables(@QueryParam("datasets") String datasets) {
        return getPortal().getLinkables(datasets);
    }

    @Path("processorgroups")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Override
    @Cache(maxAge = MAX_AGE)
    public List<ProcessorGroup> getProcessorGroups(@QueryParam("mart") String mart) {
        return getPortal().getProcessorGroups(mart);
    }

    @Path("processors")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<Processor> getProcessors(@QueryParam("mart") String mart,
            @QueryParam("processorgroup") String group) {
        return getPortal().getProcessors(mart, group);
    }

    // Special requests
    @Path("datasets/mapped")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = MAX_AGE)
    public Map<String,List<Dataset>> getMappedDatasetsAsJson(@QueryParam("mart") String martNames) {
        return getPortal().getDatasetMapByMarts(martNames);
    }

    @Path("datasets/mapped")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Cache(maxAge = MAX_AGE)
    public Response getMappedDatasetsAsXml(@QueryParam("mart") String martNames) {
        try {
            Map map = getPortal().getDatasetMapByMarts(martNames);
            return ResponseFormatter.prepare("xml", map, "");
        } catch (Exception e) { throw new BioMartApiException(e); }
    }

    @Path("attributes/mapped")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = MAX_AGE)
    public Response getAttributeMapByDatasetAsJson(@QueryParam("datasets") String datasets,
            @QueryParam("config") String config, @QueryParam("container") String container) {
        try {
            return ResponseFormatter.prepare("json", getPortal().getAttributeMapByDataset(datasets, config, container), "");
        } catch (Exception e) { throw new BioMartApiException(e); }
    }

    @Path("filters/mapped")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Cache(maxAge = MAX_AGE)
    public Response getFilterMapByDatasetAsJson(@QueryParam("datasets") String datasets,
            @QueryParam("config") String config, @QueryParam("container") String container) {
        try {
            return ResponseFormatter.prepare("json", getPortal().getFilterMapByDataset(datasets, config, container), "");
        } catch (Exception e) { throw new BioMartApiException(e); }
    }

    @Path("attributes/mapped")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Cache(maxAge = MAX_AGE)
    public Response getAttributeMapByDatasetAsXml(@QueryParam("datasets") String datasets,
            @QueryParam("config") String config, @QueryParam("container") String container) {
        try {
            return ResponseFormatter.prepare("xml", getPortal().getAttributeMapByDataset(datasets, config, container), "");
        } catch (Exception e) { throw new BioMartApiException(e); }
    }

    @Path("filters/mapped")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = MAX_AGE)
    public Response getFilterMapByDatasetAsXml(@QueryParam("datasets") String datasets,
            @QueryParam("config") String config, @QueryParam("container") String container) {
        try {
            return ResponseFormatter.prepare("xml", getPortal().getFilterMapByDataset(datasets, config, container), "");
        } catch (Exception e) { throw new BioMartApiException(e); }
    }

    // Only available for XML
    @Path("xml/configs/{mart}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Cache(maxAge = MAX_AGE)
    public Response getConfigsForMart(
            @QueryParam("callback") @DefaultValue("") String callback,
            @PathParam("mart") String martName)
            throws TechnicalException, FunctionalException {

        List<Mart> marts = getPortal().getMarts(null);
        for (Mart mart : marts) {
            if (mart.getName().equals(martName)) {
                return Response.ok(mart.getMartInXML()).build();
            }
        }
        return Response.status(Status.NOT_FOUND).entity("Mart not found: " + martName).build();
    }

    @Path("filter_values")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Cache(maxAge = MAX_AGE)
    @Override
    public List<FilterData> getFilterValues(
            @QueryParam("filter") String filterName,
            @QueryParam("value") String value,
            @QueryParam("datasets") String datasets) {
        return getPortal().getFilterData(datasets, filterName, value);
    }

    /*
     * Results handling
     */
    @Path("results")
    @GET
    public Response doGetStreamResults(
            @QueryParam("download") @DefaultValue("false")  Boolean download,
            @QueryParam("iframe") @DefaultValue("false") Boolean iframe,
            @QueryParam("uuid") @DefaultValue("default") String uuid,
            @QueryParam("scope") @DefaultValue("biomart.streaming") String scope,
            @QueryParam("query") String query) throws Exception {

        return handleResults(query, download, iframe, uuid, scope);
    }

    @Path("results")
    @POST
    public Response doPostStreamResults(
            @FormParam("download") @DefaultValue("false")  Boolean download,
            @FormParam("iframe") @DefaultValue("false")  Boolean iframe,
            @FormParam("uuid") @DefaultValue("default") String uuid,
            @FormParam("scope") @DefaultValue("biomart.streaming") String scope,
            @FormParam("query") String query) throws Exception {
        return handleResults(query, download, iframe, uuid, scope);
    }

    @Override
    public String getResults(String xml) {
        OutputStream out = new ByteArrayOutputStream();
        getPortal().executeQuery(xml, out);
        return out.toString();
    }

    protected Response handleResults(final String query, final boolean download,
            final boolean iframe, final String uuid, final String scope) throws BioMartApiException {

        final Portal portal = getPortal();
        final ProcessorStreamingOutput so =  new ProcessorStreamingOutput(query,
                portal, iframe, uuid, scope, getAcceptHeader());
        final String mime = so.getContentType();

        if (download) {
            servletResponse.setHeader("Content-Disposition", "attachment; filename=results.txt");
        }

        return Response.ok(so, mime).build();
    }

    private String[] getAcceptHeader() {
        String accept = servletRequest.getHeader("Accept");

        if (accept == null)
            return ArrayUtils.EMPTY_STRING_ARRAY;

        List<String> list = new ArrayList<String>();

        for (String params : accept.split(";")) {
            for (String type : params.split(",")) {
                type = type.trim();
                if (!type.startsWith("q=")) // Ignore qvalue
                    list.add(type);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    protected String getUsername(HttpServletRequest request) {
        String userId = null;

        // 1. OAuth
        try {
            userId = Authenticator.getUsername(request);
        } catch (BioMartUnauthorizedException e) {
            throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build());
        }

        // 2. Session?
        if (userId == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                userId = (String)session.getAttribute("openid_identifier");
            }
        }

        if (userId != null)
            Log.info("Using user="+userId);

        return userId;
    }

    private Portal getPortal() {
        return new Portal(factory, getUsername(servletRequest));
    }
}
