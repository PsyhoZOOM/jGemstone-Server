package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.util.ArrayList;

public class PPPoEInterfaces {

  String serviceName;
  ArrayList<PPPoEClients> ppPoEClients;
  String clentsOnline;

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public ArrayList<PPPoEClients> getPpPoEClients() {
    return ppPoEClients;
  }

  public void setPpPoEClients(
      ArrayList<PPPoEClients> ppPoEClients) {
    this.ppPoEClients = ppPoEClients;
  }

  public String getClentsOnline() {
    return clentsOnline;
  }

  public void setClentsOnline(String clentsOnline) {
    this.clentsOnline = clentsOnline;
  }
}
