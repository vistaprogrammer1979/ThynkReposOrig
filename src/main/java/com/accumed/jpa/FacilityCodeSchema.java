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
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author smutlak
 */
@Entity
@Table(name = "BS_FACILITY_CODE_SCHEMA")
@SequenceGenerator(name="CUST_SEQ_BS_FACILITY_CODE_SCHEMA", sequenceName="CUST_SEQ_BS_FACILITY_CODE_SCHEMA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FacilityCodeSchema.findAll", query = "SELECT a FROM FacilityCodeSchema a  "),
    @NamedQuery(name = "FacilityCodeSchema.findNotDeleted", query = "SELECT a FROM FacilityCodeSchema a WHERE a.deleted = '0' "),
    @NamedQuery(name = "FacilityCodeSchema.findById", query = "SELECT a FROM FacilityCodeSchema a WHERE a.id = :id"),
    @NamedQuery(name = "FacilityCodeSchema.findByFacilityLicense", query = "SELECT a FROM FacilityCodeSchema a WHERE a.facilityLicense = :facilityLicense"),
    @NamedQuery(name = "FacilityCodeSchema.findByEffectiveDate", query = "SELECT a FROM FacilityCodeSchema a WHERE a.effectiveDate = :effectiveDate"),
    @NamedQuery(name = "FacilityCodeSchema.findByExpiryDate", query = "SELECT a FROM FacilityCodeSchema a WHERE a.expiryDate = :expiryDate"),
    @NamedQuery(name = "FacilityCodeSchema.findByType", query = "SELECT a FROM FacilityCodeSchema a WHERE a.type = :type"),
    @NamedQuery(name = "FacilityCodeSchema.findByRegulatorSchema", query = "SELECT a FROM FacilityCodeSchema a WHERE a.regulatorSchema = :regulatorSchema"),
    @NamedQuery(name = "FacilityCodeSchema.findByInsuranceLicense", query = "SELECT a FROM FacilityCodeSchema a WHERE a.insuranceLicense = :insuranceLicense")})
public class FacilityCodeSchema implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_BS_FACILITY_CODE_SCHEMA")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    private Integer id;
    @Size(max = 50)
    @Column(name = "FACILITY_LICENSE")
    private String facilityLicense;
    @Size(max = 50)
     @Column(name = "INSURANCE_LICENSE")
    private String insuranceLicense;
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
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "modify_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modefiedDate;
    @Column(name = "modify_by")
    private Integer modifyBy;
     @Column(name = "IsDeleted")
    private boolean deleted;

    public FacilityCodeSchema() {
    }

    public FacilityCodeSchema(Integer id) {
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

    public String getInsuranceLicense() {
        return insuranceLicense;
    }

    public void setInsuranceLicense(String insuranceLicense) {
        this.insuranceLicense = insuranceLicense;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModefiedDate() {
        return modefiedDate;
    }

    public void setModefiedDate(Date modefiedDate) {
        this.modefiedDate = modefiedDate;
    }

    public Integer getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(Integer modifyBy) {
        this.modifyBy = modifyBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
        if (!(object instanceof FacilityCodeSchema)) {
            return false;
        }
        FacilityCodeSchema other = (FacilityCodeSchema) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.FacilityCodeSchema[ id=" + id + " ]";
    }
    
}
