package net.yuvideo.jgemstone.server.classes.MIKROTIK_API;

import java.util.ArrayList;

public class MTDevice {

  String hostName;
  String ip;
  String pppoeService;
  String[] interfaces;
  ArrayList<PPPoEInterfaces> ppPoEInterfaces;
  String[] vlans;


  boolean error;
  String errorMSG;

  public MTDevice(String ip) {
    this.ip = ip;
  }

  public MTDevice() {

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

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getPppoeService() {
    return pppoeService;
  }

  public void setPppoeService(String pppoeService) {
    this.pppoeService = pppoeService;
  }

  public String[] getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(String[] interfaces) {
    this.interfaces = interfaces;
  }

  public ArrayList<PPPoEInterfaces> getPpPoEInterfaces() {
    return ppPoEInterfaces;
  }

  public void setPpPoEInterfaces(
      ArrayList<PPPoEInterfaces> ppPoEInterfaces) {
    this.ppPoEInterfaces = ppPoEInterfaces;
  }

  public String[] getVlans() {
    return vlans;
  }

  public void setVlans(String[] vlans) {
    this.vlans = vlans;
  }
}
