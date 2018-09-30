package ru.nikityan.easy.rpc.socket;

/**
 * @param <T>
 */
public interface Message<T> {

    /**
     * @return
     */
    T getPayload();

    /**
     * @return
     */
    MessageHeaders getMessageHeader();
}
