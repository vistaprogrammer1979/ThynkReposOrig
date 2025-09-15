/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author wfakhra
 */
@Stateless
public class FacilityRecieverSchemaFacade extends AbstractFacade<FacilityReceiverSchema> {
    @PersistenceContext(unitName = "accumedPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public FacilityRecieverSchemaFacade() {
        super(FacilityReceiverSchema.class);
    }

    public void remove(FacilityReceiverSchema selectedRow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
