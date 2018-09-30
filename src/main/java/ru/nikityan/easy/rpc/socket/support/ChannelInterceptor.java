package ru.nikityan.easy.rpc.socket.support;


import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;

public interface ChannelInterceptor {

    /**
     * Invoked before the Message is actually sent to the channel.
     * This allows for modification of the Message if necessary.
     * If this method returns {@code null} then the actual
     * send invocation will not occur.
     */

    Message<?> preSend(Message<?> message, MessageChannel channel);


    /**
     * Invoked immediately after the send invocation. The boolean
     * value argument represents the return value of that invocation.
     */
    void postSend(Message<?> message, MessageChannel channel, boolean sent);
}
