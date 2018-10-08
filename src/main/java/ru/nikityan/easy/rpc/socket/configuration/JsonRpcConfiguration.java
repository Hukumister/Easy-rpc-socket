package ru.nikityan.easy.rpc.socket.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WebSocketConfig.class)
public class SpringConfiguration {

}
