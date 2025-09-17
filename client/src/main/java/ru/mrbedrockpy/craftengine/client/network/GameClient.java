package ru.mrbedrockpy.craftengine.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.mrbedrockpy.craftengine.client.network.game.GameClientListener;
import ru.mrbedrockpy.craftengine.client.network.packet.Packet;
import ru.mrbedrockpy.craftengine.client.network.util.ClientPacketDecoder;
import ru.mrbedrockpy.craftengine.client.network.util.ClientPacketEncoder;
import ru.mrbedrockpy.craftengine.client.network.util.VarintFrameDecoder;
import ru.mrbedrockpy.craftengine.client.network.util.VarintFrameEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class GameClient {

    private final String host;
    private final int port;

    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;

    private volatile GameClientListener listener = new GameClientListener() {
        @Override
        public void onPacket(Packet packet) {}
    };
    private final PacketRegistry registry;

    public GameClient(String host, int port, PacketRegistry registry) {
        this.host = host;
        this.port = port;
        this.registry = registry;
    }

    public void setListener(GameClientListener l) {
        this.listener = (l != null) ? l : new GameClientListener() {
            @Override
            public void onPacket(Packet packet) {

            }
        };
    }

    public void connect() throws InterruptedException {
        Bootstrap b = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // [frame] length(varint) + payload
                        p.addLast(new VarintFrameDecoder());
                        p.addLast(new ClientPacketDecoder(registry));
                        p.addLast(new ClientPacketEncoder(registry));
                        p.addLast(new VarintFrameEncoder());
                        p.addLast(new SimpleChannelInboundHandler<Packet>() {
                            @Override public void channelActive(ChannelHandlerContext ctx) {
                                listener.onConnected();
                            }
                            @Override protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
                                listener.onPacket(msg);
                            }
                            @Override public void channelInactive(ChannelHandlerContext ctx) {
                                listener.onDisconnected("connection closed", null);
                            }
                            @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                listener.onDisconnected("netty error", cause);
                                ctx.close();
                            }
                        });
                    }
                });

        ChannelFuture f = b.connect(host, port).sync();
        channel = f.channel();
    }

    public void send(Packet packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        }
    }

    public void close() {
        try {
            if (channel != null) channel.close().syncUninterruptibly();
        } finally {
            group.shutdownGracefully();
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }
}