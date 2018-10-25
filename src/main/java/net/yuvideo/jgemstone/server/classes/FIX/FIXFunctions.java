package net.yuvideo.jgemstone.server.classes.FIX;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 7/15/17.
 */
public class FIXFunctions {

  private database db;
  private String operName;
  private boolean error;
  private String errorMSG;

  private DecimalFormat df = new DecimalFormat("#.00");
  public FIXFunctions(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }


  public boolean check_TELBr_bussy(String fix_tel) {
    PreparedStatement ps;
    ResultSet rs;
    String query;
    boolean brojExist = false;

    query = "SELECT brojTel from FIX_brojevi WHERE brojTel=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, fix_tel);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        brojExist = true;
      }

      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return brojExist;
  }

  public void addBroj(JSONObject rLine) {
    PreparedStatement ps;
    String query;

    query = "INSERT INTO FIX_brojevi (brojTel, UserID) VALUES (?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("FIX_TEL"));
      ps.setInt(2, rLine.getInt("userID"));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  public int getPaketID(String fix_naziv) {
    PreparedStatement ps;
    ResultSet rs;
    String query;
    int paketID = 0;

    query = "SELECT id FROM FIX_paketi WHERE naziv=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, fix_naziv);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        paketID = rs.getInt("id");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return paketID;
  }


  public void deleteService(String brojTelefona) {
    PreparedStatement ps;
    String query = "DELETE FROM FIX_brojevi WHERE brojTel=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, brojTelefona);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  public JSONObject getAccountSaobracaj(int id_ServiceUser, String zaMesec) {
    JSONObject jsonObject = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM userDebts WHERE paketType = 'FIX_SAOBRACAJ' AND id_ServiceUser=? and zaMesec=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_ServiceUser);
      ps.setString(2, zaMesec);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        jsonObject.put("id", rs.getInt("id"));
        jsonObject.put("nazivPaketa", rs.getString("nazivPaketa"));
        jsonObject.put("datumZaduzenja", rs.getString("datumZaduzenja"));
        jsonObject.put("userID", rs.getInt("userID"));

        //ukupno sa pdv i popustom
        double cena = rs.getDouble("cena");
        double pdv = rs.getDouble("PDV");
        double popust = rs.getDouble("popust");
        int kolicina = 1;

        jsonObject.put("popust", popust);
        jsonObject.put("paketType", rs.getString("paketType"));
        jsonObject.put("cena", cena);
        jsonObject.put("uplaceno", rs.getDouble("uplaceno"));
        jsonObject.put("kolicina", kolicina);
        jsonObject.put("osnovica", cena * kolicina);
        jsonObject.put("datumUplate", rs.getString("datumUplate"));
        jsonObject.put("dug", rs.getDouble("dug"));
        jsonObject.put("operater", rs.getString("operater"));
        jsonObject.put("zaduzenOd", rs.getString("zaduzenOd"));
        jsonObject.put("zaMesec", rs.getString("zaMesec"));
        jsonObject.put("skipProduzenje", rs.getBoolean("skipProduzenje"));
        jsonObject.put("pdv", pdv);

      }

      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return jsonObject;
  }

  public void obracunajZaMesec(String zaMesec) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT FIKSNA_TEL FROM servicesUser WHERE paketType LIKE '%FIX%' ";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          zaduziFixSaobracaj(rs.getString("FIKSNA_TEL"), zaMesec);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * Zaduzivanje korisnika sa Fiksnim saobracajem. Ako postoji zaduzenje za prosledjeni mesec boti
   * ce obrisani i ponovo sracunati
   *
   * @param brojTelefona broj telefona koji se zaduzuje
   * @param mesec mesec koji za koji se zaduzuje
   */
  public void zaduziFixSaobracaj(String brojTelefona, String mesec) {
    //brisanje zaduzivanje ako postoji
    deleteZaduzivanja(brojTelefona, mesec);

    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT SUM(chargedAmountRSD) as ukupno FROM csv WHERE account = ? AND connectTime like ? ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, brojTelefona);
      ps.setString(2, String.format("%s%%", mesec));
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        rs.next();
        zaduziKorisnika(brojTelefona, rs.getDouble("ukupno"), mesec);
      }

      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  private void deleteZaduzivanja(String brojTelefona, String mesec) {
    PreparedStatement ps;
    String query = "delete FROM zaduzenja WHERE naziv = ? AND zaMesec=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, String.format("Saobraćaj-%s", brojTelefona));
      ps.setString(2, mesec);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  private void deleteZaduzenje(int id) {
    PreparedStatement ps;
    String query = "DELETE FROM zaduzenja WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  private void zaduziKorisnika(String brojTelefona, Double ukupno, String zaMesec) {
    PreparedStatement ps;
    ResultSet rs = null;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String query;

    double pdv = 0;
    double cena = 0;
    int userID = 0;

    query = "SELECT * FROM servicesUser WHERE FIKSNA_TEL=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, brojTelefona);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        rs.next();
        //zaduzivanje saobracaja korisnika bez pdv
        //pdv = rs.getDouble("pdv");
        userID = rs.getInt("userID");

      } else {
        ps.close();
        rs.close();
        return;
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    //zaduzivanje saobracaja korisnika bez pdv
    //cena = ukupno - valueToPercent.getPDVOfValue(ukupno, pdv);

    query = "INSERT INTO zaduzenja "
        + "(datum, cena, pdv, naziv, opis, zaduzenOd, userID, paketType, dug, zaMesec) "
        + "VALUES "
        + "(?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDate.now().format(dtf));
      //ako zuduzujemo korisnika sa pdvom umesto ukupno upisati cenu  ^^ pogledaj gore komentar
      ps.setDouble(2, Double.parseDouble(df.format(ukupno)));
      ps.setDouble(3, pdv);
      ps.setString(4, String.format("Saobraćaj-%s", brojTelefona));
      ps.setString(5,
          String.format("Zaduženje saobracaja za broj %s, mesec %s", brojTelefona, zaMesec));
      ps.setString(6, getOperName());
      ps.setInt(7, userID);
      ps.setString(8, "FIX_SAOBRACAJ");
      ps.setDouble(9, Double.parseDouble(df.format(ukupno)));
      ps.setString(10, zaMesec);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    System.out.println(String.format("broj %s cena: %f ukupno: %f", brojTelefona, cena, ukupno));

  }

  public boolean deleteFixPaket(int id) {
    boolean deleted = false;
    PreparedStatement ps;
    String query = "DELETE FROM FIX_paketi WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      deleted = true;
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return deleted;


  }

  public JSONObject getPoziviZaMesec(String zaMesec, String account) {
    CSVData csvData = new CSVData(db);
    JSONObject data = csvData.getZaMesecByCountry(zaMesec, account);
    if (csvData.isError()) {
      data.put("ERROR", csvData.getErrorMSG());
    }
    return data;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
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

  public void deleteCSV_ID(JSONArray intArrays) {
    PreparedStatement ps;
    String query = "DELETE FROM csv WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      for (int i = 0; i < intArrays.length(); i++) {
        ps.setInt(1, intArrays.getInt(i));
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }
}
