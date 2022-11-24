package org.example;

import org.eclipse.jetty.cdi.CdiDecoratingListener;
import org.eclipse.jetty.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.cdi.CdiSpiDecorator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ListenerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.example.rest.SandwichEndpoint;
import org.glassfish.jersey.servlet.ServletContainer;

public class Main
{
    static
    {
        // Wire up java.util.logging (used by weld) to slf4j.
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    enum WeldMode
    {
        // Do nothing special to integrate Weld, let weld work it out.
        FALLBACK,

        // Expect:INFO: WELD-ENV-001201: Jetty 7.2+ detected, CDI injection will be available in Servlets and Filters. Injection into Listeners is not supported.
        DECORATING_LISTENER,
        // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters.
        CDI_DECORATING_LISTENER,
        // // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
        CDI_SPI_DECORATOR,
        // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
        CDI_SERVLET_CONTAINER_INITIALIZER,
        // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
        CDI_SERVLET_CONTAINER_INITIALIZER_WITH_INTEGRATION_ATTRIBUTE,
        // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
        CDI_SERVLET_CONTAINER_INITIALIZER_WITH_ENHANCED_LISTENER,

        // This is the preferred mode from the Weld team.
        // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
        RECOMMENDED
    }

    public static void main(String[] args) throws Exception
    {
        Server server = newServer(9000, WeldMode.RECOMMENDED);
        server.start();
        server.join();
    }

    public static Server newServer(int port, WeldMode weldMode)
    {
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        setupWeld(context, weldMode);

        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/rest/*");
        servletHolder.setInitOrder(1);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", SandwichEndpoint.class.getPackageName());

        server.setHandler(context);
        return server;
    }

    private static void setupWeld(ServletContextHandler context, WeldMode mode)
    {
        // Setup Jetty weld integration
        switch (mode)
        {
            case FALLBACK:
                // Expect:INFO: WELD-ENV-001201: Jetty 7.2+ detected, CDI injection will be available in Servlets and Filters. Injection into Listeners is not supported.
                context.getServletHandler().addListener(new ListenerHolder(org.jboss.weld.environment.servlet.Listener.class));
                break;

            case DECORATING_LISTENER:
                // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters.
                context.addEventListener(new org.eclipse.jetty.webapp.DecoratingListener(context));
                context.getServletHandler().addListener(new ListenerHolder(org.jboss.weld.environment.servlet.Listener.class));
                break;

            case CDI_DECORATING_LISTENER:
                // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters.
                context.addEventListener(new CdiDecoratingListener(context));
                context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
                break;

            case CDI_SPI_DECORATOR:
                // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
                context.getObjectFactory().addDecorator(new CdiSpiDecorator(context));
                context.getServletHandler().addListener(new ListenerHolder(org.jboss.weld.environment.servlet.Listener.class));
                break;

            case CDI_SERVLET_CONTAINER_INITIALIZER:
                // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
                context.addServletContainerInitializer(new CdiServletContainerInitializer());
                context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
                break;

            case CDI_SERVLET_CONTAINER_INITIALIZER_WITH_INTEGRATION_ATTRIBUTE:
                // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
                context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
                context.addServletContainerInitializer(new CdiServletContainerInitializer());
                context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
                break;

            case CDI_SERVLET_CONTAINER_INITIALIZER_WITH_ENHANCED_LISTENER:
                // Expect:INFO: WELD-ENV-001213: Jetty CDI SPI support detected, CDI injection will be available in Listeners, Servlets and Filters.
                context.addServletContainerInitializer(new CdiServletContainerInitializer());
                context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());
                break;

            // NOTE: This is the preferred mode from the Weld team.
            case RECOMMENDED:
                // Expect:INFO: WELD-ENV-001212: Jetty CdiDecoratingListener support detected, CDI injection will be available in Listeners, Servlets and Filters
                context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
                context.addServletContainerInitializer(new CdiServletContainerInitializer());
                context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());
                break;
        }
    }
}
