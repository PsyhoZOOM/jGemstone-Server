package net.yuvideo.jgemstone.server.classes.WiFi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class WiFiData {

  private database db;
  private boolean error;
  private String errorMSG;

  public WiFiData(database db) {
    this.db = db;
  }

  public JSONObject getWifiData() {
    JSONObject wiData = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM wifiTracker";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;

        while (rs.next()) {
          JSONObject wifiData = new JSONObject();
          wifiData.put("id", rs.getInt("id"));
          wifiData.put("mac", rs.getString("mac"));
          wifiData.put("hsIP", rs.getString("hostIP"));
          wifiData.put("hsName", rs.getString("hostAP"));
          wifiData.put("signal", rs.getString("wifiSignal"));
          wifiData.put("lastTimeSeen", rs.getString("lastTimeSeen"));
          wifiData.put("lastUpdated", rs.getString("lastUpdated"));
          wiData.put(String.valueOf(i), wifiData);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return wiData;
  }


  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }
}
