package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import io.netty.buffer.ByteBuf;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodecs;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketHandleContext;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

public record BlockUpdatePacketS2C(Vector3i pos, Block block) implements Packet {}
