package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.SocketFactory;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class MikrotikAPI {

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


  public MikrotikAPI(database db) {
    this.db = db;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM networkDevices WHERE type='Mikrotik'";
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
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public MikrotikAPI() {

  }


  public boolean checkUserOnline(String username) {
    boolean isOnline = false;
    for (MikrotikAPI mt : mikrotikAPIArrayList) {
      try {
        ApiConnection apiConnection = ApiConnection
            .connect(SocketFactory.getDefault(), mt.getIp(), ApiConnection.DEFAULT_PORT,
                ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
        apiConnection.login(mt.getUserName(), mt.getPass());
        if (!apiConnection.isConnected()) {
          setError(true);
          setErrorMSG(String.format("Nije moguce povezivanje sa NAS-om %s", mt.getIp()));
        } else {
          List<Map<String, String>> execute = apiConnection.execute("/ppp/active/print");
          apiConnection.close();
          int i = 0;
          for (Map<String, String> response : execute) {
            if (response.get("name").equals(username)) {
              setSessionID(response.get("session-id"));
              return true;
            }
          }
        }
      } catch (MikrotikApiException e) {
        setErrorMSG(e.getMessage());
        setError(true);
        e.printStackTrace();
      }

    }
    return false;


  }

  public JSONObject checkUsersOnline(String userName) {
    JSONObject users = new JSONObject();
    for (MikrotikAPI mikrotikAPI : mikrotikAPIArrayList) {
      try {

        ApiConnection apiConnection = ApiConnection
            .connect(SocketFactory.getDefault(), mikrotikAPI.getIp(),
                ApiConnection.DEFAULT_PORT, ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
        apiConnection.login(mikrotikAPI.getUserName(), mikrotikAPI.getPass());
        if (!apiConnection.isConnected()) {
          setErrorMSG(String.format("Nije Moguce povezivanje sa NAS-om ", mikrotikAPI.getIp()));
        } else {
          List<Map<String, String>> execute = apiConnection.execute("/ppp/active/print detail");
          for (Map<String, String> res : execute) {
            if (res.get("name").equals(userName) || res.get("name")
                .contains(String.format("%s-", userName))) {
              JSONObject userData = new JSONObject();
              userData.put("userName", res.get("name"));
              userData.put("service", res.get("service"));
              userData.put("callerID", res.get("caller-id"));
              userData.put("address", res.get("address"));
              userData.put("uptime", res.get("uptime"));
              userData.put("sessionID", res.get("session-id"));
              userData.put("NASIP", mikrotikAPI.getIp());
              userData.put("NASName", mikrotikAPI.getName());
              userData.put("identification", res.get("name"));
              userData.put("userStats",
                  getUserStats(res.get("address"), mikrotikAPI.getUserName(), mikrotikAPI.getPass(),
                      mikrotikAPI.getIp()));
              users.put(res.get("name"), userData);

            }
          }
        }
      } catch (MikrotikApiException e) {
        setError(true);
        setErrorMSG(e.getMessage());
      }

    }
    return users;
  }


  public JSONObject getOnlineUser(String login, String pass, String ip, String NASName) {
    JSONObject onlineUsers = new JSONObject();
    ApiConnection con = null;



    try {
      con = ApiConnection
          .connect(SocketFactory.getDefault(), ip, ApiConnection.DEFAULT_PORT,
              ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
      con.login(login, pass);
      if (!con.isConnected()) {
        return null;
      }
      List<Map<String, String>> execute = con.execute("/ppp/active/print detail");
      con.close();
      int i = 0;
      for (Map<String, String> res : execute) {
        JSONObject user = new JSONObject();
        user.put("name", res.get("name"));
        user.put("ip", res.get("address"));
        user.put("MAC", res.get("caller-id"));
        user.put("service", res.get("service"));
        user.put("uptime", res.get("uptime"));
        user.put("sessionID", res.get("session-id"));
        user.put("nasIP", ip);
        user.put("NASName", NASName);
        onlineUsers.put(String.valueOf(i), user);
        i++;

      }
      if (con.isConnected()) {
        con.close();
      }
    } catch (MikrotikApiException e) {

      e.printStackTrace();
      onlineUsers.put("ERROR", e.getMessage());
      return null;
    }
    return onlineUsers;

  }

  public JSONObject getUserStats(String ip, String login,
      String pass, String nasIP) {
    JSONObject userStat = new JSONObject();
    try {
      ApiConnection con = ApiConnection
          .connect(SocketFactory.getDefault(), nasIP, ApiConnection.DEFAULT_PORT,
              ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
      con.login(login, pass);

      String cmd;
      cmd = String.format("/ip/address/print where network=%s", ip);
      List<Map<String, String>> execute = con.execute(cmd);
      String anInterface = "";
      for (Map<String, String> sr : execute) {
        anInterface = sr.get("interface");
      }

      cmd = String.format("/interface/print detail ", anInterface);
      execute = con.execute(cmd);
      con.close();

      for (Map<String, String> res : execute) {
        if (!res.get("name").equals(anInterface)) {
          continue;
        }
        userStat.put("name", res.get("name"));
        userStat.put("linkUp", res.get("last-link-up-time"));
        userStat.put("rxByte", res.get("rx-byte"));
        userStat.put("txByte", res.get("tx-byte"));
        userStat.put("rxError", res.get("rx-error"));
        userStat.put("txError", res.get("tx-error"));
        userStat.put("NAS", nasIP);
        userStat.put("name", anInterface);
      }
      if (!userStat.has("name")) {
        userStat.put("USER_OFFLINE", "");
      }


    } catch (MikrotikApiException e) {
      userStat.put("ERROR", e.getMessage());
      e.printStackTrace();
    }
    return userStat;
  }

  public JSONObject customCommand(String cmd, String nasIP, String user, String pass) {
    JSONObject object = new JSONObject();
    try {
      ApiConnection apiConnection = ApiConnection
          .connect(SocketFactory.getDefault(), nasIP, ApiConnection.DEFAULT_PORT,
              ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
      apiConnection.login(user, pass);

      String a = apiConnection.execute(cmd,
          new ResultListener() {
            @Override
            public void receive(Map<String, String> result) {

              JSONObject keys = new JSONObject();
              int i = 0;
              for (String key : result.keySet()) {
                keys.put(key, result.get(key));
              }
              object.put(String.valueOf(i), keys);
              i++;


            }

            @Override
            public void error(MikrotikApiException ex) {
              ex.printStackTrace();
            }

            @Override
            public void completed() {

            }
          });


      /*
      List<Map<String, String>> execute = apiConnection.execute(cmd);
      int i=0;
      for (Map<String, String> res : execute){

        JSONObject keys = new JSONObject();
        for (String key : res.keySet()){
          keys.put(key, res.get(key));
        }
        object.put(String.valueOf(i), keys);
        i++;



    }
    */
    } catch (MikrotikApiException e) {
      e.printStackTrace();
    }

    return object;
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

}
