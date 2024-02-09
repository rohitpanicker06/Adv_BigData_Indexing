package com.example.demo1.service;

import com.example.demo1.constants.SupplierConstants;
import com.example.demo1.dao.PlanDAO;
import com.example.demo1.errors.custom.pattern.factory.ApiResponseFactory;
import com.example.demo1.suppliers.FunctionalLambdas;
import org.apache.commons.codec.digest.DigestUtils;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class PlanManagementService {

    @Autowired
    private PlanDAO planDAO;

    @Autowired
    private ValidationService validationService;


    public ResponseEntity<Object> validateAndCreatePlan(JSONObject planJson) {
        try {
            validationService.validateJson(planJson);
            String planKey = FunctionalLambdas.planKeyGeneratorFunction.apply(planJson);
            return checkIfKeyExists(planKey) ? getPlanExistsResponse() : createPlan(planJson, planKey);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseFactory.getSchemaValidationError(e));
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseFactory.getUriSyntaxExpcetion());
        }
    }

    public boolean checkIfKeyExists(String key) {
        return planDAO.checkIfExists(key);
    }

    private ResponseEntity<Object> getPlanExistsResponse() {
        return FunctionalLambdas.planExistsResponseSupplier.get();
    }
    private ResponseEntity<Object> createPlan(JSONObject planJson, String planKey) throws URISyntaxException {
        String eTag = savePlan(planKey, planJson.toString());
        URI location = new URI(SupplierConstants.URI_PLAN + planJson.optString(SupplierConstants.OBJECT_ID));
        return ResponseEntity.created(location).eTag(eTag).body(ApiResponseFactory.getPlanCreatedSuccessfully(planJson.optString("objectId")));
    }
    public String savePlan(String key, String planJsonString) {
        String newETag = DigestUtils.md5Hex(planJsonString);
        planDAO.hSet(key, key, planJsonString);
        planDAO.hSet(key, SupplierConstants.E_TAG, newETag);
        return newETag;
    }

    public boolean deletePlan(String key) {
        return planDAO.del(key) == 1;
    }

    public String getETag(String key) {
        return planDAO.hGet(key, SupplierConstants.E_TAG);
    }

    public JSONObject getPlanByKey(String key) {
        String planString = planDAO.hGet(key, key);
        return new JSONObject(planString);
    }

    public ResponseEntity<Object> getPlanById( HttpHeaders headers, String planId){
        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        if (!checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getPlanNotFoundError(key));
        }

        String oldETag = getETag(key);
        String receivedETag = headers.getFirst(FunctionalLambdas.ifNoneMatchHeaderSupplier.get());
        if (receivedETag != null && receivedETag.equals(oldETag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(oldETag)
                    .body(ApiResponseFactory.getPlantNotModifiedResponse(key));
        }
        String plan = getPlanByKey(key).toString();
        return ResponseEntity.ok().eTag(oldETag).body(plan);
    }


    public ResponseEntity<Object> deletePlanById(String planId)
    {
        String key = FunctionalLambdas.planIdGeneratorFunction.apply(planId);
        if (!checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseFactory.getPlanNotFoundError(key));
        }

        if (deletePlan(key)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().body(ApiResponseFactory.getInternalServerError());

    }



}
