package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by PsyhoZOOM on 9/21/17.
 */
public class EMMServer implements Runnable {
    private final int MINUTES = 60 * 100;
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
        try {
            ps = db.conn.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        sendEmmUDP = new sendEMMUDP(this.host, this.port);
        while (true) {
            try {
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        sendEmmUDP.send(
                                rs.getInt("idKartica"),
                                LocalDate.parse(rs.getDate("createDate").toString(), df),
                                LocalDate.parse(rs.getDate("endDate").toString(), df),
                                rs.getInt("paketID"),
                                5

                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(timeout * MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
