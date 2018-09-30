package ru.nikityan.easy.rpc.socket.handlers;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Interface that is used as a listener for closing a WebSocket,
 * each time the WebSocket is closed, the method of this interface is called
 *
 * @see WebSocketSession
 * @see CloseStatus
 */
public interface LifeCircleWebSocket {

    /**
     * The method is called every time the WebSocket is closed
     *
     * @param session session of a closed WebSocketSession
     * @param status  close status of a closed WebSocketSession
     */
    void doOnClose(WebSocketSession session, CloseStatus status);
}
