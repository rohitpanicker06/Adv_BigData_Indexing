package com.example.demo1.service;

import com.example.demo1.constants.SupplierConstants;
import com.example.demo1.dao.PlanDAO;
import com.example.demo1.errors.custom.pattern.factory.ApiResponse;
import com.example.demo1.errors.custom.pattern.factory.ApiResponseFactory;
import com.example.demo1.suppliers.FunctionalLambdas;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class PlanManagementService {

    @Autowired
    private PlanDAO planDAO;

    @Autowired
    private ValidationService validationService;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public ResponseEntity<Object> validateAndCreatePlan(JSONObject planJson) {
        try {
            validationService.validateJson(planJson);
            String planKey = FunctionalLambdas.planKeyGeneratorFunction.apply(planJson);
            return checkIfKeyExists(planKey) ? getPlanExistsResponse() : createPlan(planJson, planKey);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseFactory.getSchemaValidationError(e));
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseFactory.getUriSyntaxExpcetion());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkIfKeyExists(String key) {
        return planDAO.checkIfExists(key);
    }

    private ResponseEntity<Object> getPlanExistsResponse() {
        return FunctionalLambdas.planExistsResponseSupplier.get();
    }
    private ResponseEntity<Object> createPlan(JSONObject planJson, String planKey) throws Exception {
        String eTag = savePlan(planKey, planJson.toString());
        URI location = new URI(SupplierConstants.URI_PLAN + planJson.optString(SupplierConstants.OBJECT_ID));
        saveKeyValuePairs(objectMapper.readTree(planJson.toString()));
        return ResponseEntity.created(location).eTag(eTag).body(ApiResponseFactory.getPlanCreatedSuccessfully(planJson.optString("objectId")));
    }
    public String savePlan(String key, String planJsonString) {
        String newETag = DigestUtils.md5Hex(planJsonString);
        planDAO.set(key, planJsonString);
        planDAO.set(key+ "_"+SupplierConstants.E_TAG, newETag);
        return newETag;
    }

    public boolean deletePlan(String key) {
        return (planDAO.del(key) == 1 && planDAO.del(key +"_"+SupplierConstants.E_TAG)==1);
    }

    public String getETag(String key) {
        return planDAO.get(key+ "_" +SupplierConstants.E_TAG);
    }

    public JSONObject getPlanByKey(String key) {
        String planString = planDAO.get(key);
        return new JSONObject(planString);
    }

    public ResponseEntity<Object> getPlanById( HttpHeaders headers, String planId){
        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        if (!checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getPlanNotFoundError(key));
        }

        String oldETag = getETag(key);
        String receivedETag = headers.getFirst(FunctionalLambdas.ifNoneMatchHeaderSupplier.get());
        if (receivedETag != null && receivedETag.replace("\"", "").equals(oldETag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(oldETag)
                    .body(ApiResponseFactory.getPlantNotModifiedResponse(key));
        }
        String plan = getPlanByKey(key).toString();
        return ResponseEntity.ok().eTag(oldETag).body(plan);
    }


    public ResponseEntity<Object> deletePlanById(String planId) throws Exception {
        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        if (!checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getPlanNotFoundError(key));
        }
        JSONObject jsonPlan = getPlanByKey(key);

        if (deletePlan(key)) {
            deleteKeyValuePairs(objectMapper.readTree(jsonPlan.toString()));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().body(ApiResponseFactory.getInternalServerError());

    }

    public ResponseEntity<Object> selfPathMethhod(String planId,String planObject, HttpHeaders headers)
    {
        if (planObject == null || planObject.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getEmptyBodyRequestError());
        }

        JSONObject plan =null;

        try {
            plan = new JSONObject(planObject);
            validationService.validateJson(plan);
        } catch (ValidationException | JSONException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseFactory.getSchemaValidationError(e));
        }

        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        ResponseEntity<Object> existingPlan = getPlanById(headers,planId);
        if(existingPlan.getStatusCode()!= HttpStatusCode.valueOf(200))
        {
            return existingPlan;
        }

        ResponseEntity<Object> result = processPlanETag(key,headers);
        if(result.getStatusCode() != HttpStatusCode.valueOf(200))
        {
            return result;
        }

        ResponseEntity<Object> updatedResult =null;

        try {
            JsonNode originalNode = objectMapper.readTree(existingPlan.getBody().toString());
            JsonNode updateNode = objectMapper.readTree(planObject);
            merge(originalNode, updateNode);
            String updatedData = objectMapper.writeValueAsString(originalNode);
            deleteKeyValuePairs(objectMapper.readTree(existingPlan.getBody().toString()));
            updatedResult= createPlan(new JSONObject(updatedData), FunctionalLambdas.planIdGeneratorFunction.apply(planId));
            saveKeyValuePairs(updateNode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error occurred while updating data");
        }
        return ResponseEntity.ok().eTag(updatedResult.getHeaders().getETag()).build();
    }

    private static void merge(JsonNode mainNode, JsonNode updateNode) {
        updateNode.fieldNames().forEachRemaining((fieldName) -> {
            JsonNode originalValue = mainNode.get(fieldName);
            JsonNode updatedValue = updateNode.get(fieldName);

            if (originalValue != null && originalValue.isObject()) {
                merge(originalValue, updatedValue);
            }
            else if ("linkedPlanServices".equals(fieldName) && originalValue.isArray() && updatedValue.isArray()) {
                mergeLinkedPlanServices((ArrayNode) originalValue, (ArrayNode) updatedValue);
            }
            else if (mainNode instanceof ObjectNode) {
                ((ObjectNode) mainNode).replace(fieldName, updatedValue);
            }
        });
    }

    private static void mergeLinkedPlanServices(ArrayNode originalServices, ArrayNode updatedServices) {
        HashSet<String> existingObjectIds = new HashSet<>();
        originalServices.forEach(service -> {
            if (service.has("objectId")) {
                existingObjectIds.add(service.get("objectId").asText());
            }
        });

        updatedServices.forEach(updatedService -> {
            String updatedObjectId = updatedService.get("objectId").asText();
            if (existingObjectIds.contains(updatedObjectId)) {
                for (int i = 0; i < originalServices.size(); i++) {
                    JsonNode originalService = originalServices.get(i);
                    if (originalService.get("objectId").asText().equals(updatedObjectId)) {
                        merge(originalService, updatedService);
                    }
                }
            } else {
                originalServices.add(updatedService);
            }
        });
    }


//    public ResponseEntity<Object> newPathMethhod(String id,String requestBody, HttpHeaders headers)
//    {
//
//        String internalID = id;
//        System.out.println(internalID);
//        String value = String.valueOf(getPlanByKey(FunctionalLambdas.planIdGeneratorFunction.apply(id)));
//
//        if (value == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "No Data Found").toString());
//        }
//        String eTag = headers.getIfMatch().get(0);
//        eTag = eTag.replace("\"", "");
//        System.out.println(eTag);
//        String newEtag = DigestUtils.md5Hex(value);
//        String latestEtag = DigestUtils.md5Hex(requestBody);
//        System.out.println("PATCH is " + newEtag);
//        System.out.println(requestBody);
//        if (eTag == null || eTag == "" || !eTag.equals(newEtag)) {
//            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(new JSONObject().put("error", "the plan was modified!").toString());
//        }
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            // Get the old node from redis using the object Id
//            JsonNode oldNode = objectMapper.readTree(value);
//            // redisService.populateNestedData(oldNode, null);
//            value = oldNode.toString();
//            // Construct the new node from the input body
//            String inputData = requestBody;
//            JsonNode newNode = objectMapper.readTree(inputData);
//            ArrayNode planServicesNew = (ArrayNode) newNode.get("linkedPlanServices");
//            Set<JsonNode> planServicesSet = new HashSet<>();
//            Set<String> objectIds = new HashSet<String>();
//            planServicesNew.addAll((ArrayNode) oldNode.get("linkedPlanServices"));
//            for (JsonNode node : planServicesNew) {
//                Iterator<Map.Entry<String, JsonNode>> sitr = node.fields();
//                while (sitr.hasNext()) {
//                    Map.Entry<String, JsonNode> val = sitr.next();
//                    if (val.getKey().equals("objectId")) {
//                        if (!objectIds.contains(val.getValue().toString())) {
//                            planServicesSet.add(node);
//                            objectIds.add(val.getValue().toString());
//                        }
//                    }
//                }
//            }
//            planServicesNew.removeAll();
//            if (!planServicesSet.isEmpty())
//                planServicesSet.forEach(s -> {
//                    planServicesNew.add(s);
//                });
//            planDAO.set(internalID, newNode.toString());
//            // planservice.deleteKeyValuePairs(oldNode);
//            saveKeyValuePairs(newNode);
//            String existingPlan = String.valueOf(getPlanByKey(internalID));
//            latestEtag = DigestUtils.md5Hex(existingPlan);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", "Invalid Data!").toString());
//        }
//
//        return ResponseEntity.ok().eTag(latestEtag)
//                .body(new JSONObject().put("message", "Patched data with key:" + internalID).toString());
//    }

    private static String generateKey(JsonNode childNode, JsonNode parentNode) {
        String childPart = childNode.has("objectType") && childNode.has("objectId")
                ? childNode.get("objectType").asText() + ":" + childNode.get("objectId").asText()
                : "";

        if (parentNode != null && parentNode.has("objectType") && parentNode.has("objectId")) {
            String parentPart = parentNode.get("objectType").asText() + ":" + parentNode.get("objectId").asText();
            return parentPart + ":" + childPart;
        } else {
            return childPart; // Root node or no parent info
        }
    }

    public void saveKeyValuePairs(JsonNode rootNode) throws Exception {
        if (rootNode.has("linkedPlanServices")) {
            for (JsonNode serviceNode : rootNode.get("linkedPlanServices")) {
                storeJsonObject(serviceNode);
                if (serviceNode.has("linkedService")) {
                    storeJsonObject( serviceNode.get("linkedService"));
                }
                if (serviceNode.has("planserviceCostShares")) {
                    storeJsonObject( serviceNode.get("planserviceCostShares"));
                }
            }
        }
    }

    public void deleteKeyValuePairs(JsonNode rootNode) throws Exception {
        if (rootNode.has("linkedPlanServices")) {
            for (JsonNode serviceNode : rootNode.get("linkedPlanServices")) {
                deleteJsonObject(serviceNode);
                if (serviceNode.has("linkedService")) {
                    deleteJsonObject( serviceNode.get("linkedService"));
                }
                if (serviceNode.has("planserviceCostShares")) {
                    deleteJsonObject( serviceNode.get("planserviceCostShares"));
                }
            }
        }
    }

    private  void storeJsonObject(JsonNode jsonObject) throws Exception {
        if (jsonObject.has("objectType") && jsonObject.has("objectId")) {
            String key = jsonObject.get("objectType").asText() + ":" + jsonObject.get("objectId").asText();
            String value = jsonObject.toString();
            planDAO.set(key, value);
        }
    }

    private  void deleteJsonObject(JsonNode jsonObject) throws Exception {
        if (jsonObject.has("objectType") && jsonObject.has("objectId")) {
            String key = jsonObject.get("objectType").asText() + ":" + jsonObject.get("objectId").asText();
            planDAO.del(key);
        }
    }

    public ResponseEntity<Object> putPlan(String planId, String planObject, HttpHeaders headers) throws Exception {
        if (planObject == null || planObject.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getEmptyBodyRequestError());
        }

        JSONObject plan =null;

        try {
            plan = new JSONObject(planObject);
            validationService.validateJson(plan);
        } catch (ValidationException | JSONException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseFactory.getSchemaValidationError(e));
    }

        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        ResponseEntity<Object> existingPlan = getPlanById(headers,planId);
        if(existingPlan.getStatusCode()!= HttpStatusCode.valueOf(200))
        {
            return createPlan(new JSONObject(planObject), key);
        }

        ResponseEntity<Object> result = processPlanETag(key,headers);
        if(result.getStatusCode() != HttpStatusCode.valueOf(200))
        {
            return result;
        }

       ResponseEntity<Object> newlyCreatedResponse = createPlan(plan, key);
        if(newlyCreatedResponse.getStatusCode()!=HttpStatusCode.valueOf(201))
        {
            return newlyCreatedResponse;
        }

        String updatedEtag = newlyCreatedResponse.getHeaders().getETag();
        URI location = new URI(SupplierConstants.URI_PLAN + plan.optString(SupplierConstants.OBJECT_ID));

        return ResponseEntity.created(location).eTag(updatedEtag).body(ApiResponseFactory.getPlanUpdatedSuccessfully(plan.optString("objectId")));

    }


    private ResponseEntity<Object> processPlanETag(String planKey, HttpHeaders headers)
    {

        String previousEtag = getETag(planKey);
        if(!processRequestEtag(headers))
        {
            return ResponseEntity.badRequest().body(ApiResponseFactory.getEtagParsingException(planKey));
        }else{
            if(headers.getIfMatch().size() == 0)
            {
               return ResponseEntity.badRequest().body(ApiResponseFactory.getEtagMissing(planKey));
            }

            if(!headers.getIfMatch().get(0).replaceAll("^\"|\"$", "").equals(previousEtag))
            {
               return ResponseEntity.status(412).body(ApiResponseFactory.getPreConditionFailedForEtag(planKey,headers.getETag()));
            }
        }

        return ResponseEntity.ok().build();
    }

    private boolean processRequestEtag( HttpHeaders headers)
    {

        try {
            List<String> ifMatch = headers.getIfMatch();
        } catch (Exception e) {
            return  false;
        }
        return  true;
    }


}
