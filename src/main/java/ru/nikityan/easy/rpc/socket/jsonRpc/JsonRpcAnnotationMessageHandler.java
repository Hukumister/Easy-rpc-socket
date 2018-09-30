package ru.nikityan.easy.rpc.socket.jsonRpc;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageChannel;
import ru.nikityan.easy.rpc.socket.SubscribeMessageChanel;
import ru.nikityan.easy.rpc.socket.core.JsonRequestMessageConverter;
import ru.nikityan.easy.rpc.socket.core.JsonRpcSendingTemplate;
import ru.nikityan.easy.rpc.socket.handler.AbstractExceptionHandlerMethodResolver;
import ru.nikityan.easy.rpc.socket.handler.resolvers.ArgumentResolver;
import ru.nikityan.easy.rpc.socket.handler.resolvers.ParamArgumentResolver;
import ru.nikityan.easy.rpc.socket.invocation.HandlerMethodReturnValueHandler;
import ru.nikityan.easy.rpc.socket.invocation.ResponseMethodReturnValueHandler;
import ru.nikityan.easy.rpc.socket.invocation.SubscribeMethodReturnValueHandler;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.Controller;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.RequestMapping;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.SubscribeMapping;
import ru.nikityan.easy.rpc.socket.support.AbstractMessageHandler;
import ru.nikityan.easy.rpc.socket.support.JsonRpcError;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;
import ru.nikityan.easy.rpc.socket.support.MessageHeaderAccessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nikit on 15.09.2018.
 */
public class JsonRpcAnnotationMessageHandler extends AbstractMessageHandler implements SmartLifecycle {

    private final Object lifecycleMonitor = new Object();

    private volatile boolean running = false;

    @NotNull
    private final SubscribeMessageChanel inboundChannel;

    @NotNull
    private final MessageChannel outboundChannel;

    public JsonRpcAnnotationMessageHandler(@NotNull SubscribeMessageChanel inboundChannel,
                                           @NotNull MessageChannel outboundChannel) {
        Assert.notNull(inboundChannel, "inbound channel is required");
        Assert.notNull(outboundChannel, "outbound channel is required");
        this.inboundChannel = inboundChannel;
        this.outboundChannel = outboundChannel;
    }

    @Override
    protected String getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMapping annotation = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (annotation != null) {
            if (StringUtils.isEmpty(annotation.value())) {
                throw new IllegalStateException("Request mapping annotation required value");
            }
            return annotation.value();
        }

        SubscribeMapping subscribe = AnnotatedElementUtils.findMergedAnnotation(method, SubscribeMapping.class);
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
        JsonRpcSendingTemplate template = new JsonRpcSendingTemplate(outboundChannel);
        ResponseMethodReturnValueHandler responseMethodReturnValueHandler = new ResponseMethodReturnValueHandler(template);
        SubscribeMethodReturnValueHandler subscribeMethodReturnValueHandler = new SubscribeMethodReturnValueHandler(template);
        return Arrays.asList(responseMethodReturnValueHandler, subscribeMethodReturnValueHandler);
    }

    @Override
    protected List<? extends ArgumentResolver> initArgumentResolvers() {
        List<ArgumentResolver> argumentResolverList = new ArrayList<>();
        argumentResolverList.add(new ParamArgumentResolver(new JsonRequestMessageConverter()));
        return argumentResolverList;
    }

    @Override
    protected AbstractExceptionHandlerMethodResolver createExceptionHandlerMethodResolverFor(Class<?> beanType) {
        return null;
    }

    @Override
    protected void handleNotFoundMethod(Message<?> message, String destination) {
        JsonRpcError rpcError = JsonRpcError.methodNotFound();
        Long messageId = message.getMessageHeader().getId();
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(messageId, rpcError);
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(jsonRpcResponse)
                .withHeaders(message.getMessageHeader())
                .build();
        boolean send = outboundChannel.send(responseMessage);
        if (!send) {
            logger.debug("Error while send message = {}", responseMessage);
        }
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class);
    }

    @Override
    protected String getDestination(Message<?> message) {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.createAccessor(message);
        return accessor.getMessageMethod();
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
