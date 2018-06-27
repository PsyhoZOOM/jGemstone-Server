package net.yuvideo.jgemstone.server.classes.NAS;

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

  public JSONObject getOnlineUsers() {
    NetworkDevices networkDevices = new NetworkDevices(db);
    JSONObject nasDevices = networkDevices.getNASDevices();
    MikrotikAPI mikrotikAPI = new MikrotikAPI();

    int nasNo = 0;
    for (int i = 0; i < nasDevices.length(); i++) {
      JSONObject onlineUsers = mikrotikAPI
          .getOnlineUser(
              nasDevices.getJSONObject(String.valueOf(i)).getString("userName"),
              nasDevices.getJSONObject(String.valueOf(i)).getString("pass"),
              nasDevices.getJSONObject(String.valueOf(i)).getString("ip"),
              nasDevices.getJSONObject(String.valueOf(i)).getString("name"));
      if (onlineUsers != null) {
        addToOnline(onlineUsers, nasNo);
        nasNo++;
      }

    }
    mergeOnlineUsers();

    return online;

  }

  private void mergeOnlineUsers() {
    JSONObject onl = new JSONObject();
    int numOnline = 0;
    for (int i = 0; i < online.length(); i++) {
      System.out.println(online);
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
