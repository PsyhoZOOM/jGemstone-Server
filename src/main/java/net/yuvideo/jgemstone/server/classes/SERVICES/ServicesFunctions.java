package net.yuvideo.jgemstone.server.classes.SERVICES;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import net.yuvideo.jgemstone.server.classes.DTV.DTVFunctions;
import net.yuvideo.jgemstone.server.classes.FIX.FIXFunctions;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.StalkerRestAPI2;
import net.yuvideo.jgemstone.server.classes.RACUNI.Uplate;
import net.yuvideo.jgemstone.server.classes.RADIUS.Radius;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.json.JSONObject;

/**
 * Created by zoom on 2/27/17.
 */
public class ServicesFunctions {

  private String operName;
  private database db;

  private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
  private DateTimeFormatter dtfNormalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private DateTimeFormatter dtfRadCheck = DateTimeFormatter.ofPattern("dd MMM yyyy");
  private DateTimeFormatter dtfRadReply = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private DateTimeFormatter dtfMesecZaduzenja = DateTimeFormatter.ofPattern("yyyy-MM");
  private DateTimeFormatter dtfIPTV = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private boolean error;
  private String errorMSG;


  public ServicesFunctions(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }

  public void addServiceDTVLinked(JSONObject rLine, String opername, int BOX_Service_ID) {
    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added,  idDTVCard,"
            + "  userID, operName, linkedService, paketType, endDate, PDV, opis,"
            + " brojUgovora, newService) "
            + "VALUES"
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("DTV_service_ID"));
      ps.setInt(2, BOX_Service_ID);
      ps.setString(3, rLine.getString("nazivPaketaDTV"));
      ps.setString(4, dtf.format(LocalDateTime.now()));
      ps.setString(5, String.valueOf(rLine.getInt("cardID")));
      ps.setInt(6, rLine.getInt("userID"));
      ps.setString(7, opername);
      ps.setBoolean(8, true);
      ps.setString(9, "LINKED_DTV");
      ps.setString(10, "2000-01-01");
      ps.setDouble(11, rLine.getDouble("pdv"));
      ps.setString(12, rLine.getString("komentar"));
      ps.setString(13, rLine.getString("brojUgovora"));
      ps.setBoolean(14, false);
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void addServiceNETLinked(JSONObject rLine, String opername, int BOX_Service_ID) {
    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added, userID, "
            + " operName, UserName, GroupName, linkedService, paketType, endDate, PDV, opis, "
            + "brojUgovora, newService) "
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("NET_service_ID"));
      ps.setInt(2, BOX_Service_ID);
      ps.setString(3, rLine.getString("nazivPaketaNET"));
      ps.setString(4, dtf.format(LocalDateTime.now()));
      ps.setInt(5, rLine.getInt("userID"));
      ps.setString(6, opername);
      ps.setString(7, rLine.getString("userName").toLowerCase());
      ps.setString(8, rLine.getString("groupName"));
      ps.setBoolean(9, true);
      ps.setString(10, "LINKED_NET");
      ps.setString(11, "2000-01-01");
      ps.setDouble(12, rLine.getDouble("pdv"));
      ps.setString(13, rLine.getString("komentar"));
      ps.setString(14, rLine.getString("brojUgovora"));
      ps.setBoolean(15, false);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void addServiceFIXLinked(JSONObject rLine, int box_service_id) {
    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, box_id, endDate, nazivPaketa, date_added, userID, operName,"
            + " FIKSNA_TEL, FIKSNA_TEL_PAKET_ID, linkedService, paketType, PDV, opis, brojUgovora, newService)"
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("FIKSNA_service_ID"));
      ps.setInt(2, box_service_id);
      ps.setString(3, "2000-01-01");
      ps.setString(4, rLine.getString("nazivPaketaFIKSNA"));
      ps.setString(5, dtf.format(LocalDateTime.now()));
      ps.setInt(6, rLine.getInt("userID"));
      ps.setString(7, getOperName());
      ps.setString(8, rLine.getString("FIX_TEL"));
      ps.setInt(9, rLine.getInt("FIKSNA_PAKET_ID"));
      ps.setBoolean(10, true);
      ps.setString(11, "LINKED_FIX");
      ps.setDouble(12, rLine.getDouble("pdv"));
      ps.setString(13, rLine.getString("komentar"));
      ps.setString(14, rLine.getString("brojUgovora"));
      ps.setBoolean(15, false);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addServiceIPTVLinked(JSONObject rLIne, String opername, int box_service_id) {
    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added, userID,  opername,"
            + "IPTV_MAC, linkedService, paketType, endDate, PDV, opis, brojUgovora, newService) "
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLIne.getInt("IPTV_Service_ID"));
      ps.setInt(2, box_service_id);
      ps.setString(3, rLIne.getString("tariff_plan"));
      ps.setString(4, dtf.format(LocalDateTime.now()));
      ps.setInt(5, rLIne.getInt("userID"));
      ps.setString(6, opername);
      ps.setString(7, rLIne.getString("STB_MAC"));
      ps.setBoolean(8, true);
      ps.setString(9, "LINKED_IPTV");
      ps.setString(10, "2000-01-01");
      ps.setDouble(11, rLIne.getDouble("pdv"));
      ps.setString(12, rLIne.getString("komentar"));
      ps.setString(13, rLIne.getString("brojUgovora"));
      ps.setBoolean(14, false);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void addServiceDTV(int id_service, String nazivPaketa, int userID,
      String opername, double popust, double cena, Boolean obracun,
      String brojUgovora, String idDTVCard, int paketID, double pdv, String opis) {

    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena,"
            + " obracun, brojUgovora, newService, idDTVCard, linkedService, paketType, endDate, PDV, opis)"
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_service);
      ps.setString(2, nazivPaketa);
      ps.setString(3, LocalDateTime.now().format(dtf));
      ps.setInt(4, userID);
      ps.setString(5, opername);
      ps.setDouble(6, popust);
      ps.setDouble(7, cena);
      ps.setBoolean(8, obracun);
      ps.setString(9, brojUgovora);
      ps.setBoolean(10, true);
      ps.setString(11, idDTVCard);
      ps.setBoolean(12, false);
      ps.setString(13, "DTV");
      ps.setString(14, "2000-01-01");
      ps.setDouble(15, pdv);
      ps.setString(16, opis);
      ps.executeUpdate();
      ps.close();


    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    if (idDTVCard.equals("0")) {
    }
    query = "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate, createDate) VALUES(?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, Integer.valueOf(idDTVCard));
      ps.setInt(2, userID);
      ps.setInt(3, paketID);
      ps.setString(4,
          LocalDate.parse("2000-01-01").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.setString(5, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }

  public void addServiceNET(JSONObject rLine) {
    NETFunctions netFunctions = new NETFunctions(db, getOperName());
    if (netFunctions.check_userName_busy(rLine.getString("userName"))) {
      setErrorMSG("KORISNIK POSTOJI");
      setError(true);
    }

    netFunctions.addUser(rLine);

    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, "
            + "obracun, brojUgovora, aktivan,  newService, UserName, GroupName, paketType, endDate, PDV, opis) VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("id"));
      ps.setString(2, rLine.getString("nazivPaketa"));
      ps.setString(3, dtf.format(LocalDateTime.now()));
      ps.setInt(4, rLine.getInt("userID"));
      ps.setString(5, getOperName());
      ps.setDouble(6, rLine.getDouble("servicePopust"));
      ps.setDouble(7, rLine.getDouble("cena"));
      ps.setBoolean(8, rLine.getBoolean("obracun"));
      ps.setString(9, rLine.getString("brojUgovora"));
      ps.setBoolean(10, false);
      ps.setBoolean(11, true);
      ps.setString(12, rLine.getString("userName").toLowerCase());
      ps.setString(13, rLine.getString("groupName"));
      ps.setString(14, "NET");
      ps.setString(15, "2000-01-01");
      ps.setDouble(16, rLine.getDouble("pdv"));
      ps.setString(17, rLine.getString("komentar"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }

  public void addServiceFIX(JSONObject rLine) {

    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, endDate, userID, operName, popust, cena, "
            + "obracun, brojUgovora, aktivan,  newService, FIKSNA_TEL, paketType, PDV, opis) "
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("id"));
      ps.setString(2, rLine.getString("nazivPaketa"));
      ps.setString(3, dtf.format(LocalDateTime.now()));
      ps.setString(4, "2000-01-01");
      ps.setInt(5, rLine.getInt("userID"));
      ps.setString(6, getOperName());
      ps.setDouble(7, rLine.getDouble("popust"));
      ps.setDouble(8, rLine.getDouble("cena"));
      ps.setBoolean(9, rLine.getBoolean("obracun"));
      ps.setString(10, rLine.getString("brojUgovora"));
      ps.setBoolean(11, false);
      ps.setBoolean(12, true);
      ps.setString(13, rLine.getString("brojTel"));
      ps.setString(14, "FIX");
      ps.setDouble(15, rLine.getDouble("pdv"));
      ps.setString(16, rLine.getString("komentar"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
    JSONObject object = new JSONObject();
    object.put("FIX_TEL", rLine.getString("brojTel"));
    object.put("userID", rLine.getInt("userID"));
    fixFunctions.addBroj(object);
  }

  public void addServiceIPTV(JSONObject rLine) {
    PreparedStatement ps;
    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena,"
            + "obracun, brojUgovora, aktivan,  newService,  IPTV_MAC, paketType, endDate, PDV, opis)"
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("id"));
      ps.setString(2, rLine.getString("nazivPaketa"));
      ps.setString(3, dtf.format(LocalDateTime.now()));
      ps.setInt(4, rLine.getInt("userID"));
      ps.setString(5, getOperName());
      ps.setDouble(6, rLine.getDouble("popust"));
      ps.setDouble(7, rLine.getDouble("cena"));
      ps.setBoolean(8, rLine.getBoolean("obracun"));
      ps.setString(9, rLine.getString("brojUgovora"));
      ps.setBoolean(10, false);
      ps.setBoolean(11, true);
      ps.setString(12, rLine.getString("STB_MAC"));
      ps.setString(13, "IPTV");
      ps.setString(14, "2000-01-01");
      ps.setDouble(15, rLine.getDouble("pdv"));
      ps.setString(16, rLine.getString("komentar"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public int addServiceOstalo(JSONObject rLine) {
    PreparedStatement ps;
    ResultSet rs;
    int lastKEY = 0;
    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, "
            +
            "obracun, aktivan, newService, paketType, PDV, endDate, brojUgovora, opis, markForDelete)"
            +
            "VALUES " +
            "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, rLine.getInt("id"));
      ps.setString(2, rLine.getString("naziv"));
      ps.setString(3, LocalDateTime.now().format(dtf));
      ps.setInt(4, rLine.getInt("userID"));
      ps.setString(5, getOperName());
      ps.setDouble(6, rLine.getDouble("popust"));
      ps.setDouble(7, rLine.getDouble("cena"));
      ps.setBoolean(8, rLine.getBoolean("obracun"));
      ps.setBoolean(9, false);
      ps.setBoolean(10, false);
      ps.setString(11, rLine.getString("paketType"));
      ps.setDouble(12, rLine.getDouble("pdv"));
      if (rLine.has("endDate")) {
        ps.setString(13, rLine.getString("endDate"));
      } else {
        ps.setString(13, "2000-01-01");
      }
      ps.setString(14, rLine.getString("brojUgovora"));
      ps.setString(15, rLine.getString("komentar"));
      ps.setBoolean(16, rLine.getBoolean("markForDelete"));
      ps.executeUpdate();

      rs = ps.getGeneratedKeys();
      if (rs.isBeforeFirst()) {
        rs.next();
        lastKEY = rs.getInt(1);
      }
      ps.close();
      rs.close();


    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return lastKEY;
  }


  /**
   * removing all DTV services with associated DTVCards and DTV Addons
   */
  private void deleteServiceDTV(int serviceID, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query;

    //pulling all cards from database and passing to dtvfunction to delete card from DTVKartice database;
    query = "SELECT idDTVCard FROM servicesUser WHERE id=? and userID=? or dtv_main=? AND userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.setInt(2, userID);
      ps.setInt(3, serviceID);
      ps.setInt(4, userID);
      rs = ps.executeQuery();
      if(rs.isBeforeFirst()) {
        DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
        while (rs.next()) {
          dtvFunctions.deleteCard(Integer.parseInt(rs.getString("idDTVCard")));
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    //deleting all services where userID |  and DTVid is service id
    query = "DELETE FROM servicesUser WHERE id =?  and userID=? or dtv_main=? AND userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.setInt(2, userID);
      ps.setInt(3, serviceID);
      ps.setInt(4, userID);
      ps.executeUpdate();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }

  private void deleteServiceNET(int serviceID) {
    PreparedStatement ps;
    PreparedStatement psDelete;
    ResultSet rs;

    String userName;

    String query = "SELECT * FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        userName = rs.getString("UserName");
        query = "DELETE FROM radusergroup WHERE username=? ";
        psDelete = db.connRad.prepareStatement(query);
        psDelete.setString(1, userName);
        psDelete.executeUpdate();
        psDelete.close();

        query = "DELETE from radreply WHERE username=?";
        psDelete = db.connRad.prepareStatement(query);
        psDelete.setString(1, userName);
        psDelete.executeUpdate();
        psDelete.close();

        query = "DELETE from radcheck WHERE username=?";
        psDelete = db.connRad.prepareStatement(query);
        psDelete.setString(1, userName);
        psDelete.executeUpdate();
        psDelete.close();

        query = "DELETE FROM servicesUser WHERE id=?";
        psDelete = db.conn.prepareStatement(query);
        psDelete.setInt(1, serviceID);
        psDelete.executeUpdate();
        psDelete.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void deleteServiceIPTV(int serviceID) {
    StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
    if (!stalkerRestAPI2.isHostAlive()) {
      setError(true);
      setErrorMSG(stalkerRestAPI2.getHostMessage());
      return;
    }

    PreparedStatement ps;
    ResultSet rs;
    String query;
    String mac = null;
    int userID = 0;

    query = "SELECT * FROM servicesUser WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        mac = rs.getString("IPTV_MAC");
        userID = rs.getInt("userID");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    //brisanje IPTV tarife RESTAPIjem
    stalkerRestAPI2.deleteAccount(mac);

    //brisanje u baziu
    query = "DELETE FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  private void deleteServiceFIX(int serviceID) {
    PreparedStatement ps;
    ResultSet rs;
    String query;
    String brojTelefona = new String();
    query = "SELECT * FROM servicesUser WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        brojTelefona = rs.getString("FIKSNA_TEL");
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    query = "DELETE FROM servicesUser WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.executeUpdate();
      ps.close();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      fixFunctions.deleteService(brojTelefona);
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  private void deleteServiceBOX(int boxServiceID, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE   box_id =? and userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, boxServiceID);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          if (rs.getString("paketType").contains("IPTV")) {
            deleteServiceIPTV(rs.getInt("id"));
            if (isError()) {
              return;
            }
          }
          if (rs.getString("paketType").contains("DTV")) {
            deleteServiceDTV_BOX(boxServiceID, userID);
          }
          if (rs.getString("paketType").contains("NET")) {
            deleteServiceNET(rs.getInt("id"));
          }
          if (rs.getString("paketType").contains("FIX")) {
            deleteServiceFIX(rs.getInt("id"));
          }
        }
      }
      ps.close();
      query = "DELETE FROM servicesUser where id=?";
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, boxServiceID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  private void deleteServiceDTV_BOX(int boxServiceID, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE box_id=? and userID=? and paketType LIKE '%DTV%' or dtv_main=? and userID=? AND paketType LIKE '%DTV%'";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, boxServiceID);
      ps.setInt(2, userID);
      ps.setInt(3, boxServiceID);
      ps.setInt(4, userID);
      rs = ps.executeQuery();
      if(rs.isBeforeFirst()) {
        DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
        while (rs.next()) {
          dtvFunctions.deleteCard(Integer.parseInt(rs.getString("idDTVCard")));
        }
      }
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    query = "DELETE FROM servicesUser WHERE box_id=? and userID=? and paketType LIKE '%DTV%' or dtv_main=? and userID=? and paketType LIKE '%DTV%'";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, boxServiceID);
      ps.setInt(2, userID);
      ps.setInt(3, boxServiceID);
      ps.setInt(4, userID);
      ps.executeUpdate();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }


  public String getDatumIsteka(int serviceID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT endDate FROM servicesUser WHERE id=?";
    String datumIsteka = null;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        datumIsteka = rs.getString("endDate");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return datumIsteka;
  }

  public void addService(JSONObject rLine, String operName, database db) {
    PreparedStatement ps;
    String cal = LocalDateTime.now().format(dtfNormalDate);
    LocalDate calZaMesec = null;

    calZaMesec = LocalDate.parse(rLine.getString("zaMesec") + "-01");

    String query =
        "INSERT INTO userDebts (id_ServiceUser,  nazivPaketa, datumZaduzenja, userID, popust, "
            + "paketType, cena, uplaceno,  dug,  zaduzenOd, zaMesec, PDV) VALUES"
            + "(?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("id_ServiceUser"));
      ps.setString(2, rLine.getString("nazivPaketa"));
      ps.setString(3, cal);
      ps.setInt(4, rLine.getInt("userID"));
      ps.setDouble(5, rLine.getDouble("popust"));
      ps.setString(6, rLine.getString("paketType"));
      double cena = rLine.getDouble("cena");
      double pdv = rLine.getDouble("pdv");
      double popust = rLine.getDouble("popust");
      double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
      dug = dug + valueToPercent.getPDVOfValue(dug, pdv);
      ps.setDouble(7, cena);
      ps.setDouble(8, 0.00);
      ps.setDouble(9, dug);
      ps.setString(10, operName);
      ps.setString(11, calZaMesec.format(dtfMesecZaduzenja));
      ps.setDouble(12, rLine.getDouble("pdv"));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  public Boolean check_service_exist(int id_ServiceUser, int userID, String zaMesec,
      database db) {
    PreparedStatement ps;
    ResultSet rs;
    boolean serviceExist = false;
    String query = "SELECT * from userDebts WHERE id_ServiceUser=? AND userID=? AND zaMesec=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_ServiceUser);
      ps.setInt(2, userID);
      ps.setString(3, zaMesec);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        serviceExist = true;
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return serviceExist;

  }

  /**
   * <b>Aktiviranje novog servisa</b> <p>Aktiviranje novog servisa zavisi od produzivanje serviser
   * (prekoracenje meseci), zaduzivanje usluga (cena do kraja meseca), update newService=0 i
   * aktivated=1</p>
   *
   * @param serviceID id servisa
   * @param userID userID (da bi dobili int prekoracenja)
   * @param operName (operater koji aktivira nov servis)
   */
  public void activateNewService(int serviceID, int userID, String operName) {
    PreparedStatement ps;
    String query;

    UsersData usersData = new UsersData(db, getOperName());
    JSONObject userData = usersData.getUserData(userID);
    int prekoracenje = userData.getInt("prekoracenje");

    String endDate = LocalDate.now().plusMonths(prekoracenje).withDayOfMonth(1)
        .format(dtfNormalDate);
    activateService(serviceID, endDate, operName);



    query = "UPDATE servicesUser set aktivan=1, date_activated=? WHERE id=? or box_id=? or dtv_main=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDateTime.now().format(dtf));
      ps.setInt(2, serviceID);
      ps.setInt(3, serviceID);
      ps.setInt(4, serviceID);

      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    query = "UPDATE users set endDate=? where id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, endDate);
      ps.setInt(2, userID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    Uplate uplate = new Uplate(getOperName(), db);
    uplate.produzivanjeServisa(userID);

  }


  public void activateBoxService(int serviceID, String endDate, String operName, boolean obracun) {

    PreparedStatement ps;
    ResultSet rs = null;
    String query;

    query = "SELECT * FROM servicesUser WHERE box_id=? OR dtv_main=? AND linkedService NOT LIKE 'LINKED%'";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.setInt(2, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          int service = rs.getInt("id");
          activateService(service, endDate, operName);
          setAktivateService(serviceID, true);
          //fix for zaduzenje addon programs
          if (rs.getString("paketType").contains("DTV_ADDON")) {
            if (rs.getBoolean("obracun"))
            zaduziKorisnika(rs.getInt("id"));
          }
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    if (obracun)
    zaduziKorisnika(serviceID);


  }

  public void activateService(int serviceID, String endDate, String operName) {
    PreparedStatement ps;
    ResultSet rs = null;
    String query = "SELECT * FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        boolean obracun = rs.getBoolean("obracun");

        if (rs.getString("paketType").equals("DTV")) {
          DTVFunctions dtvFunctions = new DTVFunctions(db, operName);
          dtvFunctions.activateDTV(serviceID, endDate, rs.getInt("userID"));
          if (dtvFunctions.isError()) {
            setErrorMSG(dtvFunctions.getErrorMSG());
            setError(true);
          }
        }

        if (rs.getString("paketType").equals("IPTV") || rs.getString("paketType")
            .equals("LINKED_IPTV")) {
          StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
          if (stalkerRestAPI2.isError()) {
            setErrorMSG(stalkerRestAPI2.getErrorMSG());
            setError(true);
            return;
          }
          stalkerRestAPI2.activateStatus(true, rs.getString("IPTV_MAC"));
          stalkerRestAPI2.setEndDate(rs.getString("IPTV_MAC"), endDate);
          setAktivateService(serviceID, true);
          setEndDate(serviceID, endDate);

          if (obracun)
          zaduziKorisnika(serviceID);
        }

        if(rs.getString("paketType").equals("FIX")) {
          setAktivateService(serviceID, true);
          setEndDate(serviceID, endDate);

          if (obracun)
          zaduziKorisnika(serviceID);
        }
        if (rs.getString("paketType").equals("NET")) {
          Radius radius = new Radius(db, operName);
          radius.activateUser(rs.getString("UserName"));
          radius.setEndDate(rs.getString("UserName"), endDate);
          setAktivateService(serviceID, true);
          setEndDate(serviceID, endDate);
          if (obracun)
          zaduziKorisnika(serviceID);
          if (radius.isError()) {
            setErrorMSG(radius.getErrorMSG());
            setError(true);
          }
        }

        if(rs.getString("paketType").equals("OSTALE_USLUGE")) {
          setAktivateService(serviceID, true);
          setEndDate(serviceID, endDate);

          if (obracun)
          zaduziKorisnika(serviceID);
        }

        if (rs.getString("paketType").equals("BOX")) {
          activateBoxService(serviceID, endDate, operName, obracun);
        }


      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
      return;
    }


    setEndDate(serviceID, endDate);
  }


  public void zaduziKorisnika(int serviceID) {
    LocalDate date = LocalDate.now();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE id=?";
    String zaMesec = LocalDate.now().format(dtfMesecZaduzenja);
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      rs.next();

      String opis="";
      int daysInMonth = 0;
      int daysToEndMonth = 0;
      Double cenaService = rs.getDouble("cena");
      Double cenaServiceOrig = cenaService;
      Double pdv = rs.getDouble("PDV");
      Double popust = rs.getDouble("popust");
      Double zaUplatu = 0.00;
      Double cenaZaDan = 0.00;
      Double cenaZaDaOrig = 0.00;
      cenaService = cenaService - valueToPercent.getPDVOfSum(cenaService, popust);
      cenaService = cenaService + valueToPercent.getPDVOfValue(cenaService, pdv);

      if (rs.getBoolean("newService")) {
        opis = "Delimicna cena";
        daysInMonth = date.getMonth().length(LocalDate.now().isLeapYear());
        daysToEndMonth = daysInMonth - date.getDayOfMonth();
        cenaZaDan = cenaService / daysInMonth;
        cenaZaDaOrig = cenaServiceOrig / daysInMonth;
        zaUplatu = cenaZaDan * daysToEndMonth;
        cenaServiceOrig = cenaZaDaOrig * daysToEndMonth;
      } else {
        zaUplatu = cenaService;
      }

      query = "INSERT INTO zaduzenja "
          + "(naziv, datum, "
          + "userID, paketType, cena, dug, popust, zaduzenOd, pdv, opis, zaMesec) "
          + "VALUES "
          + "(?,?,?,?,?,?,?,?,?,?,?)";

      ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, rs.getString("nazivPaketa"));
      ps.setString(2, LocalDate.now().toString());
      ps.setInt(3, rs.getInt("userID"));
      ps.setString(4, rs.getString("paketType"));
      ps.setDouble(5, cenaServiceOrig);
      ps.setDouble(6, zaUplatu);
      ps.setDouble(7, rs.getDouble("popust"));
      ps.setString(8, operName);
      ps.setDouble(9, rs.getDouble("PDV"));
      ps.setString(10, opis);
      ps.setString(11, zaMesec);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }


  public void zaduziKorisnikaBeginOfMonth(int serviceID, int userID) {
    ServiceData serviceData = getServiceData(serviceID);
    //ako je nov servis samo cemog izbrisati necemo zaduzivati nista
    if (!serviceData.isAktivan())
      return;
    if (serviceData.getPaketType().equals("BOX")) {
      razduziBOX(serviceData.getId(), userID);
    }else if (serviceData.getPaketType().equals("DTV")) {
      razduziDTV(serviceData.getId(), userID);
    }else{
      razduziService(serviceData.getId(), userID);
    }
  }

  private void razduziService(int serviceID, int userID){
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE id=? AND userID=?";
    DateTimeFormatter format_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter format_motn = DateTimeFormatter.ofPattern("yyyy-MM");
    DecimalFormat df = new DecimalFormat("#.00");
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1,serviceID);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        while (rs.next()){
          query =
              "INSERT INTO zaduzenja (datum, cena, pdv, popust, naziv, zaduzenOd, userID, paketType, dug, zaMesec)"
                  + "VALUES "
                  + "(?,?,?,?,?,?,?,?,?,?)";
          ps = db.conn.prepareStatement(query);
          ps.setString(1, LocalDate.now().format(format_date));
          ps.setDouble(2, rs.getDouble("cena"));
          ps.setDouble(3, rs.getDouble("PDV"));
          ps.setDouble(4, rs.getDouble("popust"));
          ps.setString(5, rs.getString("nazivPaketa").toUpperCase());
          ps.setString(6, "SYSTEM");
          ps.setInt(7, rs.getInt("userID"));
          ps.setString(8, rs.getString("paketType"));

          double cena = rs.getDouble("cena");
          cena = countCenaFromStartOfMonth(cena);
          double pdv = rs.getDouble("pdv");
          double popust = rs.getDouble("popust");
          double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
          dug = dug + valueToPercent.getPDVOfValue(dug , pdv);
          ps.setDouble(9, dug);
          ps.setString(10, LocalDateTime.now().format(format_motn));
          ps.executeUpdate();

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private  void razduziDTV(int serviceID, int userID){
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE dtv_main=? OR id=? AND userID=?";
    DateTimeFormatter format_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter format_motn = DateTimeFormatter.ofPattern("yyyy-MM");
    DecimalFormat df = new DecimalFormat("#.00");
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1,serviceID);
      ps.setInt(2, serviceID);
      ps.setInt(3, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        while (rs.next()){
          query =
              "INSERT INTO zaduzenja (datum, cena, pdv, popust, naziv, zaduzenOd, userID, paketType, dug, zaMesec)"
                  + "VALUES "
                  + "(?,?,?,?,?,?,?,?,?,?)";
          ps = db.conn.prepareStatement(query);
          ps.setString(1, LocalDate.now().format(format_date));
          ps.setDouble(2, rs.getDouble("cena"));
          ps.setDouble(3, rs.getDouble("PDV"));
          ps.setDouble(4, rs.getDouble("popust"));
          ps.setString(5, rs.getString("nazivPaketa").toUpperCase());
          ps.setString(6, "SYSTEM");
          ps.setInt(7, rs.getInt("userID"));
          ps.setString(8, rs.getString("paketType"));

          double cena = rs.getDouble("cena");
          cena = countCenaFromStartOfMonth(cena);
          double pdv = rs.getDouble("pdv");
          double popust = rs.getDouble("popust");
          double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
          dug = dug + valueToPercent.getPDVOfValue(dug , pdv);
          ps.setDouble(9, dug);
          ps.setString(10, LocalDateTime.now().format(format_motn));
          ps.executeUpdate();

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void razduziBOX(int serviceID, int userID){
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE box_id=? OR id=? or dtv_main=? AND userID=?";
    DateTimeFormatter format_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter format_motn = DateTimeFormatter.ofPattern("yyyy-MM");
    DecimalFormat df = new DecimalFormat("#.00");
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1,serviceID);
      ps.setInt(2, serviceID);
      ps.setInt(3, serviceID);
      ps.setInt(4, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        while (rs.next()){
          query =
              "INSERT INTO zaduzenja (datum, cena, pdv, popust, naziv, zaduzenOd, userID, paketType, dug, zaMesec)"
                  + "VALUES "
                  + "(?,?,?,?,?,?,?,?,?,?)";
          ps = db.conn.prepareStatement(query);
          ps.setString(1, LocalDate.now().format(format_date));
          ps.setDouble(2, rs.getDouble("cena"));
          ps.setDouble(3, rs.getDouble("PDV"));
          ps.setDouble(4, rs.getDouble("popust"));
          ps.setString(5, rs.getString("nazivPaketa").toUpperCase());
          ps.setString(6, "SYSTEM");
          ps.setInt(7, rs.getInt("userID"));
          ps.setString(8, rs.getString("paketType"));

          double cena = rs.getDouble("cena");
          cena = countCenaFromStartOfMonth(cena);
          double pdv = rs.getDouble("pdv");
          double popust = rs.getDouble("popust");
          double dug = cena - valueToPercent.getPDVOfSum(cena, popust);
          dug = dug + valueToPercent.getPDVOfValue(dug , pdv);
          ps.setDouble(9, dug);
          ps.setString(10, LocalDateTime.now().format(format_motn));
          ps.executeUpdate();

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private double countCenaFromStartOfMonth(double cena) {
    LocalDateTime dateEnd = LocalDateTime.now();

    int day = dateEnd.getDayOfMonth();
    int daysInMont = LocalDate.now().lengthOfMonth();

    double cenaPerDay = cena / daysInMont;

    double dug = cenaPerDay*day;


    return dug;




  }

  public void produziService(int serviceID, String endDateStr, int prekoracenje, String operName,
      boolean skipProduzenje) {
    PreparedStatement ps;
    ResultSet rs;
    boolean newService = false;
    String UserName = null;
    int idCard = 0;
    String IPTV_MAC = null;

    String type = "NONE";
    String query;
    if (endDateStr.isEmpty()) {
      endDateStr = "2000-01-01";
    }
    LocalDate endDate = LocalDate.parse(endDateStr, dtfNormalDate);

    query = "SELECT * FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        type = rs.getString("paketType");
        newService = rs.getBoolean("newService");
        UserName = rs.getString("UserName");
        idCard = rs.getInt("idDTVCard");
        IPTV_MAC = rs.getString("IPTV_MAC");

        if (newService) {
          //ako produzeni service je 0 onda stavljamo 1 zbog mesec dana  unapred.
          //Nadam se samo u slucaju Ostalih servica.
          if (prekoracenje == 0) {
            prekoracenje = 1;
          }
          endDate = LocalDate.now();
          endDate = endDate.plusMonths(prekoracenje);
          endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
        } else {
          endDate = LocalDate.parse(LocalDate.parse(rs.getString("endDate")).format(dtfNormalDate));
          prekoracenje = 1;
          endDate = endDate.plusMonths(prekoracenje);
          endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
        }
        if (skipProduzenje) {
          endDate = LocalDate.parse(LocalDate.parse(rs.getString("endDate")).format(dtfNormalDate));
          prekoracenje = 0;
          endDate.plusMonths(prekoracenje);
          endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());

        }

      }

      rs.close();
      ps.close();

    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    switch (type) {
      case "NET":
      case "LINKED_NET":
        setEndDateNET(UserName, endDate);
        break;

      case "IPTV":
      case "LINKED_IPTV":
        setEndDateIPTV(IPTV_MAC, endDate);
        break;

      case "DTV":
      case "LINKED_DTV":
        setEndDateDTV(idCard, endDate, db);
        break;
    }

    query = "UPDATE servicesUser SET endDate=?,  aktivan=? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, endDate.format(dtfNormalDate));
      ps.setBoolean(2, true);
      ps.setInt(3, serviceID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }


  private void setEndDateNET(String username, LocalDate endDate) {
    String eDateRadCheck = endDate.format(dtfRadCheck);
    LocalTime time = LocalTime.of(23, 59, 59);
    String eDateRadReply = LocalDateTime.of(endDate, time).minusDays(1).format(dtfRadReply);

    PreparedStatement ps;
    String query;

    try {
      query = "UPDATE radreply SET value=? WHERE username=? AND attribute='WISPR-Session-Terminate-Time'";
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, eDateRadReply);
      ps.setString(2, username);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Expiration'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, eDateRadCheck);
      ps.setString(2, username);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void setEndDateDTV(int idCard, LocalDate endDate, database db) {
    String eDate = endDate.format(dtfNormalDate);
    String query;
    PreparedStatement ps;

    query = "UPDATE DTVKartice SET endDate=? WHERE idKartica=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, eDate);
      ps.setInt(2, idCard);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }


  private void setEndDateIPTV(String STB_MAC, LocalDate endDate) {
    String eDate = LocalDateTime.of(endDate, LocalTime.of(00, 00, 00)).format(dtfIPTV);
    StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
    stalkerRestAPI2.setEndDate(STB_MAC, eDate);
  }

  public String getIdentify(int id_servicesUser, database db) {
    PreparedStatement ps;
    ResultSet rs;
    String ident = "NEPOZNAT";

    String query = "SELECT * from servicesUser WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_servicesUser);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        if (rs.getString("idDTVCard") != null) {
          ident = rs.getString("idDTVCard");
        }
        if (rs.getString("UserName") != null) {
          ident = rs.getString("UserName");
        }
        if (rs.getString("FIKSNA_TEL") != null) {
          ident = rs.getString("FIKSNA_TEL");
        }
        if (rs.getString("IPTV_MAC") != null) {
          ident = rs.getString("IPTV_MAC");
        }
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return ident;
  }


  public boolean boxHaveFIX(int id_serviceUser) {
    PreparedStatement ps;
    ResultSet rs;
    boolean haveFix = false;
    String query = "SELECT FIKSNA_TEL FROM servicesUser WHERE id=? AND FIKSNA_TEL IS NOT NULL AND FIKSNA_TEL != '' ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_serviceUser);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        haveFix = true;
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return haveFix;
  }

  public void updateService(String operName, database db, JSONObject rLine) {
    PreparedStatement ps;
    String query = "";
    if (rLine.has("idDTVCard")) {
      query = "UPDATE servicesUser set idDTVCard=? where id=?";
    } else if (rLine.has("IPTV_MAC")) {
      query = "UPDATE servicesUser set IPTV_MAC=? where id=?";
    }

    try {
      ps = db.conn.prepareStatement(query);
      if (rLine.has("idDTVCard")) {
        ps.setString(1, rLine.getString("idDTVCard"));
        ps.setInt(2, rLine.getInt("id"));
      } else if (rLine.has("IPTV_MAC")) {
        ps.setString(1, rLine.getString("IPTV_MAC"));
        ps.setInt(2, rLine.getInt("id"));
      }
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    //update popust i obracun
    query = "UPDATE servicesUser SERT popust=? where id=?";

    //update field of DTVCard no with a new one
    if (rLine.has("idDTVCard")) {
      query = "UPDATE DTVKartice SET idKartica=? WHERE idKartica=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("idDTVCard"));
        ps.setString(2, rLine.getString("old_idDTVCard"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        setError(true);
        setErrorMSG(e.getMessage());
        e.printStackTrace();
      }
    }

  }


  public void deletePaketOstalo(int id) {
    PreparedStatement ps;
    String queru = "DELETE FROM ostaleUsluge WHERE id=?";

    try {
      ps = db.conn.prepareStatement(queru);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  public ServiceData getServiceData(int serviceID) {
    ServiceData serviceData = new ServiceData(db);
    return serviceData.getData(serviceID);
  }

  public JSONObject getServiceDataJSON(int serviceID) {
    ServiceData serviceData = new ServiceData(db);
    JSONObject object = serviceData.getDataJSON(serviceID);
    return object;
  }

  public JSONObject getServiceDetail(int serviceID, String operName) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE id = ?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        object.put("id", rs.getString("id"));
        object.put("nazivPaketa", rs.getString("nazivPaketa"));
        object.put("endDateService", rs.getString("endDate"));
        object.put("paketType", rs.getString("paketType"));
        String paketType = rs.getString("paketType");
        if (paketType.contains("DTV")) {
          object.put("identification", rs.getString("idDTVCard"));
          object.put("DTVPaket", rs.getString("DTVPaket"));
          object.put("endDate", getEndDateDTVCard(rs.getString("idDTVCard")));

        }
        if (paketType.contains("NET")) {
          Radius radius = new Radius(db, operName);
          JSONObject radReplyData = radius.getRadReplyData(rs.getString("UserName"));
          for (String key : radReplyData.keySet()) {
            object.put(key, radReplyData.getString(key));
          }

        }
        if (paketType.contains("FIX")) {
          object.put("FIKSNA_TEL", rs.getString("FIKSNA_TEL"));
        }
        if (paketType.contains("IPTV")) {
          StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
          JSONObject iptv_acc_info = stalkerRestAPI2.getAccInfo(rs.getString("IPTV_MAC"));
          object.put("IPTV_MAC", rs.getString("IPTV_MAC"));
          object.put("userName", iptv_acc_info);
        }
        if (paketType.contains("DTV")) {
          DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
          String endDate = dtvFunctions.getEndDate(rs.getInt("idDTVCard"));
          object.put("endDate", endDate);
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    return object;

  }


  private String getEndDateDTVCard(String idDTVCard) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT endDate from DTVKartice WHERE idKartica = ?";
    String endDate = null;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, Integer.valueOf(idDTVCard));
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        endDate = rs.getString("endDate");
      }
      ps.close();
      rs.close();


    } catch (SQLException e) {
      endDate = endDate + e.getMessage();
      e.printStackTrace();
    }
    return endDate;
  }

  public JSONObject changeDTVCard(JSONObject rLine) {
    JSONObject resp = new JSONObject();
    String query;
    PreparedStatement ps;

    //check if card exist
    query = "SELECT  idKartica FROM DTVKartice WHERE idKartica=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("DTVCardNew"));
      ResultSet rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        resp.put("ERROR", String.format("Kartica %d postoji", rLine.getInt("DTVCardNew")));
        ps.close();
        rs.close();
        return resp;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    query = "UPDATE servicesUser set idDTVCard = ? WHERE id =? ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("DTVCardNew"));
      ps.setInt(2, rLine.getInt("serviceID"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      resp.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    query = "UPDATE DTVKartice set idKartica = ? WHERE idKartica = ?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("DTVCardNew"));
      ps.setString(2, rLine.getString("DTVCardCurrent"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      resp.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    return resp;
  }

  public JSONObject changeDTVEndDate(JSONObject rLine) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    String query = "UPDATE servicesUser set endDate =? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("endDate"));
      ps.setInt(2, rLine.getInt("serviceID"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    query = "UPDATE DTVKartice set endDate = ? WHERE idKartica = ? ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("endDate"));
      ps.setInt(2, rLine.getInt("DTVCard"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    return object;
  }


  public void changeServiceComment(int servieID, String komentar) {
    PreparedStatement ps;
    String query = "UPDATE servicesUser SET komentar = ? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, komentar);
      ps.setInt(2, servieID);
      ps.executeUpdate();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public void setEndDate(int serviceID, String endDate) {
    PreparedStatement ps;
    String query = "UPDATE servicesUser set endDate = ? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, endDate);
      ps.setInt(2, serviceID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }


  public void deleteService(int serviceID, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE id =? AND userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          if (rs.getString("paketType").equals("BOX")) {
            deleteServiceBOX(serviceID, userID);
            return;
          }
          if (rs.getString("paketType").contains("IPTV")) {
            deleteServiceIPTV(rs.getInt("id"));
            if (isError()) {
              return;
            }
          }
          if (rs.getString("paketType").contains("DTV")) {
            deleteServiceDTV(rs.getInt("id"), userID);
          }
          if (rs.getString("paketType").contains("NET")) {
            deleteServiceNET(rs.getInt("id"));
          }
          if (rs.getString("paketType").contains("FIX")) {
            deleteServiceFIX(rs.getInt("id"));
          }
        }
      }
      ps.close();
      query = "DELETE FROM servicesUser where id=?";
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public static String getFiksnaTel(int id_servicesUser, database db) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT FIKSNA_TEL FROM servicesUser WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id_servicesUser);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        return rs.getString("FIKSNA_TEL");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addServiceDTVAddonCardService(JSONObject rLine) {
    PreparedStatement ps;
    int ID_OF_INSERTED_ROW = 0;
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    ServiceData mainDTVServiceID = servicesFunctions
        .getServiceData(rLine.getInt("mainDTVServiceID"));

    String query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, obracun, brojUgovora, "
            + "aktivan, newService, idDTVCard, linkedService, paketType, endDate, PDV, opis, dtv_main) "
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, rLine.getInt("serviceID"));
      ps.setString(2, rLine.getString("naziv"));
      ps.setString(3, LocalDateTime.now().format(dtf));
      ps.setInt(4, rLine.getInt("userID"));
      ps.setString(5, getOperName());
      ps.setDouble(6, rLine.getDouble("popust"));
      ps.setDouble(7, rLine.getDouble("cena"));
      ps.setBoolean(8, rLine.getBoolean("obracun"));
      ps.setString(9, rLine.getString("brUgovora"));
      ps.setBoolean(10, rLine.getBoolean("aktivan"));
      ps.setBoolean(11, true);
      ps.setInt(12, rLine.getInt("cardID"));
      ps.setBoolean(13, false);
      ps.setString(14, rLine.getString("paketType"));
      ps.setString(15, rLine.getString("endDate"));
      ps.setDouble(16, rLine.getDouble("pdv"));
      ps.setString(17,
          String.format("Dodatna kartica vezana za %s", mainDTVServiceID.getIdDTVCard()));
      ps.setInt(18, rLine.getInt("mainDTVServiceID"));

      ps.executeUpdate();

      ResultSet rs = ps.getGeneratedKeys();
      if (rs.next()) {
        ID_OF_INSERTED_ROW = rs.getInt(1);
      }
      ps.close();
      rs.close();

      //ako nije insertovan row vrati gresku
      if (ID_OF_INSERTED_ROW == 0) {
        setError(true);
        setErrorMSG("Ne mogu da pronadjem id servisa ili servis nije snimljen (SQL GRESKA)");
        return;
      }

    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
      return;
    }

    //ako je servis aktivan onda aktiviraj i obracunaj dodatnu karticu
    if (rLine.getBoolean("aktivan") && mainDTVServiceID.isObracun()) {
      zaduziKorisnika(ID_OF_INSERTED_ROW);
    }


  }

  /**
   * @param dtvMain row containg box_id with LINKED_DTV paketType
   * @param userID id of user
   * @return boxID of dtvMain id :)
   */
  public int getBoxIDOfDTVLinkedPaket(int dtvMain, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    int boxID = 0;
    String query = "SELECT box_id from servicesUser WHERE id=? and paketType='LINKED_DTV' AND userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, dtvMain);
      ps.setInt(2, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        boxID = rs.getInt("box_id");
      }

      rs.close();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return boxID;
  }

  public void setAktivateService(int id, boolean aktive) {
    PreparedStatement ps;
    String query = "UPDATE servicesUser set Aktivan=?, date_activated=? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setBoolean(1, aktive);
      ps.setString(2, LocalDateTime.now().format(dtf));
      ps.setInt(3, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  public void produziAllService(int userID, String endDate) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          String type = rs.getString("paketType");
          String UserName = rs.getString("UserName");
          String IPTV_MAC = rs.getString("IPTV_MAC");
          String idCard = rs.getString("idDTVCard");
          switch (type) {
            case "NET":
            case "LINKED_NET":
              setEndDateNET(UserName, LocalDate.parse(endDate));
              break;

            case "IPTV":
            case "LINKED_IPTV":
              setEndDateIPTV(IPTV_MAC, LocalDate.parse(endDate));
              break;

            case "DTV":
            case "DTV_ADDON":
            case "LINKED_DTV":
              setEndDateDTV(Integer.valueOf(idCard), LocalDate.parse(endDate), db);
              break;
          }

          query = "UPDATE servicesUser SET endDate=?,  aktivan=? WHERE id=?";
          try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, endDate);
            ps.setBoolean(2, true);
            ps.setInt(3, rs.getInt("id"));
            ps.executeUpdate();
            ps.close();
          } catch (SQLException e) {
            setError(true);
            setErrorMSG(e.getMessage());
            e.printStackTrace();
          }


        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public boolean isUserHaveGlobalService(JSONObject rLine, int userID) {
    boolean have = false;

    String search = "";

    if(rLine.getBoolean("chkKTV"))
      search="%DTV%";

    else if(rLine.getBoolean("chkNET"))
      search="%NET%";

    else if(rLine.getBoolean("chkIPTV"))
      search = "%IPTV%";

    else if(rLine.getBoolean("chkFIX"))
      search = "%FI%";

    else search ="%";

    String query = "SELECT id FROM servicesUser WHERE userID=? AND paketType like ?";

    PreparedStatement ps;
    ResultSet rs;

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      ps.setString(2, search);
      rs=ps.executeQuery();
      if (rs.isBeforeFirst()){
        ps.close();
        rs.close();
        return true;
      }

      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return false;

  }



  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public database getDb() {
    return db;
  }

  public void setDb(database db) {
    this.db = db;
  }


  public JSONObject getUserServices(int userID) {
    String query = "SELECT *  FROM servicesUser WHERE userID=? AND linkedService=false AND paketType NOT LIKE '%ADDON%' ";
    PreparedStatement ps;
    JSONObject jObj = new JSONObject();
    ResultSet rs;

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        JSONObject service;
        int i = 0;
        while (rs.next()) {
          service = new JSONObject();
          service.put("id", rs.getInt("id"));
          service.put("userID", rs.getString("userID"));
          service.put("brojUgovora", rs.getString("brojUgovora"));
          service.put("cena", rs.getDouble("cena"));
          service.put("popust", rs.getDouble("popust"));
          service.put("pdv", rs.getDouble("PDV"));
          service.put("operName", rs.getString("operName"));
          service.put("date_added", rs.getString("date_added"));
          service.put("nazivPaketa", rs.getString("nazivPaketa"));
          service.put("naziv", rs.getString("nazivPaketa"));

          //SETTING idUniqueName
          String[] uniqueName = new String[4];
          if (!rs.getString("IPTV_MAC").trim().isEmpty()) {
            service.put("idUniqueName", rs.getString("IPTV_MAC"));
            uniqueName[0] = "IPTV: " + rs.getString("IPTV_MAC");
            service.put("IPTV_MAC", rs.getString("IPTV_MAC"));
            service.put("STB_MAC", rs.getString("IPTV_MAC"));
          } else if (!rs.getString("UserName").trim().isEmpty()) {
            service.put("idUniqueName", rs.getString("UserName"));
            uniqueName[1] = "NET: " + rs.getString("UserName");
          } else if (!rs.getString("idDTVCard").trim().isEmpty()) {
            service.put("idUniqueName", rs.getString("idDTVCard"));
            uniqueName[2] = "DTV: " + rs.getString("idDTVCard");
          } else if (!rs.getString("FIKSNA_TEL").trim().isEmpty()) {
            service.put("idUniqueName", rs.getString("FIKSNA_TEL"));
            uniqueName[3] = "FIKSNA: " + rs.getString("FIKSNA_TEL");
          } else {
            service.put("idUniqueName", rs.getString("nazivPaketa"));
          }

          if (rs.getString("paketType").equals("BOX")) {
            String serviceSTR = "";
            for (String str : uniqueName) {
              if (str != null) {
                if (!str.isEmpty()) {
                  serviceSTR += str + " ";
                }
              }
            }
            service.put("idUniqueName", serviceSTR);
          }


          service.put("userName", rs.getString("UserName"));
          service.put("groupName", rs.getString("GroupName"));
          service.put("IPTV_MAC", rs.getString("IPTV_MAC"));
          service.put("FIKSNA_TEL", rs.getString("FIKSNA_TEL"));
          service.put("obracun", rs.getBoolean("obracun"));
          service.put("aktivan", rs.getBoolean("aktivan"));
          service.put("id_service", rs.getInt("id_service"));
          service.put("box", rs.getBoolean("BOX_service"));
          service.put("box_ID", rs.getInt("box_id"));
          service.put("brojTel", rs.getString("FIKSNA_TEL"));
          service.put("paketType", rs.getString("paketType"));
          service.put("linkedService", rs.getBoolean("linkedService"));
          service.put("newService", rs.getBoolean("newService"));
          service.put("idDTVCard", rs.getString("idDTVCard"));
          service.put("endDate", rs.getString("endDate"));
          service.put("komentar", rs.getString("komentar"));
          service.put("opis", rs.getString("opis"));
          service.put("dtv_main", rs.getString("dtv_main"));

          jObj.put(String.valueOf(i), service);
          i++;
        }
      }
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return jObj;
  }
}
