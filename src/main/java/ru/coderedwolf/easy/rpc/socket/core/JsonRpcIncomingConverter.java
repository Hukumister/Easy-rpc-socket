package ru.coderedwolf.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.coderedwolf.easy.rpc.socket.exceptions.JsonRequestException;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;

/**
 * @author CodeRedWolf
 * @since 1.0
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
            validJson(incoming);
            jsonRpcRequest = gson.fromJson(incoming, JsonRpcRequest.class);
            validRequest(jsonRpcRequest);
            return MessageBuilder.fromPayload(jsonRpcRequest).build();
        } catch (JsonSyntaxException ex) {
            throw new JsonRequestException(ex, JsonRpcError.parseError(), jsonRpcRequest);
        }
    }

    private void validJson(String incoming) {
        JsonObject jsonObject = gson.fromJson(incoming, JsonObject.class);
        if(!jsonObject.isJsonObject()){
            throw new JsonRequestException(JsonRpcError.badRequest(), null);
        }
    }

    private void validRequest(JsonRpcRequest jsonRpcRequest) {
        if (StringUtils.isEmpty(jsonRpcRequest.getMethod())) {
            throw new JsonRequestException(JsonRpcError.badRequest(), jsonRpcRequest);
        }
    }
}
