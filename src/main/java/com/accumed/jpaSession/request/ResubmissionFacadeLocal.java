/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Resubmission;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface ResubmissionFacadeLocal {

    void create(Resubmission resubmission);

    void edit(Resubmission resubmission);

    void remove(Resubmission resubmission);

    Resubmission find(Object id);

    List<Resubmission> findAll();

    List<Resubmission> findRange(int[] range);

    int count();
    
}
