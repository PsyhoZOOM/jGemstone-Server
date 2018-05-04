package net.yuvideo.jgemstone.server.classes.BOX;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

  public database db;
  private ResultSet rs;
  private PreparedStatement ps;
  private String query;

  private int BOX_Service_ID;
  private SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");

  public String addBox(JSONObject rLine, String opername) {

    query =
        "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, "
            +
            "cena, obracun, brojUgovora, aktivan, produzenje, newService, idDTVCard, username, GroupName, IPTV_MAC, FIKSNA_TEL, linkedService, BOX_service, paketType, PDV)"
            +
            "VALUES " +
            "(?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?)";

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
      ps.setInt(11, rLine.getInt("produzenje"));
      ps.setBoolean(12, true);
      if (rLine.has("DTVKartica")) {
        ps.setInt(13, rLine.getInt("DTVKartica"));
      } else {
        ps.setNull(13, Types.VARCHAR);
      }
      if (rLine.has("userName")) {
        ps.setString(14, rLine.getString("userName"));
      } else {
        ps.setNull(14, Types.VARCHAR);
      }
      if (rLine.has("groupName")) {
        ps.setString(15, rLine.getString("groupName"));
      } else {
        ps.setNull(15, Types.VARCHAR);
      }
      if (rLine.has("STB_MAC")) {
        ps.setString(16, rLine.getString("STB_MAC"));
      } else {
        ps.setNull(16, Types.VARCHAR);
      }
      if (rLine.has("FIX_TEL")) {
        ps.setString(17, rLine.getString("FIX_TEL"));
      } else {
        ps.setNull(17, Types.VARCHAR);
      }

      ps.setBoolean(18, false);
      ps.setBoolean(19, true);
      ps.setString(20, "BOX");
      ps.setDouble(21, rLine.getDouble("pdv"));

      ps.executeUpdate();
      ResultSet rsBoxId = ps.getGeneratedKeys();
      rsBoxId.next();
      BOX_Service_ID = rsBoxId.getInt(1);

      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
      if (!stalkerRestAPI2.isHostAlive) {
        return "IPTV Server nije u funkciji.\n Probajte kasnije ili se obratite tehnickoj podrsci!";
      }
      if (rLine.has("STB_MAC")) {
        add_iptv(rLine, opername);
      }
      if (rLine.has("groupName")) {
        add_internet(rLine, opername);
      }
      if (rLine.has("DTVKartica")) {
        add_dtv(rLine, opername);
      }
      if (rLine.has("FIX_TEL")) {
        if (!rLine.getString("FIX_TEL").isEmpty()) {
          add_fix(rLine, opername);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();

    }

    return "OK";
  }

  private String add_iptv(JSONObject rLine, String opername) {
    JSONObject json = IPTVFunctions.add_account(rLine, this.db);
    if (json.has("ERROR")) {
      return json.getString("ERROR");
    }
    ServicesFunctions.addServiceIPTVLinked(rLine, opername, BOX_Service_ID, this.db);
    return "OK";
  }

  public int getBOX_ID() {
    return this.BOX_Service_ID;
  }

  private void add_internet(JSONObject rLine, String opername) {
    NETFunctions.addUser(rLine, this.db);
    ServicesFunctions.addServiceNETLinked(rLine, opername, BOX_Service_ID, this.db);

  }

  private void add_dtv(JSONObject rLine, String opername) {

    DTVFunctions.addCard(rLine, opername, this.db);
    ServicesFunctions.addServiceDTVLinked(rLine, opername, BOX_Service_ID, this.db);
  }

  private void add_fix(JSONObject rLine, String opername) {
    FIXFunctions.addBroj(rLine, opername, this.db);
    ServicesFunctions.addServiceFIXLinked(rLine, opername, BOX_Service_ID, this.db);
  }

}