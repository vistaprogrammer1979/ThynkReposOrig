<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="com.accumed.webservices.AccumedValidatorWS" %>
<%@ page import="com.accumed.webservices.LoggingManager" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>THYNK UAE</title>
        <script src="resources/js/jquery-1.11.1.min.js"></script>
        <script src="resources/js/jquery.canvasjs.min.js"></script>
        <style>
            body{
                font-size: 10pt;
            }
            #main{
                width: 100%;
                text-align: center;
            }
            #titleContainer{
                font: bold 25pt monospace;
                color: midnightblue; 
            }
            .peFieldSet{
                padding-left: 10px;
                margin-right: 15px;
                margin-left: 5px;
                margin-bottom: 10px;
            }
            legend{
                color: midnightblue;
                font: bold 12pt monospace;
            }
            .peTabDiv{
                padding-left: 50px;
                padding-right: 25px;
            }
            .peLabelInTable{
                text-align: left;
            }
            .peValueInTable{
                text-align: left;
            }
            .peSepInTable{
                text-align: left;
                width:50px;
            }
            /*  .chartWrapper {
                  position: relative;
              }
  
              .chartWrapper > canvas {
                  position: absolute;
                  left: 0;
                  top: 0;
                  pointer-events:none;
              }
  
              .chartAreaWrapper {
                  width: 600px;
                  overflow-x: scroll;
              }*/
        </style>
        <script>
            window.onload = function () {

            var options = {
            animationEnabled: true,
                    theme: "light2",
                    title: {
                    text: "Supervisors Total-Active"
                    },
                    axisX: {
                    valueFormatString: "MMM,DD HH:mm"
                    },
                    axisY: {
                    title: "Supervisors",
                            suffix: "",
                            minimum: 0
                    },
                    toolTip: {
                    shared: true
                    },
                    legend: {
                    cursor: "pointer",
                            verticalAlign: "bottom",
                            horizontalAlign: "left",
                            dockInsidePlotArea: true,
                            itemclick: toogleDataSeries
                    },
                    data: [{
                    type: "line",
                            showInLegend: true,
                            name: "Active",
                            markerType: "square",
                            xValueFormatString: "MMM,DD HH:mm",
                            color: "#FF0000",
                            yValueFormatString: "#,##0",
                            dataPoints: [
            <% for (int i = 0; i < com.accumed.webservices.AccumedValidatorWS.getValidatorStates().size() - 1; i++) {%>
                            {x: new Date(<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.YEAR)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.MONTH)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.DAY_OF_MONTH)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.HOUR_OF_DAY)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.MINUTE)%>), y:<%=com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getSupervisorsActive()%>},
            <% } %>
                            ]
                    },
                    {
                    type: "line",
                            showInLegend: true,
                            name: "Count",
                            lineDashType: "dash",
                            yValueFormatString: "#,##0",
                            dataPoints: [
            <% for (int i = 0; i < com.accumed.webservices.AccumedValidatorWS.getValidatorStates().size() - 1; i++) {%>
                            {x: new Date(<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.YEAR)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.MONTH)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.DAY_OF_MONTH)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.HOUR_OF_DAY)%>,<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getCalendar().get(java.util.Calendar.MINUTE)%>), y:<%=com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getSupervisorsCount()%>},
            <% } %>

                            ]
                    }]
            };
            $("#chartContainer").CanvasJSChart(options);
            function toogleDataSeries(e) {
            if (typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
            e.dataSeries.visible = false;
            } else {
            e.dataSeries.visible = true;
            }
            e.chart.render();
            }

            }

            <% for (int i = 0; i < com.accumed.webservices.AccumedValidatorWS.getValidatorStates().size(); i++) {%>
            colArray[<%= i%>] = "<%= com.accumed.webservices.AccumedValidatorWS.getValidatorStates().get(i).getTime()%>";
            <% } %>
        </script>
    </head>
    <body>
        <form action="${pageContext.request.contextPath}/jspActionsServlet" method="post">
            <%

                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd,MMM yyyy hh:mm:ss");
                com.accumed.re.agents.repo.CachedRepository repo = null;
                com.accumed.re.pool.WorkersPool workersPool = null;
                int workersCount = 0;
//                int asynchronousAccountants = 0;
//                int synchronousAccountants = 0;
                String LogHistory = com.accumed.webservices.Statistics.isLogHistory() ? "Disable Logging patient claim History" : "Enable Logging patient claim History";
                
                String DBRules = com.accumed.webservices.Statistics.isDisableDBRules() ? "Enable DB rules" : "Disable DB rules";
                int busyWorkersCount = 0;
                int returnFixedPoolSize = 0;
                int saveFixedPool = 0;
                String rulesLaws = "";
                String rulesLawsLoadingTime = "";
                java.util.HashMap<String, Long> tables = null;

                if (com.accumed.webservices.AccumedValidatorWS.getCachedRepositoryService() != null
                        && com.accumed.webservices.AccumedValidatorWS.getCachedRepositoryService().getRepo() != null
                        && com.accumed.webservices.AccumedValidatorWS.getCachedRepositoryService().getRepo().getCachedDB() != null) {
                    repo = com.accumed.webservices.AccumedValidatorWS.getCachedRepositoryService().getRepo();
                    tables = repo.getDistinctTableList();

                    if (com.accumed.webservices.AccumedValidatorWS.getWorkersPool() != null) {
                        workersPool = com.accumed.webservices.AccumedValidatorWS.getWorkersPool();
                        if (workersPool != null) {
                            workersCount = workersPool.getCount();
//                            asynchronousAccountants = workersPool.getNumIdle();
//                            synchronousAccountants = workersPool.getNumIdle();
                            busyWorkersCount = workersPool.getNumActive();
                            rulesLaws = com.accumed.re.pool.Worker.getPackagesDir();
                            java.util.Date rPackageTime= workersPool.getRulesPackageTime();
                            if (rPackageTime != null) {
                                rulesLawsLoadingTime = formatter.format(rPackageTime);
                            }
                        }
                    }
                    if (com.accumed.webservices.AccumedValidatorWS.getReturnFixedPool() != null) {
                        //returnFixedPoolSize = com.accumed.webservices.AccumedValidatorWS.getReturnFixedPool().getQueue().size();
                        java.util.concurrent.ScheduledExecutorService Scheduled_Executor_Service = com.accumed.webservices.AccumedValidatorWS.getReturnFixedPool();
                        if (Scheduled_Executor_Service instanceof java.util.concurrent.ScheduledThreadPoolExecutor) {
                            java.util.concurrent.ScheduledThreadPoolExecutor implementation = (java.util.concurrent.ScheduledThreadPoolExecutor) Scheduled_Executor_Service;
                            returnFixedPoolSize = implementation.getQueue().size();
                        }
                    }
                    if (com.accumed.webservices.AccumedValidatorWS.getSaveFixedPool() != null) {
                        saveFixedPool = com.accumed.webservices.AccumedValidatorWS.getSaveFixedPool().getQueue().size();
                    }
                }

                String sMinRequest = "0";
                if (AccumedValidatorWS.getMinRequest() != null) {
                    sMinRequest = AccumedValidatorWS.getMinRequest().getPeriodInMilli() + "/APM-" + AccumedValidatorWS.getMinRequest().getClaimId();
                }

                String sMaxRequest = "0";
                if (AccumedValidatorWS.getMaxRequest() != null) {
                    sMaxRequest = AccumedValidatorWS.getMaxRequest().getPeriodInMilli() + "/APM-" + AccumedValidatorWS.getMaxRequest().getClaimId();
                }

                String sLastMinuteRequestCount = "0";
                String sLastMinuteAverageRequestTime = "0";
                if (AccumedValidatorWS.getWorkLogger() != null) {
                    com.accumed.re.agents.Pair<Long, Long> pair = AccumedValidatorWS.getWorkLogger().getLastMinuteAverageRequestTime(1);
                    if (pair.getElement0() > 0) {
                        sLastMinuteAverageRequestTime = (pair.getElement1() / pair.getElement0()) + "";
                        sLastMinuteRequestCount = pair.getElement0() + "";
                    }
                }

                String sLast10MinuteRequestCount = "0";
                String sLast10MinuteAverageRequestTime = "0";
                if (AccumedValidatorWS.getWorkLogger() != null) {
                    com.accumed.re.agents.Pair<Long, Long> pair = AccumedValidatorWS.getWorkLogger().getLastMinuteAverageRequestTime(10);
                    if (pair.getElement0() > 0) {
                        sLast10MinuteAverageRequestTime = (pair.getElement1() / pair.getElement0()) + "";
                        sLast10MinuteRequestCount = pair.getElement0() + "";
                    }
                }

                String sLast30MinuteRequestCount = "0";
                String sLast30MinuteAverageRequestTime = "0";
                if (AccumedValidatorWS.getWorkLogger() != null) {
                    com.accumed.re.agents.Pair<Long, Long> pair = AccumedValidatorWS.getWorkLogger().getLastMinuteAverageRequestTime(30);
                    if (pair.getElement0() > 0) {
                        sLast30MinuteAverageRequestTime = (pair.getElement1() / pair.getElement0()) + "";
                        sLast30MinuteRequestCount = pair.getElement0() + "";
                    }
                }

                boolean isCachedRepositoryAgentRunning = false;
                boolean isDroolsUpdaterAgentRunning = false;
                boolean isWorkLoggerAgentRunning = false;
                isCachedRepositoryAgentRunning = com.accumed.webservices.BackgroundTaskManager.isRunningCachedRepositoryFuture();
                isDroolsUpdaterAgentRunning = com.accumed.webservices.BackgroundTaskManager.isRunningDroolsUpdaterFuture();
                isWorkLoggerAgentRunning = com.accumed.webservices.BackgroundTaskManager.isRunningWorkLoggerFuture();
                LoggingManager loggingManager = LoggingManager.getInstance();
                boolean logInfoEnabled = loggingManager.isLogInfoEnabled();
                boolean logRequest = loggingManager.isLogRequest();

            %>

            <table id="main">
                <tr><td><div id="titleContainer"><label id="title">THYNK UAE</label></div></td></tr>
            </table>
            <br/>
            <div id="chartContainer" style="height: 300px; width: 100%; overflow-x: auto; overflow-y:hidden;">
            </div>
            <br/>
            <hr style="height:5px; background-color: black;"/>
            <%if (repo != null && workersPool != null) {%>
            <table>
                <tr>
                    <td>
                        <fieldset class="peFieldSet" title="Cached Repository">
                            <legend>Cached Repository</legend>
                            <table>
                                <tr>
                                    <td>
                                        <table class="peTabDiv">
                                            <tr><td class="peLabelInTable">Status:</td><td class="peValueInTable"><%=repo.isValid() ? "valid" : "<span style='color:red;'>invalid</span>"%></td><td class="peSepInTable"></td></tr>
                                            <tr><td class="peLabelInTable">Cached tables count:</td><td class="peValueInTable"><%=repo.getDistinctTableList().size()%></td></tr>
                                            <tr><td class="peLabelInTable">Invalid objects count:</td><td class="peValueInTable"><%=repo.getInvalidCachedDataCount() == 0 ? "0" : "<span style='color:red;'>" + repo.getInvalidCachedDataCount() + "</span>"%></td></tr>
                                            <tr><td class="peLabelInTable">Timestamp:</td><td class="peValueInTable"><%=formatter.format(repo.getTimeStamp())%></td><td class="peSepInTable"></td></tr>
                                        </table>
                                    </td>
                                    <td>
                                        <div style="height:150px; width:250px;overflow: auto;">
                                            <p>Cached Tables</p>
                                            <table>
                                                <% if (tables != null) {
                                                        int cnt = 0;

                                                        for (String sKey : tables.keySet()) {
                                                            //entry.getValue()
                                                %>
                                                <tr><td><%=++cnt%></td><td><%=sKey%></td></tr>
                                                <%}
                                                    }%>
                                            </table>
                                        </div>

                                    </td>
                                    <td>
                                        <div style="height:150px; width:250px;overflow: auto;">
                                            <!--<p>Loaded custom contracts</p>
                                            <table>
                                                
                                            </table>-->
                                        </div>

                                    </td>
                                </tr>
                            </table>
                        </fieldset>
                        <fieldset class="peFieldSet" title="Rules Supervisors">
                            <legend>Rules Supervisors</legend>
                            <table class="peTabDiv">
                                <tr><td class="peLabelInTable">Supervisors Count</td><td class="peValueInTable"><%=workersCount%></td><td class="peSepInTable"></td><td class="peLabelInTable"><!--Synchronous Accountants-->--</td><td class="peValueInTable">--</td><td class="peSepInTable"></td><td class="peLabelInTable">Supervision Laws</td><td class="peValueInTable"><%=rulesLaws%></td></tr>
                                <tr><td class="peLabelInTable">Busy Supervisors</td><td class="peValueInTable"><%=busyWorkersCount%></td><td class="peSepInTable"></td><td class="peLabelInTable"><!--Asyn Workers-->--</td><td class="peValueInTable">--</td><td class="peSepInTable"></td><td class="peLabelInTable">Laws versions</td><td class="peValueInTable"><%=rulesLawsLoadingTime%></td></tr>
                            </table>
                        </fieldset>
                        <fieldset class="peFieldSet" title="Service Agents">
                            <legend>Service Agents</legend>
                            <table class="peTabDiv">
                                <tr><td class="peLabelInTable">Cached Repository Agent</td><td class="peValueInTable"><%=isCachedRepositoryAgentRunning ? "running" : "<span style='color:red;'>stopped</span>"%></td><td class="peSepInTable"></td><td class="peLabelInTable"><input type="submit" name="RestartAllAgents" value="Restart All"/></td><td class="peLabelInTable">Cleansing queue size</td><td class="peValueInTable"><%=returnFixedPoolSize%></td></tr>
                                <!--<tr><td class="peLabelInTable">Laws Agent</td><td class="peValueInTable"><%=isDroolsUpdaterAgentRunning ? "running" : "<span style='color:red;'>stopped</span>"%></td><td class="peSepInTable"></td><td class="peLabelInTable">--</td></tr>-->
                                <tr><td class="peLabelInTable">Logger Agent</td><td class="peValueInTable"><%=isWorkLoggerAgentRunning ? "running" : "<span style='color:red;'>stopped</span>"%></td><td class="peSepInTable"></td><td class="peLabelInTable">--</td><td class="peLabelInTable">Logging queue size</td><td class="peValueInTable"><%=saveFixedPool%></td></tr>
                            </table>
                        </fieldset>

                        <fieldset class="peFieldSet" title="Rules Service" style=" margin-bottom: 0px;">
                            <legend>Rules Service</legend>
                            <table class="peTabDiv">
                                <tr><td class="peLabelInTable">--</td><td class="peValueInTable">--</td><td class=nTable"></td><td class="peLabelInTable">Last request time:</td><td class="peValueInTable"><%=AccumedValidatorWS.getLastRequestTime() == null ? "0" : AccumedValidatorWS.getLastRequestTime()%></td></tr>
                                <tr><td class="peLabelInTable">Average validation time:</td><td class="peValueInTable"><%=AccumedValidatorWS.getAverageClaimProcessTime() == null ? "0" : AccumedValidatorWS.getAverageClaimProcessTime()%></td><td class=nTable"></td><td class="peLabelInTable">Last minute validation claims count:</td><td class="peValueInTable"><%=sLastMinuteRequestCount%></td></tr>
                                <tr><td class="peLabelInTable">Min. validation time:</td><td class="peValueInTable"><%=sMinRequest%></td><td class="peSepInTable"></td><td class="peLabelInTable">Last 10 minutes validation claims count:</td><td class="peValueInTable"><%=sLast10MinuteRequestCount%></td></tr>
                                <tr><td class="peLabelInTable">Max. validation time:</td><td class="peValueInTable"><%=sMaxRequest%></td><td class=nTable"></td><td class="peLabelInTable">Last 30 minutes validation claims count:</td><td class="peValueInTable"><%=sLast30MinuteRequestCount%></td></tr>
                                <tr><td class="peLabelInTable">Last minute average time:</td><td class="peValueInTable"><%=sLastMinuteAverageRequestTime%></td><td class="peSepInTable"></td><td class="peLabelInTable">Total requests:</td><td class="peValueInTable"><%=AccumedValidatorWS.getTotalRequests()%></td></tr>
                                <tr><td class="peLabelInTable">Last 10 minutes average time:</td><td class="peValueInTable"><%=sLast10MinuteAverageRequestTime%></td><td class="peSepInTable"></td><td class="peLabelInTable">Total validation claims count:</td><td class="peValueInTable"><%=AccumedValidatorWS.getTotalProcessedClaims()%></td></tr>
                            </table>
                        </fieldset>
                    </td>
                    <td valign="top">
                        <fieldset class="peFieldSet" title="Options">
                            <legend>Options</legend>
                            <table>
                                <tr><td>
                                        <input type="submit" name="LoggingPatientHistory" value="<%=LogHistory%>"/>
                                    </td></tr>
                                <tr><td>
                                        <input type="submit"  name="EnableDBRules" value="<%=DBRules%>"/>
                                    </td></tr>
                            </table>
                        </fieldset>
                        <fieldset class="peFieldSet" title="Claim validation history">
                            <legend>Claim validation history</legend>

                        </fieldset>
                    </td>
                </tr>
            </table>
            <table id="footer" style="width:100%;">
                <tr><td style=" text-align: right;"><%@ include file="version.jsp" %></td></tr>
            </table>
            <%} else {%>
            <div style="font-size:35pt; color: red; width: 100%; height: 100%; text-align: center; vertical-align: middle;">Initializing ...</div>

            <%}%>
        </form>
          <form action="toggleLogging.jsp" method="post">
            <input type="hidden" name="action2" value="<%= logRequest ? "disable" : "enable"%>">
            <button type="submit">
                <%= logRequest ? "Disable Request Logging" : "Enable Request Logging"%>
            </button>
            :<label><%= logRequest ? "Request Logging Is Enabled" : "Request Logging Is Disabled"%></label>
        </form>
        
    </body>
</html>
