<%-- 
    Document   : downloadLog
    Created on : Nov 3, 2020, 9:47:27 AM
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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% if (session.getAttribute("username") != null) { %>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>UAE Rules Engine Maintenance</title>
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
    </head>
    <body>


        <jsp:include page="header.jsp" >
            <jsp:param name="activated" value="Download Log" /> 
        </jsp:include>

        <h1>Download Log Files!</h1>

        <%
            File absolute = new File("");
            String absolute_path="";
            if (absolute.getAbsolutePath().contains("bin")){
                 absolute_path = absolute.getAbsolutePath().replaceFirst("bin", "logs");
            }else{
                absolute_path = "logs";
            }
            java.io.File dir = new java.io.File(absolute_path);
            String[] list = dir.list();
        %>
        <%                
            java.util.HashMap<Long,File> hash = new java.util.HashMap<Long,File>();
            for (String temp_file_name : list) {
                
                File temp_File = new File(absolute_path+File.separator+temp_file_name);
                hash.put(temp_File.lastModified(),temp_File);
            }
            java.util.List<java.util.Map.Entry<Long,File>> sortedFiles = new java.util.ArrayList<java.util.Map.Entry<Long,File>>(hash.entrySet());
            java.util.Collections.sort(sortedFiles, new java.util.Comparator<java.util.Map.Entry<Long,File>>() {
                @Override
                public int compare(java.util.Map.Entry<Long,File> e1, java.util.Map.Entry<Long,File> e2) {
                    if (e1 != null && e2 != null) {
                        if ((new Date(e1.getKey())).before(new Date(e2.getKey()))) {
                            return 1;
                        } else {
                            if ((new Date(e1.getKey())).equals(new Date(e2.getKey()))) {
                                return 0;
                            } else {
                                return -1;
                            }
                        }
                    }
                    return 0;
                }
            });
            List<File> result = new ArrayList<File>();
            int j = 0;
            while (j < 32 && j < sortedFiles.size()) {
                result.add(sortedFiles.get(j).getValue());
                j+=1;
            }
        %>
        <%if (list.length > 0) {%>
        <table id="customers">
            <tr>
                <th>file</th>
                <th>size</th>
                <th>last modification</th>
                <th>download</th>
            </tr>
            <%for (File f:result) {
                    if (f.isFile()) {
                        String temp_name = absolute_path+File.separator+f.getName();
            %>

            <tr>
                <td><%=f.getName()%></td>
                <td><%=java.lang.Math.ceil((f.length() / 1024.0) * 100) / 100%></td>
                <td><%=(new java.util.Date(f.lastModified())).toString()%></td>
                <td><a href="UploadDownloadFileServlet?fileName=<%=temp_name%>" >download</a></td>
            </tr>

            <%
                    }
                }%>
        </table>
        <%}
        %>
    </body>
</html>
<% } else {
        response.sendRedirect("index.jsp");
    }

%>