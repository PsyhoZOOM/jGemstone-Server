package net.yuvideo.jgemstone.server.classes.RACUNI;

import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Set;

public class UserRacun {

    private final String mestoRacuna;
    private database db;
    private String zaMesec;
    private String imePrezime;
    private String adresaRacuna;
    private String sifraKorisnika;
    private String datumZaduzenja;
    private String adresaKorisnka;

    private double zaduzenjeZaObrPeriod = 0.00;
    private double ukupnoOsnovica = 0.00;
    private double ukupnoPDV = 0.00;
    private UsersData user;

    private DecimalFormat df = new DecimalFormat("0.00");
    private JSONObject racun;


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

        getDataZaMesec(rLine.getInt("userID"), rLine.getString("zaMesec"));

    }


    private void getDataZaMesec(int userID, String zaMesec) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE userID=? AND zaMesec=?";

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
                    nazivUsluge = rs.getString("nazivPaketa");
                    datumZaduzenja = rs.getString("datumZaduzenja");
                    double cena = rs.getDouble("cena");
                    double stopaPopust = rs.getDouble("popust");
                    double stopaPDV = rs.getDouble("PDV");

                    racunSingle.put("nazivUsluge", nazivUsluge);
                    racunSingle.put("cena", cena);
                    racunSingle.put("stopaPopust", stopaPopust);
                    racunSingle.put("stopaPDV", stopaPDV);
                    racunSingle.put("kolicina", 1);
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
            finalRacun.put("zaMesec", zaMesec);
            finalRacun.put("zaPeriod", zaMesec);
            finalRacun.put("adresaRacuna", adresaRacuna);
            finalRacun.put("mestoRacuna", mestoRacuna);
            finalRacun.put("ukupnoOsnovica", Double.valueOf(df.format(ukupnoOsnovica)));
            finalRacun.put("ukupnoPDV", Double.valueOf(df.format(ukupnoPDV)));
            finalRacun.put("zaduzenjeZaObrPeriod", Double.valueOf(df.format(zaduzenjeZaObrPeriod)));
            finalRacun.put("prethodniDug", Double.valueOf(df.format(prethodniDug)));
            finalRacun.put("ukupnoZaUplatu", Double.valueOf(df.format(ukupnoZaUplatu)));

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
            double osnovica = cena * kolicina;
            double ukupno;


            popust = valueToPercent.getDiffValue(osnovica, stopaPopust);
            osnovica -= popust;
            pdv = valueToPercent.getDiffValue(osnovica, stopaPDV);
            ukupno = osnovica + pdv;
            zaduzenjeZaObrPeriod += osnovica + pdv;
            ukupnoPDV += pdv;
            ukupnoOsnovica += osnovica;

            JSONObject tmp = new JSONObject();
            tmp.put("nazivUsluge", nazivUsluge);
            tmp.put("kolicina", kolicina);
            tmp.put("cena", Double.valueOf(df.format(cena)));
            tmp.put("popust", Double.valueOf(df.format(stopaPopust)));
            tmp.put("osnovica", Double.valueOf(df.format(osnovica)));
            tmp.put("pdv", Double.valueOf(df.format(pdv)));
            tmp.put("stopaPDV", Double.valueOf(df.format(stopaPDV)));
            tmp.put("ukupno", Double.valueOf(df.format(ukupno)));
            racunNew.put(String.valueOf(i), tmp);

        }

        return racunNew;
    }

    private JSONObject calculateDuplicateRacun(JSONObject racun) {
        JSONObject racunTmp = new JSONObject();
        for (int i = 0; i < racun.length(); i++) {
            int kolicina = 1;

            for (int z = i + 1; z < racun.length(); z++) {
                JSONObject racunI = racun.getJSONObject(String.valueOf(i));
                JSONObject racunZ = racun.getJSONObject(String.valueOf(z));
                if (
                        racunI.getDouble("cena") == racunZ.getDouble("cena") &&
                                racunI.getDouble("stopaPDV") == racunZ.getDouble("stopaPDV") &&
                                racunI.getDouble("stopaPopust") == racunZ.getDouble("stopaPopust") &&
                                racunI.getString("nazivUsluge").equals(racunZ.getString("nazivUsluge"))
                        ) {
                    kolicina++;
                    racun.getJSONObject(String.valueOf(i)).remove("kolicina");
                    racun.getJSONObject(String.valueOf(i)).put("kolicina", kolicina);
                    racun.remove(String.valueOf(z));

                }
            }
        }

        //sortinig keyset
        Set<String> string = racun.keySet();
        int i = 0;
        for (String str : string) {
            racunTmp.put(String.valueOf(i), racun.getJSONObject(str));
            i++;
        }


        return racunTmp;
    }


    private double getPrethodniDug(int userID, String zaMesec) {
        PreparedStatement ps;
        ResultSet rs;
        double ukupnoUplaceno = 0;
        String query = "SELECT SUM(uplaceno) as uplaceno FROM uplate WHERE userID=?";
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


        query = "SELECT * FROM userDebts WHERE zaMesec < ? AND userID=?";
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
                    double pdv = rs.getDouble("pdv");
                    double iznos = cena - valueToPercent.getValueOfPercentSub(cena, popust);
                    prethodniDug += iznos + valueToPercent.getValueOfPercentAdd(iznos, pdv);

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
}
