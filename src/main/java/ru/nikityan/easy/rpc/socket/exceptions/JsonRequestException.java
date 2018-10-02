package ru.nikityan.easy.rpc.socket.exceptions;

import org.jetbrains.annotations.Nullable;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;

/**
 * Exception used in jsonrpc protocol.
 */
public class JsonRequestException extends RuntimeException {
    /**
     * Code use like in HTTP code error
     */
    private final JsonRpcError rpcError;

    private final JsonRpcRequest originalRequest;

    public JsonRequestException(JsonRpcError rpcError, JsonRpcRequest originalRequest) {
        super();
        this.rpcError = rpcError;
        this.originalRequest = originalRequest;
    }

    public JsonRequestException(Throwable cause, JsonRpcError rpcError, JsonRpcRequest originalRequest) {
        super(cause);
        this.rpcError = rpcError;
        this.originalRequest = originalRequest;
    }

    public JsonRpcError getRpcError() {
        return rpcError;
    }

    @Nullable
    public JsonRpcRequest getOriginalRequest() {
        return originalRequest;
    }
}
