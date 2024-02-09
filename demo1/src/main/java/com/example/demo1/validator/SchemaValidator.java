package com.example.demo1.validator;

import com.example.demo1.suppliers.FunctionalLambdas;
import jakarta.annotation.PostConstruct;
import org.everit.json.schema.ValidationException;
import org.springframework.stereotype.Service;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import java.util.function.Function;

@Service
public class SchemaValidator {
    private final Function<String, Schema> schemaLoader;
    private Schema schema;

    public SchemaValidator() {
        this.schemaLoader = this::loadSchema;
    }

    @PostConstruct
    public void init() {
        this.schema = schemaLoader.apply(FunctionalLambdas.schemaFileNameSupplier.get());
    }

    private Schema loadSchema(String path) {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Unable to locate Schema file " + path);
            }
            JSONObject jsonSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.load(jsonSchema);
        } catch (Exception e) {
            throw new IllegalStateException("Error while loading schema file", e);
        }
    }

    public void validateJson(JSONObject jsonObject) throws ValidationException {
        if (this.schema == null) {
            throw new IllegalStateException("Schema has not been initialized");
        }
        this.schema.validate(jsonObject);
    }
}
