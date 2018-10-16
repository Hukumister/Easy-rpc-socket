package ru.coderedwolf.easy.rpc.socket.exceptions;

import ru.coderedwolf.easy.rpc.socket.Message;

/**
 * Throw this exception if fail send message.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public class MessageSendException extends MessagingException {

    public MessageSendException(String description) {
        super(description);
    }

    public MessageSendException(String description, Throwable cause) {
        super(description, cause);
    }

    public MessageSendException(Message<?> message, String description) {
        super(message, description);
    }

    public MessageSendException(Message<?> message, String description, Throwable cause) {
        super(message, description, cause);
    }
}
