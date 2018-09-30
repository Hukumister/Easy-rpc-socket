package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;

/**
 * Created by Nikit on 25.08.2018.
 */
public class RequestMessage extends GenericMessage<JsonRpcRequest> {

    private final JsonRpcRequest request;

    /**
     * @param request
     * @param messageHeaders
     */
    public RequestMessage(JsonRpcRequest request, MessageHeaders messageHeaders) {
        super(request, messageHeaders);
        this.request = request;
    }

    public JsonRpcRequest getRequest() {
        return request;
    }
}
