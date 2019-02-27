package net.yuvideo.jgemstone.server.classes.IZVESTAJI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class Izvestaji {

  private final database db;
  private final String operName;
  String errorMSG;
  boolean error;

  public Izvestaji(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }

  public JSONObject getIzvestajPoDatumu(JSONObject object){
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM uplate WHERE datum >= ? and datum <= ? and obracunZaMesec IS NULL";
    JSONObject uplate = new JSONObject();
    try {
      ps =db.conn.prepareStatement(query);
      ps.setString(1, object.getString("od"));
      ps.setString(2, object.getString("do"));
      UsersData usersData = new UsersData(db, operName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        int i =0;
        while (rs.next()){
          JSONObject user= usersData.getUserData(rs.getInt("userID"));
          JSONObject uplata = new JSONObject();
          uplata.put("uplaceno", rs.getDouble("duguje"));
          uplata.put("jBroj", user.getString("jBroj"));
          uplata.put("ime", user.getString("ime"));
          uplata.put("datumUplate", rs.getString("datum"));
          uplata.put("opis", rs.getString("opis"));
          uplata.put("operater", rs.getString("operater"));
          uplata.put("userID", rs.getString("userID"));
          uplata.put("id", rs.getString("id"));
          uplate.put(String.valueOf(i), uplata);
          i++;

        }
      }
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return uplate;

  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }
}
