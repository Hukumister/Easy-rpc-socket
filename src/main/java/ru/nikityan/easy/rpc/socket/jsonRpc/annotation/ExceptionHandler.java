package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation marks a method that will catch exceptions thrown in the controller.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {

    /**
     * Interception exception class.
     */
    Class<? extends Throwable>[] value() default {};
}
