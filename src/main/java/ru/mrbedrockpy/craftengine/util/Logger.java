package ru.mrbedrockpy.craftengine.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.util.lang.TextColor;

import java.util.*;

@Getter
@AllArgsConstructor
public class Logger {

    private final String namespace;

    public void info(Object... msg) {
        System.out.println(":: " + TextColor.GREEN + getNowTime() + " INFO [" + namespace + "] >> " + Arrays.toString(msg) + TextColor.RESET);
    }

    public void error(Object... msg) {
        System.out.println(":: " + TextColor.RED + getNowTime() + " ERROR [" + namespace + "] >> " + Arrays.toString(msg) + TextColor.RESET);
    }

    public void warn(Object... msg) {
        System.out.println(":: " + TextColor.YELLOW + getNowTime() + " ERROR [" + namespace + "] >> " + Arrays.toString(msg) + TextColor.RESET);
    }

    private String getNowTime() {
        Calendar calendar = new GregorianCalendar();
        return validate(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                validate(calendar.get(Calendar.MINUTE)) + ":" +
                validate(calendar.get(Calendar.SECOND));
    }

    private String validate(int digit) {
        String string = String.valueOf(digit);
        if (string.length() == 1) string = "0" + string;
        return string;
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getName());
    }

    public static Logger getLogger(String namespace) {
        return new Logger(namespace);
    }

}
