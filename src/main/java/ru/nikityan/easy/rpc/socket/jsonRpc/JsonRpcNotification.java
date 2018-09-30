package ru.nikityan.easy.rpc.socket.jsonRpc;

public class JsonRpcNotification {
    /**
     * Method name
     */
    private final String method;
    /**
     * Dto object for notification
     */
    private final Object params;


    /**
     * Create instance of JsonRpcNotification
     *
     * @param method method name
     * @param params dto for send notification
     */
    public JsonRpcNotification(String method, Object params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public Object getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "JsonRpcNotification{" +
                "method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
