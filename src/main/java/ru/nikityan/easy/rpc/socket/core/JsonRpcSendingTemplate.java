package ru.nikityan.easy.rpc.socket.core;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.exceptions.MessageSendException;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import java.util.Map;

/**
 * Created by Nikit on 30.09.2018.
 */
public class JsonRpcSendingTemplate implements MessageSendingOperations {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final MessageChannel messageChannel;

    private final MessageConverter messageConverter = new JsonRpcMessageConverter();

    public JsonRpcSendingTemplate(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    @Override
    public void send(Message<?> message) throws MessagingException {
        doSend(message);
    }

    @Override
    public void send(String destination, Message<?> message) throws MessagingException {
        MessageHeaderAccessor messageHeaderAccessor = MessageHeaderAccessor.ofMessage(message);
        messageHeaderAccessor.setMessageMethod(destination);
        MessageHeaders messageHeaders = messageHeaderAccessor.getMessageHeaders();
        messageHeaderAccessor.setImmutable();
        Message<?> sendMessage = MessageBuilder.fromMessage(message)
                .withHeaders(messageHeaders)
                .build();
        doSend(sendMessage);
    }

    @Override
    public void convertAndSend(String destination, Object payload) throws MessagingException {
        convertAndSend(destination, payload, null, null);
    }

    @Override
    public void send(String destination, Object payload, MessagingPostProcessor postProcessor) throws MessagingException {
        convertAndSend(destination, payload, null, postProcessor);
    }

    @Override
    public void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException {
        convertAndSend(destination, payload, headers, null);
    }

    private void doSend(Message<?> message) {
        String messageMethod = MessageHeaderAccessor.getMessageMethod(message.getMessageHeader());
        if (StringUtils.isEmpty(messageMethod)) {
            throw new IllegalArgumentException("Message method is required");
        }

        boolean sent = this.messageChannel.send(message);
        logger.debug("Send message, messageMethod = {}, message = {}", messageMethod, message);
        if (!sent) {
            throw new MessageSendException(message, "Failed to send message to destination " + messageMethod);
        }
    }

    protected void convertAndSend(String destination, Object payload, @Nullable Map<String, Object> headers,
                                  @Nullable MessagingPostProcessor postProcessor) {
        MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.NOTIFICATION, -1L);
        Message<?> message = doConvert(payload, messageHeaders, postProcessor);
        send(destination, message);
    }

    protected Message<?> doConvert(Object payload, MessageHeaders messageHeaders,
                                   @Nullable MessagingPostProcessor postProcessor) {
        Message<?> message = messageConverter.toMessage(payload, messageHeaders);
        if (postProcessor != null) {
            postProcessor.postProcessMessage(message);
        }
        return message;
    }
}
