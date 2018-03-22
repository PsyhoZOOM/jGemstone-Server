package net.yuvideo.jgemstone.server.classes.OBRACUNI;

import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MesecniObracun {
    public boolean hasError = false;
    public String errorMessage = "";
    public JSONObject mesecniObracunObject;
    DecimalFormat df = new DecimalFormat("0.00");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
    DateTimeFormatter dtfNormal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String operName;


    public JSONObject getMesecniObracun(int userOd, int userDo, String datumOd, String datumDo, String operName, database db) {
        this.operName = operName;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM userDebts WHERE zaMesec >= ? AND zaMesec <= ? AND id >= ? AND id <= ?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, LocalDate.parse(datumOd, dtfNormal).format(dtf));
            ps.setString(2, LocalDate.parse(datumDo, dtfNormal).format(dtf));
            ps.setInt(3, userOd);
            ps.setInt(4, userDo);
            rs = ps.executeQuery();
            System.out.println(ps.toString());
            if (rs.isBeforeFirst()) {
                int i = 0;
                mesecniObracunObject = new JSONObject();
                while (rs.next()) {
                    JSONObject mesec = new JSONObject();
                    Double cena = rs.getDouble("cena");
                    Double popust = rs.getDouble("popust");
                    Double pdv = rs.getDouble("pdv");
                    Double cenaSaPopustom = Double.valueOf(df.format(cena - valueToPercent.getDiffValue(cena, popust)));
                    Double pdvCena = Double.valueOf(df.format(valueToPercent.getValueOfPercentAdd(cenaSaPopustom, pdv)));
                    UsersData user = new UsersData(db, operName);
                    JSONObject userObj = user.getUserData(rs.getInt("userID"));
                    String imePrezime = userObj.getString("ime");
                    String jBroj = userObj.getString("jBroj");

                    mesec.put("id", rs.getInt("id"));
                    mesec.put("imePrezime", imePrezime);
                    mesec.put("jBroj", jBroj);
                    mesec.put("cena", (Double.valueOf(df.format(cenaSaPopustom))));
                    mesec.put("popust", Double.valueOf(df.format(popust)));
                    mesec.put("pdv", Double.valueOf(df.format(pdv)));
                    mesec.put("pdvCena", Double.valueOf(df.format(pdvCena)));
                    mesec.put("ukupno", Double.valueOf(df.format(cenaSaPopustom + pdvCena)));
                    mesecniObracunObject.put(String.valueOf(i), mesec);
                    i++;
                }
            }

        } catch (SQLException e) {
            hasError = true;
            errorMessage = e.getMessage();
            e.printStackTrace();
        }

        return mesecniObracunObject;
    }
}