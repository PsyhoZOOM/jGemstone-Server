package net.yuvideo.jgemstone.server.classes.DTV;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServiceData;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by zoom on 2/27/17.
 */
public class DTVFunctions {
  private static SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
  private boolean error;
  private String errorMSG;
  database db;
  private String operName;


  public DTVFunctions(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }

  public boolean check_card_busy(int cardID) {
    if (cardID == 0) {
      return false;
    }
    PreparedStatement ps;
    ResultSet rs;
    Boolean cardExist = false;

    String query = "SELECT idKartica from DTVKartice where idKartica = ?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, cardID);
      rs = ps.executeQuery();
      cardExist = rs.isBeforeFirst();
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return cardExist;

  }

  public void addCard(JSONObject rLine) {

    PreparedStatement ps;

    String query =
        "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate, createDate ) VALUES " +
            "(?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, rLine.getInt("cardID"));
      ps.setInt(2, rLine.getInt("userID"));
      ps.setInt(3, rLine.getInt("paketID"));
      ps.setString(4, rLine.getString("endDate"));
      ps.setString(5, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public int getPacketCriteriaGroup(int id_packet) {
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
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return paketID;
  }




  public String getEndDate(int DTVCardID){
    PreparedStatement ps;
    ResultSet rs;
    String endDate = null;
    String query = "SELECT endDate FROM DTVKartice WHERE idKartica = ?";
    try {
      ps =  db.conn.prepareStatement(query);
      ps.setInt(1, DTVCardID);
      rs = ps.executeQuery();
      if(rs.isBeforeFirst()) {
        rs.next();
        endDate = rs.getString("endDate");
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      this.error = true;
      this.errorMSG = e.getMessage();
      e.printStackTrace();
    }
    return  endDate;
  }

  public JSONObject getUserServiceKartice(int idService) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    int i = 0;

    //get MainCard
    object.put(String.valueOf(i), getUserCard(idService));

    //getAddonCards
    String query = "SELECT id from servicesUser WHERE dtv_main=? and paketType='DTV_ADDON' ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idService);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          i++;
          object.put(String.valueOf(i), getUserCard(rs.getInt("id")));
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return object;
  }

  public JSONObject getUserCard(int idService) {
    JSONObject object = new JSONObject();
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    ServiceData serviceData = servicesFunctions.getServiceData(idService);
    DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
    object = dtvPaketFunctions.getCard(Integer.parseInt(serviceData.getIdDTVCard()));
    object.put("idService", idService);
    if (dtvPaketFunctions.isError()) {
      object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      setError(true);
      setErrorMSG(dtvPaketFunctions.getErrorMSG());
    } else if (servicesFunctions.isError()) {
      object.put("ERROR", serviceData.getErrorMSG());
      setErrorMSG(servicesFunctions.getErrorMSG());
      setError(true);
    }
    return object;

  }

  public void updateUserDTVPaketID(int paketID, int idService) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "select idDTVCard from servicesUser where id=? or dtv_main=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idService);
      ps.setInt(2, idService);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          updateDTVKarticuPaketID(rs.getInt("idDTVCard"), paketID);
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  private void updateDTVKarticuPaketID(int idDTVCard, int paketID) {
    PreparedStatement ps;
    String query = "UPDATE DTVKartice set paketID=? WHERE idKartica=? ";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, paketID);
      ps.setInt(2, idDTVCard);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }


  public void addAddonCard(JSONObject rLine) {
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.addServiceDTVAddonCardService(rLine);
    addCard(rLine);
  }

  public void addAddonPaket(int serviceID, int dodatakID, int userID) {
    DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());

    DTVPaketData dtvAddon = dtvPaketFunctions.getDTVAddonData(dodatakID);

    ServiceData serviceData = servicesFunctions.getServiceData(serviceID);

    JSONObject object = new JSONObject();
    object.put("serviceID", dtvAddon.getId());
    object.put("mainDTVServiceID", serviceData.getId());
    object.put("cardID", serviceData.getIdDTVCard());
    object.put("naziv", dtvAddon.getNaziv());
    object.put("userID", userID);
    object.put("popust", serviceData.getPopust());
    object.put("cena", dtvAddon.getCena());
    object.put("obracun", serviceData.isObracun());
    object.put("paketType", "DTV_ADDON_PROGRAM");
    object.put("pdv", dtvAddon.getPdv());
    object.put("endDate", serviceData.getEndDate());
    object.put("brUgovora", serviceData.getBrojUgovora());
    object.put("produzenje", serviceData.getProduzenje());
    object.put("markForDelete", false);
    object
        .put("opis", String.format("Programski dodatak za karticu %s", serviceData.getIdDTVCard()));
    object.put("aktivan", serviceData.isAktivan());

    servicesFunctions.addServiceDTVAddonCardService(object);
    //ako je servis aktivan zaduziti korisnika
    //a ako nije servis aktivan, kada se aktivira mora zaduziti dodatni paketi

  }

  public void removeAddonPaket(int serviceID, String naziv, int userID) {
    PreparedStatement ps;
    String query = "DELETE FROM servicesUser WHERE nazivPaketa=? and userID=? and dtv_main=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, naziv);
      ps.setInt(2, userID);
      ps.setInt(3, serviceID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }

  public void activateDTV(int serviceID, String endDate, int userID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT id, idDTVCard, paketType, obracun FROM servicesUser WHERE id=?  and userID=? or dtv_main=? and userID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      ps.setInt(2, userID);
      ps.setInt(3, serviceID);
      ps.setInt(4, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
        while (rs.next()) {
          servicesFunctions.setEndDate(rs.getInt("id"), endDate);
          servicesFunctions.setAktivateService(rs.getInt("id"), true);

          if (rs.getBoolean("obracun"))
          servicesFunctions.zaduziKorisnika(rs.getInt("id"));
          activateCards(rs.getInt("id"), endDate);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void activateCards(int id, String endDate) {
    PreparedStatement ps;
    ResultSet rs;

    //Get idDTVCard from database
    String query = "SELECT id, idDTVCard, paketType FROM servicesUser WHERE id=? or dtv_main=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.setInt(2, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
          servicesFunctions.setEndDate(rs.getInt("id"), endDate);
          if (!rs.getString("paketType").equals("DTV_ADDON_PROGRAM")) {
            setEndDate(rs.getInt("idDTVCard"), endDate, operName);
          }


        }
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }


  public void setEndDate(int idKartica, String endDate, String operName) {
    PreparedStatement ps;
    String query = "UPDATE DTVKartice SET endDate=? WHERE idKartica=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, endDate);
      ps.setInt(2, idKartica);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  public JSONObject getUserAddonsID(int dtv_id, int userID, boolean isBox) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "";
    if (isBox) {
      query = "SELECT id FROM servicesUser WHERE box_id=? AND userID=? AND paketType LIKE '%DTV%' OR dtv_main=? AND userID=? AND paketType LIKE '%DTV%'";
    } else {
      query = "SELECT id FROM servicesUser WHERE id=? and userID=? or dtv_main =? and userID=?";
    }
    try {
      ps = db.conn.prepareStatement(query);
      if (isBox) {
        ps.setInt(1, dtv_id);
        ps.setInt(2, userID);
        ps.setInt(3, dtv_id);
        ps.setInt(4, userID);
      } else {
        ps.setInt(1, dtv_id);
        ps.setInt(2, userID);
        ps.setInt(3, dtv_id);
        ps.setInt(4, userID);
      }
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        ServiceData servicesData = new ServiceData(db, getOperName());
        while (rs.next()) {
          JSONObject service = servicesData.getDataJSON(rs.getInt("id"));
          object.put(String.valueOf(i), service);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return object;
  }


  public void deleteCard(int idKartica) {
    PreparedStatement ps;
    String query = "DELETE from DTVKartice WHERE idKartica=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idKartica);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
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


}
