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
  DatagramPacket datagramPacket;
  byte[] recvData;
  database db;

  @Override
  public void run() {
      recvData = new byte[128];

    while (true) {
      datagramPacket = new DatagramPacket(recvData, recvData.length);
      try {
        serverSock = new DatagramSocket((8544));
        System.out.println("RECEIVING UDP DATA: \n");
        serverSock.receive(datagramPacket);
        byte[] data = datagramPacket.getData();
        JSONObject obje = new JSONObject(new String(data));
        String a = new String(data);
        System.out.println("RECIEVED: " + data);
        System.out.println("TO STRING: " + a);
        System.out.println("JSONOB: " + obje.toString());
        LocationsClients.updateClient(obje, db);
        serverSock.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    }

  public void setDatabase(database database) {
    this.db = database;
  }

  public void close() {
    this.serverSock.close();
  }
}
