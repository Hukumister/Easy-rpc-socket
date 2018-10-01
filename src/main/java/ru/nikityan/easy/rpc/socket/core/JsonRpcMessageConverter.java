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
 * Created by Nikit on 01.10.2018.
 */
public class JsonRpcMessageConverter implements MessageConverter {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        Object payload = message.getPayload();
        return gson.fromJson(payload.toString(), targetClass);
    }

    @Override
    public Message<?> toMessage(Object payload, @Nullable MessageHeaders headers) {
        if (headers != null) {
            MessageType messageType = headers.getMessageType();
            if (messageType == MessageType.NOTIFICATION) {
                String messageMethod = MessageHeaderAccessor.getMessageMethod(headers);
                JsonRpcNotification notification = new JsonRpcNotification(messageMethod, payload);
                return MessageBuilder.fromPayload(notification)
                        .withHeaders(headers)
                        .build();
            } else if (messageType == MessageType.RESPONCE) {
                JsonRpcResponse rpcResponse = new JsonRpcResponse(headers.getId(), payload);
                return MessageBuilder.fromPayload(rpcResponse)
                        .withHeaders(headers)
                        .build();
            }
        }
        return MessageBuilder.fromPayload(payload).withHeaders(headers).build();
    }
}
