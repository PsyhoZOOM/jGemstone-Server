package classes.IPTV;

import classes.database;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        AuthStringENC = new BASE64Encoder().encode(AuthString.getBytes());
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

    public JSONObject setEndDate(JSONObject rLine, String endDate) {
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("stb").path("1")
                .queryParam("end_date", endDate)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .put(ClientResponse.class, "end_date=" + endDate);
        String aa = clientResponse.getEntity(String.class);
        System.out.println("ACC INFO:" + aa);
        System.out.println(webResource.toString());

        JSONObject accInfo = new JSONObject();

        getAccInfo(rLine.getString("STB_MAC"), endDate);
        return accInfo;


    }

    public JSONObject getAccInfo(String stb_mac, String endDate) {
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("accounts").path(stb_mac)
                .accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .get(ClientResponse.class);
        String aa = clientResponse.getEntity(String.class);
        System.out.println("ACC INFO:" + aa);
        System.out.println(webResource.toString());

        JSONObject accInfo = new JSONObject();
        return accInfo;
    }

    public void deleteAccount(String stb_mac) {
        webResource = apiClient.resource(url);
        clientResponse = webResource.path("stb").path(stb_mac)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + AuthStringENC)
                .delete(ClientResponse.class);

        String aa = clientResponse.getEntity(String.class);
        System.out.println("DELETE ACCOUNT: " + aa);
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
        System.out.println(resp);
        JSONObject jsonObject = new JSONObject(resp);

        if (jsonObject.get("results") == JSONObject.NULL) {
            System.out.println("NEMA USERS");
            userExists = false;
        }

        System.out.println(userExists);
        return userExists;
    }
}
