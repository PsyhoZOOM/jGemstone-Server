package net.yuvideo.jgemstone.server.classes.SNMP;

public class SNMPDevices {

  private String hostName;
  private String ip;
  private String mac;
  private String signal;
  private String hostAP;
  private String dateUpdated;

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

  public String getMac() {
    return mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public String getSignal() {
    return signal;
  }

  public void setSignal(String signal) {
    this.signal = signal;
  }

  public String getHostAP() {
    return hostAP;
  }

  public void setHostAP(String hostAP) {
    this.hostAP = hostAP;
  }

  public String getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(String dateUpdated) {
    this.dateUpdated = dateUpdated;
  }
}
