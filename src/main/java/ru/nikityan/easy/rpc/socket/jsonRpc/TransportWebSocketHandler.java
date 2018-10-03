package ru.nikityan.easy.rpc.socket.jsonRpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import ru.nikityan.easy.rpc.socket.*;
import ru.nikityan.easy.rpc.socket.core.JsonRpcIncomingConverter;
import ru.nikityan.easy.rpc.socket.core.MessageConverter;
import ru.nikityan.easy.rpc.socket.exceptions.JsonRequestException;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nikit on 02.10.2018.
 */
public class TransportWebSocketHandler extends AbstractWebSocketHandler implements MessageHandler, SmartLifecycle {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final Map<String, Set<WebSocketSession>> subscribers = new ConcurrentHashMap<>();

    private final MessageConverter messageConverter = new JsonRpcIncomingConverter();

    private int sendTimeLimit = 10 * 1000;

    private int sendBufferSizeLimit = 512 * 1024;

    private volatile boolean running = false;

    private final Object lifecycleMonitor = new Object();

    private final SubscribeMessageChanel clientInboundChannel;

    private final MessageChannel clientOutboundChannel;

    public TransportWebSocketHandler(SubscribeMessageChanel clientInboundChannel, MessageChannel clientOutboundChannel) {
        Assert.notNull(clientInboundChannel, "Inbound MessageChannel must not be null");
        Assert.notNull(clientInboundChannel, "Outbound MessageChannel must not be null");

        this.clientOutboundChannel = clientOutboundChannel;
        this.clientInboundChannel = clientInboundChannel;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        synchronized (this.lifecycleMonitor) {
            stop();
            if (callback != null) {
                callback.run();
            }
        }
    }

    @Override
    public void start() {
        synchronized (this.lifecycleMonitor) {
            this.clientInboundChannel.subscribe(this);
            this.running = true;
        }
    }

    @Override
    public void stop() {
        synchronized (this.lifecycleMonitor) {
            this.running = false;
            this.clientInboundChannel.unSubscribe(this);
        }

        for (WebSocketSession socketSession : this.sessions.values()) {
            try {
                socketSession.close(CloseStatus.GOING_AWAY);
            } catch (Throwable ex) {
                logger.warn("Failed to close {}", socketSession, ex);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!session.isOpen()) {
            return;
        }
        WebSocketSession webSocketSession = decorateSession(session);
        this.sessions.put(webSocketSession.getId(), webSocketSession);
        logger.debug("Open connection webSocket session, sessionId = {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Message<?> sendMessage = messageConverter.toMessage(message.getPayload());
            MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(sendMessage);
            accessor.setSessionId(session.getId());
            sendMessage = MessageBuilder.fromMessage(sendMessage)
                    .withHeaders(accessor.getMessageHeaders())
                    .build();
            boolean result = this.clientOutboundChannel.send(sendMessage);
            if (!result) {
                logger.warn("Fail sent message to message chanel message = {}", message);
            }
        } catch (JsonRequestException ex) {
            handleFailRequest(session, ex);
        }
    }

    private void handleFailRequest(WebSocketSession session, JsonRequestException exception) {
        JsonRpcRequest request = exception.getOriginalRequest();
        JsonRpcResponse rpcResponse;
        if (request != null) {
            rpcResponse = new JsonRpcResponse(request.getId(), exception.getRpcError());
        } else {
            rpcResponse = new JsonRpcResponse(-1L, exception.getRpcError());
        }
        Message<JsonRpcResponse> message = MessageBuilder.fromPayload(rpcResponse).build();
        sendMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.debug("Close connection, webSocket sessionId = {}, close status = {}", session.getId(), closeStatus);
        this.sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String sessionId = MessageHeaderAccessor.getSessionId(message.getMessageHeader());
        WebSocketSession webSocketSession = sessions.get(sessionId);

        if (webSocketSession == null) {
            return;
        }

        String subscribeMethod = MessageHeaderAccessor.getSubscribeMethod(message.getMessageHeader());
        String sendMessageMethod = MessageHeaderAccessor.getSendMessageMethod(message.getMessageHeader());

        if (subscribeMethod != null) {
            if (!subscribers.containsKey(subscribeMethod)) {
                subscribers.put(subscribeMethod, new CopyOnWriteArraySet<>());
            }
            subscribers.get(subscribeMethod).add(webSocketSession);
            sendMessage(webSocketSession, message);
            return;
        }

        if (sendMessageMethod != null) {
            broadCastMessage(sendMessageMethod, message);
        } else {
            sendMessage(webSocketSession, message);
        }
    }

    private void sendMessage(WebSocketSession socketSession, Message<?> message) {
        JsonRpcResponse rpcResponse;
        Object payload = message.getPayload();
        if (payload instanceof JsonRpcResponse) {
            rpcResponse = (JsonRpcResponse) payload;
        } else {
            MessageHeaders messageHeader = message.getMessageHeader();
            rpcResponse = new JsonRpcResponse(messageHeader.getId(), payload);
        }
        String json = gson.toJson(rpcResponse);
        try {
            TextMessage textMessage = new TextMessage(json);
            socketSession.sendMessage(textMessage);
        } catch (IOException ex) {
            logger.warn("Error while try to send text message session = {}, message = {}", socketSession, message);
        }
    }

    private void broadCastMessage(String method, Message<?> message) {
        Set<WebSocketSession> sessions = subscribers.get(method);
        if (sessions == null) {
            logger.debug("Session set is null");
            return;
        }
        for (WebSocketSession socketSession : sessions) {
            sendMessage(socketSession, message);
        }
    }

    protected WebSocketSession decorateSession(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, getSendTimeLimit(), getSendBufferSizeLimit());
    }

    public int getSendTimeLimit() {
        return sendTimeLimit;
    }

    public void setSendTimeLimit(int sendTimeLimit) {
        this.sendTimeLimit = sendTimeLimit;
    }

    public int getSendBufferSizeLimit() {
        return sendBufferSizeLimit;
    }

    public void setSendBufferSizeLimit(int sendBufferSizeLimit) {
        this.sendBufferSizeLimit = sendBufferSizeLimit;
    }
}
