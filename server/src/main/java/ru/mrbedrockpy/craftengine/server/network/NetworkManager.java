package ru.mrbedrockpy.craftengine.server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import ru.mrbedrockpy.craftengine.server.Logger;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;

public class NetworkManager extends Thread {

    private final int port;
    private final ConcurrentQueue<Packet> incomingQueue;
    private final PacketRegistry packetRegistry;
    private final Logger logger = Logger.getLogger(NetworkManager.class);

    private volatile boolean running = true;
    
    public NetworkManager(int port, ConcurrentQueue<Packet> incomingQueue, PacketRegistry packetRegistry) {
        this.port = port;
        this.incomingQueue = incomingQueue;
        this.packetRegistry = packetRegistry;
        setName("NetworkManager-Thread");
    }
    
    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new NetworkHandler(incomingQueue, logger, packetRegistry));
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            logger.info("Network manager started on port " + port);

            while (running) {
                Thread.sleep(1000);
            }

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Network manager interrupted");
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
    
    public void shutdown() {
        running = false;
        interrupt();
    }

    private static class NetworkHandler extends ChannelInboundHandlerAdapter {

        private final ConcurrentQueue<Packet> incomingQueue;
        private final Logger logger;
        private final PacketRegistry packetRegistry;

        public NetworkHandler(ConcurrentQueue<Packet> incomingQueue, Logger logger, PacketRegistry packetRegistry) {
            this.incomingQueue = incomingQueue;
            this.logger = logger;
            this.packetRegistry = packetRegistry;
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                ByteBuf in = (ByteBuf) msg;
                if (in.writableBytes() < 1) {
                    logger.error("Invalid packet length");
                    return;
                }
                PacketCodec<? extends Packet> codec = packetRegistry.byId(PacketDirection.C2S, in.readByte());
                if (codec == null) {
                    logger.error("Unknown packet");
                    return;
                }
                Packet packet = codec.decode(in);
                if (packet == null) {
                    logger.error("Invalid packet");
                    return;
                }
                incomingQueue.add(packet);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace(System.err);
            ctx.close();
        }
    }
}
