package net.yuvideo.jgemstone.server.classes.OBRACUNI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

public class MesecniObracun {

  public boolean hasError = false;
  public String errorMessage = "";
  public JSONObject mesecniObracunObject;
  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
  DateTimeFormatter dtfNormal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private String operName;


  public JSONObject getMesecniObracun(int userOd, int userDo, String datumOd, String datumDo,
      String operName, database db) {
    this.operName = operName;
    mesecniObracunObject = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM userDebts WHERE zaMesec >= ? AND zaMesec <= ? AND userID >= ? AND userID <= ?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDate.parse(datumOd, dtfNormal).format(dtf));
      ps.setString(2, LocalDate.parse(datumDo, dtfNormal).format(dtf));
      ps.setInt(3, userOd);
      ps.setInt(4, userDo);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject mesec = new JSONObject();
          Double cena = rs.getDouble("cena");
          Double popust = rs.getDouble("popust");
          Double pdv = rs.getDouble("pdv");
          Double cenaSaPopustom = cena - valueToPercent.getPDVOfSum(cena, popust);
          Double pdvCena = valueToPercent.getPDVOfValue(cenaSaPopustom, pdv);
          UsersData user = new UsersData(db, operName);
          JSONObject userObj = user.getUserData(rs.getInt("userID"));
          String imePrezime = userObj.getString("ime");
          String jBroj = userObj.getString("jBroj");

          mesec.put("id", rs.getInt("id"));
          mesec.put("imePrezime", imePrezime);
          mesec.put("jBroj", jBroj);
          mesec.put("cena", (cenaSaPopustom));
          mesec.put("popust", popust);
          mesec.put("pdv",pdv);
          mesec.put("pdvCena", pdvCena);
          mesec.put("ukupno", cenaSaPopustom + pdvCena);
          mesecniObracunObject.put(String.valueOf(i), mesec);
          i++;
        }
      }

    } catch (SQLException e) {
      hasError = true;
      errorMessage = e.getMessage();
      e.printStackTrace();
    }

    return mesecniObracunObject;
  }
}
