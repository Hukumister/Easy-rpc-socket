package ru.nikityan.easy.rpc.socket;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;


public class JettyWebSocketTestServer implements WebSocketTestServer {

    private Server server;

    private ServletContextHandler contextHandler;

    private int port;

    @Override
    public void setup() {
        this.server = new Server(8088);
    }

    @Override
    public void start() throws Exception {
        this.server.start();
        this.contextHandler.start();

        Connector[] connectors = server.getConnectors();
        NetworkConnector connector = (NetworkConnector) connectors[0];
        this.port = connector.getLocalPort();
    }

    @Override
    public void stop() throws Exception {
        try {
            if (this.contextHandler.isRunning()) {
                this.contextHandler.stop();
            }
        } finally {
            if (this.server.isRunning()) {
                this.server.setStopTimeout(5000);
                this.server.stop();
            }
        }
    }

    @Override
    public void config(WebApplicationContext webApplicationContext) {
        ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(webApplicationContext));
        this.contextHandler = new ServletContextHandler();
        this.contextHandler.addServlet(servletHolder, "/");
        this.server.setHandler(this.contextHandler);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public ServletContext getServletContext() {
        return this.contextHandler.getServletContext();
    }
}
