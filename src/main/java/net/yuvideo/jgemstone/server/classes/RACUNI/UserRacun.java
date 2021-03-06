package net.yuvideo.jgemstone.server.classes.RACUNI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

public class UserRacun {

  private String mestoRacuna;
  private String adresaFirme;
  private String mestoFirme;
  private database db;
  private String zaMesec;
  private String imePrezime;
  private String nazivFirme;
  private String adresaRacuna;
  private String sifraKorisnika;
  private String datumZaduzenja;
  private String adresaKorisnka;
  private String PIB;
  private String maticniBroj;
  private String kontaktOsobaTel;
  private String fax;
  private String tekuciRacun;

  private double zaduzenjeZaObrPeriod = 0.00;
  private double ukupnoOsnovica = 0.00;
  private double ukupnoPDV = 0.00;
  private UsersData user;
  private String operName;
  private boolean error;
  private String errorMSG;

  private JSONObject racun;

  public UserRacun(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }


  public UserRacun(JSONObject rLine, String operName, database db) {
    this.db = db;
    user = new UsersData(db, operName);
    JSONObject userData = user.getUserData(rLine.getInt("userID"));
    this.imePrezime = userData.getString("ime");
    this.adresaRacuna = userData.getString("adresaRacuna");
    this.mestoRacuna = userData.getString("mestoRacuna");
    this.adresaKorisnka = userData.getString("adresa");
    this.sifraKorisnika = userData.getString("jBroj");
    this.zaMesec = rLine.getString("zaMesec");
    this.nazivFirme = userData.getString("nazivFirme");
    this.adresaFirme = userData.getString("adresaFirme");
    this.mestoFirme = userData.getString("mestoFirme");
    this.PIB = userData.getString("PIB");
    this.maticniBroj = userData.getString("maticniBroj");
    this.kontaktOsobaTel = userData.getString("kontaktOsobaTel");
    this.fax = userData.getString("fax");
    this.tekuciRacun = userData.getString("tekuciRacun");

    getDataZaMesec(rLine.getInt("userID"), rLine.getString("zaMesec"));

  }


  private void getDataZaMesec(int userID, String zaMesec) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM zaduzenja WHERE userID=? AND zaMesec=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      ps.setString(2, zaMesec);
      rs = ps.executeQuery();

      racun = new JSONObject();
      String nazivUsluge;

      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject racunSingle = new JSONObject();
          nazivUsluge = rs.getString("naziv");
          datumZaduzenja = rs.getString("datum");
          double cena = rs.getDouble("cena");
          double stopaPopust = rs.getDouble("popust");
          double stopaPDV = rs.getDouble("PDV");
          int kolicina = rs.getInt("kolicina");
          double osnovica = cena * kolicina;
          double zaUplatu = osnovica - valueToPercent.getPDVOfValue(osnovica, stopaPopust);
          zaUplatu = zaUplatu + valueToPercent.getPDVOfValue(zaUplatu, stopaPDV);

          String jMere = rs.getString("jMere");

          racunSingle.put("nazivUsluge", nazivUsluge);
          racunSingle.put("cena", cena);
          racunSingle.put("stopaPopust", stopaPopust);
          racunSingle.put("stopaPDV", stopaPDV);
          racunSingle.put("kolicina", kolicina);
          racunSingle.put("jMere", jMere);
          racunSingle.put("osnovica", osnovica);
          racunSingle.put("zaUplatu", zaUplatu);
          racunSingle.put("index", i);
          racun.put(String.valueOf(i), racunSingle);

          i++;
        }
      }

      racun = calculateDuplicateRacun(racun);
      racun = calculateSUM(racun);
      int u = racun.length();
      double prethodniDug = getPrethodniDug(userID, zaMesec);
      double ukupnoZaUplatu = zaduzenjeZaObrPeriod + prethodniDug;

      JSONObject finalRacun = new JSONObject();
      finalRacun.put("imePrezime", imePrezime);
      finalRacun.put("adresaKorisnika", adresaKorisnka);
      finalRacun.put("sifraKorisnika", sifraKorisnika);
      finalRacun.put("nazivFirme", nazivFirme);
      finalRacun.put("adresaFirme", adresaFirme);
      finalRacun.put("mestoFirme", mestoFirme);
      finalRacun.put("PIB", PIB);
      finalRacun.put("fax", fax);
      finalRacun.put("tekuciRacun", tekuciRacun);
      finalRacun.put("maticniBroj", maticniBroj);
      finalRacun.put("kontaktOsobaTel", kontaktOsobaTel);
      finalRacun.put("zaMesec", zaMesec);
      finalRacun.put("zaPeriod", zaMesec);
      finalRacun.put("adresaRacuna", adresaRacuna);
      finalRacun.put("mestoRacuna", mestoRacuna);
      finalRacun.put("ukupnoOsnovica", ukupnoOsnovica);
      finalRacun.put("ukupnoPDV", ukupnoPDV);
      finalRacun.put("zaduzenjeZaObrPeriod", zaduzenjeZaObrPeriod);
      finalRacun.put("prethodniDug", prethodniDug);
      finalRacun.put("ukupnoZaUplatu", ukupnoZaUplatu);

      racun.put(String.valueOf(u), finalRacun);

      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  private JSONObject calculateSUM(JSONObject racun) {
    JSONObject racunNew = new JSONObject();
    for (int i = 0; i < racun.length(); i++) {
      JSONObject rcnTmp = racun.getJSONObject(String.valueOf(i));
      String nazivUsluge = rcnTmp.getString("nazivUsluge");
      double cena = rcnTmp.getDouble("cena");
      double popust = 0.00;
      double stopaPopust = rcnTmp.getDouble("stopaPopust");
      double pdv = 0.00;
      double stopaPDV = rcnTmp.getDouble("stopaPDV");
      int kolicina = rcnTmp.getInt("kolicina");
      String jMere = rcnTmp.getString("jMere");
      double osnovica = cena * kolicina;
      double vrednostBezPDV=  osnovica;
      double ukupno;

      popust = valueToPercent.getPDVOfSum(osnovica, stopaPopust);
      osnovica -= popust;
      pdv = valueToPercent.getPDVOfValue(osnovica, stopaPDV);
      ukupno = osnovica + pdv;
      zaduzenjeZaObrPeriod += osnovica + pdv;
      ukupnoPDV += pdv;
      ukupnoOsnovica += osnovica;

      JSONObject tmp = new JSONObject();
      tmp.put("nazivUsluge", nazivUsluge);
      tmp.put("kolicina", kolicina);
      tmp.put("jMere", jMere);
      tmp.put("cena", cena);
      tmp.put("popust", stopaPopust);
      tmp.put("osnovica", osnovica);
      tmp.put("pdv", pdv);
      tmp.put("stopaPDV", stopaPDV);
      tmp.put("ukupno", ukupno);
      tmp.put("vrednostBezPDV", vrednostBezPDV);
      racunNew.put(String.valueOf(i), tmp);

    }

    return racunNew;
  }

  private JSONObject calculateDuplicateRacun(JSONObject racun) {
    JSONObject racunTmp = new JSONObject();
    for (int i = 0; i < racun.length(); i++) {

      for (int z = i + 1; z < racun.length(); z++) {
        int kolicina = 0;
        double osnovica = 0;
        double zaUplatu = 0;
        {
          JSONObject racunI = racun.getJSONObject(String.valueOf(i));
          JSONObject racunZ = racun.getJSONObject(String.valueOf(z));

          if (!racunI.has("osnovica") || !racunZ.has("osnovica")) {
            System.out.println("ERRORRRRR");
          }
          if (
              racunI.getDouble("cena") == racunZ.getDouble("cena") &&
                  racunI.getDouble("stopaPDV") == racunZ.getDouble("stopaPDV") &&
                  racunI.getDouble("stopaPopust") == racunZ.getDouble("stopaPopust") &&
                  racunI.getString("nazivUsluge").equals(racunZ.getString("nazivUsluge")) &&
                  racunI.getString("jMere").equals(racunZ.getString("jMere"))
              ) {


            kolicina = racunI.getInt("kolicina") + racunZ.getInt("kolicina");
            osnovica = racunI.getDouble("osnovica") + racunZ.getDouble("osnovica");
            zaUplatu = racunI.getDouble("zaUplatu") + racunZ.getDouble("zaUplatu");

            racun.getJSONObject(String.valueOf(i)).remove("kolicina");
            racun.getJSONObject(String.valueOf(i)).put("kolicina", kolicina);

            racun.getJSONObject(String.valueOf(i)).remove("osnovica");
            racun.getJSONObject(String.valueOf(i)).put("osnovica", osnovica);

            racun.getJSONObject(String.valueOf(i)).remove("zaUlatu");
            racun.getJSONObject(String.valueOf(i)).put("zaUplatu", zaUplatu);

            racun.remove(String.valueOf(z));
            racun = sortJSON(racun);
          }
          kolicina = 0;
          zaUplatu = 0;
          osnovica = 0;

        }
      }
    }
    return racun;
  }

  private JSONObject sortJSON(JSONObject racun) {
    JSONObject tmp = new JSONObject();
    int i = 0;
    for (String ket : racun.keySet()) {
      tmp.put(String.valueOf(i), racun.getJSONObject(ket));
      i++;

    }
    return tmp;
  }

  public double getUkupanDug(int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "select sum(duguje) as uplaceno, sum(potrazuje) as dug from uplate where userID=?";
    double dug = 0;
    double uplaceno = 0;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        uplaceno = rs.getDouble("uplaceno");
        dug = rs.getDouble("dug");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return dug - uplaceno;
  }

  private double getPrethodniDug(int userID, String zaMesec) {
    PreparedStatement ps;
    ResultSet rs;
    double ukupnoUplaceno = 0;
    String query = "SELECT SUM(duguje) as uplaceno FROM uplate WHERE userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        ukupnoUplaceno = rs.getDouble("uplaceno");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    query = "SELECT * FROM zaduzenja WHERE zaMesec < ? AND userID=?";
    double prethodniDug = 0;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, zaMesec);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          double popust = rs.getDouble("popust");
          double cena = rs.getDouble("cena");
          int kolicina = rs.getInt("kolicina");
          double pdv = rs.getDouble("pdv");
          double osnovica = cena * kolicina;
          osnovica = osnovica - valueToPercent.getPDVOfValue(osnovica, popust);
          double iznos = osnovica + valueToPercent.getPDVOfValue(osnovica, pdv);
          prethodniDug += iznos;

        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    prethodniDug = prethodniDug - ukupnoUplaceno;

    return prethodniDug;
  }

  public JSONObject getData() {
    return this.racun;
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
