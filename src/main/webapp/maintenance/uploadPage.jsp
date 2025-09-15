<%-- 
    Document   : uploadPage
    Created on : Nov 3, 2020, 9:51:41 AM
    Author     : Anas
--%>

<%@page import="javax.naming.InitialContext"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="java.io.File"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.*,org.apache.commons.fileupload.FileItem"%>
<% if (session.getAttribute("username")!= null){ %>
<html>
    <head>
        <title>UAE Rules Engine Maintenance</title>
        <style>
            input[type=text], select {
                width: 100%;
                padding: 12px 20px;
                margin: 8px 0;
                display: inline-block;
                border: 1px solid #ccc;
                border-radius: 4px;
                box-sizing: border-box;
            }

            input[type=submit] {
                width: 20%;
                background-color: #4CAF50;
                color: white;
                padding: 14px 20px;
                margin: 8px 0;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            }

            input[type=submit]:hover {
                background-color: #45a049;
            }

            .mydiv {
                border-radius: 5px;
                background-color: #f2f2f2;
                padding: 20px;
            }
        </style>
        <style>
            span{
                color: #d6de5C;
                padding-bottom:40px;
                font-size: 30px;
                padding-left:30px ;
                font-weight: bold;
            }

        </style>
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
        <script>
            function onSubmit(txt) {
                if (txt != 'null' && txt != null && txt != "")
                    alert(txt);
            }
        </script>
    </head>
    <%
        String message = (String) session.getAttribute("message");

        if (message != null) {
            session.removeAttribute("message");
            
        }


    %>

    <body onload="onSubmit('<%=message%>')" > 

        <jsp:include page="header.jsp" >
            <jsp:param name="activated" value="Upload Packages" /> 
        </jsp:include>

    <center>
        <div class="mydiv">
            <form action="UploadPackagesProcessServlet" method="post"
                  enctype="multipart/form-data">
                Select File to  Upload:<input type="file" name="fileName" accept=".pkg" name="package" required ><br>
                <input type="submit" value="Upload">
            </form>
        </div>
        <br>

        <div class="mydiv">
            <%
                String fileUploadPath = (String) (new InitialContext().lookup("java:comp/env/com.accumed.rules_packages.dir"));
                String current_root = fileUploadPath + File.separator;

                java.io.File curent_file;
                java.io.File current_dir = new java.io.File(current_root);


            %>
            <span>Current Packages</span>
            <table id="customers">
                <tr>
                    <th>File</th>

                    <th>Last Modification</th>
                    <th>Size (KB)</th>
                </tr>
                <%                    if (current_dir.exists()) {
                        String[] list = current_dir.list();
                        java.util.List<String> arr_list = Arrays.asList(list);
                        Collections.sort(arr_list);
                        //java.util.List<String> arr_list = new java.util.ArrayList<String>();

                        if (arr_list.size() > 0) {%>

                <%for (int i = 0; i < arr_list.size(); i++) {
                        curent_file = new java.io.File(current_root + arr_list.get(i));
                        if (curent_file.isFile()) {
                            String tempName = curent_file.getName();
                %>

                <tr>
                    <td><%=tempName%></td>

                    <td><%=new java.util.Date(curent_file.lastModified())%></td>
                    <td><%=java.lang.Math.ceil((curent_file.length() / 1024.0) * 100) / 100%></td>
                </tr>

                <%}
                            }
                        }
                    }%>
            </table>

        </div>
        <br>
        <div class="mydiv">
            <%
                //String fileUploadPath = (String) (new InitialContext().lookup("java:comp/env/com.accumed.rules_packages.dir"));
                String root = fileUploadPath + "_Backup" + File.separator;

                java.io.File file;
                java.io.File dir = new java.io.File(root);


            %>
            <span>Bauckup Packages</span>
            <table id="customers">
                <tr>
                    <th>File</th>
                    <th>Last Modification</th>
                    <th>Size (KB)</th>
                    <th>Rollback</th>
                </tr>
                <%                    if (dir.exists()) {
                        String[] list = dir.list();
                        java.util.List<String> arr_list = Arrays.asList(list);
                        Collections.sort(arr_list);
                        //java.util.List<String> arr_list = new java.util.ArrayList<String>();

                        if (arr_list.size() > 0) {%>

                <%for (int i = 0; i < arr_list.size(); i++) {
                        file = new java.io.File(root + arr_list.get(i));
                        if (file.isFile()) {
                            String tempName = file.getName();
                %>

                <tr>
                    <td><%=tempName%></td>
                    <td><%=new java.util.Date(file.lastModified())%></td>
                    <td><%=java.lang.Math.ceil((file.length() / 1024.0) * 100) / 100%></td>
                    <td><a href="roolbackProcess?fileName=<%=tempName%>">rollback</a></td>
                </tr>

                <%}
                            }
                        }
                    }%>
            </table>

        </div>
    </center>

</body>

</html>
<% }else{
response.sendRedirect("index.jsp");
}

%>
