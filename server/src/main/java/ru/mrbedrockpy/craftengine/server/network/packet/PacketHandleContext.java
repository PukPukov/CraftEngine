package ru.mrbedrockpy.craftengine.server.network.packet;

import io.netty.channel.Channel;
import ru.mrbedrockpy.craftengine.server.Logger;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PacketHandleContext {

    protected final Channel channel;
    protected PacketSender sender;
    protected final Logger logger;
    protected final long tick;
    protected final Instant now;

    protected final Map<String, Object> attrs = new ConcurrentHashMap<>();

    protected PacketHandleContext(Builder<?> b) {
        this.channel = b.channel;
        this.sender  = b.sender;
        this.logger  = b.logger;
        this.tick    = b.tick;
        this.now     = (b.now != null ? b.now : Instant.now());
    }

    // --- общее API ---
    public Channel channel()                 { return channel; }
    public Logger logger()                   { return logger; }
    public long tick()                       { return tick; }
    public Instant now()                     { return now; }
    public InetSocketAddress remoteAddress() { return (InetSocketAddress) channel.remoteAddress(); }

    public void send(Packet pkt)      { sender.send(pkt); }
    public void sendNow(Packet pkt)   { sender.sendNow(pkt); }
    public void flush()               { sender.flush(); }
    public void disconnect(String why){ sender.close(why); }
    public boolean isConnected()      { return sender.isOpen(); }

    public PacketHandleContext setAttr(String k, Object v) {
        if (v == null) attrs.remove(k); else attrs.put(k, v);
        return this;
    }
    @SuppressWarnings("unchecked")
    public <T> T getAttr(String k) { return (T) attrs.get(k); }

    public abstract boolean isServerSide();

    public static abstract class Builder<B extends Builder<B>> {
        private Channel channel;
        protected PacketSender sender;
        private Logger logger;
        private long tick;
        private Instant now;

        public B channel(Channel ch)   { this.channel = ch; return self(); }
        public B sender(PacketSender s){ this.sender  = s;  return self(); }
        public B logger(Logger l)      { this.logger  = l;  return self(); }
        public B tick(long t)          { this.tick    = t;  return self(); }
        public B now(Instant n)        { this.now     = n;  return self(); }

        protected abstract B self();
    }
}
