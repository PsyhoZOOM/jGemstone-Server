package net.yuvideo.jgemstone.server;

import net.yuvideo.jgemstone.server.classes.ClientWorker;
import net.yuvideo.jgemstone.server.classes.EMMServer;
import net.yuvideo.jgemstone.server.classes.SchedullerTask;
import net.yuvideo.jgemstone.server.classes.database;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;


/**
 * Created by zoom on 8/8/16.
 */
public class Server {

	Logger LOGGER = Logger.getLogger("MAIN");

	public static void main(String[] args) {

		SSLServerSocket serverSock = null;
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


		//SSL SOCKET INIT
		try {
			KeyStore serverKeys = KeyStore.getInstance("JKS");
			FileInputStream fin;
			serverKeys.load(ClassLoader.getSystemResourceAsStream("ssl/plainserver.jks"), "jgemstone".toCharArray());
			KeyManagerFactory serverKeyManager = KeyManagerFactory.getInstance("SunX509");
			serverKeyManager.init(serverKeys, "jgemstone".toCharArray());
			KeyStore clientPub = KeyStore.getInstance("JKS");
			clientPub.load(ClassLoader.getSystemResourceAsStream("ssl/clientpub.jks"), "jgemstone".toCharArray());
			TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
			trustManager.init(clientPub);
			SSLContext ssl = SSLContext.getInstance("TLS");
			ssl.init(serverKeyManager.getKeyManagers(), trustManager.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
			serverSock = (SSLServerSocket) ssl.getServerSocketFactory().createServerSocket(portNumber);

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

		//ONLINE OPERS OFFLINE
		db = new database();
		query = "UPDATE onlineOperaters SET online=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, 0);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//SCHEDULER MONTHLY ..
		SchedullerTask st;
		st = new SchedullerTask(1);
		st.db = db;

		st.DEBUG = DEBUG;
		Thread scheduller = new Thread(st);
		scheduller.start();

		//EMM SEND UDP
		query = "SELECT settings, value FROM settings";
		ResultSet rs;
		String EMMhost = "127.0.0.1";
		int EMMport = 10000;
		int EMMTimeout = 1000;
		try {
			ps = db.conn.prepareStatement(query);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					if (rs.getString("settings").equals("DTV_UDP_TIMEOUT")) {
						EMMTimeout = Integer.valueOf(rs.getString("value"));
					}
					if (rs.getString("settings").equals("DTV_EMM_HOST")) {
						EMMhost = rs.getString("value");
					}
					if (rs.getString("settings").equals("DTV_EMM_PORT")) {
						EMMport = Integer.valueOf(rs.getString("value"));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		EMMServer emmServer = new EMMServer(EMMTimeout, db, EMMhost, EMMport);
		Thread emmThread = new Thread(emmServer);
		emmThread.start();

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
