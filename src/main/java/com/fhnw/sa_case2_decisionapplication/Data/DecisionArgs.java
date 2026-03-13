package com.fhnw.sa_case2_decisionapplication.Data;


public class DecisionArgs {
	public enum DecisionType {
		AUTOMATIC,
		MANUAL
	}

	public enum ShippingMethod {
		SPECIAL, NORMAL, AIR
	}

	public enum DestinationCountry {
		ARG, JAP, DE, CH, RUS
	}

	private Integer weight;
	private DestinationCountry destinationCountry; // Land
	private DecisionType decisionType; // Flag
	private ShippingMethod shippingMethod; // Action
	private String carrier;
	private Integer ruleId;


	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public DestinationCountry getDestinationCountry() {
		return destinationCountry;
	}
	public void setDestinationCountry(DestinationCountry destinationCountry) {
		this.destinationCountry = destinationCountry;
	}
	public DecisionType getDecisionType() {
		return decisionType;
	}
	public void setDecisionType(DecisionType decisionType) {
		this.decisionType = decisionType;
	}

	public ShippingMethod getShippingMethod() {
		return shippingMethod;
	}
	public void setShippingMethod(ShippingMethod shippingMethod) {
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