package classes;

import JGemstone.classes.monthlyScheduler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by zoom on 9/8/16.
 */
public class SchedullerTask implements Runnable {
    //final int MINUTES = (60 * 1000); //for normal use
    final int MINUTES = (60 * 100); //for debuging use
    public int timeout;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    ArrayList<ClientWorker> clientWorkerArrayList = new ArrayList<ClientWorker>();
    ClientWorker clientWorker;
    ResultSet rs;
    String check_date;
    private boolean service_run = true;
    private Thread threadClientWorker;
    private database db = new database();
    private String query;
    private monthlyScheduler monthlyScheduler = new monthlyScheduler();
    public Boolean DEBUG;
    private Logger LOGGER = LogManager.getLogger();
    private String info;


    public SchedullerTask(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void run() {



        while (true) {
            check_scheduler_tasks();
            show_clients();

            try {
                Thread.sleep(timeout * MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void check_scheduler_tasks() {
        format = new SimpleDateFormat("yyyy-MM-01");
        check_date = format.format(new Date());
        query = String.format("SELECT value FROM scheduler WHERE name='user_debts' AND value ='%s'", check_date);
        rs = db.query_data(query);

        try {
            if (!rs.next()) {
                monthlyScheduler.monthlyScheduler();
            }

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
            if (DEBUG) {
                LOGGER.log(Level.TRACE, info);
            }
            if (clientWorker.get_socket().isClosed()) {
                query = String.format("UPDATE online_opers SET online='0' WHERE uniq_id='%d'",
                        clientWorkerArrayList.get(i).hashCode());
                db.query = query;
                db.executeUpdate();
                service_run = false;
                close_client_socket(i);

            } else {
                if (clientWorkerArrayList.get(i).client_db_update == false) {
                    SimpleDateFormat datum_oper = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    db.query = String.format("INSERT online_opers SET username='%s', remote_address='%s', date='%s', " +
                                    "online='%d', uniq_id='%d', array_id='%d'",
                            clientWorkerArrayList.get(i).getOperName(),
                            clientWorkerArrayList.get(i).get_socket().getRemoteSocketAddress(),
                            datum_oper.format(new Date()), 1, clientWorkerArrayList.get(i).hashCode(), i

                    );

                    db.executeUpdate();
                    clientWorkerArrayList.get(i).client_db_update = true;
                }
            }
        }
    }

    private void close_client_socket(int i) {
        System.out.println(String.format("Client %s IP: %s disconected..",
                clientWorkerArrayList.get(i).getOperName(),
                clientWorkerArrayList.get(i).get_socket().getInetAddress()).replaceFirst("/", ""));
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
