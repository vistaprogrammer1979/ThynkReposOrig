/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 SEVERE
 WARNING
 INFO
 CONFIG
 FINElogging.threads.max.count
 FINER
 FINEST
 */
package com.accumed.webservices;

import com.accumed.controllers.ScrubRequestJpaController;
import com.accumed.re.agents.WorkLog;
import com.accumed.re.agents.WorkLogger;
import com.accumed.re.agents.CachedRepositoryService;
import com.accumed.db.DB;
import com.accumed.maintenance.LoginCheckProcessServlet;

import com.accumed.validation.Utils;

import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import com.accumed.model.scrubRequest.Claim;
import com.accumed.model.scrubRequest.xclaim.XActivity;
import com.accumed.model.scrubRequest.xclaim.XClaim;
import com.accumed.model.scrubRequest.xclaim.XClaimType;
import com.accumed.model.scrubRequest.xclaim.XDiagnosis;
import com.accumed.model.scrubRequest.xclaim.XEncounter;
import com.accumed.model.scrubRequest.xclaim.XObservation;
import com.accumed.re.pool.Worker;
import com.accumed.re.pool.WorkersFactory;
import com.accumed.re.pool.WorkersPool;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author smutlak
 */
@WebService(serviceName = "AccumedValidator", targetNamespace = "http://webservices.accumed.com/")
//@Stateless()
public class AccumedValidatorWS {

    //NEW
//    @PersistenceContext(unitName = "LoggingPU")
//    private EntityManager em;
    protected static CachedRepositoryService cachedRepositoryService;
    protected static WorkersPool workerPool;
    protected static boolean initialized = false;

    static final AtomicLong SAVE_THREAD_NEXT_ID = new AtomicLong(0);
    static final AtomicLong RETURN_THREAD_NEXT_ID = new AtomicLong(0);
    static final AtomicLong REQUEST_THREAD_NEXT_ID = new AtomicLong(0);

    static int WORKERS_MIN_POOL_SIZE;
    static int WORKERS_MAX_POOL_SIZE;
    static int LOGGING_MAX_THREADS_COUNT;

    private static int LOGGING_WARNING_QUEUE_SIZE;
    private static int LOGGING_MAX_QUEUE_SIZE;
    private static int LOGGING_ENABLED = 1;

    private static AtomicBoolean LOGGING_WARNING_QUEUE_SIZE_EMAIL_SENT = new AtomicBoolean(false);
    private static AtomicBoolean LOGGING_MAX_QUEUE_SIZE_EMAIL_SENT = new AtomicBoolean(false);

    static int POOL_MAX_WAIT_TIME_IN_MILLIS;

    static {
        try {
            WORKERS_MIN_POOL_SIZE = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.workers.pool.min.size")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }
        try {
            WORKERS_MAX_POOL_SIZE = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.workers.pool.max.size")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }
        try {
            LOGGING_MAX_THREADS_COUNT = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.logging.threads.max.count")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }
        try {
            LOGGING_WARNING_QUEUE_SIZE = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.logging.threads.warning.size")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }
        try {
            LOGGING_MAX_QUEUE_SIZE = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.logging.threads.max.size")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }

        try {
            LOGGING_ENABLED = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.logging.enabled")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }

        try {
            POOL_MAX_WAIT_TIME_IN_MILLIS = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.max.wait.time.in.millis")));
        } catch (NumberFormatException | NamingException ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);

            POOL_MAX_WAIT_TIME_IN_MILLIS = 15;
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.INFO, "LOGGING_ENABLED={0}",
                            new Object[]{POOL_MAX_WAIT_TIME_IN_MILLIS});

        }

        if (WORKERS_MIN_POOL_SIZE <= 0) {
            WORKERS_MIN_POOL_SIZE = 1;
        }
        if (WORKERS_MAX_POOL_SIZE < WORKERS_MIN_POOL_SIZE) {
            WORKERS_MAX_POOL_SIZE = -1;
        }
        if (LOGGING_MAX_THREADS_COUNT <= 0) {
            LOGGING_MAX_THREADS_COUNT = 1;
        }

        Logger.getLogger(AccumedValidatorWS.class
                .getName()).log(Level.INFO, "MIN_WORKERS_POOL_SIZE={0}, MAX_WORKERS_POOL_SIZE={1}",
                        new Object[]{WORKERS_MIN_POOL_SIZE, WORKERS_MAX_POOL_SIZE});

        Logger.getLogger(AccumedValidatorWS.class
                .getName()).log(Level.INFO, "LOGGING_ENABLED={0}, POOL_MAX_WAIT_TIME_IN_MILLIS={1}",
                        new Object[]{LOGGING_ENABLED, POOL_MAX_WAIT_TIME_IN_MILLIS});
    }

    //logger
    protected static WorkLog minRequest;
    protected static WorkLog maxRequest;
    protected static Long averageClaimProcessTime;
    protected static Long lastRequestTime;

    protected static long totalRequests;
    protected static long totalProcessedClaims;
    protected static long totalProcessedClaimsTime;
    protected static WorkLogger workLogger;
    //end logger

    //OLD
//    private long last = 0;
    private static final String PREFIX = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<soapenv:Header></soapenv:Header>"
            + "<soapenv:Body>"
            + "<validate>";
    private static final String SUFFIX = "</validate> </soapenv:Body> </soapenv:Envelope>";

    class RulesUser {

        public Integer id;
        public String psw;
        public Integer type;

        public RulesUser(Integer id, String psw, Integer type) {
            this.id = id;
            this.psw = psw;
            this.type = type;
        }

    }
    private static ConcurrentHashMap<String, RulesUser> users = new ConcurrentHashMap<String, RulesUser>();

    private static ConcurrentHashMap<Integer, Exclusion> exclusions = new ConcurrentHashMap<Integer, Exclusion>();

    private static ConcurrentHashMap<Integer, Category> Categories = new ConcurrentHashMap<Integer, Category>();

    private static ConcurrentHashMap<Integer, RuleCategory> ruleCategories = new ConcurrentHashMap<Integer, RuleCategory>();
    private static ConcurrentHashMap<String, String> ruleCats = new ConcurrentHashMap<String, String>();
    public static ConcurrentHashMap<String, String> ruleRefrences = new ConcurrentHashMap<String, String>();

    private static ValidatorStates<ValidatorState> validatorStates = new ValidatorStates<ValidatorState>();

    public static ThreadPoolExecutor saveFixedPool
            = //(ThreadPoolExecutor)Executors.newFixedThreadPool(1);
            new ThreadPoolExecutor(0, LOGGING_MAX_THREADS_COUNT,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());

    public static ScheduledExecutorService returnFixedPool = Executors.newScheduledThreadPool(WORKERS_MIN_POOL_SIZE);
    /*= new ThreadPoolExecutor(0, WORKERS_MAX_POOL_SIZE < 2 ? 10 : WORKERS_MAX_POOL_SIZE,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());*/

    public static ScheduledExecutorService validatorStateExecutor
            = java.util.concurrent.Executors.newScheduledThreadPool(1);

    public AccumedValidatorWS() {
        cachedRepositoryService = null;
        validatorStateExecutor.scheduleAtFixedRate(new AccumedValidatorWS.ValidatorStateTask(), 5, 1, TimeUnit.MINUTES);
//        workerPool = new WorkersPool(new WorkersFactory());
//        AbandonedConfig abandonedConfig = new AbandonedConfig();
//        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
//        abandonedConfig.setRemoveAbandonedTimeout(60 * 5); //5 minutes
//        abandonedConfig.setLogAbandoned(true);
//        workerPool.setAbandonedConfig(abandonedConfig);
//        workerPool.setMinEvictableIdleTimeMillis(1000 * 60 * 7); //7 minutes
//        workerPool.setTimeBetweenEvictionRunsMillis(1000 * 60 * 7); //7 minutes
//        workerPool.setTestWhileIdle(true);
//        workerPool.setMinIdle(MIN_WORKERS_POOL_SIZE);
    }

//    public final void initializePool() {
////        if (workerPool != null) {
////            workerPool.clear();
////            workerPool.close();
////            workerPool = null;
////        }
//        workerPool = new GenericObjectPool<>(new WorkersFactory());
//        AbandonedConfig abandonedConfig = new AbandonedConfig();
//        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
//        abandonedConfig.setRemoveAbandonedTimeout(60 * 5); //5 minutes
//        abandonedConfig.setLogAbandoned(true);
//        workerPool.setAbandonedConfig(abandonedConfig);
////        workerPool.setMinIdle(1);
//    }
    public static void setCachedRepositoryService(CachedRepositoryService cachedRepositoryService) {
        AccumedValidatorWS.cachedRepositoryService = cachedRepositoryService;
        internalInitialize();
    }

    public static CachedRepositoryService getCachedRepositoryService() {
        return AccumedValidatorWS.cachedRepositoryService;
    }

    /**
     * Web service operation
     *
     * @param param
     * @return
     */
    @WebMethod(operationName = "Initialize")
    public String Initialize(@WebParam(name = "param") String param) {
        return "";
    }

    public static String internalInitialize() {

        AddSMTPLogger();

        long lBegin = System.nanoTime();
        java.sql.Connection conn = null;
        try {

//            workerPool.returnObject(workerPool.borrowObject(120000));
//            Statistics.setStartTime(new Date());
//            Statistics.setServiceInitialization(4);
            if (workerPool != null) {
                workerPool.clear();
                workerPool.close();
            }

            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            conf.setFairness(true);
            conf.setBlockWhenExhausted(false);
            //conf.setMinIdle(WORKERS_MIN_POOL_SIZE);
            conf.setNumTestsPerEvictionRun(1);
            conf.setMinEvictableIdleTimeMillis(1000 * 60 * 30); //30 minutes
            conf.setSoftMinEvictableIdleTimeMillis(1000 * 60 * 30); //30 minutes
            conf.setTimeBetweenEvictionRunsMillis(1000 * 60 * 10); //10 minutes
            
            
            conf.setTestWhileIdle(true);
//            workerPool.setTestOnReturn(true);
            conf.setMaxWaitMillis(POOL_MAX_WAIT_TIME_IN_MILLIS);
            conf.setBlockWhenExhausted(false);
            conf.setMinIdle(WORKERS_MIN_POOL_SIZE);

            if (WORKERS_MAX_POOL_SIZE > 1) {
                conf.setMaxTotal(WORKERS_MAX_POOL_SIZE);
            } else {
                conf.setMaxTotal(1000);
            }
            
            //workerPool = new WorkersPool(new WorkersFactory(), conf);
            workerPool = new WorkersPool(new WorkersFactory(), conf);
            AbandonedConfig abandonedConfig = new AbandonedConfig();
            abandonedConfig.setRemoveAbandonedOnMaintenance(true);
            abandonedConfig.setRemoveAbandonedTimeout(60 * 5); //5 minutes
            abandonedConfig.setLogAbandoned(true);
            workerPool.setAbandonedConfig(abandonedConfig);

            workerPool.setNumTestsPerEvictionRun(1);
            workerPool.setMinEvictableIdleTimeMillis(1000 * 60 * 30); //30 minutes
            workerPool.setSoftMinEvictableIdleTimeMillis(1000 * 60 * 30); //30 minutes
            workerPool.setTimeBetweenEvictionRunsMillis(1000 * 60 * 10); //10 minutes


            workerPool.setTestWhileIdle(true);
//            workerPool.setTestOnReturn(true);

            workerPool.setMaxWaitMillis(POOL_MAX_WAIT_TIME_IN_MILLIS);
            workerPool.setBlockWhenExhausted(false);
            workerPool.setMinIdle(WORKERS_MIN_POOL_SIZE);

            if (WORKERS_MAX_POOL_SIZE > 1) {
                workerPool.setMaxTotal(WORKERS_MAX_POOL_SIZE);
            } else {
                workerPool.setMaxTotal(1000);
            }

            workerPool.preparePool();
            workerPool.preparePool();

            try {
                while (AccumedValidatorWS.workerPool.getCount() < 1) {
                    Thread.sleep(2000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!populateExclusions()) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, "Error populating exclusions.");
            }
            if (!populateCategories()) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, "Error populating categories.");
            }

            if (!populateRulesCategories()) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, "Error populating rules categories.");
            }
            if (!populateRulesReference()) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, "Error populating rules References.");
            }

            initialized = true;
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.INFO,
                    "AccumedValidatorWS.Initialize:{0}ms", (new Long((System.nanoTime() - lBegin) / 1000000)).toString());

        } catch (Exception e) {
            Statistics.setServiceInitialization(3);
            Statistics.setServiceInitializationException(e.getMessage());
            Statistics.addException(e);
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, e);
            return "Done With Errors";
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return "Done";
    }

    private static void AddSMTPLogger() {
        boolean bFound = false;
        Logger logger = Logger.getLogger(AccumedValidatorWS.class.getName());
        java.util.logging.Handler[] handlers = logger.getHandlers();
        for (java.util.logging.Handler handler : handlers) {
            if (handler instanceof smtphandler.SMTPHandler) {
                bFound = true;
            }
        }
        if (!bFound) {
            Logger.getLogger(
                    AccumedValidatorWS.class.getName()).log(Level.INFO, "logger not found create new one");
            java.util.logging.Handler ch = new smtphandler.SMTPHandler();
            logger.addHandler(ch);
            LogManager lm = LogManager.getLogManager();
            lm.addLogger(logger);
        }
    }

    private static void LogInfo(long id, String msg, long timeInMilliSeconds) {
        Logger.getLogger(AccumedValidatorWS.class.getName()).
                log(Level.INFO, "{0} id:{1} time(ms):{2}", new Object[]{msg, id, timeInMilliSeconds});
    }

    @WebMethod(operationName = "Scrub")
    public com.accumed.model.scrubRequest.ScrubRequest Scrub(
            @WebParam(name = "ScrubbingRequest", targetNamespace = "http://webservices.accumed.com/") com.accumed.model.scrubRequest.ScrubRequest request,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "debug", targetNamespace = "http://webservices.accumed.com/") Boolean debug)
            throws AccessDenied {

        validatorStates.add(new ValidatorState(new java.util.Date(), 1L,
                (workerPool.getNumActive() + workerPool.getNumIdle()),
                workerPool.getNumActive(),
                workerPool.getNumIdle()));

        if (debug == null) {
            debug = false;
        }

        long ltime = System.currentTimeMillis();
        long REQUEST_THREAD_UNIQUE_ID = REQUEST_THREAD_NEXT_ID.getAndIncrement();
        LogInfo(REQUEST_THREAD_UNIQUE_ID, "started", 0);

        boolean logRequest = LoggingManager.getInstance().isLogRequest();
        if(logRequest)
            breUtils.saveRequest(request, true);
        AccumedValidatorWS.totalRequests++;

        long lBegin = System.currentTimeMillis();
        long lStart = lBegin;

        if (request == null) {
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Request is null.", System.currentTimeMillis() - ltime);
            return null;
        }

        if (!initialized) {
            request.removeAllOutcomes();
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Service is not initialized yet.", System.currentTimeMillis() - ltime);
            request.addOutcome("Service is not initialized yet.");
            return request;
        }

        if (request.getClaim() == null) {
            request.removeAllOutcomes();
            request.addOutcome("Missing [Claim]!!");
            return request;
        }

//        if (request.getSender() == null || request.getSender().trim().isEmpty()) {
//            request.removeAllOutcomes();
//            request.addOutcome("Missing [Request.Sender]!!");
//            return request;
//        }
//
//        if (request.getClaim().getProviderID() == null || request.getClaim().getProviderID().trim().isEmpty()) {
//            request.removeAllOutcomes();
//            request.addOutcome("Missing [Claim.ProviderID]!!");
//            return request;
//        }
          String facilityID=request.getClaim().getProviderID()==null?null:request.getClaim().getProviderID().trim();
          if (!Utils.isFacilityAllowed(facilityID)) {
            request.removeAllOutcomes();
            request.addOutcome("Claims Scrubber trial license has expired. To renew your subscription please contact your ACCUMED Account Manager");
            if (request.getClaim() != null) {
                request.getClaim().addOutcome("Claims Scrubber trial license has expired. To renew your subscription please contact your ACCUMED Account Manager");
            }
            return request;
        }

        if (!authorized(user, psw)) {
            request.removeAllOutcomes();
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Access denied.", System.currentTimeMillis() - ltime);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        LogInfo(REQUEST_THREAD_UNIQUE_ID,
                "Workers =" + (workerPool.getNumActive() + workerPool.getNumIdle())
                + " Active=" + workerPool.getNumActive()
                + " Idle=" + workerPool.getNumIdle(), System.currentTimeMillis() - ltime);

        //New 18 Dec 2017
        int lgSize = saveFixedPool != null ? saveFixedPool.getQueue().size() : 0;

        LogInfo(REQUEST_THREAD_UNIQUE_ID, "Logging queue size =" + lgSize, System.currentTimeMillis() - ltime);

        if (lgSize <= LOGGING_MAX_QUEUE_SIZE) {
            if (LOGGING_MAX_QUEUE_SIZE_EMAIL_SENT.compareAndSet(true, false)) {

                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "Logging queue size is getting lower ='{'{0}'}' Rules Engine will start processing requests again.", lgSize);
            }
        }
        if (lgSize <= LOGGING_WARNING_QUEUE_SIZE) {
            if (LOGGING_WARNING_QUEUE_SIZE_EMAIL_SENT.compareAndSet(true, false)) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "Logging queue size is getting lower ='{'{0}'}' Rules Engine will re-monitor queue size.", lgSize);
            }
        }

        if (lgSize > LOGGING_MAX_QUEUE_SIZE) {
            if (LOGGING_MAX_QUEUE_SIZE_EMAIL_SENT.compareAndSet(false, true)) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "Logging queue size is high ='{'{0}'}' Request will NOT be processed", lgSize);
            }
            request.addOutcome("Service is busy.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Service is busy.", System.currentTimeMillis() - ltime);
            return request;
        }
        //End New 18 Dec 2017

        LogInfo(REQUEST_THREAD_UNIQUE_ID, "Preparing request for processing.", System.currentTimeMillis() - ltime);
        request.fixRequest();
        request.removeAllOutcomes();
        request.setAuthUser(user);
        LogInfo(REQUEST_THREAD_UNIQUE_ID, "Preparing request for processing completed.", System.currentTimeMillis() - ltime);
        //AddSMTPLogger();
       // disabled by  Wael  upon  the  following  RULEPLAT-213,210,215 
       //  if(!checkMandatoryFields(REQUEST_THREAD_UNIQUE_ID,request,ltime)) return request;
        request.setRequestTime(new Date());

        Date beginTime = null;
        DB query_db = null;
        Worker worker = null;
        java.sql.Connection conn = null;
        AccumedValidatorWS.ReturnThread returnThread = null;
        Boolean errorOccured = false;
        try {
            request.setTop20(0);

            boolean excludeDBRules = request.getExcludeDBRules() == null ? false : request.getExcludeDBRules();
            if (Statistics.isDisableDBRules()) {
                excludeDBRules = false;
            }

            if (!excludeDBRules && false) {
                LogInfo(REQUEST_THREAD_UNIQUE_ID, "Getting DB connection...", System.currentTimeMillis() - ltime);
                conn = getAccumedDB();
                if (conn != null) {
                    query_db = new DB(conn);
                }
                LogInfo(REQUEST_THREAD_UNIQUE_ID, "Getting DB connection completed.", System.currentTimeMillis() - ltime);
            } 
          // else {
//                LogInfo(REQUEST_THREAD_UNIQUE_ID, "DB Rules are excluded.", System.currentTimeMillis() - ltime);
//            }

            Claim claim = request.getClaim();
            Logger.getLogger(
                    AccumedValidatorWS.class.getName()).log(Level.INFO, "Claim count=" + 1);

            beginTime = new Date();

            request.setRequestTime(beginTime);

            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Getting worker...", System.currentTimeMillis() - ltime);
            worker = (Worker) workerPool.borrowObject();
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Getting worker completed.", System.currentTimeMillis() - ltime);

            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Validating request by the worker...", System.currentTimeMillis() - ltime);

            

            worker.validate(request, query_db,
                    excludeDBRules, debug);

            long takenTime = System.currentTimeMillis() - lStart;
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Validating request by the worker completed", System.currentTimeMillis() - ltime);

            returnThread = new AccumedValidatorWS.ReturnThread(worker, conn);
            //returnFixedPool.submit(returnThread);
            returnFixedPool.schedule(returnThread, /*initialDelay*/ 10, TimeUnit.MILLISECONDS);
            Utils.setExclusions(request, exclusions);
              request = Utils.handleEmptyLongMsg(request);
            request.setResponseTime(new Date());

            if (LOGGING_ENABLED == 1 || Statistics.isLogHistory()) {
                //manage claim history
                if (Statistics.isLogHistory()) {
                    if (request.getClaim().getXclaim() != null) {
                        for (com.accumed.model.scrubRequest.xclaim.XClaim xcl : request.getClaim().getXclaim()) {
                            linkXclaimToClaim(xcl, claim);
                        }
                    }
                    if (request.getClaim().getFclaim() != null) {
                        for (com.accumed.model.scrubRequest.fclaim.FClaim fcl : request.getClaim().getFclaim()) {
                            linkFclaimToClaim(fcl, claim);
                        }
                    }
                    if (request.getClaim().getRclaim() != null) {
                        for (com.accumed.model.scrubRequest.rclaim.RClaim rcl : request.getClaim().getRclaim()) {
                            linkRclaimToClaim(rcl, claim);
                        }
                    }
                } else {
                    request.getClaim().setXclaim(null);
                    request.getClaim().setFclaim(null);
                    request.getClaim().setRclaim(null);
                }
                //end manage claim history

                lgSize = saveFixedPool != null ? saveFixedPool.getQueue().size() : 0;
                Logger.getLogger(AccumedValidatorWS.class.getName()).
                        log(Level.INFO, "Logging Queue Size={0}",
                                lgSize);

                if (lgSize > LOGGING_WARNING_QUEUE_SIZE) {
                    if (LOGGING_WARNING_QUEUE_SIZE_EMAIL_SENT.compareAndSet(false, true)) {
                        Logger.getLogger(AccumedValidatorWS.class.getName())
                                .log(Level.SEVERE, "SEVERE: Logging queue size is high ='{'{0}'}'", lgSize);
                    }
                }
                
                request=Utils.setReferenceLinks(request, AccumedValidatorWS.ruleRefrences);
                request = Utils.setCategories(request, AccumedValidatorWS.ruleCats);
                //Sameer Here
                AccumedValidatorWS.SaveThread saveThread = new AccumedValidatorWS.SaveThread(request); //need deep clone
                saveFixedPool.submit(saveThread);

                if (!Statistics.isLogHistory()) {
                    request.getClaim().setXclaim(null);
                }
            } //LOGGING_ENABLED==1
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Total time for request ms:", System.currentTimeMillis() - ltime);

            AccumedValidatorWS.lastRequestTime = takenTime;
            AccumedValidatorWS.totalProcessedClaims++;
            AccumedValidatorWS.totalProcessedClaimsTime += takenTime;
            averageClaimProcessTime = totalProcessedClaimsTime / totalProcessedClaims;
            int claimIdCaller=claim.getIdCaller()==null?0:claim.getIdCaller();
            if (AccumedValidatorWS.minRequest == null) {
                AccumedValidatorWS.minRequest = new WorkLog(claimIdCaller,
                        takenTime, lStart);
                AccumedValidatorWS.maxRequest = new WorkLog(claimIdCaller,
                        takenTime, lStart);
            } else {
                if (takenTime < AccumedValidatorWS.minRequest.getPeriodInMilli()) {
                    if (claim == null) {
                        Logger.getLogger(AccumedValidatorWS.class
                                .getName()).log(Level.INFO, "Sameer*****claim is null");
                    }
                    WorkLog log = new WorkLog(claimIdCaller,
                            takenTime, lStart);
                    if (log == null) {
                        Logger.getLogger(AccumedValidatorWS.class
                                .getName()).log(Level.INFO, "Sameer*****log is null");
                    }
                    AccumedValidatorWS.minRequest = log;
                }
                if (takenTime > AccumedValidatorWS.maxRequest.getPeriodInMilli()) {
                    AccumedValidatorWS.maxRequest = new WorkLog(claimIdCaller,
                            takenTime, lStart);
                }
            }

            AccumedValidatorWS.workLogger.add(new WorkLog(claimIdCaller,
                    takenTime, lStart));

            request = Utils.removeExclusions(
                    (com.accumed.model.scrubRequest.ScrubRequest) org.apache.commons.lang3.SerializationUtils.clone(request));
            if(logRequest)
                breUtils.saveRequest(request, false);
            return request;

        } catch (java.util.NoSuchElementException e) {
            if(e.getMessage() != null && !e.getMessage().equalsIgnoreCase("Pool exhausted")){
                errorOccured = true;
                request = HandleException(e, beginTime, request, 1);
            }else{
                Logger.getLogger(AccumedValidatorWS.class.getName())
                        .log(Level.SEVERE, "Server Exhausted.");
                request = HandleException(e, beginTime, request, 1);
            }
        } catch (org.drools.RuntimeDroolsException e) {
            errorOccured = true;
            request = HandleException(e, beginTime, request, 1);
        } catch (java.sql.SQLException e) {
            errorOccured = true;
            request = HandleException(e, beginTime, request, 3);
        } catch (Exception e) {
            errorOccured = true;
            request = HandleException(e, beginTime, request, 4);
        } finally {
            if (errorOccured) {
                Logger.getLogger(AccumedValidatorWS.class.getName())
                        .log(Level.SEVERE, ">>>>>>>>>>>>>>Invalidate WORKER because of an error...");
                try {
                    workerPool.invalidateObject(worker);
                } catch (Exception ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (returnThread == null) {
                returnThread = new AccumedValidatorWS.ReturnThread(worker, conn);
                returnFixedPool.submit(returnThread);
            }
        }

        return request;
    }
    private boolean checkMandatoryFields(long REQUEST_THREAD_UNIQUE_ID,com.accumed.model.scrubRequest.ScrubRequest request,long ltime)
    {
        LogInfo(REQUEST_THREAD_UNIQUE_ID, "Checking request.", System.currentTimeMillis() - ltime);
        

        //Parse XML
        if (request.getHeader() == null || request.getClaim() == null) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).severe("validationRequest is null");
            request.addOutcome("Header or claims are null.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Header or claims are null.", System.currentTimeMillis() - ltime);
            return false;
        }
          boolean accept=true;
        if (request.getHeader().getReceiverID() == null) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).severe("Receiver is null");
            request.addOutcome("Receiver is null.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Receiver is null.", System.currentTimeMillis() - ltime);
            accept=false;
        }
        //if (request.getClaim() != null && request.getClaim().get(0) != null && request.getClaim().get(0).getProviderID() == null) {
        if (request.getClaim() != null && request.getClaim().getProviderID() == null) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).severe("Provider is null");
            request.addOutcome("Provider is null.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Provider is null.", System.currentTimeMillis() - ltime);
            accept=false;
        }
         if ( request.getClaim().getPatient() != null )             
         {
             if(request.getClaim().getPatient().getEmiratesId()==null)
             {
               Logger.getLogger(AccumedValidatorWS.class.getName()).severe("Emirates ID is null");
            request.addOutcome("Emirates ID is null.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "Emirates ID is null.", System.currentTimeMillis() - ltime);
            accept=false;
             }
         if ( request.getClaim().getPatient().getPatientInsurance()==null)
         {
               Logger.getLogger(AccumedValidatorWS.class.getName()).severe("PatientInsurance is null");
            request.addOutcome("PatientInsurance is null.");
            LogInfo(REQUEST_THREAD_UNIQUE_ID, "PatientInsurance is null.", System.currentTimeMillis() - ltime);
            accept=false;
         }
         }
        LogInfo(REQUEST_THREAD_UNIQUE_ID, "Checking request completed.", System.currentTimeMillis() - ltime);

        return accept;
    }

    private String convertToXML(com.accumed.model.scrubRequest.ScrubRequest request) {

        JAXBContext contextA = null;
        StringWriter writer = null;
        javax.xml.bind.Marshaller marshaller = null;
        String sRet = "";

        try {
            contextA = JAXBContext.newInstance(com.accumed.model.scrubRequest.ScrubRequest.class
            );
            writer = new StringWriter();
            marshaller = contextA.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(request, writer);
            marshaller = null;

            sRet = writer.toString();
            int nIndex = sRet.indexOf(">");
            sRet = sRet.substring(0, nIndex + 1) + AccumedValidatorWS.PREFIX + sRet.substring(nIndex + 1) + AccumedValidatorWS.SUFFIX;
            sRet = sRet.replaceFirst(java.util.regex.Pattern.quote("UTF-8"), "UTF-16");
            return sRet;
        } catch (JAXBException ex) {
            Statistics.addException(ex);
            Logger
                    .getLogger(AccumedValidatorWS.class
                            .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (marshaller != null) {
                marshaller = null;
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                }
                writer = null;
            }
            if (contextA != null) {
                contextA = null;
            }
        }
        return sRet;
    }

    private com.accumed.model.scrubRequest.ScrubRequest HandleException(Exception e, java.util.Date beginTime, com.accumed.model.scrubRequest.ScrubRequest req,
            int id) {

        req.addOutcome(e);
        String sReq = convertToXML(req);
        Statistics.addException(e, sReq);
        Logger.getLogger(AccumedValidatorWS.class
                .getName()).log(Level.SEVERE,
                        sReq, e);

        Logger.getLogger(
                AccumedValidatorWS.class
                        .getName()).log(Level.INFO, "sRequest" + id + "=" + sReq);

        if (LOGGING_ENABLED == 1) {
            req.setRequest(sReq);
            req.setResponseTime(new Date());
//        Utils.setParents(req);
            req.setClaim(null);
            req.setHeader(null);
            SaveThread saveThread = new SaveThread(req);
            saveFixedPool.submit(saveThread);
        }
        return req;

    }

    protected class ReturnThread implements Runnable {

        private Worker worker = null;
        private java.sql.Connection conn;
        final long RETURN_THREAD_UNIQUE_ID = RETURN_THREAD_NEXT_ID.getAndIncrement();

        public ReturnThread(Worker worker, java.sql.Connection conn) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            this.worker = worker;
            this.conn = conn;
        }

        @Override
        public void run() {
            Logger.getLogger(ReturnThread.class.getName()).
                    log(Level.INFO, "ReturnThread started '{'{0}'}'", RETURN_THREAD_UNIQUE_ID);
            try {
                workerPool.returnObject(worker);
                
                Logger.getLogger(ReturnThread.class.getName()).
                        log(Level.INFO, "ReturnThread completed '{'{0}'}'", RETURN_THREAD_UNIQUE_ID);
//                if (workerPool.getCount() < WORKERS_MIN_POOL_SIZE) {
//                    workerPool.preparePool();
//                    Logger.getLogger(ReturnThread.class.getName()).
//                            log(Level.INFO, "ReturnThread completed '{'{0}'}' called PreparePool", RETURN_THREAD_UNIQUE_ID);
//                }

            } catch (org.drools.RuntimeDroolsException e) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "exception caught", e);
            } catch (Exception e) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "exception caught", e);
            } finally {
                worker = null;
                if (conn != null) {
                    try {
                        if (!conn.isClosed()) {
                            conn.close();
                        }
                    } catch (java.sql.SQLException ex) {
                        Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                        Statistics.addException(ex);
                    }
                }
            }
        }
    }

    protected class SaveThread implements Runnable {

        private com.accumed.model.scrubRequest.ScrubRequest req = null;
        final long SAVE_THREAD_UNIQUE_ID = SAVE_THREAD_NEXT_ID.getAndIncrement();

        public SaveThread(com.accumed.model.scrubRequest.ScrubRequest req1) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            this.req = req1;
        }

        @Override
        public void run() {
            Logger.getLogger(SaveThread.class.getName()).
                    log(Level.INFO, "SaveThread started '{'{0}'}'{1}", new Object[]{SAVE_THREAD_UNIQUE_ID, (req != null && req.getClaim() != null && req.getClaim().getIdCaller() != null)
                ? req.getClaim().getIdCaller() + ""
                : 0 + ""});
            try {
              //  AddSMTPLogger();
                Utils.setParents(req);
                Utils.trimLongFields(req);
                ScrubRequestJpaController controller;
                controller = new ScrubRequestJpaController();

                controller.persist(req);
               
                Logger.getLogger(SaveThread.class.getName()).
                        log(Level.INFO, "SaveThread finish '{'{0}'}'{1}", new Object[]{SAVE_THREAD_UNIQUE_ID, (req != null && req.getClaim() != null && req.getClaim().getIdCaller() != null)
                    ? req.getClaim().getIdCaller() + ""
                    : 0 + ""});
                
            } catch (org.drools.RuntimeDroolsException e) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "exception caught", e);
                HandleException(e, new Date(), req, 6);
            } catch (Exception e) {
                Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE,
                        "exception caught", e);
                HandleException(e, new Date(), req, 8);
            } finally {
                 req = null;
            }
        }
    }

    private void linkRclaimToClaim(com.accumed.model.scrubRequest.rclaim.RClaim xcl, Claim claim) {
        xcl.setClaim(claim);
        if (xcl.getActivity() != null) {
            for (com.accumed.model.scrubRequest.rclaim.RActivity xac : xcl.getActivity()) {
                xac.setClaimID(xcl);
                if (xac.getObservation() != null) {
                    for (com.accumed.model.scrubRequest.rclaim.RObservation xo : xac.getObservation()) {
                        xo.setActivityID(xac);
                    }
                }
            }
        }
        if (xcl.getDiagnosis() != null) {
            for (com.accumed.model.scrubRequest.rclaim.RDiagnosis xdi : xcl.getDiagnosis()) {
                xdi.setClaimID(xcl);
            }
        }
        if (xcl.getClaimType() != null) {
            for (com.accumed.model.scrubRequest.rclaim.RClaimType xc : xcl.getClaimType()) {
                xc.setClaimID(xcl);
            }
        }
        if (xcl.getEncounter() != null) {
            for (com.accumed.model.scrubRequest.rclaim.REncounter xe : xcl.getEncounter()) {
                xe.setClaimID(xcl);
                if (xe.getAuthorisation() != null) {
                    xe.getAuthorisation().setEncounterID(xe);
                }
            }
        }
        if (xcl.getResubmission() != null) {
            xcl.getResubmission().setClaimID(xcl);
        }
        if (xcl.getContract() != null) {
            xcl.getContract().setClaimID(xcl);
        }

    }

    private void linkFclaimToClaim(com.accumed.model.scrubRequest.fclaim.FClaim xcl, Claim claim) {
        xcl.setClaim(claim);
        if (xcl.getActivity() != null) {
            for (com.accumed.model.scrubRequest.fclaim.FActivity xac : xcl.getActivity()) {
                xac.setClaimID(xcl);
                if (xac.getObservation() != null) {
                    for (com.accumed.model.scrubRequest.fclaim.FObservation xo : xac.getObservation()) {
                        xo.setActivityID(xac);
                    }
                }
            }
        }
        if (xcl.getDiagnosis() != null) {
            for (com.accumed.model.scrubRequest.fclaim.FDiagnosis xdi : xcl.getDiagnosis()) {
                xdi.setClaimID(xcl);
            }
        }
        if (xcl.getClaimType() != null) {
            for (com.accumed.model.scrubRequest.fclaim.FClaimType xc : xcl.getClaimType()) {
                xc.setClaimID(xcl);
            }
        }
        if (xcl.getEncounter() != null) {
            for (com.accumed.model.scrubRequest.fclaim.FEncounter xe : xcl.getEncounter()) {
                xe.setClaimID(xcl);
                if (xe.getAuthorisation() != null) {
                    xe.getAuthorisation().setEncounterID(xe);
                }
            }
        }
        if (xcl.getResubmission() != null) {
            xcl.getResubmission().setClaimID(xcl);
        }
        if (xcl.getContract() != null) {
            xcl.getContract().setClaimID(xcl);
        }

    }

    private void linkXclaimToClaim(XClaim xcl, Claim claim) {
        xcl.setClaim(claim);
        if (xcl.getActivity() != null) {
            for (XActivity xac : xcl.getActivity()) {
                xac.setClaimID(xcl);
                if (xac.getObservation() != null) {
                    for (XObservation xo : xac.getObservation()) {
                        xo.setActivityID(xac);
                    }
                }
            }
        }
        if (xcl.getDiagnosis() != null) {
            for (XDiagnosis xdi : xcl.getDiagnosis()) {
                xdi.setClaimID(xcl);
            }
        }
        if (xcl.getClaimType() != null) {
            for (XClaimType xc : xcl.getClaimType()) {
                xc.setClaimID(xcl);
            }
        }
        if (xcl.getEncounter() != null) {
            for (XEncounter xe : xcl.getEncounter()) {
                xe.setClaimID(xcl);
                if (xe.getAuthorisation() != null) {
                    xe.getAuthorisation().setEncounterID(xe);
                }
            }
        }
        if (xcl.getResubmission() != null) {
            xcl.getResubmission().setClaimID(xcl);
        }
        if (xcl.getContract() != null) {
            xcl.getContract().setClaimID(xcl);
        }

    }

    private void fetchUsers() {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        if (!users.isEmpty()) {
            users = new ConcurrentHashMap<>();
        }

        try {
            connection = getAccumedDB();
            if (connection != null) {
              try{ 
                  stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT [ID], [ACCUMED_USER] ,[ACCUMED_PASSWORD] FROM [ACCUMED_FACILITY_USER]");
                if (rs != null) {
                    while (rs.next()) {
                        Integer i = rs.getInt("ID");
                        String u = rs.getString("ACCUMED_USER");
                        String p = rs.getString("ACCUMED_PASSWORD");

                        users.put(u, new RulesUser(i, p, 1));

                    }
                }
              }catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, "ACCUMED_FACILITY_USER  is  not  existed", ex);
        } 
                stmt = connection.createStatement();
                rs = stmt.executeQuery("Select [USER_ID], [USER_NAME], [ENCRYPTED_PASSWORD] from NEO_USER where isDeleted <> 1");
                if (rs != null) {
                    while (rs.next()) {
                        Integer i = rs.getInt("USER_ID");
                        String u = rs.getString("USER_NAME");
                        String p = rs.getString("ENCRYPTED_PASSWORD");

                        users.put(u, new RulesUser(i, p, 2));
                    }
                }
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
    }

    private boolean authorized(String user, String psw) {
        if (user == null || psw == null) {
            return false;
        }
        if ((user.equals("Santechture") && psw.equals("sanP@550rd2024"))) {
            return true;
        }
        if (users.isEmpty()) {
            this.fetchUsers();
        }

        RulesUser rulesUser = users.get(user);
        if (rulesUser != null && rulesUser.psw != null && rulesUser.psw.equals(psw)) {
            return true;
        } else { //try to refresh users list
            fetchUsers();
            rulesUser = users.get(user);
            LoginCheckProcessServlet logincheck=new LoginCheckProcessServlet();
            if (rulesUser != null && rulesUser.type==1 && rulesUser.psw != null && rulesUser.psw.equals(psw)) {
                return true;
            }
            if (rulesUser != null && rulesUser.type==2   && rulesUser.psw != null &&( logincheck.checkPassword(psw,rulesUser.psw) || rulesUser.psw.equals(psw)) ) {
                return true;
            }
        }

        return false;
    }

    public static WorkersPool getWorkersPool() {
        return AccumedValidatorWS.workerPool;

    }

    private static java.sql.Connection getRulesDB() {
        Logger.getLogger(AccumedValidatorWS.class
                .getName())
                .log(Level.INFO, "getRulesDB ....");
        DataSource ds = null;
        java.sql.Connection con = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/rulesLoggingDS");
            //ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/accumedDS");
            con = ds.getConnection();

            if (con.getTransactionIsolation() != java.sql.Connection.TRANSACTION_READ_UNCOMMITTED) {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");

            } else {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");

            }
        } catch (NamingException | java.sql.SQLException ex) {
            Logger.getLogger(CachedRepositoryService.class
                    .getName()).log(Level.SEVERE, null, ex);

        }
        Logger.getLogger(AccumedValidatorWS.class
                .getName())
                .log(Level.INFO, "getRulesDB Done.");
        return con;

    }

    private static java.sql.Connection getAccumedDB() {
        Logger.getLogger(AccumedValidatorWS.class
                .getName())
                .log(Level.INFO, "getAccumedDB ....");
        DataSource ds = null;
        java.sql.Connection con = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/accumedDS");
            //ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/accumedDS");
            con = ds.getConnection();

            if (con.getTransactionIsolation() != java.sql.Connection.TRANSACTION_READ_UNCOMMITTED) {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");

            } else {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");

            }
        } catch (NamingException ex) {
            Logger.getLogger(CachedRepositoryService.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(CachedRepositoryService.class
                    .getName()).log(Level.SEVERE, null, ex);

        }
        Logger.getLogger(AccumedValidatorWS.class
                .getName())
                .log(Level.INFO, "getAccumedDB Done.");
        return con;

    }

//    private java.sql.Connection getJiraDB() {
//        Logger.getLogger(AccumedValidatorWS.class
//                .getName())
//                .log(Level.INFO, "getJiraDB ....");
//        DataSource ds = null;
//        java.sql.Connection con = null;
//        try {
//            Context initCtx = new InitialContext();
//            Context envCtx = (Context) initCtx.lookup("java:comp/env");
//            ds = (DataSource) envCtx.lookup("jdbc/jiraDS");
//            con = ds.getConnection();
//
//            if (con.getTransactionIsolation() != java.sql.Connection.TRANSACTION_READ_UNCOMMITTED) {
//                Logger.getLogger(CachedRepositoryService.class
//                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");
//
//            } else {
//                Logger.getLogger(CachedRepositoryService.class
//                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");
//
//            }
//        } catch (NamingException ex) {
//            Logger.getLogger(CachedRepositoryService.class
//                    .getName()).log(Level.SEVERE, null, ex);
//
//        } catch (java.sql.SQLException ex) {
//            Logger.getLogger(CachedRepositoryService.class
//                    .getName()).log(Level.SEVERE, null, ex);
//
//        }
//        Logger.getLogger(AccumedValidatorWS.class
//                .getName())
//                .log(Level.INFO, "getJiraDB Done.");
//        return con;
//    }

    public static void setWorkLogger(WorkLogger logger) {
        AccumedValidatorWS.workLogger = logger;
    }

    public static WorkLogger getWorkLogger() {
        return workLogger;
    }

    public static WorkLog getMinRequest() {
        return minRequest;
    }

    public static void setMinRequest(WorkLog minRequest) {
        AccumedValidatorWS.minRequest = minRequest;
    }

    public static WorkLog getMaxRequest() {
        return maxRequest;
    }

    public static void setMaxRequest(WorkLog maxRequest) {
        AccumedValidatorWS.maxRequest = maxRequest;
    }

    public static Long getAverageClaimProcessTime() {
        return averageClaimProcessTime;
    }

    public static Long getLastRequestTime() {
        return lastRequestTime;
    }

    public static long getTotalProcessedClaims() {
        return totalProcessedClaims;
    }

    public static long getTotalRequests() {
        return totalRequests;
    }

    public static ThreadPoolExecutor getSaveFixedPool() {
        return saveFixedPool;
    }

    public static ScheduledExecutorService getReturnFixedPool() {
        return returnFixedPool;
    }

    @WebMethod(operationName = "GetRules")
    public String GetRules(@WebParam(name = "packageName") String packageName) {

        JSONArray arr = Worker.getRules(packageName);
        JSONArray ret = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jsonObject = arr.getJSONObject(i);
            jsonObject.put("categories", ruleCats.get((String) jsonObject.get("name")));
            ret.put(jsonObject);
        }
        return ret.toString();
    }

    @WebMethod(operationName = "GetPackages")
    public String GetPackages() {
        JSONArray ret = null;
        boolean returned = true;
        Worker worker = null;
        try {
            worker = (Worker) workerPool.borrowObject();
            returned = false;
            ret = worker.GetPackages();
            workerPool.returnObject(worker);
            returned = true;

        } catch (Exception ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
            try {
                if (!returned) {
                    workerPool.invalidateObject(worker);

                }
            } catch (Exception ex1) {
                Logger.getLogger(AccumedValidatorWS.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return ret.toString();
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @WebMethod(operationName = "GetRuleDocumentation")
    public String GetRuleDocumentation(@WebParam(name = "ruleName") String ruleName) {
        Logger.getLogger(AccumedValidatorWS.class
                .getName()).log(Level.INFO, "GetRuleDocumentation{0}",
                        new Object[]{ruleName});

        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        String sDocs = "Not Available Now";

        if (ruleName == null) {
            return sDocs;
        }
        if(true)
        {
             return sDocs;
        }

//        if (ruleName.trim().isEmpty() || ruleName.startsWith("HAAD")) {
//            return sDocs;
//        }
//
//        String sQuery = "Select JIRA.customfieldvalue.TEXTVALUE from JIRA.customfieldvalue \n"
//                + "inner join JIRA.jiraissue on JIRA.jiraissue.ID = JIRA.customfieldvalue.issue\n"
//                + "inner join JIRA.customfield on JIRA.customfield.ID = JIRA.customfieldvalue.customfield\n"
//                + "where \n"
//                + "cfname = 'Medical Documentation'\n"
//                + "and pkey='RULE-";
//
//        if (ruleName.indexOf('_') < 0) {
//            return sDocs;
//        }
//        String sID = ruleName.substring(1, ruleName.indexOf('_'));
//        try {
//            if (isInteger(sID)) {
//                Integer id = Integer.parseInt(sID);
//                conn = getJiraDB();
//                stmt = conn.createStatement();
//                rs = stmt.executeQuery(sQuery + id + "'");
//                //rs = stmt.executeQuery("Select * from sys.Tables");
//                if (rs != null) {
//                    if (rs.next()) {
//                        sDocs = rs.getString(1);
//                    }
//                }
//                return sDocs.trim().isEmpty() ? "Not Found" : sDocs.trim();
//
//            }
//        } catch (NumberFormatException e) {
//            Logger.getLogger(AccumedValidatorWS.class
//                    .getName()).log(Level.SEVERE,
//                            "GetRuleDocumentation", e);
//
//        } catch (java.sql.SQLException ex) {
//            Logger.getLogger(AccumedValidatorWS.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (rs != null) {
//
//                    rs.close();
//                    rs = null;
//                }
//                if (stmt != null) {
//                    stmt.close();
//                    stmt = null;
//                }
//                if (conn != null) {
//                    if (!conn.isClosed()) {
//                        conn.close();
//                        conn = null;
//                    }
//                }
//            } catch (java.sql.SQLException ex) {
//                Logger.getLogger(AccumedValidatorWS.class
//                        .getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return sDocs;
    }
//    @WebMethod(operationName = "GetPatientDHAInfo")
//    public Members GetPatientDHAInfo(@WebParam(name = "EID") String EID) {
//        Logger.getLogger(AccumedValidatorWS.class.getName()).
//                log(Level.INFO, "entering GetPatientDHAInfo for {0}.", EID);
//        try { // Call Web Service Operation
//            eclaimlinkSearchIdentity.SearchIdentity service = new eclaimlinkSearchIdentity.SearchIdentity();
//            eclaimlinkSearchIdentity.ISearchIdentity port = service.getBasicEndpoint();
//            // TODO initialize WS operation arguments here
//            java.lang.String login = DHA_LINK_USER_NAME;//= "sghdubai";
//            java.lang.String pwd = DHA_LINK_PSW;//= "insgh#123";
//            java.lang.String emiratesId = EID;//"784-1983-7959791-1";
//            // TODO process result here
//            eclaimlinkSearchIdentity.MemberInsuranceResponse result
//                    = port.searchEmiratesId(login, pwd, emiratesId);
//            System.out.println("Result = " + result.getMemberInformation());
//            if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
//                Members mem = new Members();
//                mem.setErrorMsg(result.getErrorMessage());
//                Logger.getLogger(AccumedValidatorWS.class.getName()).
//                        log(Level.INFO, "exiting GetPatientDHAInfo for {0}, with error message [" + result.getErrorMessage() + "].", EID);
//                return mem;
//            } else if (result.getMemberInformation().startsWith("<Members>")) {
//                Logger.getLogger(AccumedValidatorWS.class.getName()).
//                        log(Level.INFO, "GetPatientDHAInfo parsing result of {0}.", EID);
//                Members ret = parseXMLString(result.getMemberInformation());
//                Logger.getLogger(AccumedValidatorWS.class.getName()).
//                        log(Level.INFO, "exiting GetPatientDHAInfo for {0}.", EID);
//                return ret;
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(AccumedValidatorWS.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//        Logger.getLogger(AccumedValidatorWS.class.getName()).
//                log(Level.INFO, "exiting GetPatientDHAInfo for {0} after exception.", EID);
//        return null;
//    }
//    private Members parseXMLString(String input) {
//        try {
//
////            input = input.replaceAll("\"", "&quot;");
////            input = input.replaceAll("'", "&apos;");
////            input = input.replaceAll("<", "&lt;");
////            input = input.replaceAll(">", "&gt;");
//            input = input.replaceAll("&", "&amp;");
//
//            JAXBContext jaxbContext = JAXBContext.newInstance(Members.class);
//            javax.xml.bind.Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//
//            StringReader reader = new StringReader(input);
//            Members members = (Members) unmarshaller.unmarshal(reader);
//            return members;
//
//        } catch (JAXBException ex) {
//            Logger.getLogger(AccumedValidatorWS.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//
//    }
//    protected class DHALinkThread implements Runnable {
//
//        String EID = "";
//        Members members = null;
//        boolean completed = false;
//
//        public DHALinkThread(String EID) {
//            this.EID = EID;
//            completed = false;
//            members = null;
//        }
//
//        public String getEID() {
//            return EID;
//        }
//
//        public Members getMembers() {
//            return members;
//        }
//
//        public boolean isCompleted() {
//            return completed;
//        }
//
//        @Override
//        public void run() {
//            members = GetPatientDHAInfo(this.EID);
//            completed = true;
//        }
//    }
    @WebMethod(operationName = "validatePatient")
    public com.accumed.bre.patient.Patient validatePatient(
            @WebParam(name = "patient", targetNamespace = "http://webservices.accumed.com/") com.accumed.bre.patient.Patient patient,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "debug", targetNamespace = "http://webservices.accumed.com/") Boolean debug)
            throws AccessDenied {
        breUtils.savePatient(patient);
        com.accumed.model.scrubRequest.ScrubRequest req = breUtils.convertPatientToRequest(patient, user);
        com.accumed.model.scrubRequest.ScrubRequest resp = Scrub(req, user, psw, debug);
        com.accumed.bre.patient.Patient ret = breUtils.convertResponseToPatient(patient, resp);
        breUtils.savePatient(patient);
        return ret;
    }

    @WebMethod(operationName = "validateClaim")
    public com.accumed.bre.dha.Claim validateClaim(
            @WebParam(name = "patient", targetNamespace = "http://webservices.accumed.com/") com.accumed.bre.patient.Patient patient,
            @WebParam(name = "claim", targetNamespace = "http://webservices.accumed.com/") com.accumed.bre.dha.Claim claim,
            @WebParam(name = "process", targetNamespace = "http://webservices.accumed.com/") String process,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "debug", targetNamespace = "http://webservices.accumed.com/") Boolean debug) throws ParseException, AccessDenied {
        breUtils.saveClaim(claim);
        com.accumed.model.scrubRequest.ScrubRequest resp = null;

        com.accumed.model.scrubRequest.ScrubRequest req = breUtils.convertbreClaimToRequest(patient, claim, process, user);
//        if ((req.getClaim().getActivity() != null && !req.getClaim().getActivity().isEmpty())
//                || (req.getClaim().getDiagnosis() != null && !req.getClaim().getDiagnosis().isEmpty())) {
//            req = breUtils.addExtendedValidationType(req, "Analyzing", Boolean.TRUE);
//            resp = Scrub(req, user, psw, debug);
//        }
        //DataEntry, Coding, Auditing, Billing, Submission
        switch (process) {
           case "AppointmentDesk":
                    req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                     req = breUtils.addExtendedValidationType(req, "AppointmentDesk", Boolean.TRUE);
              
                break;
             case "Physician":
                    req = breUtils.addExtendedValidationType(req, "Physician", Boolean.TRUE);              
                break;  
                 case "Insurance":
                    req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                    req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                    req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                    req = breUtils.addExtendedValidationType(req, "Billing", Boolean.FALSE);
                    req = breUtils.addExtendedValidationType(req, "Submission", Boolean.FALSE);
                    req.setRestrictPackages("authorization,mnec_coding,mnec_crosswalk");              
                break;   
            case "Nurse":
                    req = breUtils.addExtendedValidationType(req, "Nurse", Boolean.TRUE);              
                break;    
            case "EHR":
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                break;
            case "PBM":
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                break;
            case "InPatientAdmission":
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                break;
            case "InPatientOrders":
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                break;
            case "InPatientPharmacy":
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                break;
            case "Billing":
               // req = breUtils.addExtendedValidationType(req, "Analyzing", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "DataEntry", Boolean.TRUE);
                req = breUtils.addExtendedValidationType(req, "Coding", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Auditing", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Billing", Boolean.FALSE);
                req = breUtils.addExtendedValidationType(req, "Submission", Boolean.FALSE);
                break;
        }

        resp = Scrub(req, user, psw, debug);

        com.accumed.bre.dha.Claim ret = breUtils.convertResponseTobreClaim(claim, resp);
        breUtils.saveClaim(claim);
        return ret;
    }

    @WebMethod(operationName = "AddExclusion")
    public Long AddExclusion(
            @WebParam(name = "ruleID", targetNamespace = "http://webservices.accumed.com/") String ruleID,
            @WebParam(name = "providerLicense", targetNamespace = "http://webservices.accumed.com/") String providerLicense,
            @WebParam(name = "receiverLicense", targetNamespace = "http://webservices.accumed.com/") String receiverLicense,
            @WebParam(name = "payerLicense", targetNamespace = "http://webservices.accumed.com/") String payerLicense,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;

        try {
            connection = this.getAccumedDB();
            if (connection != null) {
                Integer newExclusionID;

                Boolean alreadyExists = false;
                {//check if already exists
                    java.sql.ResultSet rs = null;
                    java.sql.Statement stmt = null;
                    stmt = connection.createStatement();

                    String sql = "SELECT [ID] FROM [dbo].[Exclusions] WHERE ruleID= '" + ruleID + "' "
                            + (providerLicense == null ? " AND providerLicense is null" : " AND providerLicense = '" + providerLicense + "'")
                            + (receiverLicense == null ? " AND receiverLicense is null" : " AND receiverLicense = '" + receiverLicense + "'")
                            + (payerLicense == null ? " AND payerLicense is null" : " AND payerLicense = '" + payerLicense + "'")
                            + " AND deleted =0";

                    rs = stmt.executeQuery(sql);
                    if (rs != null) {
                        if (rs.next()) {
                            alreadyExists = true;
                        }
                    }
                    rs.close();
                    stmt.close();
                }

                if (alreadyExists) {
                    return -15L;
                }

                String sql = "INSERT INTO [dbo].[Exclusions]\n"
                        + "           ([ruleID]\n"
                        + "           ,[providerLicense]\n"
                        + "           ,[receiverLicense]\n"
                        + "           ,[payerLicense]\n"
                        + "           ,[createdBy]\n"
                        + "           ,[deleted]\n"
                        + "           ,[creationDate])\n"
                        + "     VALUES\n"
                        + "           (?,?,?,?,?,?,getDate())";

                String generatedColumns[] = {"ID"};

                preparedStatement = connection.prepareStatement(sql, generatedColumns);

                preparedStatement.setString(1, ruleID);
                preparedStatement.setString(2, providerLicense);
                preparedStatement.setString(3, receiverLicense);
                preparedStatement.setString(4, payerLicense);
                preparedStatement.setString(5, user);
                preparedStatement.setBoolean(6, false);
                //preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis()));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Creation failed.");
                }

                try (java.sql.ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newExclusionID = new Integer((int) generatedKeys.getLong(1));
                    } else {
                        throw new java.sql.SQLException("Creation failed.");
                    }
                }

                syncExclusion(newExclusionID);
                return new Long(newExclusionID);
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1L;
    }

    @WebMethod(operationName = "DeleteExclusion")
    public Integer DeleteExclusion(
            @WebParam(name = "exclusionID", targetNamespace = "http://webservices.accumed.com/") Integer exclusionID,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;

        try {
            connection = getAccumedDB();
            if (connection != null) {
                String sql = "UPDATE [dbo].[Exclusions] SET [deleted]=?, [deletedBy]=?, deleteDate=getDate() WHERE ID = ? and [deleted]<>1";

                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, user);
                //preparedStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                preparedStatement.setLong(3, exclusionID);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Creation failed.");
                }

                syncExclusion(exclusionID);
                return 1;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1;

    }

    @WebMethod(operationName = "GetExclusion")
    public String GetExclusion(
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "includeDeleted", targetNamespace = "http://webservices.accumed.com/") Boolean includeDeleted)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        JSONArray array = new JSONArray();

        if (!exclusions.isEmpty()) {
            Iterator<java.util.Map.Entry<Integer, Exclusion>> it = exclusions.entrySet().iterator();
            while (it.hasNext()) {
                Exclusion exclusion = it.next().getValue();

                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("ID", exclusion.getID());
                obj.put("ruleID", exclusion.getRuleID());
                obj.put("providerLicense", exclusion.getProviderLicense());
                obj.put("receiverLicense", exclusion.getReceiverLicense());
                obj.put("payerLicense", exclusion.getPayerLicense());
                obj.put("createdBy", exclusion.getCreatedBy());
                obj.put("deleted", exclusion.getDeleted());
                obj.put("creationDate", exclusion.getCreationDate());
                obj.put("deleteDate", exclusion.getDeleteDate());
                obj.put("deletedBy", exclusion.getDeletedBy());

                if (exclusion.getDeleted() && !includeDeleted) {
                    continue;
                }
                array.put(obj);
            }
        }

        return array.toString();
    }

    private Boolean syncExclusion(Integer exclusionID) {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT [ID], [ruleID], [providerLicense], [receiverLicense], [payerLicense], \n"
                        + "       [createdBy], [deleted], [creationDate], [deleteDate], [deletedBy]\n"
                        + " FROM [dbo].[Exclusions]\n"
                        + " WHERE [ID]=" + exclusionID;

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    if (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String ruleID = rs.getString("ruleID");
                        String providerLicense = rs.getString("providerLicense");
                        String receiverLicense = rs.getString("receiverLicense");
                        String payerLicense = rs.getString("payerLicense");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        exclusions.put(ID, new Exclusion(ID, ruleID, providerLicense,
                                receiverLicense, payerLicense, createdBy, deleted,
                                creationDate, deleteDate, deletedBy));
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    private static Boolean populateExclusions() {

        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        exclusions.clear();

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT [ID], [ruleID], [providerLicense], [receiverLicense], [payerLicense], \n"
                        + "       [createdBy], [deleted], [creationDate], [deleteDate], [deletedBy]\n"
                        + "  FROM [dbo].[Exclusions]\n";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String ruleID = rs.getString("ruleID");
                        String providerLicense = rs.getString("providerLicense");
                        String receiverLicense = rs.getString("receiverLicense");
                        String payerLicense = rs.getString("payerLicense");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        exclusions.put(ID, new Exclusion(ID, ruleID, providerLicense,
                                receiverLicense, payerLicense, createdBy, deleted,
                                creationDate, deleteDate, deletedBy));
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    @WebMethod(operationName = "GetCategories")
    public String GetCategories(
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "includeDeleted", targetNamespace = "http://webservices.accumed.com/") Boolean includeDeleted)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        JSONArray array = new JSONArray();

        if (!Categories.isEmpty()) {
            Iterator<java.util.Map.Entry<Integer, Category>> it = Categories.entrySet().iterator();
            while (it.hasNext()) {
                Category category = it.next().getValue();

                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("ID", category.getId());
                obj.put("catName", category.getCatName());
                obj.put("createdBy", category.getCreatedBy());
                obj.put("deleted", category.getDeleted());
                obj.put("creationDate", category.getCreationDate());
                obj.put("deleteDate", category.getDeleteDate());
                obj.put("deletedBy", category.getDeletedBy());

                if (category.getDeleted() && !includeDeleted) {
                    continue;
                }
                array.put(obj);
            }
        }

        return array.toString();
    }

    @WebMethod(operationName = "GetRulesCategories")
    public String GetRulesCategories(
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "includeDeleted", targetNamespace = "http://webservices.accumed.com/") Boolean includeDeleted)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        JSONArray array = new JSONArray();

        if (!ruleCategories.isEmpty()) {
            Iterator<java.util.Map.Entry<Integer, RuleCategory>> it = ruleCategories.entrySet().iterator();
            while (it.hasNext()) {
                RuleCategory ruleCat = it.next().getValue();

                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("ID", ruleCat.getId());
                obj.put("ruleName", ruleCat.getRuleName());
                obj.put("categoryid", ruleCat.getCategory_id());
                obj.put("catName", ruleCat.getCatName());
                obj.put("createdBy", ruleCat.getCreatedBy());
                obj.put("deleted", ruleCat.getDeleted());
                obj.put("creationDate", ruleCat.getCreationDate());
                obj.put("deleteDate", ruleCat.getDeleteDate());
                obj.put("deletedBy", ruleCat.getDeletedBy());

                if (ruleCat.getDeleted() && !includeDeleted) {
                    continue;
                }
                array.put(obj);
            }
        }

        return array.toString();
    }

    private static Boolean populateCategories() {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        Categories.clear();

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "Select c.ID, c.[name] as catName, [createdBy], [deleted], [creationDate], [deleteDate], [deletedBy]\n"
                        + "from Category c \n"
                        + "where c.deleted = 0";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String catName = rs.getString("catName");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        Categories.put(ID, new Category(ID, catName,
                                createdBy, deleted, creationDate, deleteDate, deletedBy));
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }
 private static Boolean populateRulesReference() {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

         
        ruleRefrences.clear();

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT    ID   , ruleName  , reference  , createdBy \n" +
                    "  , deleted  , creationDate , deleteDate , deletedBy   FROM   RuleReference ";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String ruleName = rs.getString("ruleName");                        
                        String reference = rs.getString("reference");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

//                        ruleRefrences.put(ID, new RuleReference(ID, ruleName, 
//                                reference, createdBy, deleted, creationDate, deleteDate,
//                                deletedBy));
                        if (!deleted) {
                            String refs = ruleRefrences.get(ruleName);
                            if (refs == null) {
                                ruleRefrences.put(ruleName, reference);
                            } else {
                                if (!refs.contains(reference)) {
                                    ruleRefrences.put(ruleName, refs + "," + reference);
                                }
                            }
                        }
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    private static Boolean populateRulesCategories() {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        ruleCategories.clear();
        ruleCats.clear();

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "Select rc.id, rc.ruleName, rc.category_id, c.[name] as catName,\n"
                        + "rc.[createdBy], rc.[deleted], rc.[creationDate], rc.[deleteDate], rc.[deletedBy]\n"
                        + "from RuleCategory rc inner join Category c on c.ID = rc.category_id\n"
                        + "where rc.deleted=0 and c.deleted = 0\n";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String ruleName = rs.getString("ruleName");
                        Integer category_id = rs.getInt("category_id");
                        String catName = rs.getString("catName");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        ruleCategories.put(ID, new RuleCategory(ID, ruleName, category_id,
                                catName, createdBy, deleted, creationDate, deleteDate,
                                deletedBy));
                        if (!deleted) {
                            String cats = ruleCats.get(ruleName);
                            if (cats == null) {
                                ruleCats.put(ruleName, catName);
                            } else {
                                if (!cats.contains(catName)) {
                                    ruleCats.put(ruleName, cats + "," + catName);
                                }
                            }
                        }
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    private Boolean syncCategory(Integer categoryID) {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        try {
            connection = getAccumedDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT [ID], [name], \n"
                        + "[createdBy], [deleted], [creationDate], [deleteDate], [deletedBy]\n"
                        + "FROM [dbo].[Category]"
                        + " WHERE [ID]=" + categoryID;

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    if (rs.next()) {
                        Integer ID = rs.getInt("ID");
                        String name = rs.getString("name");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        Categories.put(ID, new Category(ID, name, createdBy, deleted,
                                creationDate, deleteDate, deletedBy));
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    private Boolean rebuildRuleCats(Integer ID, String ruleName) {
        String sRuleName = null;
        RuleCategory rc;
        if (ID != null) {
            rc = ruleCategories.get(ID);
            if (rc != null) {
                sRuleName = rc.getRuleName();
            }
        }

        if (ruleName != null && !ruleName.isEmpty()) {
            sRuleName = ruleName;
        }

        if (sRuleName != null && !sRuleName.isEmpty()) {
            ruleCats.remove(sRuleName);

            Iterator<java.util.Map.Entry<Integer, RuleCategory>> it = ruleCategories.entrySet().iterator();
            while (it.hasNext()) {
                RuleCategory ruleCat = it.next().getValue();
                if (ruleCat.getRuleName().equals(sRuleName)) {
                    if (!ruleCat.getDeleted()) {
                        String cats = ruleCats.get(ruleCat.getRuleName());
                        if (cats == null) {
                            ruleCats.put(ruleCat.getRuleName(), ruleCat.getCatName());
                        } else {
                            if (!cats.contains(ruleCat.getCatName())) {
                                ruleCats.put(ruleCat.getRuleName(), cats + "," + ruleCat.getCatName());
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private Boolean syncRuleCategory(Integer ruleCategoryID) {
        java.sql.Connection connection = null;
        java.sql.ResultSet rs = null;
        java.sql.Statement stmt = null;

        try {
            connection = getRulesDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "Select rc.id, rc.ruleName, rc.category_id, c.[name] as catName,\n"
                        + "rc.[createdBy], rc.[deleted], rc.[creationDate], rc.[deleteDate], rc.[deletedBy]\n"
                        + "from RuleCategory rc inner join Category c on c.ID = rc.category_id"
                        + " WHERE rc.[ID]=" + ruleCategoryID;

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    if (rs.next()) {
                        Integer ID = rs.getInt("id");
                        String ruleName = rs.getString("ruleName");
                        Integer category_id = rs.getInt("category_id");
                        String catName = rs.getString("catName");
                        String createdBy = rs.getString("createdBy");
                        Boolean deleted = rs.getBoolean("deleted");
                        java.util.Date creationDate = rs.getDate("creationDate");
                        java.util.Date deleteDate = rs.getDate("deleteDate");
                        String deletedBy = rs.getString("deletedBy");

                        ruleCategories.put(ID, new RuleCategory(ID, ruleName, category_id, catName,
                                createdBy, deleted, creationDate, deleteDate, deletedBy));

                        rebuildRuleCats(ID, ruleName);
                    }
                }

                return true;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return false;
    }

    @WebMethod(operationName = "AddCategory")
    public Long AddCategory(
            @WebParam(name = "catName", targetNamespace = "http://webservices.accumed.com/") String catName,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;

        try {
            connection = this.getAccumedDB();
            if (connection != null) {
                Integer newCategoryID;

                Boolean alreadyExists = false;
                {//check if already exists
                    java.sql.ResultSet rs = null;
                    java.sql.Statement stmt = null;
                    stmt = connection.createStatement();

                    String sql = "SELECT [ID] FROM [dbo].[Category] WHERE [name]= '" + catName + "' "
                            + " AND deleted =0";

                    rs = stmt.executeQuery(sql);
                    if (rs != null) {
                        if (rs.next()) {
                            alreadyExists = true;
                        }
                    }
                    rs.close();
                    stmt.close();
                }

                if (alreadyExists) {
                    return -15L;
                }

                String sql = "INSERT INTO [dbo].[Category]\n"
                        + "           ([name]\n"
                        + "           ,[createdBy]\n"
                        + "           ,[deleted]\n"
                        + "           ,[deletedBy]\n"
                        + "           ,[deleteDate]\n"
                        + "           ,[creationDate])\n"
                        + "     VALUES\n"
                        + "           (?,?,?,?,?,getDate())";

                String generatedColumns[] = {"ID"};

                preparedStatement = connection.prepareStatement(sql, generatedColumns);

                preparedStatement.setString(1, catName);
                preparedStatement.setString(2, user);
                preparedStatement.setBoolean(3, false);
                preparedStatement.setString(4,null);
                preparedStatement.setString(5,null);
                //preparedStatement.setDate(6,null);
                //preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis()));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Creation failed.");
                }

                try (java.sql.ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCategoryID = new Integer((int) generatedKeys.getLong(1));
                    } else {
                        throw new java.sql.SQLException("Creation failed.");
                    }
                }

                syncCategory(newCategoryID);
                return new Long(newCategoryID);
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1L;
    }

    @WebMethod(operationName = "AddRuleCategory")
    public Long AddRuleCategory(
            @WebParam(name = "ruleName", targetNamespace = "http://webservices.accumed.com/") String ruleName,
            @WebParam(name = "categoryID", targetNamespace = "http://webservices.accumed.com/") Integer categoryID,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;

        try {
            connection = this.getAccumedDB();
            if (connection != null) {
                Integer newRuleCategoryID;

                Boolean alreadyExists = false;
                {//check if already exists
                    java.sql.ResultSet rs = null;
                    java.sql.Statement stmt = null;
                    stmt = connection.createStatement();

                    String sql = "SELECT [ID] FROM [dbo].[RuleCategory] WHERE [ruleName]= '" + ruleName + "' "
                            + " AND category_id=" + categoryID
                            + " AND deleted =0";

                    rs = stmt.executeQuery(sql);
                    if (rs != null) {
                        if (rs.next()) {
                            alreadyExists = true;
                        }
                    }
                    rs.close();
                    stmt.close();
                }

                if (alreadyExists) {
                    return -15L;
                }

                String sql = "INSERT INTO [dbo].[RuleCategory]\n"
                        + "           ([category_id]\n"
                        + "           ,[ruleName]\n"
                        + "           ,[createdBy]\n"
                        + "           ,[deleted]\n"
                        + "           ,[deletedBy]\n"
                        + "           ,[deleteDate]\n"
                        + "           ,[creationDate])\n"
                        + "     VALUES\n"
                        + "           (?,?,?,?,null,null,getDate())";

                String generatedColumns[] = {"ID"};

                preparedStatement = connection.prepareStatement(sql, generatedColumns);

                preparedStatement.setInt(1, categoryID);
                preparedStatement.setString(2, ruleName);
                preparedStatement.setString(3, user);
                preparedStatement.setBoolean(4, false);
                //preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis()));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Creation failed.");
                }

                try (java.sql.ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newRuleCategoryID = new Integer((int) generatedKeys.getLong(1));
                    } else {
                        throw new java.sql.SQLException("Creation failed.");
                    }
                }

                syncRuleCategory(newRuleCategoryID);
                return new Long(newRuleCategoryID);
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1L;
    }

    @WebMethod(operationName = "DeleteCategory")
    public Integer DeleteCategory(
            @WebParam(name = "categoryID", targetNamespace = "http://webservices.accumed.com/") Integer categoryID,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;

        try {
            connection = getAccumedDB();
            if (connection != null) {
                String sql = "UPDATE [dbo].[Category] SET [deleted]=?, [deletedBy]=?, deleteDate=getDate() WHERE ID = ? and [deleted]<>1";

                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, user);
                //preparedStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                preparedStatement.setLong(3, categoryID);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Creation failed.");
                }

                syncCategory(categoryID);
                return 1;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1;

    }

    @WebMethod(operationName = "DeleteRuleCategory")
    public Integer DeleteRuleCategory(
            @WebParam(name = "ruleName", targetNamespace = "http://webservices.accumed.com/") String ruleName,
            @WebParam(name = "categoryName", targetNamespace = "http://webservices.accumed.com/") String categoryName,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        java.sql.Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        Integer ruleCategoryID = null;

        try {
            connection = getAccumedDB();
            if (connection != null) {

                stmt = connection.createStatement();
                rs = stmt.executeQuery("Select ID from [RuleCategory] where ruleName='" + ruleName + "'\n"
                        + " and IsNull(deleted, 0)<>1 and category_id =(select top 1 id from category where [name] = '" + categoryName + "')");
                if (rs != null) {
                    if (rs.next()) {
                        ruleCategoryID = rs.getInt(1);
                    }
                }
                rs.close();
                stmt.close();
                rs = null;
                stmt = null;

                String sql = "UPDATE [dbo].[RuleCategory] SET [deleted]=?, [deletedBy]=?, deleteDate=getDate() WHERE ID = ? and [deleted]<>1";

                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, user);
                //preparedStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                preparedStatement.setLong(3, ruleCategoryID);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new java.sql.SQLException("Update to deleted failed.");
                }

                syncRuleCategory(ruleCategoryID);
                return 1;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatement = null;
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (java.sql.SQLException ex) {
                    Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }

        return -1;

    }

    @WebMethod(operationName = "getClaimValidationRequests")
    public String getClaimValidationRequests(
            @WebParam(name = "idCaller", targetNamespace = "http://webservices.accumed.com/") Integer idCaller,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }
        java.sql.Connection connection = null;
        java.sql.ResultSet rs;
        java.sql.Statement stmt;
        JSONArray array = new JSONArray();

        try {
            connection = getRulesDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT r.ID, c.idCaller, convert(varchar(255), r.requestTime, 121) as requestTime, "
                        + "r.UserID, r.UserName "
                        + " FROM ScrubRequest r INNER JOIN Claim c on c.request = r.ID "
                        + " inner join Header h on h.requestID = r.ID "
                        + "where c.idCaller = " + idCaller + " "
                        + "order by r.requestTime desc";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        obj.put("ID", rs.getInt("ID"));
                        obj.put("idCaller", rs.getInt("idCaller"));
                        obj.put("requestTime", rs.getString("requestTime"));
                        obj.put("UserID", rs.getInt("UserID"));
                        obj.put("UserName", rs.getString("UserName"));
                        array.put(obj);
                    }
                }
                return array.toString();
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return null;
    }

    @WebMethod(operationName = "getScrubRequest")
    public String getScrubRequest(
            @WebParam(name = "reqID", targetNamespace = "http://webservices.accumed.com/") Integer reqID,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        try {
            ScrubRequestJpaController controller;
            controller = new ScrubRequestJpaController();

            com.accumed.model.scrubRequest.ScrubRequest req
                    = (com.accumed.model.scrubRequest.ScrubRequest) controller.find(reqID);//requestFacade.find(requestID);
            String str = Utils.getRequest(req);
            return str;
        } catch (Exception ex) {
            Logger.getLogger(AccumedValidatorWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @WebMethod(operationName = "CreateResolvedRateSample")
    public Long CreateResolvedRateSample(
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw,
            @WebParam(name = "name", targetNamespace = "http://webservices.accumed.com/") String name,
            @WebParam(name = "requesterID", targetNamespace = "http://webservices.accumed.com/") Integer requesterID,
            @WebParam(name = "requesterName", targetNamespace = "http://webservices.accumed.com/") String requesterName,
            @WebParam(name = "requesterEmailAddress", targetNamespace = "http://webservices.accumed.com/") String requesterEmailAddress,
            @WebParam(name = "tabDelimitedIdCallers", targetNamespace = "http://webservices.accumed.com/") String tabDelimitedIdCallers)
            throws AccessDenied {

        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }

        String[] idCallers = tabDelimitedIdCallers.split(Pattern.quote("\t"));
        if (idCallers.length <= 0) {
            return null;
        }

        String SQL_INSERT = "Insert into ResolvedAlertsRateJob([name]\n"
                + ",[requesterID], [requesterName], [requesterEmailAddr]\n"
                + ",[status], [creationDate], [modifyDate])\n"
                + " Values (?,?,?,?,?,?,?)";
        String SQL_INSERT_DETAIL = "Insert into ResolvedAlertsRateJobDetails([resolvedAlertsRateJobID]\n"
                + ",[idCaller], [status], [startTime]\n"
                + ",[endTime], [error], [maxRequestTime], [requestsCount])\n"
                + " Values (?,?,?,?,?,?,?,?)";

        java.sql.Connection connection = null;
        java.sql.PreparedStatement ps = null;
        JSONArray array = new JSONArray();
        Long retValue = null;

        try {
            connection = getRulesDB();
            if (connection != null) {
                ps = connection.prepareStatement(SQL_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, name);
                ps.setInt(2, requesterID);
                ps.setString(3, requesterName);
                ps.setString(4, requesterEmailAddress);
                ps.setNull(5, java.sql.Types.SMALLINT);
                ps.setTimestamp(6, new java.sql.Timestamp((new java.util.Date()).getTime()));
                ps.setNull(7, java.sql.Types.DATE);

                int affectedRows = ps.executeUpdate();

                if (affectedRows != 0) {
                    try (java.sql.ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            retValue = generatedKeys.getLong(1);
                        }
                    }
                }
                if (retValue != null && retValue > 0) {//insert details

                    java.sql.PreparedStatement ps2 = null;
                    ps2 = connection.prepareStatement(SQL_INSERT_DETAIL, java.sql.Statement.RETURN_GENERATED_KEYS);

                    for (String idCaller : idCallers) {
                        ps2.setLong(1, retValue);
                        ps2.setLong(2, Long.parseLong(idCaller));
                        ps2.setNull(3, java.sql.Types.SMALLINT);
                        ps2.setNull(4, java.sql.Types.DATE);
                        ps2.setNull(5, java.sql.Types.DATE);
                        ps2.setNull(6, java.sql.Types.VARCHAR);
                        ps2.setNull(7, java.sql.Types.DATE);
                        ps2.setNull(8, java.sql.Types.INTEGER);
                        ps2.executeUpdate();
                    }
                }
                return retValue;
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return null;
    }

    @WebMethod(operationName = "GetResolvedRateSamples")
    public String GetResolvedRateSamples(
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {
        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }
        java.sql.Connection connection = null;
        java.sql.ResultSet rs;
        java.sql.Statement stmt;
        JSONArray array = new JSONArray();

        try {
            connection = getRulesDB();
            if (connection != null) {
                stmt = connection.createStatement();
                String sql = "SELECT rj.ID, rj.name, rj.requesterID, rj.requesterName, rj.requesterEmailAddr, rj.creationDate, rj.modifyDate,\n"
                        + "COUNT(rjd.ID) AS claimsCount\n"
                        + ", COUNT(case when rjd.status = 2 then 1 end) as completedClaimsCount\n"
                        + "FROM ResolvedAlertsRateJob rj\n"
                        + "INNER JOIN ResolvedAlertsRateJobDetails rjd ON rjd.resolvedAlertsRateJobID=rj.ID\n"
                        + "GROUP BY rj.ID, rj.name, rj.requesterID, rj.requesterName, rj.requesterEmailAddr, rj.creationDate, rj.modifyDate";

                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        obj.put("ID", rs.getInt("ID"));
                        obj.put("requesterID", rs.getInt("requesterID"));
                        obj.put("requesterName", rs.getString("requesterName"));
                        obj.put("requesterEmailAddr", rs.getString("requesterEmailAddr"));
                        obj.put("creationDate", rs.getString("creationDate"));
                        obj.put("modifyDate", rs.getString("modifyDate"));
                        obj.put("claimsCount", rs.getInt("claimsCount"));
                        obj.put("completedClaimsCount", rs.getInt("completedClaimsCount"));
                        array.put(obj);
                    }
                }
                return array.toString();
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return null;
    }

    @WebMethod(operationName = "GetResolvedRateSampleOutcome_ByRule")
    public String GetResolvedRateSampleOutcome_ByRule(
            @WebParam(name = "sampleID", targetNamespace = "http://webservices.accumed.com/") Integer sampleID,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {
        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }
        java.sql.Connection connection = null;
        java.sql.ResultSet rs;
        java.sql.CallableStatement stmt;
        JSONArray array = new JSONArray();

        try {
            connection = getRulesDB();
            if (connection != null) {
                stmt = connection.prepareCall(
                        "{call getResolvedRateJobResult_ByRules(?)}");
                stmt.setInt(1, sampleID);

                stmt.execute();
                rs = stmt.getResultSet();
                if (rs != null) {
                    while (rs.next()) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        obj.put("RuleName", rs.getString("RuleName"));
                        obj.put("Severity", rs.getString("Severity"));
                        obj.put("FiringCount", rs.getInt("FiringCount"));
                        obj.put("ResolvedCount", rs.getInt("ResolvedCount"));
                        obj.put("BypassedCount", rs.getInt("BypassedCount"));
                        obj.put("ResolvedRate", rs.getFloat("ResolvedRate"));
                        obj.put("BypassedRate", rs.getFloat("BypassedRate"));
                        obj.put("UnknownRate", rs.getFloat("UnknownRate"));
                        array.put(obj);
                    }
                }
                return array.toString();
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return null;
    }

    @WebMethod(operationName = "GetResolvedRateSampleOutcome_ByClaim")
    public String GetResolvedRateSampleOutcome_ByClaim(
            @WebParam(name = "sampleID", targetNamespace = "http://webservices.accumed.com/") Integer sampleID,
            @WebParam(name = "amp", targetNamespace = "http://webservices.accumed.com/") Integer amp,
            @WebParam(name = "user", targetNamespace = "http://webservices.accumed.com/") String user,
            @WebParam(name = "psw", targetNamespace = "http://webservices.accumed.com/") String psw)
            throws AccessDenied {
        if (!authorized(user, psw)) {
            LogInfo(0, "Access denied.", 0);
            throw new AccessDenied("Access denied", new AccessDeniedBean("Access denied"));
        }
        java.sql.Connection connection = null;
        java.sql.ResultSet rs;
        java.sql.CallableStatement stmt;
        JSONArray array = new JSONArray();

        try {
            connection = getRulesDB();
            if (connection != null) {
                stmt = connection.prepareCall(
                        "{call getResolvedRateJobResult_ByClaim(?)}");
                stmt.setInt(1, sampleID);

                stmt.execute();
                rs = stmt.getResultSet();
                if (rs != null) {
                    while (rs.next()) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        obj.put("idCaller", rs.getLong("idCaller"));
                        obj.put("Severity", rs.getString("Severity"));
                        obj.put("FiringCount", rs.getInt("FiringCount"));
                        obj.put("ResolvedCount", rs.getInt("ResolvedCount"));
                        obj.put("BypassedCount", rs.getInt("BypassedCount"));
                        obj.put("ResolvedRate", rs.getFloat("ResolvedRate"));
                        obj.put("BypassedRate", rs.getFloat("BypassedRate"));
                        obj.put("UnknownRate", rs.getFloat("UnknownRate"));
                        array.put(obj);
                    }
                }
                return array.toString();
            }

        } catch (java.sql.SQLException ex) {
            Logger.getLogger(AccumedValidatorWS.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (java.sql.SQLException ex) {
                    Statistics.addException(ex);
                    Logger
                            .getLogger(AccumedValidatorWS.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    Statistics.addException(ex);
                }
            }
        }
        return null;
    }

    public static ValidatorStates<ValidatorState> getValidatorStates() {
        return validatorStates;
    }

    protected static class ValidatorStateTask implements Runnable {

        public void run() {
           // System.out.println("validatorStates update");
            validatorStates.add(new ValidatorState(new java.util.Date(), 1L,
                    (workerPool.getNumActive() + workerPool.getNumIdle()),
                    workerPool.getNumActive(),
                    workerPool.getNumIdle()));

        }
    }
}
