package ru.nikityan.easy.rpc.socket.support;

import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

import java.util.Map;

/**
 * Created by Nikit on 28.09.2018.
 */
public class MessageHeaderAccessor {

    public final static String SUBSCRIBE_METHOD = "subscribeMethodName";

    public final static String MESSAGE_METHOD = "messageMethod";

    private final MuttableHeaders messageHeaders;

    private MessageHeaderAccessor(Message<?> message) {
        this.messageHeaders = new MuttableHeaders(message.getMessageHeader());
    }

    public static MessageHeaderAccessor createAccessor(Message<?> message) {
        Assert.notNull(message, "message required");
        return new MessageHeaderAccessor(message);
    }

    @Nullable
    public static String getSubscribeMethod(MessageHeaders messageHeaders) {
        Object subscribeName = messageHeaders.get(SUBSCRIBE_METHOD);
        if (subscribeName != null) {
            return (String) subscribeName;
        }
        return null;
    }

    public void setMessageMethod(String method) {
        this.messageHeaders.getRawHeader().put(MESSAGE_METHOD, method);
    }

    public void setSubscribeName(String method) {
        this.messageHeaders.getRawHeader().put(SUBSCRIBE_METHOD, method);
    }

    public String getMessageMethod() {
        Object header = getHeader(MESSAGE_METHOD);
        return header != null ? (String) header : "";
    }

    public String getSubscribeMethod() {
        Object header = getHeader(SUBSCRIBE_METHOD);
        return header != null ? (String) header : null;
    }

    public Object getHeader(String key) {
        return this.messageHeaders.get(key);
    }

    public void putHeader(String key, Object value) {
        this.messageHeaders.getRawHeader().put(key, value);
    }

    public void setImmutable() {
        this.messageHeaders.setImuttable();
    }

    public MessageHeaders getMessageHeaders() {
        return this.messageHeaders;
    }

    private static class MuttableHeaders extends MessageHeaders {

        private boolean muttable = true;

        public MuttableHeaders(Map<String, Object> headers) {
            super(headers);
        }

        public void setImuttable() {
            this.muttable = false;
        }

        @Override
        protected Map<String, Object> getRawHeader() {
            Assert.state(muttable, "Already immutable");
            return super.getRawHeader();
        }
    }
}
