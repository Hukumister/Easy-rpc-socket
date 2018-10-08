package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;


import org.springframework.context.annotation.Import;
import ru.nikityan.easy.rpc.socket.configuration.JsonRpcConfiguration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(JsonRpcConfiguration.class)
public @interface EnableEasySocketRpc {
}
