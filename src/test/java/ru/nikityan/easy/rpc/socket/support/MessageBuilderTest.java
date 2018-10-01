package ru.nikityan.easy.rpc.socket.support;

import org.junit.Test;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcNotification;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nikit on 25.08.2018.
 */
public class MessageBuilderTest {

    @Test
    public void errorMessageCreate() {
        JsonRpcError jsonRpcError = JsonRpcError.badRequest();
        JsonRpcResponse rpcResponse = new JsonRpcResponse(34L, jsonRpcError);
        Message<JsonRpcResponse> responseMessage = MessageBuilder
                .fromPayload(rpcResponse)
                .build();

        assertEquals(responseMessage.getPayload().getError(), JsonRpcError.badRequest());
    }

    @Test
    public void notificationMessage() {
        JsonRpcNotification notification = new JsonRpcNotification("method", "param");
        NotificationMessage message = (NotificationMessage) MessageBuilder
                .fromPayload(notification)
                .build();

        assertEquals((long) message.getMessageHeader().getId(), -1);
        assertEquals(message.getNotification().getMethod(), notification.getMethod());
    }

    @Test
    public void buildFromMessage() throws Exception {
        JsonRpcNotification notification = new JsonRpcNotification("method", "param");
        Message<?> message = MessageBuilder
                .fromPayload(notification)
                .build();

        Message fromMessage = MessageBuilder.fromMessage(message).build();
        JsonRpcNotification from = (JsonRpcNotification) fromMessage.getPayload();
        JsonRpcNotification to = (JsonRpcNotification) message.getPayload();

        assertEquals(from.getParams(), to.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExIfFromMessageNull() {
        MessageBuilder.fromMessage(null).build();
    }


    @Test
    public void buildFromRequestSetHeader() throws Exception {
        JsonRpcRequest rpcRequest = new JsonRpcRequest(45L, "method", null);
        Message<JsonRpcRequest> build = MessageBuilder
                .fromPayload(rpcRequest)
                .build();

        assertEquals(build.getMessageHeader().get(MessageHeaderAccessor.MESSAGE_METHOD), "method");
        assertEquals((long) build.getMessageHeader().getId(), 45L);
    }

    @Test
    public void fromMessageWithHeaders() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");

        Message<String> build = MessageBuilder.fromPayload("message").build();
        Message<String> message = MessageBuilder.fromMessage(build).withHeaders(map).build();

        assertEquals(message.getMessageHeader().get("foo"), "bar");
    }
}