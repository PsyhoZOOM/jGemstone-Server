package net.yuvideo.jgemstone.server.classes.RACUNI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

public class Zaduzenja {

  database db;

  private boolean error;
  private String errorMSG;


  public Zaduzenja(database db) {
    this.db = db;
  }

  public JSONObject getZaduzenjaOfUser(int userID, boolean sveUplate) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query;

    if (sveUplate) {
      query = "SELECT * FROM userDebts where userID=? AND paketType != 'FIX_SAOBRACAJ' ORDER BY zaMesec ASC";
    } else {
      query = "SELECT * FROM userDebts WHERE userID=? AND dug > uplaceno AND paketType != 'FIX_SAOBRACAJ' ORDER BY zaMesec ASC";
    }

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        JSONObject userDebt;
        int i = 0;
        while (rs.next()) {
          double cena = rs.getDouble("cena");
          double popust = rs.getDouble("popust");
          double pdv = rs.getDouble("pdv");
          int kolicina = rs.getInt("kolicina");
          double osnovica = cena * kolicina;
          double zaUplatu = osnovica - valueToPercent.getPDVOfValue(osnovica, popust);
          double uplaceno = rs.getDouble("uplaceno");

          zaUplatu = zaUplatu + valueToPercent.getPDVOfValue(zaUplatu, pdv);

          userDebt = new JSONObject();
          userDebt.put("id", rs.getInt("id"));
          userDebt.put("id_ServiceUser", rs.getInt("id_ServiceUser"));
          userDebt.put("nazivPaketa", rs.getString("nazivPaketa"));
          userDebt.put("datumZaduzenja", rs.getDate("datumZaduzenja"));
          userDebt.put("userID", rs.getInt("userID"));
          userDebt.put("popust", popust);
          userDebt.put("paketType", rs.getString("paketType"));
          userDebt.put("cena", cena);
          userDebt.put("kolicina", kolicina);
          userDebt.put("dug", zaUplatu);
          userDebt.put("zaUplatu", zaUplatu);
          userDebt.put("paketType", rs.getString("paketType"));
          userDebt.put("pdv", pdv);
          userDebt.put("popust", popust);
          userDebt.put("osnovica", osnovica);
          userDebt.put("uplaceno", uplaceno);
          userDebt.put("datumUplate", rs.getString("datumUplate"));
          userDebt.put("operater", rs.getString("operater"));
          userDebt.put("zaduzenOd", rs.getString("zaduzenOd"));
          userDebt.put("zaMesec", rs.getString("zaMesec"));
          userDebt.put("skipProduzenje", rs.getBoolean("skipProduzenje"));
          object.put(String.valueOf(i), userDebt);
          i++;
        }
      }
      rs.close();
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return object;
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
