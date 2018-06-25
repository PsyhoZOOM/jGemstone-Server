package net.yuvideo.jgemstone.server.classes.USERS;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import net.yuvideo.jgemstone.server.classes.Users;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class UserFunc {

  public static boolean deleteUser(int userId, database db) {
    String query = "DELETE FROM users WHERE id=?";
    PreparedStatement ps;
    boolean deleted = true;

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
    } catch (Exception e) {
      deleted = false;
      e.printStackTrace();
    }

    //delete from Services_user
    query = "DELETE FROM servicesUser  WHERE  userID=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {

      deleted = false;
      e.printStackTrace();
    }

    query = "DELETE FROM userDebts WHERE userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    query = "DELETE FROM ugovori_korisnik WHERE userID=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      deleted = false;
      e.printStackTrace();
    }

    query = "DELETE FROM uplate WHERE userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      deleted = false;
      e.printStackTrace();
    }

    return deleted;


  }


  public void createUser(Users user, database db) {
    PreparedStatement ps;
    String query;
    query = "INSERT INTO users (ime, datumRodjenja, operater, postBr, mesto, brLk, JMBG, "
        + "adresa,  komentar, telFiksni, telMobilni, datumKreiranja)"
        + "VALUES (?, ?, ?, ?, ?, ?, ? ,? ,? ,? ,?, ?)";

    try {
      ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

      ps.setString(1, user.getIme());
      ps.setString(2, user.getDatum_rodjenja());
      ps.setString(3, "SYSTEM-" + LocalDate.now().toString());
      ps.setString(4, user.getPostanski_broj());
      ps.setString(5, user.getMesto());
      ps.setString(6, user.getBr_lk());
      ps.setString(7, user.getJMBG());
      ps.setString(8, user.getAdresa());
      ps.setString(9, "AUTOMATSKI PREBACEN IZ STARE BAZE");
      ps.setString(10, user.getFiksni());
      ps.setString(11, user.getMobilni());
      ps.setString(12,
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));

      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
