package ru.mrbedrockpy.craftengine.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.*;

@UtilityClass
public class FileUtil {

    @Nullable
    public String getTextFile(File file) {
        if (file == null || !file.exists()) return null;
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) sb.append(reader.readLine()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            Logger.getLogger(FileUtil.class).error(e);
            return "";
        }
    }

    public void setTextFile(File file, String yamlContent) {
        try {
            if (file == null) return;
            if (yamlContent == null) return;
            if (file.isDirectory()) return;
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.flush();
            writer.write(yamlContent);
            writer.close();
        } catch (Exception e) {
            Logger.getLogger(FileUtil.class).error(e);
        }
    }
}
