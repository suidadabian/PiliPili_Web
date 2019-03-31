package com.pilipili;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public final class Util {
    public static Map<String, String> parsePostRequestBody(String body) throws UnsupportedEncodingException {
        String[] keyValuePairs = body.split("&");
        Map<String, String> map = new HashMap<>();
        for (String keyValuePair : keyValuePairs) {
            int index = keyValuePair.indexOf('=');
            String key = keyValuePair.substring(0, index);
            String value = keyValuePair.substring(index + 1, keyValuePair.length());
            map.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
        }
        return map;
    }

    public static String generateErrorJson(int code) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(code)
                .endObject();
        jsonWriter.flush();
        String errorJson = stringWriter.toString();
        jsonWriter.close();
        return errorJson;
    }
}
