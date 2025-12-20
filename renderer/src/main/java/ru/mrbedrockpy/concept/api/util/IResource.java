package ru.mrbedrockpy.concept.api.util;

import java.io.InputStream;

public interface IResource {

    String getId();

    InputStream open(String path);

    boolean isExists(String path);

}
