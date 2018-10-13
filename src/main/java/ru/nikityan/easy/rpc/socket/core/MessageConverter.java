package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

/**
 * Created by Nikit on 24.08.2018.
 */
public interface MessageConverter {

    /**
     * @param message
     * @param targetClass
     * @return
     */
    Object fromMessage(Message<?> message, Class<?> targetClass);

    /**
     * @param payload
     * @param headers
     * @return
     */
    Message<?> toMessage(Object payload, MessageHeaders headers);

    /**
     * @param payload
     * @return
     */
    default Message<?> toMessage(Object payload) {
        return toMessage(payload, null);
    }
}

