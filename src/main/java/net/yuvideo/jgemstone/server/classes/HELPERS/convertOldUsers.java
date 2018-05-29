package net.yuvideo.jgemstone.server.classes.HELPERS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.USERS.UserFunc;
import net.yuvideo.jgemstone.server.classes.Users;
import net.yuvideo.jgemstone.server.classes.database;

public class convertOldUsers {

  Users users;
  Users newUsers;
  database db;


  public convertOldUsers(database db) {
    this.db = db;
    goGo();
  }

  private void goGo() {
    String query = "SELECT * FROM users";
    PreparedStatement ps;
    ResultSet rs;
    try {
      ps = db.connRad2.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          users = new Users();
          users.setIme(rs.getString("ime"));
          users.setDatum_rodjenja(rs.getString("datumrodjenja"));
          users.setPostanski_broj(rs.getString("postbr"));
          users.setMesto(rs.getString("mesto"));
          users.setBr_lk(rs.getString("brlk"));
          users.setJMBG(rs.getString("mbr"));
          users.setAdresa(rs.getString("adresa"));
          users.setFiksni(rs.getString("brtel"));
          users.setMobilni(rs.getString("brtel"));
          UserFunc usersFunc = new UserFunc();
          usersFunc.createUser(users, db);
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
