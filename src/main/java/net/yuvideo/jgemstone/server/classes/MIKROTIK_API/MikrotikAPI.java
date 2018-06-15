package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.util.List;
import java.util.Map;
import javax.net.SocketFactory;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import org.json.JSONObject;

public class MikrotikAPI {

  public JSONObject getOnlineUser(String login, String pass, String ip, String NASName) {
    JSONObject onlineUsers = new JSONObject();
    ApiConnection con = null;



    try {
      con = ApiConnection
          .connect(SocketFactory.getDefault(), ip, ApiConnection.DEFAULT_PORT,
              ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
      con.login(login, pass);
      List<Map<String, String>> execute = con.execute("/ppp/active/print detail");
      con.close();
      int i = 0;
      for (Map<String, String> res : execute) {
        System.out.println(res);
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
      System.out.println(cmd);
      List<Map<String, String>> execute = con.execute(cmd);
      String anInterface = "";
      for (Map<String, String> sr : execute) {
        System.out.println(sr);
        anInterface = sr.get("interface");
      }

      cmd = String.format("/interface/print detail ", anInterface);
      System.out.println(cmd);
      execute = con.execute(cmd);
      con.close();

      for (Map<String, String> res : execute) {
        if (!res.get("name").equals(anInterface)) {
          continue;
        }
        System.out.println(res);
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
    System.out.println(cmd);
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

              System.out.println(result);
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
              System.out.println("COMPLETE");
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
}
