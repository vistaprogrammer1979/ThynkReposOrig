/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpa;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author smutlak
 */
@Stateless
public class RulesCwCptReceiversFacade extends AbstractFacade<RulesCwCptReceivers> {
    @PersistenceContext(unitName = "accumedPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RulesCwCptReceiversFacade() {
        super(RulesCwCptReceivers.class);
    }
    
    public List<RulesCwCptReceivers> findByCptAndVersion(String cptCode, int icdsVersion){
         Query query = em.createNamedQuery("RulesCwCptReceivers.findByCptAndVersion", RulesCwCptReceivers.class);
         query.setParameter("cpt", cptCode);
         query.setParameter("icdsVersion", icdsVersion);
         return query.getResultList();
    }
    
}
