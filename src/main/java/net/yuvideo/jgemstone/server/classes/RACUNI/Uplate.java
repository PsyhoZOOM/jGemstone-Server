package net.yuvideo.jgemstone.server.classes.RACUNI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class Uplate {

  private database db;
  private String operName;
  private String errorMSG;
  private boolean error;
  private SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


  public Uplate(String operName, database db) {
    this.db = db;
    this.operName = operName;
  }

  public void novaUplata(int userID, double uplaceno, String opis, String datumUplate) {
    PreparedStatement ps;
    String query = "INSERT INTO uplate (datum,  duguje, operater, opis, userID, realDate)"
        + "VALUES "
        + "(?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, datumUplate);
      ps.setDouble(2, uplaceno);
      ps.setString(3, operName);
      ps.setString(4, opis);
      ps.setInt(5, userID);
      ps.setString(6,
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    //produzivanje serisa
    produzivanjeServisa(userID);
  }

  public void deleteUplata(int idUplate) {
    PreparedStatement ps;
    String query = "DELETE FROM uplate WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idUplate);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());

      e.printStackTrace();
    }

  }

  /**
   * Produzivanje servisa <br> <p>Produzivanje servisa provera ukupan dug po mesecima i ukupne
   * uplate od korisnika, zatim skida svaki mesec ako uplata zadovaljava sumu, ako na kraju ostane
   * manje meseca od (int) produzenja korisniku se produzavaju svi servisi za jedan mesec (+1).</p>
   * <p>1. Uzimamo variablu produzenje.</p> <p>2. Uzimamo ukupne uplate.</p> <p>3. Uzimamo ukupan
   * dug po mesecima.</p> <p>4. Skidamo svaki mesec od sume. Ako suma dodje do 0 a ostalo je manje
   * meseca od produzenja produzujemo servis.</p>
   *
   * @param userID korisnik za produzivanje servisa.
   */
  public String produzivanjeServisa(int userID) {

    int prekoracenje = 0;
    double ukupnoUplaceno = 0;
    double dug = 0;
    UsersData usersData = new UsersData(db, getOperName());
    //Potreban nam je int var od produzenja
    prekoracenje = usersData.getPrekoracenje(userID);

    //Potrebne su nam sve uplate korisnika;
    ukupnoUplaceno = usersData.getUkupnoUplaceno(userID);

    //Potrebna su name zaduzenja po mesecima
    JSONObject ukupnoZaduzenjePoMesecima = usersData.getUkupnoZaduzenjePoMesecima(userID);

    //get endDate service;
    String endDate = null;
    LocalDate date = null;

    System.out.println(ukupnoZaduzenjePoMesecima.length());
    //ako nema uplate aj zdravo
    if (ukupnoZaduzenjePoMesecima.length() <= 0) {
      return null;
    }
    int meseci = 0;
    for (int i = 0; i < ukupnoZaduzenjePoMesecima.length(); i++) {
      endDate = ukupnoZaduzenjePoMesecima.getJSONObject(String.valueOf(i)).getString("zaMesec");
      dug = ukupnoZaduzenjePoMesecima.getJSONObject(String.valueOf(i)).getDouble("dug");
      date = LocalDate.parse(endDate + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
          .plusMonths(prekoracenje);

      System.out.println(String.format("uplaceno: %f dug: %f", ukupnoUplaceno, dug));
      if (ukupnoUplaceno >= dug) {
        ukupnoUplaceno = ukupnoUplaceno - dug;
        date = LocalDate.parse(endDate + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .plusMonths(prekoracenje + 1);
        System.out.println(ukupnoUplaceno);
        meseci++;

      } else {
        break;
      }
    }
    if (prekoracenje <= meseci) {
      System.out.println("Produzujemo servies za jedan mesec");
      String msg = String
          .format("Prekoracenje: %d, Ukupno zaduzenje: %d, Mesec %d, endDate= %s", prekoracenje,
              ukupnoZaduzenjePoMesecima.length(), meseci,
              date.toString());
      System.out.println(msg);
    } else {
      System.out.println("NE PRODUZUJEMO");
      String msg = String
          .format("Prekoracenje: %d, Ukupno zaduzenje: %d, Mesec %d, endDate= %s", prekoracenje,
              ukupnoZaduzenjePoMesecima.length(), meseci, date.toString());
      System.out.println(msg);
    }

    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.produziAllService(userID, date.toString());

    return date.toString();

  }

  public JSONObject getMesecnaZaduzenja(int userID) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = " SELECT sum(dug) as dug, zaMesec FROM zaduzenja WHERE userID=? group by zaMesec order by zaMesec desc";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject data = new JSONObject();
          data.put("zaMesec", rs.getString("zaMesec"));
          data.put("dug", rs.getDouble("dug"));
          object.put(String.valueOf(i), data);
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

  public JSONObject getMesecnaZaduzenjaServisi(int userID, String zaMesec) {
    JSONObject object = new JSONObject();

    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT *  FROM zaduzenja WHERE userID=? and zaMesec=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      ps.setString(2, zaMesec);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject data = new JSONObject();
          data.put("id", rs.getInt("id"));
          data.put("datum", rs.getString("datum"));
          data.put("cena", rs.getDouble("cena"));
          data.put("kolicina", rs.getInt("kolicina"));
          data.put("jMere", rs.getString("jMere"));
          data.put("pdv", rs.getDouble("pdv"));
          data.put("popust", rs.getDouble("popust"));
          data.put("naziv", rs.getString("naziv"));
          data.put("opis", rs.getString("opis"));
          data.put("zaduzenOd", rs.getString("zaduzenOd"));
          data.put("userID", rs.getInt("userID"));
          data.put("paketType", rs.getString("paketType"));
          data.put("dug", rs.getDouble("dug"));
          data.put("zaMesec", rs.getString("zaMesec"));
          object.put(String.valueOf(i), data);
          i++;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return object;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
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
