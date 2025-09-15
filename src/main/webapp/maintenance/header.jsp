<%-- 
    Document   : header
    Created on : Nov 3, 2020, 9:41:55 AM
    Author     : Anas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <!--    <style>
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
        </style>-->
    <style>
        body {font-family: Arial, Helvetica, sans-serif;}

        .navbar {
            width: 100%;
            background-color: #555;
            overflow: auto;
        }

        .navbar a {
            float: left;
            padding: 12px;
            color: white;
            text-decoration: none;
            font-size: 17px;
        }

        .navbar a:hover {
            background-color: #000;
        }

        .active {
            background-color: #4CAF50;
        }

        @media screen and (max-width: 500px) {
            .navbar a {
                float: none;
                display: block;
            }
        }
    </style>

</head>
<body>
    <% String activated = request.getParameter("activated");%>
 
    <div class="navbar">
        <a 
            <% if (activated.equals("Home")) { %> class="active"  <%}%> 
            
            href="../index.jsp"
           
            >
            <i class="fa fa-fw fa-home"></i> Statistics
        </a> 
        <a 
            <% if (activated.equals("Upload Packages")) { %> class="active"  <%}%>
            <% if (session.getAttribute("username") == null) { %>
            href="index.jsp"
            <% } else {%>
            href="uploadPage.jsp"
            <% } %>
            >
            <i class="fa fa-fw fa-search"></i> Upload Packages
        </a> 
        <a 
            <% if (activated.equals("Download Log")) { %> class="active"  <%}%>
            <% if (session.getAttribute("username") == null) { %>
            href="index.jsp"
            <% } else {%>
            href="downloadLog.jsp"
            <% } %>
            >
            <i class="fa fa-fw fa-envelope"></i> Download Log
        </a>
                    <a 
            <% if (activated.equals("Packages Audit")) { %> class="active"  <%}%>
            <% if (session.getAttribute("username") == null) { %>
            href="index.jsp"
            <% } else {%>
            href="PackagesAudit.jsp"
            <% } %>
            >
            <i class="fa fa-fw fa-envelope"></i> Packages Audit
        </a> 
        <a onclick=""
           <% if (activated.equals("Logout")) { %> class="active"  <%}%>
           <% if (session.getAttribute("username") == null) { %>
           href="index.jsp"
           <% } else {%>
           href="Logout.jsp"
           <% }%>
           >
            <i class="fa fa-fw fa-user"></i> Logout
        </a>
    </div>

</body>   
