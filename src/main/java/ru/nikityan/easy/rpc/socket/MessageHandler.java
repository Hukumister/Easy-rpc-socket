package ru.nikityan.easy.rpc.socket;


import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;

/**
 * Interface contains a method for hande message {@link Message}
 *
 * @author CodeRedWolf
 * @since 1.0
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * Handle given message.
     *
     * @param message the message to be handled
     * @throws MessagingException if handle fail method.
     */
    void handleMessage(Message<?> message) throws MessagingException;
}
