package com.example.demo1.controller;


import com.example.demo1.service.PlanService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/plan")
public class CrdController {

    @Autowired
    private PlanService planService;

    @PostMapping(value = "/", produces = "application/json")
    public ResponseEntity<Object> createPlan(@RequestBody Optional<String> planString){
        return planService.createPlan(planString);

    }

    @GetMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> getPlanById(
            @RequestHeader HttpHeaders headers,
            @PathVariable String planId
    ){
        return planService.getPlan(headers, planId);
    }

    @DeleteMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> deletePlan(@PathVariable String planId) throws Exception {
        return planService.deletePlanById(planId);
    }

    @PatchMapping(value = "/{planId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> patchPlan(@PathVariable String planId,
                                       @RequestBody(required = false) String planObject,
                                       @RequestHeader HttpHeaders headers) throws URISyntaxException {
        return planService.patchPlanById(planId,planObject,headers);
    }


    @PutMapping(value="/{plantId}", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> putPlan(@PathVariable String planId, @RequestBody String planObject, @RequestHeader HttpHeaders headers) throws Exception {
        return planService.putPlanById(planId, planObject, headers);
    }



}
