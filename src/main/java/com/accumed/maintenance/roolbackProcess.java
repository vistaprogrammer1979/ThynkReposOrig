/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance;

import com.accumed.re.agents.CachedRepositoryService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author Anas
 */
@WebServlet("/maintenance/roolbackProcess")

public class roolbackProcess extends HttpServlet {

    private String fileUploadPath;
    private String fileOldPath;

    @Override
    public void init() throws ServletException {
        try {
            this.fileUploadPath = (String) (new InitialContext().lookup("java:comp/env/com.accumed.rules_packages.dir"));
            this.fileOldPath = fileUploadPath + "_Backup" + File.separator;
            //this.fileOldPath = (String) (new InitialContext().lookup("java:comp/env/com.accumed.rules_packages.rollback_dir"));
        } catch (NamingException ex) {
            Logger.getLogger(roolbackProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String User_NAME = (String) session.getAttribute("username");
        if (User_NAME != null) {
            try {

                String fileName = request.getParameter("fileName"); //processRequest(request, response);
                File temp = new File(this.fileUploadPath + File.separator + fileName);
                if (temp.exists()) {
                    temp.delete();
                }
                InputStream is = null;
                OutputStream os = null;
                File uploadFile = new File(this.fileUploadPath + File.separator + fileName);
                File oldFile = new File(this.fileOldPath + File.separator + fileName);

                os = new FileOutputStream(uploadFile);
                is = new FileInputStream(oldFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                //os.flush();

                is.close();
                os.close();
                uploadFile.setLastModified(oldFile.lastModified());

                dbLog(request, fileName, response);

                String message = "rollback for " + fileName + " has been done successfully !!";
                session.setAttribute("message", message);

            } catch (Exception ex) {
                Logger.getLogger(roolbackProcess.class.getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
//                getServletContext()
//                        .getRequestDispatcher("/maintenance/uploadPage.jsp").forward(
//                        request, response);
                response.sendRedirect("uploadPage.jsp");
            }
        } else {
            response.sendRedirect("index.jsp");
        }

    }

    void dbLog(HttpServletRequest request, String filename, HttpServletResponse response) {
        Connection con = getAccumedDB();
        try {
            //Connection con = DriverManager.getConnection(url, usr, pass);
            Statement st = con.createStatement();
            HttpSession session = request.getSession();
            Integer User_ID = (Integer) (session.getAttribute("USER_ID"));
            String User_NAME = (String) session.getAttribute("username");

            String query = "insert into RE_UPDATE_OBJECTS_AUDIT(OBJ_TYPE,OBJ_NAME,UPDATE_DATE,UPDATE_USER_ID,UPDATE_USER_NAME)\n" + "VALUES (1,?,getDate(),?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, filename);
            preparedStatement.setInt(2, User_ID);
            preparedStatement.setString(3, User_NAME);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                Logger.getLogger("LoginCheck").log(Level.SEVERE, null, new java.sql.SQLException("Creation failed."));
            }
        } catch (SQLException ex) {
            Logger.getLogger(roolbackProcess.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(roolbackProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
