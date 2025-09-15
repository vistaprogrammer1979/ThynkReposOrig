/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Activity;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface ActivityFacadeLocal {

    void create(Activity activity);

    void edit(Activity activity);

    void remove(Activity activity);

    Activity find(Object id);

    List<Activity> findAll();

    List<Activity> findRange(int[] range);

    int count();
    
}
