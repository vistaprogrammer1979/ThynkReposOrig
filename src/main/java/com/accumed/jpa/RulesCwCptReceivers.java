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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author smutlak
 */
@Entity
@Table(name = "RULES_CW_CPT_RECEIVERS")
@SequenceGenerator(name="CUST_SEQ_RULES_CW_CPT_RECEIVERS", sequenceName="CUST_SEQ_RULES_CW_CPT_RECEIVERS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RulesCwCptReceivers.findAll", query = "SELECT r FROM RulesCwCptReceivers r"),
    @NamedQuery(name = "RulesCwCptReceivers.findById", query = "SELECT r FROM RulesCwCptReceivers r WHERE r.id = :id"),
    @NamedQuery(name = "RulesCwCptReceivers.findByCpt", query = "SELECT r FROM RulesCwCptReceivers r WHERE r.cpt = :cpt"),
    @NamedQuery(name = "RulesCwCptReceivers.findByCptAndVersion", query = "SELECT r FROM RulesCwCptReceivers r WHERE r.cpt = :cpt AND r.icdsVersion = :icdsVersion"),
    @NamedQuery(name = "RulesCwCptReceivers.findByReceiverLicense", query = "SELECT r FROM RulesCwCptReceivers r WHERE r.receiverLicense = :receiverLicense")})
public class RulesCwCptReceivers implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_RULES_CW_CPT_RECEIVERS")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "CPT", nullable = false, length = 256)
    private String cpt;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "receiver_license", nullable = false, length = 256)
    private String receiverLicense;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "icds_version", nullable = false)
    private int icdsVersion;
    
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "modify_by")
    private Integer modifyBy;
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column(name = "modify_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyDate;

    public RulesCwCptReceivers() {
    }

    public RulesCwCptReceivers(Integer id) {
        this.id = id;
    }

    public RulesCwCptReceivers(Integer id, String cpt, String receiverLicense) {
        this.id = id;
        this.cpt = cpt;
        this.receiverLicense = receiverLicense;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCpt() {
        return cpt;
    }

    public void setCpt(String cpt) {
        this.cpt = cpt;
    }

    public String getReceiverLicense() {
        return receiverLicense;
    }

    public void setReceiverLicense(String receiverLicense) {
        this.receiverLicense = receiverLicense;
    }

    public int getIcdsVersion() {
        return icdsVersion;
    }

    public void setIcdsVersion(int icdsVersion) {
        this.icdsVersion = icdsVersion;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(Integer modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
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
        if (!(object instanceof RulesCwCptReceivers)) {
            return false;
        }
        RulesCwCptReceivers other = (RulesCwCptReceivers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.RulesCwCptReceivers[ id=" + id + " ]";
    }
    
}
