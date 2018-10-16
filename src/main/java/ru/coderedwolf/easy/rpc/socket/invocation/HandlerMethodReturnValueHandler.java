package ru.coderedwolf.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import ru.coderedwolf.easy.rpc.socket.Message;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(@Nullable Object returnValue,
                           MethodParameter returnType,
                           Message<?> message) throws Exception;
}
