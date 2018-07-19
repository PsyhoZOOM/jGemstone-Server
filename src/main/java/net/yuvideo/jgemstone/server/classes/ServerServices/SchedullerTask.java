package net.yuvideo.jgemstone.server.classes.ServerServices;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import net.yuvideo.jgemstone.server.classes.ClientWorker;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.monthlyScheduler;
import org.apache.log4j.Logger;


/**
 * Created by zoom on 9/8/16.
 */
public class SchedullerTask implements Runnable {

  private static final Logger LOGGER = Logger.getLogger("SCHEDULER_TASK");
  //final int MINUTES = (60 * 1000); //for normal use
  final int MINUTES = (1000); //for debuging use
  public int timeout;
  public database db;
  public int DEBUG;
  public int DEBUG_VAL;
  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  Date date;
  ArrayList<ClientWorker> clientWorkerArrayList = new ArrayList<ClientWorker>();
  ClientWorker clientWorker;
  ResultSet rs;
  String check_date;
  private boolean service_run = true;
  private Thread threadClientWorker;
  private PreparedStatement ps;
  private String query;
  private net.yuvideo.jgemstone.server.classes.monthlyScheduler monthlyScheduler;
  private String info;


  public SchedullerTask(int timeout) {
    this.timeout = timeout;
  }

  @Override
  public void run() {

    while (true) {
      LocalDateTime dateTime = LocalDateTime.now();
      check_scheduler_tasks();

      //show connected clients
      if (DEBUG_VAL == 2) {
        show_clients();
      }

      System.out.println(dateTime.getSecond());
      //run every half hour
      if (dateTime.getSecond() == 1) {
        WiFiTracker wiFiTracker = new WiFiTracker(db);
        if (wiFiTracker.isError()) {
          LOGGER.error(wiFiTracker.getErrorMSG());
        }
      }

      try {
        Thread.sleep(timeout * MINUTES);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void check_scheduler_tasks() {
    monthlyScheduler = new monthlyScheduler();
    monthlyScheduler.db = db;
    format = new SimpleDateFormat("yyyy-MM-01");
    check_date = format.format(new Date());
    query = "SELECT date FROM scheduler WHERE name=? AND date =? ";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, "user_debts");
      ps.setString(2, check_date);
      rs = ps.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      if (!rs.isBeforeFirst()) {
        monthlyScheduler.monthlyScheduler();

        query = "INSERT INTO scheduler (name, date) VALUES " +
            "(?,?)";

        try {
          ps = db.conn.prepareStatement(query);
          ps.setString(1, "user_debts");
          ps.setString(2, check_date);
          ps.executeUpdate();
        } catch (SQLException e) {
          e.printStackTrace();
        }

      }
      rs.close();
      ps.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void show_clients() {
    System.out.println("Ukupno klijenta: " + clientWorkerArrayList.size());
    for (int i = 0; i < clientWorkerArrayList.size(); i++) {
      info = String.format("\n************U S E R  I N F O************" +
              "\nUser Name: %s, \nIP: %s, \nUID: %s" +
              "\n========================================",
          clientWorkerArrayList.get(i).getOperName(),
          clientWorkerArrayList.get(i).get_socket().getInetAddress(),
          clientWorkerArrayList.get(i).hashCode()
      );
      if (DEBUG > 1) {
        LOGGER.info(info);
      }
      if (clientWorker.get_socket().isClosed()) {
        query = "UPDATE onlineOperaters SET online=0 WHERE uniqueID=?";
        try {
          ps = db.conn.prepareStatement(query);
          ps.setInt(1, clientWorkerArrayList.get(i).hashCode());
          ps.executeUpdate();
          ps.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        service_run = false;
        close_client_socket(i);

      } else {
        if (clientWorkerArrayList.get(i).client_db_update == false) {
          SimpleDateFormat datum_oper = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          query =
              "INSERT INTO onlineOperaters (username, remote_address, date, online, uniqueID, arrayID) "
                  +
                  "VALUES " +
                  "(?,?,?,?,?,?)";
          try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, clientWorkerArrayList.get(i).getOperName());
            ps.setString(2,
                clientWorkerArrayList.get(i).get_socket().getRemoteSocketAddress().toString());
            ps.setString(3, datum_oper.format(new Date()));
            ps.setInt(4, 1);
            ps.setInt(5, clientWorkerArrayList.get(i).hashCode());
            ps.setInt(6, i);
            ps.executeUpdate();
            ps.close();


          } catch (SQLException e) {
            e.printStackTrace();
          }

          clientWorkerArrayList.get(i).client_db_update = true;
        }
      }
    }
  }

  private void close_client_socket(int i) {
    System.out.println(String.format("Client %s IP: %s disconected..",
        clientWorkerArrayList.get(i).getOperName(),
        clientWorkerArrayList.get(i).get_socket().getInetAddress()).replaceFirst("/", ""));
    try {
      clientWorkerArrayList.get(i).get_socket().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    clientWorkerArrayList.remove(i);
    threadClientWorker.interrupt();
  }

  public void add_client(ClientWorker client) {
    clientWorker = client;
    clientWorkerArrayList.add(clientWorker);
  }

  public void add_thread_clientWorker(Thread th) {
    this.threadClientWorker = th;
  }
}
