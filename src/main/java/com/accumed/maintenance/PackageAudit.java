/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance;

import   java.util.Date;

/**
 *
 * @author Anas
 */
public class PackageAudit {
    int ID;
    int OBJ_TYPE;
    String OBJ_NAME;
    String UPDATE_DATE;
    int UPDATE_USER_ID;
    String UPDATE_USER_NAME;

    public PackageAudit() {
    }
    
    public PackageAudit(int ID, int OBJ_TYPE, String OBJ_NAME, String UPDATE_DATE, int UPDATE_USER_ID, String UPDATE_USER_NAME) {
        this.ID = ID;
        this.OBJ_TYPE = OBJ_TYPE;
        this.OBJ_NAME = OBJ_NAME;
        this.UPDATE_DATE = UPDATE_DATE;
        this.UPDATE_USER_ID = UPDATE_USER_ID;
        this.UPDATE_USER_NAME = UPDATE_USER_NAME;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setOBJ_TYPE(int OBJ_TYPE) {
        this.OBJ_TYPE = OBJ_TYPE;
    }

    public void setOBJ_NAME(String OBJ_NAME) {
        this.OBJ_NAME = OBJ_NAME;
    }

    public void setUPDATE_DATE(String UPDATE_DATE) {
        this.UPDATE_DATE = UPDATE_DATE;
    }

    public void setUPDATE_USER_ID(int UPDATE_USER_ID) {
        this.UPDATE_USER_ID = UPDATE_USER_ID;
    }

    public void setUPDATE_USER_NAME(String UPDATE_USER_NAME) {
        this.UPDATE_USER_NAME = UPDATE_USER_NAME;
    }

    public int getID() {
        return ID;
    }

    public int getOBJ_TYPE() {
        return OBJ_TYPE;
    }

    public String getOBJ_NAME() {
        return OBJ_NAME;
    }

    public String getUPDATE_DATE() {
        return UPDATE_DATE;
    }

    public int getUPDATE_USER_ID() {
        return UPDATE_USER_ID;
    }

    public String getUPDATE_USER_NAME() {
        return UPDATE_USER_NAME;
    }

    
    
    
}
