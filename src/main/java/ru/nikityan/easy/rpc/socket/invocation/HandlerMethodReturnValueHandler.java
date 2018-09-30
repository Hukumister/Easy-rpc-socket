package ru.nikityan.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import ru.nikityan.easy.rpc.socket.Message;

/**
 * Created by Nikit on 11.09.2018.
 */
public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(@Nullable Object returnValue,
                           MethodParameter returnType,
                           Message<?> message) throws Exception;
}
