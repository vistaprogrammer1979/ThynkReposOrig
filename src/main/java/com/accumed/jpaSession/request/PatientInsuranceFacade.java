/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.PatientInsurance;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author smutlak
 */
@Stateless
public class PatientInsuranceFacade extends AbstractFacade<PatientInsurance> implements PatientInsuranceFacadeLocal {
    @PersistenceContext(unitName = "LoggingPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PatientInsuranceFacade() {
        super(PatientInsurance.class);
    }
    
}
