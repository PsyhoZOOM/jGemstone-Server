package net.yuvideo.jgemstone.server.classes.IPTV;

import net.yuvideo.jgemstone.server.classes.database;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    ClientConfig clientConfig;
    Response response;
    WebTarget target;
    Invocation.Builder builder;
    HttpAuthenticationFeature hhtpAuthFeature;

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

        //  apiClient = Client.create();

        hhtpAuthFeature = HttpAuthenticationFeature.basic(username, pass);
        clientConfig = new ClientConfig();
        clientConfig.register(hhtpAuthFeature);
        apiClient = ClientBuilder.newClient(clientConfig);
        target = apiClient.target(url);





    }

    public JSONObject getPakets_ALL() {
        JSONObject jsonObject = null;
        JSONArray jsonArray;

        target = target.path("tariffs");
        builder = target.request(MediaType.APPLICATION_JSON);

        response = builder.get();

        if (response.getStatus() != 200) {
            jsonObject = new JSONObject();
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            String a = response.readEntity(String.class);
            jsonArray = new JSONObject(a).getJSONArray("results");
            jsonObject = new JSONObject();

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject.put(String.valueOf(i), jsonArray.get(i));
            }
        }


        return jsonObject;
    }

    public JSONObject getUsersData(int accounID) {
        JSONObject jsonObject = new JSONObject();
        target = target.path("accounts");
        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.get();

        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            JSONArray jsonArr = new JSONObject(response.readEntity(String.class)).getJSONArray("results");
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonObject.put(String.valueOf(i), jsonArr.get(i));
            }

        }
        return jsonObject;
    }

    public JSONObject saveUSER(JSONObject rLine) {
        JSONObject jsonObject = new JSONObject();
        target = target.path("accounts");

        JSONObject jobj = new JSONObject();
        jobj
                .put("login", rLine.getString("login"))
                .put("full_name", rLine.getString("full_name"))
                .put("account_number", "userID")
                .put("end_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString())
                .put("tarrif_plan", rLine.getString("tariff_plan"))
                .put("password", rLine.getString("password"))
                .put("stb_mac", rLine.getString("STB_MAC"))
                .put("status", 0)
                .put("comment", String.format("Korisnik %s, account broj: %d, login: %s, password: %s",
                        rLine.getString("full_name"), rLine.getInt("userID"), rLine.getString("login"),
                        rLine.getString("password")));

        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.put(Entity.json(jobj));

        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject.put("MESSAGE", response.readEntity(String.class));
            System.out.println(response.readEntity(String.class));
        }
        return jsonObject;
    }


    public JSONObject setEndDate(String STB_MAC, String endDate) {
        JSONObject jsonObject = new JSONObject();
        target = target.path("accounts")
                .path(STB_MAC);
        JSONObject jendDate = new JSONObject();
        jendDate.put("end_date", endDate);

        response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jendDate));


        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
            System.out.println(response.readEntity(String.class));
        }


        return jsonObject;
    }


    public JSONObject getAccInfo(String stb_mac) {
        JSONObject jsonObject = new JSONObject();
        target = target.path("accounts");
        target = target.path(stb_mac);

        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.get();

        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
        }

        return jsonObject;

    }

    public JSONObject deleteAccount(String stb_mac) {
        JSONObject jsonObject = new JSONObject();
        if (stb_mac == null) {
            jsonObject.put("ERROR", "MISSING_MAC");
            return jsonObject;
        }

        target = target.path("accounts")
                .path(stb_mac);
        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.delete();

        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
        }

        return jsonObject;
    }

    public void changeMac(String acc, String stb_mac) {
        JSONObject jsonObject = new JSONObject();
        if (acc == null || stb_mac == null) {
            jsonObject.put("ERROR", "ACCOUNT_OR_MAC_EMPTY");
            return;
        }
        target = target.path(acc);
        target = target.path(stb_mac);
        jsonObject = new JSONObject();
        jsonObject.put("stb_mac", stb_mac);

        response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonObject));

        if (response.getStatus() != 200) {
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
            System.out.println("IPTV_ACCOUNT_DELETE:  " + jsonObject.get("results"));
        }


    }

    public boolean checkUser(String stb_mac) {
        boolean userExist = true;
        JSONObject jsonObject = new JSONObject();
        target = target.path("accounts");
        target = target.path(stb_mac);

        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.get();

        if (response.getStatus() != 200) {
            userExist = true;
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
            System.out.println(jsonObject);
            userExist = false;
        }

        return userExist;
    }

    public void activateStatus(boolean status, String stb_mac) {
        JSONObject jsonObject = new JSONObject();
        int statusInt = 0;
        if (status) {
            statusInt = 1;
        }
        target = target.path("accounts");
        target = target.path(stb_mac);

        jsonObject.put("status", statusInt);

        response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonObject));

        if (response.getStatus() != 200) {
            jsonObject = new JSONObject();
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));

        }

        System.out.println("AKTIVATE_STATUS: " + jsonObject);

    }

    public String get_end_date(String STB_MAC) {
        JSONObject jsonObject = new JSONObject();
        String end_date = "0000-00-00";
        target = target.path("accounts");
        builder = target.request(MediaType.APPLICATION_JSON);
        response = builder.get();

        if (response.getStatus() != 200) {
            jsonObject = new JSONObject();
            jsonObject.put("ERROR", response.getStatusInfo());
        } else {
            jsonObject = new JSONObject(response.readEntity(String.class));
            end_date = jsonObject.getString("end_date");

        }

        return end_date;

    }


}
