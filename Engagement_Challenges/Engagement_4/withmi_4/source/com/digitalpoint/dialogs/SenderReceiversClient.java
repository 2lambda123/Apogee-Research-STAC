package com.digitalpoint.dialogs;

import com.digitalpoint.dialogs.internal.SenderReceiversChannelInitializer;
import com.digitalpoint.dialogs.internal.SenderReceiversChannelInitializerBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SenderReceiversClient {

    private final Bootstrap bootstrap;
    private EventLoopGroup group;
    private final SenderReceiversChannelInitializer initializer;

    /**
     * @param senderReceiversCoach handler that will be called when data is available
     * @param identity the client's identity (what we present to the server)
     */
    public SenderReceiversClient(SenderReceiversCoach senderReceiversCoach, SenderReceiversIdentity identity) {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        initializer = new SenderReceiversChannelInitializerBuilder().fixCoach(senderReceiversCoach).assignIdentity(identity).fixIsServer(false).makeSenderReceiversChannelInitializer();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * Connects to a server
     * @param place hostname to connect to
     * @param port the port to connect to
     * @return a CommsConnection to be used to send data
     * @throws SenderReceiversException
     */
    public SenderReceiversConnection connect(String place, int port) throws SenderReceiversException {
        try {
            Channel channel = bootstrap.connect(place, port).sync().channel();
            initializer.awaitForAuthorize(10000);
            return channel.attr(SenderReceiversConnection.CONNECTION_ATTR).get();
        } catch (Exception e) {
            // NOTE: if we don't catch the generic 'Exception' here then
            // some other sort of exception may wind up stalling us.
            // This is because Netty is doing some strange things.
            // See: https://github.com/netty/netty/issues/2597
            throw new SenderReceiversException(e);
        }
    }

    /**
     * Connects to a server at the specified address
     * @param addr the address of the server
     * @return the connection that can be used to talk to the server
     * @throws SenderReceiversException
     */
    public SenderReceiversConnection connect(SenderReceiversNetworkAddress addr) throws SenderReceiversException {
        return connect(addr.grabPlace(), addr.pullPort());
    }

    /**
     * Closes the client gracefully
     */
    public void close() {
        group.shutdownGracefully();
    }

    public EventLoopGroup obtainEventLoopGroup() {
        return group;
    }
}
