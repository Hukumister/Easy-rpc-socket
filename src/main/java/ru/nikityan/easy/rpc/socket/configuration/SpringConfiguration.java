package ru.nikityan.easy.rpc.socket.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.nikityan.easy.rpc.socket.handlers.impl.JsonRpcDispatcherHandler;

@Configuration
@Import(WebSocketConfig.class)
public class SpringConfiguration {

    @Bean
    public JsonRpcDispatcherHandler jsonRpcDispatcherHandler() {
        return new JsonRpcDispatcherHandler();
    }
}
