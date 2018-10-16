package ru.coderedwolf.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageType;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcNotification;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

import static org.junit.Assert.assertEquals;

/**
 * @author CodeRedWolf
 * @since 1.0
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
        accessor.setMessageType(MessageType.NOTIFICATION);

        Message<?> message = converter.toMessage(result, accessor.getMessageHeaders(), "message");
        JsonRpcNotification notification = (JsonRpcNotification) message.getPayload();

        assertEquals(notification.getParams(), "result");
        assertEquals(notification.getMethod(), "message");
    }


    @Test
    public void withoutHeader() throws Exception {
        String result = "result";

        Message<?> message = converter.toMessage(result, null);

        assertEquals(message.getPayload(), "result");
    }
}