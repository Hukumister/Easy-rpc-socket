package ru.nikityan.easy.rpc.socket.configuration;


import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import ru.nikityan.easy.rpc.socket.handlers.impl.JsonRpcDispatcherHandler;

import javax.servlet.ServletContext;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ServletContext context;
    private final JsonRpcDispatcherHandler jsonRpcDispatcherHandler;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public WebSocketConfig(ServletContext context, JsonRpcDispatcherHandler jsonRpcDispatcherHandler) {
        this.context = context;
        this.jsonRpcDispatcherHandler = jsonRpcDispatcherHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jsonRpcDispatcherHandler, "/*")
                .setHandshakeHandler(handshakeHandler())
                .setAllowedOrigins("*");
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setIdleTimeout(120_000);
        return new DefaultHandshakeHandler(
                new JettyRequestUpgradeStrategy(new WebSocketServerFactory(context, policy)));
    }


}
