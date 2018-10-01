package ru.nikityan.easy.rpc.socket.core;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.support.ListMapTypeAdapter;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.RequestMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikit on 29.09.2018.
 */
public class JsonRequestMessageConverter implements SmartMessageConverter {

    private final Gson gsonConverter;

    public JsonRequestMessageConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        initTypeAdapters().forEach(gsonBuilder::registerTypeAdapter);
        this.gsonConverter = gsonBuilder.create();
    }

    @NotNull
    protected Map<Class<?>, TypeAdapter<?>> initTypeAdapters() {
        ListMapTypeAdapter mapTypeAdapter = new ListMapTypeAdapter();
        Map<Class<?>, TypeAdapter<?>> map = new HashMap<>();
        map.put(List.class, mapTypeAdapter);
        map.put(Map.class, mapTypeAdapter);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        JsonRpcRequest request = getRequest(message);
        if (request != null) {
            return gsonConverter.fromJson(request.getParams(), targetClass);
        }
        return null;
    }

    @Override
    public Message<?> toMessage(Object payload, MessageHeaders headers) {
        long messageId = headers == null ? -1 : headers.getId();
        if (payload instanceof JsonRpcError) {
            JsonRpcError rpcError = (JsonRpcError) payload;
            JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(messageId, rpcError);
            return MessageBuilder
                    .fromPayload(jsonRpcResponse)
                    .withHeaders(headers)
                    .build();
        }
        JsonRpcResponse rpcResponse = new JsonRpcResponse(messageId, payload);
        return MessageBuilder.fromPayload(rpcResponse)
                .withHeaders(headers)
                .build();
    }

    @Nullable
    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {
        if (conversionHint == null) {
            return fromMessage(message, targetClass);
        }
        if (conversionHint instanceof String) {
            JsonRpcRequest request = getRequest(message);
            if (request == null) {
                return null;
            }
            JsonElement params = request.getParams();
            if (!params.isJsonObject()) {
                throw new IllegalStateException("Can not correlate param name with input json element," +
                        " use json object for it");
            }
            JsonObject jsonObject = params.getAsJsonObject();
            JsonElement jsonElement = jsonObject.get((String) conversionHint);
            return gsonConverter.fromJson(jsonElement, targetClass);
        }
        return null;
    }

    @Nullable
    @Override
    public Message<?> toMessage(Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint) {
        return toMessage(payload, headers);
    }

    @Nullable
    private JsonRpcRequest getRequest(Message<?> message) {
        if (message instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) message;
            return requestMessage.getRequest();
        }
        return null;
    }
}
