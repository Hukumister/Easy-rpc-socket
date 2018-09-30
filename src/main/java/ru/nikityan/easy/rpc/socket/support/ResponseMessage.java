package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;

/**
 * Created by Nikit on 25.08.2018.
 */
public class ResponseMessage extends GenericMessage<JsonRpcResponse> {

    private final JsonRpcResponse response;

    /**
     * @param response
     * @param messageHeaders
     */
    public ResponseMessage(JsonRpcResponse response, MessageHeaders messageHeaders) {
        super(response, messageHeaders);
        this.response = response;
    }

    public JsonRpcResponse getResponse() {
        return response;
    }
}
