package ru.mrbedrockpy.craftengine.server.network.packet;

import java.util.concurrent.Executor;

public interface PacketHandler<P extends Packet, C extends PacketHandleContext> {

    void handle(C ctx, P packet);

    default void validate(C ctx, P packet) {}

    default void onError(C ctx, P packet, Throwable error) {
        ctx.logger().error("Packet handle error: " + packet.getClass().getSimpleName() + " from " + ctx.remoteAddress() + " " + error);
        ctx.disconnect("Protocol error");
    }
}