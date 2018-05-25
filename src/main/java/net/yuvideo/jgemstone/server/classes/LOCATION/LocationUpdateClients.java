package net.yuvideo.jgemstone.server.classes.LOCATION;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class LocationUpdateClients {

  public static void updateClient(JSONObject object, database db) {
    PreparedStatement ps;
    String query;
    boolean exist = checkIfClientExist(object.getString("identification"), db);
    if (exist) {
      query = "UPDATE gpsTracking set identification= ?, latitude =?, longitude=? lastUpdateTime = ? WHERE identification =?";
    } else {

      query = "INSERT INTO gpsTracking (identification, latitude, longitude, lastUpdateTime) VALUES (?,?,?,?)";
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
    String query = "SELECT * FROM gpsTracking WHERE username=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, name);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }
}
