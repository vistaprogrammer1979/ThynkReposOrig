/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Header;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author smutlak
 */
@Stateless
public class HeaderFacade extends AbstractFacade<Header> implements HeaderFacadeLocal {
    @PersistenceContext(unitName = "LoggingPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HeaderFacade() {
        super(Header.class);
    }
    
}
