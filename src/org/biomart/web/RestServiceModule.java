package org.biomart.web;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.biomart.api.factory.XmlMartRegistryModule;
import org.biomart.processors.ProcessorModule;
import org.mortbay.servlet.GzipFilter;

/**
 *
 * @author jhsu
 */
public class RestServiceModule extends ServletModule {
    @Override
    protected void configureServlets() {
        install(new XmlMartRegistryModule());
        install(new ProcessorModule());

        bind(GzipFilter.class).in(Scopes.SINGLETON);

        filter("*").through(AuthFilter.class);
        filter("*").through(FlashMessageFilter.class);
        filter("*").through(LocationsFilter.class);
        filter("/admin/*").through(ForceHttpsFilter.class);
        filter("*").through(GzipFilter.class, new ImmutableMap.Builder<String,String>()
                .put("mimeTypes", "text/css,text/javascript,image/svg+xml")
                .build());

        filter("*").through(PortalFilter.class);

        filter("/rest/*", "/martservice/*", "/biomart/*", "/semantic/*").through(GuiceContainer.class,
            new ImmutableMap.Builder<String,String>()
               .put("javax.ws.rs.Application", "org.biomart.api.rest.App")
               .put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.biomart.api;org.codehaus.jackson.jaxrs")
               .build());
    }
}
