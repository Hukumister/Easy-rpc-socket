package ru.coderedwolf.easy.rpc.socket.support;

import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcNotification;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class NotificationMessage extends GenericMessage<JsonRpcNotification> {

    private final JsonRpcNotification notification;

    /**
     * Create notification message from notification {@link NotificationMessage}
     * and message headers {@link MessageHeaders}.
     *
     * @param notification   given notification object.
     * @param messageHeaders given message headers.
     */
    public NotificationMessage(JsonRpcNotification notification, MessageHeaders messageHeaders) {
        super(notification, messageHeaders);
        this.notification = notification;
    }

    /**
     * Gets a notification.
     */
    public JsonRpcNotification getNotification() {
        return notification;
    }
}
