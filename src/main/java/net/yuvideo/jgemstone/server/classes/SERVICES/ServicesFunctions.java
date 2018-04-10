package net.yuvideo.jgemstone.server.classes.SERVICES;

import net.yuvideo.jgemstone.server.classes.FIX.FIXFunctions;
import net.yuvideo.jgemstone.server.classes.FaktureFunct;
import net.yuvideo.jgemstone.server.classes.INTERNET.NETFunctions;
import net.yuvideo.jgemstone.server.classes.IPTV.StalkerRestAPI2;
import net.yuvideo.jgemstone.server.classes.database;
import net.yuvideo.jgemstone.server.classes.valueToPercent;
import org.json.JSONObject;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Created by zoom on 2/27/17.
 */
public class ServicesFunctions {

	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
	private static DateTimeFormatter dtfNormalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static DateTimeFormatter dtfRadCheck = DateTimeFormatter.ofPattern("dd MMM yyyy");
	private static DateTimeFormatter dtfRadReply = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static DateTimeFormatter dtfMesecZaduzenja = DateTimeFormatter.ofPattern("yyyy-MM");
	private static DateTimeFormatter dtfIPTV = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static DecimalFormat df = new DecimalFormat("#.##");

    private boolean haveError = false;
    private String errorMessage = "";

	public static void addServiceLinked(JSONObject rLine, String opername, database db) {
		ResultSet rs;
		PreparedStatement ps;
		String query = "INSERT INTO servicesUser "
                + "(id_service, box_id, nazivPaketa, UserName, idDTVCard, DTVPaket, userID, obracun, produzenje, operName, linkedService, paketType, PDV, opis) "
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = db.conn.prepareStatement(query);
			if (rLine.has("DTV_service_ID")) {
				ps.setInt(1, rLine.getInt("DTV_service_ID"));
			}
			if (rLine.has("NET_service_ID")) {
				ps.setInt(1, rLine.getInt("NET_service_ID"));
			}
			if (rLine.has("FIKSNA_service_ID")) {
				ps.setInt(1, rLine.getInt("FIKSNA_service_ID"));
			}
			if (rLine.has("IPTV_service_ID")) {
				ps.setInt(1, rLine.getInt("IPTV_service_ID"));
			}
			ps.setInt(2, rLine.getInt("idPaket"));
			if (rLine.has("nazivPaketaDTV")) {
				ps.setString(3, rLine.getString("nazivPaketaDTV"));
			}
			if (rLine.has("nazivPaketaNET")) {
				ps.setString(3, rLine.getString("nazivPaketaNET"));
			}
			if (rLine.has("nazivPaketaFIKSNA")) {
				ps.setString(3, "nazivPaketFIKSNA");
			}
			if (rLine.has("nazivPaketaIPTV")) {
				ps.setString(3, "nazivPaketaIPTV");
			}

			ps.setString(4, rLine.getString("userName"));
			if (rLine.has("DTVKartica")) {
				ps.setString(5, String.valueOf(rLine.getInt("DTVKartica")));
			} else {
				ps.setNull(5, Types.VARCHAR);
			}
			if (rLine.has("DTVPaket")) {
				ps.setInt(6, rLine.getInt("DTVPaket"));
			} else {
				ps.setNull(6, Types.VARCHAR);
			}
			ps.setInt(7, rLine.getInt("userID"));
			ps.setBoolean(8, false);
			ps.setInt(9, rLine.getInt("produzenje"));
			ps.setString(10, opername);
			ps.setBoolean(11, true);
			ps.setString(12, "LINKED");
			ps.setDouble(13, rLine.getDouble("pdv"));
            ps.setString(14, rLine.getString("komentar"));

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addServiceDTVLinked(JSONObject rLine, String opername, int BOX_Service_ID, database db) {
		PreparedStatement ps;
        String query = "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added,  idDTVCard, DTVPaket,  userID, produzenje, operName, linkedService, paketType, endDate, PDV, opis) "
				+ "VALUES"
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("DTV_service_ID"));
			ps.setInt(2, BOX_Service_ID);
			ps.setString(3, rLine.getString("nazivPaketaDTV"));
			ps.setString(4, dtf.format(LocalDateTime.now()));
			ps.setString(5, String.valueOf(rLine.getInt("DTVKartica")));
			ps.setInt(6, rLine.getInt("DTVPaket"));
			ps.setInt(7, rLine.getInt("userID"));
			ps.setInt(8, rLine.getInt("produzenje"));
			ps.setString(9, opername);
			ps.setBoolean(10, true);
			ps.setString(11, "LINKED_DTV");
			ps.setString(12, "2000-01-01");
			ps.setDouble(13, rLine.getDouble("pdv"));
            ps.setString(14, rLine.getString("komentar"));
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void addServiceNETLinked(JSONObject rLine, String opername, int BOX_Service_ID, database db) {
		PreparedStatement ps;
        String query = "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added, userID, produzenje, operName, UserName, GroupName, linkedService, paketType, endDate, PDV, opis ) "
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("NET_service_ID"));
			ps.setInt(2, BOX_Service_ID);
			ps.setString(3, rLine.getString("nazivPaketaNET"));
			ps.setString(4, dtf.format(LocalDateTime.now()));
			ps.setInt(5, rLine.getInt("userID"));
			ps.setInt(6, rLine.getInt("produzenje"));
			ps.setString(7, opername);
			ps.setString(8, rLine.getString("userName"));
			ps.setString(9, rLine.getString("groupName"));
			ps.setBoolean(10, true);
			ps.setString(11, "LINKED_NET");
			ps.setString(12, "2000-01-01");
			ps.setDouble(13, rLine.getDouble("pdv"));
            ps.setString(14, rLine.getString("komentar"));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void addServiceFIXLinked(JSONObject rLine, String opername, int box_service_id, database db) {
		PreparedStatement ps;
		String query = "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added, userID, operName,"
                + " FIKSNA_TEL, FIKSNA_TEL_PAKET_ID, linkedService, paketType, PDV, opis)"
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("FIKSNA_service_ID"));
			ps.setInt(2, box_service_id);
			ps.setString(3, rLine.getString("nazivPaketaFIKSNA"));
			ps.setString(4, dtf.format(LocalDateTime.now()));
			ps.setInt(5, rLine.getInt("userID"));
			ps.setString(6, opername);
			ps.setString(7, rLine.getString("FIX_TEL"));
			ps.setInt(8, rLine.getInt("FIKSNA_PAKET_ID"));
			ps.setBoolean(9, true);
			ps.setString(10, "LINKED_FIX");
			ps.setDouble(11, rLine.getDouble("pdv"));
            ps.setString(12, rLine.getString("komentar"));
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addServiceIPTVLinked(JSONObject rLIne, String opername, int box_service_id, database db) {
		PreparedStatement ps;
		String query = "INSERT INTO servicesUser (id_service, box_id, nazivPaketa, date_added, userID, produzenje, opername,"
                + "IPTV_MAC, linkedService, paketType, endDate, PDV, opis) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLIne.getInt("IPTV_Service_ID"));
			ps.setInt(2, box_service_id);
			ps.setString(3, rLIne.getString("tariff_plan"));
			ps.setString(4, dtf.format(LocalDateTime.now()));
			ps.setInt(5, rLIne.getInt("userID"));
			ps.setInt(6, rLIne.getInt("produzenje"));
			ps.setString(7, opername);
			ps.setString(8, rLIne.getString("STB_MAC"));
			ps.setBoolean(9, true);
			ps.setString(10, "LINKED_IPTV");
			ps.setString(11, "2000-01-01");
			ps.setDouble(12, rLIne.getDouble("pdv"));
            ps.setString(13, rLIne.getString("komentar"));
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static String addServiceDTV(int id_service, String nazivPaketa, int userID,
                                       String opername, double popust, double cena, Boolean obracun,
                                       String brojUgovora, int produzenje, String idDTVCard, int DTVPaket, double pdv, String opis, database db) {
		PreparedStatement ps;
		String ServiceAdded;
		String query = "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena,"
                + " obracun, brojUgovora, produzenje, newService, idDTVCard, DTVPaket, linkedService, paketType, endDate, PDV, opis)"
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, id_service);
			ps.setString(2, nazivPaketa);
			ps.setString(3, LocalDateTime.now().format(dtf));
			ps.setInt(4, userID);
			ps.setString(5, opername);
			ps.setDouble(6, popust);
			ps.setDouble(7, cena);
			ps.setBoolean(8, obracun);
			ps.setString(9, brojUgovora);
			ps.setInt(10, produzenje);
			ps.setBoolean(11, true);
			ps.setString(12, idDTVCard);
			ps.setInt(13, DTVPaket);
			ps.setBoolean(14, false);
			ps.setString(15, "DTV");
			ps.setString(16, "2000-01-01");
			ps.setDouble(17, pdv);
            ps.setString(18, opis);
			ps.executeUpdate();

			ServiceAdded = "SERVICE_ADDED";

		} catch (SQLException e) {
			ServiceAdded = e.getMessage();
			e.printStackTrace();
		}

		query = "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate, createDate) VALUES(?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, Integer.valueOf(idDTVCard));
			ps.setInt(2, userID);
			ps.setInt(3, DTVPaket);
            ps.setString(4, LocalDate.parse("2000-01-01").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ps.setString(5, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ServiceAdded;

	}

	public static String addServiceNET(JSONObject rLine, String opername, database db) {
		String Message;
		if (NETFunctions.check_userName_busy(rLine.getString("userName"), db)) {
			Message = "USER_EXIST";
			return Message;
		}

		NETFunctions.addUser(rLine, db);

		PreparedStatement ps;
		String query = "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, "
                + "obracun, brojUgovora, aktivan, produzenje, newService, UserName, GroupName, paketType, endDate, PDV, opis) VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("id"));
			ps.setString(2, rLine.getString("nazivPaketa"));
			ps.setString(3, dtf.format(LocalDateTime.now()));
			ps.setInt(4, rLine.getInt("userID"));
			ps.setString(5, opername);
			ps.setDouble(6, rLine.getDouble("servicePopust"));
			ps.setDouble(7, rLine.getDouble("cena"));
			ps.setBoolean(8, rLine.getBoolean("obracun"));
			ps.setString(9, rLine.getString("brojUgovora"));
			ps.setBoolean(10, false);
			ps.setInt(11, rLine.getInt("produzenje"));
			ps.setBoolean(12, true);
			ps.setString(13, rLine.getString("userName"));
			ps.setString(14, rLine.getString("groupName"));
			ps.setString(15, "NET");
			ps.setString(16, "2000-01-01");
			ps.setDouble(17, rLine.getDouble("pdv"));
            ps.setString(18, rLine.getString("komentar"));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Message = "USER_ADDED";
		return Message;
	}

	public static String addServiceFIX(JSONObject rLine, String opername, database db) {
		String Message;

		PreparedStatement ps;
		String query = "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, "
                + "obracun, brojUgovora, aktivan, produzenje, newService, FIKSNA_TEL, paketType, PDV, opis) "
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("id"));
			ps.setString(2, rLine.getString("nazivPaketa"));
			ps.setString(3, dtf.format(LocalDateTime.now()));
			ps.setInt(4, rLine.getInt("userID"));
			ps.setString(5, opername);
			ps.setDouble(6, rLine.getDouble("popust"));
			ps.setDouble(7, rLine.getDouble("cena"));
			ps.setBoolean(8, rLine.getBoolean("obracun"));
			ps.setString(9, rLine.getString("brojUgovora"));
			ps.setBoolean(10, false);
			ps.setInt(11, 0);
			ps.setBoolean(12, true);
			ps.setString(13, rLine.getString("brojTel"));
			ps.setString(14, "FIX");
			ps.setDouble(15, rLine.getDouble("pdv"));
            ps.setString(16, rLine.getString("komentar"));
			ps.executeUpdate();
			ps.close();
			Message = "SERVICE_ADDED";
		} catch (SQLException e) {
			Message = e.getMessage();
			e.printStackTrace();
		}
		return Message;
	}

	public static String addServiceIPTV(JSONObject rLine, String opername, database db) {
		String Message = null;
		PreparedStatement ps;
		String query = "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena,"
                + "obracun, brojUgovora, aktivan, produzenje, newService, IPTV_EXT_ID, IPTV_MAC, paketType, endDate, PDV, opis)"
				+ "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("id"));
			ps.setString(2, rLine.getString("nazivPaketa"));
			ps.setString(3, dtf.format(LocalDateTime.now()));
			ps.setInt(4, rLine.getInt("userID"));
			ps.setString(5, opername);
			ps.setDouble(6, rLine.getDouble("popust"));
			ps.setDouble(7, rLine.getDouble("cena"));
			ps.setBoolean(8, rLine.getBoolean("obracun"));
			ps.setString(9, rLine.getString("brojUgovora"));
			ps.setBoolean(10, false);
			ps.setInt(11, rLine.getInt("produzenje"));
			ps.setBoolean(12, true);
			ps.setString(13, rLine.getString("external_id"));
			ps.setString(14, rLine.getString("STB_MAC"));
			ps.setString(15, "IPTV");
			ps.setString(16, "2000-01-01");
			ps.setDouble(17, rLine.getDouble("pdv"));
            ps.setString(18, rLine.getString("komentar"));
			ps.executeUpdate();
			ps.close();
			Message = "SERVICE_ADDED";
		} catch (SQLException e) {
			Message = e.getMessage();
			e.printStackTrace();
		}
		return Message;
	}

    public static String addServiceOstalo(JSONObject rLine, String opername, database db) {
        PreparedStatement ps;
        String Message = "SERVICE_ADDED";
        String query = "INSERT INTO servicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, " +
                "obracun, aktivan, newService, paketType, PDV, endDate, brojUgovora, opis)" +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setString(2, rLine.getString("naziv"));
            ps.setString(3, LocalDateTime.now().format(dtf));
            ps.setInt(4, rLine.getInt("userID"));
            ps.setString(5, opername);
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setDouble(7, rLine.getDouble("cena"));
            ps.setBoolean(8, rLine.getBoolean("obracun"));
            ps.setBoolean(9, false);
            ps.setBoolean(10, true);
            ps.setString(11, rLine.getString("paketType"));
            ps.setDouble(12, rLine.getDouble("pdv"));
            ps.setString(13, "");
            ps.setString(14, rLine.getString("brojUgovora"));
            ps.setString(15, rLine.getString("komentar"));
            ps.executeUpdate();
            ps.close();


        } catch (SQLException e) {
            Message = e.getMessage();
            e.printStackTrace();
        }

        return Message;
    }

    public static void deleteServiceOstalo(JSONObject delObj, String opername, database db) {
        PreparedStatement ps;
        String query = "DELETE FROM servicesUser WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, delObj.getInt("id"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static void deleteServiceDTV(JSONObject delObj, String operName, database db) {
		PreparedStatement ps;
		PreparedStatement psDelete;
		ResultSet rs;
		String query;
		String DTVKartica;

		query = "SELECT * FROM servicesUser WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				DTVKartica = rs.getString("idDTVCard");
				query = "DELETE FROM DTVKartice WHERE idKartica=?";
				psDelete = db.conn.prepareStatement(query);
				psDelete.setInt(1, Integer.valueOf(DTVKartica));
				psDelete.executeUpdate();
				ps.close();

				query = "DELETE FROM servicesUser WHERE id=?";
				psDelete = db.conn.prepareStatement(query);
				psDelete.setInt(1, delObj.getInt("id"));
				psDelete.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteServiceNET(JSONObject delObj, String operName, database db) {
		PreparedStatement ps;
		PreparedStatement psDelete;
		ResultSet rs;

		String userName;

		String query = "SELECT * FROM servicesUser WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				userName = rs.getString("UserName");
				query = "DELETE FROM radusergroup WHERE username=? ";
				psDelete = db.connRad.prepareStatement(query);
				psDelete.setString(1, userName);
				psDelete.executeUpdate();

				query = "DELETE from radreply WHERE username=?";
				psDelete = db.connRad.prepareStatement(query);
				psDelete.setString(1, userName);
				psDelete.executeUpdate();

				query = "DELETE from radcheck WHERE username=?";
				psDelete = db.connRad.prepareStatement(query);
				psDelete.setString(1, userName);
				psDelete.executeUpdate();

				query = "DELETE FROM servicesUser WHERE id=?";
				psDelete = db.conn.prepareStatement(query);
				psDelete.setInt(1, delObj.getInt("id"));
				psDelete.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteServiceIPTV(JSONObject delObj, String operName, database db) {
		PreparedStatement ps;
		ResultSet rs;
		String query;
		String mac = null;
		int userID = 0;

		query = "SELECT * FROM servicesUser WHERE id=?";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				mac = rs.getString("IPTV_MAC");
				userID = rs.getInt("userID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//brisanje IPTV tarife RESTAPIjem
		System.out.println("MAC ZA BRSIANJ:" + mac);
		StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
		stalkerRestAPI2.deleteAccount(mac);

		//brisanje u baziu
		query = "DELETE FROM servicesUser WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void deleteServiceFIX(JSONObject delObj, String operName, database db) {
		PreparedStatement ps;
		ResultSet rs;
		String query;
		String brojTelefona = new String();
		query = "SELECT * FROM servicesUser WHERE id=?";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				brojTelefona = rs.getString("FIKSNA_TEL");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		query = "DELETE FROM servicesUser WHERE id=?";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			ps.executeUpdate();
			ps.close();
			FIXFunctions.deleteService(brojTelefona, db);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteServiceBOX(JSONObject delObj, String operName, database db) {
		PreparedStatement ps;
		String query;

		query = "DELETE FROM servicesUser WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, delObj.getInt("id"));
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public static String getDatumIsteka(JSONObject rLine, database db) {
		PreparedStatement ps;
		ResultSet rs;
		String query = "SELECT endDate FROM servicesUser WHERE id=?";
		String datumIsteka = null;
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("serviceID"));
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				datumIsteka = rs.getString("endDate");
			}
            if (datumIsteka == null) {
                query = "SELECT endDate FROM servicesUser WHERE box_id=?";
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("serviceID"));
                rs = ps.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    datumIsteka = rs.getString("endDate");
                }
            }
        } catch (SQLException e) {
			e.printStackTrace();
		}

		return datumIsteka;
	}

	public static String addService(JSONObject rLine, String operName, database db) {
		PreparedStatement ps;
		String cal = LocalDateTime.now().format(dtfNormalDate);
		LocalDate calZaMesec = null;

		calZaMesec = LocalDate.parse(rLine.getString("zaMesec") + "-01");

		String query = "INSERT INTO userDebts (id_ServiceUser,  nazivPaketa, datumZaduzenja, userID, popust, "
				+ "paketType, cena, uplaceno,  dug,  zaduzenOd, zaMesec, PDV) VALUES"
				+ "(?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rLine.getInt("id_ServiceUser"));
			ps.setString(2, rLine.getString("nazivPaketa"));
			ps.setString(3, cal);
			ps.setInt(4, rLine.getInt("userID"));
			ps.setDouble(5, rLine.getDouble("popust"));
			ps.setString(6, rLine.getString("paketType"));
			double cena = rLine.getDouble("cena");
			double pdv = rLine.getDouble("pdv");
			double popust = rLine.getDouble("popust");
            double dug = cena - valueToPercent.getDiffValue(cena, popust);
            dug = dug + valueToPercent.getDiffValue(dug, pdv);
            ps.setDouble(7, cena);
			ps.setDouble(8, 0.00);
			ps.setDouble(9, Double.parseDouble(df.format(dug)));
			ps.setString(10, operName);
			ps.setString(11, calZaMesec.format(dtfMesecZaduzenja));
			ps.setDouble(12, rLine.getDouble("pdv"));
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return "Usluga zaduzena";
	}

	public static Boolean check_service_exist(int id_ServiceUser, int userID, String zaMesec, database db) {
		PreparedStatement ps;
		ResultSet rs;
		boolean serviceExist = false;
		String query = "SELECT * from userDebts WHERE id_ServiceUser=? AND userID=? AND zaMesec=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, id_ServiceUser);
			ps.setInt(2, userID);
			ps.setString(3, zaMesec);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				serviceExist = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return serviceExist;

	}

	public static void activateBoxService(String operName, ResultSet rs, database db) {
		PreparedStatement ps;
		ResultSet resultSet;
		String query;
        int lastInserID = 0;

		query = "SELECT * FROM servicesUser WHERE box_id=?";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, rs.getInt("id"));
			resultSet = ps.executeQuery();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					produziService(resultSet.getInt("id"), operName, false, db);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		query = "INSERT INTO userDebts "
				+ "(id_ServiceUser, nazivPaketa, datumZaduzenja, userID, "
                + "paketType, cena, dug, popust, zaduzenOd, zaMesec, PDV) "
                + "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?)";

		int daysInMonth = 0;
		int daysToEndMonth = 0;
        Double cenaService = 0.00;
		Double cenaServiceOrig = 0.00;
        Double pdv = 0.00;
        Double popust = 0.00;
        try {
            cenaService = rs.getDouble("cena");
			cenaServiceOrig = cenaService;
            pdv = rs.getDouble("PDV");
            popust = rs.getDouble("popust");
            cenaService = cenaService + valueToPercent.getDiffValue(cenaService, pdv);
            cenaService = cenaService - valueToPercent.getDiffValue(cenaService, popust);
			cenaServiceOrig = cenaServiceOrig - valueToPercent.getDiffValue(cenaServiceOrig, popust);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		Double zaUplatu = 0.00;
		Double cenaZaDan = 0.00;
		Double cenaZaDanOrig = 0.00;
		LocalDateTime date = LocalDateTime.now();

        //status on STALKER
		try {
			if (rs.getString("paketType").equals("IPTV") || rs.getString("paketType").equals("LINKED_IPTV")) {
				StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
				stalkerRestAPI2.activateStatus(true, rs.getString("IPTV_MAC"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if (rs.getBoolean("newService")) {
				daysInMonth = date.getMonth().length(true);
				daysToEndMonth = daysInMonth - date.getDayOfMonth();
				cenaZaDan = cenaService / daysInMonth;
				cenaZaDanOrig = cenaServiceOrig / daysInMonth;

				if (rs.getBoolean("newService")) {
					zaUplatu = cenaZaDan * daysToEndMonth;
					cenaServiceOrig = cenaZaDanOrig * daysToEndMonth;
				} else {
					zaUplatu = cenaService;
				}
			}

            ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, rs.getInt("id"));
			ps.setString(2, rs.getString("nazivPaketa"));
			ps.setString(3, LocalDate.now().toString());
			ps.setInt(4, rs.getInt("userID"));
			ps.setString(5, rs.getString("paketType"));
			ps.setDouble(6, cenaServiceOrig);
			ps.setDouble(7, Double.parseDouble(df.format(zaUplatu)));
            ps.setDouble(8, rs.getDouble("popust"));
            ps.setString(9, operName);
            ps.setString(10, LocalDate.now().format(dtfMesecZaduzenja));
            ps.setDouble(11, rs.getDouble("pdv"));
            ps.executeUpdate();
            ResultSet rsID = ps.getGeneratedKeys();
            if (rsID.next()) {
                lastInserID = rsID.getInt(1);
            }
        } catch (SQLException e) {

			e.printStackTrace();
		}


        //INSERT FAKTURE IF USER HAS FIRMA
        try {
            FaktureFunct faktureFunct = new FaktureFunct(rs.getInt("userID"), LocalDate.now(), operName, db);
            if (faktureFunct.hasFirma) {
                query = "SELECT * FROM userDebts WHERE id=?";
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, lastInserID);
                ResultSet resultSet1 = ps.executeQuery();
                if (resultSet1.next()) {
                    faktureFunct.createFakturu(resultSet1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }




		query = "UPDATE servicesUser set aktivan=1, newService=false, date_activated=? WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setString(1, LocalDateTime.now().toString());
			ps.setInt(2, rs.getInt("id"));
			ps.executeUpdate();
			ps.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void activateService(String operName, ResultSet rs, database db) {
        int lastInserID = 0;
        try {
			if (rs.getBoolean("newService")) {
				LocalDateTime date = LocalDateTime.now();
				String query;
				int daysInMonth = 0;
				int daysToEndMonth = 0;
				Double cenaService = rs.getDouble("cena");
				Double cenaServiceOrig = cenaService;
                Double pdv = rs.getDouble("PDV");
                Double popust = rs.getDouble("popust");
                Double zaUplatu = 0.00;
                Double cenaZaDan = 0.00;
				Double cenaZaDaOrig = 0.00;
                cenaService = cenaService + valueToPercent.getDiffValue(cenaService, pdv);
                cenaService = cenaService - valueToPercent.getDiffValue(cenaService, popust);

				if (rs.getBoolean("newService")) {
                    daysInMonth = date.getMonth().length(LocalDate.now().isLeapYear());
                    daysToEndMonth = daysInMonth - date.getDayOfMonth();
					cenaZaDan = cenaService / daysInMonth;
					cenaZaDaOrig = cenaServiceOrig / daysInMonth;
					zaUplatu = cenaZaDan * daysToEndMonth;
					cenaServiceOrig = cenaZaDaOrig * daysToEndMonth;
				} else {
					zaUplatu = cenaService;
				}

				PreparedStatement ps;

				query = "INSERT INTO userDebts "
						+ "(id_ServiceUser, nazivPaketa, datumZaduzenja, "
                        + "userID, paketType, cena, dug, popust, zaduzenOd, zaMesec, PDV) "
                        + "VALUES "
                        + "(?,?,?,?,?,?,?,?,?,?,?)";

                ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, rs.getInt("id"));
				ps.setString(2, rs.getString("nazivPaketa"));
				ps.setString(3, LocalDate.now().toString());
				ps.setInt(4, rs.getInt("userID"));
				ps.setString(5, rs.getString("paketType"));
				ps.setDouble(6, cenaServiceOrig);
				ps.setDouble(7, Double.parseDouble(df.format(zaUplatu)));
                ps.setDouble(8, rs.getDouble("popust"));
                ps.setString(9, operName);
                ps.setString(10, LocalDate.now().format(dtfMesecZaduzenja));
                ps.setDouble(11, rs.getDouble("PDV"));
                ps.executeUpdate();

                ResultSet rsID = ps.getGeneratedKeys();
                if (rsID.next()) {
                    lastInserID = rsID.getInt(1);
                }

				produziService(rs.getInt("id"), operName, false, db);

			}

            //INSERT FAKTURE IF USER HAS FIRMA
            try {
                FaktureFunct faktureFunct = new FaktureFunct(rs.getInt("userID"), LocalDate.now(), operName, db);
                if (faktureFunct.hasFirma) {
                    String query = "SELECT * FROM userDebts WHERE id=?";
                    PreparedStatement ps = db.conn.prepareStatement(query);
                    ps.setInt(1, lastInserID);
                    ResultSet resultSet = ps.executeQuery();
                    if (resultSet.next()) {
                        faktureFunct.createFakturu(resultSet);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }



			//samo u slucaju ako ima IPTV
			if (rs.getString("IPTV_MAC") != null) {
				StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
				stalkerRestAPI2.activateStatus(true, rs.getString("IPTV_MAC"));
			}

			String query = "UPDATE servicesUser SET aktivan=1, newService=false, date_activated=? WHERE id=?";
			PreparedStatement ps = db.conn.prepareStatement(query);
			ps.setString(1, LocalDateTime.now().toString());
			ps.setInt(2, rs.getInt("id"));
			ps.executeUpdate();
			ps.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void produziService(int serviceID, String operName, boolean skipProduzenje, database db) {
		PreparedStatement ps;
		ResultSet rs;
		boolean newService = false;
		int produzenje = 0;
		String UserName = null;
		int idCard = 0;
		String IPTV_MAC = null;

		LocalDate endDate = LocalDate.now();
		String type = "NONE";
		String query;

		query = "SELECT * FROM servicesUser WHERE id=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, serviceID);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				type = rs.getString("paketType");
				newService = rs.getBoolean("newService");
				produzenje = rs.getInt("produzenje");
				UserName = rs.getString("UserName");
				idCard = rs.getInt("idDTVCard");
				IPTV_MAC = rs.getString("IPTV_MAC");

				if (newService) {
					endDate = endDate.plusMonths(produzenje);
					endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
				} else {
					endDate = LocalDate.parse(LocalDate.parse(rs.getString("endDate")).format(dtfNormalDate));
					produzenje = 1;
					endDate = endDate.plusMonths(produzenje);
					endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
				}
				if (skipProduzenje) {
					endDate = LocalDate.parse(LocalDate.parse(rs.getString("endDate")).format(dtfNormalDate));
					produzenje = 0;
					endDate.plusMonths(produzenje);
					endDate = endDate.with(TemporalAdjusters.firstDayOfMonth());

				}

			}
			rs.close();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		switch (type) {
			case "NET":
			case "LINKED_NET":
				setEndDateNET(UserName, endDate, db);
				break;

			case "IPTV":
			case "LINKED_IPTV":
				setEndDateIPTV(IPTV_MAC, endDate, db);
				break;

			case "DTV":
			case "LINKED_DTV":
				setEndDateDTV(idCard, endDate, db);
				break;
		}

		if (newService) {
			query = "UPDATE servicesUser SET endDate=?,  newService=false WHERE id=?";
		} else {
			query = "UPDATE servicesUser SET endDate=?, newService=false WHERE id=?";
		}
		try {
			ps = db.conn.prepareStatement(query);
			ps.setString(1, endDate.format(dtfNormalDate));
			ps.setInt(2, serviceID);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void setEndDateNET(String username, LocalDate endDate, database db) {
		String eDateRadCheck = endDate.format(dtfRadCheck);
		LocalTime time = LocalTime.of(00, 00, 00);
		String eDateRadReply = LocalDateTime.of(endDate, time).format(dtfRadReply);

		PreparedStatement ps;
		String query;

		try {
			query = "UPDATE radreply SET value=? WHERE username=? AND attribute='WISPR-Session-Terminate-time'";
			ps = db.connRad.prepareStatement(query);
			ps.setString(1, eDateRadReply);
			ps.setString(2, username);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		query = "UPDATE radcheck SET value=? WHERE username=? AND attribute='Expiration'";
		try {
			ps = db.connRad.prepareStatement(query);
			ps.setString(1, eDateRadCheck);
			ps.setString(2, username);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void setEndDateDTV(int idCard, LocalDate endDate, database db) {
		String eDate = endDate.format(dtfNormalDate);
		String query;
		PreparedStatement ps;

		query = "UPDATE DTVKartice SET endDate=? WHERE idKartica=?";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setString(1, eDate);
			ps.setInt(2, idCard);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void setEndDateIPTV(String STB_MAC, LocalDate endDate, database db) {
		String eDate = LocalDateTime.of(endDate, LocalTime.of(00, 00, 00)).format(dtfIPTV);
		StalkerRestAPI2 stalkerRestAPI2 = new StalkerRestAPI2(db);
		stalkerRestAPI2.setEndDate(STB_MAC, eDate);
		System.out.println("ENDATE IPTV: " + STB_MAC + " " + eDate);
	}

	public static String getIdentify(int id_servicesUser, database db) {
		PreparedStatement ps;
		ResultSet rs;
        String ident = "NEPOZNAT";

		String query = "SELECT * from servicesUser WHERE id=?";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, id_servicesUser);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				if (rs.getString("idDTVCard") != null)
					ident = rs.getString("idDTVCard");
				if (rs.getString("UserName") != null)
					ident = rs.getString("UserName");
				if (rs.getString("FIKSNA_TEL") != null)
					ident = rs.getString("FIKSNA_TEL");
				if (rs.getString("IPTV_MAC") != null)
					ident = rs.getString("IPTV_MAC");
			}
			ps.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ident;
	}

    public static String uplataLOG(JSONObject rLine, database db) {
        PreparedStatement ps;
        ResultSet rs = null;
        String result;
		String query;

        query = "INSERT INTO uplate " +
                "(datumUplate, uplaceno, nazivServisa, idServisa, mesto, operater, userID, napomena, identification, idUserDebts, mestoUplate, zaMesec) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, LocalDateTime.now().toString());
            ps.setDouble(2, rLine.getDouble("uplaceno"));
			ps.setString(3, rLine.getString("nazivPaketa"));
			ps.setInt(4, rLine.getInt("id_ServiceUser"));
            ps.setString(5, "mesto");
            ps.setString(6, rLine.getString("operater"));
            ps.setInt(7, rLine.getInt("userID"));
            ps.setString(8, "");
            ps.setString(9, rLine.getString("identification"));
            ps.setInt(10, rLine.getInt("id"));
            ps.setString(11, rLine.getString("mestoUplate"));
            ps.setString(12, rLine.getString("zaMesec"));
            ps.executeUpdate();
            result = "UPLACENO";
        } catch (SQLException e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

	public static boolean boxHaveFIX(int id_serviceUser, database db) {
		PreparedStatement ps;
		ResultSet rs;
		boolean haveFix = false;
		String query = "SELECT FIKSNA_TEL FROM servicesUser WHERE id=? AND FIKSNA_TEL IS NOT NULL AND FIKSNA_TEL != '' ";
		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, id_serviceUser);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				haveFix = true;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return haveFix;
	}

    public void updateService(String operName, database db, JSONObject rLine) {
        PreparedStatement ps;
        String query = "";
        if (rLine.has("idDTVCard")) {
            query = "UPDATE servicesUser set idDTVCard=? where id=?";
        } else if (rLine.has("IPTV_MAC")) {
            query = "UPDATE servicesUser set IPTV_MAC=? where id=?";
        }

        try {
            ps = db.conn.prepareStatement(query);
            if (rLine.has("idDTVCard")) {
                ps.setString(1, rLine.getString("idDTVCard"));
                ps.setInt(2, rLine.getInt("id"));
            } else if (rLine.has("IPTV_MAC")) {
                ps.setString(1, rLine.getString("IPTV_MAC"));
                ps.setInt(2, rLine.getInt("id"));
            }
            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            this.haveError = true;
            this.errorMessage = e.getMessage();
            e.printStackTrace();
        }

        //update popust i obracun
        query = "UPDATE servicesUser SERT popust=? where id=?";


        //update field of DTVCard no with a new one
        if (rLine.has("idDTVCard")) {
            query = "UPDATE DTVKartice SET idKartica=? WHERE idKartica=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, rLine.getString("idDTVCard"));
                ps.setString(2, rLine.getString("old_idDTVCard"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isHaveError() {
        return haveError;
    }

    public void setHaveError(boolean haveError) {
        this.haveError = haveError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
