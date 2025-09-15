<%-- 
    Document   : encoding Page
    Created on : Nov 3, 2020, 9:51:41 AM
    Author     : Anas
--%>

<%@page import="javax.naming.InitialContext"%>

<%@page import="java.io.File"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
 
<% if (session.getAttribute("username") != null) { %>
<html>
    <head>
        <title>UAE Rules Engine Maintenance</title>
         <script src="../resources/js/jquery-1.11.1.min.js"></script>
        
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
       
            span{
                color: #d6de5C;
                padding-bottom:40px;
                font-size: 30px;
                padding-left:30px ;
                font-weight: bold;
            }

       
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
            function drowRows(){
                 var s = document.getElementById("countOfParameters").innerHTML;
   
             
             var str="";
             for (var i=0;i<s;i++ ){
             str=str+" column name  <input type='txt' name ='columnName"+i+"' width= '30%'> </input>"+ 
                     "<select name='fieldType"+i+"' id='fieldType"+i+"' width= '30%' >"+
                    " <option value='Int'>Int</option>"+
                    "<option value='Varchar'>Varchar</option>"+
                    "<option value='String'>String</option>"+
                      "</select>";
                               }
                               alert(str);
                               $("#columnsSpace").val(str);
                             //  document.getElementById("columnsSpace").innerHTML=+str;
            }
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
            <jsp:param name="activated" value="Encode Data" /> 
        </jsp:include>

    <center>
        <div class="mydiv">
            <form action="EncodeDataServlet" method="post">
                select  query:<textarea id="selectArea" name="selectArea" rows="4" cols="50" required> </textarea>
                <br>
                Insert  query:<textarea id="insertArea" name="selectArea" rows="4" cols="50" required> </textarea><br>
               Number of Fields to  be encrypted :<select name="countOfParameters" id="countOfParameters"  width= "30%" onchange="drowRows()" >
                    <option value="0">0</option>
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                     <option value="4">4</option>
                    <option value="5">5</option>
                    <option value="6">6</option>
                     <option value="7">7</option>
                    <option value="8">8</option>
                    <option value="9">9</option>
                    <option value="10">10</option>
                </select> 
               <div id="columnsSpace"> </div>
               
                </br>
                <input type="submit" value="Encode Data">
            </form>
        </div>
        <br>


    </center>

</body>

</html>
<% } else {
        response.sendRedirect("index.jsp");
    }

%>
