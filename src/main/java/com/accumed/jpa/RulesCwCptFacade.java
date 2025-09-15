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
public class RulesCwCptFacade extends AbstractFacade<RulesCwCpt> {
    @PersistenceContext(unitName = "accumedPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RulesCwCptFacade() {
        super(RulesCwCpt.class);
    }
    
    public List<RulesCwCpt> findByCptAndVersion(String cptCode, int icdsVersion){
         Query query = em.createNamedQuery("RulesCwCpt.findByCptAndVersion", RulesCwCpt.class);
         query.setParameter("cpt", cptCode);
         query.setParameter("icdsVersion", icdsVersion);
         return query.getResultList();
    }
    public List<RulesCwCpt> findByCptAndVersionAndCptType(String cptCode, int icdsVersion, int cptType){
         Query query = em.createNamedQuery("RulesCwCpt.findByCptAndVersionAndCPTType", RulesCwCpt.class);
         query.setParameter("cpt", cptCode);
         query.setParameter("icdsVersion", icdsVersion);
         query.setParameter("type", cptType);
         return query.getResultList();
    }
}
