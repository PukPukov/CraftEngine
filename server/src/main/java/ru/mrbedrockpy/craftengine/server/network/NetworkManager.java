package ru.mrbedrockpy.craftengine.server.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.server.Logger;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;

import ru.mrbedrockpy.craftengine.server.network.packet.util.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NetworkManager extends Thread {


    public interface ConnectionListener {
        void onConnected(Channel ch);
        void onDisconnected(Channel ch);
    }

    public enum Mode { SERVER, CLIENT }

    private final Mode mode;
    private final String host;  // для клиента
    private final int port;     // для сервера и клиента

    private final ConcurrentQueue<Server.IncomingPacket> incomingQueue;
    private final PacketRegistry packetRegistry;
    private final Logger logger = Logger.getLogger(NetworkManager.class);

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

    private EventLoopGroup bossGroup;   // только сервер
    private EventLoopGroup workerGroup; // сервер/клиент
    @Getter
    private Channel channel;

    // --- Конструкторы ---
    // Сервер
    public static NetworkManager server(int port,
                                        ConcurrentQueue<Server.IncomingPacket> incomingQueue,
                                        PacketRegistry packetRegistry) {
        return new NetworkManager(Mode.SERVER, null, port, incomingQueue, packetRegistry);
    }
    // Клиент
    public static NetworkManager client(String host, int port,
                                        ConcurrentQueue<Server.IncomingPacket> incomingQueue,
                                        PacketRegistry packetRegistry) {
        Objects.requireNonNull(host, "host");
        return new NetworkManager(Mode.CLIENT, host, port, incomingQueue, packetRegistry);
    }

    private NetworkManager(Mode mode, String host, int port,
                           ConcurrentQueue<Server.IncomingPacket> incomingQueue,
                           PacketRegistry packetRegistry) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.host = host;
        this.port = port;
        this.incomingQueue = Objects.requireNonNull(incomingQueue, "incomingQueue");
        this.packetRegistry = Objects.requireNonNull(packetRegistry, "packetRegistry");
        setName("NetworkManager-" + mode.name().toLowerCase());
        setDaemon(false);
    }

    public void addListener(ConnectionListener l) { listeners.add(l); }
    public void removeListener(ConnectionListener l) { listeners.remove(l); }

    // Общий инициализатор пайплайна
    private ChannelInitializer<SocketChannel> pipeline(Mode sideMode) {
        final boolean isServer = (sideMode == Mode.SERVER);
        final PacketDirection inboundDir  = isServer ? PacketDirection.C2S : PacketDirection.S2C;
        final PacketDirection outboundDir = isServer ? PacketDirection.S2C : PacketDirection.C2S;

        return new ChannelInitializer<>() {
            @Override protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();

                p.addLast(new VarintFrameDecoder());
                p.addLast(new PacketDecoder(packetRegistry, inboundDir));

                p.addLast(new VarintFrameEncoder());
                p.addLast(new PacketEncoder(packetRegistry, outboundDir));

                p.addLast(new SimpleChannelInboundHandler<Packet>() {
                    @Override protected void channelRead0(ChannelHandlerContext ctx, Packet pkt) {
                        incomingQueue.add(new Server.IncomingPacket(ctx.channel(), pkt));
                    }
                    @Override public void channelActive(ChannelHandlerContext ctx) {
                        for (var l : listeners) l.onConnected(ctx.channel());
                    }
                    @Override public void channelInactive(ChannelHandlerContext ctx) {
                        for (var l : listeners) l.onDisconnected(ctx.channel());
                    }
                    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        logger.error("Channel exception: " + ctx.channel(), cause);
                        ctx.close();
                    }
                });
            }
        };
    }

    public Channel connectSync() {
        if (mode != Mode.CLIENT) throw new IllegalStateException("Only for client");
        workerGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        try {
            Bootstrap b = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(pipeline(Mode.CLIENT));

            ChannelFuture cf = b.connect(host, port).sync(); // блокируемся
            this.channel = cf.channel();
            return this.channel;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while connecting", ie);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to connect", t);
        }
    }

    @Override
    public void run() {
        if (mode == Mode.SERVER) runServer();
        else runClient();
    }

    private void runServer() {
        bossGroup   = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        try {
            ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(pipeline(Mode.SERVER))
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture bindFuture = b.bind(port).sync();
            channel = bindFuture.channel();
            logger.info("NetworkManager SERVER started on port " + port);

            channel.closeFuture().sync(); // ждём закрытия
        } catch (InterruptedException ie) {
            logger.warn("NetworkManager SERVER interrupted");
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logger.error("NetworkManager SERVER fatal error", t);
        } finally {
            shutdownEventLoops();
            logger.info("NetworkManager SERVER stopped");
        }
    }

    private void runClient() {
        workerGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        try {
            Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(pipeline(Mode.CLIENT));

            ChannelFuture connectFuture = b.connect(host, port).sync();
            channel = connectFuture.channel();
            logger.info("NetworkManager CLIENT connected to " + host + ":" + port);

            channel.closeFuture().sync(); // ждём закрытия
        } catch (InterruptedException ie) {
            logger.warn("NetworkManager CLIENT interrupted");
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logger.error("NetworkManager CLIENT fatal error", t);
        } finally {
            shutdownEventLoops();
            logger.info("NetworkManager CLIENT stopped");
        }
    }

    // ---------- API ----------
    /** Асинхронно закрыть канал и петли. */
    public void shutdown() {
        Channel ch = this.channel;
        if (ch != null) {
            ch.eventLoop().execute(() -> {
                if (ch.isOpen()) ch.close();
            });
        } else {
            shutdownEventLoops();
        }
        interrupt();
    }

    /** Удобный helper для отправки пакета по активному каналу. */
    public void send(Packet pkt) {
        Channel ch = this.channel;
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(pkt);
        } else {
            logger.warn("send(): channel is not active");
        }
    }

    private void shutdownEventLoops() {
        if (bossGroup != null)  bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup = null;
    }
}