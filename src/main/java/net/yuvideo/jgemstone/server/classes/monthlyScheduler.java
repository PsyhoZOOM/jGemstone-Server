package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import net.yuvideo.jgemstone.server.classes.OBRACUNI.MesecniObracun;

/**
 * Created by zoom on 9/9/16.
 */
public class monthlyScheduler {

  private final Logger LOGGER = Logger.getLogger("MONTHLY_SCHEDULER");
  public database db;
  private DateTimeFormatter format_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private DateTimeFormatter format_month = DateTimeFormatter.ofPattern("yyyy-MM");

  public void monthlyScheduler() {
    PreparedStatement ps;
    ResultSet rs;
    String query;
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
              "INSERT INTO zaduzenja (datum, cena, pdv, popust, naziv, zaduzenOd, userID, paketType, dug, zaMesec)"
                  + "VALUES "
                  + "(?,?,?,?,?,?,?,?,?,?)";
          ps = db.conn.prepareStatement(query);
          ps.setString(1, LocalDate.now().format(format_date));
          ps.setDouble(2, rs.getDouble("cena"));
          ps.setDouble(3, rs.getDouble("PDV"));
          ps.setDouble(4, rs.getDouble("popust"));
          ps.setString(5, rs.getString("nazivPaketa"));
          ps.setString(6, "SYSTEM");
          ps.setInt(7, rs.getInt("userID"));
          ps.setString(8, rs.getString("paketType"));

          double cena = rs.getDouble("cena");
          double pdv = rs.getDouble("pdv");
          double popust = rs.getDouble("popust");
          double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
          dug = dug + valueToPercent.getPDVOfValue(dug, pdv);
          ps.setDouble(9, dug);
          ps.setString(10, date.format(format_month));

          //ako je je nov servis preskociti zaduzivanje i updejtovati newService=0, to je u slucaju
          //da je vec zaduzen (npr Delimicna cena na pola meseca..) da se ne bi duplirala zaduzenja.
          //
          if (!rs.getBoolean("newService")) {
            ps.executeUpdate();
            //ako je servis obelezen za brisanje, posto smo ga zaduzili sada cemo ga izbrisati ;)
            if (rs.getBoolean("markForDelete")) {

              deleteMarkForDeleteService(rs.getInt("id"));

            }
          } else {
            setNewService(rs.getInt("id"), false);
          }
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  private void deleteMarkForDeleteService(int id) {
    PreparedStatement ps;
    String query = "DELETE FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  private void setNewService(int id, boolean active) {
    PreparedStatement ps;
    String query = "UPDATE servicesUser SET newService=? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setBoolean(1, active);
      ps.setInt(2, id);
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


}
