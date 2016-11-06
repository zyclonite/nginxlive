/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Created by zyclonite on 25/03/15.
 */
public class SyslogNullLongDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final JsonToken t = jsonParser.getCurrentToken();
        if(t == JsonToken.VALUE_STRING) {
            final String value = jsonParser.getValueAsString();
            if (value.equals("-")) {
                return null;
            }
        }
        return jsonParser.getValueAsLong();
    }
}
