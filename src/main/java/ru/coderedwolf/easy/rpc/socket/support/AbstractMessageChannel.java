package ru.coderedwolf.easy.rpc.socket.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.coderedwolf.easy.rpc.socket.MessageChannel;
import ru.coderedwolf.easy.rpc.socket.exceptions.MessagingException;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.exceptions.MessageSendException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nikit on 24.08.2018.
 */
public abstract class AbstractMessageChannel implements MessageChannel, InterceptableChannel {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<ChannelInterceptor> interceptors = new ArrayList<>(5);

    @Override
    public void addInterceptor(ChannelInterceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
    }

    @Override
    public List<ChannelInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public void removeInterceptor(ChannelInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    @Override
    public final boolean send(Message<?> message) {
        if (message == null) {
            throw new NullPointerException("message can not be null");
        }
        boolean sent = false;
        Message<?> useMessage = message;
        try {
            useMessage = applyPreSend(useMessage);
            if (useMessage == null) {
                return false;
            }
            sent = sendMessage(useMessage);
            applyPostSend(useMessage, sent);
            return sent;
        } catch (Throwable ex) {
            applyPostSend(useMessage, sent);
            if (ex instanceof MessagingException) {
                throw (MessagingException) ex;
            }
            throw new MessageSendException(useMessage, "fail to send message");
        }
    }

    public abstract boolean sendMessage(Message<?> message);

    protected Message<?> applyPreSend(Message<?> message) {
        Message<?> useMessage = message;
        for (ChannelInterceptor interceptor : interceptors) {
            Message<?> resultMessage = interceptor.preSend(useMessage, this);
            if (resultMessage == null) {
                logger.debug("return null after pre send");
                applyPostSend(message, false);
                return null;
            }
            useMessage = resultMessage;
        }
        return useMessage;
    }


    protected void applyPostSend(Message<?> message, boolean sent) {
        for (ChannelInterceptor interceptor : interceptors) {
            interceptor.postSend(message, this, sent);
        }
    }
}
