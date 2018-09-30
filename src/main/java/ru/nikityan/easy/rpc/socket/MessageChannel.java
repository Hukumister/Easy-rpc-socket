package ru.nikityan.easy.rpc.socket;


@FunctionalInterface
public interface MessageChannel {

    /**
     * @param message
     * @return
     */
    boolean send(Message<?> message);
}
