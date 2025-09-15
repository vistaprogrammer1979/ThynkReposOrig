/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.accumed.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 *
 * @author smutlak
 */
@Entity
@Table(name = "ACCUMED_CODE_GROUPS")
@SequenceGenerator(name="CUST_SEQ_ACCUMED_CODE_GROUPS", sequenceName="CUST_SEQ_ACCUMED_CODE_GROUPS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AccumedCodeGroups.findAll", query = "SELECT a FROM AccumedCodeGroups a"),
    @NamedQuery(name = "AccumedCodeGroups.findById", query = "SELECT a FROM AccumedCodeGroups a WHERE a.id = :id"),
    @NamedQuery(name = "AccumedCodeGroups.findByName", query = "SELECT a FROM AccumedCodeGroups a WHERE a.name = :name"),
    @NamedQuery(name = "AccumedCodeGroups.findByDescription", query = "SELECT a FROM AccumedCodeGroups a WHERE a.description = :description"),
    @NamedQuery(name = "AccumedCodeGroups.findByType", query = "SELECT a FROM AccumedCodeGroups a WHERE a.type = :type"),
    @NamedQuery(name = "AccumedCodeGroups.findByVersion", query = "SELECT a FROM AccumedCodeGroups a WHERE a.version = :version"),
    @NamedQuery(name = "AccumedCodeGroups.findByCreationDate", query = "SELECT a FROM AccumedCodeGroups a WHERE a.creationDate = :creationDate"),
    @NamedQuery(name = "AccumedCodeGroups.findByCreatedBy", query = "SELECT a FROM AccumedCodeGroups a WHERE a.createdBy = :createdBy"),
    @NamedQuery(name = "AccumedCodeGroups.findByModifyDate", query = "SELECT a FROM AccumedCodeGroups a WHERE a.modifyDate = :modifyDate"),
    @NamedQuery(name = "AccumedCodeGroups.findByModifyBy", query = "SELECT a FROM AccumedCodeGroups a WHERE a.modifyBy = :modifyBy")})
public class AccumedCodeGroups implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(generator="CUST_SEQ_ACCUMED_CODE_GROUPS")
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    private Integer id;
    @Size(max = 50)
    @Column(name = "NAME")
    private String name;
    @Size(max = 50)
    @Column(name = "DESCRIPTION")
    private String description;
    @Size(max = 50)
    @Column(name = "TYPE")
    private String type;
    @Size(max = 50)
    @Column(name = "VERSION")
    private String version;
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
    @OneToMany(mappedBy = "parentId")
    private Collection<AccumedCodeGroups> accumedCodeGroupsCollection;
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID")
    @ManyToOne
    private AccumedCodeGroups parentId;
    @OneToMany(cascade = CascadeType.ALL, fetch= FetchType.EAGER, orphanRemoval=true, mappedBy = "groupId")
    private Collection<AccumedGroupCodes> accumedGroupCodesCollection;
     
    public AccumedCodeGroups() {
    }

    public AccumedCodeGroups(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    @XmlTransient
    public Collection<AccumedCodeGroups> getAccumedCodeGroupsCollection() {
        return accumedCodeGroupsCollection;
    }

    public void setAccumedCodeGroupsCollection(Collection<AccumedCodeGroups> accumedCodeGroupsCollection) {
        this.accumedCodeGroupsCollection = accumedCodeGroupsCollection;
    }

    public AccumedCodeGroups getParentId() {
        return parentId;
    }

    public void setParentId(AccumedCodeGroups parentId) {
        this.parentId = parentId;
    }

  

    @XmlTransient
    
    public Collection<AccumedGroupCodes> getAccumedGroupCodesCollection() {
        return accumedGroupCodesCollection;
    }

    public void setAccumedGroupCodesCollection(Collection<AccumedGroupCodes> accumedGroupCodesCollection) {
        this.accumedGroupCodesCollection = accumedGroupCodesCollection;
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
        if (!(object instanceof AccumedCodeGroups)) {
            return false;
        }
        AccumedCodeGroups other = (AccumedCodeGroups) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.accumed.jpa.AccumedCodeGroups[ id=" + id + " ]";
    }
    
}
