package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHandler;
import ru.nikityan.easy.rpc.socket.exceptions.MessageSendException;

import java.util.concurrent.Executor;

/**
 * Created by Nikit on 25.08.2018.
 */
public class ExecutorSubscribableChannel extends AbstractSubscribeChannel {

    private final Executor executor;

    public ExecutorSubscribableChannel() {
        this(null);
    }

    public ExecutorSubscribableChannel(Executor executor) {
        this.executor = executor;
    }

    @Override
    public boolean sendMessage(Message<?> message) {
        for (MessageHandler messageHandler : getSubscribers()) {
            SendTask sendTask = new SendTask(message, messageHandler);
            if (executor != null) {
                executor.execute(sendTask);
            } else {
                sendTask.run();
            }
        }
        return true;
    }

    private class SendTask implements Runnable {

        private final Message<?> message;

        private final MessageHandler messageHandler;

        private SendTask(Message<?> message, MessageHandler messageHandler) {
            this.message = message;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            try {
                this.messageHandler.handleMessage(message);
            } catch (Throwable throwable) {
                String description = "Failed to handle " + message + " to " + this + " in " + this.messageHandler;
                throw new MessageSendException(message, description, throwable);
            }
        }
    }
}
