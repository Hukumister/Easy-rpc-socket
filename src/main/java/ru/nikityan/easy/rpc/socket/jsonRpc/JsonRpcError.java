package ru.nikityan.easy.rpc.socket.jsonRpc;

import org.jetbrains.annotations.NotNull;

public class JsonRpcError {
    /**
     * Json rpc code error
     */
    private final int code;
    /**
     * Message error
     */
    private final String message;

    /**
     * @return
     */
    @NotNull
    public static JsonRpcError parseError() {
        return new JsonRpcError(-32700, "Parse error");
    }

    /**
     * @return
     */
    @NotNull
    public static JsonRpcError badRequest() {
        return new JsonRpcError(-32600, "Invalid Request");
    }

    /**
     * @return
     */
    @NotNull
    public static JsonRpcError methodNotFound() {
        return new JsonRpcError(-32601, "Method not found");
    }

    @NotNull
    public static JsonRpcError internalError() {
        return new JsonRpcError(-32000, "Internal server error");
    }

    /**
     * @param code    json rpc code error
     * @param message error message for jsonRpcError
     */
    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return error code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return message error
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "JsonRpcError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRpcError that = (JsonRpcError) o;

        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code;
    }
}
