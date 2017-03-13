package classes.DTV;

import classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by zoom on 2/27/17.
 */
public class DTVFunctions {
    private static SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");

    public static Boolean check_card_busy(int cardID, database db) {
        PreparedStatement ps;
        ResultSet rs;
        Boolean cardExist = false;

        String query = "SELECT idKartica from DTVKartice where idKartica = ?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, cardID);
            rs = ps.executeQuery();
            cardExist = rs.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cardExist;

    }

    public static void addCard(JSONObject rLine, String opername, database db) {

        PreparedStatement ps;
        ResultSet rs;

        String query = "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate ) VALUES " +
                "(?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("DTVKartica"));
            ps.setInt(2, rLine.getInt("userID"));
            ps.setInt(3, rLine.getInt("DTVPaket"));
            ps.setString(4, "1970-01-01");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getPacketCriteriaGroup(int id_packet, database db) {
        PreparedStatement ps;
        ResultSet rs;
        int paketID = 0;

        String query = "SELECT idPaket FROM digitalniTVPaketi WHERE id=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id_packet);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                paketID = rs.getInt("idPaket");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("PAKET ID: " + paketID + "id_packeta: " + id_packet);
        return paketID;
    }

    public static void activateNewService(JSONObject rLine, database db) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(normalDate.parse(rLine.getString("endDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        PreparedStatement ps;
        int produzenje = rLine.getInt("produzenje");

        if (rLine.getString("paketType").equals("DTV")) {
            String query = "UPDATE DTVKartice SET endDate=? where idKartica=?";
            calendar.add(Calendar.MONTH, produzenje);

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, normalDate.format(calendar.getTime()));
                ps.setString(2, rLine.getString("idUniqueName"));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //add_user_debt_first_time(rLine);
        }
    }
}
