package ru.coderedwolf.easy.rpc.socket.support;

import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageHandler;
import ru.coderedwolf.easy.rpc.socket.exceptions.MessageSendException;

import java.util.concurrent.Executor;

/**
 * This class describe implementation of channel.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public class ExecutorSubscribableChannel extends AbstractSubscribeChannel {

    private final Executor executor;

    /**
     * Constructor without executor.
     */
    public ExecutorSubscribableChannel() {
        this(null);
    }

    /**
     * Constructor without executor with given executor.
     */
    public ExecutorSubscribableChannel(Executor executor) {
        this.executor = executor;
    }

    /**
     * Send message to channel, all subscribers handle this message.
     *
     * @param message given message.
     */
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

    private static class SendTask implements Runnable {

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
