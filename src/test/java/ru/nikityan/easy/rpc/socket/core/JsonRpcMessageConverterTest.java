package ru.nikityan.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcNotification;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nikit on 01.10.2018.
 */
public class JsonRpcMessageConverterTest {

    private final Gson gson = new GsonBuilder().create();

    private JsonRpcMessageConverter converter = new JsonRpcMessageConverter();

    @Test
    public void fromMessage() throws Exception {
        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", new JsonPrimitive("foo"));
        String json = gson.toJson(rpcRequest);
        Message<String> build = MessageBuilder.fromPayload(json).build();

        Object fromMessage = converter.fromMessage(build, JsonRpcRequest.class);

        JsonRpcRequest request = (JsonRpcRequest) fromMessage;

        assertEquals(request.getId(), 1L);
        assertEquals(request.getMethod(), "method");
    }

    @Test
    public void toMessageResponse() throws Exception {
        String result = "result";

        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(null);
        accessor.setMessageMethod("message");
        accessor.setMessageType(MessageType.RESPONSE);

        Message<?> message = converter.toMessage(result, accessor.getMessageHeaders());
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) message.getPayload();

        assertEquals(jsonRpcResponse.getResult(), "result");
    }

    @Test
    public void toMessageNotification() throws Exception {
        String result = "result";

        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(null);
        accessor.setMessageMethod("message");
        accessor.setMessageType(MessageType.NOTIFICATION);

        Message<?> message = converter.toMessage(result, accessor.getMessageHeaders());
        JsonRpcNotification notification = (JsonRpcNotification) message.getPayload();

        assertEquals(notification.getParams(), "result");
    }

    @Test
    public void withoutHeader() throws Exception {
        String result = "result";

        Message<?> message = converter.toMessage(result, null);

        assertEquals(message.getPayload(), "result");
    }
}