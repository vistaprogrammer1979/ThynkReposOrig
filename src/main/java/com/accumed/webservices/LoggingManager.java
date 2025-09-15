/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accumed.webservices;

/**
 *
 * @author This pc
 */
public class LoggingManager {
     private boolean logInfo = false; // Default value
     private boolean logRequest = false; // Default value

    public boolean isLogRequest() {
        return logRequest;
    }

    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }

    public boolean isLogInfoEnabled() {
        return logInfo;
    }

    public void setLogInfoEnabled(boolean logInfo) {
        this.logInfo = logInfo;
    }

    // Singleton instance
    private static final LoggingManager INSTANCE = new LoggingManager();

    public static LoggingManager getInstance() {
        
        return INSTANCE;
    }
}
