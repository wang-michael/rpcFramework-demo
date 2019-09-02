package com.michaelwang.serialization.common;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author jiuwang.wjw
 */
public class FDateJsonSerializer extends JsonSerializer<Date> {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    @Override public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        jsonGenerator.writeString(date != null ? DATE_FORMAT.format(date) : "null");
    }

}
