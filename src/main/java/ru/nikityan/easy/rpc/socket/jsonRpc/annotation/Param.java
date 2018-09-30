package ru.nikityan.easy.rpc.socket.jsonRpc.annotation;

import java.lang.annotation.*;

/**
 * Created by Nikit on 17.09.2018.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    String value() default "";
}
