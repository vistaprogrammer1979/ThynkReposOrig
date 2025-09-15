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
@Table(name = "RULES_CW_ICD")
@SequenceGenerator(name="CUST_SEQ_RULES_CW_ICD", sequenceName="CUST_SEQ_RULES_CW_ICD")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RulesCwIcd.findAll", query = "SELECT r FROM RulesCwIcd r"),
    @NamedQuery(name = "RulesCwIcd.findById", query = "SELECT r FROM RulesCwIcd r WHERE r.id = :id"),
    @NamedQuery(name = "RulesCwIcd.findByCpt", query = "SELECT r FROM RulesCwIcd r WHERE r.cpt = :cpt"),
    @NamedQuery(name = "RulesCwIcd.findByIcdsVersion", query = "SELECT r FROM RulesCwIcd r WHERE r.icdsVersion = :icdsVersion"),
    @NamedQuery(name = "RulesCwIcd.findByIcd", query = "SELECT r FROM RulesCwIcd r WHERE r.icd = :icd"),
    @NamedQuery(name = "RulesCwIcd.findByCreatedBy", query = "SELECT r FROM RulesCwIcd r WHERE r.createdBy = :createdBy"),
    @NamedQuery(name = "RulesCwIcd.findByModifyBy", query = "SELECT r FROM RulesCwIcd r WHERE r.modifyBy = :modifyBy"),
    @NamedQuery(name = "RulesCwIcd.findByCreationDate", query = "SELECT r FROM RulesCwIcd r WHERE r.creationDate = :creationDate"),
    @NamedQuery(name = "RulesCwIcd.findByCptAndVersion", query = "SELECT r FROM RulesCwIcd r WHERE r.cpt = :cpt AND r.icdsVersion = :icdsVersion"),
    @NamedQuery(name = "RulesCwIcd.findByModifyDate", query = "SELECT r FROM RulesCwIcd r WHERE r.modifyDate = :modifyDate")})
public class RulesCwIcd implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_RULES_CW_ICD")
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
    @Column(name = "icds_version", nullable = false)
    private int icdsVersion;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "ICD", nullable = false, length = 256)
    private String icd;
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

    public RulesCwIcd() {
    }

    public RulesCwIcd(Integer id) {
        this.id = id;
    }

    public RulesCwIcd(Integer id, String cpt, int icdsVersion, String icd) {
        this.id = id;
        this.cpt = cpt;
        this.icdsVersion = icdsVersion;
        this.icd = icd;
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

    public int getIcdsVersion() {
        return icdsVersion;
    }

    public void setIcdsVersion(int icdsVersion) {
        this.icdsVersion = icdsVersion;
    }

    public String getIcd() {
        return icd;
    }

    public void setIcd(String icd) {
        this.icd = icd;
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
        if (!(object instanceof RulesCwIcd)) {
            return false;
        }
        RulesCwIcd other = (RulesCwIcd) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.RulesCwIcd[ id=" + id + " ]";
    }
    
}
