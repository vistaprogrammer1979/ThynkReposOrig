/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.re.agents;

import com.accumed.re.agents.repo.CachedData;
import com.accumed.re.agents.repo.CachedRepository;
import com.accumed.re.pool.WorkersFactory;
import com.accumed.webservices.AccumedValidatorWS;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author smutlak
 */
public class CachedRepositoryService implements Runnable {

//    @Resource(name = "pricingDB")
//    private Connection pricingDB;
    private CachedRepository repo;
    private Date repoDay;

    public CachedRepositoryService() {

    }

    @Override
    public void run() {
        try {
//            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "CachedRepository task running at " + new Date());
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "CachedRepository task running at {0}", new Date());
            if (repo == null) {
                repo = new CachedRepository();
                initialize(null);
                populate();

            } else {
                if (repo.isValid()
                        && AccumedValidatorWS.getWorkersPool().getNumActive() <= AccumedValidatorWS.getWorkersPool().getNumIdle()) {
                    int tablesChangedCount = checkSynchronization();

                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "Check Cached Repository returned {0} out of date tables.", tablesChangedCount);
                    if (repoDay == null) {
                        repoDay = RepoUtils.getMidnightYesterday();
                    } else if (repoDay.before(RepoUtils.getMidnightYesterday())) {
                        Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "*****it is MidNight :) ---> Refresh the whole repository ");
                        repoDay = RepoUtils.getMidnightYesterday();
                        repo = null;
                        //System.gc();
                        repo = new CachedRepository();
                        initialize(null);
                        populate();
//                        AccumedValidatorWS.getWorkersPool().clear();
                    }
                } else if(AccumedValidatorWS.getWorkersPool().getNumActive() <= AccumedValidatorWS.getWorkersPool().getNumIdle()){
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "Cached Repository is invalid re-synchronize...");
                    int tablesChangedCount = reSynchronize();
//                    if (tablesChangedCount > 0 && 
//                            AccumedValidatorWS.getWorkersPool().getNumActive() <= AccumedValidatorWS.getWorkersPool().getNumIdle()) {
//                        AccumedValidatorWS.InvalidateWorkers();
//                    } 
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "reSynchronizing {0} tables.", tablesChangedCount);
                }
            }
        } catch (Throwable e) {
            //Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, "Exception={0}\nMessage={1}\nLocalizedMessage={2}\nCause={3}", new Object[]{e.toString(), e.getMessage(), e.getLocalizedMessage(), e.getCause().toString()});
            Logger.getLogger(DroolsUpdaterService.class.getName()).log(Level.SEVERE, "Exception {0}{1}", new Object[]{e.toString(), RepoUtils.stackTraceToString(e)});
        }
    }

    private Connection getAccumedDB() {
        DataSource ds = null;
        Connection con = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/accumedDS");
            //ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/accumedDS");
            con = ds.getConnection();
            if (con.getTransactionIsolation() != Connection.TRANSACTION_READ_UNCOMMITTED) {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");
            } else {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");
            }
        } catch (NamingException ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    private void initialize(String logicalName) {
        Logger.getLogger(DroolsUpdaterService.class.getName()).log(Level.INFO, "CachedRepositoryService.initialize...");
        Connection dbConn = getAccumedDB();

        try {
            if (logicalName != null) {
                java.lang.reflect.Method method = RepoUtils.class.getMethod("get" + logicalName, Connection.class, String.class);
                repo.addCachedData(logicalName, (CachedData) method.invoke(null, dbConn, logicalName));
            } else {
                String[] data = new String[]{
                    "Clinicians","ClinicianCategories","Facilities","Payers", "CodeGroups",//"ICDExclusions",
                    "AAPC_CPT_AGE_GENDER_MATERNITY","IncompatibleCodesList","AAPC_CPT","AAPC_NCCI_CPTS_List",
                    "InsurerPackages", 
                    "MarketFacilities", "ProfessionDiagnosis","ActivityQuantityLimit",  
                    "FacilityNetworks", 
                    "NaCptsDos", "AddOnCodes", "RecieverPayers", "FacilityCodeSchema", "FacilityReceiverSchema",
                    "DhaDrugPrices", "HaadCPTCodes", "HaadDentalCodes",
                    "HaadDRGCodes", "HaadDrugCodes", "HaadHCPCSCodes", "HaadICDCodes",
                    "HaadServiceCodes", "DhaCPTCodes", "DhaDentalCodes", "DhaDrugCodes",
                    "DhaHCPCSCodes", "DhaICDCodes", "DhaServiceCodes","ICDContradictories", 
                    "CrossWalk_CPT","ICDExclusionMaster","ICDAcuteChronic","CrosswalkExclusion","STTFacilityNetworkMap","ICD10_AdditionalCodes"
                        };
                /* "DhaDrugPrices","DeletedDrugs","LOINCS", "HaadCPTCodes", "HaadDentalCodes",
                    "HaadDRGCodes", "HaadDrugCodes", "HaadHCPCSCodes", "HaadICDCodes",
                    "HaadServiceCodes", "DhaCPTCodes", "DhaDentalCodes", "DhaDrugCodes",
                    "DhaHCPCSCodes", "DhaICDCodes", "DhaServiceCodes","FacilitySchemas","ACode_OValue" 
                *//*Deleted by Rasha 01-06-2020/*//*not used Any more*/
                
                
                //14/May/2019 smutlak   remove --> , "ICDExclusion"

                /*insertDentalCodeRequireToothNo*/ /*global variable*//*done*
                /*insertDiagnosisGroups*/ /*global variable*//*done*//*problem*
                /*insertCPTGroups*/ /*global variable*//*done*/
                /*insertDamanBasicDrugs*/ /*global variable*/ /*deleted*
                /*insertDamanBasicExclusions*/ /*global variable*//*Problem*/

                for (String dataName : data) {
                    Logger.getLogger(DroolsUpdaterService.class.getName()).
                            log(Level.INFO, "Calling get{0}...", dataName);
                    java.lang.reflect.Method method = com.accumed.re.agents.RepoUtils.class.getMethod("get" + dataName, Connection.class, String.class);
                    repo.addCachedData(dataName, (CachedData) method.invoke(null, dbConn, dataName));
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
                dbConn = null;
            }
        }
    }

    private void populate() {
        com.accumed.re.pool.Worker.setCachedRepositoryService(this);
        AccumedValidatorWS.setCachedRepositoryService(this);
        DroolsUpdaterService.setCachedRepositoryService(this);
        WorkersFactory.setCachedRepositoryService(this);
    }

    public int reSynchronize() {
        Logger.getLogger(DroolsUpdaterService.class.getName()).log(Level.INFO, "CachedRepositoryService.reSynchronize...");
        int ret = 0;
        Connection dbConn = null;
        try {
            dbConn = getAccumedDB();
            for (Map.Entry<String, CachedData> entry : repo.getCachedDB().entrySet()) {
                CachedData cachedData = entry.getValue();
                if (Status.INVALID == cachedData.getStatus()) {
                    this.initialize(entry.getKey());
                    ret++;
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.INFO, "{0} reSynchronized.", entry.getKey());

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
                dbConn = null;
            }
        }
        return ret;
    }

    private int checkSynchronization() {
        Logger.getLogger(DroolsUpdaterService.class.getName()).log(Level.INFO, "CachedRepositoryService.checkSynchronization...");
        Connection dbConn = null;
        try {
            dbConn = getAccumedDB();
            return repo.checkSynchronization(dbConn);
        } catch (Exception ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
                dbConn = null;
            }
        }
        return 0;
    }

    public CachedRepository getRepo() {
        return repo;
    }
}
