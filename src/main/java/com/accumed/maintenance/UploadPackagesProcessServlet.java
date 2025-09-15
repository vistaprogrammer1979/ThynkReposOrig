/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance;

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
import java.util.Iterator;
import java.util.List;
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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author Anas
 */
@WebServlet("/maintenance/UploadPackagesProcessServlet")
public class UploadPackagesProcessServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String User_NAME = (String) session.getAttribute("username");
        if (User_NAME != null) {
            try {

                String fileUploadPath = (String) (new InitialContext().lookup("java:comp/env/com.accumed.rules_packages.dir"));
                String fileOldPath = fileUploadPath + "_Backup" + File.separator;
                File dir = new File(fileOldPath);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                ServletFileUpload uploader = new ServletFileUpload(new DiskFileItemFactory());
                List<FileItem> fileItemsList = uploader.parseRequest(request);
                Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
                while (fileItemsIterator.hasNext()) {
                    FileItem fileItem = fileItemsIterator.next();
                    long sizeInBytes = fileItem.getSize();
                    String fileName = fileItem.getName();
                    File file;
                    File dest;
                    String TempFileName = "";
                    if (fileName.lastIndexOf("\\") >= 0) {
                        file = new File(fileUploadPath + File.separator
                                + fileName.substring(fileName.lastIndexOf("\\")));
                        dest = new File(fileOldPath + File.separator
                                + fileName.substring(fileName.lastIndexOf("\\")));
                        TempFileName = fileName.substring(fileName.lastIndexOf("\\"));
                    } else {
                        file = new File(fileUploadPath + File.separator
                                + fileName);
                        dest = new File(fileOldPath + File.separator
                                + fileName);
                        TempFileName = fileName;
                    }

                    if (file.exists()) {

                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            is = new FileInputStream(file);
                            os = new FileOutputStream(dest);
                            byte[] buffer = new byte[1024];
                            int length;
                            if (dest.exists()) {
                                dest.delete();
                            }
                            while ((length = is.read(buffer)) > 0) {
                                os.write(buffer, 0, length);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);

                        } finally {
                            is.close();
                            os.close();
                        }
                        dest.setLastModified(file.lastModified());

                        file.delete();

                    }
                    fileItem.write(file);
                    dbLog(request, fileName, response);
                    String message = TempFileName + "(" + sizeInBytes + " Bytes) has been uploaded at " + fileUploadPath.replace("\\", "\\\\") + "successfully !!";
                    request.getSession().setAttribute("message", message);
                }

            } catch (Exception ex) {
                Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                // getServletContext()
                //      .getRequestDispatcher("/maintenance/uploadPage.jsp").forward(
                //                        request, response);
                response.sendRedirect("uploadPage.jsp");
            }
        } else {
            response.sendRedirect("index.jsp");
        }
    }

    void dbLog(HttpServletRequest request, String filename, HttpServletResponse response
    ) {
        Connection con = getAccumedDB();
        try {

            Statement st = con.createStatement();
            HttpSession session = request.getSession();
            Integer User_ID = (Integer) (session.getAttribute("USER_ID"));
            String User_NAME = (String) session.getAttribute("username");

            String query = "insert into RE_UPDATE_OBJECTS_AUDIT(OBJ_TYPE,OBJ_NAME,UPDATE_DATE,UPDATE_USER_ID,UPDATE_USER_NAME)\n" + "VALUES (0,?,getDate(),?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, filename);
            preparedStatement.setInt(2, User_ID);
            preparedStatement.setString(3, User_NAME);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, new java.sql.SQLException("Creation failed."));
            }

        } catch (SQLException ex) {
            Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            con = ds.getConnection();

            if (con.getTransactionIsolation() != Connection.TRANSACTION_READ_UNCOMMITTED) {
                Logger.getLogger(UploadPackagesProcessServlet.class
                        .getName()).log(Level.SEVERE, "DB connection is NOT READ_UNCOMMITTED.");
            } else {
                Logger.getLogger(UploadPackagesProcessServlet.class
                        .getName()).log(Level.INFO, "DB connection is READ_UNCOMMITTED.");
            }
        } catch (NamingException ex) {
            Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UploadPackagesProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
