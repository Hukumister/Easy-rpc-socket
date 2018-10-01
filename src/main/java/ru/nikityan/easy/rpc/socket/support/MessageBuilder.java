package ru.nikityan.easy.rpc.socket.support;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcNotification;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nikit on 25.08.2018.
 */
public final class MessageBuilder<T> {

    private final T payload;

    @Nullable
    private Map<String, Object> headers;

    private MessageBuilder(T payload) {
        this.payload = payload;
    }

    private MessageBuilder(@Nullable Message<T> originalMessage) {
        Assert.notNull(originalMessage, "Message must not be null");
        this.payload = originalMessage.getPayload();
    }

    @NotNull
    public static <T> MessageBuilder<T> fromMessage(Message<T> originalMessage) {
        return new MessageBuilder<>(originalMessage);
    }

    @NotNull
    public static <T> MessageBuilder<T> fromPayload(T payload) {
        return new MessageBuilder<>(payload);
    }

    public MessageBuilder<T> withHeaders(Map<String, Object> messageHeaders) {
        this.headers = messageHeaders;
        return this;
    }

    @SuppressWarnings("unchecked")
    public Message<T> build() {
        Assert.notNull(payload, "Payload must not be null");
        if (payload instanceof JsonRpcResponse) {
            JsonRpcResponse response = (JsonRpcResponse) this.payload;
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.RESPONCE, response.getId());
            return (Message<T>) new ResponseMessage(response, messageHeaders);
        }
        if (payload instanceof JsonRpcNotification) {
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.NOTIFICATION, null);
            return (Message<T>) new NotificationMessage((JsonRpcNotification) payload, messageHeaders);
        }
        if (payload instanceof JsonRpcRequest) {
            JsonRpcRequest request = (JsonRpcRequest) this.payload;
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(MessageHeaderAccessor.MESSAGE_METHOD, request.getMethod());
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.REQUEST, request.getId());
            return (Message<T>) new RequestMessage(request, messageHeaders);
        }
        return new GenericMessage<>(payload, headers);
    }
}
