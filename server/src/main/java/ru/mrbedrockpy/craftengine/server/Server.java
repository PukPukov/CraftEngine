package ru.mrbedrockpy.craftengine.server;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.TestPacket;

public class Server {

    public static class TestClient {
        public static void main(String[] args) throws Exception {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new ClientHandler());
                            }
                        });
                ChannelFuture f = b.connect("5.35.84.241", 8080).sync();
                Channel channel = f.channel();
                TestPacket packet = new TestPacket(20);
                channel.writeAndFlush(((PacketCodec<TestPacket>) PacketRegistry.INSTANCE.getByPacket(packet.getClass())).encode(packet));
                Thread.sleep(2000);
                channel.close();
            } finally {
                group.shutdownGracefully();
            }
        }

        static class ClientHandler extends ChannelInboundHandlerAdapter {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ByteBuf in = (ByteBuf) msg;
                try {
                    String response = in.toString(CharsetUtil.UTF_8);
                    System.out.println("Клиент получил: " + response);
                } finally {
                    in.release();
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }
        }
    }

}
