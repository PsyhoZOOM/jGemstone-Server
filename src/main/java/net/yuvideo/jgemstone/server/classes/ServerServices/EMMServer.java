package net.yuvideo.jgemstone.server.classes.ServerServices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.database;
import org.apache.log4j.Logger;
import org.omg.CORBA.TIMEOUT;

/**
 * Created by PsyhoZOOM on 9/21/17.
 */
public class EMMServer implements Runnable {

  public Logger LOGGER;
  public int DEBUG;
  DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private int timeout;
  private Thread EMMServerThread;
  private sendEMMUDP sendEmmUDP;
  private database db;
  private String query;
  private String host;
  private int port;

  public EMMServer(int timeout, database db, String host, int port) {
    this.timeout = timeout;
    this.db = db;
    this.host = host;
    this.port = port;
  }

  @Override
  public void run() {
    PreparedStatement ps = null;
    ResultSet rs = null;
    query = "SELECT * FROM DTVKartice ";

    sendEmmUDP = new sendEMMUDP(this.host, this.port);
    LOGGER.info("Starting EMM Service");
    while (true) {
      try {
        ps = db.conn.prepareStatement(query);
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          while (rs.next()) {
            if (DEBUG > 1) {
              System.out.println(
                  String.format
                      ("Send EMMUDP Packet to: %s:%d CARD_ID: %d Packet: %d",
                          this.host,
                          this.port,
                          rs.getInt("idKartica"), rs.getInt("paketID")));
            }
            if (rs.getInt("idKartica") == 0) {
              continue;
            }
            sendEmmUDP.send(
                rs.getInt("idKartica"),
                LocalDate.parse(rs.getDate("createDate").toString(), df),
                LocalDate.parse(rs.getDate("endDate").toString(), df),
                getCode(rs.getInt("paketID")),
                5
            );
            Thread.sleep(timeout/2);
          }
        }
        rs.close();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }
  }

  private int getCode(int paketID) {
    PreparedStatement ps = null;
    ResultSet rs;
    int PaketID=0;

    String query = "SELECT code from casPaket WHERE paketID=?";
    try {
      ps =db.conn.prepareStatement(query);
      ps.setInt(1, paketID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        rs.next();
        PaketID = rs.getInt("code");
      }


    } catch (SQLException e) {
      e.printStackTrace();
    }
    return PaketID;
  }
}
