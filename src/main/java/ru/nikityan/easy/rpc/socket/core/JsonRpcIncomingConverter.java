package ru.nikityan.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.exceptions.JsonRequestException;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;

/**
 * Created by Nikit on 01.10.2018.
 */
public class JsonRpcIncomingConverter implements MessageConverter {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Message<?> toMessage(Object payload, @Nullable MessageHeaders headers) {
        String incoming;
        if (payload instanceof String) {
            incoming = (String) payload;
        } else {
            throw new IllegalArgumentException("Converter support only string");
        }
        JsonRpcRequest jsonRpcRequest = null;
        try {
            jsonRpcRequest = gson.fromJson(incoming, JsonRpcRequest.class);
            validRequest(jsonRpcRequest);
            return MessageBuilder.fromPayload(jsonRpcRequest).build();
        } catch (JsonSyntaxException ex) {
            throw new JsonRequestException(ex, JsonRpcError.parseError(), jsonRpcRequest);
        }
    }

    private void validRequest(JsonRpcRequest jsonRpcRequest) {
        if (StringUtils.isEmpty(jsonRpcRequest.getMethod())) {
            throw new JsonRequestException(JsonRpcError.badRequest(), jsonRpcRequest);
        }
    }
}
