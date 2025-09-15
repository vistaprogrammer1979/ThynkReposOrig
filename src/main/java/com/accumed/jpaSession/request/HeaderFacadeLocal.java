/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.Header;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface HeaderFacadeLocal {

    void create(Header header);

    void edit(Header header);

    void remove(Header header);

    Header find(Object id);

    List<Header> findAll();

    List<Header> findRange(int[] range);

    int count();
    
}
