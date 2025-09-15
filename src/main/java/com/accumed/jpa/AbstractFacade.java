/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.jpa;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 *
 * @author smutlak
 */
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    private boolean constraintValidationsDetected(T entity) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        javax.validation.Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
        if (constraintViolations.size() > 0) {
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation<T> cv = iterator.next();
                Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, 
                        "constraintValidationsDetected_EXCEPTION:{0}.{1} {2}", new Object[]{cv.getRootBeanClass().getName(), cv.getPropertyPath(), cv.getMessage()});
            }
            return true;
        } else {
            return false;
        }
    }

    public void create(T entity) {
        if (!constraintValidationsDetected(entity)) {
        EntityManager em = getEntityManager();
        em.persist(entity);
        em.flush();

    }
        //EntityManager em = emf.createEntityManager();
        /*EntityManager em = getEntityManager();
         EntityTransaction tx = em.getTransaction();
         try {
            
         tx.begin();
         em.persist(entity);
         tx.commit();
            
         } catch (Exception e) {
         tx.rollback();
         Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
         em.getTransaction().rollback();
         } finally {
         em.close();
         }*/
    }

    public void edit(T entity) {
        if (!constraintValidationsDetected(entity)) {
            getEntityManager().merge(entity);
        }
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    public void batchCreate(List list) {
        EntityManager em = getEntityManager();;
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        for (Object t : list) {
            em.persist(t);
        }
        em.flush();
        tx.commit();
        em.close();
    }

}
