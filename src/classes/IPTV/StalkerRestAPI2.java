package classes.IPTV;

import classes.database;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
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
        System.out.println("APIV2: " + username + " pass: " + pass + " URL: " + url);
        webResource = apiClient.resource(url + "tariffs");
        clientResponse = webResource.accept("application/json")
                .header("Authorization", "Basic " + AuthStringENC)
                .get(ClientResponse.class);
        String tariffs = clientResponse.getEntity(String.class);
        System.out.println(tariffs);

        JSONObject tarifobj = new JSONObject(tariffs);


        return tarifobj;


    }
}
