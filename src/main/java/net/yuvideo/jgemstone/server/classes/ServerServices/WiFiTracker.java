package net.yuvideo.jgemstone.server.classes.ServerServices;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import net.yuvideo.jgemstone.server.classes.NetworkDevices;
import net.yuvideo.jgemstone.server.classes.SNMP.GetWifiSignal;
import net.yuvideo.jgemstone.server.classes.SNMP.SNMPDevices;
import net.yuvideo.jgemstone.server.classes.database;

public class WiFiTracker {

  String errorMSG;
  boolean error;

  private ArrayList<NetworkDevices> networkDevices = new ArrayList<>();
  database db;

  public WiFiTracker(database db) {
    this.db = db;
    this.networkDevices = getSNMPDevices();
    updateDevicesToDb(this.networkDevices);
  }

  private void updateDevicesToDb(ArrayList<NetworkDevices> networkDevices) {
    PreparedStatement ps;
    String query = "INSERT INTO wifiTracker (mac, ip, hostAP, signal, lastTimeSeen, lastUpdated)"
        + "VALUES (?,?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      for (NetworkDevices device : networkDevices) {
        GetWifiSignal getWifiSignal = new GetWifiSignal();
        ArrayList<SNMPDevices> snmpDev = getWifiSignal
            .getMACDevices(device.getIp(), device.getPass(), "v1");
        for (SNMPDevices snmpDevice : snmpDev) {
          ps.setString(1, snmpDevice.getMac());
          ps.setString(2, snmpDevice.getIp());
          ps.setString(3, snmpDevice.getHostAP());
          ps.setString(4, LocalDate.now().toString());
          ps.setString(5, LocalDate.now().toString());
          ps.executeUpdate();
        }
      }
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

  }


  private ArrayList<NetworkDevices> getSNMPDevices() {
    NetworkDevices networkDev = new NetworkDevices(this.db);
    ArrayList<NetworkDevices> netDevArr = networkDev.getAllDevices();
    for (NetworkDevices netDev : netDevArr) {
      if (netDev.getAccessType().equals("SNMP")) {
        networkDevices.add(netDev);
      }
    }
    return networkDevices;
  }


  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }
}
