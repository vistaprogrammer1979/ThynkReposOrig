/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.webservices;

import com.accumed.re.agents.CachedRepositoryService;
import com.accumed.re.agents.ResolvedRateJobService;
import com.accumed.re.agents.WorkLogger;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author smutlak
 */
public class BackgroundTaskManager implements ServletContextListener {

    private static final int MAXIMUM_CURRENT = 3;
    private static int INITIAL_DELAY = 10;
    private static int REPO_CHECK_INTERVAL = 30;

    static {
        try {
            INITIAL_DELAY = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.initialDelay")));
            REPO_CHECK_INTERVAL = Integer.parseInt((String) (new InitialContext().lookup("java:comp/env/com.accumed.rules.engine.repository.check.interval")));
        } catch (Exception ex) {
            Logger.getLogger(BackgroundTaskManager.class.getName()).log(Level.SEVERE,
                    "exception caught", ex);
        }
        Logger.getLogger(BackgroundTaskManager.class
                .getName()).log(Level.INFO, "initialDelay={0}, check.interval={1}",
                        new Object[]{INITIAL_DELAY, REPO_CHECK_INTERVAL});
    }

    private static ScheduledThreadPoolExecutor executor = null;
    private static ScheduledFuture cachedRepositoryFuture;
    private static ScheduledFuture droolsUpdaterFuture;
    private static ScheduledFuture workLoggerFuture;
    private static ScheduledFuture ResolvedRateJobFuture;

    synchronized public static void restartAgents() {
        if (executor != null) {
            executor.shutdownNow();//shutdown();
            while (!executor.isTerminated()) {
                Logger.getLogger(BackgroundTaskManager.class.getName()).log(Level.INFO, "Waiting agents to shutdown..");
                Logger.getLogger(BackgroundTaskManager.class.getName()).log(Level.SEVERE, "Exception: Waiting agents to shutdown..");
            }
            executor = null;
        }
        executor = new ScheduledThreadPoolExecutor(MAXIMUM_CURRENT);

        cachedRepositoryFuture = executor.scheduleWithFixedDelay(new CachedRepositoryService(),
                INITIAL_DELAY, REPO_CHECK_INTERVAL, TimeUnit.SECONDS);
        /*droolsUpdaterFuture = executor.scheduleWithFixedDelay(new DroolsUpdaterService(),
                ((INITIAL_DELAY*100) + REPO_CHECK_INTERVAL) / 2,
                ((INITIAL_DELAY*100) + REPO_CHECK_INTERVAL) / 2, TimeUnit.SECONDS);*/

        WorkLogger workLogger = new WorkLogger();
        workLoggerFuture = executor.scheduleWithFixedDelay(workLogger,
                INITIAL_DELAY, 30, TimeUnit.SECONDS);
        
      //  ResolvedRateJobFuture = executor.scheduleWithFixedDelay(new ResolvedRateJobService(),
        //        10, 15, TimeUnit.MINUTES);
        
        AccumedValidatorWS.setWorkLogger(workLogger);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
        executor = null;
        if (AccumedValidatorWS.saveFixedPool != null) {
            AccumedValidatorWS.saveFixedPool.shutdown();
        }
        if (AccumedValidatorWS.returnFixedPool != null) {
            AccumedValidatorWS.returnFixedPool.shutdown();
        }

        Logger.getLogger(
                BackgroundTaskManager.class.getName()).log(Level.INFO, "contextDestroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        restartAgents();

//        BasicConfigurator.configure();
        Logger.getLogger(
                BackgroundTaskManager.class.getName()).log(Level.INFO, "contextInitialized");
    }

    public static boolean isRunningCachedRepositoryFuture() {
        return (cachedRepositoryFuture == null || !(cachedRepositoryFuture instanceof ScheduledFuture)) ? false
                : cachedRepositoryFuture.getDelay(TimeUnit.MILLISECONDS) > 0;
    }

    public static boolean isRunningDroolsUpdaterFuture() {
        return (droolsUpdaterFuture == null || !(droolsUpdaterFuture instanceof ScheduledFuture)) ? false
                : droolsUpdaterFuture.getDelay(TimeUnit.MILLISECONDS) > 0;
    }

    public static boolean isRunningWorkLoggerFuture() {
        return (workLoggerFuture == null || !(workLoggerFuture instanceof ScheduledFuture)) ? false
                : workLoggerFuture.getDelay(TimeUnit.MILLISECONDS) > 0;
    }
}
