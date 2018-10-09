package net.yuvideo.jgemstone.server.classes.OBRACUNI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.Users;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

public class MesecniObracun {

  public boolean hasError = false;
  public String errorMessage = "";
  public JSONObject mesecniObracunObject;
  private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
  private DateTimeFormatter dtfNormal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private String operName;
  private database db;
  private boolean error;
  private String errorMSG;

  public MesecniObracun(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }


  public JSONObject getMesecniObracunPDV(int userOd, int userDo, String datumOd, String datumDo,
      String operName) {
    this.operName = operName;
    mesecniObracunObject = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM  zaduzenja WHERE zaMesec >= ? AND zaMesec <= ? AND userID >= ? AND userID <= ?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDate.parse(datumOd, dtfNormal).format(dtf));
      ps.setString(2, LocalDate.parse(datumDo, dtfNormal).format(dtf));
      ps.setInt(3, userOd);
      ps.setInt(4, userDo);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        double ukupnaOsnovica = 0;
        double ukupnoPDV = 0;
        double ukupno = 0;
        while (rs.next()) {
          JSONObject mesec = new JSONObject();
          double cena = rs.getDouble("cena");
          double popust = rs.getDouble("popust");
          double pdv = rs.getDouble("pdv");
          double cenaSaPopustom = cena - valueToPercent.getPDVOfSum(cena, popust);
          int kolicina = rs.getInt("kolicina");
          double osnovica = cenaSaPopustom * kolicina;
          double pdvCena = valueToPercent.getPDVOfValue(osnovica, pdv);

          ukupnoPDV += pdvCena;
          ukupnaOsnovica += osnovica;
          ukupno += osnovica + pdvCena;

          UsersData user = new UsersData(db, operName);
          JSONObject userObj = user.getUserData(rs.getInt("userID"));
          String imePrezime = userObj.getString("ime");
          String jBroj = userObj.getString("jBroj");

          mesec.put("id", rs.getInt("id"));
          mesec.put("naziv", rs.getString("naziv"));
          mesec.put("imePrezime", imePrezime);
          mesec.put("jBroj", jBroj);
          mesec.put("cena", cenaSaPopustom);
          mesec.put("kolicina", kolicina);
          mesec.put("popust", popust);
          mesec.put("pdv",pdv);
          mesec.put("pdvCena", pdvCena);
          mesec.put("osnovica", osnovica);
          mesec.put("ukupno", osnovica + pdvCena);
          mesecniObracunObject.put(String.valueOf(i), mesec);
          i++;
        }
        JSONObject finalObj = new JSONObject();
        finalObj.put("ukupnoPDV", ukupnoPDV);
        finalObj.put("ukupnaOsnovica", ukupnaOsnovica);
        finalObj.put("ukupno", ukupno);
        mesecniObracunObject.put(String.valueOf(i), finalObj);

      }

    } catch (SQLException e) {
      hasError = true;
      errorMessage = e.getMessage();
      e.printStackTrace();
    }

    return mesecniObracunObject;
  }

  /**
   * Obracunavanje za mesec. Ako vec postji obracun za taj mesec biti ce obrisani i ponovo ce se
   * izvrsiti obracun
   *
   * @param zaMesec mesec za koji se vrsi obracun
   * @return ukupan obracun za sve korisnike za taj mesec
   */
  public double obracunajZaMesec(String zaMesec) {
    PreparedStatement ps;
    ResultSet rs;
    double ukupno = 0;
    String query = "SELECT  SUM(dug) as dug, userID from zaduzenja WHERE zaMesec = ? group by userID";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, zaMesec);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          ukupno += rs.getDouble("dug");
          checkObracunExist(zaMesec, rs.getInt("userID"));
          zaduziKorisnika(rs.getInt("userID"), rs.getDouble("dug"), zaMesec);
        }
      }

      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return ukupno;
  }

  private void checkObracunExist(String zaMesec, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT obracunZaMesec from uplate where obracunZaMesec=? and userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, zaMesec);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        deleteObracunZaMesec(zaMesec, userID);
      }
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  private void deleteObracunZaMesec(String zaMesec, int userID) {
    PreparedStatement ps;
    String query = "DELETE FROM uplate WHERE obracunZaMesec=? and userID=? ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, zaMesec);
      ps.setInt(2, userID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }


  private void zaduziKorisnika(int userID, double dug, String zaMesec) {
    PreparedStatement ps;
    String query;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/YY");
    UsersData usersData = new UsersData(db, getOperName());
    JSONObject userData = usersData.getUserData(userID);
    System.out.println(userID);
    String jBroj = userData.getString("jBroj");
    String opis = String.format("%s/%s", jBroj,
        LocalDate.parse(zaMesec + "-01", dtfNormal).format(dateTimeFormatter));

    query = "INSERT INTO uplate "
        + "(datum, potrazuje, operater, opis, userID, obracunZaMesec) "
        + "VALUES "
        + "(?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDate.now().format(dtfNormal));
      ps.setDouble(2, dug);
      ps.setString(3, getOperName());
      ps.setString(4, opis);
      ps.setInt(5, userID);
      ps.setString(6, zaMesec);
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

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }
}
