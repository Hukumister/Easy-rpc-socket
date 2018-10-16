package ru.coderedwolf.easy.rpc.socket.jsonRpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageChannel;
import ru.coderedwolf.easy.rpc.socket.SubscribeMessageChanel;
import ru.coderedwolf.easy.rpc.socket.TestWebSocketSession;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class TransportWebSocketHandlerTest {

    private static final Gson gson = new GsonBuilder().create();

    @Mock
    private MessageChannel outChanel;

    @Mock
    private SubscribeMessageChanel inChanel;

    private TransportWebSocketHandler handler;

    private TestWebSocketSession socketSession = new TestWebSocketSession();
    private TestWebSocketSession socketSubSession = new TestWebSocketSession();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        handler = new TransportWebSocketHandler(inChanel, outChanel);
        socketSession.setId("123");
        socketSession.setOpen(true);

        socketSubSession.setId("456");
        socketSubSession.setOpen(true);

        handler.afterConnectionEstablished(socketSession);
        handler.afterConnectionEstablished(socketSubSession);
    }

    @Test
    public void validateJsonOnInput() throws Exception {
        String noParse = "{\"method\": \"subscribe\", \"params\": {\"name:456}, \"id\": 123}";
        TextMessage message = new TextMessage(noParse);

        handler.handleMessage(socketSession, message);

        WebSocketMessage<?> socketMessage = socketSession.getSentMessages().get(0);
        String payload = (String) socketMessage.getPayload();
        JsonRpcResponse response = gson.fromJson(payload, JsonRpcResponse.class);

        assertNotNull(response.getError());
    }

    @Test
    public void handleMessageWithoutSubscribe() throws Exception {
        Message<JsonRpcNotification> build = notification(null, "method", "123");
        handler.handleMessage(build);

        assertEquals(socketSession.getSentMessages().size(), 1);
    }

    @Test
    public void handleNotificationSubscribe() throws Exception {
        Message<JsonRpcNotification> notification = notification("method", null, "123");
        handler.handleMessage(notification);

        Message<JsonRpcNotification> notificationMessage = notification(null, "method", "123");
        handler.handleMessage(notificationMessage);

        assertEquals(socketSession.getSentMessages().size(), 2);
    }

    @Test
    public void handleMethodResult() throws Exception {
        Message<JsonRpcNotification> notification = notification(null, null, "123");
        handler.handleMessage(notification);

        assertEquals(socketSession.getSentMessages().size(), 1);
    }

    @Test
    public void handleNotParse() throws Exception {
        String noParse = "{ ()foo}";
        TextMessage message = new TextMessage(noParse);

        handler.handleMessage(socketSession, message);

        WebSocketMessage<?> socketMessage = socketSession.getSentMessages().get(0);
        String payload = (String) socketMessage.getPayload();
        JsonRpcResponse response = gson.fromJson(payload, JsonRpcResponse.class);

        assertNotNull(response.getError());
    }

    @Test
    public void handleBadRequest() throws Exception {
        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, null, null);
        String json = gson.toJson(rpcRequest);
        TextMessage message = new TextMessage(json);

        handler.handleMessage(socketSession, message);

        WebSocketMessage<?> socketMessage = socketSession.getSentMessages().get(0);
        String payload = (String) socketMessage.getPayload();
        JsonRpcResponse response = gson.fromJson(payload, JsonRpcResponse.class);

        assertNotNull(response.getError());
    }

    @Test
    public void correctRequest() throws Exception {
        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", null);
        String json = gson.toJson(rpcRequest);
        TextMessage message = new TextMessage(json);

        ArgumentCaptor<Message> arg = ArgumentCaptor.forClass(Message.class);

        handler.handleMessage(socketSession, message);
        verify(outChanel).send(arg.capture());

        Message<?> value = arg.getValue();
        JsonRpcRequest jsonRpcRequest = (JsonRpcRequest) value.getPayload();

        assertEquals(jsonRpcRequest.getMethod(), "method");
        assertEquals((long) value.getMessageHeader().getId(), 1L);
    }

    @Test
    public void convertToResponseIfMessagePayloadNotResponse() throws Exception {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(null);
        accessor.setMessageMethod("method");
        accessor.setSessionId("123");
        Message<String> stringMessage = MessageBuilder.fromPayload("string")
                .withHeaders(accessor.getMessageHeaders())
                .build();
        handler.handleMessage(stringMessage);

        WebSocketMessage<?> socketMessage = socketSession.getSentMessages().get(0);
        String payload = (String) socketMessage.getPayload();
        JsonRpcResponse response = gson.fromJson(payload, JsonRpcResponse.class);

        assertEquals(response.getResult(), "string");
    }

    @Test
    public void notSendByBroadcastIfNotSubscribe() throws Exception {
        Message<JsonRpcNotification> notification = notification("subscribe", "method", "123");
        handler.handleMessage(notification);

        Message<JsonRpcNotification> notificationBroadcast
                = notification(null, "subscribe", null);
        handler.handleMessage(notificationBroadcast);

        assertEquals(socketSession.getSentMessages().size(), 2);
        assertEquals(socketSubSession.getSentMessages().size(), 0);
    }

    @Test
    public void removeFromSubscribersAfterCloseConnection() throws Exception {
        Message<JsonRpcNotification> notification1
                = notification("subscribe", "subscribe", "123");
        Message<JsonRpcNotification> notification2
                = notification("subscribe", "subscribe", "456");

        handler.handleMessage(notification1);
        handler.handleMessage(notification2);

        handler.afterConnectionClosed(socketSession, CloseStatus.BAD_DATA);

        Message<JsonRpcNotification> notification = notification(null, "subscribe", null);
        handler.handleMessage(notification);

        assertEquals(socketSession.getSentMessages().size(), 1);
        assertEquals(socketSubSession.getSentMessages().size(), 2);
    }

    @Test
    public void notConvertMessageToResponseUseBroadcast() throws Exception {
        Message<JsonRpcNotification> subscribe
                = notification("method", "method", "123");
        handler.handleMessage(subscribe);

        Message<JsonRpcNotification> notification
                = notification(null, "method", null);
        handler.handleMessage(notification);

        WebSocketMessage<?> webSocketMessage = socketSession.getSentMessages().get(1);
        String payload = (String) webSocketMessage.getPayload();

        assertFalse(payload.contains("\"id\":-1"));
        JsonRpcNotification result = gson.fromJson(payload, JsonRpcNotification.class);

        assertEquals(result.getMethod(), "method");
    }


    @Test
    public void ifRequestMessageMethodContainInSubscribersUnsubscribe() throws Exception {
        Message<JsonRpcNotification> subscribe
                = notification("method", "method", "123");
        handler.handleMessage(subscribe);

        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", null);
        String json = gson.toJson(rpcRequest);
        TextMessage message = new TextMessage(json);

        handler.handleMessage(socketSession, message);
        handler.handleMessage(socketSession, message);
        verify(outChanel).send(any());

        WebSocketMessage<?> webSocketMessage = socketSession.getSentMessages().get(1);
        String payload = (String) webSocketMessage.getPayload();
        JsonRpcResponse response = gson.fromJson(payload, JsonRpcResponse.class);

        assertNull(response.getError());
        assertNull(response.getResult());
    }

    private Message<JsonRpcNotification> notification(String subscribe, String sendMethod, String sessionId) {
        JsonRpcNotification notification = new JsonRpcNotification("method", null);
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(null);
        accessor.setSendMessageMethod(sendMethod);
        accessor.setMessageMethod("method");
        accessor.setSessionId(sessionId);
        if (subscribe != null) {
            accessor.setSubscribeName(subscribe);
        }

        return MessageBuilder.fromPayload(notification)
                .withHeaders(accessor.getMessageHeaders())
                .build();
    }
}