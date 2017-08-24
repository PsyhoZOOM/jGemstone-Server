package classes.FIX;

import classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 7/15/17.
 */
public class FIXFunctions {
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
        String query = "SELECT zaMesec";
        boolean exist = true;

        try {
            //ovde sam stao!
            ps = db.conn.prepareStatement(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exist;
    }

    public static void deleteService(String brojTelefona, database db) {
        PreparedStatement ps;
        String query = "DELETE FROM FIX_brojevi";
    }
}
