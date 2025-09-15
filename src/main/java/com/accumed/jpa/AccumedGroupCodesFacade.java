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
 * @author smutlak
 */
@Stateless
public class AccumedGroupCodesFacade extends AbstractFacade<AccumedGroupCodes> {
    @PersistenceContext(unitName = "accumedPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AccumedGroupCodesFacade() {
        super(AccumedGroupCodes.class);
    }
    
}
