package ru.coderedwolf.easy.rpc.socket;

/**
 * Message chanel for register {@link MessageHandler}. Invoke message handler when send message
 * this chanel.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public interface SubscribeMessageChanel extends MessageChannel {

    /**
     * Subscribe message handler.
     *
     * @return true id success subscribe, false if already subscribe
     */
    boolean subscribe(MessageHandler messageHandler);

    /**
     * Unsubscribe message handler.
     *
     * @return true is success unsubscribe, false if message handler not exist.
     */
    boolean unSubscribe(MessageHandler messageHandler);
}
