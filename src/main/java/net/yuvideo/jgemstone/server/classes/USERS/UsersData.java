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

        //TODO set user data
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
