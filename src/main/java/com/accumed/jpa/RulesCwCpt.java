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
@Table(name = "RULES_CW_CPT")
@SequenceGenerator(name="CUST_SEQ_RULES_CW_CPT", sequenceName="CUST_SEQ_RULES_CW_CPT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RulesCwCpt.findAll", query = "SELECT r FROM RulesCwCpt r"),
    @NamedQuery(name = "RulesCwCpt.findById", query = "SELECT r FROM RulesCwCpt r WHERE r.id = :id"),
    @NamedQuery(name = "RulesCwCpt.findByCpt", query = "SELECT r FROM RulesCwCpt r WHERE r.cpt = :cpt"),
    @NamedQuery(name = "RulesCwCpt.findByCptAndVersion", query = "SELECT r FROM RulesCwCpt r WHERE r.cpt = :cpt AND r.icdsVersion = :icdsVersion"),
    @NamedQuery(name = "RulesCwCpt.findByCptAndVersionAndCPTType", query = "SELECT r FROM RulesCwCpt r WHERE r.cpt = :cpt AND r.icdsVersion = :icdsVersion And r.type = :type"),
    @NamedQuery(name = "RulesCwCpt.findByIcdsVersion", query = "SELECT r FROM RulesCwCpt r WHERE r.icdsVersion = :icdsVersion"),
    @NamedQuery(name = "RulesCwCpt.findBySeverity", query = "SELECT r FROM RulesCwCpt r WHERE r.severity = :severity"),
    @NamedQuery(name = "RulesCwCpt.findByRefLink", query = "SELECT r FROM RulesCwCpt r WHERE r.refLink = :refLink"),
    @NamedQuery(name = "RulesCwCpt.findByDocumentation", query = "SELECT r FROM RulesCwCpt r WHERE r.documentation = :documentation"),
    @NamedQuery(name = "RulesCwCpt.findByShortMsg", query = "SELECT r FROM RulesCwCpt r WHERE r.shortMsg = :shortMsg"),
    @NamedQuery(name = "RulesCwCpt.findByLongMsg", query = "SELECT r FROM RulesCwCpt r WHERE r.longMsg = :longMsg"),
    @NamedQuery(name = "RulesCwCpt.findByCreatedBy", query = "SELECT r FROM RulesCwCpt r WHERE r.createdBy = :createdBy"),
    @NamedQuery(name = "RulesCwCpt.findByModifyBy", query = "SELECT r FROM RulesCwCpt r WHERE r.modifyBy = :modifyBy"),
    @NamedQuery(name = "RulesCwCpt.findByCreationDate", query = "SELECT r FROM RulesCwCpt r WHERE r.creationDate = :creationDate"),
    @NamedQuery(name = "RulesCwCpt.findByModifyDate", query = "SELECT r FROM RulesCwCpt r WHERE r.modifyDate = :modifyDate")})
public class RulesCwCpt implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_RULES_CW_CPT")
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
    @Column(name = "severity")
    private Integer severity;   
    @Column(name = "CPT_TYPE")
     private int type;    
    @Size(max = 1073741823)
    @Column(name = "refLink", length = 1073741823)
    private String refLink;
    @Size(max = 1073741823)
    @Column(name = "documentation", length = 1073741823)
    private String documentation;
    @Size(max = 256)
    @Column(name = "shortMsg", length = 256)
    private String shortMsg;
    @Size(max = 1073741823)
    @Column(name = "longMsg", length = 1073741823)
    private String longMsg;
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

    public RulesCwCpt() {
    }

    public RulesCwCpt(Integer id) {
        this.id = id;
    }

    public RulesCwCpt(Integer id, String cpt, int icdsVersion) {
        this.id = id;
        this.cpt = cpt;
        this.icdsVersion = icdsVersion;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getRefLink() {
        return refLink;
    }

    public void setRefLink(String refLink) {
        this.refLink = refLink;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getShortMsg() {
        return shortMsg;
    }

    public void setShortMsg(String shortMsg) {
        this.shortMsg = shortMsg;
    }

    public String getLongMsg() {
        return longMsg;
    }

    public void setLongMsg(String longMsg) {
        this.longMsg = longMsg;
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
        if (!(object instanceof RulesCwCpt)) {
            return false;
        }
        RulesCwCpt other = (RulesCwCpt) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.RulesCwCpt[ id=" + id + " ]";
    }
    
}
