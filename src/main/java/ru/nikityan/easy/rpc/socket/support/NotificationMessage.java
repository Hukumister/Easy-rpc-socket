package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcNotification;

/**
 * Created by Nikit on 17.09.2018.
 */
public class NotificationMessage extends GenericMessage<JsonRpcNotification> {

    private final JsonRpcNotification notification;

    /**
     * @param notification
     * @param messageHeaders
     */
    public NotificationMessage(JsonRpcNotification notification, MessageHeaders messageHeaders) {
        super(notification, messageHeaders);
        this.notification = notification;
    }

    /**
     * @return
     */
    public JsonRpcNotification getNotification() {
        return notification;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "notification=" + notification +
                '}';
    }
}
