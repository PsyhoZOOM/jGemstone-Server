package net.yuvideo.jgemstone.server.classes.ServerServices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import net.yuvideo.jgemstone.server.classes.LOCATION.LocationsClients;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class GPSReceiver implements Runnable {

  DatagramSocket serverSock;
  byte[] recvData;
  database db;
  double longitude;
  double latitude;
  String ident;

  @Override
  public void run() {
    recvData = new byte[128];

    try {
      serverSock = new DatagramSocket((8544));
    } catch (SocketException e) {
      e.printStackTrace();
    }
    DatagramPacket datagramPacket = new DatagramPacket(recvData, recvData.length);
      try {
        System.out.println("RECEIVING UDP DATA: \n");
        serverSock.receive(datagramPacket);
        byte[] data = datagramPacket.getData();
        JSONObject obje = new JSONObject(new String(data));
        String a = new String(data);
        System.out.println("RECIEVED: " + data);
        System.out.println("TO STRING: " + a);
        System.out.println("JSONOB: " + obje.toString());
        ident = obje.getString("identification");
        longitude = obje.getDouble("long");
        latitude = obje.getDouble("lat");
        LocationsClients.updateClient(obje, db);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  public void setDatabase(database database) {
    this.db = database;
  }

  public void close() {
    this.serverSock.close();
  }
}