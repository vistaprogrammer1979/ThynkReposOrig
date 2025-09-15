/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "ACCUMED_GROUP_CODES")
@SequenceGenerator(name="CUST_SEQ_ACCUMED_GROUP_CODES", sequenceName="CUST_SEQ_ACCUMED_GROUP_CODES")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AccumedGroupCodes.findAll", query = "SELECT a FROM AccumedGroupCodes a"),
    @NamedQuery(name = "AccumedGroupCodes.findById", query = "SELECT a FROM AccumedGroupCodes a WHERE a.id = :id"),
    @NamedQuery(name = "AccumedGroupCodes.findByGroupId", query = "SELECT a FROM AccumedGroupCodes a WHERE a.groupId = :groupId"),
    @NamedQuery(name = "AccumedGroupCodes.findByFrom", query = "SELECT a FROM AccumedGroupCodes a WHERE a.from = :from"),
    @NamedQuery(name = "AccumedGroupCodes.findByTo", query = "SELECT a FROM AccumedGroupCodes a WHERE a.to = :to"),
    @NamedQuery(name = "AccumedGroupCodes.findByCreationDate", query = "SELECT a FROM AccumedGroupCodes a WHERE a.creationDate = :creationDate"),
    @NamedQuery(name = "AccumedGroupCodes.findByCreatedBy", query = "SELECT a FROM AccumedGroupCodes a WHERE a.createdBy = :createdBy"),
    @NamedQuery(name = "AccumedGroupCodes.findByModifyDate", query = "SELECT a FROM AccumedGroupCodes a WHERE a.modifyDate = :modifyDate"),
    @NamedQuery(name = "AccumedGroupCodes.findByModifyBy", query = "SELECT a FROM AccumedGroupCodes a WHERE a.modifyBy = :modifyBy")})
public class AccumedGroupCodes implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_ACCUMED_GROUP_CODES")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    private Integer id;
    @Size(max = 25)
    @Column(name = "[FROM]")
    private String from;
    @Size(max = 25)
    @Column(name = "[TO]")
    private String to;
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "modify_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyDate;
    @Column(name = "modify_by")
    private Integer modifyBy;
    @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AccumedCodeGroups groupId;

    public AccumedGroupCodes() {
    }

    public AccumedGroupCodes(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Integer getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(Integer modifyBy) {
        this.modifyBy = modifyBy;
    }

    public AccumedCodeGroups getGroupId() {
        return groupId;
    }

    public void setGroupId(AccumedCodeGroups groupId) {
        this.groupId = groupId;
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
        if (!(object instanceof AccumedGroupCodes)) {
            return false;
        }
        AccumedGroupCodes other = (AccumedGroupCodes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.AccumedGroupCodes[ id=" + id + " ]";
    }
    
}
