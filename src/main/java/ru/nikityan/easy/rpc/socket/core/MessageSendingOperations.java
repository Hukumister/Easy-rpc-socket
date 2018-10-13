package ru.nikityan.easy.rpc.socket.core;


import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;

import java.util.Map;

/**
 * The interface provides methods for sending notification to subscribers.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public interface MessageSendingOperations {

    /**
     * Send given message.
     * Use the method with care so all necessary headers may not be initialized.
     *
     * @param message incoming message.
     * @throws MessagingException if fail to send message.
     */
    void send(Message<?> message) throws MessagingException;

    /**
     * Send incoming message for all subscribers by method (destination).
     *
     * @param destination name of subscribe.
     * @param message     given message. Use the method with care so all necessary headers may not be initialized.
     * @throws MessagingException if fail to send message.
     */
    void send(String destination, Message<?> message) throws MessagingException;

    /**
     * Send message thar create from incoming payload for all subscribers by method (destination).
     *
     * @param destination   name of subscribe.
     * @param payload       param of notification.
     * @param postProcessor than handle before send and after convert to message.
     * @throws MessagingException if fail to send message.
     * @see ru.nikityan.easy.rpc.socket.jsonRpc.annotation.Subscribe
     */
    void convertAndSend(String destination, Object payload, MessagingPostProcessor postProcessor) throws MessagingException;

    /**
     * Send message thar create from incoming payload for all subscribers by method (destination).
     *
     * @param destination name of subscribe.
     * @param payload     param of notification.
     * @throws MessagingException if fail to send message.
     * @see ru.nikityan.easy.rpc.socket.jsonRpc.annotation.Subscribe
     */
    void convertAndSend(String destination, Object payload) throws MessagingException;

    /**
     * Send message thar create from incoming payload and add custom headers for all subscribers by method (destination).
     *
     * @param destination name of subscribe.
     * @param payload     param of notification.
     * @param headers     custom headers. This header now you can use only on message post processor.
     * @throws MessagingException if fail to send message.
     * @see MessagingPostProcessor
     */
    void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException;
}
