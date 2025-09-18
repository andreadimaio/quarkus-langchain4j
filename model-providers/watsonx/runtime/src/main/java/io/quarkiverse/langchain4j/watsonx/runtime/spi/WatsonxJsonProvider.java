package io.quarkiverse.langchain4j.watsonx.runtime.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonx.ai.core.spi.json.JsonProvider;
import com.ibm.watsonx.ai.core.spi.json.TypeToken;

import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder;

public class WatsonxJsonProvider implements JsonProvider {

    private static final ObjectMapper MAPPER = SnakeCaseObjectMapperHolder.MAPPER;

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T fromJson(String json, TypeToken<T> type) {
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructType(type.getType());
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String prettyPrint(Object value) {
        try {
            if (value instanceof String str)
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree((str)));
            else
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }
}
