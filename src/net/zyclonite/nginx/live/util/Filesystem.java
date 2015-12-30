/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.util;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by zyclonite on 29/10/15.
 */
public class Filesystem {
    public static final String TMP_DIR_BASE = ".vertx";

    public static String createTempDir() {
        final File tmpDir = new File(TMP_DIR_BASE);
        if(!tmpDir.exists() || !tmpDir.isDirectory()) {
            if (!tmpDir.mkdirs()) {
                throw new IllegalStateException("Failed to create tmp dir");
            }
        }
        return TMP_DIR_BASE;
    }

    public static JsonObject readConfig(final String configFileName) throws FileNotFoundException {
        final JsonObject config;
        try (final Scanner scanner = new Scanner(new File(configFileName)).useDelimiter("\\A")){
            final String sconf = scanner.next();
            try {
                config = new JsonObject(sconf);
            } catch (final DecodeException e) {
                throw new DecodeException("Configuration file " + sconf + " does not contain a valid JSON object");
            }
        } catch (final FileNotFoundException e) {
            throw new FileNotFoundException(configFileName+" not found");
        }
        return config;
    }
}
