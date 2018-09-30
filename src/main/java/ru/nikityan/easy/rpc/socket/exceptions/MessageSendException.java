package ru.nikityan.easy.rpc.socket.exceptions;

import ru.nikityan.easy.rpc.socket.Message;

/**
 * Created by Nikit on 24.08.2018.
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
