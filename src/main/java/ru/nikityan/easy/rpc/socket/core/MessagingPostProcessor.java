package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public interface MessagingPostProcessor {

    /**
     * Handle this method before send message and after convert.
     *
     * @param message given message.
     * @return changed message to send. Not return null.
     */
    Message<?> postProcessMessage(Message<?> message);
}
