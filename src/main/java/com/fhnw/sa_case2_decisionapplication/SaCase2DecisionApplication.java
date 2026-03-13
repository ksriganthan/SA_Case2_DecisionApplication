package com.fhnw.sa_case2_decisionapplication;

import com.fhnw.sa_case2_decisionapplication.Data.DecisionArgs;
import com.fhnw.sa_case2_decisionapplication.RuleEngine.RuleEngineLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaCase2DecisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaCase2DecisionApplication.class, args);
        DecisionArgs testConsignment = new DecisionArgs();
        testConsignment.setWeight(100L);
        testConsignment.setDestinationCountry(DecisionArgs.DestinationCountry.ARG);

        RuleEngineLauncher launcher = new RuleEngineLauncher();
        launcher.makeDecision(testConsignment);

        System.out.println("DecisionType: " + testConsignment.getDecisionType());
        System.out.println("ShippingMethod: " + testConsignment.getShippingMethod());
        System.out.println("Carrier: " + testConsignment.getCarrier());
        System.out.println("RuleId: " + testConsignment.getRuleId());
    }

}
