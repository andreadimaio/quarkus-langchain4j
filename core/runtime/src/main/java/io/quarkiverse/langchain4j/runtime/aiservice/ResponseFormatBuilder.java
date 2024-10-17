package io.quarkiverse.langchain4j.runtime.aiservice;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.output.JsonSchemas;

public class ResponseFormatBuilder {

    private final JsonSchema jsonSchema;
    private final ResponseFormatType type;

    public ResponseFormatBuilder() {
        this.jsonSchema = null;
        this.type = ResponseFormatType.TEXT;
    }

    public ResponseFormatBuilder(Type returnType) {
        Optional<JsonSchema> jsonSchema = JsonSchemas.jsonSchemaFrom(returnType);
        if (jsonSchema.isPresent()) {
            this.jsonSchema = jsonSchema.get();
            this.type = ResponseFormatType.JSON;
        } else {
            this.jsonSchema = null;
            this.type = ResponseFormatType.TEXT;
        }
    }

    public ResponseFormat build(Set<Capability> capabilities) {
        return switch(type) {
            case JSON -> {
                if (capabilities.contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA)) {
                    yield ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(jsonSchema)
                        .build();
                }
                yield ResponseFormat.JSON;
            }
            case TEXT -> ResponseFormat.TEXT;
        };
    }
}
