package net.yuvideo.jgemstone.server.classes.LOCATION;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class LocationsClients {
  private static  DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd mm:hh:ss");


  public static void updateClient(JSONObject object, database db) {
    PreparedStatement ps;
    String query;

      query = "INSERT INTO gpsTrackerPath (identification, latitude, longitude, lastUpdateTime, name) VALUES (?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, object.getString("imei"));
      ps.setDouble(2, object.getDouble("lat"));
      ps.setDouble(3, object.getDouble("long"));
      ps.setString(4, LocalDateTime.now().toString());
      ps.setString(5, object.getString("name"));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    updateClientPosition(object, db);
  }

  private static void updateClientPosition(JSONObject object, database db) {
    PreparedStatement ps;
    String query;
    boolean exist = checkIfClientExist(object.getString("imei"), db);

    if (exist){
      query = "UPDATE gpsTracker SET latitude=?, longitude=?, lastUpdateTime=? WHERE identification=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setDouble(1, object.getDouble("lat"));
        ps.setDouble(2, object.getDouble("long"));
        ps.setString(3, LocalDateTime.now().format(dateTimeFormatter));
        ps.setString(4, object.getString("imei"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }else{
      query = "INSERT INTO gpsTracker (identification, latitude, longitude, lastUpdateTime, name) VALUES (?,?,?,?,?)";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, object.getString("imei"));
        ps.setDouble(2, object.getDouble("lat"));
        ps.setDouble(3, object.getDouble("long"));
        ps.setString(4, LocalDateTime.now().toString());
        ps.setString(5, object.getString("name"));
        ps.executeUpdate();
        ps.close();

      } catch (SQLException e) {
        e.printStackTrace();
      }

    }

  }


  private static boolean checkIfClientExist(String name, database db) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM gpsTracker WHERE identification=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, name);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        ps.close();
        rs.close();
        return true;
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public JSONObject getAll(database db) {
    JSONObject jsonObject = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;

    //String query = "SELECT DISTINCT (name) as name, id, identification, latitude, longitude, lastUpdateTime FROM gpsTracker order by id DESC";
    String query = "SELECT DISTINCT * FROM gpsTracker group by identification order by id DESC";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject object = new JSONObject();
          object.put("id", rs.getInt("id"));
          object.put("identification", rs.getString("identification"));
          object.put("name", rs.getString("name"));
          object.put("latitude", rs.getDouble("latitude"));
          object.put("longitude", rs.getDouble("longitude"));
          object.put("lastUpdateTime", rs.getString("lastUpdateTime"));
          jsonObject.put(String.valueOf(i), object);
          i++;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return jsonObject;
  }

  public JSONObject getAllDevices(database db){
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;

    String qeury = "SELECT * FROM gpsTracker GROUP BY identification";

    try {
      ps = db.conn.prepareStatement(qeury);
      rs= ps.executeQuery();
      if(rs.isBeforeFirst()){
        int i = 0;
        while (rs.next()){
          JSONObject device = new JSONObject();
          device.put("id", rs.getInt("id"));
          device.put("identification", rs.getString("identification"));
          device.put("name", rs.getString("name"));
          object.put(String.valueOf(i), device);
          i++;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return object;
  }

  public JSONObject getGPSPath(String identification, String startTime, String endTime, database db){
    PreparedStatement ps;
    ResultSet rs;
    JSONObject object = new JSONObject();
    String query = "SELECT * FROM gpsTrackerPath WHERE identification=? AND lastUpdateTime >= ? AND lastUpdateTime <= ?";
    if (identification.contains("3582400")){
      System.out.println("HEE");
    }

    try {
      ps= db.conn.prepareStatement(query);
      ps.setString(1, identification);
      ps.setString(2, startTime);
      ps.setString(3, endTime);
      rs= ps.executeQuery();
      if (rs.isBeforeFirst()){
        int i = 0;
        while (rs.next()) {
          JSONObject path = new JSONObject();
          path.put("longitude", rs.getDouble("longitude"));
          path.put("latitude", rs.getDouble("latitude"));
          object.put(String.valueOf(i), path);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return object;
  }





}
