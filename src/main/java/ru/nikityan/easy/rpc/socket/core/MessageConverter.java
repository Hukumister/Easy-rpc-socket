package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

/**
 * Created by Nikit on 24.08.2018.
 */
public interface MessageConverter {

    Object fromMessage(Message<?> message, Class<?> targetClass);

    Message<?> toMessage(Object payload, MessageHeaders headers);

    default Message<?> toMessage(Object payload) {
        return toMessage(payload, null);
    }
}

