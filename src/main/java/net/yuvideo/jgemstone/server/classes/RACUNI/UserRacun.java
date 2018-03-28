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

    private int userID;
    private int kolicina;
    private double iznos;
    private double popust;
    private double osnovica;
    private double stopPDV;
    private double PDV;
    private double ukupno;
    private double prethodniDUG = 0;

    private UsersData user;

    DecimalFormat df = new DecimalFormat("#,###,###,###,###,###,##0.00");

    private JSONObject data;


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
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE userID=? AND zaMesec=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setString(2, zaMesec);
            rs = ps.executeQuery();
            data = new JSONObject();
            if(rs.isBeforeFirst()){
                int i=0;
                double ukupnoUkupno= 0;
                double ukupnoPDV=0;
                double ukpnoOsnovica=0;
                while (rs.next()){
                    String usluga = rs.getString("nazivPaketa");
                    int kolicina = 1; ////TODO setkolicina
                    double iznos = rs.getDouble("cena");
                    double popust = rs.getDouble("popust");
                    double osnovica =  iznos * kolicina;
                    double stopaPDV = rs.getDouble("PDV");
                    double PDV = valueToPercent.getValueOfPercentAdd(iznos, stopaPDV);
                    double ukupno;
                    ukupno = osnovica - valueToPercent.getValueOfPercentAdd(osnovica, popust) + PDV;



                    ////TODO get data
                    data.put(String.valueOf(i), String.format(
                                    "Ime: %20s " +
                                    "Usluga: %-30s " +
                                    "Iznos: %10s " +
                                            "Popust: %10s " +
                                            "Osnovica: %10s " +
                                            "Stopa PDV: %10s " +
                                            "PDV: %10s " +
                                            "UKUPNO: %10s ",
                            this.imePrezime,
                            usluga,
                            df.format(iznos),
                            df.format(popust),
                            df.format(osnovica),
                            df.format(stopaPDV),
                            df.format(PDV),
                            df.format(ukupno)
                    ));
                    ukupnoPDV += PDV;
                    ukpnoOsnovica += osnovica;
                    i++;

                }
                prethodniDUG = getPrethodniDug(userID, zaMesec);
                ukupnoUkupno = ukpnoOsnovica + ukupnoPDV;
                data.put(String.valueOf(i), String.format("Ukupno osnovica: %-10s Ukupno PDV: %-10s Ukupno: %-10s", df.format(ukpnoOsnovica), df.format(ukupnoPDV), df.format(ukupnoUkupno)));
                i++;
                data.put(String.valueOf(i), String.format("Prethodni dug: %-10s",df.format(prethodniDUG)));
                i++;
                data.put(String.valueOf(i), String.format("UKUPAN DUG: %-10s", df.format(ukupnoUkupno+prethodniDUG)));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private double getPrethodniDug(int userID, String zaMesec){
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE zaMesec < ? AND userID=?";
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
                    double uplaceno = rs.getDouble("uplaceno");
                    double iznos = cena - valueToPercent.getValueOfPercentAdd(cena,popust);
                    prethodniDug += iznos + valueToPercent.getValueOfPercentAdd(iznos, pdv) ;
                    prethodniDug -= uplaceno;
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prethodniDug;
    }


    public JSONObject getData() {
        return data;
    }
}
