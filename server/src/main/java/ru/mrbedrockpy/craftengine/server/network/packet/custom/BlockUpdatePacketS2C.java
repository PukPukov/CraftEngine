package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

public record BlockUpdatePacketS2C(Vector3i pos, Block block) implements Packet {}
