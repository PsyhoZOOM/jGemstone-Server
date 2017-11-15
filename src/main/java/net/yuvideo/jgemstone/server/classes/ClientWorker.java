package net.yuvideo.jgemstone.server.classes;

import com.csvreader.CsvReader;
import net.yuvideo.jgemstone.server.classes.BOX.addBoxService;
import net.yuvideo.jgemstone.server.classes.DTV.DTVFunctions;
import net.yuvideo.jgemstone.server.classes.FIX.FIXFunctions;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.IPTVFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.StalkerRestAPI2;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import org.json.JSONObject;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by PsyhoZOOM on 8/8/16.
 */
public class ClientWorker implements Runnable {

    private static final DecimalFormat df = new DecimalFormat("#.##");
    public boolean DEBUG = false;
    public boolean client_db_update = false;
    private static final Logger LOGGER = Logger.getLogger("CLIENT");
    //private Socket client;
    private SSLSocket client;
    private InputStreamReader Isr = null;
    private OutputStreamWriter Osw = null;
    private BufferedReader Bfr;
    private BufferedWriter Bfw;
    private database db;
    private ResultSet rs;
    private PreparedStatement ps;
    private String query;
    private String operName;
    private boolean client_authenticated = false;
    private Date date;
    private final SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
    private final SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat formatMonthDate = new SimpleDateFormat("yyyy-MM");

    //JSON Grupa
    ///JSON PART
    private JSONObject jObj;
    private JSONObject jGrupe;
    //JSON Users
    private JSONObject jUsers;

    public ClientWorker(SSLSocket client) {
        //this.client = client;
        this.client = client;
    }

    public Socket get_socket() {
        return this.client;
    }

    @Override
    public void run() {

        db = new database();
        db.DEBUG = DEBUG;

        LOGGER.log(Level.INFO, String.valueOf(DEBUG));

        System.out.println(String.format("Client connected: %s", this.client
                .getRemoteSocketAddress()));
        while (!client.isClosed()) {

            if (Isr == null) {
                try {
                    Isr = new InputStreamReader(client.getInputStream());
                    Bfr = new BufferedReader(Isr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Waitin for client data..");
            try {

//unencripted
                String A = Bfr.readLine();

                if (A == null) {
                    client.close();
                    break;
                }

                jObj = new JSONObject(A);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Reading Bfr.readline()" + jObj);
            object_worker(jObj);

        }
    }

    private void close_database() {
        db.closeDatabase();

    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    private void object_worker(JSONObject rLine) {

        if (Osw == null) {
            try {
                Osw = new OutputStreamWriter(client.getOutputStream());
                Bfw = new BufferedWriter(Osw);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (rLine.get("action").equals("login")) {
            LOGGER.info("LOGIN CRED: " + rLine);
            client_authenticated = check_Login(rLine.getString("username"), rLine.getString("password"));
            this.operName = rLine.get("username").toString();
            if (client_authenticated) {
                jObj = new JSONObject();
                jObj.put("Message", "LOGIN_OK");
                send_object(jObj);
                return;
            }
        }

        if (!client_authenticated) {

            jObj = new JSONObject();
            jObj.put("Message", "LOGIN_FAILED");
            send_object(jObj);
            try {
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (rLine.get("action").equals("checkPing")) {
            jObj = new JSONObject();
            jObj.put("PONG", "PONG");
            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("get_users")) {
            query = "SELECT * FROM users WHERE  ime LIKE ? or id LIKE ? or jBroj LIKE ?  ";
            String userSearch;
            if (!rLine.has("username")) {
                userSearch = "%";
            } else {
                userSearch = "%" + rLine.getString("username") + "%";
            }
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, userSearch);
                ps.setString(2, userSearch);
                ps.setString(3, userSearch);
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            LOGGER.info(ps.toString());
            jUsers = new JSONObject();

            int i = 0;
            try {
                while (rs.next()) {

                    try {
                        jObj = new JSONObject();
                        jObj.put("id", rs.getInt("id"));
                        jObj.put("fullName", rs.getString("ime"));
                        jObj.put("mesto", rs.getString("mesto"));
                        jObj.put("adresa", (rs.getString("adresa")));
                        jObj.put("adresaUsluge", rs.getString("adresaUsluge"));
                        jObj.put("mestoUsluge", rs.getString("mestoUsluge"));
                        jObj.put("brLk", rs.getString("brlk"));
                        jObj.put("datumRodjenja", rs.getString("datumRodjenja"));
                        jObj.put("telFixni", rs.getString("telFiksni"));
                        jObj.put("telMobilni", rs.getString("telMobilni"));
                        jObj.put("JMBG", rs.getString("JMBG"));
                        jObj.put("komentar", rs.getString("komentar"));
                        jObj.put("postBr", rs.getString("postbr"));
                        jObj.put("jBroj", rs.getString("jBroj"));
                        jObj.put("jAdresa", rs.getString("jAdresa"));
                        jObj.put("jAdresaBroj", rs.getString("jAdresaBroj"));
                        jObj.put("jMesto", rs.getString("jMesto"));
                        jObj.put("dug", df.format(get_userDebt(rs.getInt("id"))));
                        jObj.put("firma", rs.getBoolean("firma"));
                        jUsers.put(String.valueOf(i), jObj);
                        i++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jUsers);
            return;
        }

        if (rLine.get("action").equals("get_user_data")) {
            query = "SELECT * FROM users WHERE id=?";
            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userId"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("fullName", rs.getString("ime"));
                    jObj.put("datumRodjenja", rs.getString("datumRodjenja"));
                    jObj.put("adresa", rs.getString("adresa"));
                    jObj.put("mesto", rs.getString("mesto"));
                    jObj.put("postBr", rs.getString("postBr"));
                    jObj.put("telFix", rs.getString("telFiksni"));
                    jObj.put("telMob", rs.getString("telMobilni"));
                    jObj.put("brLk", rs.getString("brLk"));
                    jObj.put("JMBG", rs.getString("JMBG"));
                    jObj.put("mestoUsluge", rs.getString("mestoUsluge"));
                    jObj.put("adresaUsluge", rs.getString("adresaUsluge"));
                    jObj.put("komentar", rs.getString("komentar"));
                    jObj.put("jMesto", rs.getString("jMesto"));
                    jObj.put("jAdresa", rs.getString("jAdresa"));
                    jObj.put("jAdresaBroj", rs.getString("jAdresaBroj"));
                    jObj.put("jBroj", rs.getString("jBroj"));

                } else {
                    jObj.put("Message", "NO_SUCH_USER");
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("get_groups")) {
            query = String.format("SELECT * FROM grupa WHERE groupname LIKE '%s%%'", rLine.get("groupName"));
            query = "SLEECT * FROM  grupa WHERE groupname LIKE ?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("groupName"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jGrupe = new JSONObject();
            int b = 0;
            try {

                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("br", b);
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("groupname", rs.getString("groupname"));
                    jObj.put("cena", rs.getString("cena"));
                    jObj.put("prepaid", rs.getInt("prepaid"));
                    jObj.put("opis", rs.getString("opis"));
                    jGrupe.put(String.valueOf(b), jObj);
                    b++;
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jGrupe);

            return;
        }

        if (rLine.get("action").equals("get_group_data")) {
            query = "SELECT * FROM grupa WHERE id LIKE ?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("groupId"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("groupName", rs.getString("groupName"));
                    jObj.put("cena", rs.getString("cena"));
                    jObj.put("prepaid", rs.getInt("prepaid"));
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("save_group_data")) {
            query = "UPDATE grupa SET groupname=?, cena=?, prepaid=?, opis=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("groupName"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setInt(3, rLine.getInt("prepaid"));
                ps.setString(4, rLine.getString("opis"));
                ps.setInt(5, rLine.getInt("groupId"));
                ps.executeQuery();

                ps.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("message", "SAVED");
            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("delete_group")) {

            query = "DELETE FROM grupa WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("groupID"));
                ps.executeQuery();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("message", String.format("GRUPA IZBRISANA"));

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("save_group")) {

            query = "INSERT INTO grupa ('groupname', 'cena', 'prepaid', 'opis') VALUES (?,?,?,?,)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("groupName"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setInt(3, rLine.getInt("prepaid"));
                ps.setString(4, rLine.getString("opis"));
                ps.executeQuery();

                ps.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("message", String.format("GROUP %s SAVED", rLine.getString("groupName")));
            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("update_user")) {
            update_user(rLine);
        }

        if (rLine.get("action").equals("setUserFirma")) {
            jObj = new JSONObject();
            boolean userHasFirma = false;
            PreparedStatement ps;
            ResultSet rs;
            String query = "SELECT * from firme WHERE userID=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) userHasFirma = true;

                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (userHasFirma) {
                query = "UPDATE firme SET " +
                        "nazivFirme=?, " +
                        "kodBanke=?, " +
                        "kontaktOsoba=?, " +
                        "PIB=?, " +
                        "maticniBroj=?, " +
                        "tekuciRacun=? " +
                        "WHERE userID=?";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setString(1, rLine.getString("nazivFirme"));
                    ps.setString(2, rLine.getString("kodBanke"));
                    ps.setString(3, rLine.getString("kontaktOsoba"));
                    ps.setString(4, rLine.getString("pib"));
                    ps.setString(5, rLine.getString("maticniBroj"));
                    ps.setString(6, rLine.getString("tekuciRacun"));
                    ps.setInt(7, rLine.getInt("userID"));
                    ps.executeUpdate();
                    ps.close();
                    setUserFirma(rLine.getInt("userID"), true);
                    jObj.put("MESSAGE", "FIRMA_EDIT_SAVE");

                } catch (SQLException e) {
                    jObj.put("ERROR", e.getMessage());
                    e.printStackTrace();

                }

            } else {
                query = "INSERT INTO firme " +
                        "(userID, nazivFirme, kontaktOsoba, kodBanke, PIB, maticniBroj, " +
                        "tekuciRacun) " +
                        "VALUES " +
                        "(?,?,?,?,?,?,?)";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setInt(1, rLine.getInt("userID"));
                    ps.setString(2, rLine.getString("nazivFirme"));
                    ps.setString(3, rLine.getString("kontaktOsoba"));
                    ps.setString(4, rLine.getString("kodBanke"));
                    ps.setString(5, rLine.getString("pib"));
                    ps.setString(6, rLine.getString("maticniBroj"));
                    ps.setString(7, rLine.getString("tekuciRacun"));
                    ps.executeUpdate();
                    ps.close();
                    setUserFirma(rLine.getInt("userID"), true);
                    jObj.put("MESSAGE", "FIRMA_SAVED");

                } catch (SQLException e) {
                    jObj.put("ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
            send_object(jObj);
        }

        if (rLine.getString("action").equals("getUserFirma")) {
            JSONObject jObj = new JSONObject();
            PreparedStatement ps;
            ResultSet rs;
            String query = "SELECT * FROM firme WHERE userID=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("userID", rs.getInt("userID"));
                    jObj.put("nazivFirme", rs.getString("nazivFirme"));
                    jObj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
                    jObj.put("kodBanke", rs.getString("kodBanke"));
                    jObj.put("pib", rs.getString("PIB"));
                    jObj.put("maticniBroj", rs.getString("maticniBroj"));
                    jObj.put("tekuciRacun", rs.getString("tekuciRacun"));
                }
                ps.close();
                rs.close();
            } catch (SQLException e) {
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
        }


        if (rLine.getString("action").equals("delete_user")) {
            delete_user(rLine);

        }

        if (rLine.getString("action").equals("new_user")) {

            query = "INSERT INTO users (ime, datumRodjenja, operater, postBr, mesto, brLk, JMBG, "
                    + "adresa,  komentar, telFiksni, telMobilni, datumKreiranja)"
                    + "VALUES (?, ?, ?, ?, ?, ? ,? ,? ,? ,?, ?, ?)";

            try {
                ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, rLine.getString("fullName"));
                ps.setString(2, rLine.getString("datumRodjenja"));
                ps.setString(3, getOperName());
                ps.setString(4, rLine.getString("postBr"));
                ps.setString(5, rLine.getString("mesto"));
                ps.setString(6, rLine.getString("brLk"));
                ps.setString(7, rLine.getString("JMBG"));
                ps.setString(8, rLine.getString("adresa"));
                ps.setString(9, rLine.getString("komentar"));
                ps.setString(10, rLine.getString("telFiksni"));
                ps.setString(11, rLine.getString("telMobilni"));
                ps.setString(12, mysql_date_format.format(new Date()));

                ps.executeUpdate();

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", "ERROR");
                jObj.put("ERROR_MESSAGE", e.getMessage());
                e.printStackTrace();
            }

            jObj = new JSONObject();
            try {
                rs = ps.getGeneratedKeys();
                rs.next();
                jObj.put("Message", "user_saved");
                jObj.put("userID", rs.getInt(1));
                rs.close();
                ps.close();
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("ERROR_MESSAGE", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_user_services")) {
            jObj = new JSONObject();
            query = "SELECT *  FROM servicesUser WHERE userID=? AND linkedService=false";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    JSONObject service;
                    int i = 0;
                    while (rs.next()) {
                        service = new JSONObject();
                        service.put("id", rs.getInt("id"));
                        service.put("brojUgovora", rs.getString("brojUgovora"));
                        service.put("cena", rs.getDouble("cena"));
                        service.put("popust", rs.getDouble("popust"));
                        service.put("operName", rs.getString("operName"));
                        service.put("date_added", rs.getString("date_added"));
                        service.put("nazivPaketa", rs.getString("nazivPaketa"));
                        if (rs.getString("idDTVCard") != null) {
                            service.put("idUniqueName", rs.getString("idDTVCard"));
                        }
                        if (rs.getString("UserName") != null) {
                            service.put("idUniqueName", rs.getString("UserName"));
                        }
                        if (rs.getString("IPTV_MAC") != null) {
                            service.put("idUniqueName", rs.getString("IPTV_MAC"));
                            service.put("IPTV_MAC", rs.getString("IPTV_MAC"));
                            service.put("STB_MAC", rs.getString("IPTV_MAC"));
                            service.put("external_id", rs.getString("IPTV_EXT_ID"));
                        }
                        service.put("obracun", rs.getBoolean("obracun"));
                        service.put("aktivan", rs.getBoolean("aktivan"));
                        service.put("produzenje", rs.getInt("produzenje"));
                        service.put("id_service", rs.getInt("id_service"));
                        service.put("box", rs.getBoolean("BOX_service"));
                        service.put("box_ID", rs.getInt("box_id"));
                        service.put("brojTel", rs.getString("FIKSNA_TEL"));
                        service.put("paketType", rs.getString("paketType"));
                        service.put("linkedService", rs.getBoolean("linkedService"));
                        service.put("newService", rs.getBoolean("newService"));
                        service.put("DTVPaketID", rs.getInt("DTVPaket"));
                        service.put("endDate", rs.getString("endDate"));

                        jObj.put(String.valueOf(i), service);
                        i++;
                    }
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("get_user_linked_services")) {

            JSONObject jObj2 = new JSONObject();
            PreparedStatement ps2;
            query = "SELECT * FROM servicesUser WHERE box_id=? AND linkedService=? AND userID=?";
            try {
                ps2 = db.conn.prepareStatement(query);
                ps2.setInt(1, rLine.getInt("box_ID"));
                ps2.setInt(2, 1);
                ps2.setInt(3, rLine.getInt("userID"));
                ResultSet rs2 = ps2.executeQuery();

                LOGGER.info(ps2.toString());
                if (rs2.isBeforeFirst()) {
                    JSONObject service;
                    int i = 0;
                    while (rs2.next()) {
                        service = new JSONObject();
                        service.put("id", rs2.getInt("id"));
                        service.put("userID", rs2.getInt("userID"));
                        service.put("id_service", rs2.getInt("id_service"));
                        service.put("box_ID", rs2.getInt("box_id"));
                        service.put("nazivPaketa", rs2.getString("nazivPaketa"));
                        service.put("produzenje", rs2.getInt("produzenje"));
                        service.put("groupName", rs2.getString("GroupName"));
                        service.put("userName", rs2.getString("UserName"));
                        service.put("idDTVCard", rs2.getString("idDTVCard"));
                        service.put("IPTV_MAC", rs2.getString("IPTV_MAC"));
                        service.put("STB_MAC", rs2.getString("IPTV_MAC"));
                        service.put("FIKSNA_TEL", rs2.getString("FIKSNA_TEL"));
                        service.put("popust", rs2.getDouble("popust"));
                        service.put("cena", rs2.getDouble("cena"));
                        service.put("linkedService", rs2.getBoolean("linkedService"));
                        service.put("aktivan", rs2.getBoolean("aktivan"));
                        service.put("endDate", rs2.getString("endDate"));
                        if (rs2.getString("GroupName") != null) {
                            service.put("paketType", "NET");
                        }
                        if (rs2.getString("idDTVCard") != null) {
                            service.put("paketType", "DTV");
                        }
                        if (rs2.getString("DTVPaket") != null) {
                            service.put("paketType", "DTV");
                        }
                        if (rs2.getString("FIKSNA_TEL") != null) {
                            service.put("paketType", "FIX");
                        }
                        if (rs2.getString("IPTV_MAC") != null) {
                            service.put("paketType", "IPTV");
                        }
                        service.put("newService", rs2.getBoolean("newService"));

                        jObj2.put(String.valueOf(i), service);
                        i++;
                    }

                    rs2.close();
                    ps2.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj2);
            return;

        }

        if (rLine.get("action").equals("activate_service")) {
            PreparedStatement ps;
            ResultSet rs;
            String query;

            jObj = new JSONObject();

            int service_id = rLine.getInt("service_id");
            query = "SELECT * FROM servicesUser WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, service_id);
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    if (rs.getString("paketType").equals("BOX")) {
                        ServicesFunctions.activateBoxService(getOperName(), rs, db);
                    } else {
                        ServicesFunctions.activateService(getOperName(), rs, db);
                    }
                }

                ps.close();
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj.put("Message", "SERVICE_AKTIVATED");

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("get_datum_isteka_servisa")) {
            jObj = new JSONObject();
            String datumIsteka = ServicesFunctions.getDatumIsteka(rLine, db);

            if (datumIsteka == null) datumIsteka = "KORISNIK NEMA SERVIS";
            jObj.put("datumIsteka", datumIsteka);
            System.out.println(jObj);
            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("add_BOX_Service")) {
            jObj = new JSONObject();

            //provera da li korisnik, kartica postoji. Ako postoji prijaviti operateru da ne moze da se napravi servis
            Boolean checkNet = false;
            Boolean checkDtv = false;
            Boolean checkFix = false;
            Boolean checkIptv = false;

            if (rLine.has("groupName")) {
                //createInternetService;
                checkNet = NETFunctions.check_userName_busy(rLine.getString("userName"), db);

            }

            if (rLine.has("DTVKartica")) {
                //create DTV SERVICE
                checkDtv = DTVFunctions.check_card_busy(rLine.getInt("DTVKartica"), db);

            }

            if (rLine.has("FIX_TEL")) {
                //create FIX SERVICE
                checkFix = FIXFunctions.check_TELBr_bussy(rLine.getString("FIX_TEL"), db);
            }

            if (rLine.has("STB_MAC")) {
                checkIptv = IPTVFunctions.checkUserBussy(rLine.getString("STB_MAC"), db);
            }

            //ako je user ili kartica zauzeta poslati obavestenje u suprotnom napraviti boxPaket serivis i dodati ostrale servise koriniku
            if (checkDtv || checkNet || checkFix || checkIptv) {
                String message = null;
                if (checkDtv) {
                    message = "DTV Kartica je zauzeta";
                    jObj.put("Error", message);
                }

                if (checkNet) {
                    message = "Internet korisnicko ime je zauzeto";
                    jObj.put("Error", message);
                }

                if (checkFix) {
                    message = "Broj telefona je zauzet";
                    jObj.put("Error", message);
                }

                if (checkIptv) {
                    message = "IPTV STB_MAC je zauzet";
                    jObj.put("Error", message);
                }
            } else {
                //add BOX to servicesUser
                addBoxService addBox = new addBoxService();
                addBox.db = db;
                addBox.addBox(rLine, getOperName());

            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("get_paket_box")) {
            jObj = new JSONObject();
            ResultSet rs = null;
            PreparedStatement ps = null;

            query = "SELECT * FROM paketBox";

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    JSONObject paketBox;
                    int i = 0;
                    while (rs.next()) {
                        paketBox = new JSONObject();
                        paketBox.put("id", rs.getInt("id"));
                        paketBox.put("naziv", rs.getString("naziv"));
                        paketBox.put("cena", rs.getDouble("cena"));
                        paketBox.put("pdv", rs.getDouble("pdv"));
                        paketBox.put("paketType", "BOX");
                        if (rs.getString("DTV_naziv") != null) {
                            paketBox.put("DTV_id", rs.getInt("DTV_id"));
                            paketBox.put("DTV_naziv", get_paket_naziv("digitalniTVPaketi", rs.getInt("DTV_id")));
                            paketBox.put("DTV_PAKET_ID", DTVFunctions.getPacketCriteriaGroup(rs.getInt("DTV_id"), this.db));
                        }

                        if (rs.getString("NET_naziv") != null) {
                            paketBox.put("NET_id", rs.getInt("NET_id"));
                            paketBox.put("NET_naziv", get_paket_naziv("internetPaketi", rs.getInt("NET_id")));
                        }

                        if (rs.getString("TEL_naziv") != null) {
                            paketBox.put("FIX_id", rs.getInt("TEL_id"));
                            paketBox.put("FIX_naziv", get_paket_naziv("FIXPaketi", rs.getInt("TEL_id")));
                            paketBox.put("FIX_PAKET_ID", FIXFunctions.getPaketID(paketBox.getString("FIX_naziv"), this.db));
                        }

                        if (rs.getString("IPTV_naziv") != null) {
                            paketBox.put("IPTV_id", rs.getInt("IPTV_id"));
                            paketBox.put("IPTV_naziv", get_paket_naziv("IPTV_Paketi", rs.getInt("IPTV_id")));
                            paketBox.put("tariff_plan", get_paket_naziv("IPTV_Paketi", rs.getInt("IPTV_id")));
                        }
                        jObj.put(String.valueOf(i), paketBox);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("add_service_to_user_DTV")) {
            jObj = new JSONObject();

            String serviceAdded = ServicesFunctions.addServiceDTV(rLine.getInt("id"), rLine.getString("nazivPaketa"),
                    rLine.getInt("userID"), getOperName(), rLine.getDouble("servicePopust"),
                    rLine.getDouble("cena"), rLine.getBoolean("obracun"), rLine.getString("brojUgovora"),
                    rLine.getInt("produzenje"), rLine.getString("idUniqueName"), rLine.getInt("packetID"), this.db);

            if (serviceAdded.equals("SERVICE_ADDED")) {
                jObj.put("Message", "SERVICE_ADDED");
            } else {
                jObj.put("Error", serviceAdded);
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("add_service_to_user_NET")) {
            jObj = new JSONObject();
            String userAdded = ServicesFunctions.addServiceNET(rLine, getOperName(), this.db);
            if (userAdded.equals("USER_EXIST")) {
                jObj.put("Error", "USER_EXIST");
            } else {
                jObj.put("Message", "USER_ADDED");
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("delete_service_user")) {
            jObj = new JSONObject();
            JSONObject delObj;

            System.out.println("DUZINA: " + rLine.length());
            //rline.lenght -2 becouse 1 = action 2 = userid
            for (int i = 0; i < rLine.length() - 2; i++) {
                delObj = (JSONObject) rLine.get(String.valueOf(i));
                System.out.println(delObj);

                System.out.println("DELETE paketType: " + delObj.getString("paketType"));
                System.out.println("DELETE ID: " + delObj.getInt("id"));
                if (delObj.getString("paketType").equals("DTV")
                        || delObj.getString("paketType").equals("LINKED_DTV")) {
                    ServicesFunctions.deleteServiceDTV(delObj, getOperName(), db);
                }
                if (delObj.getString("paketType").equals("NET")
                        || delObj.getString("paketType").equals("LINKED_NET")) {
                    ServicesFunctions.deleteServiceNET(delObj, getOperName(), db);
                }

                if (delObj.getString("paketType").equals("BOX")) {
                    ServicesFunctions.deleteServiceBOX(delObj, getOperName(), db);
                }

                if (delObj.getString("paketType").equals("FIX")
                        || delObj.getString("paketType").equals("LINKED_FIX")) {
                    ServicesFunctions.deleteServiceFIX(delObj, getOperName(), db);
                }

                if (delObj.getString("paketType").equals("IPTV")
                        || delObj.getString("paketType").equals("LINKED_IPTV")) {
                    ServicesFunctions.deleteServiceIPTV(delObj, getOperName(), db);
                }

            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("new_uplata")) {
            jObj = new JSONObject();
            query = "INSERT INTO uplate (datumUplate, uplaceno, mesto, operater, userID, napomena) VALUES (?,?,?,?,?,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("datumUplate"));
                ps.setDouble(2, rLine.getDouble("uplaceno"));
                ps.setString(3, rLine.getString("mesto"));
                ps.setString(4, getOperName());
                ps.setInt(5, rLine.getInt("userID"));
                ps.setString(6, rLine.getString("napomena"));
                ps.executeUpdate();
                jObj.put("Message", "UPLATA_UPLACENA");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("DELETE_UPLATA")) {
            jObj = new JSONObject();

            query = "DELETE FROM uplate where id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("uplataID"));
                ps.executeUpdate();
                jObj.put("Message", "UPLATA_DELETED");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_uplate_user")) {
            jObj = new JSONObject();
            query = "SELECT * FROM uplate WHERE userId = ? ORDER BY datumUplate DESC";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                JSONObject uplate;
                int i = 0;
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        uplate = new JSONObject();
                        uplate.put("uplaceno", rs.getDouble("uplaceno"));
                        uplate.put("id", rs.getInt("id"));
                        uplate.put("datumUplate", rs.getDate("datumUplate"));
                        uplate.put("mesto", rs.getString("mesto"));
                        uplate.put("operater", rs.getString("operater"));
                        uplate.put("napomena", rs.getString("napomena"));
                        jObj.put(String.valueOf(i), uplate);
                        i++;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_zaduzenja_user")) {
            jObj = new JSONObject();
            if (rLine.getBoolean("sveUplate")) {
                query = "SELECT * FROM userDebts where userID=? ORDER BY zaMesec ASC";
            } else {
                query = "SELECT * FROM userDebts WHERE userId=? AND dug > uplaceno ORDER BY zaMesec ASC";
            }

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    JSONObject userDebt;
                    int i = 0;
                    while (rs.next()) {

                        userDebt = new JSONObject();
                        userDebt.put("id", rs.getInt("id"));
                        userDebt.put("id_ServiceUser", rs.getInt("id_ServiceUser"));
                        userDebt.put("nazivPaketa", rs.getString("nazivPaketa"));
                        userDebt.put("datumZaduzenja", rs.getDate("datumZaduzenja"));
                        userDebt.put("userID", rs.getInt("userID"));
                        userDebt.put("popust", rs.getDouble("popust"));
                        userDebt.put("paketType", rs.getString("paketType"));
                        userDebt.put("cena", rs.getDouble("cena"));
                        userDebt.put("uplaceno", rs.getDouble("uplaceno"));
                        userDebt.put("datumUplate", rs.getString("datumUplate"));
                        userDebt.put("dug", rs.getDouble("dug"));
                        userDebt.put("operater", rs.getString("operater"));
                        userDebt.put("zaduzenOd", rs.getString("zaduzenOd"));
                        userDebt.put("zaMesec", rs.getString("zaMesec"));
                        userDebt.put("skipProduzenje", rs.getBoolean("skipProduzenje"));
                        jObj.put(String.valueOf(i), userDebt);
                        i++;
                    }
                }

            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("uplata_servisa")) {
            jObj = new JSONObject();

            PreparedStatement ps = null;
            ResultSet rs;

            query = "UPDATE userDebts SET uplaceno=?, datumUplate=?, operater=?, skipProduzenje=true WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setDouble(1, rLine.getDouble("uplaceno"));
                ps.setString(2, date_format_full.format(Calendar.getInstance().getTime()));
                ps.setString(3, getOperName());
                ps.setInt(4, rLine.getInt("id"));
                ps.executeUpdate();
                jObj.put("Message", "SERVICE_PAYMENTS_DONE");
                ps.close();
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            if (rLine.getString("paketType").equals("BOX")) {
                query = "SELECT * FROM servicesUser WHERE box_id=?";
            } else {
                query = "SELECT * FROM servicesUser WHERE id=?";
            }
            rs = null;
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("id_ServiceUser"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        ServicesFunctions.produziService(rs.getInt("id"), getOperName(), rLine.getBoolean("skipProduzenje"), db);
                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
            }


            try {
                ps.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("zaduzi_servis_manual")) {
            jObj = new JSONObject();
            Calendar cal = Calendar.getInstance();
            Calendar calRate = Calendar.getInstance();
            calRate.set(Calendar.DAY_OF_MONTH, 1);
            try {
                calRate.setTime(formatMonthDate.parse(rLine.getString("zaMesec")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int rate = rLine.getInt("rate");
            int month = 0;

            for (int i = 0; i < rate; i++) {

                query = "INSERT INTO userDebts (id_ServiceUser, nazivPaketa, datumZaduzenja, userID, popust,"
                        + " paketType, cena, uplaceno, dug, zaduzenOd, zaMesec)"
                        + " VALUES"
                        + " (?,?,?,?,?,?,?,?,?,?,?)";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setNull(1, Types.INTEGER);
                    ps.setString(2, rLine.getString("nazivPaketa"));
                    ps.setString(3, normalDate.format(cal.getTime()));
                    ps.setInt(4, rLine.getInt("userID"));
                    ps.setDouble(5, 0.00);
                    ps.setString(6, rLine.getString("paketType"));
                    ps.setDouble(7, rLine.getDouble("cena"));
                    ps.setDouble(8, 0.00);
                    ps.setDouble(9, Double.parseDouble(df.format(rLine.getDouble("cena") / rate)));
                    ps.setString(10, getOperName());
                    calRate.add(Calendar.MONTH, month);
                    if (month == 0) {
                        month++;
                    }
                    ps.setString(11, formatMonthDate.format(calRate.getTime()));
                    ps.executeUpdate();
                    ps.close();

                    LOGGER.info(ps.toString());
                } catch (SQLException e) {
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }

            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("zaduzi_uslugu")) {
            //zaduzivanje usluga unapred
            jObj = new JSONObject();
            JSONObject Message = new JSONObject();
            if (!ServicesFunctions.check_service_exist(rLine.getInt("id_ServiceUser"),
                    rLine.getInt("userID"), rLine.getString("zaMesec"), db)) {
                Message.put("serviceExist", ServicesFunctions.addService(rLine, getOperName(), db));
                PreparedStatement ps;
                ResultSet rs;
                String query;
                query = "SELECT * FROM servicesUser WHERE id_service =?";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setInt(1, rLine.getInt("id_ServiceUser"));
                    rs = ps.executeQuery();
                    if (rs.isBeforeFirst()) {
                        rs.next();
                        /// skipProduzenje proveriti
                        ServicesFunctions.produziService(rs.getInt("id"), getOperName(), false, db);
                    }
                    ps.close();
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                jObj.put("Message", "Usluga za mesec " + rLine.getString("zaMesec") + " zaduzena");
            } else {
                jObj.put("Error", "Usluga za mesec " + rLine.getString("zaMesec") + " postoji!");
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("add_new_ugovor")) {
            query = "INSERT INTO ugovori_types "
                    + "(naziv,  text_ugovor)"
                    + " VALUES (?,?)";
            LOGGER.info(query + rLine.getString("nazivUgovora"));

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivUgovora"));
                db.ps.setString(2, rLine.getString("textUgovora"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
            }
            jObj = new JSONObject();
            jObj.put("Message", "UGOVOR_ADDED");
            send_object(jObj);
            return;
        }
        if (rLine.getString("action").equals("get_ugovori")) {
            query = "SELECT * FROM ugovori_types";
            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JSONObject ugovoryArr = new JSONObject();
            int i = 0;

            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("idUgovora", rs.getInt("id"));
                    jObj.put("nazivUgovora", rs.getString("naziv"));
                    jObj.put("textUgovora", rs.getString("text_ugovor"));
                    ugovoryArr.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(ugovoryArr);
            return;
        }

        if (rLine.getString("action").equals("get_single_ugovor")) {
            query = "SELECT * from ugovori_types where id=?";
            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("idUgovora"));
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();

            try {
                rs.next();
                jObj.put("idUgovora", rs.getInt("id"));
                jObj.put("nazivUgovora", rs.getString("naziv"));
                jObj.put("textUgovora", rs.getString("text_ugovor"));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("delete_ugovor")) {
            query = "DELETE FROM ugovori_types WHERE id=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("ugovorId"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            jObj = new JSONObject();
            jObj.put("Message", "UGOVOR_DELETED");
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("update_ugovor_temp")) {
            query = "UPDATE ugovori_types SET text_ugovor=?, naziv=? WHERE id=?";
            jObj = new JSONObject();
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("text_ugovora"));
                ps.setString(2, rLine.getString("naziv"));
                ps.setInt(3, rLine.getInt("id"));
                ps.executeUpdate();
                jObj.put("Message", "UGOVOR_UPDATED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_ugovori_user")) {
            jObj = new JSONObject();
            query = "SELECT * FROM ugovori_korisnik WHERE userID=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    JSONObject ugovori;
                    int i = 0;
                    while (rs.next()) {
                        ugovori = new JSONObject();
                        ugovori.put("id", rs.getInt("id"));
                        ugovori.put("brojUgovora", rs.getString("brojUgovora"));
                        ugovori.put("naziv", rs.getString("naziv"));
                        ugovori.put("vrsta", rs.getString("vrsta"));
                        ugovori.put("textUgovora", rs.getString("textUgovora"));
                        ugovori.put("komentar", rs.getString("komentar"));
                        ugovori.put("pocetakUgovora", rs.getString("pocetakUgovora"));
                        ugovori.put("krajUgovora", rs.getString("krajUgovora"));
                        ugovori.put("userID", rs.getInt("userID"));
                        ugovori.put("serviceID", rs.getInt("serviceID"));
                        jObj.put(String.valueOf(i), ugovori);
                        i++;
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("save_user_ugovor")) {
            jObj = new JSONObject();

            PreparedStatement ps;
            ResultSet rs;
            String query;

            query = "SELECT brojUgovora from ugovori_korisnik where brojUgovora=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("brojUgovora"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("ERROR", String.format("BROJ UGOVORA JE ZAUZET", rs.getString("brojUgovora")));
                    send_object(jObj);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            query = "INSERT INTO ugovori_korisnik (naziv, textUgovora, komentar, pocetakUgovora, krajUgovora, userID, brojUgovora) "
                    + "VALUES "
                    + "(?,?,?,?,?,?,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, rLine.getString("textUgovora"));
                ps.setString(3, rLine.getString("komentar"));
                ps.setString(4, rLine.getString("pocetakUgovora"));
                ps.setString(5, rLine.getString("krajUgovora"));
                ps.setInt(6, rLine.getInt("userID"));
                ps.setString(7, rLine.getString("brojUgovora"));
                ps.executeUpdate();
                jObj.put("Message", "UGOVOR_ADDED");

            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("check_brUgovora_busy")) {
            jObj = new JSONObject();
            String query = "SELECT * FROM ugovori_korisnik WHERE brojUgovora=?";
            PreparedStatement ps;
            ResultSet rs;

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("brojUgovora"));
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("ERROR", String.format("BROJ UGOVORA %s je zauzet", rs.getString("brojUgovora")));
                    send_object(jObj);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj.put("MESSAGE", "OK");
            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("update_user_ugovor")) {
            jObj = new JSONObject();
            query = "UPDATE ugovori_korisnik SET textUgovora=? WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("textUgovora"));
                ps.setInt(2, rLine.getInt("id"));
                ps.executeUpdate();
                jObj.put("Message", "UGOVOR_UPDATED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_ugovor_user")) {
            jObj = new JSONObject();
            query = "SELECT * FROM ugovori_korisnik WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("ugovorID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("textUgovora", rs.getString("textUgovora"));
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("naziv", rs.getString("naziv"));
                    jObj.put("komentar", rs.getString("komentar"));
                    jObj.put("pocetakUgovora", rs.getString("pocetakUgovora"));
                    jObj.put("krajUgovora", rs.getString("krajUgovora"));
                    jObj.put("userID", rs.getInt("userID"));
                    jObj.put("serviceID", rs.getInt("serviceID"));
                    jObj.put("brojUgovora", rs.getShort("brojUgovora"));

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("saveOprema")) {
            query = "INSERT INTO oprema (naziv, model, komentar, userId) VALUES"
                    + "(?,?,?,?)";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivOpreme"));
                db.ps.setString(2, rLine.getString("modelOpreme"));
                db.ps.setString(3, rLine.getString("kometarOpreme"));
                db.ps.setInt(4, rLine.getInt("userId"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("Message", "USER_OPREMA_SAVED");

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_fakture")) {
            query = "SELECT * FROM jFakture WHERE userId=? AND brFakture LIKE ? AND godina LIKE ?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userId"));
                if (rLine.has("brFakture")) {
                    if (rLine.get("brFakture").equals("")) {
                        db.ps.setString(2, "%");
                    } else {
                        db.ps.setInt(2, rLine.getInt("brFakture"));
                    }
                } else {
                    db.ps.setString(2, "%");
                }
                if (rLine.has("godina")) {
                    if (rLine.get("godina").equals("")) {
                        db.ps.setString(3, "%");
                    } else {
                        db.ps.setString(3, rLine.getString("godina"));
                    }
                } else {
                    db.ps.setString(3, "%");
                }
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JSONObject FaktureObj = new JSONObject();
            Double VrednostBezPDV = 0.00;
            Double OsnovicaZaPDV = 0.00;
            int stopaPDV = 0;
            Double iznosPDV = 0.00;
            Double VrednostSaPDV = 0.00;
            int kolicina = 0;
            double jedCena = 0.00;

            int i = 0;
            try {
                while (rs.next()) {

                    kolicina = rs.getInt("kolicina");
                    jedCena = rs.getDouble("jedCena");
                    stopaPDV = rs.getInt("stopaPdv");

                    VrednostBezPDV = jedCena * kolicina;
                    OsnovicaZaPDV = VrednostBezPDV;
                    iznosPDV = stopaPDV / 100.00 * OsnovicaZaPDV;
                    VrednostSaPDV = OsnovicaZaPDV + iznosPDV;

                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("vrstaNaziv", rs.getString("vrstaNaziv"));
                    jObj.put("jedMere", rs.getString("jedMere"));
                    jObj.put("kolicina", kolicina);
                    jObj.put("jedCena", jedCena);
                    jObj.put("stopaPdv", stopaPDV);
                    jObj.put("brFakture", rs.getInt("brFakture"));
                    jObj.put("godina", rs.getString("godina"));
                    jObj.put("dateCreated", rs.getString("dateCreated"));

                    //calculate
                    jObj.put("VrednostBezPDV", VrednostBezPDV);
                    jObj.put("OsnovicaZaPDV", OsnovicaZaPDV);
                    jObj.put("iznosPDV", iznosPDV);
                    jObj.put("VrednostSaPDV", VrednostSaPDV);

                    FaktureObj.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(FaktureObj);
            return;
        }

        if (rLine.getString("action").equals("delete_fakturu")) {
            query = "DELETE FROM jFakture WHERE id=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("idFactura"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            jObj = new JSONObject();
            jObj.put("Message", "FACUTRE_DELETED");
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("snimiFakturu")) {
            query = "INSERT INTO jFakture (vrstaNaziv, jedMere, kolicina, jedCena, stopaPDV, brFakture, godina, userId, dateCreated)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivMesec"));
                db.ps.setString(2, rLine.getString("jedMere"));
                db.ps.setInt(3, rLine.getInt("kolicina"));
                db.ps.setDouble(4, rLine.getDouble("jedinacnaCena"));
                db.ps.setInt(5, rLine.getInt("stopaPDV"));
                db.ps.setInt(6, rLine.getInt("brFakture"));
                db.ps.setString(7, rLine.getString("godina"));
                db.ps.setInt(8, rLine.getInt("userId"));
                db.ps.setString(9, formatter.format(date));
                LOGGER.info("QUYERY: " + db.ps.toString());
                db.ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("Message", "FACTURE_ADDED");
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("PING")) {
            jObj = new JSONObject();
            jObj.put("Message", "PONG");
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("getInternetGroups")) {
            query = "SELECT * FROM grupa";

            try {
                db.ps = db.conn.prepareStatement(query);
                rs = db.ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    JSONObject grupa;
                    JSONObject grupe = new JSONObject();
                    int i = 0;
                    while (rs.next()) {
                        grupa = new JSONObject();
                        grupa.put("id", rs.getInt("id"));
                        grupa.put("groupName", rs.getString("groupname"));
                        grupa.put("prepaid", rs.getInt("prepaid"));
                        grupa.put("cena", rs.getString("cena"));
                        grupa.put("opis", rs.getString("opis"));
                        grupe.put(String.valueOf(i), grupa);
                        i++;
                    }
                    send_object(grupe);

                    return;


                } else {
                    jObj = new JSONObject();
                    jObj.put("Message", "NO_GROUPS");
                    send_object(jObj);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        if (rLine.getString("action").equals("getMesta")) {

            query = "SELECT * FROM mesta";
            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();

                JSONObject mesta = new JSONObject();
                JSONObject mesto;

                int i = 0;
                while (rs.next()) {
                    mesto = new JSONObject();
                    mesto.put("id", rs.getInt("id"));
                    mesto.put("nazivMesta", rs.getString("naziv"));
                    mesto.put("brojMesta", rs.getString("broj"));
                    mesta.put(String.valueOf(i), mesto);
                    i++;
                }
                send_object(mesta);
                return;

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
                send_object(jObj);
                e.printStackTrace();
            }
            return;
        }

        if (rLine.getString("action").equals("getMesto")) {
            query = "SELECT * FROM mesta WHERE id=?";
            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("broj"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("nazivMesta", rs.getString("naziv"));
                    jObj.put("brojMesta", rs.getString("broj"));
                } else {
                    jObj.put("Message", "NO_RECORD");

                }
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("addMesto")) {
            query = "INSERT INTO mesta (naziv, broj) VALUES (?,?)";

            jObj = new JSONObject();
            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivMesta"));
                db.ps.setString(2, rLine.getString("brojMesta"));
                db.ps.executeUpdate();
                jObj.put("Message", "MESTO_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());

                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("getAdrese")) {
            query = "SELECT * FROM adrese WHERE idMesta = ?";
            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("idMesta"));
                rs = ps.executeQuery();

                JSONObject adrese = new JSONObject();
                JSONObject adresa;

                int i = 0;

                while (rs.next()) {
                    adresa = new JSONObject();
                    adresa.put("id", rs.getInt("id"));
                    adresa.put("nazivAdrese", rs.getString("naziv"));
                    adresa.put("brojAdrese", rs.getString("broj"));
                    adresa.put("idMesta", rs.getInt("idMesta"));
                    adresa.put("brojMesta", rs.getString("brojMesta"));
                    adresa.put("nazivMesta", rs.getString("nazivMesta"));
                    adrese.put(String.valueOf(i), adresa);
                    i++;
                }
                send_object(adrese);
                return;

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
                send_object(jObj);
                e.printStackTrace();
            }
            return;
        }

        if (rLine.getString("action").equals("getAdresa")) {
            query = "SELECT * FROM adrese WHERE id=?";

            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("idMesta"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("nazivAdrese", rs.getString("naziv"));
                    jObj.put("brojAdrese", rs.getString("broj"));
                    jObj.put("idMesta", rs.getInt("idMesta"));
                    jObj.put("brojMesta", rs.getString("brojMesta"));
                    jObj.put("nazivMesta", rs.getString("nazivMesta"));
                } else {
                    jObj.put("Message", "NO_RECORD");
                }

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("addAdresa")) {
            query = "INSERT INTO adrese (naziv, broj, idMesta, brojMesta, nazivMesta) VALUES (?, ?, ?, ?, ?)";

            jObj = new JSONObject();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivAdrese"));
                db.ps.setString(2, rLine.getString("brojAdrese"));
                db.ps.setInt(3, rLine.getInt("idMesta"));
                db.ps.setString(4, rLine.getString("brojMesta"));
                db.ps.setString(5, rLine.getString("nazivMesta"));
                db.ps.executeUpdate();
                jObj.put("Message", "ADRESS_ADDED");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("delAdresa")) {
            query = "DELETE FROM adrese WHERE id=?";

            jObj = new JSONObject();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("id"));
                db.ps.executeUpdate();
                jObj.put("Message", "ADRESS_DELETED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("DEL_MESTO")) {
            jObj = new JSONObject();

            query = "DELETE FROM mesta WHERE id=?";
            String query2 = "DELETE FROM jAdrese WHERE idMesta=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("idMesta"));
                db.ps.executeUpdate();
                db.ps = db.conn.prepareStatement(query2);
                db.ps.setInt(1, rLine.getInt("idMesta"));
                db.ps.executeUpdate();
                jObj.put("Message", "MESTO_DELETED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("GET_OPREMA_NAZIV")) {
            jObj = new JSONObject();
            JSONObject nazivOprema;

            query = "SELECT DISTINCT(naziv), id , naziv from oprema GROUP BY naziv";

            try {
                db.ps = db.conn.prepareStatement(query);
                rs = db.ps.executeQuery();

                int i = 0;
                while (rs.next()) {
                    nazivOprema = new JSONObject();
                    nazivOprema.put("naziv", rs.getString("naziv"));
                    nazivOprema.put("id", rs.getInt("id"));
                    jObj.put(String.valueOf(i), nazivOprema);
                    i++;
                }

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("GET_OPREMA")) {
            jObj = new JSONObject();
            JSONObject oprema;
            query = "SELECT * FROM oprema";

            try {
                db.ps = db.conn.prepareStatement(query);
                rs = db.ps.executeQuery();

                int i = 0;

                while (rs.next()) {
                    oprema = new JSONObject();
                    oprema.put("id", rs.getInt("id"));
                    oprema.put("naziv", rs.getString("naziv"));
                    oprema.put("model", rs.getString("model"));
                    jObj.put(String.valueOf(i), oprema);
                    i++;
                }

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("GET_MODEL_OPREME")) {
            jObj = new JSONObject();

            query = "SELECT * FROM oprema WHERE naziv=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("naziv"));
                rs = db.ps.executeQuery();

                JSONObject modelObj;
                int i = 0;
                while (rs.next()) {
                    modelObj = new JSONObject();
                    modelObj.put("id", rs.getInt("id"));
                    modelObj.put("model", rs.getString("model"));
                    modelObj.put("naziv", rs.getString("naziv"));
                    jObj.put(String.valueOf(i), modelObj);
                    i++;
                }

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("ADD_OPREMA")) {
            jObj = new JSONObject();

            query = "INSERT INTO oprema (naziv, model) VALUES (?,?)";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("naziv"));
                db.ps.setString(2, rLine.getString("model"));
                db.ps.executeUpdate();
                jObj.put("Message", "OPREMA_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("DEL_OPREMA")) {
            jObj = new JSONObject();

            query = "DELETE FROM oprema WHERE id=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("id"));
                db.ps.executeUpdate();
                jObj.put("Message", "OPREMA_DELETED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("ADD_USER_OPREMA")) {
            jObj = new JSONObject();

            query = "INSERT INTO opremaKorisnik (naziv, model, komentar, userID, sn, MAC, naplata) "
                    + "VALUES "
                    + "(?, ?, ? ,? ,? ,? ,?)";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("naziv"));
                db.ps.setString(2, rLine.getString("model"));
                db.ps.setString(3, rLine.getString("komentar"));
                db.ps.setInt(4, rLine.getInt("userID"));
                db.ps.setString(5, rLine.getString("sn"));
                db.ps.setString(6, rLine.getString("MAC"));
                db.ps.setInt(7, rLine.getInt("naplata"));
                db.ps.executeUpdate();
                jObj.put("Message", "USER_OPREMA_ADDED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }
        if (rLine.getString("action").equals("DELETE_USER_OPREMA")) {
            jObj = new JSONObject();
            query = "DELETE FROM opremaKorisnik WHERE id=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("id"));
                db.ps.executeUpdate();

                jObj.put("Message", "USER_OPREMA_DELETED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("GET_USER_OPREMA")) {
            jObj = new JSONObject();

            query = "SELECT * FROM opremaKorisnik where userID=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userID"));
                rs = db.ps.executeQuery();

                JSONObject opremaKor;
                int i = 0;
                while (rs.next()) {
                    opremaKor = new JSONObject();
                    opremaKor.put("id", rs.getInt("id"));
                    opremaKor.put("naziv", rs.getString("naziv"));
                    opremaKor.put("model", rs.getString("model"));
                    opremaKor.put("komentar", rs.getString("komentar"));
                    opremaKor.put("serial", rs.getString("sn"));
                    opremaKor.put("MAC", rs.getString("MAC"));
                    opremaKor.put("naplata", rs.getInt("naplata"));
                    opremaKor.put("userID", rs.getInt("userID"));
                    jObj.put(String.valueOf(i), opremaKor);
                    i++;

                }
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("getOperaters")) {
            jObj = new JSONObject();
            query = "SELECT * FROM operateri";
            JSONObject opers;

            int i = 0;

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        opers = new JSONObject();
                        opers.put("id", rs.getInt("Id"));
                        opers.put("ime", rs.getString("ime"));
                        opers.put("username", rs.getString("username"));
                        opers.put("aktivan", rs.getBoolean("aktivan"));
                        opers.put("adresa", rs.getString("adresa"));
                        opers.put("komentar", rs.getString("komentar"));
                        opers.put("telefon", rs.getString("telefon"));
                        jObj.put(String.valueOf(i), opers);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("saveOperater")) {
            jObj = new JSONObject();
            query = "INSERT INTO operateri (username,password, adresa, telefon, komentar, aktivan, ime)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("username"));
                ps.setString(2, rLine.getString("password"));
                ps.setString(3, rLine.getString("adresa"));
                ps.setString(4, rLine.getString("telefon"));
                ps.setString(5, rLine.getString("komentar"));
                ps.setBoolean(6, rLine.getBoolean("aktivan"));
                ps.setString(7, rLine.getString("ime"));
                ps.executeUpdate();
                jObj.put("Message", "OPER_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("updateOperater")) {
            jObj = new JSONObject();
            if (rLine.has("password")) {
                query = "UPDATE operateri SET  adresa=?, telefon=?, komentar=?, "
                        + "aktivan=?, ime=?, password=? WHERE id=?";
            } else {
                query = "UPDATE operateri  SET adresa=?, telefon=?, komentar=?, "
                        + "aktivan=?, ime=? WHERE id=?";
            }

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("adresa"));
                ps.setString(2, rLine.getString("telefon"));
                ps.setString(3, rLine.getString("komentar"));
                ps.setBoolean(4, rLine.getBoolean("aktivan"));
                ps.setString(5, rLine.getString("ime"));
                if (rLine.has("password")) {
                    ps.setString(6, rLine.getString("password"));
                    ps.setInt(7, rLine.getInt("operaterID"));
                } else {
                    ps.setInt(6, rLine.getInt("operaterID"));
                }

                ps.executeUpdate();
                jObj.put("Message", "OPER_UPDATED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("deleteOper")) {
            jObj = new JSONObject();

            query = "DELETE FROM operateri WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("operID"));
                ps.executeUpdate();
                jObj.put("Message", "OPER_DELETED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("editOperPermission")) {
            jObj = new JSONObject();

            query = "DELETE  FROM operaterDozvole WHERE operaterID = ? AND dozvola= ?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("operaterID"));
                ps.setString(2, rLine.getString("dozvola"));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "INSERT INTO operaterDozvole (dozvola, operaterID, value) VALUES (?,?,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("dozvola"));
                ps.setInt(2, rLine.getInt("operaterID"));
                ps.setBoolean(3, rLine.getBoolean("value"));
                ps.executeUpdate();
                jObj.put("Message", "PERMS_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("getOperPermissions")) {
            jObj = new JSONObject();
            JSONObject objectPerm;

            query = "SELECT * from operaterDozvole WHERE operaterID = ?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("operaterID"));
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    int i = 0;
                    while (rs.next()) {
                        objectPerm = new JSONObject();
                        objectPerm.put("id", rs.getInt("id"));
                        objectPerm.put("dozvola", rs.getString("dozvola"));
                        objectPerm.put("operaterID", rs.getInt("operaterID"));
                        objectPerm.put("value", rs.getBoolean("value"));
                        jObj.put(String.valueOf(i), objectPerm);
                        i++;
                    }
                }

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_internet_paketi")) {
            jObj = new JSONObject();
            JSONObject paketi;

            query = "SELECT * FROM internetPaketi";

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    int i = 0;
                    while (rs.next()) {
                        paketi = new JSONObject();
                        paketi.put("id", rs.getInt("id"));
                        paketi.put("naziv", rs.getString("naziv"));
                        paketi.put("brzina", rs.getString("brzina"));
                        paketi.put("cena", rs.getDouble("cena"));
                        paketi.put("pdv", rs.getDouble("pdv"));
                        paketi.put("opis", rs.getString("opis"));
                        paketi.put("idleTimeout", rs.getString("idleTimeout"));
                        jObj.put(String.valueOf(i), paketi);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("snimi_internet_paket")) {
            jObj = new JSONObject();

            int radID = 0;

            query = "INSERT INTO radgroupreply (groupname, attribute, op, value) VALUES "
                    + "(?,?,?,?)";

            try {
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, "Mikrotik-Rate-Limit");
                ps.setString(3, "=");
                ps.setString(4, rLine.getString("brzina"));
                ps.executeUpdate();

                query = "INSERT INTO radgroupreply (groupname, attribute, op, value) VALUES "
                        + "(?,?,?,?)";
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, "Idle-Timeout");
                ps.setString(3, "=");
                ps.setString(4, rLine.getString("idleTimeout"));
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "INSERT INTO internetPaketi (naziv, brzina, cena, opis,  idleTimeout, pdv) VALUES "
                    + "(?, ?, ?, ?, ?, ?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, rLine.getString("brzina"));
                ps.setDouble(3, rLine.getDouble("cena"));
                ps.setString(4, rLine.getString("opis"));
                ps.setString(5, rLine.getString("idleTimeout"));
                ps.setDouble(6, rLine.getDouble("pdv"));

                ps.executeUpdate();

                jObj.put("Message", "INTERNET_PAKET_SAVED");

            } catch (SQLException e) {
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

            return;
        }

        if (rLine.get("action").equals("update_internet_paket")) {
            jObj = new JSONObject();
            query = "UPDATE radgroupreply SET value = ? WHERE  attribute = ? AND groupname= ?";

            try {
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("brzina"));
                ps.setString(2, "Mikrotik-Rate-Limit");
                ps.setString(3, rLine.getString("naziv"));
                ps.executeUpdate();

                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("idleTimeout"));
                ps.setString(2, "Idle-Timeout");
                ps.setString(3, rLine.getString("naziv"));
                ps.executeUpdate();

                query = "UPDATE internetPaketi SET brzina=?, cena=?, opis=?, idleTimeout=?, pdv=? WHERE id=?";

                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("brzina"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setString(3, rLine.getString("opis"));
                ps.setInt(4, rLine.getInt("idleTimeout"));
                ps.setDouble(5, rLine.getDouble("pdv"));
                ps.setInt(6, rLine.getInt("idPaket"));
                ps.executeUpdate();


                jObj.put("Message", "INTERNET_PAKET_UPDATED");

            } catch (SQLException e) {
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("getDigitalTVPaketi")) {
            jObj = new JSONObject();
            query = "SELECT * FROM  digitalniTVPaketi";

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    JSONObject dtv;
                    int i = 0;
                    while (rs.next()) {
                        dtv = new JSONObject();
                        dtv.put("id", rs.getInt("id"));
                        dtv.put("naziv", rs.getString("naziv"));
                        dtv.put("cena", rs.getDouble("cena"));
                        dtv.put("pdv", rs.getDouble("pdv"));
                        dtv.put("idPaket", rs.getInt("idPaket"));
                        dtv.put("opis", rs.getString("opis"));
                        jObj.put(String.valueOf(i), dtv);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("add_dtv_paket")) {
            jObj = new JSONObject();
            query = "INSERT INTO digitalniTVPaketi (naziv, cena, idPaket, opis, pdv) VALUES "
                    + "(?, ?, ?, ?, ?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setInt(3, rLine.getInt("idPaket"));
                ps.setString(4, rLine.getString("opis"));
                ps.setDouble(5, rLine.getDouble("pdv"));
                ps.executeUpdate();
                jObj.put("Message", "DTV_PAKET_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("edit_dtv_paket")) {
            jObj = new JSONObject();
            query = "UPDATE digitalniTVPaketi SET cena=?, idPaket=?, opis=?, pdv=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setDouble(1, rLine.getDouble("cena"));
                ps.setInt(2, rLine.getInt("idPaket"));
                ps.setString(3, rLine.getString("opis"));
                ps.setDouble(4, rLine.getDouble("pdv"));
                ps.setInt(5, rLine.getInt("id"));

                ps.executeUpdate();

                jObj.put("Message", "PACKET_EDIT_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("save_Box_Paket")) {
            jObj = new JSONObject();
            query = "INSERT INTO paketBox (naziv, DTV_id, DTV_naziv, NET_id, NET_naziv, TEL_id, TEL_naziv, IPTV_id, IPTV_naziv, cena, pdv)"
                    + "VALUES"
                    + "(?,?,?,?,?,?,?,?,?,?,?)";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                if (rLine.has("DTV_id")) {
                    ps.setInt(2, rLine.getInt("DTV_id"));
                    ps.setString(3, rLine.getString("DTV_naziv"));
                } else {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.VARCHAR);
                }

                if (rLine.has("NET_id")) {
                    ps.setInt(4, rLine.getInt("NET_id"));
                    ps.setString(5, rLine.getString("NET_naziv"));
                } else {
                    ps.setNull(4, Types.INTEGER);
                    ps.setNull(5, Types.VARCHAR);
                }

                if (rLine.has("FIX_id")) {
                    ps.setInt(6, rLine.getInt("FIX_id"));
                    ps.setString(7, rLine.getString("FIX_naziv"));
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.VARCHAR);
                }

                if (rLine.has("IPTV_id")) {
                    ps.setInt(8, rLine.getInt("IPTV_id"));
                    ps.setString(9, rLine.getString("IPTV_naziv"));
                } else {
                    ps.setNull(8, Types.INTEGER);
                    ps.setNull(9, Types.VARCHAR);
                }

                ps.setDouble(10, rLine.getDouble("cena"));
                ps.setDouble(11, rLine.getDouble("pdv"));
                ps.executeUpdate();
                jObj.put("Message", "BOX_SAVED");

            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("show_fixTel_paketi")) {
            JSONObject paket;
            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement("SELECT * FROM FIX_paketi");
                rs = ps.executeQuery();
                int i = 0;
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        paket = new JSONObject();
                        paket.put("id", rs.getInt("id"));
                        paket.put("naziv", rs.getString("naziv"));
                        paket.put("pretplata", rs.getDouble("pretplata"));
                        paket.put("pdv", rs.getDouble("pdv"));
                        paket.put("besplatniMinutiFiksna", rs.getInt("besplatniMinutiFiksna"));
                        jObj.put(String.valueOf(i), paket);
                        i++;
                    }
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        //FIKSNA TLEFONIJA PAKETI
        if (rLine.get("action").equals("add_fixTel_paket")) {
            jObj = new JSONObject();
            try {
                ps = db.conn.prepareStatement("INSERT INTO FIX_paketi"
                        + "(naziv, pretplata, PDV, besplatniMinutiFiksna) VALUES (?,?,?,?)");
                ps.setString(1, rLine.getString("naziv"));
                ps.setDouble(2, rLine.getDouble("pretplata"));
                ps.setDouble(3, rLine.getDouble("pdv"));
                ps.setInt(4, rLine.getInt("besplatniMinutiFiksna"));
                ps.executeUpdate();
                ps.close();
                jObj.put("Message", String.format("Paket %s snimljen", rLine.getString("naziv")));

            } catch (SQLException e) {
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("del_fixTel_paket")) {
            jObj = new JSONObject();
            String query = "DELETE FROM FIX_paketi WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("id"));
                ps.executeUpdate();
                ps.close();
                jObj.put("Message", "PAKET_DELETED");

            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.get("action").equals("edit_fixTel_paket")) {
            jObj = new JSONObject();
            String query = "UPDATE FIX_paketi SET naziv=?, pretplata=?, PDV=?, besplatniMinutiFiksna=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setDouble(2, rLine.getDouble("pretplata"));
                ps.setDouble(3, rLine.getDouble("pdv"));
                ps.setInt(4, rLine.getInt("besplatniMinutiFiksna"));
                ps.setInt(5, rLine.getInt("id"));
                ps.executeUpdate();
                ps.close();
                jObj.put("Message", "PAKET_EDIT_SAVED");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        //FIXNA OBRACUNI
        if (rLine.get("action").equals("obracun_fiksne_zaMesec")) {

        }

        if (rLine.getString("action").equals("addFixUslugu")) {
            jObj = new JSONObject();
            String message = ServicesFunctions.addServiceFIX(rLine, getOperName(), db);
            jObj.put("Message", message);
            send_object(jObj);
            return;

        }

        if (rLine.get("action").equals("add_CSV_FIX_Telefonija")) {
            CsvReader csvReader = null;
            PreparedStatement ps = null;
            String query = "INSERT INTO csv (account,  `from`, `to`, country, description, connectTime, chargedTimeMS, "
                    + "chargedTimeS, chargedAmountRSD, serviceName, chargedQuantity, serviceUnit, customerID, fileName)"
                    + "VALUES"
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            jObj = new JSONObject();
            for (String key : rLine.keySet()) {
                try {
                    csvReader = new CsvReader(new StringReader((String) rLine.get(key)));
                    csvReader.setDelimiter(',');
                    csvReader.readHeaders();
                    ps = db.conn.prepareStatement(query);
                    while (csvReader.readRecord()) {
                        //ako je csv fajl  na kraju prekinuti import
                        if (csvReader.get("Account").equals("SUBTOTAL") || csvReader.get("Account").isEmpty()
                                || csvReader.get("Service Name").equals("Payments")) {
                            break;
                        }
                        if (Double.parseDouble(csvReader.get("Charged Amount, RSD")) < 0) {
                            continue;
                        }
                        String filename = key;
                        String customerID = key.substring(key.lastIndexOf("-"));
                        customerID = customerID.replace("-customer", "");
                        customerID = customerID.replace(".csv", "");

                        ps.setString(1, csvReader.get("Account"));
                        ps.setString(2, csvReader.get("From"));
                        ps.setString(3, csvReader.get("To"));
                        if (csvReader.get("Country").isEmpty()) {
                            ps.setString(4, "Lokalni poziv");
                        } else {
                            ps.setString(4, csvReader.get("Country"));
                        }
                        ps.setString(5, csvReader.get("Description"));
                        ps.setString(6, csvReader.get("Connect Time"));
                        ps.setString(7, csvReader.get("Charged Time, min:sec"));
                        ps.setInt(8, Integer.parseInt(csvReader.get("Charged Time, sec.")));
                        ps.setDouble(9, Double.parseDouble(csvReader.get("Charged Amount, RSD")));
                        ps.setString(10, csvReader.get("Service Name"));
                        ps.setInt(11, Integer.parseInt(csvReader.get("Charged quantity")));
                        ps.setString(12, csvReader.get("Service unit"));
                        ps.setString(13, customerID);
                        ps.setString(14, filename);

                        ps.executeUpdate();

                    }
                    ps.close();
                    jObj.put("Mesage", "CSV_IMPORT_SUCCESS");
                } catch (FileNotFoundException e) {
                    jObj.put("ERROR", e.getMessage());
                    e.printStackTrace();
                } catch (SQLException e) {
                    jObj.put("ERROR", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    jObj.put("ERROR", e.getMessage());
                    e.printStackTrace();
                }

            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_CSV_Data")) {
            jObj = new JSONObject();
            PreparedStatement ps;
            ResultSet rs;
            String query = "SELECT * FROM csv";
            int i = 0;
            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    JSONObject csvData;
                    while (rs.next()) {
                        csvData = new JSONObject();
                        csvData.put("id", rs.getInt("id"));
                        csvData.put("account", rs.getString("account"));
                        csvData.put("from", rs.getString("from"));
                        csvData.put("to", rs.getString("to"));
                        csvData.put("country", rs.getString("country"));
                        csvData.put("description", rs.getString("description"));
                        csvData.put("connectTime", rs.getString("connectTime"));
                        csvData.put("chargedTimeMS", rs.getString("chargedTimeMS"));
                        csvData.put("chargedTimeS", rs.getInt("chargedTimeS"));
                        csvData.put("chargedAmountRSD", rs.getDouble("chargedAmountRSD"));
                        csvData.put("serviceName", rs.getString("serviceName"));
                        csvData.put("chargedQuantity", rs.getInt("chargedQuantity"));
                        csvData.put("serviceUnit", rs.getString("serviceUnit"));
                        csvData.put("customerID", rs.getString("customerID"));
                        csvData.put("fileName", rs.getString("fileName"));
                        jObj.put(String.valueOf(i), csvData);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("ERROR", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        //END FIKSNA TELEFONIJA PAKETI
        //IPTV
        //test
        if (rLine.getString("action").equals("test_REST_API")) {
            StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
            //stalkerRestAPI2.changeMac(1, "00:1A:79:00:39:EE");
            //stalkerRestAPI2.checkUser(rLine.getString("STB_MAC"));
            //stalkerRestAPI2.changeMac("5", "00:11:33:44:55:11");
            //jObj = stalkerRestAPI2.getUsersData(1);
            JSONObject UserObj = new JSONObject();
            UserObj.put("login", "TEST_IZ_APIJA");
            UserObj.put("full_name", "API_NAME_TEST");
            UserObj.put("userID", 299);
            UserObj.put("tariff_plan", "Apsolutno SVE");
            UserObj.put("password", "passwordNekiAPI");
            UserObj.put("STB_MAC", "00:00:00:02:02:03");
            UserObj.put("status", 0);
            UserObj.put("end_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            //jObj = stalkerRestAPI2.saveUSER(UserObj);
            //jObj = stalkerRestAPI2.setEndDate(UserObj.getString("STB_MAC"), UserObj.getString("end_date"));
            //jObj = stalkerRestAPI2.deleteAccount(UserObj.getString("STB_MAC"));
            jObj = stalkerRestAPI2.changeMac(UserObj.getInt("userID"), "00:00:00:02:02:03");
            //boolean chkSUser = stalkerRestAPI2.checkUser(UserObj.getString("STB_MAC"));

            //stalkerRestAPI2.activateStatus(true, UserObj.getString("STB_MAC"));
            //String a = stalkerRestAPI2.get_end_date(UserObj.getString("STB_MAC"));
            System.out.println("STLKER_API_RESPONSE: " + jObj.toString());
            jObj = new JSONObject();
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("snimiNovIPTVPaket")) {
            jObj = new JSONObject();

            PreparedStatement ps;
            String query = "INSERT INTO IPTV_Paketi (name, iptv_id, external_id,"
		    + " cena, pdv, opis) "
                    + "VALUES"
                    + " (?,?,?,?,?,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("nazivPaketa"));
                ps.setInt(2, rLine.getInt("iptv_id"));
                ps.setString(3, rLine.getString("external_id"));
                ps.setDouble(4, rLine.getDouble("cena"));
		ps.setDouble(5, rLine.getDouble("pdv"));
                ps.setString(6, rLine.getString("opis"));
                ps.executeUpdate();
                rs.close();
                jObj.put("Message", "PAKET_SAVED");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("snimiEditIPTVPaket")) {
            jObj = new JSONObject();

            PreparedStatement ps;
            String query = "UPDATE IPTV_Paketi SET "
                    + "name = ?, iptv_id=? , external_id=?, cena=?, pdv=? "
		    + "opis=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("name"));
                ps.setInt(2, rLine.getInt("iptv_id"));
                ps.setString(3, rLine.getString("external_id"));
                ps.setDouble(4, rLine.getDouble("cena"));
                ps.setString(5, rLine.getString("opis"));
		ps.setDouble(6, rLine.getDouble("pdv"));
                ps.setInt(7, rLine.getInt("id"));

                ps.executeUpdate();
                rs.close();

            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            jObj.put("Message", "PAKET_EDITED");

            send_object(jObj);
            return;

        }

        if (rLine.getString("action").equals("getIPTVPakets")) {
            jObj = new JSONObject();
            StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db);

            send_object(stAPI2.getPakets_ALL());
            return;

        }

        if (rLine.getString("action").equals("getIPTVDataLocal")) {
            jObj = new JSONObject();
            PreparedStatement ps;
            ResultSet rs;
            String query = "SELECT  * FROM IPTV_Paketi";

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
                int i = 0;
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        JSONObject paketObj = new JSONObject();
                        paketObj.put("name", rs.getString("name"));
                        paketObj.put("cena", rs.getString("cena"));
                        paketObj.put("opis", rs.getString("opis"));
                        paketObj.put("external_id", rs.getString("external_id"));
                        paketObj.put("id", rs.getInt("id"));
                        paketObj.put("IPTV_id", rs.getInt("iptv_id"));
                        paketObj.put("pdv", rs.getDouble("pdv"));
                        jObj.put(String.valueOf(i), paketObj);
                        i++;
                    }
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("getIPTVUsers")) {
            jObj = new JSONObject();
            StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db);

        }

        if (rLine.getString("action").equals("save_IPTV_USER")) {
            JSONObject jObj;
            StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db);
            jObj = stAPI2.saveUSER(rLine);
            ServicesFunctions.addServiceIPTV(rLine, operName, db);
            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("get_paket_IPTV")) {
            jObj = new JSONObject();
            PreparedStatement ps;
            ResultSet rs;
            String query = "SELECT  * FROM IPTV_Paketi";

            try {
                ps = db.conn.prepareStatement(query);
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    int i = 0;
                    while (rs.next()) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", rs.getInt("id"));
                        obj.put("name", rs.getString("name"));
                        obj.put("external_id", rs.getString("external_id"));
                        obj.put("cena", rs.getDouble("cena"));
                        obj.put("pdv", rs.getDouble("pdv"));
                        obj.put("opis", rs.getString("opis"));
                        jObj.put(String.valueOf(i), obj);
                        i++;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("check_fix_obracun")) {
            jObj = new JSONObject();
            boolean exist = false;
            PreparedStatement ps;
            ResultSet rs;

            String query = "SELECT * FROM userDebts WHERE zaMesec=? AND paketType LIKE '%FIX'";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("zaMesec"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    exist = true;
                }
                ps.close();
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            jObj.put("exist", exist);

            send_object(jObj);
            return;
        }

        if (rLine.getString("action").equals("obracunaj_za_mesec")) {
            jObj = new JSONObject();

            jObj = FIXFunctions.obracunajZaMesec(db, rLine.getString("zaMesec"), getOperName());

            send_object(jObj);
            return;
        }
    }

    private void setUserFirma(int userID, boolean hasFirma) {
        PreparedStatement ps;
        String query = "UPDATE users SET firma=? WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setBoolean(1, hasFirma);
            ps.setInt(2, userID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Double get_userDebt(int userID) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT dug, uplaceno FROM userDebts WHERE userID=? ";
        double dug = 0.00;
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    dug += (rs.getDouble("dug") - rs.getDouble("uplaceno"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dug;
    }

    private String get_paket_naziv(String digitalniTVPaket, int dtv_id) {
        String naziv = "";
        try {
            if (digitalniTVPaket.equals("FIXPaketi")) {
                String query = String.format("SELECT * FROM FIX_paketi WHERE id=?", digitalniTVPaket);
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, dtv_id);
            } else {
                ps = db.conn.prepareStatement("SELECT * FROM " + digitalniTVPaket + " WHERE id=?");
                ps.setInt(1, dtv_id);
            }
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                if (digitalniTVPaket.equals("IPTV_Paketi")) {
                    naziv = rs.getString("name");
                } else {
                    naziv = rs.getString("naziv");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return naziv;
    }

    private void delete_user(JSONObject mes) {
        int userId = mes.getInt("userId");
        query = "DELETE FROM users WHERE id=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //delete from Services_user
        query = "DELETE FROM Services_User, user_debts,  WHERE  userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        jObj = new JSONObject();
        jObj.put("message", "USER_DELETED");
        send_object(jObj);
        return;
    }

    private void update_user(JSONObject jObju) {
        jObj = new JSONObject();
        int userID = jObju.getInt("userID");
        query = "UPDATE users SET ime = ?, datumrodjenja = ?, adresa = ?, mesto = ?,"
                + " postbr = ?, telFiksni = ?, telMobilni = ?,  brlk = ?,  JMBG =?, adresaUsluge = ?, "
                + "mestoUsluge = ?, jAdresaBroj=?, jAdresa = ?, jMesto=?, jBroj=?, "
                + "komentar = ? WHERE id = ? ";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, jObju.getString("fullName"));
            ps.setString(2, jObju.getString("datumRodjenja"));
            ps.setString(3, jObju.getString("adresa"));
            ps.setString(4, jObju.getString("mesto"));
            ps.setString(5, jObju.getString("postBr"));
            ps.setString(6, jObju.getString("telFiksni"));
            ps.setString(7, jObju.getString("telMobilni"));
            ps.setString(8, jObju.getString("brLk"));
            ps.setString(9, jObju.getString("JMBG"));
            ps.setString(10, jObju.getString("adresaRacuna"));
            ps.setString(11, jObju.getString("mestoRacuna"));
            ps.setString(12, jObju.getString("jAdresaBroj"));
            ps.setString(13, jObju.getString("jAdresa"));
            ps.setString(14, jObju.getString("jMesto"));
            ps.setString(15, jObju.getString("jBroj"));
            ps.setString(16, jObju.getString("komentar"));
            ps.setInt(17, userID);
            ps.executeUpdate();
            jObj.put("Message", String.format("USER: %s UPDATED", userID));
            ps.close();

        } catch (SQLException e) {
            jObj.put("Message", "ERROR_USER_NOT_UPDATED");
            jObj.put("Error", e.getMessage());
            e.printStackTrace();
        }

        // db.query = query;
        // db.executeUpdate();
        send_object(jObj);
        return;
    }

    private Boolean check_Login(String username, String password) {
        String userName = null;
        String passWord = null;
        boolean aktivan = false;
        this.setOperName(userName);

        try {
            ps = db.conn.prepareStatement("SELECT username,password, aktivan FROM operateri WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                userName = rs.getString("username");
                passWord = rs.getString("password");
                aktivan = rs.getBoolean("aktivan");

            } else {
                userName = null;
                passWord = null;
                aktivan = false;
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (userName != null && passWord != null) {
            if (userName.equals(username) && passWord.equals(password) && aktivan == true) {
                client_authenticated = true;
                this.operName = userName;
            }
        } else {
            LOGGER.info(String.format("Login Error, User: %s  Pass: "
                    + "%s Client: ", username, password, client
                    .getRemoteSocketAddress()));
            client_authenticated = false;
            jObj = new JSONObject();
            jObj.put("Message", "LOGIN_FAILED");
            send_object(jObj);
            try {

                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return client_authenticated;
    }

    public void send_object(JSONObject obj) {
        LOGGER.info("Sending Object: " + obj.toString());

        if (client.isClosed()) {
            LOGGER.info("CLIENT DISCONNECTED!!");
            try {
                client.close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {

            Bfw.write(obj.toString());
            Bfw.newLine();
            Bfw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
