package net.yuvideo.jgemstone.server.classes.INTERNET;

import net.yuvideo.jgemstone.server.classes.database;
import classes.md5_digiest;
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
public class NETFunctions {

    private static SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat radcheckEndDate = new SimpleDateFormat("dd MMM yyyy");
    private static SimpleDateFormat radreplyEndDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    public static Boolean check_userName_busy(String username, database db) {
        ResultSet rs;
        PreparedStatement ps;
        String query = "SELECT username FROM radcheck where username=?";
        Boolean user_Exist = false;

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, username);
            rs = ps.executeQuery();
            user_Exist = rs.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return user_Exist;
    }

    public static void addUser(JSONObject rLine, database db) {

        ResultSet rs;
        PreparedStatement ps;


        String query = "INSERT INTO radusergroup (username, groupname, priority ) VALUES " +
                "(?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, rLine.getString("groupName"));
            ps.setInt(3, 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT  INTO   radcheck (username, attribute, op, value) VALUES" +
                "(?,?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, "MD5-Password");
            ps.setString(3, ":=");
            ps.setString(4, new md5_digiest(rLine.getString("passWord")).get_hash());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT INTO radcheck (username, attribute, op, value) VALUES" +
                "(?,?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, "Simultaneous-Use");
            ps.setString(3, ":=");
            ps.setString(4, "1");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT INTO radcheck (username, attribute, op, value) VALUES " +
                "(?,?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, "Auth-Type");
            ps.setString(3, ":=");
            ps.setString(4, "Reject");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT  INTO radreply (username, attribute, op , value) VALUES" +
                "(?,?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, "WISPR-Session-Terminate-time");
            ps.setString(3, "=");
            ps.setString(4, "1970-1-1T23:59:59");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "INSERT INTO radcheck (username, attribute, op, value) VALUES" +
                "(?,?,?,?)";

        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, rLine.getString("userName"));
            ps.setString(2, "Expiration");
            ps.setString(3, ":=");
            ps.setString(4, "01 Jan 1970");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void activateNewService(JSONObject rLine, database db) {

        PreparedStatement ps;
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(normalDate.parse(rLine.getString("endDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int produzenje = rLine.getInt("produzenje");

        try {
            calendar.setTime(normalDate.parse(rLine.getString("endDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if (rLine.getString("paketType").equals("INTERNET")) {
            String query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Auth-Type'";
            try {
                ps = db.connRad.prepareStatement(query);
                if (rLine.getBoolean("aktivan")) {
                    ps.setString(1, "Accept");
                } else {
                    ps.setString(1, "Reject");
                }
                ps.setString(2, rLine.getString("idUniqueName"));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Expiration'";
            calendar.add(Calendar.MONTH, produzenje);

            try {
                ps = db.connRad.prepareStatement(query);
                String end = radcheckEndDate.format(calendar.getTime());
                ps.setString(1, radcheckEndDate.format(calendar.getTime()));
                ps.setString(2, rLine.getString("idUniqueName"));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "UPDATE radreply SET value=? WHERE username=? AND attribute='WISPR-Session-Terminate-time'";
            calendar.add(Calendar.MINUTE, -1);

            try {
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, radreplyEndDate.format(calendar.getTime()));
                ps.setString(2, rLine.getString("idUniqueName"));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //add_user_debt_first_time(rLine);

        }

    }


}
