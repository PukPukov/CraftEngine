package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ByteBufUtil {
    public static void writeString(ByteBuf out, String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        VarInt.write(out, b.length); out.writeBytes(b);
    }
    public static String readString(ByteBuf in) {
        int len = VarInt.read(in);
        byte[] b = new byte[len]; in.readBytes(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
