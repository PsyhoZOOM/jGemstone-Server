package net.yuvideo.jgemstone.server.classes.IPTV;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 7/21/17.
 */
public class StalkerRestAPI2 {

  private boolean isHostAlive;
  public String hostMessage;
  Client apiClient;
  ClientConfig clientConfig;
  ClientResponse response;
  WebResource webResource;
  private database db;
  private String username;
  private String pass;
  private String url;
  private String AuthString;
  private String AuthStringENC;

  public StalkerRestAPI2(database db) {
    this.db = db;
    setupAPI();
  }

  private void setupAPI() {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT settings, value FROM `settings` WHERE `settings` LIKE ?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, "MINISTRA%");
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          if (rs.getString("settings").equals("MINISTRA_API_URL")) {
            this.url = rs.getString("value");
          }
          if (rs.getString("settings").equals("MINISTRA_API_USER")) {
            this.username = rs.getString("value");
          }
          if (rs.getString("settings").equals("MINISTRA_API_PASS")) {
            this.pass = rs.getString("value");
          }
        }
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    AuthString = username + ":" + pass;
    AuthStringENC = Base64.getEncoder().encodeToString(AuthString.getBytes());

    clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

    apiClient = Client.create(clientConfig);
    try {

      webResource = apiClient.resource(url).path("itv");

      response = webResource.accept("application/json")
          .header("Authorization","Basic "+ AuthStringENC)
          .get(ClientResponse.class);
    } catch (NullPointerException e) {
      e.printStackTrace();
      isHostAlive = false;
      hostMessage = e.getMessage();
    } finally {
      if (webResource == null) {
        isHostAlive = false;
        return;

      } else {
        System.out.println(response.getStatus());
      }
    }
    if(response.getStatus() != 200 ){
      isHostAlive  = false;
      hostMessage = String.valueOf("ERROR: " + response.getStatus());
      return;
    }else {
      isHostAlive = true;
    }


  }

  public JSONObject getPakets_ALL() {
    JSONObject jsonObject = null;
    JSONArray jsonArray;
    String a = null;

    setupAPI();

    webResource = apiClient.resource(url).path("tariffs");
    response = webResource.accept("application/json")
        .header("Authorization", "Basic " + this.AuthStringENC)
        .get(ClientResponse.class);



    if (response.getStatus() != 200) {
      jsonObject = new JSONObject();
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      try {
        a = response.getEntity(String.class);
        hostMessage = a;

      }catch (JSONException e){
        e.printStackTrace();
      }
      jsonObject = new JSONObject(a);

    }

    JSONObject jsonTariffs =new JSONObject();

    for(String key : jsonObject.keySet()){
      if(key.equals("results")){
        JSONArray obj = jsonObject.getJSONArray("results");
        for(int i =0; i<obj.length();i++){
          jsonTariffs.put(String.valueOf(i), obj.getJSONObject(i));
        }
      }

    }
    return jsonTariffs;
  }


  public JSONObject saveUSER(JSONObject rLine) {
    setupAPI();
    if (!isHostAlive) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("ERROR",
          "HOST_IS_DEAD - IPTV SERVER NIJE DOSTUPAN. \n SERVIS NIJE MOGUCE NAPRAVITI. \n " +
              "POKUSAJTE PONOVO KASNIJE ILI SE OBRATITE ADMINISTRATORU!");
      return jsonObject;

    }
    JSONObject jsonObject = new JSONObject();
    webResource = apiClient.resource(url).path("accounts");

    JSONObject jobj = new JSONObject();
    jobj
        .put("login", rLine.getString("login"))
        .put("full_name", rLine.getString("full_name"))
        .put("account_number", rLine.getInt("userID"))
        .put("end_date",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .toString())
        .put("tarrif_plan", rLine.getString("tariff_plan"))
        .put("password", rLine.getString("password"))
        .put("stb_mac", rLine.getString("STB_MAC"))
        .put("status", 0)
        .put("comment", String.format("Korisnik %s, account broj: %d, login: %s, password: %s",
            rLine.getString("full_name"), rLine.getInt("userID"), rLine.getString("login"),
            rLine.getString("password")));

    String postStr = null;

    Map<String, Object> stringObjectMap = jobj.toMap();

    for (String key : stringObjectMap.keySet()) {
      postStr += key + "=" + stringObjectMap.get(key) + "&";
    }


    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+this.AuthStringENC)
        .post(ClientResponse.class, postStr);


    if (response.getStatus() != 200) {
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject.put("MESSAGE", response.getEntity(String.class));
    }


    return jsonObject;
  }

  public JSONObject setEndDate(String STB_MAC, String endDate) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();
    JSONObject jendDate = new JSONObject();


    webResource = apiClient.resource(url).path("accounts").path(STB_MAC);
    response = webResource.accept(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .put(ClientResponse.class, "&end_date="+endDate);

    if (response.getStatus() != 200) {
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));
    }

    return jsonObject;
  }

  public JSONObject getAccInfo(String stb_mac) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();

    webResource = apiClient.resource(url).path("accounts").path(stb_mac);
    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .get(ClientResponse.class);
    if (response.getStatus() != 200) {
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));
    }

    return jsonObject;
  }

  public JSONObject deleteAccount(String stb_mac) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();
    if (stb_mac == null) {
      jsonObject.put("ERROR", "MISSING_MAC");
      return jsonObject;
    }


    webResource = apiClient.resource(url).path("accounts").path(stb_mac);
    webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .delete();


    return jsonObject;
  }

  public JSONObject changeMac(int acc, String stb_mac) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();
    if (acc <= 0 || stb_mac == null) {
      jsonObject.put("ERROR", "ACCOUNT_OR_MAC_EMPTY");
      return jsonObject;
    }

    webResource = apiClient.resource(url).path("accounts").path(String.valueOf(acc));
    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .put(ClientResponse.class, "stb_mac="+stb_mac);

    if (response.getStatus() != 200) {
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));
    }

    return jsonObject;
  }

  public boolean checkUser(String stb_mac) {
    boolean userExist = true;
    setupAPI();
    JSONObject jsonObject = new JSONObject();
    webResource = apiClient.resource(url).path("accounts").path(stb_mac);
    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .get(ClientResponse.class);

    if (response.getStatus() != 200) {
      userExist = true;
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));

      userExist = !jsonObject.has("error");
    }

    return userExist;
  }

  public JSONObject activateStatus(boolean status, String stb_mac) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();
    if(!isHostAlive){
     jsonObject.put("ERROR", "GRESKA: "+hostMessage);
     return jsonObject;
    }
    int statusInt = 0;
    if (status) {
      statusInt = 1;
    }

    webResource = apiClient.resource(url).path("accounts").path(stb_mac);
    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+this.AuthStringENC)
        .put(ClientResponse.class, "&status="+statusInt);

    if (response.getStatus() != 200) {
      jsonObject = new JSONObject();
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));
    }

    return  jsonObject;

  }

  public String get_end_date(String STB_MAC) {
    JSONObject jsonObject = new JSONObject();
    setupAPI();
    String end_date = "0000-00-00";
    webResource = apiClient.resource(url).path("accounts").path(STB_MAC);
    response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic "+AuthStringENC)
        .get(ClientResponse.class);

    if (response.getStatus() != 200) {
      jsonObject = new JSONObject();
      jsonObject.put("ERROR", response.getStatusInfo());
    } else {
      jsonObject = new JSONObject(response.getEntity(String.class));

      JSONArray jsonarr = jsonObject.getJSONArray("results");
      for (int i = 0; i < jsonarr.length(); i++) {
        jsonObject = jsonarr.getJSONObject(i);
        if (jsonObject.has("end_date")) {
          end_date = jsonObject.getString("end_date");
        }
      }

    }

    return end_date;
  }


  public JSONObject checkHost() {
    JSONObject pakets_all = getPakets_ALL();
    JSONObject object = new JSONObject();
    if (pakets_all.has("ERROR")) {
      object.put("ERROR", pakets_all.getString("ERROR"));
    }
    return object;

  }


  public boolean isHostAlive() {
    return isHostAlive;
  }

  public void setHostAlive(boolean hostAlive) {
    isHostAlive = hostAlive;
  }

  public String getHostMessage() {
    return hostMessage;
  }

  public void setHostMessage(String hostMessage) {
    this.hostMessage = hostMessage;
  }
}
