package ru.nikityan.easy.rpc.socket.core;


import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;

import java.util.Map;

public interface MessageSendingOperations {

    void send(Message<?> message) throws MessagingException;

    void send(String destination, Message<?> message) throws MessagingException;

    void convertAndSend(String destination, Object payload) throws MessagingException;

    void send(String destination, Object payload, MessagingPostProcessor postProcessor) throws MessagingException;

    void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException;
}
