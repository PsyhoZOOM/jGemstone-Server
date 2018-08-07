package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.util.ArrayList;
import org.json.JSONObject;

public class MTOnlinePPPoEClients {

  private String interfaceName;
  private String userName;
  private String serviceName;
  private String mtu;
  private String mru;
  private String remoteAddress;
  private String uptime;

  private ArrayList<MikrotikAPI> mtDevices = new ArrayList<>();


  public MTOnlinePPPoEClients() {
  }

  public MTOnlinePPPoEClients(
      ArrayList<MikrotikAPI> mtDevices) {
    this.mtDevices = mtDevices;

    getOnlineUsers();

  }

  private JSONObject getOnlineUsers() {
    return null;
  }


  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getMtu() {
    return mtu;
  }

  public void setMtu(String mtu) {
    this.mtu = mtu;
  }

  public String getMru() {
    return mru;
  }

  public void setMru(String mru) {
    this.mru = mru;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public String getUptime() {
    return uptime;
  }

  public void setUptime(String uptime) {
    this.uptime = uptime;
  }

  public ArrayList<MikrotikAPI> getMtDevices() {
    return mtDevices;
  }

  public void setMtDevices(
      ArrayList<MikrotikAPI> mtDevices) {
    this.mtDevices = mtDevices;
  }

}
