package net.yuvideo.jgemstone.server.classes.FIX;

import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
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
        DecimalFormat df = new DecimalFormat("0.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String query = "INSERT INTO FIX_Debts (zaMesec, dug, account, operName, datumZaduzenja) " +
                "VALUES (?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, zaMesec);
            ps.setDouble(2, Double.parseDouble(df.format(ukupno)));
            ps.setString(3, account);
            ps.setString(4, operName);
            ps.setString(5, LocalDateTime.now().format(dtf));
            ps.executeUpdate();
            ps.close();
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

    public static JSONObject getAccountSaobracaj(String account, String zaMesec, Double pdv, Double popust, database db) {
        JSONObject jsonObject = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM FIX_Debts WHERE account=? and zaMesec=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, account);
            ps.setString(2, zaMesec);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                Double cena = rs.getDouble("dug");
                Double dug = cena - valueToPercent.getDiffValue(cena, popust);
                dug = dug + valueToPercent.getDiffValue(dug, pdv);
                jsonObject.put("dug", dug);
                jsonObject.put("cena", cena);
                jsonObject.put("pdv", pdv);
                jsonObject.put("popust", popust);
                jsonObject.put("id", rs.getInt("id"));
                jsonObject.put("zaMesec", rs.getString("zaMesec"));
                jsonObject.put("account", rs.getString("account"));
                jsonObject.put("identification", rs.getString("account"));
                jsonObject.put("operater", rs.getString("operName"));
                jsonObject.put("datumZaduzenja", rs.getString("datumZaduzenja"));
                jsonObject.put("uplaceno", rs.getDouble("uplaceno"));
                jsonObject.put("datumUplate", rs.getString("datumUplate"));
                jsonObject.put("uplatio", rs.getString("uplatio"));
                jsonObject.put("paketType", "LINKED_FIX_SAOBRACAJ");


                System.out.println("JSON " + jsonObject.toString());
            } else {
                jsonObject.put("dug", 0.00);
                jsonObject.put("cena", 0.00);
                jsonObject.put("pdv", pdv);
                jsonObject.put("popust", popust);
                jsonObject.put("id", 0);
                jsonObject.put("zaMesec", zaMesec);
                jsonObject.put("account", account);
                jsonObject.put("identification", account);
                jsonObject.put("operater", "");
                jsonObject.put("uplaceno", 0.00);
                jsonObject.put("datumUplate", "1000-01-01 00:00:00.0");
                jsonObject.put("uplatio", "");
                jsonObject.put("datumZaduzenja", "0000-00-00");
                jsonObject.put("paketType", "LINKED_FIX_SAOBRACAJ");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            jsonObject.put("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return jsonObject;
    }

}
