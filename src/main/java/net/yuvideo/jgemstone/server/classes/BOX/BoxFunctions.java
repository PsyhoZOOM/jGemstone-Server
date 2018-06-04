package net.yuvideo.jgemstone.server.classes.BOX;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;

public class BoxFunctions {

  database db;
  public String error;

  public BoxFunctions(database db) {
    this.db = db;
  }


  public boolean deleteBoxPaket(int id) {
    boolean deleted = false;
    String query;
    PreparedStatement ps;
    query = "DELETE FROM paketBox WHERE id=?";
    try {
      ps = this.db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
      deleted = true;
    } catch (SQLException e) {
      error = e.getMessage();
      e.printStackTrace();
    }

    return deleted;
  }
}
