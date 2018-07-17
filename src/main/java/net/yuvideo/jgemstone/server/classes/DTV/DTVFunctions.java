package net.yuvideo.jgemstone.server.classes.DTV;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by zoom on 2/27/17.
 */
public class DTVFunctions {
  database db;

  private static SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
  private boolean error;
  private String errorMSG;


  public DTVFunctions(database db) {
    this.db = db;
  }

  public DTVFunctions() {
  }

  public static Boolean check_card_busy(int cardID, database db) {
    if (cardID == 0) {
      return false;
    }
    PreparedStatement ps;
    ResultSet rs;
    Boolean cardExist = false;

    String query = "SELECT idKartica from DTVKartice where idKartica = ?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, cardID);
      rs = ps.executeQuery();
      cardExist = rs.isBeforeFirst();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return cardExist;

  }

  public static void addCard(JSONObject rLine, String opername, database db) {
    if (rLine.getInt("DTVKartica") == 0) {
      return;
    }

    PreparedStatement ps;

    String query =
        "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate, createDate ) VALUES " +
            "(?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("DTVKartica"));
      ps.setInt(2, rLine.getInt("userID"));
      ps.setInt(3, rLine.getInt("DTVPaket"));
      ps.setString(4, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.setString(5, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static int getPacketCriteriaGroup(int id_packet, database db) {
    PreparedStatement ps;
    ResultSet rs;
    int paketID = 0;

    String query = "SELECT idPaket FROM digitalniTVPaketi WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_packet);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        paketID = rs.getInt("idPaket");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return paketID;
  }

  public static void activateNewService(JSONObject rLine, database db) {
    Calendar calendar = Calendar.getInstance();
    try {
      calendar.setTime(normalDate.parse(rLine.getString("endDate")));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    PreparedStatement ps;
    int produzenje = rLine.getInt("produzenje");

    if (rLine.getString("paketType").equals("DTV")) {
      String query = "UPDATE DTVKartice SET endDate=? where idKartica=?";
      calendar.add(Calendar.MONTH, produzenje);

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, normalDate.format(calendar.getTime()));
        ps.setString(2, rLine.getString("idUniqueName"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      //add_user_debt_first_time(rLine);
    }
  }

  public String getEndDate(int DTVCardID){
    PreparedStatement ps;
    ResultSet rs;
    String endDate = null;
    String query = "SELECT endDate FROM DTVKartice WHERE idKartica = ?";
    try {
      ps =  db.conn.prepareStatement(query);
      ps.setInt(1, DTVCardID);
      rs = ps.executeQuery();
      if(rs.isBeforeFirst()) {
        rs.next();
        endDate = rs.getString("endDate");
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      this.error = true;
      this.errorMSG = e.getMessage();
      e.printStackTrace();
    }
    return  endDate;
  }
}
