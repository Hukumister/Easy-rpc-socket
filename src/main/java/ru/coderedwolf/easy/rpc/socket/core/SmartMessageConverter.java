package ru.coderedwolf.easy.rpc.socket.core;

import org.jetbrains.annotations.Nullable;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;

/**
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public interface SmartMessageConverter extends MessageConverter {

    @Nullable
    Object fromMessage(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint);

    @Nullable
    Message<?> toMessage(Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint);
}
