package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;

import java.util.Map;

/**
 * Created by Nikit on 30.09.2018.
 */
public class JsonRpcSendingTemplate implements MessageSendingOperations {

    private final MessageChannel messageChannel;

    public JsonRpcSendingTemplate(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public void send(Message<?> message) throws MessagingException {

    }

    @Override
    public void send(String destination, Message<?> message) throws MessagingException {

    }

    @Override
    public void convertAndSend(String destination, Object payload) throws MessagingException {

    }

    @Override
    public void send(String destination, Object payload, MessagingPostProcessor postProcessor) throws MessagingException {

    }

    @Override
    public void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException {

    }
}
