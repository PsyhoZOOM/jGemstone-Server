package classes;

import JGemstone.classes.Groups;
import JGemstone.classes.Services;
import JGemstone.classes.Uplate;
import JGemstone.classes.Users;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by PsyhoZOOM on 8/8/16.
 */
public class ClientWorker implements Runnable {

    public boolean DEBUG = false;
    public boolean client_db_update = false;
    private Logger LOGGER = LogManager.getLogger("CLIENT");
    private Socket client;
    private InputStreamReader Isr = null;
    private OutputStreamWriter Osw = null;
    private BufferedReader Bfr;
    private BufferedWriter Bfw;
    private database db;
    private ResultSet rs;
    private PreparedStatement ps;
    private String query;
    private Users user = null;
    private Groups groups = null;
    private Services services = null;
    private ArrayList<Services> servicesArrayList;
    private Uplate uplate = null;
    private ArrayList<Uplate> uplateArrayList;
    private String operName;
    private boolean client_authenticated = false;
    private Date date;
    private SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");


    ///JSON PART
    private JSONObject jObj;

    //JSON Grupa

    private JSONObject jGrupe;

    //JSON Services
    private JSONObject jsService;

    //JSON Users
    private JSONObject jUsers;

    //JSON Uplate
    private JSONObject jUplate;


    public ClientWorker(Socket client) {
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


        System.out.println(String.format("CLient connected: %s", this.client
                .getRemoteSocketAddress()));
        while (!client.isClosed()) {

            if (Isr == null) {
                try {
                    Isr = new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8);
                    Bfr = new BufferedReader(Isr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Waitin for client data..");
            try {
                String A = Bfr.readLine();
                //jObj=new JSONObject(Bfr.readLine());
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
                Osw = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);
                Bfw = new BufferedWriter(Osw);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (rLine.get("action").equals("login")) {
            client_authenticated = check_Login(rLine.get("username").toString(), rLine.get("password").toString());
            this.operName = rLine.get("username").toString();
            if (client_authenticated) {
                jObj = new JSONObject();
                jObj.put("Message", "LOGIN_OK");
                send_object(jObj);
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
                LOGGER.error(e.getMessage());
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
                        jObj.put("jBroj", rs.getString("jMesto") + rs.getString("jAdresa") + rs.getInt("id"));
                        jObj.put("jAdresa", rs.getString("jAdresa"));
                        jObj.put("jAdresaBroj", rs.getString("jAdresaBroj"));
                        jObj.put("jMesto", rs.getString("jMesto"));

                        LOGGER.info(jObj);

                    } catch (SQLException e) {
                        LOGGER.error(e.getMessage());
                    }
                    jUsers.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jUsers);
        }

        if (rLine.get("action").equals("get_user_data")) {
            query = "SELECT * FROM users WHERE id=?";
            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userId"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
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
                    jObj.put("jBroj", rs.getString("jMesto") + rs.getString("jAdresa") + rs.getInt("id"));

                } else {
                    jObj.put("Message", "NO_SUCH_USER");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
        }

        if (rLine.get("action").equals("get_groups")) {
            query = String.format("SELECT * FROM grupa WHERE groupname LIKE '%s%%'", rLine.get("groupName"));
            query = "SLEECT * FROM  grupa WHERE groupname LIKE ?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("groupName"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
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
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }
            send_object(jGrupe);
        }

        if (rLine.get("action").equals("get_group_data")) {
            query = "SELECT * FROM grupa WHERE id LIKE ?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("groupId"));
                rs = ps.executeQuery();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }

            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("groupName", rs.getString("groupName"));
                    jObj.put("cena", rs.getString("cena"));
                    jObj.put("prepaid", rs.getInt("prepaid"));
                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }

            send_object(jObj);
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

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }

            jObj = new JSONObject();
            jObj.put("message", "SAVED");
            send_object(jObj);

        }


        if (rLine.get("action").equals("delete_group")) {

            query = "DELETE FROM grupa WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("groupID"));
                ps.executeQuery();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }

            jObj = new JSONObject();
            jObj.put("message", String.format("GRUPA IZBRISANA"));

            send_object(jObj);
        }

        if (rLine.getString("action").equals("save_group")) {

            query = "INSERT INTO grupa (groupname, cena, prepaid, opis) VALUES (?,?,?,?,)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("groupName"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setInt(3, rLine.getInt("prepaid"));
                ps.setString(4, rLine.getString("opis"));

                ps.executeQuery();

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }

            jObj = new JSONObject();
            jObj.put("message", String.format("GROUP %s SAVED", rLine.getString("groupName")));
            send_object(jObj);
        }


        if (rLine.get("action").equals("update_user")) {
            update_user(rLine);
        }

        if(rLine.getString("action").equals("get_firma")){
            query = "SELECT * FROM firme WHERE userId=?";

            jObj = new JSONObject();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userId"));
                LOGGER.info(db.ps.toString());
                rs= db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if(rs.isBeforeFirst()){
                    try {
                        rs.next();
                        jObj.put("nazivFirme", rs.getString("nazivFirme"));
                        jObj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
                        jObj.put("kodBanke", rs.getString("kodBanke"));
                        jObj.put("pib", rs.getString("pib"));
                        jObj.put("maticniBrFirme", rs.getString("maticniBrFirme"));
                        jObj.put("brTekuciRacun", rs.getString("brTekuciRacun"));
                        jObj.put("brojFakture", rs.getString("brojFakture"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else{
                    jObj.put("Message", "NO_DATA");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);



        }

        if(rLine.get("action").equals("save_firma")){
            query = "DELETE FROM firme WHERE userId=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userID"));
                db.ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "INSERT INTO firme (nazivFirme, kontaktOsoba, kodBanke, pib, maticniBrFirme," +
                    "brTekuciRacun, userId, brojFakture) VALUE (?, ?, ?, ?, ?, ?, ?, ?)";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1,rLine.getString("nazivFirme"));
                db.ps.setString(2,rLine.getString("kontaktOsoba"));
                db.ps.setString(3,rLine.getString("kodBanke"));
                db.ps.setString(4,rLine.getString("pib"));
                db.ps.setString(5,rLine.getString("maticniBrFirme"));
                db.ps.setString(6,rLine.getString("brTekuciRacun"));
                db.ps.setInt(7, rLine.getInt("userID"));
                db.ps.setInt(8, rLine.getInt("brojFakture"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("Message", "USER_FIRMA_SAVED");
            send_object(jObj);


        }

        if (rLine.getString("action").equals("delete_user")) {
            delete_user(rLine);

        }

        if (rLine.getString("action").equals("new_user")) {


            query = "INSERT INTO users (ime, datumRodjenja, operater, postBr, mesto, brLk, JMBG, " +
                    "adresa,  komentar, telFiksni, telMobilni, datumKreiranja)" +
                    "VALUES (?, ?, ?, ?, ?, ? ,? ,? ,? ,?, ?, ?)";



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
                LOGGER.error(e.getMessage());
            }

            jObj = new JSONObject();
            try {
                rs = ps.getGeneratedKeys();
                rs.next();
                jObj.put("Message", "user_saved");
                jObj.put("userID", rs.getInt(1));
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("ERROR_MESSAGE", e.getMessage());
                e.printStackTrace();
            }


            send_object(jObj);
        }

        if (rLine.get("action").equals("get_services")) {
            query = String.format("SELECT * FROM Services WHERE naziv LIKE '%s%%'", rLine.get("serviceName"));
            System.out.println(query);
            rs = db.query_data(query);
            jsService = new JSONObject();
            int jsCount = 0;
            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("naziv", rs.getString("naziv"));
                    jObj.put("cena", rs.getInt("cena"));
                    jObj.put("opis", rs.getString("opis"));
                    jsService.put(String.valueOf(jsCount), jObj);
                    jsCount++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println(jsService);

            send_object(jsService);
        }

        if (rLine.getString("action").equals("get_user_services")) {
            ArrayList<Services> services_id = new ArrayList<Services>();
            Services srv_id;
            query = "SELECT id, id_service FROM Services_User WHERE userID=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userID"));
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JSONObject jsServicesArray = new JSONObject();

            try {
                while (rs.next()) {
                    srv_id = new Services();
                    srv_id.setId(rs.getInt("id"));
                    srv_id.setService_id(rs.getInt("id_service"));
                    services_id.add(srv_id);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < services_id.size(); i++) {
                query = String.format("SELECT * FROM Services WHERE id='%d'", services_id.get(i).getService_id());
                LOGGER.info("query: " + query + String.format("service id: %d", services_id.get(i).getService_id()));
                rs = db.query_data(query);
                jObj = new JSONObject();

                try {
                    rs.next();
                    jObj.put("id", services_id.get(i).getId());
                    jObj.put("serviceId", services_id.get(i).getService_id());
                    jObj.put("naziv", rs.getString("naziv"));
                    jObj.put("cena", Double.valueOf(rs.getInt("cena")));
                    jObj.put("opis", rs.getString("opis"));
                    jsServicesArray.put(String.valueOf(i), jObj);
                    LOGGER.info(String.format("Sservices int: %d", i));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            LOGGER.info("SERVISIIII: " + jsServicesArray);
            send_object(jsServicesArray);
        }

        if (rLine.get("action").equals("get_services_data")) {
            query = String.format("SELECT * FROM Services where id='%d'", rLine.getInt("serviceId"));
            System.out.println(query);
            rs = db.query_data(query);
            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("naziv", rs.getString("naziv"));
                    jObj.put("cena", rs.getString("cena"));
                    jObj.put("opis", rs.getString("opis"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
        }

        if (rLine.get("action").equals("new_service")) {
            query = String.format("INSERT INTO Services (naziv,cena,opis) VALUES ('%s', '%d', '%s')",
                    rLine.getString("naziv"), (int) rLine.getDouble("cena"), rLine.getString("opis"));
            db.query = query;
            db.executeUpdate();

            jObj = new JSONObject();
            jObj.put("message", "SERVICE_SAVED");
            send_object(jObj);
            //send_object(mes);

        }

        if (rLine.get("action").equals("delete_service")) {
            query = String.format("DELETE FROM Services WHERE id='%s' ", rLine.getInt("serviceId"));
            db.query = query;
            db.executeUpdate();

            query = String.format("DELETE * FROM Service_User where service_id='%d'", rLine.getInt("serviceId"));

            jObj = new JSONObject();
            jObj.put("message", String.format("SERVICE ID:%d DELETED", rLine.getInt("serviceId")));
            send_object(jObj);

        }

        if (rLine.get("action").equals("save_service")) {
            query = String.format("UPDATE Services SET naziv='%s', cena='%s', opis='%s' WHERE id='%s'",
                    rLine.getString("naziv"), (int) rLine.getDouble("cena"), rLine.getString("opis"),
                    rLine.getInt("serviceId"));

            db.query = query;
            db.executeUpdate();
            jObj = new JSONObject();
            jObj.put("message", "SERVICE_UPDATED");
            send_object(jObj);
        }

        if (rLine.getString("action").equals("add_service_to_user")) {

            jObj = new JSONObject();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GregorianCalendar calendar = new GregorianCalendar();
            Date date = new Date();
            calendar.setTime(date);
            String datum = format.format(calendar.getTime());

            query = "INSERT into ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, paketType, cena, obracun) " +
                    "VALUES (?, ? ,?, ?, ?, ?, ?, ?, ?)";


            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("id"));
                ps.setString(2, rLine.getString("nazivPaketa"));
                ps.setString(3, String.valueOf(datum));
                ps.setInt(4, rLine.getInt("userID"));
                ps.setString(5, getOperName());
                ps.setDouble(6, (rLine.getDouble("servicePopust")));
                ps.setString(7, rLine.getString("paketType"));
                ps.setDouble(8, rLine.getDouble("cena"));
                ps.setBoolean(9, rLine.getBoolean("obracun"));
                ps.executeUpdate();

                jObj.put("Message", String.format("Service id: %d added to user: %d", rLine.getInt("id"), rLine.getInt("userID")));
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }


            send_object(jObj);

        }

        if (rLine.getString("action").equals("delete_service_user")) {
            query = "DELETE FROM Services_User WHERE id=?";


            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("id"));
                db.ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            jObj = new JSONObject();
            jObj.put("message", String.format("Service id:%s deleted", rLine.getInt("id")));

            send_object(jObj);
        }

        if (rLine.getString("action").equals("get_uplate_user")) {
            jObj = new JSONObject();
            query = "SELECT * FROM userDebts where userID=? ORDER BY datumZaduzenja DESC";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    JSONObject userDebt;
                    int i = 0;
                    while (rs.next()) {
                        double cena = valueToPercent.getValue(rs.getDouble("cena"), rs.getDouble("popust"));
                        double zaUplatu = cena - rs.getDouble("uplaceno");


                        userDebt = new JSONObject();
                        userDebt.put("id", rs.getInt("id"));
                        userDebt.put("id_ServiceUser", rs.getInt("id_ServiceUser"));
                        userDebt.put("id_service", rs.getInt("id_service"));
                        userDebt.put("nazivPaketa", rs.getString("nazivPaketa"));
                        userDebt.put("datumZaduzenja", rs.getDate("datumZaduzenja"));
                        userDebt.put("userID", rs.getInt("userID"));
                        userDebt.put("popust", rs.getDouble("popust"));
                        userDebt.put("paketType", rs.getString("paketType"));
                        userDebt.put("cena", rs.getDouble("cena"));
                        userDebt.put("uplaceno", rs.getDouble("uplaceno"));
                        userDebt.put("datumUplate", rs.getString("datumUplate"));
                        userDebt.put("zaUplatu", zaUplatu);
                        userDebt.put("operater", rs.getString("operater"));
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

        }



        if (rLine.getString("action").equals("add_new_ugovor")) {
            query = "INSERT INTO ugovori_types " +
                    "(naziv,  text_ugovor)" +
                    " VALUES (?,?)";
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

        }

        if(rLine.getString("action").equals("get_single_ugovor")){
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


        }

        if(rLine.getString("action").equals("delete_ugovor")){
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


        }

        if(rLine.getString("action").equals("add_user_ugovor")){
            query = "INSERT INTO ugovori (pocetak, kraj, komentar, operater, userID, br, ugovori_text, nazivUgovora) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("odDate"));
                db.ps.setString(2, rLine.getString("doDate"));
                db.ps.setString(3, rLine.getString("komentar"));
                db.ps.setString(4, getOperName());
                db.ps.setInt(5, rLine.getInt("userID"));
                db.ps.setString(6, rLine.getString("brUgovora"));
                db.ps.setString(7, rLine.getString("textUgovora"));
                db.ps.setString(8, rLine.getString("nazivUgovora"));
                db.ps.executeUpdate();


            } catch (SQLException e) {
                e.printStackTrace();
            }
            jObj = new JSONObject();
            jObj.put("Message","UGOVOR_ADDED_TO_USER");
            send_object(jObj);
        }

        if(rLine.getString("action").equals("get_ugovor_single_user")){
            query = "SELECT * FROM ugovori WHERE id=?";
            try{
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("idUgovora"));
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            try {
                rs.next();
                jObj.put("textUgovora", rs.getString("ugovori_text"));
                jObj.put("id", rs.getInt("id"));
                jObj.put("pocetak", rs.getString("pocetak"));
                jObj.put("kraj", rs.getString("kraj"));
                jObj.put("komentar", rs.getString("komentar"));
                jObj.put("operater", rs.getString("operater"));
                jObj.put("userID", rs.getString("userID"));
                jObj.put("nazivUgovora", rs.getString("nazivUgovora"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
        }

        if(rLine.getString("action").equals("get_ugovori_user")){
            query = "SELECT * FROM ugovori WHERE userID=? ORDER BY id DESC";

            try{
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userID"));
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JSONObject ugovoryArr= new JSONObject();
            int i=0;

            try {
                while(rs.next()){
                    jObj=new JSONObject();
                    jObj.put("idUgovora", rs.getInt("id"));
                    jObj.put("pocetakUgovora", rs.getString("pocetak"));
                    jObj.put("krajUgovora", rs.getString("kraj"));
                    jObj.put("komentar", rs.getString("komentar"));
                    jObj.put("operater", rs.getString("operater"));
                    jObj.put("userID", rs.getString("userID"));
                    jObj.put("brUgovora", rs.getInt("br"));
                    jObj.put("textUgovora", rs.getString("ugovori_text"));
                    jObj.put("nazivUgovora", rs.getString("nazivUgovora"));
                    ugovoryArr.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(ugovoryArr);
        }

        if(rLine.getString("action").equals("update_ugovor")){
            query = "UPDATE ugovori set ugovori_text=? WHERE id=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("textUgovora"));
                db.ps.setInt(2, rLine.getInt("idUgovora"));
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            jObj= new JSONObject();
            jObj.put("Message", "UGOVOR_USER_UPDATED");
            send_object(jObj);
        }

        if(rLine.getString("action").equals("saveOprema")){
            query = "INSERT INTO oprema (naziv, model, komentar, userId) VALUES" +
                    "(?,?,?,?)";

            try {
                db.ps =  db.conn.prepareStatement(query);
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

        }

        if(rLine.getString("action").equals("get_fakture")){
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

            int i=0;
            try {
                while(rs.next()) {

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
                    jObj.put("VrednostSaPDV" ,VrednostSaPDV);

                    FaktureObj.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(FaktureObj);


        }

        if(rLine.getString("action").equals("delete_fakturu")){
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
        }

        if(rLine.getString("action").equals("snimiFakturu")){
            query = "INSERT INTO jFakture (vrstaNaziv, jedMere, kolicina, jedCena, stopaPDV, brFakture, godina, userId, dateCreated)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Format formatter  = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setString(1, rLine.getString("nazivMesec"));
                db.ps.setString(2, rLine.getString("jedMere"));
                db.ps.setInt(3, rLine.getInt("kolicina"));
                db.ps.setDouble(4, rLine.getDouble("jedinacnaCena"));
                db.ps.setInt(5,rLine.getInt("stopaPDV"));
                db.ps.setInt(6, rLine.getInt("brFakture"));
                db.ps.setString(7, rLine.getString("godina"));
                db.ps.setInt(8, rLine.getInt("userId"));
                db.ps.setString(9, formatter.format(date));
                LOGGER.info("QUYERY: "+ db.ps.toString());
                db.ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            jObj = new JSONObject();
            jObj.put("Message", "FACTURE_ADDED");
            send_object(jObj);

        }

        if (rLine.getString("action").equals("PING")) {
            jObj = new JSONObject();
            jObj.put("Message", "PONG");
            send_object(jObj);
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
                } else {
                    jObj = new JSONObject();
                    jObj.put("Message", "NO_GROUPS");
                    send_object(jObj);
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

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
                send_object(jObj);
                e.printStackTrace();
            }
        }

        if (rLine.getString("action").equals("getMesto")) {
            query = "SELECT * FROM mesta WHERE broj=?";
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

            } catch (SQLException e) {
                jObj = new JSONObject();
                jObj.put("Message", e.getMessage());
                send_object(jObj);
                e.printStackTrace();
            }
        }

        if (rLine.getString("action").equals("getAdresa")) {
            query = "SELECT * FROM adrese WHERE broj=?";

            jObj = new JSONObject();

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("broj"));
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
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

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

        }

        if (rLine.getString("action").equals("ADD_USER_OPREMA")) {
            jObj = new JSONObject();

            query = "INSERT INTO oprema_korisnik (naziv, model, komentar, userID, sn, MAC, naplata) " +
                    "VALUES " +
                    "(?, ?, ? ,? ,? ,? ,?)";

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
        }
        if (rLine.getString("action").equals("DELETE_USER_OPREMA")) {
            jObj = new JSONObject();
            query = "DELETE FROM oprema_korisnik WHERE id=?";

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

        }

        if (rLine.getString("action").equals("saveOperater")) {
            jObj = new JSONObject();
            query = "INSERT INTO operateri (username,password, adresa, telefon, komentar, aktivan, ime)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

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
        }

        if (rLine.getString("action").equals("updateOperater")) {
            jObj = new JSONObject();
            if (rLine.has("password")) {
                query = "UPDATE operateri SET  adresa=?, telefon=?, komentar=?, " +
                        "aktivan=?, ime=?, password=? WHERE id=?";
            } else {
                query = "UPDATE operateri  SET adresa=?, telefon=?, komentar=?, " +
                        "aktivan=?, ime=? WHERE id=?";
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

        }

        if (rLine.getString("action").equals("deleteOper")) {
            jObj = new JSONObject();

            query = "DELETE FROM operateri WHER id=?";

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
                        paketi.put("opis", rs.getString("opis"));
                        paketi.put("prekoracenje", rs.getInt("prekoracenje"));
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
        }


        if (rLine.getString("action").equals("snimi_internet_paket")) {
            jObj = new JSONObject();

            int radID = 0;

            query = "INSERT INTO radgroupreply (groupname, attribute, op, value) VALUES " +
                    "(?,?,?,?)";


            try {
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, "Mikrotik-Rate-Limit");
                ps.setString(3, "=");
                ps.setString(4, rLine.getString("brzina"));
                ps.executeUpdate();


                query = "INSERT INTO radgroupreply (groupname, attribute, op, value) VALUES " +
                        "(?,?,?,?)";
                ps = db.connRad.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, "Idle-Timeout");
                ps.setString(3, "=");
                ps.setString(4, rLine.getString("idleTimeout"));
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = "INSERT INTO internetPaketi (naziv, brzina, cena, opis, prekoracenje, idleTimeout) VALUES " +
                    "(?, ?, ?, ?, ? ,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setString(2, rLine.getString("brzina"));
                ps.setDouble(3, rLine.getDouble("cena"));
                ps.setString(4, rLine.getString("opis"));
                ps.setInt(5, rLine.getInt("prekoracenje"));
                ps.setString(6, rLine.getString("idleTimeout"));

                ps.executeUpdate();

                jObj.put("Message", "INTERNET_PAKET_SAVED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

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

                query = "UPDATE internetPaketi SET brzina=?, cena=?, opis=?, prekoracenje=?, idleTimeout=? WHERE naziv=?";

                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("brzina"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setString(3, rLine.getString("opis"));
                ps.setInt(4, rLine.getInt("prekoracenje"));
                ps.setInt(5, rLine.getInt("idleTimeout"));
                ps.setString(6, rLine.getString("naziv"));
                ps.executeUpdate();

                jObj.put("Message", "INTERNET_PAKET_UPDATED");

            } catch (SQLException e) {
                jObj.put("Message", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

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
                        dtv.put("idPaket", rs.getInt("idPaket"));
                        dtv.put("opis", rs.getString("opis"));
                        dtv.put("prekoracenje", rs.getInt("prekoracenje"));
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
        }

        if (rLine.get("action").equals("add_dtv_paket")) {
            jObj = new JSONObject();
            query = "INSERT INTO digitalniTVPaketi (naziv, cena, idPaket, opis, prekoracenje) VALUES " +
                    "(?, ?, ?, ? ,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("naziv"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setInt(3, rLine.getInt("idPaket"));
                ps.setString(4, rLine.getString("opis"));
                ps.setInt(5, rLine.getInt("prekoracenje"));

                ps.executeUpdate();
                jObj.put("Message", "DTV_PAKET_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

        }

        if (rLine.get("action").equals("edit_dtv_paket")) {
            jObj = new JSONObject();
            query = "UPDATE digitalniTVPaketi SET cena=?, idPaket=?, opis=?, prekoracenje=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setDouble(1, rLine.getDouble("cena"));
                ps.setInt(2, rLine.getInt("idPaket"));
                ps.setString(3, rLine.getString("opis"));
                ps.setInt(4, rLine.getInt("prekoracenje"));
                ps.setInt(5, rLine.getInt("id"));

                ps.executeUpdate();
                jObj.put("Message", "PACKET_EDIT_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

        }
    }


    private void delete_user(JSONObject mes) {
        int userId = mes.getInt("userId");
        String username = mes.getString("userName");
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
    }

    private void update_user(JSONObject jObju) {
        jObj = new JSONObject();
        int userID = jObju.getInt("userID");
        query = "UPDATE users SET ime = ?, datumrodjenja = ?, adresa = ?, mesto = ?," +
                " postbr = ?, telFiksni = ?, telMobilni = ?,  brlk = ?,  JMBG =?, adresaUsluge = ?, " +
                "mestoUsluge = ?, jAdresaBroj=?, jAdresa = ?, jMesto=?, jBroj=?, " +
                "komentar = ? WHERE id = ? ";


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
            ps.setString(10, jObju.getString("adresaUsluge"));
            ps.setString(11, jObju.getString("mestoUsluge"));
            ps.setString(12, jObju.getString("jAdresaBroj"));
            ps.setString(13, jObju.getString("jAdresa"));
            ps.setString(14, jObju.getString("jMesto"));
            ps.setString(15, jObju.getString("jBroj"));
            ps.setString(16, jObju.getString("komentar"));
            ps.setInt(17, userID);
            ps.executeUpdate();
            jObj.put("Message", String.format("USER: %s UPDATED", userID));

        } catch (SQLException e) {
            jObj.put("Message", "ERROR_USER_NOT_UPDATED");
            jObj.put("Error", e.getMessage());
            e.printStackTrace();
        }

        // db.query = query;
        // db.executeUpdate();


        send_object(jObj);
    }


    private Boolean check_Login(String username, String password) {
        String userName = null;
        String passWord = null;
        boolean aktivan = false;
        System.out.println("checking login" + username + password);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (userName != null && passWord != null) {
            if (userName.equals(username) && passWord.equals(password) && aktivan == true) {
                System.out.println("usr loged in");
                client_authenticated = true;
                this.operName = userName;
            }
        } else {
            LOGGER.info(String.format("Login Error, User: %s  Pass: " +
                    "%s Client: ", username, password, client
                    .getRemoteSocketAddress
                            ()));
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
        System.out.println("Sending OBject: " + obj.toString());
        if (client.isClosed()) {
            LOGGER.warn("CLIENT DISCONNECTED!!");
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
        }

    }


}

