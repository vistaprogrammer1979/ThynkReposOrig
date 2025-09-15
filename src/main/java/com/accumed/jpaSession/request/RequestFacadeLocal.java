/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.ScrubRequest;
import java.util.List;
import javax.ejb.Local;
import javax.sql.DataSource;
import javax.persistence.EntityManager;

/**
 *
 * @author smutlak
 */
@Local
public interface RequestFacadeLocal {

    ScrubRequest save(ScrubRequest claimValidation);
    
    void create(ScrubRequest request);

    void edit(ScrubRequest request);

    void remove(ScrubRequest request);

    ScrubRequest find(Object id);

    List<ScrubRequest> findAll();

    List<ScrubRequest> findRange(int[] range);

    int count();
    
    public DataSource getQueryDataSource();
    public DataSource getRulesDataSource();
    public List<ScrubRequest> findByClaimIdCaller(Integer claimIdCaller);
    
}
