package ru.nikityan.easy.rpc.socket.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.exceptions.MessageSendException;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Nikit on 01.10.2018.
 */
public class JsonRpcSendingTemplateTest {

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private JsonRpcSendingTemplate sendingTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(messageChannel.send(any())).thenReturn(true);
    }

    @Test(expected = MessageSendException.class)
    public void throwExIfMessageNotSent() throws Exception {
        when(messageChannel.send(any())).thenReturn(false);
        sendingTemplate.convertAndSend("method", "result");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendWithNullDestination() throws Exception {
        sendingTemplate.convertAndSend("", "result");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendWithEmptyDestination() throws Exception {
        sendingTemplate.convertAndSend(null, "result");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessageWithoutMessageMethod() throws Exception {
        Message<String> stringMessage = MessageBuilder.fromPayload("foo").build();
        sendingTemplate.send(stringMessage);
    }

    @Test
    public void simpleSendMessage() throws Exception {
        MessageHeaderAccessor messageHeaderAccessor = MessageHeaderAccessor.ofHeaders(null);
        messageHeaderAccessor.setMessageMethod("method");
        Message<String> stringMessage = MessageBuilder.fromPayload("foo")
                .withHeaders(messageHeaderAccessor.getMessageHeaders())
                .build();
        sendingTemplate.send(stringMessage);

        verify(messageChannel).send(any());
    }

    @Test
    public void sendMessageWithDestination() throws Exception {
        Message<String> stringMessage = MessageBuilder.fromPayload("foo").build();
        sendingTemplate.send("method", stringMessage);

        verify(messageChannel).send(any());
    }

    @Test
    public void convertAndSendMessageUseDestination() throws Exception {
        sendingTemplate.convertAndSend("method", "result");

        verify(messageChannel).send(argThat(messageHeaderMatcher(messageHeaders ->
                Objects.equals(MessageHeaderAccessor.getMessageMethod(messageHeaders), "method")
                        && messageHeaders.getMessageType() == MessageType.NOTIFICATION)));
    }

    @Test
    public void convertAndSendWithPostProcessor() throws Exception {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        sendingTemplate.send("method", "result", message -> {
            atomicBoolean.set(true);
            return message;
        });

        assertTrue(atomicBoolean.get());
        verify(messageChannel).send(argThat(messageHeaderMatcher(messageHeaders ->
                Objects.equals(MessageHeaderAccessor.getMessageMethod(messageHeaders), "method")
                        && messageHeaders.getMessageType() == MessageType.NOTIFICATION)));
    }

    @Test
    public void convertAndSendWithHeaders() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("foo", "bar");
        headers.put("bar", "foo");

        sendingTemplate.convertAndSend("method", "result", headers);
        verify(messageChannel).send(argThat(messageHeaderMatcher(messageHeaders ->
                Objects.equals(MessageHeaderAccessor.getMessageMethod(messageHeaders), "method")
                        && messageHeaders.getMessageType() == MessageType.NOTIFICATION &&
                        messageHeaders.containsKey("foo") && messageHeaders.containsKey("bar"))));
    }

    private Matcher<Message<?>> messageHeaderMatcher(Function<MessageHeaders, Boolean> function) {
        return new BaseMatcher<Message<?>>() {
            @Override
            public boolean matches(Object item) {
                Message<?> message = (Message<?>) item;
                MessageHeaders messageHeader = message.getMessageHeader();
                return function.apply(messageHeader);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Not found required header on message");
            }
        };
    }
}