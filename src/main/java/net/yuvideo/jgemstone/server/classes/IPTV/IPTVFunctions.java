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
  private String operName;


  public IPTVFunctions(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }


  public JSONObject add_account(JSONObject rLine) {
    StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db, getOperName());
    JSONObject jsonObject = restAPI2.saveUSER(rLine);
    return jsonObject;
  }

  public boolean checkUserBussy(String STB_MAC) {
    StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db, getOperName());
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

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }
}
