package net.yuvideo.jgemstone.server.classes.FIX;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 8/29/17.
 */
public class CSVData implements Serializable {

  private int id;
  private String account;
  private String from;
  private String to;
  private String country;
  private String description;
  private String connectTime;
  private String chargedTimeMinSec;
  private int chargedTimeSec;
  private double chargedAmountRSD;
  private String serviceName;
  private int chargedQuantity;
  private String serviceUnit;
  private String customerID;
  private String fileName;

  private boolean error = false;
  private String errorMSG;


  private JSONObject csvData;
  private database db;

  public CSVData() {
  }

  public CSVData(database db) {
    this.db = db;
  }


  public JSONObject getZaMesecByCountry(String zaMesec, String account) {
    System.out.println(zaMesec);
    PreparedStatement ps;
    ResultSet rs;
    csvData = new JSONObject();
    String query = "SELECT SUM(chargedTimeS) AS chargedTimeS, country FROM csv WHERE connectTime LIKE ? AND account LIKE ? GROUP BY country";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, zaMesec + "%");
      ps.setString(2, account + "%");
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject data = new JSONObject();
          //data.put("id", rs.getInt("id"));
          //data.put("account", rs.getString("account"));
          //data.put("from", rs.getString("from"));
          //data.put("to", rs.getString("to"));
          data.put("country", rs.getString("country"));
          //data.put("description", rs.getString("description"));
          //data.put("connectTime", rs.getString("connectTime"));
          //data.put("chargedTimeMS", rs.getString("chargedTimeMS"));
          data.put("chargedTimeS", rs.getInt("chargedTimeS"));
          //data.put("chargedAmountRSD", rs.getDouble("chargedAmountRSD"));
          //data.put("serviceName", rs.getString("serviceName"));
          //data.put("chargedQuantity", rs.getInt("chargedQuantity"));
          //data.put("serviceUnit", rs.getString("serviceUnit"));
          //data.put("customerID", rs.getString("customerID"));
          //data.put("fileName", rs.getString("fileName"));
          csvData.put(String.valueOf(i), data);
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
    return getCsvData();


  }


  public JSONObject getCsvData() {
    return csvData;
  }

  public void setCsvData(JSONObject csvData) {
    this.csvData = csvData;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getCustomerID() {
    return customerID;
  }

  public void setCustomerID(String customerID) {
    this.customerID = customerID;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getConnectTime() {
    return connectTime;
  }

  public void setConnectTime(String connectTime) {
    this.connectTime = connectTime;
  }

  public String getChargedTimeMinSec() {
    return chargedTimeMinSec;
  }

  public void setChargedTimeMinSec(String chargedTimeMinSec) {
    this.chargedTimeMinSec = chargedTimeMinSec;
  }

  public int getChargedTimeSec() {
    return chargedTimeSec;
  }

  public void setChargedTimeSec(int chargedTimeSec) {
    this.chargedTimeSec = chargedTimeSec;
  }

  public double getChargedAmountRSD() {
    return chargedAmountRSD;
  }

  public void setChargedAmountRSD(double chargedAmountRSD) {
    this.chargedAmountRSD = chargedAmountRSD;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public int getChargedQuantity() {
    return chargedQuantity;
  }

  public void setChargedQuantity(int chargedQuantity) {
    this.chargedQuantity = chargedQuantity;
  }

  public String getServiceUnit() {
    return serviceUnit;
  }

  public void setServiceUnit(String serviceUnit) {
    this.serviceUnit = serviceUnit;
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
