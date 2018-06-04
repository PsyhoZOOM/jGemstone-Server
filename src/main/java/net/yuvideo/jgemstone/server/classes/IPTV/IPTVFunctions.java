package net.yuvideo.jgemstone.server.classes.IPTV;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 8/16/17.
 */
public class IPTVFunctions {

  database db;
  public String error;

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

  public static Boolean checkUserBussy(String STB_MAC, database db) {
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
      error = e.getMessage();
      e.printStackTrace();
    }
    return deleted;
  }
}
