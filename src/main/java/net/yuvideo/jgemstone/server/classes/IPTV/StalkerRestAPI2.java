package net.yuvideo.jgemstone.server.classes.IPTV;

import net.yuvideo.jgemstone.server.classes.database;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	public boolean isHostAlive = false;
	public String hostMessage;

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
					if (rs.getString("settings").equals("IPTV_API2_URL")) {
						this.url = rs.getString("value");
					}
					if (rs.getString("settings").equals("IPTV_API2_USERNAME")) {
						this.username = rs.getString("value");
					}
					if (rs.getString("settings").equals("IPTV_API2_PASSWORD")) {
						this.pass = rs.getString("value");
					}
				}
			}
			System.out.println(ps.toString());
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		AuthString = username + ":" + pass;
		AuthStringENC = Base64.getEncoder().encodeToString(AuthString.getBytes());

		hhtpAuthFeature = HttpAuthenticationFeature.basic(username, pass);
		clientConfig = new ClientConfig();
		clientConfig.register(hhtpAuthFeature);
		clientConfig.register(JacksonJsonProvider.class);
		apiClient = ClientBuilder.newClient(clientConfig);
		target = apiClient.target(url);




		checkIsHostAlive();
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
		checkIsHostAlive();
		if (!isHostAlive) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ERROR", "HOST_IS_DEAD - IPTV SERVER NIJE DOSTUPAN. \n SERVIS NIJE MOGUCE NAPRAVITI. \n " +
					"POKUSAJTE PONOVO KASNIJE ILI SE OBRATITE ADMINISTRATORU!");
			return jsonObject;

		}
		JSONObject jsonObject = new JSONObject();
		target = target.path("accounts");

		JSONObject jobj = new JSONObject();
		jobj
				.put("login", rLine.getString("login"))
				.put("full_name", rLine.getString("full_name"))
				.put("account_number", rLine.getInt("userID"))
				.put("end_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString())
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

		response = target.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(postStr, MediaType.APPLICATION_JSON), Response.class);

		if (response.getStatus() != 200) {
			jsonObject.put("ERROR", response.getStatusInfo());
		} else {
			jsonObject.put("MESSAGE", response.readEntity(String.class));
			System.out.println(jsonObject.get("MESSAGE"));
		}

		return jsonObject;
	}

	public JSONObject setEndDate(String STB_MAC, String endDate) {
		JSONObject jsonObject = new JSONObject();
		target = target.path("accounts")
				.path(STB_MAC);
		JSONObject jendDate = new JSONObject();
		jendDate.put("end_date", endDate);

		response = target.request(MediaType.APPLICATION_JSON).put(Entity.json("end_date=" + endDate));

		if (response.getStatus() != 200) {
			jsonObject.put("ERROR", response.getStatusInfo());
		} else {
			jsonObject = new JSONObject(response.readEntity(String.class));
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

	public JSONObject changeMac(int acc, String stb_mac) {
		JSONObject jsonObject = new JSONObject();
		if (acc <= 0 || stb_mac == null) {
			jsonObject.put("ERROR", "ACCOUNT_OR_MAC_EMPTY");
			return jsonObject;
		}
		target = target.path("accounts");
		target = target.path(String.valueOf(acc));

		response = target.request(MediaType.APPLICATION_JSON).put(Entity.entity("stb_mac=" + stb_mac, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {
			jsonObject.put("ERROR", response.getStatusInfo());
		} else {
			jsonObject = new JSONObject(response.readEntity(String.class));
			System.out.println("IPTV_ACCOUNT_CHANGE_MAC:  " + jsonObject.toString());
		}

		return jsonObject;
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

			if (jsonObject.has("error")) {
				userExist = false;
			} else {
				userExist = true;
			}
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

		response = target.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity("status=" + statusInt, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {
			jsonObject = new JSONObject();
			jsonObject.put("ERROR", response.getStatusInfo());
		} else {
			jsonObject = new JSONObject(response.readEntity(String.class));

			System.out.println(jsonObject);
		}

		System.out.println("AKTIVATE_STATUS: " + jsonObject);

	}

	public String get_end_date(String STB_MAC) {
		JSONObject jsonObject = new JSONObject();
		String end_date = "0000-00-00";
		target = target.path("accounts");
		target = target.path(STB_MAC);
		builder = target.request(MediaType.APPLICATION_JSON);
		response = builder.get();

		if (response.getStatus() != 200) {
			jsonObject = new JSONObject();
			jsonObject.put("ERROR", response.getStatusInfo());
		} else {
			jsonObject = new JSONObject(response.readEntity(String.class));

			JSONArray jsonarr = jsonObject.getJSONArray("results");
			for (int i = 0; i < jsonarr.length(); i++) {
				System.out.println(jsonarr.get(i));
				jsonObject = jsonarr.getJSONObject(i);
				if (jsonObject.has("end_date")) {
					end_date = jsonObject.getString("end_date");
				}
			}

		}

		return end_date;
	}

	private void checkIsHostAlive() {
		try {
			URL urlSite = new URL(this.url);
			HttpURLConnection connection = (HttpURLConnection) urlSite.openConnection();
			connection.setRequestMethod("GET");
			try {
				connection.connect();

			} catch (ConnectException e) {
				e.getMessage();
			} finally {
				this.isHostAlive = false;
			}



			int code= connection.getResponseCode();
			if(code==200){
				this.isHostAlive = true;
				
			}
			
		} catch (MalformedURLException ex) {
			Logger.getLogger(StalkerRestAPI2.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			this.hostMessage=ex.getMessage();
			Logger.getLogger(StalkerRestAPI2.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}

}
