package ru.coderedwolf.easy.rpc.socket.jsonRpc;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageChannel;
import ru.coderedwolf.easy.rpc.socket.SubscribeMessageChanel;
import ru.coderedwolf.easy.rpc.socket.core.JsonRequestMessageConverter;
import ru.coderedwolf.easy.rpc.socket.core.JsonRpcSendingTemplate;
import ru.coderedwolf.easy.rpc.socket.handler.resolvers.ParamArgumentResolver;
import ru.coderedwolf.easy.rpc.socket.invocation.ExceptionMethodReturnValueHandler;
import ru.coderedwolf.easy.rpc.socket.invocation.HandlerMethodReturnValueHandler;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.Subscribe;
import ru.coderedwolf.easy.rpc.socket.support.AbstractMessageHandler;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.handler.AbstractExceptionHandlerMethodResolver;
import ru.coderedwolf.easy.rpc.socket.handler.resolvers.ArgumentResolver;
import ru.coderedwolf.easy.rpc.socket.invocation.ResponseMethodReturnValueHandler;
import ru.coderedwolf.easy.rpc.socket.invocation.SubscribeMethodReturnValueHandler;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.JsonRpcController;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.RequestMethod;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRpcAnnotationMessageHandler extends AbstractMessageHandler implements SmartLifecycle {

    private final Object lifecycleMonitor = new Object();

    private volatile boolean running = false;

    @NotNull
    private final SubscribeMessageChanel inboundChannel;

    @NotNull
    private final JsonRpcSendingTemplate template;

    public JsonRpcAnnotationMessageHandler(@NotNull SubscribeMessageChanel inboundChannel,
                                           @NotNull MessageChannel outboundChannel) {
        Assert.notNull(inboundChannel, "inbound channel is required");
        Assert.notNull(outboundChannel, "outbound channel is required");

        this.inboundChannel = inboundChannel;
        this.template = new JsonRpcSendingTemplate(outboundChannel);
    }

    @Override
    protected String getMappingForMethod(java.lang.reflect.Method method, Class<?> handlerType) {
        RequestMethod annotation = AnnotatedElementUtils.findMergedAnnotation(method, RequestMethod.class);
        if (annotation != null) {
            if (StringUtils.isEmpty(annotation.value())) {
                throw new IllegalStateException("Request mapping annotation required value");
            }
            return annotation.value();
        }

        Subscribe subscribe = AnnotatedElementUtils.findMergedAnnotation(method, Subscribe.class);
        if (subscribe != null) {
            if (StringUtils.isEmpty(subscribe.value())) {
                throw new IllegalStateException("Subscribe annotation required value");
            }
            return subscribe.value();
        }
        return null;
    }

    @Override
    protected List<HandlerMethodReturnValueHandler> initMethodReturnValue() {
        ResponseMethodReturnValueHandler responseMethodReturnValueHandler = new ResponseMethodReturnValueHandler(template);
        SubscribeMethodReturnValueHandler subscribeMethodReturnValueHandler = new SubscribeMethodReturnValueHandler(template);
        ExceptionMethodReturnValueHandler exceptionMethodReturnValueHandler = new ExceptionMethodReturnValueHandler(template);
        return Arrays.asList(responseMethodReturnValueHandler,
                subscribeMethodReturnValueHandler,
                exceptionMethodReturnValueHandler);
    }

    @Override
    protected List<? extends ArgumentResolver> initArgumentResolvers() {
        List<ArgumentResolver> argumentResolverList = new ArrayList<>();
        argumentResolverList.add(new ParamArgumentResolver(new JsonRequestMessageConverter()));
        return argumentResolverList;
    }

    @Override
    protected AbstractExceptionHandlerMethodResolver createExceptionHandlerMethodResolverFor(Class<?> beanType) {
        return new AnnotationExceptionHandlerMethodResolver(beanType);
    }

    @Override
    protected void handleNotFoundMethod(Message<?> message, String destination) {
        JsonRpcError rpcError = JsonRpcError.methodNotFound();
        Long messageId = message.getMessageHeader().getId();
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(messageId, rpcError);
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(jsonRpcResponse)
                .withHeaders(message.getMessageHeader())
                .build();
        template.send(destination, responseMessage);
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, JsonRpcController.class);
    }

    @Override
    protected String getDestination(Message<?> message) {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(message);
        return accessor.getMessageMethod();
    }

    @Override
    protected void handleDefaultError(Exception exception, Message<?> message) {
        JsonRpcResponse rpcResponse = new JsonRpcResponse(message.getMessageHeader().getId(),
                JsonRpcError.internalError());
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse)
                .withHeaders(message.getMessageHeader())
                .build();
        template.send(responseMessage);
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        synchronized (this.lifecycleMonitor) {
            stop();
            callback.run();
        }
    }

    @Override
    public void start() {
        synchronized (this.lifecycleMonitor) {
            this.running = true;
            inboundChannel.subscribe(this);
        }
    }

    @Override
    public void stop() {
        synchronized (this.lifecycleMonitor) {
            this.running = false;
            inboundChannel.unSubscribe(this);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
