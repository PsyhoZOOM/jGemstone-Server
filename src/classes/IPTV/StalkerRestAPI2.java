package classes.IPTV;

import classes.database;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        webResource = apiClient.resource(url + "tariffs");
        clientResponse = webResource.accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
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
        webResource = apiClient.resource(url + "accounts");
        String request;

        request = "&login=" + rLine.getString("login");
        request += "&full_name=" + rLine.getString("full_name");
        request += "&account_number=" + rLine.getInt("account_number");
        request += "&tarrif_plan=" + rLine.getString("tarrif_plan");
        request += "&password=" + rLine.getString("password");
        request += "&status=1";

        clientResponse = webResource.accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .post(ClientResponse.class, request);


        JSONObject respObj = new JSONObject();
        respObj.put("Message", clientResponse.getEntity(String.class));
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

}
