package net.yuvideo.jgemstone.server.classes.DTV;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;

public class DTVPaketFunctions {

  database db;
  public String error;

  public DTVPaketFunctions(database db) {
    this.db = db;
  }

  public boolean deleteDTVPaket(int id) {
    boolean deleted = false;
    PreparedStatement ps;
    String query = "DELETE FROM digitalniTVPaketi WHERE id=?";
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
