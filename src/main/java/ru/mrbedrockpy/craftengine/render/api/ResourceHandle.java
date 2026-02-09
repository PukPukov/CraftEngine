package ru.mrbedrockpy.craftengine.render.api;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record ResourceHandle(String path, ResourceSource source) {

    public InputStream open() {
        return source.open(path);
    }

    public boolean exists() {
        return source.exists(path);
    }

    public String readString() {
        try (InputStream in = req(open())) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage readImage() {
        try (InputStream in = req(open())) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IOException("Unsupported/invalid image: " + this);
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> T req(T v) {
        if (v == null) try {
            throw new IOException("Resource not found: null stream for " + ResourceHandle.class.getSimpleName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override public String toString() { return source.id() + ":" + path; }
}
