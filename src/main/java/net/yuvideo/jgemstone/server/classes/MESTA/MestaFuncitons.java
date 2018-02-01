package net.yuvideo.jgemstone.server.classes.MESTA;

import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 2/1/18.
 */
public class MestaFuncitons {
    private final database db;

    public MestaFuncitons(database db) {
        this.db = db;
    }


    public JSONObject getAllMesta() {
        JSONObject mestaObj = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM mesta";
        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject mesta = new JSONObject();
                    mesta.put("id", rs.getInt("id"));
                    mesta.put("naziv", rs.getString("naziv"));
                    mesta.put("broj", rs.getString("broj"));
                    mestaObj.put(String.valueOf(i), mesta);
                    i++;
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            mestaObj.put("ERROR", e.getMessage());
            e.printStackTrace();
        }


        return mestaObj;
    }

    public JSONObject getAllAdrese() {
        JSONObject adresaObj = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM adrese";
        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject adrese = new JSONObject();
                    adrese.put("id", rs.getInt("id"));
                    adrese.put("naziv", rs.getString("naziv"));
                    adrese.put("broj", rs.getString("broj"));
                    adrese.put("idMesta", rs.getInt("idMesta"));
                    adrese.put("brojMesta", rs.getString("brojMesta"));
                    adrese.put("nazivMesta", rs.getString("nazivMesta"));
                    adresaObj.put(String.valueOf(i), adrese);
                    i++;
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            adresaObj.put("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return adresaObj;

    }


    public String getNazivMesta(String jMesto) {
        PreparedStatement ps;
        ResultSet rs;
        String nazivMesta = "";
        String query = "SELECT naziv FROM mesta WHERE broj =?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, jMesto);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                nazivMesta = rs.getString("naziv");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nazivMesta;
    }

    public String getNazivMesta(int idMesta) {
        PreparedStatement ps;
        ResultSet rs;
        String nazivMesta = "";
        String query = "SELECT naziv FROM mesta WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, idMesta);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                nazivMesta = rs.getString("naziv");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nazivMesta;
    }

    public String getNazivAdrese(String jMesto, String jAdresa) {
        PreparedStatement ps;
        ResultSet rs;
        String naziv = "";
        String query = "SELECT naziv FROM adrese WHERE brojMesta = ? AND broj = ?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, jMesto);
            ps.setString(2, jAdresa);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                naziv = rs.getString("naziv");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return naziv;
    }


}
