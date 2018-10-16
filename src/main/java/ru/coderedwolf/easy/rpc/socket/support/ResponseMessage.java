package ru.coderedwolf.easy.rpc.socket.support;

import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcResponse;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ResponseMessage extends GenericMessage<JsonRpcResponse> {

    private final JsonRpcResponse response;

    /**
     * Create Response message from response {@link JsonRpcResponse}
     * and message headers {@link MessageHeaders}.
     *
     * @param response       given response
     * @param messageHeaders message headers.
     */
    public ResponseMessage(JsonRpcResponse response, MessageHeaders messageHeaders) {
        super(response, messageHeaders);
        this.response = response;
    }

    /**
     * Gets a response.
     */
    public JsonRpcResponse getResponse() {
        return response;
    }
}
