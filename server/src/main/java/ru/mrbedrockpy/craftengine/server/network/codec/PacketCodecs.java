package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.network.packet.util.VarInt;
import ru.mrbedrockpy.craftengine.server.registry.Registries;

// Кодеки которые будут повторятся из пакета в пакет и переиспользоватся
public class PacketCodecs {
    public static final BufCodec<Vector3i> POS_CODEC = BufCodec.of(
            (pos, buf) -> {
                buf.writeInt(pos.x);
                buf.writeInt(pos.y);
                buf.writeInt(pos.z);
            },
            buf -> new Vector3i(buf.readInt(), buf.readInt(), buf.readInt())
    );

    public static final BufCodec<Block> BLOCK_CODEC = BufCodec.of(
            (block, buf) -> buf.writeInt(Registries.BLOCKS.getId(block)),
            (buf) -> Registries.BLOCKS.get(buf.readInt())
    );
}
