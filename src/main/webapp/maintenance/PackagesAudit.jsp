<%-- 
    Document   : audit.jsp
    Created on : Nov 9, 2020, 5:05:46 PM
    Author     : Anas
--%>

<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.io.File"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Arrays"%>
<%@page import="com.accumed.maintenance.PackagesAuditService"%>
<%@page import="com.accumed.maintenance.PackageAudit"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% if (session.getAttribute("username") != null) { %>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>UAE Rules Engine Maintenance</title>
        <link rel="stylesheet" type="text/css" href="../resources/css/jquery.dataTables.min.css">
        <script type="text/javascript" charset="utf8" src="../resources/js/jquery-3.5.1.min.js"></script>
        <script type="text/javascript" charset="utf8" src="../resources/js/jquery.dataTables.min.js"></script>
        <style>
            #customers {
                font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
                border-collapse: collapse;
                width: 100%;
            }

            #customers td, #customers th {
                border: 1px solid #ddd;
                padding: 8px;
            }

            #customers tr:nth-child(even){background-color: #f2f2f2;}

            #customers tr:hover {background-color: #ddd;}

            #customers th {
                padding-top: 12px;
                padding-bottom: 12px;
                text-align: left;
                background-color: #4CAF50;
                color: white;
            }

        </style>
        <style>
            div.ex1 {
                background-color: lightblue;
                width: 110px;
                height: 110px;
                overflow: scroll;
            }
            div.mytable {
                height: 400px;



            }
            ul {
                list-style-type: none;
                margin: 0;
                padding: 0;
                overflow: hidden;
                background-color: #333;
            }

            li {
                float: left;
            }

            li a {
                display: block;
                color: white;
                text-align: center;
                padding: 14px 16px;
                text-decoration: none;
            }

            li a:hover:not(.active) {
                background-color: #111;
            }

            .active {
                background-color: #4CAF50;
            }
        </style>
        <script>
            $(document).ready(function () {
                $('#customers').DataTable({
                    
                  
                    "sPaginationType": "full_numbers",

                    //SHOW PROCESSING BAR
                    "bProcessing": true,

                    //ENABLE RELOADING
                    "bRetreive": true,

                    //ENABLE THEME ROLLER
                    "bJQueryUI": true,

                    //AUTO DESTROY WHEN REINITIALIZED
                    "bDestroy": true,
                    scroller: {
                        loadingIndicator: true}

                });
            });
        </script>

    </head>
    <body>



        <jsp:include page="header.jsp" >
            <jsp:param name="activated" value="Packages Audit" /> 
        </jsp:include>


        <%

            PackagesAuditService service = new PackagesAuditService();
            List<PackageAudit> list = service.getPackagesAudit();
        %>

        <div class="mytable">
            <table id="customers">
                <thead>
                    <tr>
                        <th>OBJ_TYPE</th>
                        <th>OBJ_NAME</th>
                        <th>UPDATE_DATE</th>
                        <th>last UPDATE_USER_NAME</th>
                    </tr>
                </thead>
                <tbody>
                    <%if (list.size() > 0) {
                            for (PackageAudit row : list) {

                    %>
                    <tr>
                        <td><%if (row.getOBJ_TYPE() == 0) {%><%="update"%><%}
                            if (row.getOBJ_TYPE() == 1) {%><%="roleback"%><%}%></td>
                        <td><%=row.getOBJ_NAME()%></td>
                        <td><%=row.getUPDATE_DATE()%></td>
                        <td><%=row.getUPDATE_USER_NAME()%> </td>
                    </tr>
                    <%}
                        } %>
                </tbody>
            </table>
        </div>

    </body>
</html>
<% } else {
        response.sendRedirect("index.jsp");
    }

%>
