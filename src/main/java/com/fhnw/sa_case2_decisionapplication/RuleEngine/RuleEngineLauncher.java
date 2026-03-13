package com.fhnw.sa_case2_decisionapplication.RuleEngine;


import com.fhnw.sa_case2_decisionapplication.Data.DecisionArgs;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Component;

@Component
public class RuleEngineLauncher {

    public DecisionArgs makeDecision(DecisionArgs decisionArgs) {
        System.setProperty("drools.dialect.mvel.strict", "false");

        KieServices kieServices = KieServices.Factory.get();

        Resource dt = ResourceFactory.newClassPathResource("rules/ShippingRules.drl.xls", getClass());
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Fehler beim Kompilieren der Regeln: "
                    + kieBuilder.getResults().getMessages());
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        KieSession kieSession = kieContainer.newKieSession();

        decisionArgs.setDecisionType(DecisionArgs.DecisionType.MANUAL);
        kieSession.insert(decisionArgs);
        int firedRules = kieSession.fireAllRules();
        kieSession.dispose();

        if (firedRules > 0) {
            System.out.println("Anzahl gefeuerte Regeln: " + firedRules);
            System.out.println("DecisionType: " + decisionArgs.getDecisionType());
            System.out.println("ShippingMethod: " + decisionArgs.getShippingMethod());
            System.out.println("Carrier: " + decisionArgs.getCarrier());
            System.out.println("RuleId: " + decisionArgs.getRuleId());
        } else {
            System.out.println("Keine Regel hat gegriffen – manuelle Entscheidung erforderlich.");
        }

        return decisionArgs;
    }

}
