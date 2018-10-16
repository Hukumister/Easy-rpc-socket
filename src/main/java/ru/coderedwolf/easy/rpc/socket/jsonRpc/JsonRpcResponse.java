package ru.coderedwolf.easy.rpc.socket.jsonRpc;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRpcResponse {
    /**
     * Identificator of message
     */
    private final long id;
    /**
     * Dto field for send result to subscriber
     */
    private final Object result;
    /**
     * Dto field error for send result to subscriber
     */
    private final JsonRpcError error;

    /**
     * Create instance from id and object dto result
     *
     * @param id     identificator of subscribe
     * @param result dto for send result to subscriber
     */
    public JsonRpcResponse(long id, Object result) {
        this.id = id;
        this.result = result;
        this.error = null;
    }

    /**
     * Create instance from id and object dto result
     *
     * @param id    identificator of subscribe
     * @param error dto error for send result to subscriber
     */
    public JsonRpcResponse(long id, JsonRpcError error) {
        this.id = id;
        this.error = error;
        this.result = null;
    }

    public long getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public JsonRpcError getError() {
        return error;
    }

    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "id=" + id +
                ", result=" + result +
                ", error=" + error +
                '}';
    }
}
