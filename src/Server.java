import classes.ClientWorker;
import classes.SchedullerTask;
import classes.database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Created by zoom on 8/8/16.
 */
public class Server {
    Logger LOGGER = LogManager.getLogger("MAIN");

    public static void main(String[] args) {

        Boolean DEBUG = false;
        String query;
        PreparedStatement ps;

        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);

            if (args[i].contains("debug=1")) {
                DEBUG = true;
            }
        }

        database db;
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket = null;

        int portNumber = 8543;

        try {
            serverSocket = new ServerSocket(portNumber);

            //TODO Create secure connection
            //  serverSocket = ssf.createServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = new database();
        query = "UPDATE onlineOperaters SET online=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        SchedullerTask st;
        st = new SchedullerTask(1);
        st.db = db;

        st.DEBUG = DEBUG;
        Thread scheduller = new Thread(st);

        scheduller.start();

        while (true) {
            ClientWorker cw;

            try {
                //Scheduler tasks timout in minutes


                cw = new ClientWorker(serverSocket.accept());
                cw.DEBUG = DEBUG;
                Thread th = new Thread(cw);
                th.start();
                st.add_client(cw);
                st.add_thread_clientWorker(th);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
