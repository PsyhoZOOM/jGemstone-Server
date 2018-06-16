package net.yuvideo.jgemstone.server.classes.RADIUS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class RadReplyUsers {

  public JSONObject getUsers(String userName, database db) {
    JSONObject users = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radreply";
    try {
      ps = db.connRad.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject user = new JSONObject();
          user.put("id", rs.getString("id"));
          user.put("username", rs.getString("username"));
          user.put("attribute", rs.getString("attribute"));
          user.put("value", rs.getString("value"));
          users.put(String.valueOf(i), user);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      users.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    return users;
  }

}
