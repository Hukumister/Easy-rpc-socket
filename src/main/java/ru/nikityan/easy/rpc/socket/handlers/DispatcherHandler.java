package ru.nikityan.easy.rpc.socket.handlers;

/**
 * This interface is responsible for registering the method that will be called
 * at the time of the client's request and at the closing time of the session
 */
public interface DispatcherHandler {

    /**
     * This method registers all methods that process client requests
     *
     * @param method   registered method
     * @param classOfT class of arguments of the registered method
     * @param handler  handler for this method
     * @param <T>      type of argument
     * @see JsonRequestHandler
     */
    <T> void onMessage(String method, Class<T> classOfT, JsonRequestHandler<T> handler);

    /**
     * This method registers all methods that listen for the webSocketSession termination
     *
     * @param lifeCircleWebSocket registered listener closing the WebSocketSession
     */
    void onClose(LifeCircleWebSocket lifeCircleWebSocket);
}
