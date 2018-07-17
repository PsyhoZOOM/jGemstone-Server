package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.JSONObject;

public class NetworkDevices {

  private database db;
  private String errorMsgp;
  private boolean error;

  private int id;
  private String name;
  private String ip;
  private String hostName;
  private String type;
  private String userName;
  private String pass;
  private String url;
  private String opis;
  private String nas;
  private String accessType;


  private ArrayList<NetworkDevices> networkDevicesArrayList;



  public NetworkDevices(database db) {
    this.db = db;
  }

  public NetworkDevices() {

  }

  public ArrayList<NetworkDevices> getAllDevices() {
    networkDevicesArrayList = new ArrayList<>();
    JSONObject allDevicesJSON = getAllDevicesJSON();
    for (int i = 0; i < allDevicesJSON.length(); i++) {
      NetworkDevices device = new NetworkDevices();
      JSONObject devObj = allDevicesJSON.getJSONObject(String.valueOf(i));
      device.setId(devObj.getInt("id"));
      device.setName(devObj.getString("name"));
      device.setIp(devObj.getString("ip"));
      device.setHostName(devObj.getString("hostName"));
      device.setType(devObj.getString("type"));
      device.setUserName(devObj.getString("userName"));
      device.setPass(devObj.getString("pass"));
      device.setUrl(devObj.getString("url"));
      device.setOpis(devObj.getString("opis"));
      device.setNas(devObj.getString("nas"));
      device.setAccessType(devObj.getString("accessType"));
      networkDevicesArrayList.add(device);
    }

    return getNetworkDevicesArrayList();

  }

  public JSONObject getAllDevicesJSON() {
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
              + "(?,?,?,?,?,?,?,?,?,?)";
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

  public JSONObject editDevice(JSONObject rLine) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    String query = "UPDATE networkDevices set name=?, ip=?, hostName=?, type=?, userName=?, pass=?,"
        + "url=?, opis=?, nas=?, accessType=? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("name"));
      ps.setString(2, rLine.getString("ip"));
      ps.setString(3, rLine.getString("hostName"));
      ps.setString(4, rLine.getString("type"));
      ps.setString(5, rLine.getString("userName"));
      ps.setString(6, rLine.getString("pass"));
      ps.setString(7, rLine.getString("url"));
      ps.setString(8, rLine.getString("opis"));
      ps.setBoolean(9, rLine.getBoolean("nas"));
      ps.setString(10, rLine.getString("accessType"));
      ps.setInt(11, rLine.getInt("id"));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      object.put("ERROR", e.getMessage());
      setError(true);
      setErrorMsgp(e.getMessage());
      e.printStackTrace();
    }
    return object;
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


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPass() {
    return pass;
  }

  public void setPass(String pass) {
    this.pass = pass;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getOpis() {
    return opis;
  }

  public void setOpis(String opis) {
    this.opis = opis;
  }

  public String getNas() {
    return nas;
  }

  public void setNas(String nas) {
    this.nas = nas;
  }

  public String getAccessType() {
    return accessType;
  }

  public void setAccessType(String accessType) {
    this.accessType = accessType;
  }

  public ArrayList<NetworkDevices> getNetworkDevicesArrayList() {
    return networkDevicesArrayList;
  }

  public void setNetworkDevicesArrayList(
      ArrayList<NetworkDevices> networkDevicesArrayList) {
    this.networkDevicesArrayList = networkDevicesArrayList;
  }
}
