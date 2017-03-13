package classes.BOX;

import classes.DTV.DTVFunctions;
import classes.INTERNET.NETFunctions;
import classes.SERVICES.ServicesFunctions;
import classes.database;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zoom on 2/27/17.
 */
public class addBoxService {
    public database db;
    private ResultSet rs;
    private PreparedStatement ps;
    private String query;

    private int BOX_Service_ID;
    private SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");

    public void addBox(JSONObject rLine, String opername) {


        query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, " +
                "cena, obracun, brojUgovora, aktivan, produzenje, newService, idDTVCard, username, GroupName, MAC_IPTV, FIKSNA_TEL, linkedService, BOX_service, paketType)" +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, rLine.getInt("id"));
            ps.setString(2, rLine.getString("nazivPaketa"));
            ps.setString(3, mysql_date_format.format(new Date()));
            ps.setInt(4, rLine.getInt("userID"));
            ps.setString(5, opername);
            ps.setDouble(6, rLine.getDouble("servicePopust"));
            ps.setDouble(7, rLine.getDouble("cena"));
            ps.setBoolean(8, rLine.getBoolean("obracun"));
            ps.setString(9, rLine.getString("brojUgovora"));
            ps.setBoolean(10, false);
            ps.setInt(11, rLine.getInt("produzenje"));
            ps.setBoolean(12, true);
            if (rLine.has("DTVKartica")) {
                ps.setInt(13, rLine.getInt("DTVKartica"));
            } else {
                ps.setNull(13, Types.VARCHAR);
            }
            if (rLine.has("userName")) {
                ps.setString(14, rLine.getString("userName"));
            } else {
                ps.setNull(14, Types.VARCHAR);
            }
            if (rLine.has("groupName")) {
                ps.setString(15, rLine.getString("groupName"));
            } else {
                ps.setNull(15, Types.VARCHAR);
            }
            if (rLine.has("MAC_IPTV")) {
                ps.setString(16, rLine.getString("MAC_IPTV"));
            } else {
                ps.setNull(16, Types.VARCHAR);
            }
            if (rLine.has("FIKSNA_TEL")) {
                ps.setString(17, rLine.getString("FIKSNA_TEL"));
            } else {
                ps.setNull(17, Types.VARCHAR);
            }

            ps.setBoolean(18, false);
            ps.setBoolean(19, true);
            ps.setString(20, "BOX");

            ps.executeUpdate();
            ResultSet rsBoxId = ps.getGeneratedKeys();
            rsBoxId.next();
            BOX_Service_ID = rsBoxId.getInt(1);
            if (rLine.has("groupName")) {
                add_internet(rLine, opername);
            }
            if (rLine.has("DTVKartica")) {
                add_dtv(rLine, opername);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }


    }

    public int getBOX_ID() {
        return this.BOX_Service_ID;
    }

    private void add_internet(JSONObject rLine, String opername) {
        NETFunctions.addUser(rLine, this.db);
        ServicesFunctions.addServiceNETLinked(rLine, opername, BOX_Service_ID, this.db);

    }

    private void add_dtv(JSONObject rLine, String opername) {

        DTVFunctions.addCard(rLine, opername, this.db);
        ServicesFunctions.addServiceDTVLinked(rLine, opername, BOX_Service_ID, this.db);
    }

}