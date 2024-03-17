package com.example.demo1.service;

import com.example.demo1.errors.custom.pattern.factory.ApiResponseFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.Optional;

@Service
public class PlanService {

   @Autowired
   PlanManagementService planManagementService;

    public ResponseEntity<Object> createPlan(Optional<String> planString)
    {
        return planString.filter(s->!s.isEmpty())
                .map(JSONObject::new).map(planManagementService::validateAndCreatePlan)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseFactory.getEmptyBodyRequestError()));
    }

    public ResponseEntity<Object> getPlan(
            @RequestHeader HttpHeaders headers,
            @PathVariable String planId
    ) {
        return planManagementService.getPlanById(headers, planId);
    }

    public ResponseEntity<Object> deletePlanById(@PathVariable String planId) throws Exception {
        return planManagementService.deletePlanById(planId);

    }

    public ResponseEntity<Object> patchPlanById(@PathVariable String objectId,
                                                @RequestBody(required = false) String planObject,
                                                @RequestHeader HttpHeaders headers) throws URISyntaxException {
        return planManagementService.selfPathMethhod(objectId,planObject,headers);
    }

    public ResponseEntity<Object> putPlanById(String planId, String planObject, HttpHeaders headers) throws Exception {
        return planManagementService.putPlan(planId,planObject, headers);
    }


}
