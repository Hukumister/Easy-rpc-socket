package ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "JsonRpcController" use protocol JsonRpc
 * This annotation marks the class that is the jsonrpc controller.
 * This jsonrpc controller processes requests via the Json Rpc protocol
 * <p>
 *
 * @author CodeRedWolf
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcController {
}
