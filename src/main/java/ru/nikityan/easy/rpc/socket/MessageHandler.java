package ru.nikityan.easy.rpc.socket;


import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;

@FunctionalInterface
public interface MessageHandler {

    /**
     *
     * @param message
     * @throws MessagingException
     */
    void handleMessage(Message<?> message) throws MessagingException;
}
