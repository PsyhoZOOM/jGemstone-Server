package JGemstone.classes;

import classes.database;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
    private SimpleDateFormat format_first_day_in_month = new SimpleDateFormat("yyyy-MM-01");
    private SimpleDateFormat forma_normal_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");

    private Users users;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private database db;
    private ResultSet rs;

    private List id_service = new ArrayList();
    private Double ukupna_cena = 0.00;

    private user_debts userDebt;
    private ArrayList<user_debts> userDebts;

    private Logger LOGGER = LogManager.getLogger("MONTHLY_SCHEDULER");


    public void monthlyScheduler() {
        userDebts = new ArrayList<>();
        db = new database();
        rs = db.query_data("SELECT * FROM Services_User");

        try {
            while (rs.next()){
                LOGGER.info("INFO USER:"+rs.getString("username"));
                userDebt=new user_debts();
                userDebt.setUserName(rs.getString("username"));
                userDebt.setId(rs.getInt("id"));
                userDebt.setServiceId(rs.getInt("id_service"));
                userDebt.setDateDebt(format_first_day_in_month.format(new Date()));
                userDebts.add(userDebt);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        for(int i=0; i< userDebts.size();i++){
            LOGGER.info("userdebts: "+userDebt.getUserName());
            db.query = String.format("INSERT INTO user_debts (username, service_id, date_debt, debt, service_name) VALUES " +
                            "('%s', '%d', '%s', '%.2f', '%s')",
                    userDebts.get(i).getUserName(), userDebts.get(i).getServiceId(), userDebts.get(i).getDateDebt(),
                    get_service_price(userDebts.get(i).getServiceId()), get_service_name(userDebts.get(i).getServiceId()) );
            db.executeUpdate();
        }



        db.query = String.format("INSERT INTO scheduler (name,  value, date) VALUES ('user_debts','%s', '%s')",
                format_first_day_in_month.format(new Date()), forma_normal_date.format(new Date()));
        db.executeUpdate();
        usersArrayList.clear();
        db.closeDatabase();
    }

    private String get_service_name(int serviceId) {
        String serviceName = "N/A";
        rs  = db.query_data(String.format("SELECT naziv FROM Services WHERE id='%d'",serviceId));
        try {
            rs.next();
            serviceName = rs.getString("naziv");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceName;

    }


    private double get_service_price(int id) {
        double cena = 0.99;
         rs = db.query_data(String.format("SELECT cena FROM Services WHERE id='%d'", id));
        try {
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            cena = rs.getDouble("cena");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cena;
    }


}

