package ru.nikityan.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcNotification;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRpcMessageConverter implements SmartMessageConverter {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        return fromMessage(message, targetClass, null);
    }

    @Override
    public Message<?> toMessage(Object payload, @Nullable MessageHeaders headers) {
        return toMessage(payload, headers, null);
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {
        Object payload = message.getPayload();
        return gson.fromJson(payload.toString(), targetClass);
    }

    @Override
    public Message<?> toMessage(Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint) {
        if (headers == null) {
            return MessageBuilder.fromPayload(payload).build();
        }
        MessageType messageType = headers.getMessageType();
        if (messageType == MessageType.NOTIFICATION) {
            String messageMethod = MessageHeaderAccessor.getMessageMethod(headers);
            if (messageMethod == null) {
                messageMethod = getMessageMethod(conversionHint);
            }
            JsonRpcNotification notification = new JsonRpcNotification(messageMethod, payload);
            return MessageBuilder.fromPayload(notification)
                    .withHeaders(headers)
                    .build();
        } else if (messageType == MessageType.RESPONSE) {
            JsonRpcResponse rpcResponse = new JsonRpcResponse(headers.getId(), payload);
            return MessageBuilder.fromPayload(rpcResponse)
                    .withHeaders(headers)
                    .build();
        }
        return MessageBuilder.fromPayload(payload).withHeaders(headers).build();
    }

    @Nullable
    private String getMessageMethod(Object conversionHint) {
        if (conversionHint != null && conversionHint instanceof String) {
            return (String) conversionHint;
        }
        return null;
    }
}
