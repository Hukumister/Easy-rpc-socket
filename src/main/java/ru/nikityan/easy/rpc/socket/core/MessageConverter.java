package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

/**
 * Message convertor  provides methods for convert to message and convert from message.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public interface MessageConverter {

    /**
     * Convert from incoming message to target class.
     *
     * @return object of target class from incoming message.
     */
    Object fromMessage(Message<?> message, Class<?> targetClass);

    /**
     * Convert to message from given headers and payload.
     *
     * @return the message.
     */
    Message<?> toMessage(Object payload, MessageHeaders headers);

    /**
     * Convert to message from payload.
     *
     * @return the message.
     */
    default Message<?> toMessage(Object payload) {
        return toMessage(payload, null);
    }
}

