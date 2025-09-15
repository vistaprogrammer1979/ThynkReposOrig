/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpaSession.request;

import com.accumed.model.scrubRequest.PatientInsurance;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author smutlak
 */
@Local
public interface PatientInsuranceFacadeLocal {

    void create(PatientInsurance patientInsurance);

    void edit(PatientInsurance patientInsurance);

    void remove(PatientInsurance patientInsurance);

    PatientInsurance find(Object id);

    List<PatientInsurance> findAll();

    List<PatientInsurance> findRange(int[] range);

    int count();
    
}
