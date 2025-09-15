/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.controllers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author smutlak
 */
public class ScrubRequestJpaController {

    static EntityManagerFactory emf;

    public ScrubRequestJpaController() {
        emf = Persistence.createEntityManagerFactory("LoggingPU");
    }

    public void persist(Object object) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Object find(Integer id) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(com.accumed.model.scrubRequest.ScrubRequest.class, id);
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }
}
