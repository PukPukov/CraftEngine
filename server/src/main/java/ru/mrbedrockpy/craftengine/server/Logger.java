package ru.mrbedrockpy.craftengine.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

@Getter
@AllArgsConstructor
public class Logger {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final String namespace;

    public void info(String msg) {
        System.out.println(":: " + ANSI_GREEN + getNowTime() + " INFO [" + namespace + "] >> " + msg + ANSI_RESET);
    }

    public void error(String msg) {
        System.out.println(":: " + ANSI_RED + getNowTime() + " ERROR [" + namespace + "] >> " + msg + ANSI_RESET);
    }

    public String validate(int digit) {
        String string = String.valueOf(digit);
        if (string.length() == 1) string = "0" + string;
        return string;
    }

    private String getNowTime() {
        Calendar calendar = new GregorianCalendar();
        return validate(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                validate(calendar.get(Calendar.MINUTE)) + ":" +
                validate(calendar.get(Calendar.SECOND));
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getName());
    }

    public static Logger getLogger(String namespace) {
        return new Logger(namespace);
    }

}
