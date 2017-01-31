package JGemstone.classes;

import classes.database;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
    private ResultSet rs;

    private List id_service = new ArrayList();
    private Double ukupna_cena = 0.00;

    private user_debts userDebt;
    private ArrayList<user_debts> userDebts;

    private Logger LOGGER = LogManager.getLogger("MONTHLY_SCHEDULER");

    private String query;

    public void monthlyScheduler() {
        userDebts = new ArrayList<>();

        query = "SELECT * FROM ServicesUser";
        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            while (rs.next()){
                userDebt=new user_debts();
                userDebt.setUserID(rs.getInt("userID"));
                userDebt.setId(rs.getInt("id"));
                userDebt.setServiceId(rs.getInt("id_service"));
                userDebt.setDateDebt(format_first_day_in_month.format(new Date()));
                userDebt.setPopust(rs.getDouble("popust"));
                userDebts.add(userDebt);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        for(int i=0; i< userDebts.size();i++){

            query = "INSERT INTO user_debts (userID, service_id, date_debt, debt, service_name, popust, debtTotal) VALUES " +
                    "(?,?,?,?,?,?,?)";


            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, userDebts.get(i).getUserID());
                db.ps.setInt(2, userDebts.get(i).getServiceId());
                db.ps.setString(3, userDebts.get(i).getDateDebt());
                db.ps.setDouble(4, get_service_price(userDebts.get(i).getServiceId()));
                db.ps.setString(5, get_service_name(userDebts.get(i).getServiceId()));
                db.ps.setDouble(6, userDebts.get(i).getPopust());
                float userDebt = (float) get_service_price(userDebts.get(i).getServiceId());
                float popust = (float) userDebts.get(i).getPopust();
                double totalDebt = 0.00;
                totalDebt = popust * userDebt / 100;
                totalDebt = userDebt - totalDebt;
                if (Double.isNaN(totalDebt) || Double.isInfinite(totalDebt)) {
                    totalDebt = 0.00;
                }
                db.ps.setDouble(7, totalDebt);
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }


        query = "INSERT INTO scheduler (name, value, date) VALUES (?, ? ,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, "user_debts");
            ps.setString(2, format_first_day_in_month.format(new Date()));
            ps.setString(3, forma_normal_date.format(new Date()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        usersArrayList.clear();
    }

    private String get_service_name(int serviceId) {
        String serviceName = "GRESKA";
        query = "SELE CT naziv FROM Services WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, serviceId);
            rs = ps.executeQuery();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            rs.next();
            serviceName = rs.getString("naziv");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceName;

    }


    private double get_service_price(int id) {
        query = "SELECT cena FROM Services WHERE id=?";
        Double cena;
        DecimalFormat df = new DecimalFormat("###,###,##0,00");
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (rs.isBeforeFirst())
                rs.next();
            cena = rs.getDouble("cena");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0.00;
        }

        return Double.parseDouble(df.format(cena));
    }


}

