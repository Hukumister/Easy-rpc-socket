package ru.nikityan.easy.rpc.socket;


import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

public interface WebSocketTestServer {

    void setup();

    void start() throws Exception;

    void stop() throws Exception;

    void config(WebApplicationContext webApplicationContext);

    int getPort();

    ServletContext getServletContext();
}
