package ru.nikityan.easy.rpc.socket.exceptions;


import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;

/**
 * Exception used in jsonrpc protocol.
 * Use for response when subscribe
 */
public class JsonResponseException extends Exception {

    /**
     * JsonRpcError when get in json rpc response
     *
     * @see JsonRpcError
     */
    private final JsonRpcError jsonRpcError;

    public JsonResponseException(JsonRpcError jsonRpcError) {
        super(String.format("message  = %s, code = %s", jsonRpcError.getMessage(), jsonRpcError.getCode()));
        this.jsonRpcError = jsonRpcError;
    }

    public JsonResponseException(String message, Throwable cause, JsonRpcError jsonRpcError) {
        super(String.format("message  = %s, code = %s", jsonRpcError.getMessage(), jsonRpcError.getCode()), cause);
        this.jsonRpcError = jsonRpcError;
    }

    /**
     * Get JsonRpcError of JsonResponseException
     *
     * @return JsonRpcError
     */
    public JsonRpcError getJsonRpcError() {
        return jsonRpcError;
    }
}
