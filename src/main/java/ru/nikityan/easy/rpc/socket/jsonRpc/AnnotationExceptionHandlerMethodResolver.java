package ru.nikityan.easy.rpc.socket.jsonRpc;

import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import ru.nikityan.easy.rpc.socket.handler.AbstractExceptionHandlerMethodResolver;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class AnnotationExceptionHandlerMethodResolver extends AbstractExceptionHandlerMethodResolver {

    protected AnnotationExceptionHandlerMethodResolver(Class<?> handlerType) {
        super(initExceptionMappings(handlerType));
    }

    private static Map<Class<? extends Throwable>, Method> initExceptionMappings(Class<?> handlerType) {
        Map<Method, ExceptionHandler> methods = MethodIntrospector.selectMethods(handlerType,
                (MethodIntrospector.MetadataLookup<ExceptionHandler>)
                        method -> AnnotationUtils.findAnnotation(method, ExceptionHandler.class));

        Map<Class<? extends Throwable>, Method> result = new HashMap<>();
        for (Map.Entry<Method, ExceptionHandler> entry : methods.entrySet()) {
            Method method = entry.getKey();

            List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>();
            exceptionTypes.addAll(Arrays.asList(entry.getValue().value()));

            if (exceptionTypes.isEmpty()) {
                exceptionTypes.addAll(getExceptionsFromMethodSignature(method));
            }

            for (Class<? extends Throwable> exceptionType : exceptionTypes) {
                Method oldMethod = result.put(exceptionType, method);
                if (oldMethod != null && !oldMethod.equals(method)) {
                    throw new IllegalStateException("Ambiguous @ExceptionHandler method mapped for [" +
                            exceptionType + "]: {" + oldMethod + ", " + method + "}");
                }
            }
        }
        return result;
    }

}
