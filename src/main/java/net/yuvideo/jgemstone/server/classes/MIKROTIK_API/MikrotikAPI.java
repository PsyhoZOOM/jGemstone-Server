package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.SocketFactory;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.database;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class MikrotikAPI {

  private String operName;
  private database db = null;

  private int id;
  private String name;
  private String ip;
  private String hostName;
  private String type;
  private String userName;
  private String pass;
  private String url;
  private String opis;
  private boolean nas;
  private String accessType;

  private boolean error;
  private String errorMSG;

  ArrayList<MikrotikAPI> mikrotikAPIArrayList = new ArrayList<>();
  private String sessionID;

  Logger LOGGER = Logger.getLogger("MikrotikAPI");
  private final int API_PORT = ApiConnection.DEFAULT_PORT;
  private final int API_TIMEOUT = 5000;
  private ApiConnection apiConnection;


  public MikrotikAPI(database db, String operName) {
    this.db = db;
    this.operName = operName;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM networkDevices WHERE type='Mikrotik' and accessType='API'";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          MikrotikAPI mikrotikAPI = new MikrotikAPI();
          mikrotikAPI.setId(rs.getInt("id"));
          mikrotikAPI.setName(rs.getString("name"));
          mikrotikAPI.setIp(rs.getString("ip"));
          mikrotikAPI.setHostName(rs.getString("hostName"));
          mikrotikAPI.setType(rs.getString("type"));
          mikrotikAPI.setUserName(rs.getString("userName"));
          mikrotikAPI.setPass(rs.getString("pass"));
          mikrotikAPI.setUrl(rs.getString("url"));
          mikrotikAPI.setOpis(rs.getString("opis"));
          mikrotikAPI.setNas(rs.getBoolean("nas"));
          mikrotikAPI.setAccessType(rs.getString("accessType"));
          mikrotikAPIArrayList.add(mikrotikAPI);
        }
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public MikrotikAPI() {

  }

  public void logout(MikrotikAPI mtDevice) {
    if (apiConnection.isConnected()) {
      try {
        apiConnection.close();
      } catch (ApiConnectionException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean login(MikrotikAPI mtDevice) {
    boolean isConnected = false;
    if (apiConnection != null) {
      isConnected = apiConnection.isConnected();
      if (isConnected) {
        return true;
      }
    }

    try {
      this.apiConnection = ApiConnection
          .connect(SocketFactory.getDefault(), mtDevice.getIp(), API_PORT, API_TIMEOUT);
      apiConnection.setTimeout(API_TIMEOUT);
      apiConnection.login(mtDevice.getUserName(), mtDevice.getPass());
    } catch (MikrotikApiException e) {
      e.printStackTrace();
      LOGGER.info(String.format("IP: %s - %s", ip, e.getMessage()));
    }

    if (isConnected) {
      isConnected = true;
    }

    return isConnected;

  }


  public MikrotikAPI getMtDev(String ip) {
    MikrotikAPI mtDevice = null;

    for (MikrotikAPI mtDev : mikrotikAPIArrayList) {
      if (mtDev.getIp().equals(ip)) {
        mtDevice = mtDev;
        break;
      }
    }

    return mtDevice;
  }


  public int getOnlineUsersCount() {
    String cmd = "/interface/pppoe-server/print";
    int count = 0;
    for (MikrotikAPI mtDev : mikrotikAPIArrayList) {
      login(mtDev);
      if (!apiConnection.isConnected()) {
        continue;
      }
      try {
        List<Map<String, String>> execute = apiConnection.execute(cmd);
        for (Map<String, String> exe : execute) {
          count++;
        }
      } catch (MikrotikApiException e) {
        e.printStackTrace();
      }
      logout(mtDev);
    }

    return count;
  }

  public JSONObject getAllUsers() {
    String cmd = "/interface/pppoe-server/print detail";
    JSONObject obj = new JSONObject();
    for (MikrotikAPI mtDev : mikrotikAPIArrayList) {
      login(mtDev);
      if (!apiConnection.isConnected()) {
        continue;
      }

      JSONObject object = new JSONObject();
      int i = 0;

      try {
        List<Map<String, String>> execute = apiConnection.execute(cmd);
        for (Map<String, String> response : execute) {
          object = new JSONObject();
          object.put("interfaceName", response.get("name"));
          object.put("service", response.get("service"));
          object.put("user", response.get("user"));
          object.put("uptime", response.get("uptime"));
          object.put("remoteMAC", response.get("remote-address"));
          object.put("nasIP", mtDev.getIp());
          object.put("nasName", mtDev.getName());
          UsersData usersData = new UsersData(db, getOperName());
          int user = usersData.getUserIDOfRadiusUserName(response.get("user"));
          object.put("userID", user);

          obj.put(String.valueOf(i), object);
          i++;

        }
      } catch (MikrotikApiException e) {
        e.printStackTrace();
      }
      logout(mtDev);
    }
    System.out.println(obj);
    return obj;
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPass() {
    return pass;
  }

  public void setPass(String pass) {
    this.pass = pass;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getOpis() {
    return opis;
  }

  public void setOpis(String opis) {
    this.opis = opis;
  }

  public boolean isNas() {
    return nas;
  }

  public void setNas(boolean nas) {
    this.nas = nas;
  }

  public String getAccessType() {
    return accessType;
  }

  public void setAccessType(String accessType) {
    this.accessType = accessType;
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

  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  public String getSessionID() {
    return sessionID;
  }


  public ArrayList<MikrotikAPI> getMikrotikAPIArrayList() {
    return mikrotikAPIArrayList;
  }

  public ApiConnection getApiConnection() {
    return apiConnection;
  }

  public void setApiConnection(ApiConnection apiConnection) {
    this.apiConnection = apiConnection;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }
}
