package ru.nikityan.easy.rpc.socket.core;

import ru.nikityan.easy.rpc.socket.Message;

/**
 * Created by Nikit on 24.08.2018.
 */
public interface MessagingPostProcessor {

    Message<?> postProcessMessage(Message<?> message);
}
