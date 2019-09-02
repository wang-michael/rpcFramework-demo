package com.michaelwang.serialization.serializer.impl;

import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.michaelwang.serialization.common.FDateJsonDeserializer;
import com.michaelwang.serialization.common.FDateJsonSerializer;
import com.michaelwang.serialization.serializer.ISerializer;

/**
 * @author jiuwang.wjw
 */
public class JSONSerializer implements ISerializer {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule module = new SimpleModule("DateTimeModule", Version.unknownVersion());
        module.addSerializer(Date.class, new FDateJsonSerializer());
        module.addDeserializer(Date.class, new FDateJsonDeserializer());

        objectMapper.registerModule(module);

    }

    private static ObjectMapper getObjectMapperInstance() {
        return objectMapper;
    }


    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            return new byte[0];
        }

        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String json = new String(data);
        try {
            return (T) objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
