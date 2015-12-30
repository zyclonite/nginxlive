/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.util;

import io.vertx.core.json.JsonObject;

/**
 * Created by zyclonite on 30/12/15.
 */
public class JsonObjectHelper {
    public static void putNonNull(final JsonObject jsonObject, final String key, final Object value) {
        if(value != null) {
            jsonObject.put(key, value);
        }
    }
}
