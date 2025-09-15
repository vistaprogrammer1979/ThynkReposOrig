<%-- 
    Document   : index
    Created on : Nov 3, 2020, 9:39:56 AM
    Author     : Anas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>UAE Rules Engine Maintenance</title>
        <script>
            function onSubmit(txt) {
                if (txt != 'null' && txt != null && txt != "")
                    alert(txt);
            }
        </script>
        <%
        String message=(String) session.getAttribute("ErrorMessage");
        
         if (message != null) {
            session.removeAttribute("message");
        } %>
    </head>
    <body onload="onSubmit('<%=message%>')">

        <jsp:include page="header.jsp" >
            <jsp:param name="activated" value="Home" /> 
        </jsp:include>
    <center>
        <form action="LoginCheckProcessServlet" method="post">
            <table>
                <thead>
                <span style="color: #d6de5C;padding-bottom:40px;font-size: 50px;padding-left:30px ;font-weight: bold;">Login</span>
                </thead>
                <tbody>
                    <tr>
                        <td >
                            <input id="username"
                                   type="text" 
                                   name="username" 
                                   style="padding-right:32px;border-color:#587a7b ;height:34px;border-radius: 7px; display: inline-block; margin-left: 2px;font-weight: normal; width: 190px; "
                                   placeholder="Username">
                        </td>
                    </tr>
                    <tr>
                        <td >
                            <input id="password" 
                                   type="password" 
                                   name="password"
                                   style="padding-right:32px;border-color:#587a7b ;height:34px;border-radius: 7px; display: inline-block; margin-left: 2px;font-weight: normal; width: 190px; " 
                                   placeholder="Password">
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input id="login"
                                   type="submit"
                                   name="ResetPasswordForm:login"
                                   value="Log in"
                                   style="font-size: 18px !important; "
                                   >
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </center>
</body>
</html>
