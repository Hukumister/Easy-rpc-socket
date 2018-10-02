package ru.nikityan.easy.rpc.socket.invocation;

import com.google.gson.JsonPrimitive;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.core.MessageSendingOperations;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.Subscribe;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * Created by Nikit on 01.10.2018.
 */
public class SubscribeMethodReturnValueHandlerTest {

    @InjectMocks
    private SubscribeMethodReturnValueHandler subscribeMethodReturnValueHandler;

    @Mock
    private MessageSendingOperations messageSendingOperations;

    private MethodParameter annotatedMethodParameter;
    private MethodParameter methodParameter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Method annotatedMethod = SubscribeMethodReturnValueHandlerTest.class.getDeclaredMethod("handle");
        Method notAnnotatedMethod = SubscribeMethodReturnValueHandlerTest.class.getDeclaredMethod("notHandle");

        this.annotatedMethodParameter = new SynthesizingMethodParameter(annotatedMethod, 0);
        this.methodParameter = new SynthesizingMethodParameter(notAnnotatedMethod, 0);
    }

    @Test
    public void supportReturnType() throws Exception {
        assertTrue(subscribeMethodReturnValueHandler.supportsReturnType(annotatedMethodParameter));
        assertFalse(subscribeMethodReturnValueHandler.supportsReturnType(methodParameter));
    }

    @Test
    public void returnMessageHaveSubscribeHeader() throws Exception {
        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", new JsonPrimitive(1L));
        Message<JsonRpcRequest> message = MessageBuilder.fromPayload(rpcRequest).build();

        subscribeMethodReturnValueHandler.handleReturnValue("return", annotatedMethodParameter, message);

        verify(messageSendingOperations).send(argThat(messageHeaderMatcher()));
    }

    @Test(expected = IllegalStateException.class)
    public void throwExIfMethodParamIsNull() throws Exception {
        Message<String> message = MessageBuilder.fromPayload("ABC").build();

        subscribeMethodReturnValueHandler.handleReturnValue("return", null, message);
    }

    @Subscribe("method")
    private void handle() {

    }

    private void notHandle() {

    }

    private Matcher<Message<?>> messageHeaderMatcher() {
        return new BaseMatcher<Message<?>>() {
            @Override
            public boolean matches(Object item) {
                Message<?> message = (Message<?>) item;
                MessageHeaders messageHeader = message.getMessageHeader();
                String subscribeMethod = MessageHeaderAccessor.getSubscribeMethod(messageHeader);
                return subscribeMethod != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Not found required header on message");
            }
        };
    }
}