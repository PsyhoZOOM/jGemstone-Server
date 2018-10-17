package net.yuvideo.jgemstone.server.classes.USERS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.MESTA.MestaFuncitons;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 2/15/18.
 */
public class UsersData {

  private database db;
  private String errorMSG;
  private boolean error;


  public UsersData(database db, String operName) {
    this.db = db;
  }

  public int getUserIDOfRadiusUserName(String userName) {
    int userid = 0;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT userID FROM servicesUser WHERE UserName=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, userName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        userid = rs.getInt("userID");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userid;
  }


  public JSONObject getUserData(int userID) {
    JSONObject user = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query;
    query = "SELECT * FROM users WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        user.put("id", rs.getInt("id"));
        user.put("ime", rs.getString("ime"));
        user.put("datumRodjenja", rs.getString("datumRodjenja"));
        user.put("postBr", rs.getString("postBr"));
        user.put("adresa", rs.getString("adresa"));
        user.put("mesto", rs.getString("mesto"));
        user.put("brLk", rs.getString("brLk"));
        user.put("JMBG", rs.getString("JMBG"));
        user.put("adresaRacuna", rs.getString("adresaRacuna"));
        user.put("mestoRacuna", rs.getString("mestoRacuna"));
        user.put("komentar", rs.getString("komentar"));
        user.put("telFiksni", rs.getString("telFiksni"));
        user.put("telMobilni", rs.getString("telMobilni"));
        user.put("datumKreiranja", rs.getString("datumKreiranja"));
        user.put("operater", rs.getString("operater"));
        user.put("jMesto", rs.getString("jMesto"));
        MestaFuncitons mestaFuncitons = new MestaFuncitons(db);
        user.put("jMestoNaziv", mestaFuncitons.getNazivMesta(rs.getString("jMesto")));
        user.put("jAdresa", rs.getString("jAdresa"));
        user.put("jAdresaNaziv",
            mestaFuncitons.getNazivAdrese(rs.getString("jMesto"), rs.getString("jAdresa")));
        user.put("jAdresaBroj", rs.getString("jAdresaBroj"));
        user.put("jBroj", rs.getString("jBroj"));
        user.put("firma", rs.getBoolean("firma"));
        user.put("nazivFirme", rs.getString("nazivFirme"));
        user.put("kontaktOsoba", rs.getString("kontaktOsoba"));
        user.put("kontaktOsobaTel", rs.getString("kontaktOsobaTel"));
        user.put("kodBanke", rs.getString("kodBanke"));
        user.put("PIB", rs.getString("PIB"));
        user.put("tekuciRacun", rs.getString("tekuciRacun"));
        user.put("maticniBroj", rs.getString("maticniBroj"));
        user.put("fax", rs.getString("fax"));
        user.put("adresaFirme", rs.getString("adresaFirme"));
        user.put("mestoFirme",  rs.getString("mestoFirme"));
        user.put("oprema", getUserOprema(userID));
        user.put("prekoracenje", rs.getInt("prekoracenje"));

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return user;
  }

  public JSONObject getUserOprema(int userID) {
    JSONObject userOpremaArr = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query;
    query = "SELECT * FROM Artikli WHERE isUser=true AND idMagacin=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject userOprema = new JSONObject();
          userOprema.put("id", rs.getInt("id"));
          userOprema.put("naziv", rs.getString("naziv"));
          userOprema.put("proizvodjac", rs.getString("proizvodjac"));
          userOprema.put("model", rs.getString("model"));
          userOprema.put("serijski", rs.getString("serijski"));
          userOprema.put("pon", rs.getString("pon"));
          userOprema.put("mac", rs.getString("mac"));
          userOprema.put("dobavljac", rs.getString("dobavljac"));
          userOprema.put("brDokumenta", rs.getString("brDokumenta"));
          userOprema.put("nabavnaCena", rs.getDouble("nabavnaCena"));
          userOprema.put("jMere", rs.getString("jMere"));
          userOprema.put("kolicina", rs.getString("kolicina"));
          userOprema.put("opis", rs.getString("opis"));
          userOprema.put("datum", rs.getString("datum"));
          userOprema.put("operName", rs.getString("operName"));
          userOprema.put("idMagacin", rs.getInt("idMagacin"));
          userOprema.put("isUser", rs.getBoolean("isUser"));
          userOprema.put("uniqueID", rs.getInt("uniqueID"));
          userOpremaArr.put(String.valueOf(i), userOprema);
          i++;
        }
      }
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return userOpremaArr;
  }

  public double getUkupnoUplaceno(int userID) {
    double uplaceno = 0;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT sum(duguje) as uplaceno from uplate where userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        uplaceno = rs.getDouble("uplaceno");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return uplaceno;
  }

  public JSONObject getUkupnoZaduzenjePoMesecima(int userID) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT potrazuje as dug, obracunZaMesec as zaMesec FROM uplate where userID=? AND  obracunZaMesec IS NOT NULL ORDER BY obracunZaMesec ASC";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject zaduzenje = new JSONObject();
          zaduzenje.put("zaMesec", rs.getString("zaMesec"));
          zaduzenje.put("dug", rs.getString("dug"));
          object.put(String.valueOf(i), zaduzenje);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return object;
  }

  public int getPrekoracenje(int userID) {
    int prekoracenje = 1; //default
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT prekoracenje FROM users WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        prekoracenje = rs.getInt("prekoracenje");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return prekoracenje;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }
}
