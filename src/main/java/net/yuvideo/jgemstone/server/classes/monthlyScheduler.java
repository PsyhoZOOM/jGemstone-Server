package net.yuvideo.jgemstone.server.classes;

import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by zoom on 9/9/16.
 */
public class monthlyScheduler {
    public database db;
    private SimpleDateFormat format_first_day_in_month = new SimpleDateFormat("yyyy-MM-01");
    private SimpleDateFormat forma_normal_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat format_month = new SimpleDateFormat("yyyy-MM");
    private Users users;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private PreparedStatement ps;
    private PreparedStatement psUpdateDebts;
    private ResultSet rs;
    private ResultSet rsUpdateDebts;

    private List id_service = new ArrayList();
    private Double ukupna_cena = 0.00;

    private user_debts userDebt;
    private ArrayList<user_debts> userDebts;

    private Logger LOGGER = LogManager.getLogger("MONTHLY_SCHEDULER");

    private String query;

    public void monthlyScheduler() {
        query = "SELECT *  FROM ServicesUser WHERE obracun=1 AND aktivan=1 AND linkedService=0  ";
        //koji je mesec zaduzenja. posto je sada novi mesec kada se zaduzuje korisnik onda idemo mesec dana u nazad.
        //obracun je za prosli mesec

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    query = "INSERT INTO userDebts (id_ServiceUser, nazivPaketa, datumZaduzenja, userID, popust, paketType, cena, dug, zaMesec)" +
                            "VALUES " +
                            "(?,?,?,?,?,?,?,?,?)";
                    psUpdateDebts = db.conn.prepareStatement(query);
                    psUpdateDebts.setInt(1, rs.getInt("id"));
                    psUpdateDebts.setString(2, rs.getString("nazivPaketa"));
                    psUpdateDebts.setDate(3, java.sql.Date.valueOf(format_first_day_in_month.format(new Date())));
                    psUpdateDebts.setInt(4, rs.getInt("userID"));
                    psUpdateDebts.setDouble(5, rs.getDouble("popust"));
                    psUpdateDebts.setString(6, rs.getString("paketType"));
                    psUpdateDebts.setDouble(7, rs.getDouble("cena"));
                    psUpdateDebts.setDouble(8, valueToPercent.getValue(rs.getDouble("cena"), rs.getDouble("popust")));
                    psUpdateDebts.setString(9, format_month.format(cal.getTime()));
                    if (rs.getInt("newService") != 1) {
                        //ako servis je vec zaduzen onda preskociti zaduzenje od strane servera :)
                        if (!check_skip_userDebt(rs.getInt("id"), rs.getInt("userID"), format_month.format(cal.getTime())))
                            psUpdateDebts.executeUpdate();
                    } else {
                        setOldService(rs.getInt("id"));
                    }
                }
            } else {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void setOldService(int id) {
        query = "UPDATE ServicesUser set newService=0 WHERE id=?";
        try {
            PreparedStatement ps2 = db.conn.prepareStatement(query);
            ps2.setInt(1, id);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean check_skip_userDebt(int id_service, int userID, String zaMesec) {
        PreparedStatement psCheck;
        ResultSet rsCheck;
        Boolean check = false;

        String queryCheck = "SELECT * FROM userDebts WHERE id_ServiceUser=? AND userID=? and zaMesec=?";
        try {
            psCheck = db.conn.prepareStatement(queryCheck);
            psCheck.setInt(1, id_service);
            psCheck.setInt(2, userID);
            psCheck.setString(3, zaMesec);
            check = rs.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return check;
    }


}

