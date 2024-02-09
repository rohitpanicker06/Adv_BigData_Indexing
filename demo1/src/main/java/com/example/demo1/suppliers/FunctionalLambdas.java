package com.example.demo1.suppliers;

import com.example.demo1.constants.SupplierConstants;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionalLambdas {

    public static final Supplier<String> schemaFileNameSupplier = ()-> SupplierConstants.SCHEMA_NAME;

    public static final Supplier<String> redisHostNameSupplier = ()-> SupplierConstants.REDIS_HOSTNAME;

    public static final Supplier<Integer> redisPortNumberSupplier = ()-> SupplierConstants.REDIS_PORT;

    public static final Supplier<String> ifNoneMatchHeaderSupplier = ()-> SupplierConstants.NONE_MATCHED_HEADER;

    public static final Function<JSONObject, String> planKeyGeneratorFunction = (jsonObject)-> jsonObject.optString(SupplierConstants.OBJECT_TYPE) + "_" + jsonObject.optString(SupplierConstants.OBJECT_ID);

    public static final Supplier<ResponseEntity<Object>> planExistsResponseSupplier = () -> ResponseEntity.status(HttpStatus.CONFLICT).body(new JSONObject().put("error", "plan already exists!").toString());

    public static final Function<String, String> planIdGeneratorFunction = (planId)->  "plan_" + planId;


}
