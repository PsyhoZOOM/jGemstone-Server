package net.yuvideo.jgemstone.server.classes.ServerServices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import net.yuvideo.jgemstone.server.classes.NetworkDevices;
import net.yuvideo.jgemstone.server.classes.SNMP.GetWifiSignal;
import net.yuvideo.jgemstone.server.classes.SNMP.SNMPDevices;
import net.yuvideo.jgemstone.server.classes.database;
import org.apache.log4j.Logger;

public class WiFiTracker {

  String errorMSG;
  boolean error;
  Logger LOGGER = Logger.getLogger("WIFI_TRACKER");


  private ArrayList<NetworkDevices> networkDevices = new ArrayList<>();
  database db;

  public WiFiTracker() {
    this.db = new database();
    this.networkDevices = getSNMPDevices();
    LOGGER.info("UPDATING WIFI SNMP CLIENT DATA");
    Thread wifiTrackThread = new Thread() {
      @Override
      public void run() {
        super.run();
        updateDevicesToDb(networkDevices);
      }
    };
    wifiTrackThread.start();
  }

  private void updateDevicesToDb(ArrayList<NetworkDevices> networkDevices) {
    PreparedStatement ps;
    String query;

    try {
      for (NetworkDevices device : networkDevices) {
        GetWifiSignal getWifiSignal = new GetWifiSignal();
        ArrayList<SNMPDevices> snmpDev = getWifiSignal
            .getMACDevices(device.getHostName(), device.getIp(), device.getPass(), "v1");
        for (SNMPDevices snmpDevice : snmpDev) {
          boolean existMac = check_if_exist_signal(snmpDevice.getMac());
          if (existMac) {
            query = "UPDATE wifiTracker SET mac =?,hostIP=?,  hostAp=?, wifiSignal=?, lastTimeSeen=?, lastUpdated=? WHERE mac =?; ";
          } else {
            query = "INSERT INTO wifiTracker (mac, hostIP, hostAP, wifiSignal, lastTimeSeen, lastUpdated) VALUES (?,?,?,?,?,?); ";

          }
          ps = db.conn.prepareStatement(query);
          ps.setString(1, snmpDevice.getMac());
          ps.setString(2, snmpDevice.getHostAP());
          ps.setString(3, snmpDevice.getHostName());
          ps.setString(4, snmpDevice.getSignal());
          ps.setString(5, snmpDevice.getDateUpdated());
          ps.setString(6, LocalDateTime.now().toString());
          if (existMac) {
            ps.setString(7, snmpDevice.getMac());
          }
          ps.executeUpdate();
          ps.close();
        }
      }

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      LOGGER.error(e.getMessage());
      e.printStackTrace();
    }

    db.closeDB();

  }

  private boolean check_if_exist_signal(String mac) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT mac FROM wifiTracker WHERE mac=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, mac);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        ps.close();
        rs.close();
        return true;
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
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
