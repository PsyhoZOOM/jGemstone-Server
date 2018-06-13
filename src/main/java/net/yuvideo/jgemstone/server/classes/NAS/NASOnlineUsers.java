package net.yuvideo.jgemstone.server.classes.NAS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.MIKROTIK_API.MikrotikAPI;
import net.yuvideo.jgemstone.server.classes.NetworkDevices;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class NASOnlineUsers {

  private final database db;
  private JSONObject online = new JSONObject();

  public NASOnlineUsers(database db) {
    this.db = db;
  }

  public JSONObject getOnlineUsers(String nasIP) {
    NetworkDevices networkDevices = new NetworkDevices(db);
    JSONObject nasDevices = networkDevices.getNASDevices();
    MikrotikAPI mikrotikAPI = new MikrotikAPI();
    for (int i = 0; i < nasDevices.length(); i++) {
      JSONObject onlineUsers = mikrotikAPI
          .getOnlineUser("apiUser", "apiPass", nasDevices.getJSONObject(
              String.valueOf(i)).getString("ip"));
      addToOnline(onlineUsers, i);
    }
    mergeOnlineUsers();

    return online;

  }

  private void mergeOnlineUsers() {
    JSONObject onl = new JSONObject();
    int numOnline = 0;
    for (int i = 0; i < online.length(); i++) {
      JSONObject object = online.getJSONObject(String.valueOf(i));
      for (int z = 0; z < object.length(); z++) {
        onl.put(String.valueOf(numOnline), object.getJSONObject(String.valueOf(z)));
        numOnline++;
      }
    }
    online = onl;
  }

  private void addToOnline(JSONObject onlineUsers, int i) {
    online.put(String.valueOf(i), onlineUsers);
  }

}
