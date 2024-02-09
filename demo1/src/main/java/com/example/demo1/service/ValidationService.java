package com.example.demo1.service;

import com.example.demo1.validator.SchemaValidator;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    @Autowired
    private SchemaValidator schemaValidator;

    public void validateJson(JSONObject planJson) throws ValidationException {
        schemaValidator.validateJson(planJson);
    }
}
