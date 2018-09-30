package ru.nikityan.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import ru.nikityan.easy.rpc.socket.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nikit on 11.09.2018.
 */
public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {

    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler handler) {
        returnValueHandlers.add(handler);
        return this;
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(List<HandlerMethodReturnValueHandler> handler) {
        returnValueHandlers.addAll(handler);
        return this;
    }

    public void clear() {
        returnValueHandlers.clear();
    }

    public List<HandlerMethodReturnValueHandler> getHandlers() {
        return Collections.unmodifiableList(returnValueHandlers);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, Message<?> message) throws Exception {
        HandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);
        if (handler == null) {
            throw new IllegalStateException("No handler for return value type: " + returnType.getParameterType());
        }
        handler.handleReturnValue(returnValue, returnType, message);
    }

    @Nullable
    private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if (handler.supportsReturnType(returnType)) {
                return handler;
            }
        }
        return null;
    }
}
