package com.fhnw.sa_case2_decisionapplication.RestAPI;


import com.fhnw.sa_case2_decisionapplication.Data.Decision;
import com.fhnw.sa_case2_decisionapplication.Data.DecisionArgs;
import com.fhnw.sa_case2_decisionapplication.Service.DecisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/decision")
public class DecisionAPI {

    @Autowired
    private DecisionService decisionService;

    @PostMapping(value = "/make", produces = "application/json")
    public ResponseEntity<?> makeDecision(@RequestBody DecisionArgs decisionArgs) {
        try {
            Decision decision = decisionService.validateConsignment(decisionArgs);
            return ResponseEntity.ok(decision);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Rule engine failure: " + e.getMessage());
        }
    }
}

