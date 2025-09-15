/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.ScrubRequest;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

/**
 *
 * @author smutlak
 */
@Stateless
public class RequestFacade extends AbstractFacade<ScrubRequest> implements RequestFacadeLocal {
    @PersistenceContext(unitName = "LoggingPU")
    private EntityManager em;
    
    @Resource(mappedName="jdbc/accumedDS")
    private DataSource dataSource;
    
    @Resource(mappedName="jdbc/RulesLoggingDS")
    private DataSource rulesDataSource;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RequestFacade() {
        super(ScrubRequest.class);
    }
    
    @Override
    public DataSource getQueryDataSource() {
        return dataSource;
        //return null;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getRulesDataSource() {
        return rulesDataSource;
    }

    public void setRulesDataSource(DataSource rulesDataSource) {
        this.rulesDataSource = rulesDataSource;
    }
    
    @Override
    public List<ScrubRequest> findByClaimIdCaller(Integer claimIdCaller){
        List results = getEntityManager().createNamedQuery("ScrubRequest.findByClaimIdCaller")
                    .setParameter("idCaller", claimIdCaller)
                    .getResultList();
        return results;
    }
    
}
