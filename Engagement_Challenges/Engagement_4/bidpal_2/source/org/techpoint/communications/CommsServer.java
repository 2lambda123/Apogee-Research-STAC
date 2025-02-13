package org.techpoint.communications;

import org.techpoint.communications.internal.CommsChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class CommsServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public CommsServer(int listenPort, CommsManager manager, CommsIdentity identity) {
        this(listenPort, manager, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param manager used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public CommsServer(int listenPort, CommsManager manager, CommsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new CommsChannelInitializer(manager, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws CommsRaiser {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new CommsRaiser(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
