package classes;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
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
    private String operName;
    private boolean client_authenticated = false;
    private Date date;
    private SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");

    private Calendar calendar = Calendar.getInstance();

    private SimpleDateFormat radcheckEndDate = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat radreplyEndDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatMonthDate = new SimpleDateFormat("yyyy-MM");


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

        if (rLine.getString("action").equals("get_firma")) {
            query = "SELECT * FROM firme WHERE userId=?";

            jObj = new JSONObject();

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userId"));
                LOGGER.info(db.ps.toString());
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (rs.isBeforeFirst()) {
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
                } else {
                    jObj.put("Message", "NO_DATA");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);


        }

        if (rLine.get("action").equals("save_firma")) {
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
                db.ps.setString(1, rLine.getString("nazivFirme"));
                db.ps.setString(2, rLine.getString("kontaktOsoba"));
                db.ps.setString(3, rLine.getString("kodBanke"));
                db.ps.setString(4, rLine.getString("pib"));
                db.ps.setString(5, rLine.getString("maticniBrFirme"));
                db.ps.setString(6, rLine.getString("brTekuciRacun"));
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
            jObj = new JSONObject();
            query = "SELECT *  FROM ServicesUser WHERE userID=?";

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
                        service.put("brojUgovora", rs.getInt("brojUgovora"));
                        service.put("cena", rs.getDouble("cena"));
                        service.put("popust", rs.getDouble("popust"));
                        service.put("operName", rs.getString("operName"));
                        service.put("date_added", rs.getString("date_added"));
                        service.put("nazivPaketa", rs.getString("nazivPaketa"));
                        service.put("vrsta", rs.getString("paketType"));
                        service.put("obracun", rs.getBoolean("obracun"));
                        service.put("aktivan", rs.getBoolean("aktivan"));
                        service.put("idUniqueName", rs.getString("id_unique_name"));
                        service.put("produzenje", rs.getInt("produzenje"));
                        service.put("id_service", rs.getInt("id_service"));


                        jObj.put(String.valueOf(i), service);
                        i++;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);


        }

        if (rLine.get("action").equals("activate_service")) {


            int produzenje = rLine.getInt("produzenje");


            try {
                calendar.setTime(normalDate.parse(rLine.getString("endDate")));
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            jObj = new JSONObject();

            //INTERNET SERVICE
            if (rLine.getString("paketType").equals("INTERNET")) {
                query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Auth-Type'";
                try {
                    ps = db.connRad.prepareStatement(query);
                    if (rLine.getBoolean("aktivan")) {
                        ps.setString(1, "Accept");
                    } else {
                        ps.setString(1, "Reject");
                    }
                    ps.setString(2, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Expiration'";
                calendar.add(Calendar.MONTH, produzenje);

                try {
                    ps = db.connRad.prepareStatement(query);
                    String end = radcheckEndDate.format(calendar.getTime());
                    ps.setString(1, radcheckEndDate.format(calendar.getTime()));
                    ps.setString(2, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }

                query = "UPDATE radreply SET value=? WHERE username=? AND attribute='WISPR-Session-Terminate-time'";
                calendar.add(Calendar.MINUTE, -1);

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, radreplyEndDate.format(calendar.getTime()));
                    ps.setString(2, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }

                add_user_debt_first_time(rLine);

            }

            //DTV SERVICE
            if (rLine.getString("paketType").equals("DTV")) {
                query = "UPDATE DTVKartice SET endDate=? where idKartica=?";
                calendar.add(Calendar.MONTH, produzenje);

                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setString(1, normalDate.format(calendar.getTime()));
                    ps.setString(2, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }

                add_user_debt_first_time(rLine);
            }


            query = "UPDATE ServicesUser SET aktivan =? where id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setBoolean(1, rLine.getBoolean("aktivan"));
                ps.setInt(2, rLine.getInt("id"));
                ps.executeUpdate();
                jObj.put("Message", "SERVICE_UPDATED");
                if (rLine.getBoolean("aktivan")) {
                    jObj.put("activate", "aktivna");
                } else {
                    jObj.put("activate", "nije aktivna");
                }

            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }


            send_object(jObj);


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
            int box_ID = 0;
            ResultSet rsBoxId;


            //service internet;
            boolean user_exist = false;


            if (rLine.getString("paketType").equals("INTERNET") && user_exist == false) {
                query = "SELECT * FROM radcheck WHERE username=?";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    rs = ps.executeQuery();
                    if (rs.isBeforeFirst()) {
                        user_exist = true;
                        jObj.put("Error", "USER_EXIST");
                        send_object(jObj);
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT INTO radusergroup (username, groupname, priority ) VALUES " +
                        "(?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, rLine.getString("nazivPaketa"));
                    ps.setInt(3, 1);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT  INTO   radcheck (username, attribute, op, value) VALUES" +
                        "(?,?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, "MD5-Password");
                    ps.setString(3, ":=");
                    ps.setString(4, rLine.getString("password"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT INTO radcheck (username, attribute, op, value) VALUES" +
                        "(?,?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, "Simultaneous-Use");
                    ps.setString(3, ":=");
                    ps.setString(4, "1");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT INTO radcheck (username, attribute, op, value) VALUES " +
                        "(?,?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, "Auth-Type");
                    ps.setString(3, ":=");
                    ps.setString(4, "Reject");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT  INTO radreply (username, attribute, op , value) VALUES" +
                        "(?,?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, "WISPR-Session-Terminate-time");
                    ps.setString(3, "=");
                    ps.setString(4, "1970-1-1T23:59:59");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "INSERT INTO radcheck (username, attribute, op, value) VALUES" +
                        "(?,?,?,?)";

                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.setString(2, "Expiration");
                    ps.setString(3, ":=");
                    ps.setString(4, "01 Jan 1970");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }

            //service DTV

            if (rLine.getString("paketType").equals("DTV")) {
                query = "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate) VALUES " +
                        "(?,?,?,?)";

                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setInt(1, rLine.getInt("idUniqueName"));
                    ps.setInt(2, rLine.getInt("userID"));
                    ps.setInt(3, rLine.getInt("packetID"));
                    ps.setString(4, "1970-01-01");
                    ps.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            if (rLine.getString("paketType").equals("BOX")) {
                query = "INSERT INTO users_Paket_BOX () " +
                        "VALUES " +
                        "()";

            }


            //Service user
            if (user_exist == false && !rLine.getString("paketType").equals("BOX")) {
                query = "INSERT into ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, paketType, cena, obracun, brojUgovora, id_unique_name, aktivan, produzenje ) " +
                        "VALUES (?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


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
                    ps.setInt(10, rLine.getInt("brojUgovora"));
                    ps.setString(11, rLine.getString("idUniqueName"));
                    ps.setBoolean(12, false);
                    ps.setInt(13, rLine.getInt("produzenje"));
                    ps.executeUpdate();


                    jObj.put("Message", String.format("Service id: %d added to user: %d", rLine.getInt("id"), rLine.getInt("userID")));
                } catch (SQLException e) {
                    jObj.put("Message", "ERROR");
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }
            } else {

                //BOX PAKET
                query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, " +
                        "paketType, cena, obracun, brojUgovora, aktivan, produzenje, linkedService)" +
                        "VALUES " +
                        "(?,?,?,?,?,?,?,?,?,?,?,?,?)";

                try {
                    ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, rLine.getInt("id"));
                    ps.setString(2, rLine.getString("nazivPaketa"));
                    ps.setString(3, String.valueOf(datum));
                    ps.setInt(4, rLine.getInt("userID"));
                    ps.setString(5, getOperName());
                    ps.setDouble(6, rLine.getDouble("popust"));
                    ps.setString(7, rLine.getString("paketType"));
                    ps.setDouble(8, rLine.getDouble("cena"));
                    ps.setBoolean(9, rLine.getBoolean("obracun"));
                    ps.setInt(10, rLine.getInt("brojUgovora"));
                    ps.setBoolean(11, false);
                    ps.setInt(12, rLine.getInt("produzenje"));
                    ps.setBoolean(13, false);
                    ps.executeUpdate();
                    rsBoxId = ps.getGeneratedKeys();
                    rsBoxId.next();
                    box_ID = rs.getInt(1);
                } catch (SQLException e) {
                    jObj.put("Error", e.getMessage());
                    e.printStackTrace();
                }

                //BOX DTV
                query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName, paketType, " +
                        "linkedService, linkedServiceID) VALUES (?,?,?,?,?,?,?,?)";

                try {


                    if (rLine.has("DTV_id")) {
                        ps = db.conn.prepareStatement(query);
                        ps.setInt(1, rLine.getInt("DTV_id"));
                        ps.setString(2, rLine.getString("DTV_naziv"));
                        ps.setString(3, String.valueOf(datum));
                        ps.setInt(4, rLine.getInt("userID"));
                        ps.setString(5, getOperName());
                        ps.setString(6, rLine.getString("paketType"));
                        ps.setBoolean(7, true);
                        ps.setInt(8, box_ID);
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                //NET
                query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName paketType," +
                        "linkedService, linkedServiceID) VALUES (?,?,?,?,?,?,?,?)";


                if (rLine.has("NET_id")) {
                    try {
                        ps = db.conn.prepareStatement(query);
                        ps.setInt(1, rLine.getInt("NET_id"));
                        ps.setString(2, rLine.getString("DTV_naziv"));
                        ps.setString(3, String.valueOf(datum));
                        ps.setInt(4, rLine.getInt("userID"));
                        ps.setString(5, getOperName());
                        ps.setBoolean(7, true);
                        ps.setInt(9, box_ID);
                        ps.executeUpdate();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }


            }


            send_object(jObj);

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
                        if (rs.getString("DTV_naziv") != null) {
                            paketBox.put("DTV_id", rs.getInt("DTV_id"));
                            paketBox.put("DTV_naziv", get_paket_naziv("digitalniTVPaketi", rs.getInt("DTV_id")));
                        }

                        if (rs.getString("NET_naziv") != null) {
                            paketBox.put("NET_id", rs.getInt("NET_id"));
                            paketBox.put("NET_naziv", get_paket_naziv("internetPaketi", rs.getInt("NET_id")));
                        }

                        if (rs.getString("TEL_naziv") != null) {
                            paketBox.put("TEL_id", rs.getInt("TEL_id"));
                            paketBox.put("TEL_naziv", get_paket_naziv("", 0));
                        }

                        if (rs.getString("IPTV_naziv") != null) {
                            paketBox.put("IPTV_id", rs.getInt("IPTV_id"));
                            paketBox.put("IPTV_naziv", get_paket_naziv("", 0));
                        }
                        paketBox.put("cena", rs.getDouble("cena"));
                        jObj.put(String.valueOf(i), paketBox);
                        i++;
                    }
                }
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

        }

        if (rLine.getString("action").equals("delete_service_user")) {
            jObj = new JSONObject();


            //INTERNET RADIUS DELETE
            if (rLine.getString("paketType").equals("INTERNET")) {
                query = "DELETE FROM radcheck where username=?";
                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "DELETE FROM radusergroup WHERE username=?";
                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                query = "DELETE FROM radreply WHERE username=?";
                try {
                    ps = db.connRad.prepareStatement(query);
                    ps.setString(1, rLine.getString("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }

            //DTV SERVICE DELETE
            if (rLine.getString("paketType").equals("DTV")) {
                query = "DELETE FROM DTVKartice WHERE userID=? AND idKartica=?";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setInt(1, rLine.getInt("userID"));
                    ps.setInt(2, rLine.getInt("idUniqueName"));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            //SERVICES USER DELETE
            try {
                query = "DELETE FROM ServicesUser WHERE id=?";

                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("serviceId"));
                db.ps.executeUpdate();


                jObj.put("message", String.format("Service id:%s deleted", rLine.getInt("serviceId")));

            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);
        }

        if (rLine.getString("action").equals("new_uplata")) {
            jObj = new JSONObject();
            query = "INSERT INTO uplate (datumUplate, uplaceno, mesto, operater, userID) VALUES (?,?,?,?,?)";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("datumUplate"));
                ps.setDouble(2, rLine.getDouble("uplaceno"));
                ps.setString(3, rLine.getString("mesto"));
                ps.setString(4, getOperName());
                ps.setInt(5, rLine.getInt("userID"));
                ps.executeUpdate();
                jObj.put("Message", "UPLATA_UPLACENA");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }


            send_object(jObj);
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
                        jObj.put(String.valueOf(i), uplate);
                        i++;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
        }


        if (rLine.getString("action").equals("get_zaduzenja_user")) {
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
                        userDebt.put("dug", rs.getDouble("dug"));
                        userDebt.put("operater", rs.getString("operater"));
                        userDebt.put("zaMesec", rs.getString("zaMesec"));
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

        if (rLine.getString("action").equals("uplata_servisa")) {
            jObj = new JSONObject();
            query = "UPDATE userDebts SET uplaceno=?, datumUplate=?, operater=?, dug=? WHERE id=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setDouble(1, rLine.getDouble("uplaceno"));
                ps.setString(2, date_format_full.format(Calendar.getInstance().getTime()));
                ps.setString(3, getOperName());
                ps.setDouble(4, rLine.getDouble("dug") - rLine.getDouble("uplaceno"));
                ps.setInt(5, rLine.getInt("id"));
                ps.executeUpdate();
                jObj.put("Message", "SERVICE_PAYMENTS_DONE");
            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            produzi_uslugu(rLine);
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
        }

        if (rLine.getString("action").equals("save_user_ugovor")) {
            jObj = new JSONObject();
            query = "INSERT INTO ugovori_korisnik (naziv, textUgovora, komentar, pocetakUgovora, krajUgovora, userID, brojUgovora) " +
                    "VALUES " +
                    "(?,?,?,?,?,?,?)";

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
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

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

        }


        if (rLine.getString("action").equals("saveOprema")) {
            query = "INSERT INTO oprema (naziv, model, komentar, userId) VALUES" +
                    "(?,?,?,?)";

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
        }

        if (rLine.getString("action").equals("snimiFakturu")) {
            query = "INSERT INTO jFakture (vrstaNaziv, jedMere, kolicina, jedCena, stopaPDV, brFakture, godina, userId, dateCreated)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                jObj.put("Error", e.getMessage());
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

            query = "INSERT INTO opremaKorisnik (naziv, model, komentar, userID, sn, MAC, naplata) " +
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
                jObj.put("Error", e.getMessage());
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

                query = "UPDATE internetPaketi SET brzina=?, cena=?, opis=?, prekoracenje=?, idleTimeout=? WHERE id=?";

                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("brzina"));
                ps.setDouble(2, rLine.getDouble("cena"));
                ps.setString(3, rLine.getString("opis"));
                ps.setInt(4, rLine.getInt("prekoracenje"));
                ps.setInt(5, rLine.getInt("idleTimeout"));
                ps.setInt(6, rLine.getInt("idPaket"));
                ps.executeUpdate();


                query = "UPDATE ServicesUser SET produzenje=? WHERE id_service=?";
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("prekoracenje"));
                ps.setInt(2, rLine.getInt("idPaket"));
                ps.executeUpdate();


                jObj.put("Message", "INTERNET_PAKET_UPDATED");

            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
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

                query = "UPDATE ServicesUser SET produzenje=? WHERE id_service=?";
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("prekoracenje"));
                ps.setInt(2, rLine.getInt("id"));
                ps.executeUpdate();

                jObj.put("Message", "PACKET_EDIT_SAVED");
            } catch (SQLException e) {
                jObj.put("Message", "ERROR");
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

        }

        if (rLine.get("action").equals("save_Box_Paket")) {
            jObj = new JSONObject();
            query = "INSERT INTO paketBox (naziv, DTV_id, DTV_naziv, NET_id, NET_naziv, TEL_id, TEL_naziv, IPTV_id, IPTV_naziv, cena)" +
                    "VALUES" +
                    "(?,?,?,?,?,?,?,?,?,?)";
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

                if (rLine.has("TEL_id")) {
                    ps.setInt(6, rLine.getInt("TEL_id"));
                    ps.setString(7, rLine.getString("TEL_naziv"));
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.VARCHAR);
                }

                if (rLine.has("IPTV_id")) {
                    ps.setInt(8, rLine.getInt("IPTV_id"));
                    ps.setString(9, rLine.getString("IPTV_naizv"));
                } else {
                    ps.setNull(8, Types.INTEGER);
                    ps.setNull(9, Types.VARCHAR);
                }

                ps.setDouble(10, rLine.getDouble("cena"));
                ps.executeUpdate();
                jObj.put("Message", "BOX_SAVED");

            } catch (SQLException e) {
                jObj.put("Error", e.getMessage());
                e.printStackTrace();
            }

            send_object(jObj);

        }
    }

    private String get_paket_naziv(String digitalniTVPaket, int dtv_id) {
        String naziv = "";
        try {
            ps = db.conn.prepareStatement("SELECT * FROM " + digitalniTVPaket + " WHERE id=?");
            ps.setInt(1, dtv_id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                naziv = rs.getString("naziv");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return naziv;
    }

    private void produzi_uslugu(JSONObject rLine) {
        DateTimeFormatter formatNormal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatYM = DateTimeFormatter.ofPattern("yyyy-MM");
        int produzenje = 0;

        //produzi internet
        if (rLine.getString("paketType").equals("INTERNET")) {
        }


        //produzi dtv
        if (rLine.getString("paketType").equals("DTV")) {
            String idKartica = null;
            String endDate = null;


            query = "SELECT * FROM ServicesUser WHERE userID=? AND id_service=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("userID"));
                ps.setInt(2, rLine.getInt("userServiceID"));
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    rs.next();
                    idKartica = rs.getString("id_unique_name");
                    produzenje = rs.getInt("produzenje");

                } else {
                    //izaci iz funkcije ako id_kartice nije pronadjena
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            query = "SELECT * FROM DTVKartice WHERE idKartica=? AND userID=?";

            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, idKartica);
                ps.setInt(2, rLine.getInt("userID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    endDate = rs.getString("endDate");
                } else {
                    //u ovom slucaju je greska. Izaci iz funkcije posto endDate ne postoji
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            Calendar callProduzenje = Calendar.getInstance();
            Calendar callEndDate = Calendar.getInstance();

            callProduzenje.setTime((Date) formatNormal.parse(rLine.getString("zaMesec")));
            callProduzenje.add(Calendar.MONTH, produzenje);

            callEndDate.setTime((Date) formatNormal.parse(endDate));

            if (callEndDate.getTime().before(callProduzenje.getTime())) {
                query = "UPDATE DTVKartice set endDate=? WHERE userId=? and idKartica=?";
                try {
                    ps = db.conn.prepareStatement(query);
                    ps.setString(1, String.valueOf(callProduzenje.getTime()));
                    ps.setInt(2, rLine.getInt("userID"));
                    ps.setString(3, idKartica);
                    ps.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
            return;

        }


        query = "UPDATE DTVKartice set endDate=? where idKartica=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, "DATEUMM");
            ps.setString(2, rLine.getString("id_unique_name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void add_user_debt_first_time(JSONObject rLine) {
        //zaduzenje korisnika dana do kraja meseca
        query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, " +
                "popust,paketType, cena, dug,  zaduzenOd, zaMesec) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("idService"));
            ps.setString(3, rLine.getString("nazivPaketa"));
            ps.setString(4, normalDate.format(Calendar.getInstance().getTime()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setString(7, rLine.getString("paketType"));
            ps.setDouble(8, rLine.getDouble("cena"));
            ps.setDouble(9, rLine.getDouble("dug"));
            ps.setString(10, getOperName());
            ps.setString(11, formatMonthDate.format(Calendar.getInstance().getTime()));
            ps.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
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

