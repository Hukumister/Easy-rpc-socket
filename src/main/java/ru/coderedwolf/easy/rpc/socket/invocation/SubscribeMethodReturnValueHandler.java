package ru.coderedwolf.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.core.MessageSendingOperations;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.Subscribe;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class SubscribeMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final MessageSendingOperations messageSendingOperations;

    public SubscribeMethodReturnValueHandler(MessageSendingOperations messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(Subscribe.class);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue,
                                  MethodParameter returnType,
                                  Message<?> message) throws Exception {

        if (returnType == null) {
            throw new IllegalStateException("Method parameter id required");
        }
        MessageHeaders messageHeader = message.getMessageHeader();
        MessageHeaderAccessor messageHeaderAccessor = MessageHeaderAccessor.ofMessage(message);

        Subscribe methodAnnotation = returnType.getMethodAnnotation(Subscribe.class);
        messageHeaderAccessor.setSubscribeName(methodAnnotation.value());
        messageHeaderAccessor.setMessageMethod(methodAnnotation.value());

        MessageHeaders messageHeaders = messageHeaderAccessor.getMessageHeaders();
        JsonRpcResponse rpcResponse = new JsonRpcResponse(messageHeader.getId(), returnValue);

        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse)
                .withHeaders(messageHeaders)
                .build();
        messageHeaderAccessor.setImmutable();
        messageSendingOperations.send(responseMessage);
    }
}
