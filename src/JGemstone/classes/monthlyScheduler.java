package JGemstone.classes;

import classes.database;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        query = "SELECT *  FROM ServicesUser WHERE obracun=1";

        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, paketType, cena)" +
                            "VALUES " +
                            "(?,?,?,?,?,?,?,?)";
                    psUpdateDebts = db.conn.prepareStatement(query);
                    psUpdateDebts.setInt(1, rs.getInt("id"));
                    psUpdateDebts.setInt(2, rs.getInt("id_service"));
                    psUpdateDebts.setString(3, rs.getString("nazivPaketa"));
                    psUpdateDebts.setDate(4, java.sql.Date.valueOf(format_first_day_in_month.format(new Date())));
                    psUpdateDebts.setInt(5, rs.getInt("userID"));
                    psUpdateDebts.setDouble(6, rs.getDouble("popust"));
                    psUpdateDebts.setString(7, rs.getString("paketType"));
                    psUpdateDebts.setDouble(8, rs.getDouble("cena"));
                    psUpdateDebts.executeUpdate();
                }
            } else {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}

