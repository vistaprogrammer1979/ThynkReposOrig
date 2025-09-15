<%-- 
    Document   : Logout
    Created on : Nov 3, 2020, 9:46:49 AM
    Author     : Anas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  
    </head>
    <body>
        <%
            session.removeAttribute("username");
            session.removeAttribute("password");
            session.removeAttribute("message");
            session.invalidate();

        %> 
        
         <%
//             getServletContext().getRequestDispatcher("/index.jsp").forward(
//                    request, response);
                response.sendRedirect("index.jsp");
        %>
    </body>


</html>
