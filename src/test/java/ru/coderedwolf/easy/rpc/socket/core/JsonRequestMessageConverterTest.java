package ru.coderedwolf.easy.rpc.socket.core;

import com.google.gson.*;
import org.junit.Test;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRequestMessageConverterTest {

    private JsonRequestMessageConverter messageConverter = new JsonRequestMessageConverter();

    @Test
    public void fromMessage() throws Exception {
        JsonPrimitive jsonPrimitive = new JsonPrimitive(34);
        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonPrimitive);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        Object message = messageConverter.fromMessage(requestMessage, int.class);

        assertEquals(message, 34);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fromMessageMap() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "foo");

        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement = gson.toJsonTree(map);
        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonElement);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        Object message = messageConverter.fromMessage(requestMessage, Map.class);
        Map<String, String> stringMap = (Map<String, String>) message;

        assertEquals(stringMap.get("foo"), "bar");
        assertEquals(stringMap.get("bar"), "foo");
    }

    @Test
    public void fromMessageCollection() throws Exception {
        JsonArray jsonElement = new JsonArray();
        jsonElement.add(1.0);
        jsonElement.add(2.0);
        jsonElement.add(3.0);
        jsonElement.add(4.0);

        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonElement);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        Object message = messageConverter.fromMessage(requestMessage, List.class);

        assertEquals(message, Arrays.asList(1.0, 2.0, 3.0, 4.0));
    }

    @Test
    public void toMessageResponse() throws Exception {
        Answer answer = new Answer("foo", 2);
        Message<?> message = messageConverter.toMessage(answer);

        Object payload = message.getPayload();
        JsonRpcResponse response = (JsonRpcResponse) payload;

        assertEquals(response.getResult(), answer);
    }

    @Test
    public void toMessageError() throws Exception {
        JsonRpcError error = JsonRpcError.parseError();
        Message<?> message = messageConverter.toMessage(error);

        JsonRpcResponse payload = (JsonRpcResponse) message.getPayload();

        assertEquals(payload.getError().getCode(), error.getCode());
    }

    @Test
    public void fromMessageReturnNullIfNotRequest() throws Exception {
        Message<?> requestMessage = MessageBuilder.fromPayload("string").build();

        Object fromMessage1 = messageConverter.fromMessage(requestMessage, String.class);
        Object fromMessage2 = messageConverter.fromMessage(requestMessage, String.class, "x");

        assertNull(fromMessage1);
        assertNull(fromMessage2);
    }

    @Test
    public void fromMessageWithParamName() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", 12);

        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonObject);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        Object fromMessage = messageConverter.fromMessage(requestMessage, int.class, "x");

        assertEquals(fromMessage, 12);
    }

    @Test(expected = IllegalStateException.class)
    public void fromMessageWithParamNameNotJsonObjectInput() throws Exception {
        JsonPrimitive jsonPrimitive = new JsonPrimitive(34);
        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonPrimitive);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        messageConverter.fromMessage(requestMessage, int.class, "x");
    }

    private static class Answer {
        private String name;
        private long id;

        public Answer(String name, long id) {
            this.name = name;
            this.id = id;
        }

    }
}