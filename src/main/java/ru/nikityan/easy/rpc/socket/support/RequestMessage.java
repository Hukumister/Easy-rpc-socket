package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class RequestMessage extends GenericMessage<JsonRpcRequest> {

    private final JsonRpcRequest request;

    /**
     * Create Request message from request {@link JsonRpcRequest}
     * and message headers {@link MessageHeaders}.
     *
     * @param request        given request object.
     * @param messageHeaders given message headers.
     */
    public RequestMessage(JsonRpcRequest request, MessageHeaders messageHeaders) {
        super(request, messageHeaders);
        this.request = request;
    }

    /**
     * Gets a request.
     */
    public JsonRpcRequest getRequest() {
        return request;
    }
}
