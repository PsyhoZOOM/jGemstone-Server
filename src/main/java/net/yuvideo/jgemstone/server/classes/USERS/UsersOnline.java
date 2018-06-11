package net.yuvideo.jgemstone.server.classes.USERS;

import net.yuvideo.jgemstone.server.classes.NetworkDevices;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class UsersOnline {

  private final database db;

  public UsersOnline(database db) {
    this.db = db;
  }

  public JSONObject getUsersOnline() {
    JSONObject object = new JSONObject();
    NetworkDevices networkDevices = new NetworkDevices(db);
    JSONObject nasDevices = networkDevices.getNASDevices();

    return object;
  }

}
