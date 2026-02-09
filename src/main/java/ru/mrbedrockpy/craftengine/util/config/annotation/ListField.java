package ru.mrbedrockpy.craftengine.util.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ListField {
    String name();
    Class<?> type();
}
