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
public class RulesCwIcdFacade extends AbstractFacade<RulesCwIcd> {
    @PersistenceContext(unitName = "accumedPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RulesCwIcdFacade() {
        super(RulesCwIcd.class);
    }
    
    public List<RulesCwIcd> findByCptAndVersion(String cptCode, int icdsVersion){
         Query query = em.createNamedQuery("RulesCwIcd.findByCptAndVersion", RulesCwIcd.class);
         query.setParameter("cpt", cptCode);
         query.setParameter("icdsVersion", icdsVersion);
         return query.getResultList();
    }
}
