package net.yuvideo.jgemstone.server.classes.INTERNET;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;

public class InternetPaket {

  database db;
  public String error;

  public InternetPaket(database db) {
    this.db = db;
  }

  public boolean deleteInternetPaket(int id) {
    PreparedStatement ps;
    boolean deleted = false;
    String query = "DELETE FROM internetPaketi WHERE id=?";
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
