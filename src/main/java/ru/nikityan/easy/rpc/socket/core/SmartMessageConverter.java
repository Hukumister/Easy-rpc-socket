package ru.nikityan.easy.rpc.socket.core;

import org.jetbrains.annotations.Nullable;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

/**
 * Created by Nikit on 30.09.2018.
 */
public interface SmartMessageConverter extends MessageConverter {

    @Nullable
    Object fromMessage(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint);

    @Nullable
    Message<?> toMessage(Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint);
}
