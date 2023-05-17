package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.base.SysEx;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class Files {

    static public String readFromClassPath(String path) throws IOException {
        return readFromClassPath(path, StandardCharsets.UTF_8);
    }

    static public String readFromClassPath(String path, Charset charset) throws IOException {
        SysEx.nullThrow(path, "Resource location must not be null");
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
        if (url == null) {
            String description = "class path resource [" + path + "]";
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not exist");
        }
        URI uri = null;
        try {
            uri = new URI(StringUtils.replace(url.toString(), " ", "%20"));
        } catch (URISyntaxException ignore) {}
        File file = new File(Objects.requireNonNull(uri).getSchemeSpecificPart());
        return FileUtils.readFileToString(file, charset.name());
    }


    static public String readFromAbsolutePath(String path) throws IOException {
        return readFromAbsolutePath(path, StandardCharsets.UTF_8);
    }

    static public String readFromAbsolutePath(String path, Charset charset) throws IOException {
        boolean unix = path.startsWith("/");
        boolean windows = Objects.equals(path.charAt(2), ':');
        SysEx.falseThrow(unix || windows, "path should start with '/' in unix like os, or c:\\ or d:\\, " +
                "but the param is" + path);
        URI uri = null;
        try {
            uri = new URI(StringUtils.replace(path, " ", "%20"));
        } catch (URISyntaxException ignore) {}
        File file = new File(Objects.requireNonNull(uri).getSchemeSpecificPart());
        return FileUtils.readFileToString(file, charset.name());
    }

}
