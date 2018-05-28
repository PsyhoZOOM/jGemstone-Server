package net.yuvideo.jgemstone.server.classes.LOCATION;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class LocationsClients {

  private Object all;

  public static void updateClient(JSONObject object, database db) {
    PreparedStatement ps;
    String query;
    boolean exist = checkIfClientExist(object.getString("identification"), db);
    if (exist) {
      query = "UPDATE gpsTracker set identification= ?, latitude =?, longitude=?, lastUpdateTime = ? WHERE identification =?";
    } else {

      query = "INSERT INTO gpsTracker (identification, latitude, longitude, lastUpdateTime) VALUES (?,?,?,?)";
    }

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, object.getString("identification"));
      ps.setDouble(2, object.getDouble("lat"));
      ps.setDouble(3, object.getDouble("long"));
      ps.setString(4, LocalDate.now().toString());
      if (exist) {
        ps.setString(5, object.getString("identification"));
      }
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
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

    String query = "SELECT * FROM gpsTracker";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject object = new JSONObject();
          object.put("id", rs.getInt("id"));
          object.put("identification", rs.getString("identification"));
          object.put("latitude", rs.getDouble("latitude"));
          object.put("longitude", rs.getDouble("longitude"));
          object.put("lastUpdateTime", rs.getString("lastUpdateTime"));
          jsonObject.put(String.valueOf(i), object);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return jsonObject;
  }
}
