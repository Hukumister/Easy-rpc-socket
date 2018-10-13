package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;


import org.springframework.context.annotation.Import;
import ru.nikityan.easy.rpc.socket.configuration.JsonRpcConfiguration;

import java.lang.annotation.*;

/**
 * With the help of this annotation all necessary library beans are connected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(JsonRpcConfiguration.class)
public @interface EnableEasySocketRpc {
}
