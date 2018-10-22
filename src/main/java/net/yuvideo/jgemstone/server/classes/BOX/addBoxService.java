package net.yuvideo.jgemstone.server.classes.BOX;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.yuvideo.jgemstone.server.classes.DTV.DTVFunctions;
import net.yuvideo.jgemstone.server.classes.FIX.FIXFunctions;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.IPTVFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.StalkerRestAPI2;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by zoom on 2/27/17.
 */
public class addBoxService {

  private final String operName;
  public database db;
  private PreparedStatement ps;
  private String query;

  private int BOX_Service_ID;
  private SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");

  public addBoxService(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }

  public boolean checkIPTVAlive() {
    StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
    if (!stalkerRestAPI2.isHostAlive()) {
      return false;
    }
    return true;
  }

  public boolean addBox(JSONObject rLine, String opername) {

    query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, "
            +
            "cena, obracun, brojUgovora, aktivan, newService, idDTVCard, username, GroupName,"
            + " IPTV_MAC, FIKSNA_TEL, linkedService, BOX_service, paketType, PDV, opis, endDate)"
            +
            "VALUES " +
            "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
      ps.setBoolean(11, true);
      if (rLine.has("cardID")) {
        ps.setInt(12, rLine.getInt("cardID"));
      } else {
        ps.setString(12, "");
      }
      if (rLine.has("userName") && rLine.has("groupName")) {
        ps.setString(13, rLine.getString("userName"));
        ps.setString(14, rLine.getString("groupName"));
      } else {
        ps.setString(13, "");
        ps.setString(14, "");
      }
      if (rLine.has("IPTV_MAC")) {
        ps.setString(15, rLine.getString("IPTV_MAC"));
      } else {
        ps.setString(15, "");
      }
      if (rLine.has("FIX_TEL")) {
        ps.setString(16, rLine.getString("FIX_TEL"));
      } else {
        ps.setString(16, "");
      }

      ps.setBoolean(17, false);
      ps.setBoolean(18, true);
      ps.setString(19, "BOX");
      ps.setDouble(20, rLine.getDouble("pdv"));
      ps.setString(21, rLine.getString("opis"));
      ps.setString(22, "2000-01-01");

      ps.executeUpdate();
      ResultSet rsBoxId = ps.getGeneratedKeys();
      rsBoxId.next();
      BOX_Service_ID = rsBoxId.getInt(1);


      if (rLine.has("STB_MAC")) {
        add_iptv(rLine, opername);
      }
      if (rLine.has("groupName")) {
        add_internet(rLine, opername);
      }
      if (rLine.has("cardID")) {
        add_dtv(rLine, opername);
      }
      if (rLine.has("FIX_TEL")) {
        if (!rLine.getString("FIX_TEL").isEmpty()) {
          add_fix(rLine, opername);
        }
      }

      ps.close();
      rsBoxId.close();


    } catch (SQLException e) {
      e.printStackTrace();
      return false;

    }

    return true;
  }

  private String add_iptv(JSONObject rLine, String opername) {
    IPTVFunctions iptvFunctions = new IPTVFunctions(db, getOperName());
    JSONObject json = iptvFunctions.add_account(rLine);
    if (json.has("ERROR")) {
      return json.getString("ERROR");
    }
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.addServiceIPTVLinked(rLine, getOperName(), BOX_Service_ID);
    return "OK";
  }

  public int getBOX_ID() {
    return this.BOX_Service_ID;
  }

  private void add_internet(JSONObject rLine, String opername) {
    NETFunctions netFunctions = new NETFunctions(db, getOperName());
    netFunctions.addUser(rLine);
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.addServiceNETLinked(rLine, getOperName(), BOX_Service_ID);

  }

  private void add_dtv(JSONObject rLine, String opername) {

    DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
    dtvFunctions.addCard(rLine);
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.addServiceDTVLinked(rLine, getOperName(), BOX_Service_ID);
  }

  private void add_fix(JSONObject rLine, String opername) {
    FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
    fixFunctions.addBroj(rLine);
    ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
    servicesFunctions.addServiceFIXLinked(rLine, BOX_Service_ID);
  }

  public String getOperName() {
    return operName;
  }

}