package ru.coderedwolf.easy.rpc.socket.jsonRpc;

import com.google.gson.JsonElement;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRpcRequest {
    /**
     * Identificator of message
     */
    private final long id;
    /**
     * Subscribe name
     */
    private final String method;
    /**
     * Arguments for method
     */
    private final JsonElement params;

    /**
     * Create instance from identificator, method name and arguments for method
     *
     * @param id     identificator of subscribe
     * @param method method(subscribe) name
     * @param params arguments for method
     */
    public JsonRpcRequest(long id, String method, JsonElement params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public long getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public JsonElement getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
