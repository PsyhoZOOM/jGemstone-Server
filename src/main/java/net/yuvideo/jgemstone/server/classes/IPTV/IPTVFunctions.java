package net.yuvideo.jgemstone.server.classes.IPTV;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 8/16/17.
 */
public class IPTVFunctions {

  database db;
  private String errorMSG;
  private boolean error;


  public IPTVFunctions(database db) {
    this.db = db;
  }

  public IPTVFunctions() {
  }

  public static JSONObject add_account(JSONObject rLine, database db) {
    StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
    JSONObject jsonObject = restAPI2.saveUSER(rLine);
    return jsonObject;
  }

  public static boolean checkUserBussy(String STB_MAC, database db) {
    StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
    return restAPI2.checkUser(STB_MAC);
  }

  public boolean deletePaket(int id) {
    PreparedStatement ps;
    boolean deleted = false;
    String query = "DELETE FROM IPTV_Paketi WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      deleted = true;
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return deleted;
  }

  public void changeMAC(String old_mac, String new_mac) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "UPDATE servicesUser SET IPTV_MAC=? WHERE IPTV_MAC=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, new_mac);
      ps.setString(2, old_mac);
      ps.executeUpdate();
    } catch (SQLException e) {
      this.setErrorMSG(e.getMessage());
      this.setError(true);
      e.printStackTrace();
    }
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
