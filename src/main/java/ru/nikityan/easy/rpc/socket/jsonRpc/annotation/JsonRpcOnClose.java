package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation, we mark a method that
 * processes close connection of websocket
 * Warning the method must have two parameters, the first is the object class WebSocketSession
 * the second is necessarily the object class CloseStatus
 *
 * @see org.springframework.web.socket.WebSocketSession
 * @see org.springframework.web.socket.CloseStatus
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcOnClose {
}
