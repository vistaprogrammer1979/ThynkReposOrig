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
@Table(name = "BS_FACILITY_RECEIVER_SCHEMA")
@SequenceGenerator(name="CUST_SEQ_BS_FACILITY_RECEIVER_SCHEMA", sequenceName="CUST_SEQ_BS_FACILITY_RECEIVER_SCHEMA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FacilityReceiverSchema.findAll", query = "SELECT a FROM FacilityReceiverSchema a  "),
    @NamedQuery(name = "FacilityReceiverSchema.findNotDeleted", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.deleted = '0' "),
    @NamedQuery(name = "FacilityReceiverSchema.findById", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.id = :id"),
    @NamedQuery(name = "FacilityReceiverSchema.findByFacilityLicense", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.facilityLicense = :facilityLicense"),
    @NamedQuery(name = "FacilityReceiverSchema.findByInsuranceLic", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.insuranceLic = :insuranceLic"),
    @NamedQuery(name = "FacilityReceiverSchema.findByEffectiveDate", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.effectiveDate = :effectiveDate"),
    @NamedQuery(name = "FacilityReceiverSchema.findByExpiryDate", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.expiryDate = :expiryDate"),

    @NamedQuery(name = "FacilityReceiverSchema.findByUsedInsuranceLicense", query = "SELECT a FROM FacilityReceiverSchema a WHERE a.usedInsuranceLicense = :usedInsuranceLicense")})
public class FacilityReceiverSchema implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_BS_FACILITY_RECEIVER_SCHEMA")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    private Integer id;
    
    @Size(max = 50)
    @Column(name = "FACILITY_LICENSE")
    private String facilityLicense;
    
    @Size(max = 50)
    @Column(name = "INSURANCE_LIC")
    private String insuranceLic;

    
    @Column(name = "EFFECTIVE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    
    @Column(name = "EXPIRY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    
    @Size(max = 50)
    @Column(name = "USED_INSURANCE_LICENSE")
    private String usedInsuranceLicense;
    
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

    public FacilityReceiverSchema() {
    }

    public FacilityReceiverSchema(Integer id) {
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

    public String getInsuranceLic() {
        return insuranceLic;
    }

    public void setInsuranceLic(String insuranceLic) {
        this.insuranceLic = insuranceLic;
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

    public String getUsedInsuranceLicense() {
        return usedInsuranceLicense;
    }

    public void setUsedInsuranceLicense(String usedInsuranceLicense) {
        this.usedInsuranceLicense = usedInsuranceLicense;
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
        if (!(object instanceof FacilityReceiverSchema)) {
            return false;
        }
        FacilityReceiverSchema other = (FacilityReceiverSchema) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.FacilityReceiverSchema[ id=" + id + " ]";
    }

}
