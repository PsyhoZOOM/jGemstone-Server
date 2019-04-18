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
  public PreparedStatement ps;
  private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  private String DB_URL = "jdbc:mysql://127.0.0.1/jgemstone?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&connectTimeout=0";
  private String DB_URL_RADIUS = "jdbc:mysql://127.0.0.1/radius?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&connectTimeout=0";
  private String UserName = "jgemstone";
  private String Password = "jg3mst0n3";
  private Logger LOGGER = Logger.getLogger("DATABASE");

  public database() {
    //this.query = query;
    initDB();
  }

  public void closeDB() {
    try {
      conn.close();
      connRad.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  public void initDB() {
    try {
      Class.forName(JDBC_DRIVER);
      System.out.println("Connecting to database...");
      conn = DriverManager.getConnection(DB_URL, UserName, Password);
      connRad = DriverManager.getConnection(DB_URL_RADIUS, UserName, Password);
      //     connRad2 = DriverManager.getConnection(DB_URL_RADIUS2, UserName, Password);
      //connIPTV = DriverManager.getConnection(DB_URL_IPTV, UserName, Password);
      //connCSV = DriverManager.getConnection(DV_URL_CSV, UserName, Password);
      //  stmt = conn.createStatement();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    LOGGER.info("Database initialized..");


  }



}
