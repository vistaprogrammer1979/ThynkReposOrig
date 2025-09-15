/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance;

import com.accumed.re.agents.CachedRepositoryService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Anas
 */
public class PackagesAuditService {

    public List<PackageAudit> getPackagesAudit() {
        Connection con = getAccumedDB();
        List<PackageAudit> auditList = new ArrayList<PackageAudit>();
        try {
            //Connection con = DriverManager.getConnection(url, usr, pass);
            Statement st = con.createStatement();

            //String query = "insert into RE_UPDATE_OBJECTS_AUDIT(OBJ_TYPE,OBJ_NAME,UPDATE_DATE,UPDATE_USER_ID,UPDATE_USER_NAME)\n" + "VALUES (1,?,getDate(),?,?)";
            String query = "SELECT  [ID]\n"
                    + "      ,[OBJ_TYPE]\n"
                    + "      ,[OBJ_NAME]\n"
                    + "      ,Convert(varchar, [UPDATE_DATE], 100) AS [UPDATE_DATE]\n"
                    + "      ,[UPDATE_USER_ID]\n"
                    + "      ,[UPDATE_USER_NAME]\n"
                    + "  FROM [RE_UPDATE_OBJECTS_AUDIT] ORDER BY [UPDATE_DATE] DESC";

            ResultSet rs = st.executeQuery(query);
            
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            while (rs.next()) {
              //  System.out.println(rs.getString("UPDATE_DATE"));
                auditList.add(new PackageAudit(
                        rs.getInt("ID"),
                        rs.getInt("OBJ_TYPE"),
                        rs.getString("OBJ_NAME"),
                        rs.getString("UPDATE_DATE"),
                        rs.getInt("UPDATE_USER_ID"),
                        rs.getString("UPDATE_USER_NAME")
                ));
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
        return auditList;
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

}
