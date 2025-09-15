/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author smutlak
 */
@Entity
@Table(name = "ACCUMED_FACILITY_SCHEMA")
@SequenceGenerator(name="CUST_SEQ_ACCUMED_FACILITY_SCHEMA", sequenceName="CUST_SEQ_ACCUMED_FACILITY_SCHEMA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AccumedFacilitySchema.findAll", query = "SELECT a FROM AccumedFacilitySchema a"),
    @NamedQuery(name = "AccumedFacilitySchema.findById", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.id = :id"),
    @NamedQuery(name = "AccumedFacilitySchema.findByFacilityLicense", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.facilityLicense = :facilityLicense"),
    @NamedQuery(name = "AccumedFacilitySchema.findByHaadInsuranceLic", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.haadInsuranceLic = :haadInsuranceLic"),
    @NamedQuery(name = "AccumedFacilitySchema.findByDhaInsuranceLic", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.dhaInsuranceLic = :dhaInsuranceLic"),
    @NamedQuery(name = "AccumedFacilitySchema.findByEffectiveDate", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.effectiveDate = :effectiveDate"),
    @NamedQuery(name = "AccumedFacilitySchema.findByExpiryDate", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.expiryDate = :expiryDate"),
    @NamedQuery(name = "AccumedFacilitySchema.findByType", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.type = :type"),
    @NamedQuery(name = "AccumedFacilitySchema.findByRegulatorSchema", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.regulatorSchema = :regulatorSchema"),
    @NamedQuery(name = "AccumedFacilitySchema.findByUsedInsuranceLicense", query = "SELECT a FROM AccumedFacilitySchema a WHERE a.usedInsuranceLicense = :usedInsuranceLicense")})
public class AccumedFacilitySchema implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_ACCUMED_FACILITY_SCHEMA")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    private Integer id;
    @Size(max = 50)
    @Column(name = "FACILITY_LICENSE")
    private String facilityLicense;
    @Size(max = 50)
    @Column(name = "HAAD_INSURANCE_LIC")
    private String haadInsuranceLic;
    @Size(max = 50)
    @Column(name = "DHA_INSURANCE_LIC")
    private String dhaInsuranceLic;
    @Column(name = "EFFECTIVE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    @Column(name = "EXPIRY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    @Column(name = "TYPE")
    private Integer type;
    @Column(name = "REGULATOR_SCHEMA")
    private Integer regulatorSchema;
    @Size(max = 50)
    @Column(name = "USED_INSURANCE_LICENSE")
    private String usedInsuranceLicense;

    public AccumedFacilitySchema() {
    }

    public AccumedFacilitySchema(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFacilityLicense() {
        return facilityLicense;
    }

    public void setFacilityLicense(String facilityLicense) {
        this.facilityLicense = facilityLicense;
    }

    public String getHaadInsuranceLic() {
        return haadInsuranceLic;
    }

    public void setHaadInsuranceLic(String haadInsuranceLic) {
        this.haadInsuranceLic = haadInsuranceLic;
    }

    public String getDhaInsuranceLic() {
        return dhaInsuranceLic;
    }

    public void setDhaInsuranceLic(String dhaInsuranceLic) {
        this.dhaInsuranceLic = dhaInsuranceLic;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRegulatorSchema() {
        return regulatorSchema;
    }

    public void setRegulatorSchema(Integer regulatorSchema) {
        this.regulatorSchema = regulatorSchema;
    }

    public String getUsedInsuranceLicense() {
        return usedInsuranceLicense;
    }

    public void setUsedInsuranceLicense(String usedInsuranceLicense) {
        this.usedInsuranceLicense = usedInsuranceLicense;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccumedFacilitySchema)) {
            return false;
        }
        AccumedFacilitySchema other = (AccumedFacilitySchema) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.AccumedFacilitySchema[ id=" + id + " ]";
    }
    
}
