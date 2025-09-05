package ru.mrbedrockpy.renderer.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLoader {
    public static InputStream loadStream(String fileName){
        try {
            InputStream is = FileLoader.class.getClassLoader().getResourceAsStream(fileName);
            if(is != null){
                return is;
            }
            Path path = Paths.get(fileName);
            if(Files.exists(path)){
                return Files.newInputStream(path);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return InputStream.nullInputStream();
    }

    public static String loadString(String fileName) {
        try (InputStream is = loadStream(fileName)) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static BufferedImage loadImage(String fileName){
        try(InputStream is = loadStream(fileName)) {
            return ImageIO.read(is);
        } catch (Exception e){
            return null;
        }
    }
}