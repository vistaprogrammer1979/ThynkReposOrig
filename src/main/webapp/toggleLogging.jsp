<%@ page import="com.accumed.webservices.LoggingManager" %>
<%
    String action1 = request.getParameter("action1");
    String action2 = request.getParameter("action2");
    LoggingManager loggingManager = LoggingManager.getInstance();

    if ("enable".equals(action1)) {
        loggingManager.setLogInfoEnabled(true);
    } else if ("disable".equals(action1)) {
        loggingManager.setLogInfoEnabled(false);
    }
    if ("enable".equals(action2)) {
        loggingManager.setLogRequest(true);
    } else if ("disable".equals(action2)) {
        loggingManager.setLogRequest(false);
    }

    // Redirect back to the original page
    response.sendRedirect("index.jsp");
%>