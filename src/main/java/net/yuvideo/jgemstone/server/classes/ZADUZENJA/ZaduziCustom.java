package net.yuvideo.jgemstone.server.classes.ZADUZENJA;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

public class ZaduziCustom {
  private DateTimeFormatter dtfNormal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("yyyy-MM");
  private String oper;
  private  database db;

  private boolean error;
  private String errorMSG;


  public ZaduziCustom(String oper, database db) {
    this.oper = oper;
    this.db = db;
  }


  public void zaduziKorisnika(JSONObject object){
    double pdv = object.getDouble("pdv");
    double cena= object.getDouble("cena");
    int kolicina = object.getInt("kolicina");
    int rate =object.getInt("brojRata");
    int userID= object.getInt("userID");
    String naziv= object.getString("naziv");
    String zaMesec= object.getString("zaMesec");
    LocalDate zaMesecDatum = LocalDate.parse(zaMesec+"-01", dtfNormal);

    for (int i = 0; i<rate;i++) {
      String sqlNaziv=  naziv;
      if (rate > 1) {
        sqlNaziv = String.format("%s rata %d od %d", naziv, i + 1, rate);
      }
      Double cenaZaSQL = cena/rate;
      Double dugZaSQL =  cenaZaSQL+valueToPercent.getPDVOfValue(cenaZaSQL, pdv);
      dugZaSQL *=kolicina;

      String query =
          "INSERT INTO zaduzenja (datum, cena, kolicina, jMere, pdv, popust, naziv, zaduzenOd, userID, paketType, dug, zaMesec, opis) "
              + "VALUES "
              + "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps;
      try {

        ps = db.conn.prepareStatement(query);
        ps.setString(1, LocalDateTime.now().format(dtfNormal));
        ps.setDouble(2, cena/rate);
        ps.setInt(3, object.getInt("kolicina"));
        ps.setString(4, "kom.");
        ps.setDouble(5, pdv);
        ps.setDouble(6, 0);
        ps.setString(7, sqlNaziv.toUpperCase());
        ps.setString(8, oper);
        ps.setInt(9, userID);
        ps.setString(10, "CUSTOM");
        ps.setDouble(11, dugZaSQL);
        ps.setString(12, zaMesecDatum.plusMonths(i).format(dtfShort));
        ps.setString(13, "Rucno zaduzenje");
        ps.executeUpdate();
        ps.close();

      } catch (SQLException e) {
        setError(true);
        setErrorMSG(e.getMessage());
        e.printStackTrace();
      }
    }

  }

  public void izmeniZaduzenje(JSONObject rLine) {
    String query = "UPDATE zaduzenja set cena=?, pdv=?, dug=? WHERE id=?";
    PreparedStatement ps;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setDouble(1, rLine.getDouble("cena"));
      ps.setDouble(2, rLine.getDouble("pdv"));
      ps.setDouble(3, rLine.getDouble("dug"));
      ps.setInt(4, rLine.getInt("id"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }


}
