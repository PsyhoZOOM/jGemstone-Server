package net.yuvideo.jgemstone.server.classes.INTERNET;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import me.legrange.mikrotik.ResultListener;
import net.yuvideo.jgemstone.server.classes.database;

public class InternetPaket {

  database db;
  public String error;

  public InternetPaket(database db) {
    this.db = db;
  }

  public boolean deleteInternetPaket(int id) {
    PreparedStatement ps;
    ResultSet rs;
    boolean deleted = false;
    String query;

    query = "SELECT naziv FROM internetPaketi WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        boolean check = forRadGroupUsers(rs.getString("naziv"));
        if (check) {
          this.error = "Neki od korisnika vec imaju Grupu. Prvo promenite korisnicima grupu!";
          return false;
        } else {
          deleteRadGroup(rs.getString("naziv"));
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      deleted = false;
      error = e.getMessage();
      e.printStackTrace();
    }

    query = "DELETE FROM internetPaketi WHERE id=?";
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

  private boolean forRadGroupUsers(String naziv) {
    boolean groupExist = false;
    String query = "SELECT groupname from radusergroup WHERE groupname=?";

    try {
      PreparedStatement ps = db.connRad.prepareStatement(query);
      ps.setString(1, naziv);
      ResultSet rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        groupExist = true;
      } else {
        groupExist = false;
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      this.error = e.getMessage();
      e.printStackTrace();
      return false;
    }
    return groupExist;
  }

  private boolean deleteRadGroup(String naziv) {
    boolean deleted = true;
    PreparedStatement ps;
    String query = "DELETE FROM radgroupreply WHERE groupname=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, naziv);
      ps.executeUpdate();
    } catch (SQLException e) {
      error = e.getMessage();
      deleted = false;
      e.printStackTrace();
    }
    return deleted;
  }


}
