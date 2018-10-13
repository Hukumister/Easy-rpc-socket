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

import java.util.Map;

/**
 * This class allows method for create instance of messages.
 *
 * @author CodeRedWolf
 * @since 1.0
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

    /**
     * Create message builder from other message with its headers.
     *
     * @param <T> type of original message.
     * @return instance of messageB builder.
     */
    @NotNull
    public static <T> MessageBuilder<T> fromMessage(Message<T> originalMessage) {
        return new MessageBuilder<>(originalMessage);
    }

    /**
     * Create message builder from payload.
     *
     * @param <T> type of payload.
     * @return instance of messageB builder.
     */
    @NotNull
    public static <T> MessageBuilder<T> fromPayload(T payload) {
        return new MessageBuilder<>(payload);
    }

    /**
     * Set message  headers.
     *
     * @return instance of message builder.
     */
    public MessageBuilder<T> withHeaders(Map<String, Object> messageHeaders) {
        this.headers = messageHeaders;
        return this;
    }

    /**
     * Create message.
     *
     * @return instance of message. {@link Message}
     */
    @SuppressWarnings("unchecked")
    public Message<T> build() {
        Assert.notNull(payload, "Payload must not be null");
        if (payload instanceof JsonRpcResponse) {
            JsonRpcResponse response = (JsonRpcResponse) this.payload;
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.RESPONSE, response.getId());
            return (Message<T>) new ResponseMessage(response, messageHeaders);
        }
        if (payload instanceof JsonRpcNotification) {
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.NOTIFICATION, null);
            return (Message<T>) new NotificationMessage((JsonRpcNotification) payload, messageHeaders);
        }
        if (payload instanceof JsonRpcRequest) {
            JsonRpcRequest request = (JsonRpcRequest) this.payload;
            MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.REQUEST, request.getId());
            MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(messageHeaders);
            accessor.setMessageMethod(request.getMethod());
            return (Message<T>) new RequestMessage(request, accessor.getMessageHeaders());
        }
        return new GenericMessage<>(payload, headers);
    }
}
