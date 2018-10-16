package ru.coderedwolf.easy.rpc.socket.support;

import ru.coderedwolf.easy.rpc.socket.MessageHandler;
import ru.coderedwolf.easy.rpc.socket.SubscribeMessageChanel;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nikit on 25.08.2018.
 */
public abstract class AbstractSubscribeChannel extends AbstractMessageChannel implements SubscribeMessageChanel {

    private final Set<MessageHandler> handlers = new CopyOnWriteArraySet<>();

    public Set<MessageHandler> getSubscribers() {
        return Collections.unmodifiableSet(handlers);
    }

    @Override
    public boolean subscribe(MessageHandler messageHandler) {
        boolean result = this.handlers.add(messageHandler);
        if (result) {
            logger.debug("add  subscriber {}", messageHandler);
        }
        return result;
    }

    @Override
    public boolean unSubscribe(MessageHandler messageHandler) {
        boolean result = this.handlers.remove(messageHandler);
        if (result) {
            logger.debug("remove subscriber {}", messageHandler);
        }
        return result;
    }
}
