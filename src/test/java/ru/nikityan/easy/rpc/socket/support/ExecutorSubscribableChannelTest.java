package ru.nikityan.easy.rpc.socket.support;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.task.TaskExecutor;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.MessageHandler;
import ru.nikityan.easy.rpc.socket.exceptions.MessageSendException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

/**
 * Created by Nikit on 25.08.2018.
 */
public class ExecutorSubscribableChannelTest {

    private ExecutorSubscribableChannel channel = new ExecutorSubscribableChannel();

    @Mock
    private MessageHandler handler;

    private final Object payload = new Object();

    private final Message<Object> message = MessageBuilder.fromPayload(this.payload).build();

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void messageMustNotBeNull() {
        this.channel.send(null);
    }

    @Test
    public void sendWithoutExecutor() {
        BeforeHandleInterceptor interceptor = new BeforeHandleInterceptor();

        this.channel.addInterceptor(interceptor);
        this.channel.subscribe(this.handler);
        this.channel.send(this.message);

        verify(this.handler).handleMessage(this.message);
        assertEquals(1, interceptor.getCounter().get());
        assertTrue(interceptor.wasAfterHandledInvoked());
    }

    @Test
    public void sendWithExecutor() {
        BeforeHandleInterceptor interceptor = new BeforeHandleInterceptor();
        TaskExecutor executor = mock(TaskExecutor.class);
        ExecutorSubscribableChannel testChannel = new ExecutorSubscribableChannel(executor);

        testChannel.addInterceptor(interceptor);
        testChannel.subscribe(this.handler);
        testChannel.send(this.message);

        verify(executor).execute(this.runnableCaptor.capture());
        verify(this.handler, never()).handleMessage(this.message);

        this.runnableCaptor.getValue().run();

        verify(this.handler).handleMessage(this.message);
        assertEquals(1, interceptor.getCounter().get());
        assertTrue(interceptor.wasAfterHandledInvoked());
    }

    @Test
    public void subscribeTwice() {
        assertTrue(this.channel.subscribe(this.handler));
        assertFalse(this.channel.subscribe(this.handler));

        this.channel.send(this.message);

        verify(this.handler, times(1)).handleMessage(this.message);
    }

    @Test
    public void unSubscribeTwice() {
        this.channel.subscribe(this.handler);

        assertTrue(this.channel.unSubscribe(this.handler));
        assertFalse(this.channel.unSubscribe(this.handler));

        this.channel.send(this.message);
        verify(this.handler, never()).handleMessage(this.message);
    }

    @Test
    public void failurePropagates() {
        RuntimeException ex = new RuntimeException();
        willThrow(ex).given(this.handler).handleMessage(this.message);
        MessageHandler secondHandler = mock(MessageHandler.class);

        this.channel.subscribe(this.handler);
        this.channel.subscribe(secondHandler);

        try {
            this.channel.send(message);
        } catch (MessageSendException actualException) {
            assertEquals(actualException.getCause(), ex);
        }
        verifyZeroInteractions(secondHandler);
    }

    @Test
    public void concurrentModification() {
        this.channel.subscribe(message1 -> channel.unSubscribe(handler));
        this.channel.subscribe(this.handler);

        this.channel.send(this.message);
        verify(this.handler).handleMessage(this.message);
    }

    @Test
    public void interceptorWithModifiedMessage() {
        Message<?> expected = mock(Message.class);
        BeforeHandleInterceptor interceptor = new BeforeHandleInterceptor();
        interceptor.setMessageToReturn(expected);

        this.channel.addInterceptor(interceptor);
        this.channel.subscribe(this.handler);
        this.channel.send(this.message);

        verify(this.handler).handleMessage(expected);
        assertEquals(1, interceptor.getCounter().get());
        assertTrue(interceptor.wasAfterHandledInvoked());
    }

    @Test
    public void interceptorWithNull() {
        BeforeHandleInterceptor beforeHandleInterceptor = new BeforeHandleInterceptor();
        NullReturningBeforeHandleInterceptor nullReturnInterceptor = new NullReturningBeforeHandleInterceptor();

        this.channel.addInterceptor(beforeHandleInterceptor);
        this.channel.addInterceptor(nullReturnInterceptor);
        this.channel.subscribe(this.handler);
        this.channel.send(this.message);

        verifyNoMoreInteractions(this.handler);
        assertEquals(1, beforeHandleInterceptor.getCounter().get());
        assertEquals(1, nullReturnInterceptor.getCounter().get());
        assertTrue(beforeHandleInterceptor.wasAfterHandledInvoked());
    }

    @Test
    public void interceptorWithException() {
        IllegalStateException expected = new IllegalStateException("Fake exception");
        willThrow(expected).given(this.handler).handleMessage(this.message);
        BeforeHandleInterceptor interceptor = new BeforeHandleInterceptor();

        this.channel.addInterceptor(interceptor);
        this.channel.subscribe(this.handler);
        try {
            this.channel.send(this.message);
        } catch (MessageSendException actual) {
            assertSame(expected, actual.getCause());
        }

        verify(this.handler).handleMessage(this.message);
        assertEquals(1, interceptor.getCounter().get());
        assertTrue(interceptor.wasAfterHandledInvoked());
    }


    private abstract static class AbstractTestInterceptor implements ChannelInterceptor {

        private AtomicInteger counter = new AtomicInteger();

        private volatile boolean afterHandledInvoked;

        public AtomicInteger getCounter() {
            return this.counter;
        }

        public boolean wasAfterHandledInvoked() {
            return this.afterHandledInvoked;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            assertNotNull(message);
            counter.incrementAndGet();
            return message;
        }

        @Override
        public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

            this.afterHandledInvoked = true;
        }
    }

    private static class BeforeHandleInterceptor extends AbstractTestInterceptor {

        private Message<?> messageToReturn;

        private RuntimeException exceptionToRaise;

        public void setMessageToReturn(Message<?> messageToReturn) {
            this.messageToReturn = messageToReturn;
        }

        @SuppressWarnings("unused")
        public void setExceptionToRaise(RuntimeException exception) {
            this.exceptionToRaise = exception;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            super.preSend(message, channel);
            if (this.exceptionToRaise != null) {
                throw this.exceptionToRaise;
            }
            return (this.messageToReturn != null ? this.messageToReturn : message);
        }
    }

    private static class NullReturningBeforeHandleInterceptor extends AbstractTestInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            super.preSend(message, channel);
            return null;
        }
    }
}