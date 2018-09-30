package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;


import org.springframework.context.annotation.Import;
import ru.nikityan.easy.rpc.socket.configuration.SpringConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Import(SpringConfiguration.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableEasySocketRpc {
}
