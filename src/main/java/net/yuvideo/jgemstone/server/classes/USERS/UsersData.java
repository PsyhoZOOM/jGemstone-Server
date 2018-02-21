package net.yuvideo.jgemstone.server.classes.USERS;

import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 2/15/18.
 */
public class UsersData {
    private database db;
    private String ERROR;
    private boolean isERROR;


    public UsersData(database db, String operName) {
        this.db = db;
    }

    public JSONObject getUserData(int userID) {
        JSONObject user = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query;
        query = "SELECT * FROM users WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                user.put("id", rs.getInt("id"));
                user.put("ime", rs.getString("ime"));
                user.put("datumRodjenja", rs.getString("datumRodjenja"));
                user.put("postBr", rs.getString("postBr"));
                user.put("adresa", rs.getString("adresa"));
                user.put("mesto", rs.getString("mesto"));
                user.put("brLk", rs.getString("brLk"));
                user.put("JMBG", rs.getString("JMBG"));
                user.put("adresaRacuna", rs.getString("adresaRacuna"));
                user.put("mestoRacuna", rs.getString("mestoRacuna"));
                user.put("komentar", rs.getString("komentar"));
                user.put("telFiksni", rs.getString("telFiksni"));
                user.put("telMobilni", rs.getString("telMobilni"));
                user.put("datumKreiranja", rs.getString("datumKreiranja"));
                user.put("operater", rs.getString("operater"));
                user.put("jMesto", rs.getString("jMesto"));
                user.put("jAdresa", rs.getString("jAdresa"));
                user.put("jAdresaBroj", rs.getString("jAdresaBroj"));
                user.put("jBroj", rs.getString("jBroj"));
                user.put("firma", rs.getBoolean("firma"));
                user.put("nazivFirme", rs.getString("nazivFirme"));
                user.put("kontaktOsoba", rs.getString("kontaktOsoba"));
                user.put("konetaktOsobaTel", rs.getString("kontaktOsobaTel"));
                user.put("kodBanke", rs.getString("kodBanke"));
                user.put("PIB", rs.getString("PIB"));
                user.put("tekuciRacun", rs.getString("tekuciRacun"));
                user.put("maticniBroj", rs.getString("maticniBroj"));
                user.put("fax", rs.getString("fax"));
                user.put("adresaFirme", rs.getString("adresaFirme"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public JSONObject getUserOprema(int userID) {
        JSONObject userOpremaArr = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query;
        query = "SELECT * FROM Artikli WHERE isUser=true AND idMagacin=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject userOprema = new JSONObject();
                    userOprema.put("id", rs.getInt("id"));
                    userOprema.put("naziv", rs.getString("naziv"));
                    userOprema.put("proizvodjac", rs.getString("proizvodjac"));
                    userOprema.put("model", rs.getString("model"));
                    userOprema.put("serijski", rs.getString("serijski"));
                    userOprema.put("pon", rs.getString("pon"));
                    userOprema.put("mac", rs.getString("mac"));
                    userOprema.put("dobavljac", rs.getString("dobavljac"));
                    userOprema.put("brDokumenta", rs.getString("brDokumenta"));
                    userOprema.put("nabavnaCena", rs.getDouble("nabavnaCena"));
                    userOprema.put("jMere", rs.getString("jMere"));
                    userOprema.put("kolicina", rs.getString("kolicina"));
                    userOprema.put("opis", rs.getString("opis"));
                    userOprema.put("datum", rs.getString("datum"));
                    userOprema.put("operName", rs.getString("operName"));
                    userOprema.put("idMagacin", rs.getInt("idMagacin"));
                    userOprema.put("isUser", rs.getBoolean("isUser"));
                    userOprema.put("uniqueID", rs.getInt("uniqueID"));
                    userOpremaArr.put(String.valueOf(i), userOprema);
                    i++;
                }
            }
        } catch (SQLException e) {
            this.setERROR(e.getMessage());
            this.setERROR(true);
            e.printStackTrace();
        }


        return userOpremaArr;
    }


    public String getERROR() {
        return ERROR;
    }

    public void setERROR(String ERROR) {
        this.ERROR = ERROR;
    }

    public boolean isERROR() {
        return isERROR;
    }

    public void setERROR(boolean ERROR) {
        isERROR = ERROR;
    }
}
