package ru.nikityan.easy.rpc.socket.handlers.impl;

import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.nikityan.easy.rpc.socket.exceptions.JsonRequestException;
import ru.nikityan.easy.rpc.socket.handlers.DispatcherHandler;
import ru.nikityan.easy.rpc.socket.handlers.JsonRequestHandler;
import ru.nikityan.easy.rpc.socket.handlers.LifeCircleWebSocket;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.nikityan.easy.rpc.socket.utils.JsonConvert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class JsonRpcDispatcherHandler extends TextWebSocketHandler implements DispatcherHandler {

    private static final Logger logger = LoggerFactory.getLogger(JsonRpcDispatcherHandler.class);

    private static final Map<String, JsonRequestHandler<JsonRpcRequest>> handlers = new ConcurrentHashMap<>();
    private static final List<LifeCircleWebSocket> webSocketList = new CopyOnWriteArrayList<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        handle(session, message);
    }

    private void handle(WebSocketSession session, TextMessage message) {
        JsonRpcRequest request = null;
        try {
            URI sessionUri = session.getUri();
            request = JsonConvert.fromJson(message.getPayload(), JsonRpcRequest.class);
            if (request.getMethod() == null) {
                throw new JsonRequestException("Method is required", HttpStatus.BAD_REQUEST);
            }

            JsonRequestHandler<JsonRpcRequest> handler = handlers.get(request.getMethod());
            if (handler == null) {
                throw new JsonRequestException(String.format("Method {%s} is unsupported", request.getMethod()),
                        HttpStatus.NOT_FOUND);
            }

            response(request, handler.handle(request, session), session);
        } catch (JsonSyntaxException ex) {
            error(new JsonRpcError(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), session);
        } catch (JsonRequestException e) {
            error(request, new JsonRpcError(e.getCode(), e.getMessage()), session);
        } catch (Throwable throwable) {
            if (request != null) {
                error(request, new JsonRpcError(HttpStatus.BAD_GATEWAY.value(), "Internal server error"), session);
            } else {
                error(new JsonRpcError(HttpStatus.BAD_GATEWAY.value(), "Internal server error"), session);
            }
            logger.error("Internal server error while handle message ", throwable);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketHolder socketSession = new WebSocketHolder(session);
        webSocketList.forEach(lifeCircleWebSocket -> lifeCircleWebSocket.doOnClose(socketSession, status));
        socketSession.shutdown();
    }

    private void error(JsonRpcRequest request, JsonRpcError error, WebSocketSession webSocket) {
        send(new JsonRpcResponse(request.getId(), error), webSocket);
    }

    private void error(JsonRpcError error, WebSocketSession webSocket) {
        send(new JsonRpcResponse(-1, error), webSocket);
    }

    private void response(JsonRpcRequest request, Object result, WebSocketSession webSocket) {
        send(new JsonRpcResponse(request.getId(), result), webSocket);
    }

    private void send(JsonRpcResponse response, WebSocketSession webSocket) {
        try {
            webSocket.sendMessage(new TextMessage(JsonConvert.toJson(response)));
        } catch (IOException e) {
            logger.error("error while send message ", e);
        }
    }

    @Override
    public <T> void onMessage(String method, Class<T> classOfT, JsonRequestHandler<T> handler) {
        if (method == null) {
            throw new IllegalArgumentException("method is required");
        }
        if (classOfT == null) {
            throw new IllegalArgumentException("class is required");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is required");
        }
        if (handlers.containsKey(method)) {
            throw new RuntimeException(String.format("Method with name \'%s\' already mapping", method));
        }
        handlers.put(method, (request, webSocket) -> {
            if (request.getParams() == null)
                return handler.handle(null, new WebSocketHolder(webSocket));
            return handler.handle(JsonConvert.fromJson(request.getParams(), classOfT), new WebSocketHolder(webSocket));
        });
        logger.info("Mapped websocket controller name = [{}] class type = [{}]", method, classOfT.toString());
    }

    @Override
    public void onClose(LifeCircleWebSocket lifeCircleWebSocket) {
        if (lifeCircleWebSocket == null) {
            throw new IllegalArgumentException("lifeCircleWebSocket implementation is required");
        }
        webSocketList.add(lifeCircleWebSocket);
    }

    /**
     * Websocket Holder - decorator for thread-safe operation with Websocket
     */
    private class WebSocketHolder implements WebSocketSession {
        private final WebSocketSession socketSession;
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();

        WebSocketHolder(WebSocketSession socketSession) {
            this.socketSession = socketSession;
        }

        @Override
        public String getId() {
            return socketSession.getId();
        }

        @Override
        public URI getUri() {
            return socketSession.getUri();
        }

        @Override
        public HttpHeaders getHandshakeHeaders() {
            return socketSession.getHandshakeHeaders();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return socketSession.getAttributes();
        }

        @Override
        public Principal getPrincipal() {
            return socketSession.getPrincipal();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return socketSession.getLocalAddress();
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return socketSession.getRemoteAddress();
        }

        @Override
        public String getAcceptedProtocol() {
            return socketSession.getAcceptedProtocol();
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
            socketSession.setTextMessageSizeLimit(messageSizeLimit);
        }

        @Override
        public int getTextMessageSizeLimit() {
            return socketSession.getTextMessageSizeLimit();
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
            socketSession.setBinaryMessageSizeLimit(messageSizeLimit);
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return socketSession.getBinaryMessageSizeLimit();
        }

        @Override
        public List<WebSocketExtension> getExtensions() {
            return socketSession.getExtensions();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) {
            executorService.submit(() -> {
                try {
                    socketSession.sendMessage(message);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        @Override
        public boolean isOpen() {
            return socketSession.isOpen();
        }

        @Override
        public void close() throws IOException {
            socketSession.close();
        }

        @Override
        public void close(CloseStatus status) throws IOException {
            socketSession.close(status);
        }

        private void shutdown() {
            executorService.shutdown();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WebSocketHolder that = (WebSocketHolder) o;
            return Objects.equals(socketSession, that.socketSession);
        }

        @Override
        public int hashCode() {
            return Objects.hash(socketSession);
        }
    }
}
