package ru.mrbedrockpy.craftengine.server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import ru.mrbedrockpy.craftengine.server.Logger;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import ru.mrbedrockpy.craftengine.server.network.packet.util.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class NetworkManager extends Thread {

    public interface ConnectionListener {
        void onConnected(Channel ch);
        void onDisconnected(Channel ch);
    }

    private final int port;
    private final ConcurrentQueue<Server.IncomingPacket> incomingQueue;
    private final PacketRegistry packetRegistry;
    private final Logger logger = Logger.getLogger(NetworkManager.class);

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NetworkManager(int port, ConcurrentQueue<Server.IncomingPacket> incomingQueue, PacketRegistry packetRegistry) {
        this.port = port;
        this.incomingQueue = Objects.requireNonNull(incomingQueue, "incomingQueue");
        this.packetRegistry = Objects.requireNonNull(packetRegistry, "packetRegistry");
        setName("NetworkManager");
        setDaemon(false);
    }

    public void addListener(ConnectionListener l) { listeners.add(l); }
    public void removeListener(ConnectionListener l) { listeners.remove(l); }

    @Override
    public void run() {
        bossGroup   = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new GameInboundHandler(incomingQueue, logger, packetRegistry, listeners))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture bindFuture = b.bind(port).sync();
            serverChannel = bindFuture.channel();
            logger.info("Network manager started on port " + port);

            serverChannel.closeFuture().sync();
        } catch (InterruptedException ie) {
            logger.error("Network manager interrupted");
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logger.error("Network manager fatal error " + t);
        } finally {
            // корректное завершение
            shutdownEventLoops();
            logger.info("Network manager stopped");
        }
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.eventLoop().execute(() -> {
                if (serverChannel.isOpen()) serverChannel.close();
            });
        } else {
            shutdownEventLoops();
        }
        interrupt();
    }

    private void shutdownEventLoops() {
        if (bossGroup != null)  bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup = null;
    }

    private static final class GameInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private final ConcurrentQueue<Server.IncomingPacket> incomingQueue;
        private final Logger logger;
        private final PacketRegistry packetRegistry;
        private final List<ConnectionListener> listeners;

        GameInboundHandler(ConcurrentQueue<Server.IncomingPacket> incomingQueue,
                           Logger logger,
                           PacketRegistry packetRegistry,
                           List<ConnectionListener> listeners) {
            super(true);
            this.incomingQueue = incomingQueue;
            this.logger = logger;
            this.packetRegistry = packetRegistry;
            this.listeners = listeners;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            for (var l : listeners) l.onConnected(ctx.channel());
            ctx.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            for (var l : listeners) l.onDisconnected(ctx.channel());
            ctx.fireChannelInactive();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
            // Фрейм: [VarInt packetId][payload...]
            if (in.readableBytes() < 1) {
                logger.error("Empty frame");
                return;
            }
            int packetId;
            try {
                packetId = VarInt.read(in);
            } catch (IndexOutOfBoundsException e) {
                logger.error("Malformed VarInt for packet id");
                return;
            }

            @SuppressWarnings("unchecked")
            PacketCodec<? extends Packet> codec = (PacketCodec<? extends Packet>)
                    packetRegistry.byId(PacketDirection.C2S, packetId);

            if (codec == null) {
                logger.error("Unknown packet id: " + packetId);
                return;
            }

            try {
                Packet packet = codec.decode(in);
                if (packet == null) {
                    logger.error("Decoded null packet id=" + packetId);
                    return;
                }
                incomingQueue.add(new Server.IncomingPacket(ctx.channel(), packet));
            } catch (Throwable t) {
                logger.error("Packet decode error, id=" + packetId + " " + t);
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Channel exception: " + ctx.channel() + cause);
            ctx.close();
        }
    }
}