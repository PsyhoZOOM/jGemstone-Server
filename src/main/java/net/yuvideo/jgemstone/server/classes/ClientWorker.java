package net.yuvideo.jgemstone.server.classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.SSLSocket;
import net.yuvideo.jgemstone.server.classes.ARTIKLI.ArtikliFunctions;
import net.yuvideo.jgemstone.server.classes.BOX.BoxFunctions;
import net.yuvideo.jgemstone.server.classes.BOX.addBoxService;
import net.yuvideo.jgemstone.server.classes.DTV.DTVFunctions;
import net.yuvideo.jgemstone.server.classes.DTV.DTVPaketFunctions;
import net.yuvideo.jgemstone.server.classes.FIX.CSVIzvestajImport;
import net.yuvideo.jgemstone.server.classes.FIX.FIXFunctions;
import net.yuvideo.jgemstone.server.classes.GROUP.GroupOper;
import net.yuvideo.jgemstone.server.classes.INTERNET.InternetPaket;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.IPTVFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.StalkerRestAPI2;
import net.yuvideo.jgemstone.server.classes.LOCATION.LocationsClients;
import net.yuvideo.jgemstone.server.classes.MESTA.MestaFuncitons;
import net.yuvideo.jgemstone.server.classes.MIKROTIK_API.MikrotikAPI;
import net.yuvideo.jgemstone.server.classes.MISC.mysqlMIsc;
import net.yuvideo.jgemstone.server.classes.OBRACUNI.MesecniObracun;
import net.yuvideo.jgemstone.server.classes.RACUNI.Uplate;
import net.yuvideo.jgemstone.server.classes.RACUNI.UserRacun;
import net.yuvideo.jgemstone.server.classes.RADIUS.Radius;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServiceData;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.ServerServices.SchedullerTask;
import net.yuvideo.jgemstone.server.classes.ServerServices.WiFiTracker;
import net.yuvideo.jgemstone.server.classes.USERS.UserFunc;
import net.yuvideo.jgemstone.server.classes.USERS.UsersData;
import net.yuvideo.jgemstone.server.classes.WiFi.WiFiData;
import net.yuvideo.jgemstone.server.classes.ZADUZENJA.ZaduziCustom;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM on 8/8/16.
 */
public class ClientWorker implements Runnable {

  public Logger LOGGER;
  private static final String S_VERSION = "0.211";
  private String C_VERSION;
  private final SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  private final SimpleDateFormat mysql_date_format = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
  private final SimpleDateFormat normalDate = new SimpleDateFormat("yyyy-MM-dd");
  private final SimpleDateFormat formatMonthDate = new SimpleDateFormat("yyyy-MM");
  public int DEBUG = 0;
  public boolean client_db_update = false;
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
  //JSON Grupa
  ///JSON PART
  private JSONObject jObj;
  private JSONObject jGrupe;
  //JSON Users
  private JSONObject jUsers;
  private int operID;
  private boolean keepAlive;
  public SchedullerTask scheduler;

  public ClientWorker(SSLSocket client) {
    //this.client = client;
    this.client = client;
    this.db = new database();

  }

  public Socket get_socket() {
    return this.client;
  }

  @Override
  public void run() {

    db.DEBUG = DEBUG;

    System.out.println(String.format("Client connected: %s", this.client
        .getRemoteSocketAddress()));

    LOGGER.info(String.format("Client connected: %s", client.getRemoteSocketAddress()));

    while (!client.isClosed()) {

      if (Isr == null) {
        try {
          Isr = new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8);
          Bfr = new BufferedReader(Isr);
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
      }


      if (DEBUG > 1) {
        System.out.println("Waitin for client data..");

      }
      try {

        String A = Bfr.readLine();

        if (A == null) {
          //client.close();
          LOGGER.info(String
              .format("Client %s %s disconnected", client.getRemoteSocketAddress().toString(),
                  getOperName()));
          break;
        }

        jObj = new JSONObject(A);
      } catch (IOException e) {
        e.printStackTrace();
        //client.close();
        break;
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (DEBUG > 0) {
        if (jObj.has("userNameLogin")) {
          setOperName(jObj.getString("userNameLogin"));
        }

        LOGGER.info(String.format("Reading IP: %s Operater: %s DATA: %s",
            client.getRemoteSocketAddress().toString(), getOperName(), jObj.toString()));
      }
      object_worker(jObj);

    }
  }


  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public int getOperID() {
    return this.operID;
  }

  public void setOperID(int id) {
    this.operID = id;
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



    if(rLine.has("C_VERSION")){
      this.C_VERSION = rLine.getString("C_VERSION");
      if(!C_VERSION.equals(this.S_VERSION)){
        jObj = new JSONObject();
        jObj.put("Message", "WRONG_VERSION");
        send_object(jObj);
        //client.close();

      }
    }else{
      //client.close();

    }


    if (rLine.get("action").equals("login")) {
      client_authenticated = check_Login(rLine.getString("username"), rLine.getString("password"));
      if (rLine.has("keepAlive")) {
        if (rLine.getBoolean("keepAlive")) {
          this.keepAlive = true;
        } else {
          this.keepAlive = false;
        }
      }


      this.operName = rLine.get("username").toString();
      if (client_authenticated) {
        jObj = new JSONObject();
        jObj.put("Message", "LOGIN_OK");
        send_object(jObj);
        if (keepAlive == false) {
          //client.close();
        }
        return;
      }
    }

    client_authenticated = check_Login(rLine.getString("userNameLogin"),
        rLine.getString("userPassLogin"));





    if (!client_authenticated) {

      jObj = new JSONObject();
      jObj.put("Message", "LOGIN_FAILED");
      send_object(jObj);
      //client.close();

      return;
    }
    setOperName(rLine.getString("userNameLogin"));
    rLine.remove("userNameLogin");
    rLine.remove("userPassLogin");
    rLine.remove("keepAlive");
    rLine.remove("C_VERSION");

    if (rLine.get("action").equals("checkPing")) {
      jObj = new JSONObject();
      jObj.put("PONG", "PONG");
      send_object(jObj);
      return;
    }

    if (rLine.get("action").equals("get_users")) {
      JSONObject object = new JSONObject();
      UserFunc userFunc = new UserFunc(db, getOperName());
      object = userFunc.getAllUsers(rLine);
      if (userFunc.isError()) {
        object.put("ERROR", userFunc.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.get("action").equals("get_next_free_ID")) {
      int freeID = mysqlMIsc.findNextFreeID(db);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("freeID", freeID);
      send_object(jsonObject);
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
          jObj.put("komentar", rs.getString("komentar"));
          jObj.put("jMesto", rs.getString("jMesto"));
          jObj.put("jAdresa", rs.getString("jAdresa"));
          jObj.put("jAdresaBroj", rs.getString("jAdresaBroj"));
          jObj.put("jBroj", rs.getString("jBroj"));
          jObj.put("adresaRacuna", rs.getString("adresaRacuna"));
          jObj.put("mestoRacuna", rs.getString("mestoRacuna"));
          MestaFuncitons mestaFuncitons = new MestaFuncitons(db);
          jObj.put("mestoUsluge", mestaFuncitons.getNazivMesta(rs.getString("jMesto")));
          jObj.put("adresaUsluge",
              mestaFuncitons.getNazivAdrese(rs.getString("jMesto"), rs.getString("jAdresa")));

          //FIRMA
          jObj.put("firma", rs.getBoolean("firma"));
          jObj.put("nazivFirme", rs.getString("nazivFirme"));
          jObj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
          jObj.put("kontaktOsobaTel", rs.getString("kontaktOsobaTel"));
          jObj.put("kodBanke", rs.getString("kodBanke"));
          jObj.put("tekuciRacun", rs.getString("tekuciRacun"));
          jObj.put("PIB", rs.getString("PIB"));
          jObj.put("maticniBroj", rs.getString("maticniBroj"));
          jObj.put("fax", rs.getString("fax"));
          jObj.put("adresaFirme", rs.getString("adresaFirme"));
          jObj.put("mestoFirme", rs.getString("mestoFirme"));
          jObj.put("email", rs.getString("email"));
          jObj.put("prekoracenje", rs.getInt("prekoracenje"));

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
      query = String
          .format("SELECT * FROM grupa WHERE groupname LIKE '%s%%'", rLine.get("groupName"));
      query = "SELECT * FROM  grupa WHERE groupname LIKE ?";

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

        if (rs.isBeforeFirst()) {
          userHasFirma = true;
        }

        rs.close();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (userHasFirma) {
        query = "UPDATE firme SET "
            + "nazivFirme=?, "
            + "kodBanke=?, "
            + "kontaktOsoba=?, "
            + "PIB=?, "
            + "maticniBroj=?, "
            + "tekuciRacun=?, "
            + "fax=?, "
            + "adresaFirme=? "
            + "WHERE userID=?";
        try {
          ps = db.conn.prepareStatement(query);
          ps.setString(1, rLine.getString("nazivFirme"));
          ps.setString(2, rLine.getString("kodBanke"));
          ps.setString(3, rLine.getString("kontaktOsoba"));
          ps.setString(4, rLine.getString("pib"));
          ps.setString(5, rLine.getString("maticniBroj"));
          ps.setString(6, rLine.getString("tekuciRacun"));
          ps.setString(7, rLine.getString("fax"));
          ps.setString(8, rLine.getString("adresaFirme"));
          ps.setInt(9, rLine.getInt("userID"));
          ps.executeUpdate();
          ps.close();
          setUserFirma(rLine.getInt("userID"), true);
          jObj.put("MESSAGE", "FIRMA_EDIT_SAVE");

        } catch (SQLException e) {
          jObj.put("ERROR", e.getMessage());
          e.printStackTrace();

        }

      } else {
        query = "INSERT INTO firme "
            + "(userID, nazivFirme, kontaktOsoba, kodBanke, PIB, maticniBroj, "
            + "tekuciRacun, fax, adresaFirme) "
            + "VALUES "
            + "(?,?,?,?,?,?,?,?,?)";
        try {
          ps = db.conn.prepareStatement(query);
          ps.setInt(1, rLine.getInt("userID"));
          ps.setString(2, rLine.getString("nazivFirme"));
          ps.setString(3, rLine.getString("kontaktOsoba"));
          ps.setString(4, rLine.getString("kodBanke"));
          ps.setString(5, rLine.getString("pib"));
          ps.setString(6, rLine.getString("maticniBroj"));
          ps.setString(7, rLine.getString("tekuciRacun"));
          ps.setString(8, rLine.getString("fax"));
          ps.setString(9, rLine.getString("adresaFirme"));
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
      String query = "SELECT * FROM users WHERE userID=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("userID"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          jObj.put("id", rs.getInt("id"));
          jObj.put("userID", rs.getInt("userID"));
          jObj.put("nazivFirme", rs.getString("nazivFirme"));
          jObj.put("kodBanke", rs.getString("kodBanke"));
          jObj.put("pib", rs.getString("PIB"));
          jObj.put("maticniBroj", rs.getString("maticniBroj"));
          jObj.put("tekuciRacun", rs.getString("tekuciRacun"));
          jObj.put("fax", rs.getString("fax"));
          jObj.put("adresaFirme", rs.getString("adresaFirme"));
          jObj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
          jObj.put("konatktOsobaTel", rs.getString("kontaktOsobaTel"));
        }
        ps.close();
        rs.close();
      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jObj);
    }

    if (rLine.getString("action").equals("deleteFirma")) {
      JSONObject jObj = new JSONObject();
      PreparedStatement ps;
      String query = "UPDATE users set firma=false WHERE id=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("userID"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException ex) {
        jObj.put("ERROR", ex.getMessage());
        ex.printStackTrace();
      }

      query = "UPDATE users SET firma=?  WHERE userID=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setBoolean(1, false);
        ps.setInt(2, rLine.getInt("userID"));
        ps.executeUpdate();
        ps.close();
        jObj.put("MESSAGE", "FIRMA IZBRISANA");

      } catch (SQLException ex) {
        jObj.put("ERROR", ex.getMessage());
      }

      query = "DELETE FROM faktureData WHERE userID=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("userID"));
        ps.executeUpdate();
        ps.close();
        jObj.put("MESSAGE", "FIRMA IZBRISANA");

      } catch (SQLException ex) {
        jObj.put("ERROR", ex.getMessage());
        ex.printStackTrace();
      }
      send_object(jObj);
    }

    if (rLine.getString("action").equals("delete_user")) {
      delete_user(rLine);

    }

    if (rLine.getString("action").equals("getUserOprema")) {
      JSONObject jsonObject = new JSONObject();
      UsersData usersData = new UsersData(db, getOperName());
      jsonObject = usersData.getUserOprema(rLine.getInt("userID"));
      if (usersData.isError()) {
        jsonObject.put("ERROR", usersData.getErrorMSG());
      }
      send_object(jsonObject);

    }

    if (rLine.getString("action").equals("new_user")) {

      boolean editUser = rLine.getBoolean("editUser");
      if(rLine.getBoolean("editUser")){
        query = "UPDATE users SET id=?, ime=?, datumRodjenja=?, operater=?, postBr=?, mesto=?, brLk=?, "
            + "JMBG=?, adresa=?, komentar=?, telFiksni=?, telMobilni=?, datumKreiranja=?, jBroj=? WHERE id=?";
      }else {
        query = "INSERT INTO users (id, ime, datumRodjenja, operater, postBr, mesto, brLk, JMBG, "
            + "adresa,  komentar, telFiksni, telMobilni, datumKreiranja, jBroj)"
            + "VALUES (?, ?, ?, ?, ?, ?, ? ,? ,? ,? ,?, ?, ?, ?)";
      }

      try {

        if(editUser) {
          ps = db.conn.prepareStatement(query);
        }else{
          ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        }

        ps.setInt(1, rLine.getInt("freeID"));
        ps.setString(2, rLine.getString("fullName"));
        ps.setString(3, rLine.getString("datumRodjenja"));
        ps.setString(4, getOperName());
        ps.setString(5, rLine.getString("postBr"));
        ps.setString(6, rLine.getString("mesto"));
        ps.setString(7, rLine.getString("brLk"));
        ps.setString(8, rLine.getString("JMBG"));
        ps.setString(9, rLine.getString("adresa"));
        ps.setString(10, rLine.getString("komentar"));
        ps.setString(11, rLine.getString("telFiksni"));
        ps.setString(12, rLine.getString("telMobilni"));
        ps.setString(13, mysql_date_format.format(new Date()));
        ps.setString(14, rLine.getString("jBroj"));
        if (rLine.getBoolean("editUser")){
          ps.setInt(15, rLine.getInt("freeID"));
        }

        ps.executeUpdate();

      } catch (SQLException e) {
        jObj = new JSONObject();
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      jObj = new JSONObject();
      try {
        jObj.put("Message", "user_saved");
        if(!editUser) {
          rs = ps.getGeneratedKeys();
          rs.next();
          System.out.println(rs.getInt(1));
          jObj.put("userID", rLine.getInt("freeID"));
        }else{
          jObj.put("userID", rLine.getInt("freeID"));

        }
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
      query = "SELECT *  FROM servicesUser WHERE userID=? AND linkedService=false AND paketType NOT LIKE '%ADDON%' ";

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
            service.put("userID", rs.getString("userID"));
            service.put("brojUgovora", rs.getString("brojUgovora"));
            service.put("cena", rs.getDouble("cena"));
            service.put("popust", rs.getDouble("popust"));
            service.put("pdv", rs.getDouble("PDV"));
            service.put("operName", rs.getString("operName"));
            service.put("date_added", rs.getString("date_added"));
            service.put("nazivPaketa", rs.getString("nazivPaketa"));
            service.put("naziv", rs.getString("nazivPaketa"));

            //SETTING idUniqueName
            String[] uniqueName = new String[4];
            if (!rs.getString("IPTV_MAC").trim().isEmpty()) {
              service.put("idUniqueName", rs.getString("IPTV_MAC"));
              uniqueName[0] = "IPTV: " + rs.getString("IPTV_MAC");
              service.put("IPTV_MAC", rs.getString("IPTV_MAC"));
              service.put("STB_MAC", rs.getString("IPTV_MAC"));
            } else if (!rs.getString("UserName").trim().isEmpty()) {
              service.put("idUniqueName", rs.getString("UserName"));
              uniqueName[1] = "NET: " + rs.getString("UserName");
            } else if (!rs.getString("idDTVCard").trim().isEmpty()) {
              service.put("idUniqueName", rs.getString("idDTVCard"));
              uniqueName[2] = "DTV: " + rs.getString("idDTVCard");
            } else if (!rs.getString("FIKSNA_TEL").trim().isEmpty()) {
              service.put("idUniqueName", rs.getString("FIKSNA_TEL"));
              uniqueName[3] = "FIKSNA: " + rs.getString("FIKSNA_TEL");
            } else {
              service.put("idUniqueName", rs.getString("nazivPaketa"));
            }

            if (rs.getString("paketType").equals("BOX")) {
              String serviceSTR = "";
              for (String str : uniqueName) {
                if (str != null) {
                  if (!str.isEmpty()) {
                    serviceSTR += str + " ";
                  }
                }
              }
              service.put("idUniqueName", serviceSTR);
            }


            service.put("userName", rs.getString("UserName"));
            service.put("groupName", rs.getString("GroupName"));
            service.put("IPTV_MAC", rs.getString("IPTV_MAC"));
            service.put("FIKSNA_TEL", rs.getString("FIKSNA_TEL"));
            service.put("obracun", rs.getBoolean("obracun"));
            service.put("aktivan", rs.getBoolean("aktivan"));
            service.put("id_service", rs.getInt("id_service"));
            service.put("box", rs.getBoolean("BOX_service"));
            service.put("box_ID", rs.getInt("box_id"));
            service.put("brojTel", rs.getString("FIKSNA_TEL"));
            service.put("paketType", rs.getString("paketType"));
            service.put("linkedService", rs.getBoolean("linkedService"));
            service.put("newService", rs.getBoolean("newService"));
            service.put("idDTVCard", rs.getString("idDTVCard"));
            service.put("endDate", rs.getString("endDate"));
            service.put("komentar", rs.getString("komentar"));
            service.put("opis", rs.getString("opis"));
            service.put("dtv_main", rs.getString("dtv_main"));

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
      query = "SELECT * FROM servicesUser WHERE box_id=? AND linkedService=? or id=? AND userID=?";
      try {
        ps2 = db.conn.prepareStatement(query);
        ps2.setInt(1, rLine.getInt("box_ID"));
        ps2.setInt(2, 1);
        ps2.setInt(3, rLine.getInt("box_ID"));
        ps2.setInt(4, rLine.getInt("userID"));
        ResultSet rs2 = ps2.executeQuery();

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
            service.put("naziv", rs2.getString("nazivPaketa"));
            service.put("groupName", rs2.getString("GroupName"));
            service.put("userName", rs2.getString("UserName"));
            service.put("idDTVCard", rs2.getString("idDTVCard"));
            service.put("IPTV_MAC", rs2.getString("IPTV_MAC"));
            service.put("STB_MAC", rs2.getString("IPTV_MAC"));
            service.put("FIKSNA_TEL", rs2.getString("FIKSNA_TEL"));
            service.put("popust", rs2.getDouble("popust"));
            service.put("cena", rs2.getDouble("cena"));
            service.put("pdv", rs2.getDouble("PDV"));
            service.put("linkedService", rs2.getBoolean("linkedService"));
            service.put("aktivan", rs2.getBoolean("aktivan"));
            service.put("obracun", rs2.getBoolean("obracun"));
            service.put("endDate", rs2.getString("endDate"));
            service.put("paketType", rs2.getString("paketType"));
            service.put("newService", rs2.getBoolean("newService"));
            service.put("komentar", rs2.getString("komentar"));
            service.put("opis", rs2.getString("opis"));
            service.put("brojUgovora", rs2.getString("brojUgovora"));
            service.put("operName", rs2.getString("operName"));
            service.put("date_added", rs2.getString("date_added"));

            if (!rs2.getString("idDTVCard").trim().isEmpty()) {
              service.put("idUniqueName", rs2.getString("idDTVCard"));
            }
            if (!rs2.getString("UserName").trim().isEmpty()) {
              service.put("idUniqueName", rs2.getString("UserName"));
              service.put("groupName", rs2.getString("GroupName"));
            }
            if (!rs2.getString("IPTV_MAC").trim().isEmpty()) {
              service.put("idUniqueName", rs2.getString("IPTV_MAC"));
              service.put("IPTV_MAC", rs2.getString("IPTV_MAC"));
              service.put("STB_MAC", rs2.getString("IPTV_MAC"));
            }
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

    if (rLine.getString("action").equals("getServiceDetail")) {
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      JSONObject serviceDetail = servicesFunctions
          .getServiceDetail(rLine.getInt("serviceID"), getOperName());
      send_object(serviceDetail);
      return;
    }

    if (rLine.getString("action").equals("changeServiceDTVCard")) {
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      JSONObject object = servicesFunctions.changeDTVCard(rLine);
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("changeServiceDTVEndDate")) {
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      JSONObject object = servicesFunctions.changeDTVEndDate(rLine);
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getRadiusServiceData")) {

      Radius radius = new Radius(db, getOperName());
      JSONObject data = radius.getRadReplyData(rLine.getString("username"));

      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      JSONObject serviceDetail = servicesFunctions
          .getServiceDetail(rLine.getInt("serviceID"), getOperName());
      data.put("nazivPaketa", serviceDetail.getString("nazivPaketa"));
      data.put("paketType", serviceDetail.getString("paketType"));
      data.put("endDateService", serviceDetail.getString("endDateService"));

      send_object(data);
      return;
    }

    if (rLine.getString("action").equals("changeRadiusPass")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      object = radius.changeUserPass(rLine.getString("username"), rLine.getString("pass"));
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("changeRadiusData")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      object = radius.changeRadReplyData(rLine);
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }
      send_object(object);
      return;

    }

    if (rLine.get("action").equals("activate_new_service")) {

      jObj = new JSONObject();
      int service_id = rLine.getInt("service_id");
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.activateNewService(service_id, rLine.getInt("userID"), getOperName());
      if (servicesFunctions.isError()) {
        jObj.put("ERROR", servicesFunctions.getErrorMSG());
      }

      send_object(jObj);
      return;

    }

    if (rLine.getString("action").equals("updateService")) {
      JSONObject jsonObject = new JSONObject();
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.updateService(getOperName(), db, rLine);
      if (servicesFunctions.isError()) {
        jsonObject.put("ERROR", servicesFunctions.getErrorMSG());
      }
      send_object(jsonObject);
      return;
    }

    if (rLine.getString("action").equals("get_datum_isteka_servisa")) {
      jObj = new JSONObject();
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      String datumIsteka = servicesFunctions.getDatumIsteka(rLine.getInt("id"));

      if (datumIsteka == null) {
        datumIsteka = "KORISNIK NEMA SERVIS";
      }
      jObj.put("datumIsteka", datumIsteka);
      send_object(jObj);
      return;

    }

    if (rLine.getString("action").equals("add_BOX_Service")) {
      JSONObject object = new JSONObject();

      //provera da li korisnik, kartica postoji. Ako postoji prijaviti operateru da ne moze da se napravi servis
      Boolean checkNet = false;
      Boolean checkDtv = false;
      Boolean checkFix = false;
      Boolean checkIptv = false;

      if (rLine.has("groupName")) {
        //createInternetService;
        NETFunctions netFunctions = new NETFunctions(db, getOperName());
        checkNet = netFunctions.check_userName_busy(rLine.getString("userName"));

      }

      if (rLine.has("cardID")) {
        //create DTV SERVICE
        DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
        checkDtv = dtvFunctions.check_card_busy(rLine.getInt("cardID"));

      }

      if (rLine.has("FIX_TEL")) {
        //create FIX SERVICE
        FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
        checkFix = fixFunctions.check_TELBr_bussy(rLine.getString("FIX_TEL"));
      }

      if (rLine.has("STB_MAC")) {
        IPTVFunctions iptvFunctions = new IPTVFunctions(db, getOperName());
        checkIptv = iptvFunctions.checkUserBussy(rLine.getString("STB_MAC"));
      }

      //ako je user ili kartica zauzeta poslati obavestenje u suprotnom napraviti boxPaket serivis i dodati ostrale servise koriniku
      if (checkDtv || checkNet || checkFix || checkIptv) {
        String message = null;
        if (checkDtv) {
          message = "DTV Kartica je zauzeta";
          object.put("ERROR", message);
        }

        if (checkNet) {
          message = "Internet korisnicko ime je zauzeto";
          object.put("ERROR", message);
        }

        if (checkFix) {
          message = "Broj telefona je zauzet";
          object.put("ERROR", message);
        }

        if (checkIptv) {
          message = "IPTV STB_MAC je zauzet";
          object.put("ERROR", message);
        }
      } else {
        //add BOX to servicesUser
        addBoxService addBox = new addBoxService(db, getOperName());
        boolean hostIsAlive = addBox.checkIPTVAlive();
        if (!hostIsAlive) {
          object.put("ERROR", "IPTV Server nije dostupan");

        } else {
          addBox.addBox(rLine, getOperName());
        }

      }

      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("get_paket_box")) {
      jObj = new JSONObject();
      ResultSet rs = null;
      PreparedStatement ps = null;
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());

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
              paketBox.put("DTV_PAKET_ID",
                  dtvFunctions.getPacketCriteriaGroup(rs.getInt("DTV_id")));
            }

            if (rs.getString("NET_naziv") != null) {
              paketBox.put("NET_id", rs.getInt("NET_id"));
              paketBox.put("NET_naziv", get_paket_naziv("internetPaketi", rs.getInt("NET_id")));
            }

            if (rs.getString("TEL_naziv") != null) {
              FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
              paketBox.put("FIX_id", rs.getInt("TEL_id"));
              paketBox.put("FIX_naziv", get_paket_naziv("FIXPaketi", rs.getInt("TEL_id")));
              paketBox.put("FIX_PAKET_ID",
                  fixFunctions.getPaketID(paketBox.getString("FIX_naziv")));
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

    if (rLine.getString("action").equals("delete_box_paket")) {
      JSONObject obj = new JSONObject();
      BoxFunctions boxFunctions = new BoxFunctions(db);
      boolean deleted = boxFunctions.deleteBoxPaket(rLine.getInt("id"));
      if (deleted) {
        obj.put("MESSAGE", "DELETED");
      } else {
        obj.put("ERROR", boxFunctions.error);
      }

      send_object(obj);

    }

    if (rLine.getString("action").equals("add_service_to_user_DTV")) {
      jObj = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      if (dtvFunctions.check_card_busy(rLine.getInt("DTVKarticaID"))) {
        jObj.put("ERROR",
            String.format("Kartica sa brojem %d je zauzeta", rLine.getInt("DTVKarticaID")));
        send_object(jObj);
        return;
      }

      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions
          .addServiceDTV(rLine.getInt("id"), rLine.getString("nazivPaketa"),
              rLine.getInt("userID"), getOperName(), rLine.getDouble("servicePopust"),
              rLine.getDouble("cena"), rLine.getBoolean("obracun"), rLine.getString("brojUgovora"),
              rLine.getString("idUniqueName"), rLine.getInt("packetID"),
              rLine.getDouble("pdv"), rLine.getString("komentar"));

      if (servicesFunctions.isError()) {
        jObj.put("ERROR", servicesFunctions.getErrorMSG());
      } else {
        jObj.put("Message", "SERVICE_ADDED");
      }
      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("add_service_to_user_NET")) {
      jObj = new JSONObject();
      NETFunctions netFunctions = new NETFunctions(db, getOperName());

      if (netFunctions.check_userName_busy(rLine.getString("userName"))) {
        jObj.put("ERROR",
            String.format("Korisničko ime %s je zauzeto", rLine.getString("userName")));
        send_object(jObj);
        return;
      }
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.addServiceNET(rLine);
      if (servicesFunctions.isError()) {
        jObj.put("ERROR", servicesFunctions.getErrorMSG());
      } else {
        jObj.put("Message",
            String.format("Korisničko ime %s je snimljeno", rLine.getString("userName")));
      }


      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("delete_service_user")) {
      jObj = new JSONObject();

      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.deleteService(rLine.getInt("serviceID"), rLine.getInt("userID"));
      if (servicesFunctions.isError()) {
        jObj.put("ERROR", servicesFunctions.getErrorMSG());
      }

      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("get_uplate_user")) {
      jObj = new JSONObject();
      UserFunc userFunc = new UserFunc(db, getOperName());
      jObj = userFunc.getUplateUser(rLine.getInt("userID"));
      if (userFunc.isError()) {
        jObj.put("ERROR", userFunc.getErrorMSG());
      }
      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("uplata_korisnika")) {
      JSONObject object = new JSONObject();
      Uplate uplate = new Uplate(getOperName(), db);
      uplate
          .novaUplata(rLine.getInt("userID"), rLine.getDouble("uplaceno"), rLine.getString("opis"),
              rLine.getString("datumUplate"));
      if (uplate.isError()) {
        object.put("ERROR", uplate.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if(rLine.getString("action").equals("zaduziKorisnikaCustom")){
      JSONObject object = new JSONObject();

      ZaduziCustom zaduziCustom = new ZaduziCustom(getOperName(), db);
      zaduziCustom.zaduziKorisnika(rLine);
      if(zaduziCustom.isError()){
        object.put("ERROR", zaduziCustom.getErrorMSG());
      }

      send_object(object);
      return;
    }


   if (rLine.getString("action").equals("IzmeniKorisnikZaduzenje")){
     JSONObject object = new JSONObject();

     ZaduziCustom zaduziCustom = new ZaduziCustom(getOperName(),db);
     zaduziCustom.izmeniZaduzenje(rLine);
     if(zaduziCustom.isError()){
       object.put("ERROR", zaduziCustom.getErrorMSG());
     }
     send_object(object);
     return;
   }

    //TODO remove me test
    if (rLine.getString("action").equals("testProduziKorisnik")) {
      JSONObject object = new JSONObject();
      Uplate uplate = new Uplate(getOperName(), db);
      String endDate = uplate.produzivanjeServisa(rLine.getInt("userID"));
      object.put("endDate", endDate);
      if (uplate.isError()) {
        object.put("ERROR", uplate.getErrorMSG());
      }
      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("uplata_delete")) {
      JSONObject object = new JSONObject();
      Uplate uplate = new Uplate(operName, db);
      uplate.deleteUplata(rLine.getInt("idUplate"));
      if (uplate.isError()) {
        object.put("ERROR", uplate.getErrorMSG());
      }
      send_object(object);
      return;
    }


    if (rLine.getString("action").equals("get_Service_ident")) {
      jObj = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM servicesUser WHERE id=?";
      String ident = "SERVIS NEMA IDENTIFIKACIJU";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("id_Service"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          if (rs.getString("idDTVCard") != null) {
            ident = rs.getString("idDTVCard");
          }
          if (rs.getString("UserName") != null) {
            ident = rs.getString("UserName");
          }
          if (rs.getString("FIKSNA_TEL") != null) {
            ident = rs.getString("FIKSNA_TEL");
          }
          if (rs.getString("IPTV_MAC") != null) {
            ident = rs.getString("IPTV_MAC");
          }
        }
      } catch (SQLException ex) {
        jObj.put("ERROR", ex.getMessage());
        ex.printStackTrace();
      }
      jObj.put("ident", ident);
      send_object(jObj);
    }

    if (rLine.getString("action").equals("add_new_ugovor")) {
      query = "INSERT INTO ugovori_types "
          + "(naziv,  text_ugovor)"
          + " VALUES (?,?)";

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
        if (rs.isBeforeFirst()) {
          rs.next();
          jObj.put("idUgovora", rs.getInt("id"));
          jObj.put("nazivUgovora", rs.getString("naziv"));
          jObj.put("textUgovora", rs.getString("text_ugovor"));
        }
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
      query =
          "INSERT INTO ugovori_korisnik (naziv, textUgovora, komentar, pocetakUgovora, krajUgovora, userID, brojUgovora, trajanje) "
              + "VALUES "
              + "(?,?,?,?,?,?,?,?)";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("naziv"));
        ps.setString(2, rLine.getString("textUgovora"));
        ps.setString(3, rLine.getString("komentar"));
        ps.setString(4, rLine.getString("pocetakUgovora"));
        ps.setString(5, rLine.getString("krajUgovora"));
        ps.setInt(6, rLine.getInt("userID"));
        ps.setString(7, rLine.getString("brojUgovora"));
        ps.setString(8, rLine.getString("trajanjeUgovora"));
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
          jObj.put("ERROR",
              String.format("BROJ UGOVORA %s je zauzet", rs.getString("brojUgovora")));
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
          jObj.put("brojUgovora", rs.getString("brojUgovora"));

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
      JSONObject faktureData;
      jObj = new JSONObject();
      query = "SELECT * FROM faktureData WHERE userId=? AND datum=? and br=?";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("userID"));
        ps.setString(2, rLine.getString("datum"));
        ps.setInt(3, rLine.getInt("br"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          int i = 0;
          while (rs.next()) {
            faktureData = new JSONObject();
            faktureData.put("id", rs.getInt("id"));
            faktureData.put("br", rs.getString("br"));
            faktureData.put("naziv", rs.getString("naziv"));
            faktureData.put("jedMere", rs.getString("jedMere"));
            faktureData.put("kolicina", rs.getInt("kolicina"));
            faktureData.put("cenaBezPDV", rs.getDouble("cenaBezPDV"));
            faktureData.put("pdv", rs.getDouble("pdv"));
            double cena = rs.getDouble("cenaBezPDV");
            double pdv = rs.getDouble("pdv");
            double iznosPDV = +valueToPercent.getPDVOfValue(cena, pdv);
            double iznosSaPDV = cena + iznosPDV;
            faktureData.put("VrednostSaPDV", iznosSaPDV);
            faktureData.put("iznosPDV", iznosPDV);
            faktureData.put("OsnovicaZaPDV", rs.getInt("kolicina") * rs.getDouble("cenaBezPDV"));
            faktureData.put("operater", rs.getString("operater"));
            faktureData.put("userID", rs.getInt("userID"));
            faktureData.put("datum", rs.getString("datum"));
            faktureData.put("godina", rs.getString("godina"));
            faktureData.put("mesec", rs.getString("mesec"));
            jObj.put(String.valueOf(i), faktureData);
            i++;
          }
        }
        rs.close();
        ps.close();
      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jObj);

      return;
    }

    if (rLine.getString("action").equals("get_uniqueFakture")) {
      JSONObject faktureData;
      jObj = new JSONObject();
      query = "SELECT * FROM faktureData WHERE userID=? GROUP BY datum";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("userID"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          int i = 0;
          while (rs.next()) {
            faktureData = new JSONObject();
            faktureData.put("id", rs.getInt("id"));
            faktureData.put("br", rs.getString("br"));
            faktureData.put("naziv", rs.getString("naziv"));
            faktureData.put("jedMere", rs.getString("jedMere"));
            faktureData.put("kolicina", rs.getInt("kolicina"));
            faktureData.put("cenaBezPDV", rs.getDouble("cenaBezPDV"));
            faktureData.put("pdv", rs.getDouble("pdv"));
            faktureData.put("operater", rs.getString("operater"));
            faktureData.put("userID", rs.getInt("userID"));
            faktureData.put("datum", rs.getString("datum"));
            faktureData.put("godina", rs.getString("godina"));
            faktureData.put("mesec", rs.getString("mesec"));
            faktureData.put("cenaBezPDV", rs.getDouble("cenaBezPDV"));
            jObj.put(String.valueOf(i), faktureData);
            i++;
          }
        }
        rs.close();
        ps.close();
      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jObj);

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
      query =
          "INSERT INTO jFakture (vrstaNaziv, jedMere, kolicina, jedCena, stopaPDV, brFakture, godina, userId, dateCreated)"
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

      query = "SELECT * FROM mesta ORDER BY naziv";
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

    if (rLine.getString("action").equals("getNazivMesta")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT naziv FROM mesta WHERE broj=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("brojMesta"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          jsonObject.put("nazivMesta", rs.getString("naziv"));
        }
        ps.close();
      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("getNazivAdrese")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT naziv FROM adrese WHERE broj=? AND brojMesta=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("brojAdrese"));
        ps.setString(2, rLine.getString("brojMesta"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          jsonObject.put("nazivAdrese", rs.getString("naziv"));
        }
        ps.close();
        rs.close();

      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jsonObject);

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

    if (rLine.getString("action").equals("getAllAdrese")) {
      query = "SELECT * FROM adrese ORDER BY nazivAdrese";
      jObj = new JSONObject();

      try {
        ps = db.conn.prepareStatement(query);
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

    if (rLine.getString("action").equals("getAdrese")) {
      query = "SELECT * FROM adrese WHERE brojMesta = ? order by naziv";
      jObj = new JSONObject();

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("brojMesta"));
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
      query = "SELECT * FROM adrese WHERE brojMesta=? AND broj=?";

      jObj = new JSONObject();

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("brojMesta"));
        ps.setString(2, rLine.getString("brojAdrese"));
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
            opers.put("type", rs.getString("type"));
            opers.put("typeNo", rs.getInt("typeNo"));
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
      query =
          "INSERT INTO operateri (username,password, adresa, telefon, komentar, aktivan, ime, type, typeNo)"
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("username"));
        ps.setString(2, rLine.getString("password"));
        ps.setString(3, rLine.getString("adresa"));
        ps.setString(4, rLine.getString("telefon"));
        ps.setString(5, rLine.getString("komentar"));
        ps.setBoolean(6, rLine.getBoolean("aktivan"));
        ps.setString(7, rLine.getString("ime"));
        ps.setString(8, rLine.getString("type"));
        ps.setInt(9, rLine.getInt("typeNo"));
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
            + "aktivan=?, ime=?, password=?, type=?, typeNo=? WHERE id=?";
      } else {
        query = "UPDATE operateri  SET adresa=?, telefon=?, komentar=?, "
            + "aktivan=?, ime=?, type=?, typeNo=? WHERE id=?";
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
          ps.setString(7, rLine.getString("type"));
          ps.setInt(8, rLine.getInt("typeNo"));
          ps.setInt(9, rLine.getInt("operaterID"));
        } else {
          ps.setString(6, rLine.getString("type"));
          ps.setInt(7, rLine.getInt("typeNo"));
          ps.setInt(8, rLine.getInt("operaterID"));
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
        ps.close();
        rs.close();

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
        ps.close();

        jObj.put("Message", "INTERNET_PAKET_UPDATED");

      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jObj);
      return;

    }

    if (rLine.getString("action").equals("delete_internet_paket")) {
      JSONObject obj = new JSONObject();
      InternetPaket internetPaket = new InternetPaket(db);
      boolean deleted = internetPaket.deleteInternetPaket(rLine.getInt("id"));
      if (deleted) {
        obj.put("MESSAGE", "DELETED");
      } else {
        obj.put("ERROR", internetPaket.error);
      }

      send_object(obj);

    }

    if (rLine.getString("action").equals("getDigitalTVAddonCards")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      object = dtvPaketFunctions.getDTVAddonCards();
      if (dtvPaketFunctions.isError()) {
        object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.get("action").equals("getDigitalTVPaketi")) {
      jObj = new JSONObject();
      if (rLine.has("showAddons")) {
        query = "SELECT * FROM  digitalniTVPaketi";
      } else {
        query = "SELECT * FROM digitalniTVPaketi WHERE dodatak=false and dodatnakartica=false";
      }

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
            dtv.put("dodatak", rs.getBoolean("dodatak"));
            dtv.put("dodatnaKartica", rs.getBoolean("dodatnaKartica"));
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
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      dtvPaketFunctions.addDTVPaket(rLine);
      if (dtvPaketFunctions.isError()) {
        object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      }
      send_object(object);
      return;

    }

    if (rLine.get("action").equals("edit_dtv_paket")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      dtvPaketFunctions.editDTVPaket(rLine);
      if (dtvPaketFunctions.isError()) {
        object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      }

      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("delete_dtv_paket")) {
      JSONObject obj = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      dtvPaketFunctions.deleteDTVPaket(rLine.getInt("id"));
      if (dtvPaketFunctions.isError()) {
        obj.put("ERROR", dtvPaketFunctions.getErrorMSG());
      } else {
        obj.put("MESSAGE", "DELETED");

      }
      send_object(obj);

    }


    if (rLine.get("action").equals("save_Box_Paket")) {
      jObj = new JSONObject();
      query =
          "INSERT INTO paketBox (naziv, DTV_id, DTV_naziv, NET_id, NET_naziv, TEL_id, TEL_naziv, IPTV_id, IPTV_naziv, cena, pdv)"
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

    if (rLine.get("action").equals("update_Box_Paket")) {
      jObj = new JSONObject();
      query =
          "INSERT INTO paketBox (naziv, DTV_id, DTV_naziv, NET_id, NET_naziv, TEL_id, TEL_naziv, IPTV_id, IPTV_naziv, cena, pdv)"
              + "VALUES"
              + "(?,?,?,?,?,?,?,?,?,?,?)";
      query = "UPDATE paketBox SET "
          + "naziv=?, "
          + "DTV_id=?, DTV_naziv=?, "
          + "NET_id=?, NET_naziv=?, "
          + "TEL_id=?, TEL_naziv=?, "
          + "IPTV_id=?, IPTV_naziv=?,"
          + "cena=?, pdv=?"
          + "WHERE id=? ";
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
        ps.setInt(12, rLine.getInt("boxID"));
        ps.executeUpdate();
        jObj.put("Message", "BOX_SAVED");

      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
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
    if (rLine.get("action").equals("dd_fixTel_paket")) {
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

    if (rLine.get("action").equals("delete_fiksna_paket")) {
      JSONObject obj = new JSONObject();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      boolean delete = fixFunctions.deleteFixPaket(rLine.getInt("id"));
      if (delete) {
        obj.put("MESSAGE", "DELETED");
      } else {
        obj.put("ERROR", fixFunctions.getErrorMSG());
      }
      send_object(obj);
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

    if (rLine.getString("action").equals("get_FIX_account_saobracaj")) {
      jObj = new JSONObject();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      jObj = fixFunctions.getAccountSaobracaj(
          rLine.getInt("id_ServiceUser"),
          rLine.getString("zaMesec"));
      if (fixFunctions.isError()) {
        jObj.put("ERROR", fixFunctions.getErrorMSG());
      }
      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("addFixUslugu")) {
      jObj = new JSONObject();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      if (fixFunctions.check_TELBr_bussy(rLine.getString("brojTel"))) {
        jObj.put("ERROR", String.format("Broj telefona %s je zauzet", rLine.getString("brojTel")));
        send_object(jObj);
        return;
      }

      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.addServiceFIX(rLine);
      if (servicesFunctions.isError()) {
        jObj.put("ERROR", servicesFunctions.getErrorMSG());
      }
      send_object(jObj);
      return;

    }

    if (rLine.get("action").equals("add_CSV_FIX_Telefonija")) {
      JSONObject object = new JSONObject();
      CSVIzvestajImport csvIzvestajImport = new CSVIzvestajImport(db);
      csvIzvestajImport.importFile(rLine);
      if (csvIzvestajImport.isError()) {
        object.put("ERROR", csvIzvestajImport.getErrorMsg());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("get_CSV_Data")) {
      jObj = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM csv WHERE connectTime >= ? AND connectTime <= ? ";
      int i = 0;
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("od") + " 00:00:00");
        ps.setString(2, rLine.getString("do") + " 23:59:59");
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

    if (rLine.getString("action").equals("get_csv_data_account")) {
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      JSONObject poziviZaMesec = new JSONObject();
      poziviZaMesec = fixFunctions
          .getPoziviZaMesec(rLine.getString("zaMesec"), rLine.getString("account"));
      if (fixFunctions.isError()) {
        poziviZaMesec.put("ERROR", fixFunctions.getErrorMSG());
      }
      send_object(poziviZaMesec);
      return;
    }

    if (rLine.getString("action").equals("deleteCSV_ID")) {
      JSONObject object = new JSONObject();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      fixFunctions.deleteCSV_ID(rLine.getJSONArray("intArrays"));
      if (fixFunctions.isError()) {
        object.put("ERROR", fixFunctions.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("obracunaj_FIX_za_mesec")) {
      JSONObject object = new JSONObject();
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      fixFunctions.obracunajZaMesec(rLine.getString("zaMesec"));
      if (fixFunctions.isError()) {
        object.put("ERROR", fixFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    //END FIKSNA TELEFONIJA PAKETI
    //IPTV

    if (rLine.getString("action").equals("check_iptv_is_alive")) {
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      JSONObject object = new JSONObject();

      if (!stalkerRestAPI2.isHostAlive()) {
        object.put("ERROR", stalkerRestAPI2.getHostMessage());
      }
      send_object(object);
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
          + "name=?, iptv_id=?, external_id=?, cena=?, pdv=?, "
          + "opis=? WHERE id=?";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("name"));
        ps.setInt(2, rLine.getInt("iptv_id"));
        ps.setString(3, rLine.getString("external_id"));
        ps.setDouble(4, rLine.getDouble("cena"));
        ps.setDouble(5, rLine.getDouble("pdv"));
        ps.setString(6, rLine.getString("opis"));
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
      StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db, getOperName());
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
      StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db, getOperName());

    }

    if (rLine.getString("action").equals("save_IPTV_USER")) {
      JSONObject jObj = new JSONObject();
      StalkerRestAPI2 stAPI2 = new StalkerRestAPI2(db, getOperName());
      if (stAPI2.checkUser(rLine.getString("STB_MAC"))) {
        jObj.put("ERROR", String.format("MAC Adresa %s je zauzeta", rLine.getString("STB_MAC")));
        send_object(jObj);
        return;
      }
      jObj = stAPI2.saveUSER(rLine);
      if (!jObj.has("ERROR")) {
        ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
        servicesFunctions.addServiceIPTV(rLine);
      }
      send_object(jObj);
      return;
    }

    if (rLine.getString("action").equals("getNextFreeUgovorID")) {
      int userID = rLine.getInt("userID");
      int noUgovor = 1;
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT count(brojUgovora) FROM ugovori_korisnik WHERE userID=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, userID);
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          noUgovor += rs.getInt(1);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("NO_UGOVORA", noUgovor);
      send_object(jsonObject);
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

    if (rLine.getString("action").equals("get_iptv_mac_info")) {
      JSONObject object = new JSONObject();
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      if (stalkerRestAPI2.isError()) {
        object.put("ERROR", stalkerRestAPI2.getErrorMSG());
      } else {
        object = stalkerRestAPI2.getAccountInfo(rLine.getString("MAC"));
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("change_iptv_pass")) {
      JSONObject object = new JSONObject();
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      if (stalkerRestAPI2.isError()) {
        object.put("ERROR", stalkerRestAPI2.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("changeIPTVEndDate")) {
      JSONObject object = new JSONObject();
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      stalkerRestAPI2.setEndDate(rLine.getString("MAC"), rLine.getString("endDate"));
      if (stalkerRestAPI2.isError()) {
        object.put("ERROR", stalkerRestAPI2.getErrorMSG());
      }
      send_object(object);
      return;
    }
    if (rLine.getString("action").equals("save_iptv_acc_data")) {
      JSONObject object = new JSONObject();
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      stalkerRestAPI2.saveAccountInfo(rLine);
      if (stalkerRestAPI2.isError()) {
        object.put("ERROR", stalkerRestAPI2.getErrorMSG());
      }
      send_object(object);
      return;
    }



    if (rLine.getString("action").equals("getOstaleUslugeData")) {
      JSONObject jObj = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM ostaleUsluge";
      try {
        ps = db.conn.prepareStatement(query);
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          int i = 0;
          while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("id", rs.getInt("id"));
            obj.put("naziv", rs.getString("naziv"));
            obj.put("cena", Double.valueOf(rs.getDouble("cena")));
            obj.put("pdv", Double.valueOf(rs.getDouble("pdv")));
            obj.put("cenaPDV", Double.valueOf(rs.getDouble("cena") + valueToPercent
                .getPDVOfValue(rs.getDouble("cena"), rs.getDouble("pdv"))));
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

    if (rLine.getString("action").equals("delete_IPTV_paket")) {
      JSONObject obj = new JSONObject();
      IPTVFunctions iptvFunctions = new IPTVFunctions(db, getOperName());
      boolean deleted = iptvFunctions.deletePaket(rLine.getInt("id"));
      if (deleted) {
        obj.put("MESSAGE", "DELETED");
      } else if (iptvFunctions.isError()) {
        obj.put("ERROR", iptvFunctions.getErrorMSG());
      }
      send_object(obj);
      return;
    }



    if (rLine.getString("action").equals("updateOstaleUslugu")) {
      PreparedStatement ps;
      JSONObject jsonObject = new JSONObject();
      String query = "UPDATE ostaleUsluge set naziv=?, cena=?, pdv=?, opis=? WHERE id =?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("naziv"));
        ps.setDouble(2, rLine.getDouble("cena"));
        ps.setDouble(3, rLine.getDouble("pdv"));
        ps.setString(4, rLine.getString("opis"));
        ps.setInt(5, rLine.getInt("id"));
        ps.executeUpdate();
        ps.close();
        rs.close();
        jsonObject.put("INFO", "SNIMLJENO");
      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("snimiOstaleUslugu")) {
      PreparedStatement ps;
      JSONObject jsonObject = new JSONObject();
      String query = "INSERT INTO ostaleUsluge (naziv, cena, pdv, opis) VALUES (?,?,?,?)";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("naziv"));
        ps.setDouble(2, rLine.getDouble("cena"));
        ps.setDouble(3, rLine.getDouble("pdv"));
        ps.setString(4, rLine.getString("opis"));
        ps.executeUpdate();
        ps.close();

        jsonObject.put("INFO", "SNIMLJENO");

      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("save_OstaleUsluge_USER")) {
      JSONObject object = new JSONObject();
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.addServiceOstalo(rLine);
      if (servicesFunctions.isError()) {
        object.put("ERROR", servicesFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("delete_OstaleUsluge_paket")) {
      JSONObject obj = new JSONObject();
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      servicesFunctions.deletePaketOstalo(rLine.getInt("id"));
      if (servicesFunctions.isError()) {
        obj.put("ERROR", servicesFunctions.getErrorMSG());
      } else {
        obj.put("MESSAGE", "IZBRISANO");
      }

      send_object(obj);
      return;

    }

    if (rLine.getString("action").equals("addArtikal")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.addArtikl(rLine);
      if (artikliFunctions.isError()) {
        jsonObject.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jsonObject.put("MESSAGE", "ARTIKL_ADDED");
      }

      send_object(jsonObject);
      return;
    }

    if (rLine.getString("action").equals("deleteArtikl")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.deleteArtikl(rLine.getInt("id"));
      if (artikliFunctions.isError()) {
        jsonObject.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jsonObject.put("MESSAGE", "ARTIKLE_DELETED");
      }

      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("editArtikal")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.editArtikl(rLine);
      if (artikliFunctions.isError()) {
        jsonObject.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jsonObject.put("MESSAGE", "ARTIKLE_EDITED");
      }

      send_object(jsonObject);
      return;
    }

    if (rLine.getString("action").equals("getAllArtikles")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.getAllArtikles();
      if (artikliFunctions.isError()) {
        jsonObject.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jsonObject = artikliFunctions.getArtikles();
      }

      send_object(jsonObject);
      return;
    }

    if (rLine.getString("action").equals("searchArtikal")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.searchArtikles(rLine);
      jsonObject = artikliFunctions.getArtikles();
      send_object(jsonObject);
      return;

    }

    if (rLine.getString("action").equals("zaduziMagacinArtikal")) {
      JSONObject jObj = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.zaduziArtikalMag(rLine);
      if (artikliFunctions.isError()) {
        jObj.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jObj.put("INFO", "SUCCESS");
      }
      send_object(jObj);
    }

    if (rLine.getString("action").equals("zaduziUserArtikal")) {
      JSONObject jsonObject = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.zaduziArtikalUser(rLine);
      if (artikliFunctions.isError()) {
        jsonObject.put("ERROR", artikliFunctions.getErrorMSG());
      } else {
        jsonObject.put("INFOP", "SUCCES");
      }
      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("razduziUserArtikal")) {
      JSONObject object = new JSONObject();
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, getOperName());
      artikliFunctions.razduziArtikalUser(rLine.getInt("artikalID"), rLine.getInt("magacinID"),
          rLine.getInt("userID"), rLine.getString("komentar"));
      if (artikliFunctions.isError()) {
        object.put("ERROR", artikliFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("editMagacin")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      String query = "UPDATE Magacin SET naziv=?, opis=?, glavniMagacin=? WHERE id=?";

      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("naziv"));
        ps.setString(2, rLine.getString("opis"));
        ps.setBoolean(3, rLine.getBoolean("glavniMagacin"));
        ps.setInt(4, rLine.getInt("id"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }

      send_object(jsonObject);

    }

    if (rLine.getString("action").equals("novMagacin")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      String query = "INSERT INTO Magacin (naziv, opis, glavniMagacin) VALUES (?,?,?)";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, rLine.getString("naziv"));
        ps.setString(2, rLine.getString("opis"));
        ps.setBoolean(3, rLine.getBoolean("glavniMagacin"));
        ps.executeUpdate();
        ps.close();

      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("getMagacini")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM Magacin";

      try {
        ps = db.conn.prepareStatement(query);
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          int i = 0;
          while (rs.next()) {
            JSONObject mag = new JSONObject();
            mag.put("id", rs.getInt("id"));
            mag.put("naziv", rs.getString("naziv"));
            mag.put("opis", rs.getString("opis"));
            mag.put("glavniMagacin", rs.getBoolean("glavniMagacin"));
            jsonObject.put(String.valueOf(i), mag);
            i++;
          }
        }
        ps.close();
        rs.close();
      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);
      return;
    }

    if (rLine.getString("action").equals("getMagacin")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM Magacin WHERE id=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("idMagacin"));
        rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
          rs.next();
          jsonObject.put("id", rs.getInt("id"));
          jsonObject.put("naziv", rs.getString("naziv"));
          jsonObject.put("opis", rs.getString("opis"));
          jsonObject.put("glavniMagacin", rs.getString("glavniMagacin"));
        }
        ps.close();
        rs.close();
      } catch (SQLException e) {
        jsonObject.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);
      return;

    }

    if (rLine.getString("action").equals("deleteMagacin")) {
      JSONObject jsonObject = new JSONObject();
      PreparedStatement ps;
      String query = "DELETE FROM Magacin WHERE id=?";
      try {
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("id"));
        ps.executeUpdate();
        query = "DELETE FROM Artikli WHERE idMagacin=?";
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, rLine.getInt("id"));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jsonObject);

    }

    if (rLine.getString("action").equals("getArtikliTracking")) {
      ArtikliFunctions artikliFunctions = new ArtikliFunctions(db, operName);
      JSONObject artOBJ = artikliFunctions
          .getArtikliTracking(rLine.getInt("artiklID"), rLine.getInt("magID"),
              rLine.getInt("uniqueID"));

      send_object(artOBJ);
      return;

    }

    if (rLine.getString("action").equals("getMesecniObracun")) {
      JSONObject jsonObject = new JSONObject();
      MesecniObracun mesecniObracun = new MesecniObracun(db, getOperName());
      jsonObject = mesecniObracun.getMesecniObracunPDV(rLine.getInt("brOd"), rLine.getInt("brDo"),
          rLine.getString("odDatuma"), rLine.getString("doDatum"), getOperName());
      if (mesecniObracun.hasError) {
        jsonObject.put("ERROR", mesecniObracun.errorMessage);
      }
      send_object(jsonObject);
    }

    if (rLine.getString("action").equals("obracunZaMesec")) {
      JSONObject object = new JSONObject();
      MesecniObracun mesecniObracun = new MesecniObracun(db, getOperName());
      //check if exist obracun
      //if exist return warning that obracun exist
      //else obracunaj
      boolean exist = mesecniObracun.checkIfExistObracun(rLine.getString("zaMesec"));
      if (exist) {
        object
            .put("ERROR", String.format("Obračun za mesec %s postoji", rLine.getString("zaMesec")));
        send_object(object);
        return;
      }
      FIXFunctions fixFunctions = new FIXFunctions(db, getOperName());
      fixFunctions.obracunajZaMesec(rLine.getString("zaMesec"));
      if (fixFunctions.isError()) {
        object.put("ERROR", fixFunctions.getErrorMSG());
        send_object(object);
        return;
      }
      double ukupanDUG = mesecniObracun.obracunajZaMesec(rLine.getString("zaMesec"));
      if (mesecniObracun.isError()) {
        object.put("ERROR", mesecniObracun.getErrorMSG());
        ukupanDUG = 0.00;
      } else {
        object.put("ukupno", ukupanDUG);
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getUserRacun")) {
      UserRacun userRacun = new UserRacun(rLine, getOperName(), db);
      send_object(userRacun.getData());
      return;
    }

    if (rLine.getString("action").equals("getUserMesecnaZaduzenja")) {
      JSONObject object = new JSONObject();
      Uplate uplate = new Uplate(getOperName(), db);
      object = uplate.getMesecnaZaduzenja(rLine.getInt("userID"));
      if (uplate.isError()) {
        object.put("ERROR", uplate.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getUserMesecnaZaduzenjaZaMesec")) {
      JSONObject object = new JSONObject();
      Uplate uplate = new Uplate(getOperName(), db);
      object = uplate
          .getMesecnaZaduzenjaServisi(rLine.getInt("userID"), rLine.getString("zaMesec"));
      if (uplate.isError()) {
        object.put("ERROR", uplate.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("OPTIONS_SAVE")) {
      JSONObject obj = new JSONObject();
      JSONObject retObj = new JSONObject();
      PreparedStatement ps = null;
      String query;

      //BRISANJE PODATAKA FIRME
      query = "DELETE  FROM settings  ";
      try {
        ps = db.conn.prepareStatement(query);
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      obj = rLine;
      query = "INSERT INTO settings (settings, value) VALUES (?,?)";

      try {
        for (String t : rLine.keySet()) {
          if(t.equals("action")) continue;
          ps = db.conn.prepareStatement(query);
          ps.setString(1, t);
          if (obj.get(t) instanceof String) {
            ps.setString(2, obj.getString(t));
          } else if (obj.get(t) instanceof Boolean) {
            ps.setBoolean(2, obj.getBoolean(t));
          }

          ps.executeUpdate();
        }
        ps.close();
      } catch (SQLException e) {
        retObj.put("EROOR", e.getMessage());
        e.printStackTrace();
      }

      send_object(retObj);
    }

    if (rLine.getString("action").equals("GET_OPTIONS")) {
      JSONObject jObj = new JSONObject();
      PreparedStatement ps;
      ResultSet rs;
      String query = "SELECT * FROM settings";
      try {
        ps = db.conn.prepareStatement(query);
        rs = ps.executeQuery();
        if(rs.isBeforeFirst()){
          while (rs.next()) {
            if (rs.getString("settings").equals("FIRMA_WEBPAGE")) {
              jObj.put("FIRMA_WEBPAGE", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_ADRESA")) {
              jObj.put("FIRMA_ADRESA", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_MBR")) {
              jObj.put("FIRMA_MBR", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_FAX")) {
              jObj.put("FIRMA_FAX", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_TELEFON")) {
              jObj.put("FIRMA_TELEFON", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_NAZIV")) {
              jObj.put("FIRMA_NAZIV", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_SERVIS_TELEFON")) {
              jObj.put("FIRMA_SERVIS_TELEFON", rs.getString("value"));
            }
            if (rs.getString("settings").equals("FIRMA_TEKUCIRACUN")) {
              jObj.put("FIRMA_TEKUCIRACUN", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_FAKTURA_PEPDV")){
              jObj.put("FIRMA_FAKTURA_PEPDV", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_SERVIS_EMAIL")){
              jObj.put("FIRMA_SERVIS_EMAIL", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_PIB")){
              jObj.put("FIRMA_PIB", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_MESTO_IZDAVANJA_RACUNA")){
              jObj.put("FIRMA_MESTO_IZDAVANJA_RACUNA", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_MESTO_PROMETA_DOBARA")){
              jObj.put("FIRMA_MESTO_PROMETA_DOBARA", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_NACIN_PLACANJA_FAKTURA")){
              jObj.put("FIRMA_NACIN_PLACANJA_FAKTURA", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_ROK_PLACANJA_RACUN")){
              jObj.put("FIRMA_ROK_PLACANJA_RACUN", rs.getString("value"));
            }
            if(rs.getString("settings").equals("FIRMA_ROK_PLACANJA_FAKTURA")){
              jObj.put("FIRMA_ROK_PLACANJA_FAKTURA", rs.getString("value"));
            }
            if (rs.getString("settings").equals("MINISTRA_API_URL")) {
              jObj.put("MINISTRA_API_URL", rs.getString("value"));
            }
            if (rs.getString("settings").equals("MINISTRA_API_USER")) {
              jObj.put("MINISTRA_API_USER", rs.getString("value"));
            }
            if (rs.getString("settings").equals("MINISTRA_API_PASS")) {
              jObj.put("MINISTRA_API_PASS", rs.getString("value"));
            }
            if (rs.getString("settings").equals("DTV_EMM_HOST")) {
              jObj.put("DTV_EMM_HOST", rs.getString("value"));
            }
            if (rs.getString("settings").equals("DTV_EMM_PORT")) {
              jObj.put("DTV_EMM_PORT", rs.getString("value"));
            }
            if (rs.getString("settings").equals("DTV_UDP_TIMEOUT")) {
              jObj.put("DTV_UDP_TIMEOUT", rs.getString("value"));
            }
            if (rs.getString("settings").equals("DTV_SERVICE")) {
              jObj.put("DTV_SERVICE", rs.getBoolean("value"));
            }
          }

        }
        ps.close();
        rs.close();
      } catch (SQLException e) {
        jObj.put("ERROR", e.getMessage());
        e.printStackTrace();
      }
      send_object(jObj);
    }

    if (rLine.getString("action").equals("getAllClientsLocations")) {
      LocationsClients locationsClients = new LocationsClients();
      JSONObject object = locationsClients.getAll(db);
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getAllNetworkDevices")) {
      NetworkDevices networkDevices = new NetworkDevices(db);
      JSONObject allDevices = networkDevices.getAllDevicesJSON();
      if (networkDevices.isError()) {
        allDevices.put("ERROR", networkDevices.getErrorMsgp());
      }
      send_object(allDevices);
      return;
    }

    if (rLine.getString("action").equals("addNetworkDevice")) {
      JSONObject obj = new JSONObject();
      NetworkDevices networkDevices = new NetworkDevices(db);
      networkDevices.addDevice(rLine);
      if (networkDevices.isError()) {
        obj.put("ERROR", networkDevices.getErrorMsgp());
      }
      send_object(obj);
      return;
    }

    if (rLine.getString("action").equals("editNetworkDevice")) {
      JSONObject object = new JSONObject();
      NetworkDevices networkDevices = new NetworkDevices(db);
      networkDevices.editDevice(rLine);
      if (networkDevices.isError()) {
        object.put("ERROR", networkDevices.getErrorMsgp());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getUserTrafficReport")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      object = radius
          .getUserTrafficReport(rLine.getString("username"), rLine.getString("startTime"),
              rLine.getString("stopTime"));
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }
      send_object(object);
      return;

    }

    //MIKROTIK API
    if (rLine.getString("action").equals("mtAPITest")) {
      MikrotikAPI api = new MikrotikAPI(db, getOperName());
      JSONObject allUsers = api.getAllUsers();
      send_object(allUsers);
      return;
    }

    if (rLine.getString("action").equals("getOnlineUsersCount")) {
      JSONObject object = new JSONObject();
      MikrotikAPI mtApi = new MikrotikAPI(db, getOperName());
      int onlineUsersCount = mtApi.getOnlineUsersCount();
      object.put("count", onlineUsersCount);
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("pingAddressMT")) {
      JSONObject object;
      MikrotikAPI mtApi = new MikrotikAPI(db, getOperName());
      object = mtApi.pingMtUser(rLine.getString("nasIP"), rLine.getString("ipAddress"));
      if (mtApi.isError()) {
        object.put("ERROR", mtApi.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("bwMonitor")) {
      JSONObject object;
      MikrotikAPI mtApi = new MikrotikAPI(db, getOperName());
      object = mtApi.bwMonitor(rLine.getString("nasIP"), rLine.getString("interfaceName"));
      if (mtApi.isError()) {
        object.put("ERROR", mtApi.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if(rLine.getString("action").equals("getOnlineUsers")){
      JSONObject object = new JSONObject();
      MikrotikAPI mikrotikAPI = new MikrotikAPI(db, getOperName());
      object = mikrotikAPI.getAllUsers();
      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("checkUsersOnline")) {
      JSONObject object = new JSONObject();
      MikrotikAPI mtApi = new MikrotikAPI(db, getOperName());
      boolean userIsOnline = mtApi.checkUserIsOnline(rLine.getString("username"));
      object.put("userOnline", userIsOnline);
      if (userIsOnline) {
        object.put("userOnlineStatus", mtApi.getOnlineUserData(rLine.getString("username")));
      }
      if (mtApi.isError()) {
        object.put("ERROR", mtApi.getErrorMSG());
      }

      send_object(object);
      return;


    }

    if (rLine.getString("action").equals("getRadReplyUsers")) {
      Radius radReplyUsers = new Radius(db, getOperName());
      JSONObject userSearch = radReplyUsers.getUsers(rLine.getString("userSearch"));
      send_object(userSearch);
      return;
    }

    if (rLine.getString("action").equals("getRadiusOnlineLOG")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      int rowCount = 100;
      if (rLine.has("rowCount")) {
        rowCount = rLine.getInt("rowCount");
      }

      object = radius.getRadiusOnlineLOG(rowCount);
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }
      send_object(object);

      return;
    }

    if (rLine.getString("action").equals("searchRadiusUsers")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      object = radius.searchAllusers();
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }
      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("getCalledID")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      String calledID = radius.getCalledID(rLine.getString("IP"), rLine.getString("sessionID"));
      object.put("calledID", calledID);
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }
      send_object(object);
      return;
    }


    if (rLine.getString("action").equals("getWifiSignalData")) {
      JSONObject object;

      WiFiData wiFiData = new WiFiData(db);
      object = wiFiData.getWifiData();
      if (wiFiData.isError()) {
        object.put("ERROR", wiFiData.getErrorMSG());
      }
      System.out.println(object);

      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("getTrafficReports")) {
      JSONObject object = new JSONObject();

      Radius radius = new Radius(db, getOperName());
      object = radius
          .getTrafficeReports(rLine.getString("startTime"),
              rLine.getString("stopTime"));
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("disconnectUser")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      String s = radius.disconnectUser(rLine.getString("username"), rLine.getString("userIP"),
          rLine.getString("nasIP"));
      object.put("info", s);
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("changeBwLimit")) {
      JSONObject object = new JSONObject();
      Radius radius = new Radius(db, getOperName());
      String s = radius.changeMTRateLimit(rLine.getString("username"), rLine.getString("userIP"),
          rLine.getString("nasIP"), rLine.getString("bwLimit"));
      object.put("info", s);
      if (radius.isError()) {
        object.put("ERROR", radius.getErrorMSG());
      }

      send_object(object);
      return;

    }


    if (rLine.getString("action").equals("sendMessage")) {
      JSONObject object = new JSONObject();

    }

    if (rLine.getString("action").equals("getUserServiceDTVKartice")) {
      JSONObject object;
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      object = dtvFunctions.getUserServiceKartice(rLine.getInt("idService"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getDTVCard")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      object = dtvFunctions.getUserCard(rLine.getInt("serviceID"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }

      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("get_user_DTV_addons")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      int dtvMain = rLine.getInt("dtv_main");
      int userID = rLine.getInt("userID");
      //if is box Service then we ne to sort out DTVPaketFrom box and then proceed with DTVPaketID;
      if (rLine.getBoolean("isBox")) {
        ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
        dtvMain = servicesFunctions.getBoxIDOfDTVLinkedPaket(dtvMain, userID);
        object = dtvFunctions.getUserAddonsID(dtvMain, userID, true);
      } else {
        object = dtvFunctions.getUserAddonsID(dtvMain, userID, false);
      }
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("addDTVAddonCard")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());

      //ako kartica postoji
      Boolean cardExist = dtvFunctions.check_card_busy(rLine.getInt("cardID"));
      if (cardExist) {
        object.put("ERROR", String.format("Kartica sa brojem %d postoji", rLine.getInt("cardID")));
        send_object(object);
        return;
      }

      dtvFunctions.addAddonCard(rLine);
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getAllDTVCards")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      object = dtvFunctions.getAllCards();
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }


    if (rLine.getString("action").equals("getAllCAS")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      object = dtvFunctions.getAllCAS();
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("addCAS")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      dtvFunctions.addCASCode();
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("deleteCAS")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      dtvFunctions.deleteCASCode(rLine.getInt("id"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("updateCASCode")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      dtvFunctions.updateCode(rLine.getInt("id"), rLine.getInt("code"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("updateCASPaketID")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvFunctions = new DTVPaketFunctions(db, getOperName());
      dtvFunctions.updatePaketID(rLine.getInt("id"), rLine.getInt("paketID"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getDTVDodatke")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      object = dtvPaketFunctions.getDTVAddons();
      if (dtvPaketFunctions.isError()) {
        object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getDTVPaketID")) {
      JSONObject object = new JSONObject();
      DTVPaketFunctions dtvPaketFunctions = new DTVPaketFunctions(db, getOperName());
      ServicesFunctions servicesFunctions = new ServicesFunctions(db, getOperName());
      ServiceData service = servicesFunctions.getServiceData(rLine.getInt("idService"));
      object.put("paketID", dtvPaketFunctions.getPacketID(service.getId_service()));

      if (dtvPaketFunctions.isError()) {
        object.put("ERROR", dtvPaketFunctions.getErrorMSG());
      }

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("updateUserDTVPaketID")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      dtvFunctions.updateUserDTVPaketID(rLine.getInt("paketID"), rLine.getInt("serviceID"));
      if (dtvFunctions.isError()) {
        object.put("ERROR", dtvFunctions.getErrorMSG());
      }
      send_object(object);
      return;

    }

    if (rLine.getString("action").equals("addDTVPaketDodatakToUser")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      dtvFunctions.addAddonPaket(rLine.getInt("serviceID"), rLine.getInt("dodatakID"),
          rLine.getInt("userID"));

      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("removeDTVPaketDodatakFromUser")) {
      JSONObject object = new JSONObject();
      DTVFunctions dtvFunctions = new DTVFunctions(db, getOperName());
      dtvFunctions.removeAddonPaket(rLine.getInt("serviceID"), rLine.getString("naziv"),
          rLine.getInt("userID"));
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("addGroup")) {
      JSONObject object = new JSONObject();
      GroupOper groupOper = new GroupOper(this.db);
      groupOper.addGroup(rLine.getString("groupName"));

      if (groupOper.isError()) {
        object.put("ERROR", groupOper.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("getGroups")) {
      JSONObject object = new JSONObject();
      GroupOper groupOper = new GroupOper(this.db);
      if (groupOper.isError()) {
        object.put("ERROR", groupOper.getErrorMSG());
      } else {
        object = groupOper.getGroupJSON();
      }
      send_object(object);
      return;
    }

    if(rLine.getString("action").equals("getGroupOperaters")){
      JSONObject object = new JSONObject();
      GroupOper groupOper = new GroupOper(this.db);
      object = groupOper.getGroupOperaters(rLine.getInt("groupID"));
      if(groupOper.isError()){
        object.put("ERROR", groupOper.getErrorMSG());
      }
      send_object(object);
      return;
    }

    //AVAILABLE OPERS
    if (rLine.getString("action").equals("getAvOpers")) {
      JSONObject object = new JSONObject();
      GroupOper groupOper = new GroupOper(this.db);
      object = groupOper.getAvOpers();
      if (groupOper.isError()) {
        object.put("ERROR", groupOper.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if (rLine.getString("action").equals("removeOperFromGroup")) {
      JSONObject object = new JSONObject();
      GroupOper groupOper = new GroupOper(this.db);
      groupOper.removeOperaterFromGroup(rLine.getInt("operID"));

      if (groupOper.isError()) {
        object.put("ERROR", groupOper.getErrorMSG());
      }
      send_object(object);
      return;
    }

    if(rLine.getString("action").equals("addOperToGroup")){
      JSONObject object = new JSONObject();

      GroupOper groupOper = new GroupOper(this.db);
      groupOper.addOperToGroup(rLine.getInt("groupID"), rLine.getInt("operID"));
      if(groupOper.isError())
        object.put("ERROR", groupOper.getErrorMSG());

      send_object(object);
      return;
    }

    if(rLine.getString("action").equals("changeMAC_IPTV")){
      JSONObject object = new JSONObject();
      StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db, getOperName());
      stalkerRestAPI2.changeMac(594, "B0:EE:7B:42:87:8F" );
      if(stalkerRestAPI2.isError()){
        object.put("ERROR", stalkerRestAPI2.getErrorMSG());
      }
      send_object(object);
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


  private String get_paket_naziv(String nazivPaketa, int dtv_id) {
    String naziv = "";
    try {
      if (nazivPaketa.equals("FIXPaketi")) {
        String query = String.format("SELECT * FROM FIX_paketi WHERE id=?", nazivPaketa);
        ps = db.conn.prepareStatement(query);
        ps.setInt(1, dtv_id);
      } else {
        ps = db.conn.prepareStatement("SELECT * FROM " + nazivPaketa + " WHERE id=?");
        ps.setInt(1, dtv_id);
      }
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        if (nazivPaketa.equals("IPTV_Paketi")) {
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
    JSONObject object = new JSONObject();
    int userId = mes.getInt("userId");

    UserFunc userFunc = new UserFunc(db, getOperName());
    userFunc.deleteUser(userId);

    if (userFunc.isError()) {
      object.put("ERROR", userFunc.getErrorMSG());
    }

    send_object(jObj);
    return;
  }

  private void update_user(JSONObject jObju) {
    jObj = new JSONObject();
    int userID = jObju.getInt("userID");
    query = "UPDATE users SET ime = ?, datumrodjenja = ?, adresa = ?, mesto = ?,"
        + " postbr = ?, telFiksni = ?, telMobilni = ?,  brlk = ?,  JMBG =?, adresaRacuna = ?, "
        + "mestoRacuna = ?, jAdresaBroj=?, jAdresa = ?, jMesto=?, jBroj=?, "
        + "komentar = ?, firma=?, nazivFirme=?, kontaktOsoba=?, kontaktOsobaTel=?, kodBanke=?, tekuciRacun=?, PIB=?, maticniBroj = ?, "
        +
        "fax=?, adresaFirme=?, mestoFirme=?, email=?, prekoracenje=? WHERE id = ? ";

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

      //FIRMA
      ps.setBoolean(17, jObju.getBoolean("firma"));
      ps.setString(18, jObju.getString("nazivFirme"));
      ps.setString(19, jObju.getString("kontaktOsoba"));
      ps.setString(20, jObju.getString("kontaktOsobaTel"));
      ps.setString(21, jObju.getString("kodBanke"));
      ps.setString(22, jObju.getString("tekuciRacun"));
      ps.setString(23, jObju.getString("PIB"));
      ps.setString(24, jObju.getString("maticniBroj"));
      ps.setString(25, jObju.getString("fax"));
      ps.setString(26, jObju.getString("adresaFirme"));
      ps.setString(27, jObju.getString("mestoFirme"));
      ps.setString(28, jObju.getString("email"));
      ps.setInt(29, jObju.getInt("prekoracenjeMeseci"));
      ps.setInt(30, userID);
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
    this.setOperName(username);

    try {
      //check if connection is timed-out
      if (!db.conn.isValid(1)) {
        db = new database();
      }
      ps = db.conn.prepareStatement(
          "SELECT id, username,password, aktivan FROM operateri WHERE username=? AND password=?");
      ps.setString(1, username);
      ps.setString(2, password);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        userName = rs.getString("username");
        passWord = rs.getString("password");
        aktivan = rs.getBoolean("aktivan");
        setOperID(rs.getInt("id"));

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
      LOGGER.warn(String.format("Login Error, User: %s  Pass: "
          + "%s Client: ", username, password, client
          .getRemoteSocketAddress()));
      client_authenticated = false;
      jObj = new JSONObject();
      jObj.put("Message", "LOGIN_FAILED");
      send_object(jObj);

      //client.close();
    }

    return client_authenticated;
  }

  public void send_object(JSONObject obj) {
    if (DEBUG > 0) {
      //  LOGGER.info("Sending Object: " + obj.toString());
    }
    if (obj.has("ERROR")) {
      LOGGER.error(String.format("GRESKA: %s", obj.getString("ERROR")));
    }

    if (client.isClosed()) {
      LOGGER.info("CLIENT DISCONNECTED!!");
      try {
        //client.close();
        Bfr.close();
        Bfw.close();
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

    try {
      if (keepAlive == false) {
        Bfw.close();
        Bfr.close();
        //client.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (client.isClosed()) {
      System.out.println("DISCONNECTED");
      try {
        Bfr.close();
        Bfw.close();
        get_socket().close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


  }

}
