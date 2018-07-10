package net.yuvideo.jgemstone.server.classes.RADIUS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class Radius {

  private final database db;

  private final DateTimeFormatter dtfRadcheck = DateTimeFormatter.ofPattern("dd MMM yyyy");
  private final DateTimeFormatter dtfRadreply = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private String errorMSG;
  private boolean error = false;

  public Radius(database db) {
    this.db = db;
  }

  public JSONObject getUsers(String userName) {
    JSONObject users = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radreply";
    try {
      ps = db.connRad.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject user = new JSONObject();
          user.put("id", rs.getString("id"));
          user.put("username", rs.getString("username"));
          user.put("attribute", rs.getString("attribute"));
          user.put("value", rs.getString("value"));
          users.put(String.valueOf(i), user);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      users.put("ERROR", e.getMessage());
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
    }

    return users;
  }

  public JSONObject getRadReplyData(String userName) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radreply WHERE username=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, userName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        object.put("username", userName);
        while (rs.next()) {
          String att = rs.getString("attribute");
          switch (att) {
            case "Framed-IP-Address":
              object.put("Framed-IP-Address", rs.getString("value"));
              break;
            case "Framed-Pool":
              object.put("Framed-Pool", rs.getString("value"));
              break;
            case "Filter-Id":
              object.put("Filter-Id", rs.getString("value"));
              break;
            case "Mikrotik-Rate-Limit":
              object.put("Mikrotik-Rate-Limit", rs.getString("value"));
              break;
            case "WISPR-Session-Terminate-Time":
              object.put("WISPR-Session-Terminate-Time", rs.getString("value"));
              break;

          }
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      object.put("ERROR", e.getMessage());
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return object;
    }

    query = "SELECT * FROM radcheck where username= ?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, userName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          String att = rs.getString("attribute");
          switch (att) {
            case "Auth-Type":
              object.put("Auth-Type", rs.getString("value"));
              break;
            case "Simultaneous-Use":
              object.put("Simultaneous-Use", rs.getString("value"));
              break;
          }
        }
      }
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    return object;
  }

  public JSONObject changeUserPass(String username, String pass) {
    JSONObject object = new JSONObject();

    PreparedStatement ps;
    String query = "UPDATE radcheck set value = ? where username=? and attribute = 'MD5-Password' ";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, pass);
      ps.setString(2, username);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }
    return object;
  }

  public JSONObject changeRadReplyData(JSONObject rLine) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    String query = "DELETE FROM radreply where username=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, rLine.getString("username"));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      object.put("ERROR", e.getMessage());
      e.printStackTrace();
    }

    query = "INSERT INTO radreply (username, attribute, op, value) VALUES (?,?,':=',?) ";

    for (String key : rLine.keySet()) {
      switch (key) {
        case "endDate":
          boolean updated = changeUserEndDate(rLine.getString("username"),
              rLine.getString("endDate"));
          if (!updated) {
            object.put("ERROR", "Ne mogu da izmenim datum isteka!");
            return object;
          }
          break;
        case "IPAddress":
          changeUserIPAddress(rLine.getString("username"), rLine.getString("IPAddress"));
          break;
        case "pool":
          changeUserIPPool(rLine.getString("username"), rLine.getString("pool"));
          break;
        case "filterID":
          changeUserFilterID(rLine.getString("username"), rLine.getString("filterID"));
          break;
        case "Reject": {
          setReject(rLine.getString("username"), rLine.getBoolean("Reject"));
        }
        case "Mikrotik-Rate-Limit": {
          setBadwidthLimit(rLine.getString("username"), rLine.getString("Mikrotik-Rate-Limit"));
        }
        case "Simultaneous-Use": {
          setSimultaneousUse(rLine.getString("username"), rLine.getString("Simultaneous-Use"));
        }
        break;
      }
    }

    return object;
  }

  private boolean setSimultaneousUse(String username, String simulUse) {
    PreparedStatement ps;
    String query = "DELETE FROM radcheck WHERE username=? and attribute='Simultaneous-Use'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return error;
    }

    if (simulUse.isEmpty()) {
      return false;
    }

    query = "INSERT INTO radcheck (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Simultaneous-Use");
      ps.setString(3, ":=");
      ps.setString(4, simulUse);
      ps.executeUpdate();
      error = false;
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }

    return false;
  }

  private boolean setBadwidthLimit(String username, String rateLimit) {
    if (rateLimit.isEmpty()) {
      return false;
    }
    PreparedStatement ps;
    String query = "DELETE FROM radreply WHERE username=? and attribute='Mikrotik-Rate-Limit'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return false;
    }

    query = "INSERT INTO radreply (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Mikrotik-Rate-Limit");
      ps.setString(3, "=");
      ps.setString(4, rateLimit);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }
    return error;
  }

  private boolean changeUserFilterID(String username, String filterID) {
    if (filterID.isEmpty()) {
      return false;
    }
    PreparedStatement ps;
    String query = "DELETE FROM radreply where username=? and attribute=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Filter-Id");
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }

    query = "INSERT INTO radreply (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Filter-Id");
      ps.setString(3, "=");
      ps.setString(4, filterID);
      ps.executeUpdate();
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }

    return error;
  }

  private boolean changeUserIPPool(String username, String pool) {
    if (pool.isEmpty()) {
      return false;
    }
    PreparedStatement ps;
    String query = "DELETE FROM radreply where username=? and attribute= ?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Framed-Pool");
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return error;
    }

    query = "INSERT INTO radreply (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Framed-Pool");
      ps.setString(3, "=");
      ps.setString(4, pool);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }

    return error;
  }

  private boolean changeUserIPAddress(String username, String ipAddress) {
    if (ipAddress.isEmpty()) {
      return false;
    }
    PreparedStatement ps;
    String query = "DELETE FROM radreply where username=? and attribute =?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Framed-IP-Address");
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
    }

    query = "INSERT INTO radreply  (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Framed-IP-Address");
      ps.setString(3, "=");
      ps.setString(4, ipAddress);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return error;
    }

    return error;
  }

  public boolean changeUserEndDate(String username, String endDate) {
    boolean updated = false;

    LocalTime time = LocalTime.of(00, 00, 00);

    LocalDate date = LocalDate.parse(endDate);
    String endDateFormated = LocalDateTime.of(date, time).format(dtfRadreply);
    System.out.println("DAT:" + endDateFormated);
    PreparedStatement ps;
    String query;
    query = "delete from radcheck where username=? and attribute='Expiration'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
      updated = true;
    } catch (SQLException e) {
      updated = false;
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return updated;
    }

    query = "DELETE FROM radreply where username=? and attribute  = 'WISPR-Session-Terminate-Time'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
      updated = true;
    } catch (SQLException e) {
      updated = false;
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return updated;
    }

    query = "INSERT INTO radcheck (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Expiration");
      ps.setString(3, ":=");
      ps.setString(4, LocalDateTime.parse(endDateFormated).format(dtfRadcheck));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
      return updated;
    }

    query = "INSERT INTO radreply (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "WISPR-Session-Terminate-Time");
      ps.setString(3, "=");
      ps.setString(4, LocalDateTime.parse(endDateFormated).minusSeconds(1).format(dtfRadreply));
      ps.executeUpdate();
      ps.close();
      updated = true;
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      updated = false;
      e.printStackTrace();
      return updated;
    }
    return updated;
  }


  public boolean setReject(String username, boolean reject) {
    PreparedStatement ps;
    String query = "DELETE FROM radcheck where username=? and attribute = 'Auth-type'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      errorMSG = e.getMessage();
      error = true;
      e.printStackTrace();
      return error;
    }

    query = "INSERT INTO radcheck (username, attribute, op, value) VALUES (?,?,?,?)";
    try {
      String rejected;
      if (reject) {
        rejected = "Reject";
      } else {
        rejected = "Accept";
      }

      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, "Auth-Type");
      ps.setString(3, ":=");
      ps.setString(4, rejected);
      ps.executeUpdate();
      ps.close();
      error = false;
    } catch (SQLException e) {
      error = true;
      errorMSG = e.getMessage();
      e.printStackTrace();
    }

    return error;
  }

  public void activateUser(String userName) {
    PreparedStatement ps;
    String query = "UPDATE radcheck set value='Accept' WHERE attribute='Auth-Type' AND username=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, userName);
      ps.executeUpdate();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }

  public String getCalledID(String ip, String sessionID) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT calledstationid from radacct WHERE framedipaddress=? AND acctsessionid =?";
    String calledID = "N/A";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, ip);
      ps.setString(2, sessionID.replace("0x", ""));
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        calledID = rs.getString("calledstationid");
      }
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return calledID;
  }

  public JSONObject getTrafficReport(String username) {
    JSONObject userTrafficObj = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radacct WHERE username=? order by radacctid desc limit ?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setInt(2, 10);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject userTraffic = new JSONObject();
          userTraffic.put("radacctid", rs.getLong("radacctid"));
          userTraffic.put("acctsessionid", rs.getString("acctsessionid"));
          userTraffic.put("acctuniqueid", rs.getString("acctuniqueid"));
          userTraffic.put("username", rs.getString("username"));
          userTraffic.put("realm", rs.getString("realm"));
          userTraffic.put("nasipaddress", rs.getString("nasipaddress"));
          userTraffic.put("nasportid", rs.getString("nasportid"));
          userTraffic.put("nasporttype", rs.getString("nasporttype"));
          userTraffic.put("acctstarttime", rs.getString("acctstarttime"));
          userTraffic.put("acctstoptime", rs.getString("acctstoptime"));
          if (rs.wasNull()) {
            userTraffic.remove("acctstoptime");
            userTraffic.put("acctstoptime", "ONLINE");
          }
          userTraffic.put("acctsessiontime", rs.getInt("acctsessiontime"));
          userTraffic.put("acctauthentic", rs.getString("acctauthentic"));
          userTraffic.put("connectinfo_start", rs.getString("connectinfo_start"));
          userTraffic.put("connectinfo_stop", rs.getString("connectinfo_stop"));
          userTraffic.put("acctinputoctets", rs.getLong("acctinputoctets"));
          userTraffic.put("acctoutputoctets", rs.getLong("acctoutputoctets"));
          userTraffic.put("calledstationid", rs.getString("calledstationid"));
          userTraffic.put("callingstationid", rs.getString("callingstationid"));
          userTraffic.put("acctterminatecause", rs.getString("acctterminatecause"));
          userTraffic.put("servicetype", rs.getString("servicetype"));
          userTraffic.put("framedprotocol", rs.getString("framedprotocol"));
          userTraffic.put("framedipaddress", rs.getString("framedipaddress"));
          userTrafficObj.put(String.valueOf(i), userTraffic);
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
    return userTrafficObj;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

}
