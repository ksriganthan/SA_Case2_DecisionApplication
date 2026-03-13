package com.fhnw.sa_case2_decisionapplication.Service;
import com.fhnw.sa_case2_decisionapplication.Data.Decision;
import com.fhnw.sa_case2_decisionapplication.Data.DecisionArgs;
import com.fhnw.sa_case2_decisionapplication.RuleEngine.RuleEngineLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DecisionService {

    @Autowired
    private RuleEngineLauncher ruleEngineLauncher;

    public Decision validateConsignment(DecisionArgs decisionArgs) {
        if (decisionArgs.getDestinationCountry() == null) {
            throw new IllegalArgumentException("Destination Country is missing or empty");
        }
        if (decisionArgs.getWeight() == null || decisionArgs.getWeight() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }

        DecisionArgs responseDecisionArgs = ruleEngineLauncher.makeDecision(decisionArgs);

        Decision decision = new Decision();
        decision.setDecisionType(responseDecisionArgs.getDecisionType());
        decision.setShippingMethod(responseDecisionArgs.getShippingMethod());
        decision.setCarrier(responseDecisionArgs.getCarrier());
        decision.setRuleId(responseDecisionArgs.getRuleId());

        return decision;
    }
}


