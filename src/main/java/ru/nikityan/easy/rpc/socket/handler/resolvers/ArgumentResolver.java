package ru.nikityan.easy.rpc.socket.handler.resolvers;

import org.springframework.core.MethodParameter;
import ru.nikityan.easy.rpc.socket.Message;

/**
 * Created by Nikit on 26.08.2018.
 */
public interface ArgumentResolver {

    /**
     * @param parameter
     * @return
     */
    boolean supportsParameter(MethodParameter parameter);

    /**
     * @param parameter
     * @param message
     * @return
     * @throws Exception
     */
    Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception;
}
