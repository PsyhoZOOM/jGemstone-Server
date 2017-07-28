import classes.ClientWorker;
import classes.SchedullerTask;
import classes.database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Created by zoom on 8/8/16.
 */
public class Server {
    Logger LOGGER = LogManager.getLogger("MAIN");

    public static void main(String[] args) {

        SSLServerSocket serverSock = null;
        SSLSocket socket = null;
        Boolean DEBUG = false;
        String query;
        PreparedStatement ps;
        int portNumber = 8543;

        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("debug=1")) {
                DEBUG = true;
            }
        }

        database db;

        System.out.println(System.getProperty("user.dir"));

        try {
            KeyStore serverKeys = KeyStore.getInstance("JKS");
            serverKeys.load(new FileInputStream("ssl/plainserver.jks"), "jgemstone".toCharArray());
            KeyManagerFactory serverKeyManager = KeyManagerFactory.getInstance("SunX509");
            serverKeyManager.init(serverKeys, "jgemstone".toCharArray());
            KeyStore clientPub = KeyStore.getInstance("JKS");
            clientPub.load(new FileInputStream("ssl/clientpub.jks"), "jgemstone".toCharArray());
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
            trustManager.init(clientPub);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(serverKeyManager.getKeyManagers(), trustManager.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
            serverSock = (SSLServerSocket) ssl.getServerSocketFactory().createServerSocket(portNumber);
            //socket = (SSLSocket) serverSock.accept();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        /*
        //OLDY NON CRYPT
        ServerSocket serverSocket = null;
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        try {
            //nonSecure
            serverSocket = new ServerSocket(portNumber);

            //TODO Create secure connection
            // serverSocket = ssf.createServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //end of non crpted
        */


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


                cw = new ClientWorker((SSLSocket) serverSock.accept());
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
