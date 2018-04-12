package net.yuvideo.jgemstone.server.classes.RACUNI;

import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class UserRacun {

    private database db;
    private String zaMesec;
    private String nazivUsluge;
    private String imePrezime;
    private String adresaRacuna;
    private String sifraKorisnika;
    private String mesto_i_datum_izdavanja;

    private UsersData user;

    private DecimalFormat df = new DecimalFormat("0.00");
    private JSONObject racun;


    public UserRacun(JSONObject rLine, String operName, database db) {
        this.db = db;
        user = new UsersData(db, operName);
        JSONObject userData = user.getUserData(rLine.getInt("userID"));
        this.imePrezime = userData.getString("ime");
        this.adresaRacuna = String.format("%s %s", userData.getString("mestoRacuna"), userData.getString("adresaRacuna"));
        this.sifraKorisnika = userData.getString("jBroj");
        this.zaMesec = rLine.getString("zaMesec");

        getDataZaMesec(rLine.getInt("userID"), rLine.getString("zaMesec"));

    }


    private void  getDataZaMesec(int userID, String zaMesec){
        System.out.println(zaMesec);
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE userID=? AND zaMesec=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setString(2, zaMesec);
            rs = ps.executeQuery();
            racun = new JSONObject();
            if(rs.isBeforeFirst()){
                int i=0;
                double ukupno = 0.00;
                double ukupnoPDV=0;
                double ukupnoOsnovica = 0;
                while (rs.next()){
                    JSONObject racunSingle = new JSONObject();
                    String usluga = rs.getString("nazivPaketa");
                    double cena = rs.getDouble("cena");
                    double popust = rs.getDouble("popust");
                    double PDV = rs.getDouble("PDV");
                    double stopaPopust = valueToPercent.getDiffValue(cena, popust);
                    double stopaPDV = valueToPercent.getDiffValue((cena - stopaPopust), PDV);
                    ukupno += (cena - stopaPopust) + stopaPDV;

                    racunSingle.put("nazivUsluge", usluga);
                    racunSingle.put("cena", Double.valueOf(df.format(cena)));
                    racunSingle.put("popust", Double.valueOf(df.format(popust)));
                    racunSingle.put("stopaPDV", Double.valueOf(df.format(stopaPDV)));
                    racunSingle.put("PDV", Double.valueOf(df.format(PDV)));
                    ukupnoPDV += stopaPDV;
                    ukupnoOsnovica += cena;

                    racun.put(String.valueOf(i), racunSingle);
                    i++;
                }

                JSONObject ukupnoObj = new JSONObject();
                ukupnoObj.put("ukupnoPDV", Double.valueOf(df.format(ukupnoPDV)));
                ukupnoObj.put("ukupnoOsnovica", Double.valueOf(df.format(ukupnoOsnovica)));
                ukupnoObj.put("ukupno", Double.valueOf(df.format(ukupno)));
                double prethodniDug = getPrethodniDug(userID, zaMesec);
                ukupnoObj.put("prethodniDug", Double.valueOf(df.format(prethodniDug)));
                double sve = prethodniDug + ukupno;
                ukupnoObj.put("ukupnoSve", Double.valueOf(df.format(sve)));
                i++;
                racun.put(String.valueOf(i), ukupnoObj);
            }
            System.out.println(racun.toString());
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private double getPrethodniDug(int userID, String zaMesec){
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
            if(rs.isBeforeFirst()){
                while (rs.next()){
                    double popust = rs.getDouble("popust");
                    double cena = rs.getDouble("cena");
                    double pdv = rs.getDouble("pdv");
                    double iznos = cena - valueToPercent.getValueOfPercentSub(cena, popust);
                    prethodniDug += iznos + valueToPercent.getValueOfPercentAdd(iznos, pdv) ;

                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        System.out.println(String.format("Prethodni dug: %f, Ukupno uplaceno: %f", prethodniDug, ukupnoUplaceno));
        prethodniDug = prethodniDug - ukupnoUplaceno;

        return prethodniDug;
    }


    public JSONObject getData() {
        return this.racun;
    }
}
