package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by zoom on 9/9/16.
 */
public class monthlyScheduler {

  private final Logger LOGGER = Logger.getLogger("MONTHLY_SCHEDULER");
  public database db;
  private SimpleDateFormat format_first_day_in_month = new SimpleDateFormat("yyyy-MM-01");
  private SimpleDateFormat forma_normal_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
  private DateTimeFormatter format_month = DateTimeFormatter.ofPattern("yyyy-MM");
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
  private String query;

  public void monthlyScheduler() {
    int userID = 0;
    query = "SELECT *  FROM servicesUser WHERE obracun=1 AND aktivan=1 AND linkedService=0  ";
    //koji je mesec zaduzenja. posto je sada novi mesec kada se zaduzuje korisnik onda idemo mesec dana u nazad.
    //obracun je za prosli mesec (-1 mesec)

    LocalDate date = LocalDate.now();
    date = date.minusMonths(1);
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          query =
              "INSERT INTO userDebts (id_ServiceUser, nazivPaketa, datumZaduzenja, userID, popust, paketType, cena, dug, zaMesec, PDV)"
                  + "VALUES "
                  + "(?,?,?,?,?,?,?,?,?,?)";
          psUpdateDebts = db.conn.prepareStatement(query);
          psUpdateDebts.setInt(1, rs.getInt("id"));
          psUpdateDebts.setString(2, rs.getString("nazivPaketa"));
          psUpdateDebts
              .setDate(3, java.sql.Date.valueOf(format_first_day_in_month.format(new Date())));
          psUpdateDebts.setInt(4, rs.getInt("userID"));
          psUpdateDebts.setDouble(5, rs.getDouble("popust"));
          psUpdateDebts.setString(6, rs.getString("paketType"));
          double cena = rs.getDouble("cena");
          double pdv = rs.getDouble("pdv");
          double popust = rs.getDouble("popust");
          double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
          dug = dug + valueToPercent.getPDVOfValue(dug, pdv);
          psUpdateDebts.setDouble(7, cena);
          //cena+pdv-popust=dug
          psUpdateDebts.setDouble(8, dug);
          psUpdateDebts.setString(9, date.format(format_month));
          psUpdateDebts.setDouble(10, rs.getDouble("pdv"));
          if (!rs.getBoolean("newService")) {
            //ako servis je vec zaduzen onda preskociti zaduzenje od strane servera :)
            if (!check_skip_userDebt(rs.getInt("id"), rs.getInt("userID"),
                date.format(format_month))) {
              psUpdateDebts.executeUpdate();
            }
          } else {
            setOldService(rs.getInt("id"));
          }
          userID = rs.getInt("userID");
        }
      }
      rs.close();
      if (psUpdateDebts != null) {
        psUpdateDebts.close();
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  private void setOldService(int id) {
    query = "UPDATE servicesUser set newService=0 WHERE id=?";
    try {
      PreparedStatement ps2 = db.conn.prepareStatement(query);
      ps2.setInt(1, id);
      ps2.executeUpdate();
      ps2.close();
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
      rsCheck = psCheck.executeQuery();
      check = rsCheck.isBeforeFirst();
      psCheck.close();
      rsCheck.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return check;
  }

}
