package classes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;


/**
 * Created by zoom on 8/11/16.
 */
public class database {
    private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String DB_URL = "jdbc:mysql://127.0.0.1/radius?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";

    public boolean DEBUG = false;

    private String UserName = "root";
    private String Password = "";

    public String query;

    public Connection conn;
    private Statement stmt;
    private ResultSet rs;
    public PreparedStatement ps;
    private Logger LOGGER = LogManager.getLogger("DATABASE");

    public database() {
        //this.query = query;
        initDB();
    }


    public void initDB() {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, UserName, Password);
            stmt = conn.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public ResultSet query_data(String query) {
        if (DEBUG) System.out.println(String.format("query_data: %s", query));
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();

        }


        return rs;
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
