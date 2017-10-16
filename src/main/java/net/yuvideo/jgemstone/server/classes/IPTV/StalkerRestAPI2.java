package net.yuvideo.jgemstone.server.classes.IPTV;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Created by PsyhoZOOM@gmail.com on 7/21/17.
 */
public class StalkerRestAPI2 {
    Client apiClient;
    WebResource webResource;
    ClientResponse clientResponse;
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
            ps.setString(1, "IPTV%");
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    System.out.println(rs.getString("settings"));
                    if (rs.getString("settings").equals("IPTV_API2_URL"))
                        this.url = rs.getString("value");
                    if (rs.getString("settings").equals("IPTV_API2_USERNAME"))
                        this.username = rs.getString("value");
                    if (rs.getString("settings").equals("IPTV_API2_PASSWORD"))
                        this.pass = rs.getString("value");
                }
            }
            System.out.println(ps.toString());
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        AuthString = username + ":" + pass;
        // AuthStringENC = new BASE64Encoder().encode(AuthString.getBytes());
	AuthStringENC = Base64.getEncoder().encodeToString(AuthString.getBytes());
        apiClient = Client.create();


    }

    public JSONObject getPakets_ALL() {
        webResource = apiClient.resource(url);
        clientResponse = webResource
                .path("tariffs")
                .header("Authorization", "Basic " + AuthStringENC)
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        String tariffs = clientResponse.getEntity(String.class);

        JSONObject tarifobj = new JSONObject(tariffs);

        JSONArray tarrifArr = tarifobj.getJSONArray("results");

        JSONObject jObj = new JSONObject();

        for (int i = 0; i < tarrifArr.length(); i++) {
            jObj.put(String.valueOf(i), tarrifArr.get(i));
        }

        return jObj;
    }

    public JSONObject getUsersData(int accountID) {
        webResource = apiClient.resource(url + "accounts");
        clientResponse = webResource.accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .get(ClientResponse.class);
        String users = clientResponse.getEntity(String.class);
        System.out.println(users);
        JSONObject userObj = new JSONObject(users);

        return userObj;
    }

    public JSONObject saveUSER(JSONObject rLine) {
        webResource = apiClient.resource(url);
        webResource.path("accounts");

        String request;
        request = "&login=" + rLine.getString("login");
        request += "&full_name=" + rLine.getString("full_name");
        request += "&account_number=" + rLine.getInt("userID");
        request += "&end_date=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString();
        request += "&tariff_plan=" + rLine.getString("tariff_plan");
        request += "&password=" + rLine.getString("password");
        request += "&stb_mac=" + rLine.getString("STB_MAC");
        String.format(request += "&comment=" + String.format("Korisnik %s, account broj: %d , login: %s, password: %s",
                rLine.getString("full_name"), rLine.getInt("userID"), rLine.getString("login"),
                rLine.getString("password")));
        request += "&status=0";
        clientResponse = webResource
                .path("accounts")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .post(ClientResponse.class, request);

        String resp = clientResponse.getEntity(String.class);

        System.out.println("RESPONSE: " + resp);
        JSONObject respObj = new JSONObject();
        respObj.put("Message", resp);
        return respObj;
    }

    public boolean checkExternalID(int external_id) {
        Boolean exist = false;
        webResource = apiClient.resource(url + "tariffs");
        clientResponse = webResource.accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .get(ClientResponse.class);
        String tarrifs = clientResponse.getEntity(String.class);
        JSONObject tarrifobj = new JSONObject(tarrifs);
        JSONArray tarrifArr = tarrifobj.getJSONArray("results");

        //check if external_id exist
        for (int i = 0; i < tarrifArr.length(); i++) {
            System.out.println(tarrifArr.get(i));
            JSONObject tf = (JSONObject) tarrifArr.get(i);
            if (tf.getInt("external_id") == external_id)
                exist = true;
        }

        return exist;

    }

    public JSONObject setEndDate(String STB_MAC, String endDate) {
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("accounts").path(STB_MAC)
                .queryParam("end_date", endDate)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .put(ClientResponse.class, "end_date=" + endDate);
        String aa = clientResponse.getEntity(String.class);

        JSONObject accInfo = new JSONObject();

        return accInfo;


    }

    public JSONObject getAccInfo(String stb_mac) {
        System.out.println(stb_mac);
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("stb").path(stb_mac)
                .accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .get(ClientResponse.class);
        System.out.println(clientResponse.toString());
        String aa = clientResponse.getEntity(String.class);
        System.out.println(aa.toString());

        JSONObject accInfo = new JSONObject(aa);
        JSONArray accInfoArr = accInfo.getJSONArray("results");
        accInfo = new JSONObject(accInfoArr);



        return accInfo;
    }

    public void deleteAccount(String stb_mac) {
        //ako je mac null onda izlaz iz funkcije
        if (stb_mac == null) return;

        System.out.println("MAC za brisanje: " + stb_mac);
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("accounts").path(stb_mac)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .delete(ClientResponse.class);

        System.out.println(clientResponse.toString());
        String aa = clientResponse.getEntity(String.class);
        System.out.println("RESPONSE: " + aa);
    }


    public void changeMac(int acc, String stb_mac) {
        webResource = apiClient.resource(url);
        clientResponse = webResource
                .path("accounts")
                .path("4")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .put(ClientResponse.class);

        String aa = clientResponse.getEntity(String.class);
        System.out.println("CHANGE MAC: " + aa);
    }

    public Boolean checkUser(String STB_MAC) {
        boolean userExists = true;
        webResource = apiClient.resource(url);
        clientResponse = webResource.
                path("accounts")
                .path(String.valueOf(STB_MAC))
                .header("Authorization", "Basic " + AuthStringENC)
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        String resp = clientResponse.getEntity(String.class);
        JSONObject jsonObject = new JSONObject(resp);

        if (jsonObject.get("results") == JSONObject.NULL) {
            System.out.println("NEMA USERS");
            userExists = false;
        }

        System.out.println(userExists);
        return userExists;
    }

    public void activateStatus(boolean status, String STB_MAC) {
        webResource = apiClient.resource(url);
        int statusInt = 0;
        if (status)
            statusInt = 1;

        clientResponse = webResource.path("accounts")
                .path(STB_MAC)
                .header("Authorization", "Basic " + AuthStringENC)
                .accept(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, "status=" + statusInt);
        System.out.println(webResource.toString());

        String aa = clientResponse.getEntity(String.class);
        System.out.println("AKTIVATED: " + aa);
    }

    public String get_end_date(String STB_MAC) {
        System.out.println("MAC ADRESA: " + STB_MAC);
        String end_date = "0000-00-00";
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("accounts")
                .path(STB_MAC)
                .header("Authorization", "Basic " + AuthStringENC)
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        String aa = clientResponse.getEntity(String.class);

        JSONObject jsonObject = new JSONObject(aa);
        jsonObject = jsonObject.getJSONArray("results").getJSONObject(0);

        if (jsonObject.has("end_date"))
            end_date = jsonObject.getString("end_date");

        return end_date;
    }
}
