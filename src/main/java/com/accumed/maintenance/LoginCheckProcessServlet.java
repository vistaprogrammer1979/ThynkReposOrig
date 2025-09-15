/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance;

import com.accumed.maintenance.passway.SCryptUtil;
import com.accumed.re.agents.CachedRepositoryService;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Anas
 */
@WebServlet("/maintenance/LoginCheckProcessServlet")
public class LoginCheckProcessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    private Connection getAccumedDB() {
        DataSource ds = null;
        Connection con = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/rulesLoggingDS");
            //ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/accumedDS");
            con = ds.getConnection();
            if (con.getTransactionIsolation() != Connection.TRANSACTION_READ_UNCOMMITTED) {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");
            } else {
                Logger.getLogger(CachedRepositoryService.class
                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");
            }
        } catch (NamingException ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CachedRepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            Connection con = getAccumedDB();
        try {
            String user_name = request.getParameter("username");
            String password = request.getParameter("password");
            String query = "SELECT top 10 [USER_ID],[USER_NAME],[ENCRYPTED_PASSWORD] from [NEO_USER] where [USER_NAME] =";
            int USER_ID = 0;
            String USER_NAME = "";
            String PASSWORD = "";
            String ENCRYPTED_PASSWORD = "";
            
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query + "'" + user_name + "'");
            
            while (rs.next()) {
                USER_ID = rs.getInt("USER_ID");
                USER_NAME = rs.getString("USER_NAME");
                
                ENCRYPTED_PASSWORD = rs.getString("ENCRYPTED_PASSWORD");
                break;
            }
            if ((user_name.equals(USER_NAME) && checkPassword(password,ENCRYPTED_PASSWORD))) {
                request.getSession().setAttribute("username", user_name);
                request.getSession().setAttribute("USER_ID", USER_ID);
                request.getSession().setAttribute("password", password);
                response.sendRedirect("mainPage.jsp");
            } else {
                request.getSession().setAttribute("ErrorMessage", "user name or password is not correct.");
                response.sendRedirect("index.jsp");
            }
            st.close();
            
        } catch (SQLException ex) {
            Logger.getLogger("LoginCheck").log(Level.SEVERE, null, ex);
            response.sendRedirect("index.jsp");
        }finally{
                try {
                    con.close();
                } catch (SQLException ex) {
                    Logger.getLogger(LoginCheckProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public boolean checkPassword(String password, String hashedPassword) {

        boolean matched = false;
        try {
            matched = SCryptUtil.check(password, hashedPassword);
        } catch (Exception ex) {
             Logger.getLogger(LoginCheckProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matched;
    }
}
