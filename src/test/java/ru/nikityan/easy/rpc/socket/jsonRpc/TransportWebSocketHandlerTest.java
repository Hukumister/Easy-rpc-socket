package ru.nikityan.easy.rpc.socket.jsonRpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.SubscribeMessageChanel;
import ru.nikityan.easy.rpc.socket.TestWebSocketSession;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * Created by Nikit on 02.10.2018.
 */
public class TransportWebSocketHandlerTest {

    private final Gson gson = new GsonBuilder().create();

    @Mock
    private MessageChannel outChanel;

    @Mock
    private SubscribeMessageChanel inChanel;

    @InjectMocks
    private TransportWebSocketHandler handler;

    private TestWebSocketSession socketSession = new TestWebSocketSession();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        socketSession.setId("123");
        socketSession.setOpen(true);

        handler.afterConnectionEstablished(socketSession);
    }

    @Test
    public void handleNotificationNotSubscribe() throws Exception {
        Message<JsonRpcNotification> build = notification(null, "method");
        handler.handleMessage(build);

        assertTrue(socketSession.getSentMessages().size() == 0);
    }

    @Test
    public void handleNotificationSubscribe() throws Exception {
        Message<JsonRpcNotification> notification = notification("method", null);
        handler.handleMessage(notification);

        Message<JsonRpcNotification> notificationMessage = notification(null, "method");
        handler.handleMessage(notificationMessage);

        assertEquals(socketSession.getSentMessages().size(), 2);
    }

    @Test
    public void handleMethodResult() throws Exception {
        Message<JsonRpcNotification> notification = notification(null, null);
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

    private Message<JsonRpcNotification> notification(String subscribe, String sendMethod) {
        JsonRpcNotification notification = new JsonRpcNotification("method", null);
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(null);
        accessor.setSendMessageMethod(sendMethod);
        accessor.setMessageMethod("method");
        accessor.setSessionId("123");
        if (subscribe != null) {
            accessor.setSubscribeName(subscribe);
        }

        return MessageBuilder.fromPayload(notification)
                .withHeaders(accessor.getMessageHeaders())
                .build();
    }
}