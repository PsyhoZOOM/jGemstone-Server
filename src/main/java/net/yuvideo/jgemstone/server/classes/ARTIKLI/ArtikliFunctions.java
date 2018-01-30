package net.yuvideo.jgemstone.server.classes.ARTIKLI;

import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

/**
 * Created by PsyhoZOOM@gmail.com on 1/30/18.
 */
public class ArtikliFunctions {
    private final database db;
    private final String operName;
    private boolean haveError = false;
    private String error = "";

    private JSONObject artikli;
    private DecimalFormat df = new DecimalFormat("0.00");

    public ArtikliFunctions(database db, String operName) {
        this.operName = operName;
        this.db = db;
    }

    public void addArtikl(JSONObject rLine) {
        PreparedStatement ps;
        String query = "INSERT INTO magacini (naziv, model, serijski, pserijski, mac, dobavljac, brDokumenta, nabavnaCena, " +
                "jMere, kolicina, opis, datum, operName) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("naziv"));
            ps.setString(2, rLine.getString("model"));
            ps.setString(3, rLine.getString("serijski"));
            ps.setString(4, rLine.getString("pserijski"));
            ps.setString(5, rLine.getString("mac"));
            ps.setString(6, rLine.getString("dobavljac"));
            ps.setString(7, rLine.getString("brDokumenta"));
            ps.setDouble(8, Double.valueOf(df.format(rLine.getDouble("nabavnaCena"))));
            ps.setString(9, rLine.getString("jMere"));
            ps.setInt(10, rLine.getInt("kolicina"));
            ps.setString(11, rLine.getString("opis"));
            ps.setString(12, LocalDateTime.now().toString());
            ps.setString(13, operName);

            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }


    }

    public void editArtikl(JSONObject rLine) {
        PreparedStatement ps;
        String query = "UPDATE magacini SET naziv=?, model=?, serijski=?, pserijski=?, mac=?, dobavljac=?, brDokumenta=?, " +
                "nabavnaCena=?, jMere=?, kolicina=?, opis=?, operName=? WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("naziv"));
            ps.setString(2, rLine.getString("model"));
            ps.setString(3, rLine.getString("serijski"));
            ps.setString(4, rLine.getString("pserijski"));
            ps.setString(5, rLine.getString("mac"));
            ps.setString(6, rLine.getString("dobavljac"));
            ps.setString(7, rLine.getString("brDokumenta"));
            ps.setDouble(8, rLine.getDouble("nabavnaCena"));
            ps.setString(9, rLine.getString("jMere"));
            ps.setDouble(10, rLine.getInt("kolicina"));
            ps.setString(11, rLine.getString("opis"));
            ps.setString(12, rLine.getString(operName));
            ps.setInt(13, rLine.getInt("id"));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

    }

    public void deleteArtikl(int id) {
        PreparedStatement ps;
        String query = "DELETE FROM magacini where id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            haveError = false;
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

    }

    public boolean isError() {
        return haveError;
    }

    public String getErrorMessage() {
        return error;
    }

    public JSONObject getArtikles() {
        return artikli;
    }

    public void searchArtikles(JSONObject rLine) {
        JSONObject jsonObject = new JSONObject();

        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM magacini WHERE naziv LIKE ? AND model LIKE ? AND serijski LIKE ? AND pserijski LIKE ? " +
                "AND mac LIKE ? AND dobavljac LIKE ? AND brDokumenta LIKE ? AND opis LIKE ?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("naziv") + "%");
            ps.setString(2, rLine.getString("model") + "%");
            ps.setString(3, rLine.getString("serijski") + "%");
            ps.setString(4, rLine.getString("pserijski") + "%");
            ps.setString(5, rLine.getString("mac") + "%");
            ps.setString(6, rLine.getString("dobavljac") + "%");
            ps.setString(7, rLine.getString("brDokumenta") + "%");
            ps.setString(8, rLine.getString("opis") + "%");
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject artObj = new JSONObject();
                    artObj.put("id", rs.getInt("id"));
                    artObj.put("naziv", rs.getString("naziv"));
                    artObj.put("model", rs.getString("model"));
                    artObj.put("serijski", rs.getString("serijski"));
                    artObj.put("pserijski", rs.getString("pserijski"));
                    artObj.put("mac", rs.getString("mac"));
                    artObj.put("dobavljac", rs.getString("dobavljac"));
                    artObj.put("brDokumenta", rs.getString("brDokumenta"));
                    artObj.put("nabavnaCena", rs.getDouble("nabavnaCena"));
                    artObj.put("jMere", rs.getString("jMere"));
                    artObj.put("kolicina", rs.getInt("kolicina"));
                    artObj.put("opis", rs.getString("opis"));
                    artObj.put("datum", rs.getString("datum"));
                    artObj.put("operName", rs.getString("operName"));
                    jsonObject.put(String.valueOf(i), artObj);
                    i++;
                }
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }


        this.artikli = jsonObject;
    }
}
