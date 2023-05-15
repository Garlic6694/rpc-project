package com.upc.garlic.provider;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface ChannelProvider {
    Channel get(InetSocketAddress inetSocketAddress);

    void set(InetSocketAddress inetSocketAddress, Channel channel);

    void remove(InetSocketAddress inetSocketAddress);
}
