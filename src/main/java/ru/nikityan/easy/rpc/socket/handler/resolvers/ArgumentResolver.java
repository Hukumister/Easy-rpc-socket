package ru.nikityan.easy.rpc.socket.handler.resolvers;

import org.springframework.core.MethodParameter;
import ru.nikityan.easy.rpc.socket.Message;

/**
 * Resolve argument from request message.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public interface ArgumentResolver {

    /**
     * Method return true if resolver support this type and can handler, false if not support.
     */
    boolean supportsParameter(MethodParameter parameter);

    /**
     * Resolve the message for the method parameter.
     *
     * @param parameter the method parameter
     * @param message   incoming message.
     * @return {@code Mono} for the argument value, possibly empty
     */
    Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception;
}
