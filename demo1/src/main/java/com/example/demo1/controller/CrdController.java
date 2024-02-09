package com.example.demo1.controller;


import com.example.demo1.service.PlanService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> deletePlan(@PathVariable String planId) {
        return planService.deletePlanById(planId);
    }


}
