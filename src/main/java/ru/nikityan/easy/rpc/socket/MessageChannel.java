package ru.nikityan.easy.rpc.socket;


/**
 * Interface contains for send message {@link Message}.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
@FunctionalInterface
public interface MessageChannel {

    /**
     * @param message message which must be sent
     * @return true if success send message, false if fail send.
     */
    boolean send(Message<?> message);
}
