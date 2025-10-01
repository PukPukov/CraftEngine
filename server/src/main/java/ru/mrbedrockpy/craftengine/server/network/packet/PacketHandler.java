package ru.mrbedrockpy.craftengine.server.network.packet;

import java.util.concurrent.Executor;

public interface PacketHandler<C extends PacketHandleContext> {

    <P extends  Packet>void handle(C ctx, P packet);

    default <P extends  Packet>void validate(C ctx, P packet) {}

    default <P extends  Packet>void onError(C ctx, P packet, Throwable error) {
        ctx.logger().error("Packet handle error: " + packet.getClass().getSimpleName() + " from " + ctx.remoteAddress() + " " + error);
        ctx.disconnect("Protocol error");
    }
}