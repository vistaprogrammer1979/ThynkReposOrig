/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Claim;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface ClaimFacadeLocal {

    void create(Claim claim);

    void edit(Claim claim);

    void remove(Claim claim);

    Claim find(Object id);

    List<Claim> findAll();

    List<Claim> findRange(int[] range);

    int count();
    
}
