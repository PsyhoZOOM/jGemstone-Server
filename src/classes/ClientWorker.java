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
import java.sql.ResultSet;
import java.sql.SQLException;
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
            jObj = new JSONObject();
            jObj.put("Message", "LOGIN OK");
            send_object(jObj);
        }

        if (!client_authenticated) {

            jObj = new JSONObject();
            jObj.put("Message", "Login Failed!");
            send_object(jObj);
            try {
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (rLine.get("action").equals("get_users")) {
            query = String.format("SELECT * FROM users WHERE username LIKE '%%%s%%' OR ime LIKE '%%%s%%' OR jbroj LIKE '%%%s%%'", rLine.getString("username"), rLine.getString("username"), rLine.getString("username"));
            LOGGER.info(query);
            rs = db.query_data(query);
            jUsers = new JSONObject();

            int i = 0;
            try {
                while (rs.next()) {

                    try {
                        jObj = new JSONObject();
                        jObj.put("id", rs.getInt("id"));
                        jObj.put("ime", rs.getString("ime"));
                        jObj.put("username", rs.getString("username"));
                        jObj.put("mesto", rs.getString("mesto"));
                        jObj.put("adresa", (rs.getString("adresa")));
                        jObj.put("adresaRacuna", rs.getString("adresaracun"));
                        jObj.put("adresaKoriscenja", rs.getString("adresakoriscenja"));
                        jObj.put("brLk", rs.getString("brlk"));
                        jObj.put("datumRodjenja", rs.getString("datumrodjenja"));
                        jObj.put("telFixni", rs.getString("brtel"));
                        jObj.put("telMobilni", rs.getString("brtelmob"));
                        jObj.put("JMBG", rs.getString("mbr"));
                        jObj.put("ostalo", rs.getString("ostalo"));
                        jObj.put("komentar", rs.getString("komentar"));
                        jObj.put("postBr", rs.getString("postbr"));

                        LOGGER.info(jObj);

                    } catch (SQLException e) {
                        e.printStackTrace();
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
            query = String.format("SELECT * FROM users WHERE id='%d'", rLine.getInt("userId"));
            rs = db.query_data(query);
            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("userName", rs.getString("username"));
                    jObj.put("fullName", rs.getString("ime"));
                    jObj.put("datumRodjenja", rs.getString("datumrodjenja"));
                    jObj.put("adresa", rs.getString("adresa"));
                    jObj.put("mesto", rs.getString("mesto"));
                    jObj.put("postBr", rs.getString("postbr"));
                    jObj.put("telFix", rs.getString("brtel"));
                    jObj.put("telMob", rs.getString("brtelmob"));
                    jObj.put("brLk", rs.getString("brlk"));
                    jObj.put("JMBG", rs.getString("mbr"));
                    jObj.put("adresaRacuna", rs.getString("adresaracun"));
                    jObj.put("adresaKoriscenja", rs.getString("adresakoriscenja"));
                    jObj.put("ostalo", rs.getString("ostalo"));
                    jObj.put("komentar", rs.getString("komentar"));
                    jObj.put("jBroj", rs.getInt("jbroj"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);
        }

        if (rLine.get("action").equals("get_groups")) {
            query = String.format("SELECT * FROM grupa WHERE groupname LIKE '%s%%'", rLine.get("groupName"));
            rs = db.query_data(query);
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
                e.printStackTrace();
            }
            send_object(jGrupe);
        }

        if (rLine.get("action").equals("get_group_data")) {
            query = String.format("SELECT * FROM grupa WHERE id LIKE '%s'", rLine.get("groupId"));
            System.out.println(query);
            rs = db.query_data(query);


            try {
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("groupName", rs.getString("groupName"));
                    jObj.put("cena", rs.getString("cena"));
                    jObj.put("prepaid", rs.getInt("prepaid"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            send_object(jObj);
        }

        if (rLine.get("action").equals("save_group_data")) {
            query = String.format("UPDATE grupa SET groupname='%s', cena='%s', prepaid='%d', opis='%s' WHERE id='%d'",
                    rLine.get("groupName"), rLine.get("cena"), rLine.getInt("prepaid"), rLine.get("opis"), rLine.get("groupId")
            );
            System.out.println(query);
            db.query = query;
            db.executeUpdate();
            jObj = new JSONObject();
            jObj.put("message", "SAVED");
            send_object(jObj);

        }


        if (rLine.get("action").equals("delete_group")) {

            query = String.format("DELETE FROM grupa WHERE ID='%s'", rLine.get("groupID"));
            db.query = query;
            db.executeUpdate();
            jObj = new JSONObject();
            jObj.put("message", String.format("GRUPA IZBRISANA"));
            send_object(jObj);
        }

        if (rLine.getString("action").equals("save_group")) {
            query = String.format("INSERT INTO grupa  (groupname, cena, prepaid, opis) VALUES " +
                            "('%s', '%s', '%s', '%s')",
                    rLine.getString("groupName"), rLine.getString("cena"), rLine.getInt("prepaid"),
                    rLine.getString("opis"));
            db.query = query;
            db.executeUpdate();
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

            query = String.format("SELECT jbroj FROM users WHERE jbroj='%s'", rLine.getString("jbroj"));
            ResultSet rs = db.query_data(query);
            try {
                if (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("message", "user_no_exist");
                    send_object(jObj);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            query = String.format("SELECT username FROM users WHERE username='%s'", rLine.getString("userName"));
            ResultSet rsUser = db.query_data(query);
            try {
                if (rsUser.next()) {
                    jObj = new JSONObject();
                    jObj.put("message", "user_exist");
                    send_object(jObj);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String username = rLine.getString("userName");
            query = String.format("INSERT INTO users (username, ime, datumrodjenja, kreirao, postbr, mesto, brlk, " +
                            "mbr, adresaracun, adresa, brtelmob, brtel, jbroj) " +
                            "VALUES " +
                            "('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s', '%s', '%s', '%d') ",
                    rLine.getString("userName"), rLine.getString("fullName"), rLine.get("datumRodjenja"),
                    operName, rLine.getString("postBr"), rLine.getString("mesto"), rLine.getString("brLk"),
                    rLine.get("JMBG"), rLine.get("adresaRacuna"), rLine.getString("adresa"), rLine.getString("telMobilni"),
                    rLine.getString("telFiksni"), rLine.getInt("jbroj")
            );
            db.query = query;
            db.executeUpdate();
            jObj = new JSONObject();
            jObj.put("message", String.format("USER: %s SAVED", username));




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

            //send last user saved
            query = "SELECT * FROM users ORDER BY id DESC LIMIT 1";

            try {
                db.ps = db.conn.prepareStatement(query);
                rs = db.ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                rs.next();
                jObj = new JSONObject();
                jObj.put("id",rs.getInt("id"));
                jObj.put("userName", rs.getString("username"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jObj);

        }

        if (rLine.getString("action").equals("get_user_services")) {
            ArrayList<Services> services_id = new ArrayList<Services>();
            Services srv_id;
            query = String.format("SELECT id,id_service FROM Services_User WHERE userID='%d'", rLine.getInt("userID"));

            rs = db.query_data(query);
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
            LOGGER.info(jsServicesArray);
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

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GregorianCalendar calendar = new GregorianCalendar();
            Date date = new Date();
            calendar.setTime(date);
            String datum = format.format(calendar.getTime());

            query = "INSERT into Services_User (id_service, date_added, userID) VALUES (?, ? ,?) ";


            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("id"));
                db.ps.setInt(2, rLine.getInt("userID"));
                db.ps.setDate(3, (java.sql.Date) date);
                db.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            jObj = new JSONObject();
            jObj.put("message", String.format("Service id: %d added to user: %s", rLine.getInt("id"), rLine.getString("userID")));
            send_object(jObj);

        }

        if (rLine.getString("action").equals("delete_service_user")) {
            query = String.format("DELETE FROM Services_User WHERE username='%s' AND id='%d'",
                    rLine.getString("userName"), rLine.getInt("id"));
            db.query = query;
            LOGGER.info(query);
            db.executeUpdate();
            jObj = new JSONObject();
            jObj.put("message", String.format("Service id:%s deleted fro user:%s", rLine.getInt("id"), rLine.getString("userName")));

            send_object(jObj);
        }

        if (rLine.get("action").equals("get_uplate_zaduzenja_user")) {
            query = String.format("SELECT * FROM user_debts WHERE username='%s' AND payed=0", rLine.getString("userName"));
            rs = db.query_data(query);
            jUplate = new JSONObject();

            try {
                int i = 0;
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("userName", rs.getString("username"));
                    jObj.put("datumUplate", rs.getString("payment_date"));
                    jObj.put("serviceName", rs.getString("service_name"));
                    jObj.put("uplaceno", rs.getString("debt"));
                    jObj.put("datumZaduzenja", rs.getString("date_debt"));
                    jObj.put("operName", rs.getString("oper_name"));
                    jUplate.put(String.valueOf(i), jObj);
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jUplate);
        }

        if (rLine.get("action").equals("get_uplate_user")) {
            query = String.format("SELECT * FROM user_debts WHERE username='%s' AND payed=1 ORDER BY id DESC", rLine.getString("userName"));
            rs = db.query_data(query);
            jUplate = new JSONObject();
            try {
                int i = 0;
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("userName", rs.getString("username"));
                    jObj.put("paymentDate", rs.getString("payment_date"));
                    jObj.put("dateDebt", rs.getString("date_debt"));
                    jObj.put("debt", rs.getString("debt"));
                    jObj.put("operName", rs.getString("oper_name"));
                    jObj.put("serviceName", rs.getString("service_name"));
                    jUplate.put(String.valueOf(i), jObj);
                    i++;

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jUplate);

        }

        if (rLine.get("action").equals("get_uplate_zaduzenja_user_sve")) {
            query = String.format("SELECT * FROM user_debts WHERE username='%s' ORDER BY id DESC", rLine.getString("userName"));
            rs = db.query_data(query);
            jUplate = new JSONObject();

            try {
                int i = 0;
                while (rs.next()) {
                    jObj = new JSONObject();
                    jObj.put("id", rs.getInt("id"));
                    jObj.put("userName", rs.getString("username"));
                    jObj.put("datumZaduzenja", rs.getString("date_debt"));
                    jObj.put("serviceName", rs.getString("service_name"));
                    jObj.put("zaUplatu", rs.getDouble("debt"));
                    if (rs.getInt("payed") == 1) {
                        jObj.put("uplaceno", rs.getDouble("debt"));
                    } else {
                        jObj.put("uplaceno", 0.00);
                    }
                    jUplate.put(String.valueOf(i), jObj);
                    i++;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            send_object(jUplate);
        }

        if (rLine.getString("action").equals("new_payment")) {

            db.query = String.format("UPDATE user_debts SET payed=1, payment_date='%s', oper_name='%s' WHERE id='%d'",
                    date_format_full.format(new Date()), operName, rLine.getInt("id"));
            LOGGER.info(db.query);
            db.executeUpdate();

            jObj = new JSONObject();
            jObj.put("message", String.format("Uplata ID: %s je uplacena", rLine.getInt("id")));
            send_object(jObj);
        }

        if (rLine.getString("action").equals("delete_payment")) {
            db.query = String.format("UPDATE user_debts set payed=0 WHERE id='%d'", rLine.getInt("paymentId"));
            db.executeUpdate();


            jObj = new JSONObject();
            jObj.put("message", String.format("Uplata %s izbrisana", rLine.get("paymentId").toString()));
            send_object(jObj);
        }


        if (rLine.getString("action").equals("get_user_debt_total")) {
            rs = db.query_data(String.format("SELECT SUM(debt) FROM user_debts WHERE username='%s'", rLine.getString("userName")));
            try {
                if (rs.isBeforeFirst()) {
                    double sum = 0;

                    while (rs.next()) {
                        sum = sum + rs.getDouble(1);
                    }

                    jObj = new JSONObject();
                    jObj.put("debtTotal", sum);
                    send_object(jObj);
                } else {
                    jObj.put("debtTotal", 0.00);
                    send_object(jObj);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (rLine.getString("action").equals("insert_uplata")) {
            query = String.format("INSERT INTO uplate (username, datum_uplate, za_mesec, uplaceno, operater, uplata, godina ) VALUES " +
                            "('%s', '%s', '%s', '%s', '%s', '%d', '%s')", rLine.getString("userName"), rLine.getString("datumUplate"),
                    rLine.getString("zaMesec"), rLine.getString("uplaceno"), operName, rLine.getInt("uplata"), rLine.getString("godina"));
            LOGGER.info(query);

            db.query = query;
            db.executeUpdate();

            jObj = new JSONObject();
            jObj.put("message", "UPLATA_IZVRSENA");
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
                LOGGER.info(db.ps.toString());
                db.ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
            jObj = new JSONObject();
            jObj.put("Message", "UGOVOR_ADDED");
            send_object(jObj);


        }
        if (rLine.getString("action").equals("get_ugovori")) {
            query = "SELECT * FROM ugovori_types";
            try {
                db.ps = db.conn.prepareStatement(query);
                rs = db.ps.executeQuery();
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
                jObj.put("userName", rs.getString("username"));
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
                    jObj.put("userName", rs.getString("username"));
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
                LOGGER.info("AAAA STRING"+db.ps.toString());
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
            query = "SELECT * FROM jFakture WHERE userId=?";

            try {
                db.ps = db.conn.prepareStatement(query);
                db.ps.setInt(1, rLine.getInt("userId"));
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
    }

    private void delete_user(JSONObject mes) {
        int userId = mes.getInt("userId");
        String username = mes.getString("userName");
        //delete from users
        query = String.format("DELETE FROM users WHERE id='%s' ", userId);
        db.query = query;
        db.executeUpdate();


        //delete from Services_user
        query = String.format("DELETE FROM Services_User WHERE username='%s'", mes.getString("userName"));
        db.query = query;
        db.executeUpdate();


        jObj = new JSONObject();
        jObj.put("message", String.format("USER %s DELETED", username));
        send_object(jObj);
    }

    private void update_user(JSONObject jObj) {
        int userID = jObj.getInt("userID");
        query = "UPDATE users SET username = ? , ime = ? , datumrodjenja = ? , adresa = ? , mesto = ? , postbr = ? , brtel = ?  , brtelmob = ? ,  brlk = ? ,  mbr =? , adresaracun = ? , ostalo = ? , adresakoriscenja = ? , komentar = ?, jbroj = ?  WHERE id = ? ";


        try {
            db.ps = db.conn.prepareStatement(query);
            db.ps.setString(1, jObj.getString("userName"));
            db.ps.setString(2, jObj.getString("fullName"));
            db.ps.setString(3, jObj.getString("datumRodjenja"));
            db.ps.setString(4, jObj.getString("adresa"));
            db.ps.setString(5, jObj.getString("mesto"));
            db.ps.setString(6, jObj.getString("postBr"));
            db.ps.setString(7, jObj.getString("telFixni"));
            db.ps.setString(8, jObj.getString("telMobilni"));
            db.ps.setString(9, jObj.getString("brLk"));
            db.ps.setString(10, jObj.getString("JMBG"));
            db.ps.setString(11, jObj.getString("adresaRacuna"));
            db.ps.setString(12, jObj.getString("ostalo"));
            db.ps.setString(13, jObj.getString("adresaKoriscenja"));
            db.ps.setString(14, jObj.getString("komentar"));
            db.ps.setInt(15, jObj.getInt("jBroj"));
            db.ps.setInt(16, userID);
            LOGGER.info(db.ps.toString());
            db.ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                db.conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }


        // db.query = query;
        // db.executeUpdate();


        jObj = new JSONObject();
        jObj.put("message", String.format("USER: %s UPDATED", userID));
        send_object(jObj);
    }


    private Boolean check_Login(String username, String password) {
        String userName = null;
        String passWord = null;
        System.out.println("checking login" + username + password);
        this.setOperName(userName);

        ResultSet rs = db.query_data(String.format("SELECT username,password FROM admin WHERE username='%s' AND password='%s'", username, password));
        try {
            if (rs.next()) {
                userName = rs.getString("username");
                passWord = rs.getString("password");
            } else {
                userName = null;
                passWord = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (userName != null && passWord != null) {
            if (userName.equals(username) && passWord.equals(password)) {
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

