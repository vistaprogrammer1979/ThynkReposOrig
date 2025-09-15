/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.jpa.Eclipselink.sequence;

/**
 *
 * @author smutlak
 */
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

public class IdentCurrentSessionCustomizer implements SessionCustomizer {

    public void customize(Session session) throws Exception {
        String[] identCurrentSequences = {
            "CUST_SEQ_ACCUMED_CODE_GROUPS",
            "CUST_SEQ_ACCUMED_FACILITY_SCHEMA",
            "CUST_SEQ_ACCUMED_GROUP_CODES",
            "CUST_SEQ_BS_FACILITY_CODE_SCHEMA",
            "CUST_SEQ_BS_FACILITY_RECEIVER_SCHEMA",
            "CUST_SEQ_RULES_CW_CPT",
            "CUST_SEQ_RULES_CW_CPT_RECEIVERS",
            "CUST_SEQ_RULES_CW_ICD"
        };

        for (int i = 0; i < identCurrentSequences.length; i++) {
            IdentCurrentSequence sequence = new IdentCurrentSequence(identCurrentSequences[i]);
            session.getLogin().addSequence(sequence);
        }
    }
}
