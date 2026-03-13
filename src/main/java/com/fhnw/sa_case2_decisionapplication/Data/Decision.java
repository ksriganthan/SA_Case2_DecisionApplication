package com.fhnw.sa_case2_decisionapplication.Data;

public class Decision {

    private DecisionArgs.DecisionType decisionType; // Flag
    private DecisionArgs.ShippingMethod shippingMethod; // Action
    private String carrier;
    private Integer ruleId;

    public DecisionArgs.DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(DecisionArgs.DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public DecisionArgs.ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(DecisionArgs.ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }
}
