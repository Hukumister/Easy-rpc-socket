package ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation;

import java.lang.annotation.*;

/**
 * Annotation which indicates that this parameter should be taken from the incoming message.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    /**
     * The name of parameter
     */
    String value() default "";
}
