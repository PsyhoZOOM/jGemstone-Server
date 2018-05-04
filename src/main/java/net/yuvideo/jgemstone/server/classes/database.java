package net.yuvideo.jgemstone.server.classes;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;


/**
 * Created by zoom on 8/11/16.
 */
public class database {

  public int DEBUG = 1;
  public String query;
  public Connection conn;
  public Connection connRad;
  public Connection connIPTV;
  //public Connection connCSV;
  public PreparedStatement ps;
  private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  private String DB_URL = "jdbc:mysql://127.0.0.1/jgemstone?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
  private String DB_URL_RADIUS = "jdbc:mysql://127.0.0.1/radius?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
  private String DB_URL_IPTV = "jdbc:mysql://127.0.0.1/stalker_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
  private String DV_URL_CSV = "jdbc:mysql://127.0.0.1/CSV?useUnicode=true&characterEncoding=UTF-8&autoreconnect=true";
  private String UserName = "jgemstone";
  private String Password = "jgemstone";
  private Statement stmt;
  private Statement stmtRad;
  private Statement stmtIPTV;
  private Statement stmtCSV;
  private ResultSet rs;
  private Logger LOGGER = Logger.getLogger("DATABASE");

  public database() {
    //this.query = query;
    initDB();
  }


  public void initDB() {
    try {
      Class.forName(JDBC_DRIVER);
      System.out.println("Connecting to database...");
      conn = DriverManager.getConnection(DB_URL, UserName, Password);
      connRad = DriverManager.getConnection(DB_URL_RADIUS, UserName, Password);
      //connIPTV = DriverManager.getConnection(DB_URL_IPTV, UserName, Password);
      //connCSV = DriverManager.getConnection(DV_URL_CSV, UserName, Password);
      stmt = conn.createStatement();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("Database initialized.");


  }

  public void executeUpdate() {

    try {
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void closeDatabase() {
    try {
      if (rs != null) {

        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }


  }

}
