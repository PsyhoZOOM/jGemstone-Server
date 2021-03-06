package net.yuvideo.jgemstone.server.classes.RADIUS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.MIKROTIK_API.MikrotikAPI;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

public class Radius {

  private database db;

  private final DateTimeFormatter dtfRadcheck = DateTimeFormatter.ofPattern("dd MMM yyyy");
  private final DateTimeFormatter dtfRadreply = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private String operName;

  private String errorMSG;
  private boolean error = false;

  public Radius(database db, String operName) {
    this.db = db;
    this.operName = operName;
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
      ps.close();
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
          setEndDate(rLine.getString("username"),
              rLine.getString("endDate"));
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

      ps.close();
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
      ps.close();
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

  public void setEndDate(String username, String endDate) {
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
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    query = "DELETE FROM radreply where username=? and attribute  = 'WISPR-Session-Terminate-Time'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
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
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
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
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
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
      ps.close();
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
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return calledID;
  }


  public JSONObject getTrafficeReports(String startTime, String stopTime) {
    JSONObject reports = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radacct  WHERE acctstarttime  >= ? AND acctstoptime <= ? ORDER BY radacctid DESC";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, startTime + "00:00:00");
      ps.setString(2, stopTime + " 23:59:59");
      rs = ps.executeQuery();
      System.out.println(ps.toString());
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject result = new JSONObject();
          result.put("id", rs.getInt("radacctid"));
          result.put("username", rs.getString("username"));
          result.put("nasIP", rs.getString("nasipaddress"));
          result.put("startTime", rs.getString("acctstarttime"));
          result.put("stopTime", rs.getString("acctstoptime"));
          result.put("onlineTime", rs.getString("acctsessiontime"));
          result.put("inputOctets", rs.getLong("acctinputoctets"));
          result.put("outputOctets", rs.getLong("acctoutputoctets"));
          result.put("service", rs.getString("calledstationid"));
          result.put("callingStationID", rs.getString("callingstationid"));
          result.put("ipAddress", rs.getString("framedipaddress"));
          result.put("terminateCause", rs.getString("acctterminatecause"));
          reports.put(String.valueOf(i), result);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());

      e.printStackTrace();
    }

    return reports;
  }

  public JSONObject getUserTrafficReport(String username, String startTime, String stopTime) {
    JSONObject userTrafficObj = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radacct WHERE username=? AND acctstarttime >= ? AND acctstoptime <= ? ORDER BY radacctid DESC ";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      ps.setString(2, startTime);
      ps.setString(3, stopTime);
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

  public JSONObject getRadiusOnlineLOG(int rowCount) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radpostauth ORDER BY authdate DESC LIMIT ?";
    int i = 0;
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setInt(1, rowCount);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          JSONObject onlineLog = new JSONObject();
          onlineLog.put("id", rs.getInt("id"));
          onlineLog.put("username", rs.getString("username"));
          onlineLog
              .put("reply", checkReplyMessage(rs.getString("reply"), rs.getString("username")));
          onlineLog.put("authdate", rs.getString("authdate"));
          onlineLog.put("clientid", rs.getString("clientid"));
          onlineLog.put("nas", rs.getString("nas"));
          object.put(String.valueOf(i), onlineLog);
          i++;

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

  private String checkReplyMessage(String reply, String userName) {
    if (reply.trim().equals("Access-Accept")) {
      return "Korisnik ulogovan";
    } else if (reply.contains("Access-Reject")) {
      NETFunctions netFunctions = new NETFunctions(db, getOperName());
      boolean userExist = netFunctions.check_userName_busy(userName);
      if (!userExist) {
        return "Korisničko ime ne postoji";
      } else {
        boolean userActive = checkIsUserActive(userName);
        if (!userActive) {
          return "Korisnik nije aktiva";
        }
      }
    } else if (reply.contains("expired")) {
      return "Uplata je istekla";
    } else if (reply.contains("already logged in")) {
      return "Korisnik je već ulogovan";
    } else if (reply.contains("empty password")) {
      return "Logovanje bez passworda";
    } else if (reply.contains("MD5 password check failed")) {
      return "Pogrešan password";
    }

    return reply;
  }

  private boolean checkIsUserActive(String userName) {
    boolean active = false;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT value FROM radcheck WHERE username=? and attribute='Auth-Type'";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, userName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        if (rs.getString("value").equals("Accept")) {
          active = true;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return active;
  }

  public JSONObject searchAllusers() {
    JSONObject usersObj = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM radcheck WHERE attribute = 'Expiration'";

    try {
      ps = db.connRad.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          UsersData usersData = new UsersData(db, getOperName());
          JSONObject userObj;
          int userID = usersData.getUserIDOfRadiusUserName(rs.getString("username"));
          userObj = usersData.getUserData(userID);
          if (userObj.isNull("id")) {
            continue;
          }
          userObj.put("endDate", rs.getString("value"));
          userObj.put("username", rs.getString("username"));
          userObj.put("groupName", getRadUserGroup_GroupName(rs.getString("username")));
          usersObj.put(String.valueOf(i), userObj);
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
    return usersObj;
  }

  private String getRadUserGroup_GroupName(String username) {
    PreparedStatement ps;
    ResultSet rs;
    String groupName = "";
    String query = "SELECT groupname FROM radusergroup WHERE username=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, username);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        groupName = rs.getString("groupname");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return groupName;
  }

  public String changeMTRateLimit(String userName, String ipAddres, String nasIP, String bwLimit) {
    String response = "";
    String data = "nema";
    String[] cmd = {"sh", "-c",
        String.format("rateLimit.sh %s %s %s %s %s", userName, ipAddres, nasIP, getNasSecret(nasIP),
            bwLimit)};

    try {
      Process exec = new ProcessBuilder(cmd).start();
      exec.waitFor();

      BufferedReader bfReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

      while ((response = bfReader.readLine()) != null) {
        if (response.contains("CoA-ACK")) {
          data = response;
        }
      }
    } catch (IOException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    } catch (InterruptedException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    if (data == null) {
      data = "nema";
    }

    return data;

  }

  public String disconnectUser(String userName, String ipAddress, String nasIP) {
    String[] cmd = {"sh", "-c",
        String.format("disconnect.sh %s %s %s %s", userName, ipAddress, nasIP,
            getNasSecret(nasIP))};

    String response = "";
    String data = null;
    try {
      Process exec = new ProcessBuilder(cmd).start();
      exec.waitFor();

      BufferedReader bfReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

      while ((response = bfReader.readLine()) != null) {
        if (response.contains("rad_recv")) {
          data = response;
        }
      }
    } catch (IOException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    } catch (InterruptedException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    if (data == null) {
      data = "nemaaa";
    }
    return data;
  }

  String getNasSecret(String nasIP) {
    String secret = "";
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT  secret from nas WHERE nasname=?";
    try {
      ps = db.connRad.prepareStatement(query);
      ps.setString(1, nasIP);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        secret = rs.getString("secret");
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return secret;
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

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }


}
