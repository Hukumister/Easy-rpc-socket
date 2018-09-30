package ru.nikityan.easy.rpc.socket;

/**
 * Created by Nikit on 25.08.2018.
 */
public interface SubscribeMessageChanel extends MessageChannel {

    boolean subscribe(MessageHandler messageHandler);

    boolean unSubscribe(MessageHandler messageHandler);
}
