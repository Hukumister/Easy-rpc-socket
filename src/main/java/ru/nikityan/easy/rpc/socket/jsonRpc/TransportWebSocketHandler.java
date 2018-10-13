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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class TransportWebSocketHandler extends AbstractWebSocketHandler implements MessageHandler, SmartLifecycle {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> subscribers = new ConcurrentHashMap<>();

    private final MessageConverter messageConverter = new JsonRpcIncomingConverter();

    private int sendTimeLimit = 10 * 1000;

    private int sendBufferSizeLimit = 512 * 1024;

    private volatile boolean running = false;

    private final Object lifecycleMonitor = new Object();

    private final SubscribeMessageChanel clientInboundChannel;

    private final MessageChannel clientOutboundChannel;

    /**
     * @param clientInboundChannel
     * @param clientOutboundChannel
     */
    public TransportWebSocketHandler(SubscribeMessageChanel clientInboundChannel, MessageChannel clientOutboundChannel) {
        Assert.notNull(clientInboundChannel, "Inbound MessageChannel must not be null");
        Assert.notNull(clientOutboundChannel, "Outbound MessageChannel must not be null");

        this.clientOutboundChannel = clientOutboundChannel;
        this.clientInboundChannel = clientInboundChannel;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
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

    /**
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Message<?> sendMessage = messageConverter.toMessage(message.getPayload());
            MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(sendMessage);
            accessor.setSessionId(session.getId());
            sendMessage = MessageBuilder.fromMessage(sendMessage)
                    .withHeaders(accessor.getMessageHeaders())
                    .build();

            boolean alreadySubscribe = verifySubscribe(session, sendMessage);
            if (alreadySubscribe) {
                unsubscribeAndSend(session, sendMessage);
                return;
            }
            boolean result = this.clientOutboundChannel.send(sendMessage);
            if (!result) {
                logger.warn("Fail sent message to message chanel message = {}", message);
            }
        } catch (JsonRequestException ex) {
            handleFailRequest(session, ex);
        }
    }

    private void unsubscribeAndSend(WebSocketSession session, Message<?> sendMessage) {
        String messageMethod = MessageHeaderAccessor.getMessageMethod(sendMessage.getMessageHeader());
        subscribers.get(messageMethod).remove(session.getId());
        JsonRpcResponse rpcResponse = new JsonRpcResponse(sendMessage.getMessageHeader().getId(), null);
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse).build();
        sendMessage(session, responseMessage);
    }

    private boolean verifySubscribe(WebSocketSession session, Message<?> sendMessage) {
        String messageMethod = MessageHeaderAccessor.getMessageMethod(sendMessage.getMessageHeader());
        return subscribers.containsKey(messageMethod) && subscribers.get(messageMethod).contains(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.debug("Close connection, webSocket sessionId = {}, close status = {}", session.getId(), closeStatus);
        this.sessions.remove(session.getId());
        this.subscribers.values().forEach(webSocketSessions -> webSocketSessions.remove(session.getId()));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String subscribeMethod = MessageHeaderAccessor.getSubscribeMethod(message.getMessageHeader());
        String sendMessageMethod = MessageHeaderAccessor.getSendMessageMethod(message.getMessageHeader());

        String sessionId = MessageHeaderAccessor.getSessionId(message.getMessageHeader());
        if (sessionId == null) {
            if (sendMessageMethod != null) {
                broadCastMessage(sendMessageMethod, message);
            }
            return;
        }

        WebSocketSession webSocketSession = sessions.get(sessionId);
        if (webSocketSession == null) {
            return;
        }

        if (subscribeMethod != null) {
            if (!subscribers.containsKey(subscribeMethod)) {
                subscribers.put(subscribeMethod, new CopyOnWriteArraySet<>());
            }
            subscribers.get(subscribeMethod).add(sessionId);
            sendMessage(webSocketSession, message);
            return;
        }

        sendMessage(webSocketSession, message);
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

    protected WebSocketSession decorateSession(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, getSendTimeLimit(), getSendBufferSizeLimit());
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

    private void broadCastMessage(String method, Message<?> message) {
        Set<String> ids = subscribers.get(method);
        if (ids == null || ids.isEmpty()) {
            logger.debug("Subscribe set is empty");
            return;
        }
        List<WebSocketSession> socketSessions = ids.stream()
                .map(sessions::get)
                .collect(Collectors.toList());
        if (socketSessions == null) {
            logger.debug("Session set is null");
            return;
        }
        for (WebSocketSession socketSession : socketSessions) {
            broadcast(socketSession, message);
        }
    }

    private void broadcast(WebSocketSession socketSession, Message<?> message) {
        JsonRpcNotification rpcNotification;
        Object payload = message.getPayload();
        if (payload instanceof JsonRpcNotification) {
            rpcNotification = (JsonRpcNotification) payload;
        } else {
            String sendMessageMethod = MessageHeaderAccessor.getSendMessageMethod(message.getMessageHeader());
            rpcNotification = new JsonRpcNotification(sendMessageMethod, payload);
        }
        String json = gson.toJson(rpcNotification);
        try {
            TextMessage textMessage = new TextMessage(json);
            socketSession.sendMessage(textMessage);
        } catch (IOException ex) {
            logger.warn("Error while try to send text message session = {}, message = {}", socketSession, message);
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
}
