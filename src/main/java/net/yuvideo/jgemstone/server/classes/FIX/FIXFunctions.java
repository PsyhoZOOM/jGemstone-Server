package net.yuvideo.jgemstone.server.classes.FIX;

import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by PsyhoZOOM@gmail.com on 7/15/17.
 */
public class FIXFunctions {
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    public static Boolean check_TELBr_bussy(String fix_tel, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String query;
        boolean brojExist = false;

        query = "SELECT brojTel from FIX_brojevi WHERE brojTel=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, fix_tel);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst())
                brojExist = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return brojExist;
    }

    public static void addBroj(JSONObject rLine, String opername, database db) {
        PreparedStatement ps;
        String query;

        query = "INSERT INTO FIX_brojevi (brojTel, UserID) VALUES (?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("FIX_TEL"));
            ps.setInt(2, rLine.getInt("userID"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static int getPaketID(String fix_naziv, database db) {
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("PAKET_ID=" + paketID);
        return paketID;
    }

    public static boolean check_if_obracun_postoji(String zaMesec, database db){
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT zaMesec FROM zaduzenjaFiksna WHERE zaMesec=?";
        boolean exist = false;

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, zaMesec);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst())
                exist = true;

            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return exist;
    }

    public static JSONObject obracunajZaMesec(database db, String zaMesec, String operName) {
        JSONObject obj = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        String query = "SELECT SUM(chargedAmountRSD) AS ukupno, account  FROM csv WHERE connectTime LIKE  ? group by account ";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, zaMesec + "%");
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    zaduziKorisnika(rs.getString("account"), rs.getDouble("ukupno"),
                            zaMesec, operName, db);
                }
            }
            ps.close();
            rs.close();
            obj.put("SNIMLJENO", true);
        } catch (SQLException e) {
            obj.put("Error", e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    private static void zaduziKorisnika(String account, Double ukupno, String zaMesec, String operName, database db) {
        PreparedStatement ps;
        ResultSet rs = null;
        DecimalFormat df = new DecimalFormat("0.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //String query = "INSERT INTO FIX_Debts (zaMesec, dug, account, operName, datumZaduzenja) " +
        //        "VALUES (?,?,?,?,?)";
        String query;

        query = "SELECT * FROM servicesUser WHERE FIKSNA_TEL = ?";


        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, account);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();

            } else {
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT INTO userDebts " +
                "(id_ServiceUser, nazivPaketa, datumZaduzenja, userID, popust, paketType, cena, uplaceno," +
                "datumUplate, dug, operater, zaduzenOd, zaMesec, skipProduzenje, PDV )" +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            double cena = ukupno;
            double popust = rs.getDouble("popust");
            double pdv = rs.getDouble("PDV");

            double dug = cena - valueToPercent.getDiffValue(cena, popust);
            dug = dug + valueToPercent.getDiffValue(cena, pdv);

            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rs.getInt("id"));
            ps.setString(2, "Saobraćaj-" + account);
            ps.setString(3, LocalDate.now().format(dtf));
            ps.setInt(4, rs.getInt("userID"));
            ps.setDouble(5, rs.getDouble("popust"));
            ps.setString(6, "FIX_SAOBRACAJ");
            ps.setDouble(7, Double.valueOf(df.format(cena)));
            ps.setDouble(8, 0.00);
            ps.setString(9, "1000-01-01 00:00:00");
            ps.setDouble(10, Double.valueOf(df.format(dug)));
            ps.setString(11, "");
            ps.setString(12, "SYSTEM");
            ps.setString(13, zaMesec);
            ps.setBoolean(14, false);
            ps.setDouble(15, rs.getDouble("PDV"));
            ps.executeUpdate();
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void deleteService(String brojTelefona, database db) {
        PreparedStatement ps;
        String query = "DELETE FROM FIX_brojevi WHERE brojTel=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, brojTelefona);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getAccountSaobracaj(String account, String zaMesec, database db) {
        JSONObject jsonObject = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE paketType = 'FIX_SAOBRACAJ' AND nazivPaketa=? and zaMesec=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, "Saobraćaj-" + account);
            ps.setString(2, zaMesec);
            rs = ps.executeQuery();
            System.out.printf(ps.toString());
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
                double ukupno = cena - valueToPercent.getDiffValue(cena, popust);
                ukupno = ukupno + valueToPercent.getDiffValue(ukupno, pdv);

                jsonObject.put("popust", popust);
                jsonObject.put("paketType", rs.getString("paketType"));
                jsonObject.put("cena", cena);
                jsonObject.put("uplaceno", rs.getDouble("uplaceno"));
                jsonObject.put("datumUplate", rs.getString("datumUplate"));
                jsonObject.put("dug", ukupno);
                jsonObject.put("operater", rs.getString("operater"));
                jsonObject.put("zaduzenOd", rs.getString("zaduzenOd"));
                jsonObject.put("zaMesec", rs.getString("zaMesec"));
                jsonObject.put("skipProduzenje", rs.getBoolean("skipProduzenje"));
                jsonObject.put("pdv", pdv);

            }


            ps.close();
            rs.close();
        } catch (SQLException e) {
            jsonObject.put("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static void uplati() {

    }

}
