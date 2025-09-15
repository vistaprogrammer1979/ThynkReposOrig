/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Workflow;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface WorkflowFacadeLocal {

    void create(Workflow workflow);

    void edit(Workflow workflow);

    void remove(Workflow workflow);

    Workflow find(Object id);

    List<Workflow> findAll();

    List<Workflow> findRange(int[] range);

    int count();
    
}
