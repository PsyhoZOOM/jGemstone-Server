package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import javafx.print.Printer.MarginType;
import javax.net.SocketFactory;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.json.JSONObject;

public class MikrotikAPI {

  public JSONObject getOnlineUser(String login, String pass, String ip) {
    JSONObject onlineUsers = new JSONObject();
    try {
      ApiConnection con = ApiConnection
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
        onlineUsers.put(String.valueOf(i), user);
        i++;

      }
    } catch (MikrotikApiException e) {
      onlineUsers.put("ERROR", e.getMessage());
      e.printStackTrace();
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


    } catch (MikrotikApiException e) {
      e.printStackTrace();
    }
    return userStat;
  }
}
