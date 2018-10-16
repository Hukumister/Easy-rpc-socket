package ru.coderedwolf.easy.rpc.socket.exceptions;

import org.springframework.core.MethodParameter;
import ru.coderedwolf.easy.rpc.socket.Message;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class MethodArgumentResolutionException extends MessageSendException {

    private final MethodParameter parameter;

    /**
     * Create a new instance providing the invalid {@code MethodParameter}.
     */
    public MethodArgumentResolutionException(Message<?> message, MethodParameter parameter) {
        super(message, getMethodParameterMessage(parameter));
        this.parameter = parameter;
    }

    /**
     * Create a new instance providing the invalid {@code MethodParameter} and
     * a prepared description.
     */
    public MethodArgumentResolutionException(Message<?> message, MethodParameter parameter, String description) {
        super(message, getMethodParameterMessage(parameter) + ": " + description);
        this.parameter = parameter;
    }


    /**
     * Return the MethodParameter that was rejected.
     */
    public final MethodParameter getMethodParameter() {
        return this.parameter;
    }


    private static String getMethodParameterMessage(MethodParameter parameter) {
        return "Could not resolve method parameter at index " + parameter.getParameterIndex() +
                " in " + parameter.toString();
    }
}
