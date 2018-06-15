package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class NetworkDevices {

  private final database db;
  private String errorMsgp;
  private boolean error;

  public NetworkDevices(database db) {
    this.db = db;
  }

  public JSONObject getAllDevices() {
    JSONObject obj = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM networkDevices";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject device = new JSONObject();
          device.put("id", rs.getInt("id"));
          device.put("name", rs.getString("name"));
          device.put("ip", rs.getString("ip"));
          device.put("hostName", rs.getString("hostName"));
          device.put("type", rs.getString("type"));
          device.put("userName", rs.getString("userName"));
          device.put("pass", rs.getString("pass"));
          device.put("url", rs.getString("url"));
          device.put("opis", rs.getString("opis"));
          device.put("nas", rs.getBoolean("nas"));
          device.put("accessType", rs.getString("accessType"));
          obj.put(String.valueOf(i), device);
          i++;
        }
      }
      setError(false);
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMsgp(e.getMessage());
      e.printStackTrace();
    }
    return obj;
  }


  public String getErrorMsgp() {
    return errorMsgp;
  }

  public void setErrorMsgp(String errorMsgp) {
    this.errorMsgp = errorMsgp;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public void addDevice(JSONObject rLine) {
    PreparedStatement ps;
    String query;
    if (rLine.has("pass")) {
      query =
          "INSERT into networkDevices (name, ip, hostName, type, url, opis, nas, accessType, userName, pass) VALUES "
              + "(?,?,?,?,?,?,?,?,?)";
    } else {
      query =
          "INSERT INTO networkDevices(name, ip, hostName, type, url, opis, nas, accessType, userName) VALUES "
              + "(?,?,?,?,?,?,?,?,?)";
    }
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("name"));
      ps.setString(2, rLine.getString("ip"));
      ps.setString(3, rLine.getString("hostName"));
      ps.setString(4, rLine.getString("type"));
      ps.setString(5, rLine.getString("url"));
      ps.setString(6, rLine.getString("opis"));
      ps.setBoolean(7, rLine.getBoolean("nas"));
      ps.setString(8, rLine.getString("accessType"));
      ps.setString(9, rLine.getString("userName"));
      if (rLine.has("pass")) {
        ps.setString(10, rLine.getString("pass"));
      }

      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMsgp(e.getMessage());
      e.printStackTrace();
    }
  }

  public JSONObject getNASDevices() {
    JSONObject NASDevices = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT* FROM networkDevices WHERE nas != FALSE";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject device = new JSONObject();
          device.put("id", rs.getInt("id"));
          device.put("name", rs.getString("name"));
          device.put("ip", rs.getString("ip"));
          device.put("hostName", rs.getString("hostName"));
          device.put("type", rs.getString("type"));
          device.put("userName", rs.getString("userName"));
          device.put("pass", rs.getString("pass"));
          device.put("url", rs.getString("url"));
          device.put("opis", rs.getString("opis"));
          device.put("nas", rs.getBoolean("nas"));
          device.put("accessType", rs.getString("accessType"));
          NASDevices.put(String.valueOf(i), device);
          i++;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return NASDevices;
  }

  public String getName(String ipAddress) {
    PreparedStatement ps;
    ResultSet rs;
    String nasName = ipAddress;
    String query = "SELECT name FROM networkDevices WHERE ip=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, ipAddress);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        nasName = rs.getString("name");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return nasName;
  }
}
