package ru.nikityan.easy.rpc.socket;

/**
 * Interface describe message. Message must contain body and headers {@link MessageHeaders}
 *
 * @param <T> the body type.
 * @author CodeRedWolf.
 * @since 1.0
 */
public interface Message<T> {

    /**
     * Gets a message payload.
     */
    T getPayload();

    /**
     * Return message headers
     */
    MessageHeaders getMessageHeader();
}
