package net.yuvideo.jgemstone.server.classes.RADIUS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class TrafficReports {

  private database db;
  private boolean error;
  private String errorMSG;

  public TrafficReports(database db) {
    this.db = db;
  }

  public JSONObject getTrafficeReports(int count, String startTime, String stopTime) {
    JSONObject reports = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radacct  WHERE acctstarttime  >= ? AND acctstoptime <= ? LIMIT ?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, startTime);
      ps.setString(2, stopTime);
      ps.setInt(3, count);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject result = new JSONObject();
          result.put("id", rs.getInt("radacctid"));
          result.put("username", rs.getString("username"));
          result.put("nasIP", rs.getString("nasipaddress"));
          result.put("startTime", rs.getString("acctstarttime"));
          result.put("stopTime", rs.getString("acctstoptime"));
          result.put("onlineTime", rs.getString("acctsessiontime"));
          result.put("inputOctets", rs.getLong("acctinputoctets"));
          result.put("outputOctets", rs.getLong("acctoutputoctets"));
          result.put("service", rs.getString("calledstationid"));
          result.put("callingStationID", rs.getString("callingstationid"));
          result.put("ipAddress", rs.getString("framedipaddress"));
          result.put("terminateCause", rs.getString("acctterminatecause"));
          reports.put(String.valueOf(i), result);
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

    return reports;
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
