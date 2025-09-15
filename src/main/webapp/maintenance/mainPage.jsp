<%-- 
    Document   : mainPage
    Created on : Nov 3, 2020, 9:49:25 AM
    Author     : Anas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% if (session.getAttribute("username") != null) { %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>UAE Rules Engine Maintenance</title>

    </head>
    <body>
        <jsp:include page="header.jsp" >
            <jsp:param name="activated" value="Home" /> 
        </jsp:include>
    <center>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <h1 style="color: #d6de5C;padding-bottom:40px;font-size: 50px;padding-left:30px ;font-weight: bold;">
            Welcome In Rules Engine Assistant
        </h1>
    </center>
</body>
</html>
<% } else {
        response.sendRedirect("index.jsp");
    }

%>
