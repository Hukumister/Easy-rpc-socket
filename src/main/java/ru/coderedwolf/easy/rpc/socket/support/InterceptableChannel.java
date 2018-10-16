package ru.coderedwolf.easy.rpc.socket.support;

import java.util.List;

/**
 * Created by Nikit on 24.08.2018.
 */
public interface InterceptableChannel {

    /**
     * Add a channel interceptor to the end of the list.
     */
    void addInterceptor(ChannelInterceptor interceptor);

    /**
     * Return the list of configured interceptors.
     */
    List<ChannelInterceptor> getInterceptors();

    /**
     * Remove channel interceptor.
     */
    void removeInterceptor(ChannelInterceptor interceptor);
}
