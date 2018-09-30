package ru.nikityan.easy.rpc.socket.handlers;

import org.springframework.web.socket.WebSocketSession;
import ru.nikityan.easy.rpc.socket.exceptions.JsonRequestException;

/**
 * An interface that contains a method that is called each time
 * a request comes from a client on the WebSocketSession
 *
 * @param <T> type of arguments method
 */
@FunctionalInterface
public interface JsonRequestHandler<T> {

    /**
     * @param arg       The argument that will be invoked in the request and submitted to the input.
     *                  Arguments are converted to the type that will be specified in the implementation
     * @param webSocket web socket session
     * @return object that will be recovered after processing, in this implementation it is serialized to json
     * @throws JsonRequestException this exception is converted to the appropriate json and sent to the client
     */
    Object handle(T arg, WebSocketSession webSocket);
}
