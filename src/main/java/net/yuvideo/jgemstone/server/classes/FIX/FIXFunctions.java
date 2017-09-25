package net.yuvideo.jgemstone.server.classes.FIX;

import net.yuvideo.jgemstone.server.classes.database;
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
        String query = "SELECT * FROM ServicesUser WHERE FIKSNA_TEL IS NOT NULL AND obracun=1";

        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    zaduziKorisnika(rs, zaMesec, operName, db);
                }
            }
            ps.close();
            obj.put("SNIMLJENO", true);
        } catch (SQLException e) {
            obj.put("Error", e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    private static void zaduziKorisnika(ResultSet rs_korisnik, String zaMesec, String operName, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT SUM(chargedAmountRSD) as charged FROM csv WHERE account=? AND connectTime LIKE ?";
        double charged = 0.00;

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rs_korisnik.getString("FIKSNA_TEL"));
            ps.setString(2, String.format("%s%%", zaMesec));
            rs = ps.executeQuery();
            System.out.println(ps.toString());
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    charged = rs.getDouble("charged");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("CHARGED: " + charged);


        query = "INSERT INTO userDebts " +
                "(id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, paketType, cena, uplaceno," +
                "dug, zaduzenOd, zaMesec)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rs_korisnik.getInt("id"));
            ps.setInt(2, rs_korisnik.getInt("id_service"));
            ps.setString(3, rs_korisnik.getString("nazivPaketa"));
            ps.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setInt(5, rs_korisnik.getInt("userID"));
            ps.setDouble(6, rs_korisnik.getDouble("popust"));
            ps.setString(7, rs_korisnik.getString("paketType"));
            Double cenaDug = rs_korisnik.getDouble("cena") + charged;
            ps.setDouble(8, Double.parseDouble(df.format(cenaDug)));
            ps.setDouble(9, 0.00);
            ps.setDouble(10, Double.parseDouble(df.format(cenaDug)));
            ps.setString(11, "SYSTEM");
            ps.setString(12, zaMesec);
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
}
