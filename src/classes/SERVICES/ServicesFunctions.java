package classes.SERVICES;

import classes.INTERNET.NETFunctions;
import classes.database;
import classes.valueToPercent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zoom on 2/27/17.
 */
public class ServicesFunctions {

    private static Format dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static Format dtfNormalDate = new SimpleDateFormat("yyyy-MM-dd");
    private static Format dtfRadCheck = new SimpleDateFormat("dd MMM yyyy");
    private static Format dtfRadReply = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static Format dtfMesecZaduzenja = new SimpleDateFormat("yyyy-MM");
    private static DecimalFormat df = new DecimalFormat("#.##");

    public static void addServiceLinked(JSONObject rLine, String opername, database db) {
        ResultSet rs;
        PreparedStatement ps;
        String query = "INSERT INTO ServicesUser " +
                "(id_service, box_id, nazivPaketa, UserName, idDTVCard, DTVPaket, userID, obracun, produzenje, operName, linkedService, paketType) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            if (rLine.has("DTV_service_ID"))
                ps.setInt(1, rLine.getInt("DTV_service_ID"));
            if (rLine.has("NET_service_ID"))
                ps.setInt(1, rLine.getInt("NET_service_ID"));
            if (rLine.has("FIKSNA_service_ID"))
                ps.setInt(1, rLine.getInt("FIKSNA_service_ID"));
            if (rLine.has("IPTV_service_ID"))
                ps.setInt(1, rLine.getInt("IPTV_service_ID"));
            ps.setInt(2, rLine.getInt("idPaket"));
            if (rLine.has("nazivPaketaDTV"))
                ps.setString(3, rLine.getString("nazivPaketaDTV"));
            if (rLine.has("nazivPaketaNET"))
                ps.setString(3, rLine.getString("nazivPaketaNET"));
            if (rLine.has("nazivPaketaFIKSNA"))
                ps.setString(3, "nazivPaketFIKSNA");
            if (rLine.has("nazivPaketaIPTV"))
                ps.setString(3, "nazivPaketaIPTV");

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

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addServiceDTVLinked(JSONObject rLine, String opername, int BOX_Service_ID, database db) {
        PreparedStatement ps;
        String query = "INSERT INTO ServicesUser (id_service, box_id, nazivPaketa, date_added,  idDTVCard, DTVPaket,  userID, produzenje, operName, linkedService, paketType) " +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("DTV_service_ID"));
            ps.setInt(2, BOX_Service_ID);
            ps.setString(3, rLine.getString("nazivPaketaDTV"));
            ps.setString(4, dtf.format(new Date()));
            ps.setString(5, String.valueOf(rLine.getInt("DTVKartica")));
            ps.setInt(6, rLine.getInt("DTVPaket"));
            ps.setInt(7, rLine.getInt("userID"));
            ps.setInt(8, rLine.getInt("produzenje"));
            ps.setString(9, opername);
            ps.setBoolean(10, true);
            ps.setString(11, "LINKED_DTV");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void addServiceNETLinked(JSONObject rLine, String opername, int BOX_Service_ID, database db) {
        PreparedStatement ps;
        String query = "INSERT INTO ServicesUser (id_service, box_id, nazivPaketa, date_added, userID, produzenje, operName, UserName, GroupName, linkedService, paketType ) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("NET_service_ID"));
            ps.setInt(2, BOX_Service_ID);
            ps.setString(3, rLine.getString("nazivPaketaNET"));
            ps.setString(4, dtf.format(new Date()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setInt(6, rLine.getInt("produzenje"));
            ps.setString(7, opername);
            ps.setString(8, rLine.getString("userName"));
            ps.setString(9, rLine.getString("groupName"));
            ps.setBoolean(10, true);
            ps.setString(11, "LINKED_NET");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String addServiceDTV(int id_service, String nazivPaketa, int userID,
                                       String opername, double popust, double cena, Boolean obracun,
                                       String brojUgovora, int produzenje, String idDTVCard, int DTVPaket, database db) {
        PreparedStatement ps;
        String ServiceAdded;
        String query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena," +
                " obracun, brojUgovora, produzenje, newService, idDTVCard, DTVPaket, linkedService, paketType)" +

                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id_service);
            ps.setString(2, nazivPaketa);
            ps.setString(3, dtf.format(new Date()));
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
            ps.executeUpdate();

            ServiceAdded = "SERVICE_ADDED";


        } catch (SQLException e) {
            ServiceAdded = e.getMessage();
            e.printStackTrace();
        }

        query = "INSERT INTO DTVKartice (idKartica, userID, paketID, endDate) VALUES(?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, Integer.valueOf(idDTVCard));
            ps.setInt(2, userID);
            ps.setInt(3, DTVPaket);
            ps.setString(4, "1970-01-01");
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
        String query = "INSERT INTO ServicesUser (id_service, nazivPaketa, date_added, userID, operName, popust, cena, " +
                "obracun, brojUgovora, aktivan, produzenje, newService, UserName, GroupName, paketType) VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setString(2, rLine.getString("nazivPaketa"));
            ps.setString(3, dtf.format(new Date()));
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
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Message = "USER_ADDED";
        return Message;

    }

    public static void activateBoxServiceNew(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        ResultSet rs = null;

        String query = "SELECT * FROM ServicesUser WHERE box_id=? and userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("userID"));
            rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (rLine.getBoolean("newService")) {
                        if (rs.getString("paketType").equals("LINKED_DTV")) {
                            activateDTVServiceBoxNew(rLine, rs, operName, db);
                        }
                        if (rs.getString("paketType").equals("LINKED_NET")) {
                            activateNetServiceBoxNew(rLine, rs, operName, db);
                        }
                    } else {
                        if (rs.getString("paketType").equals("LINKED_DTV")) {
                            activateDTVServiceBox(rLine, rs, operName, db);
                        }
                        if (rs.getString("paketType").equals("LINKED_NET")) {
                            activateNetServiceBox(rLine, rs, operName, db);
                        }
                    }
                }
            } else {

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        query = "UPDATE ServicesUser  SET aktivan=? WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setBoolean(1, true);
            ps.setInt(2, rLine.getInt("id"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int current_day = cal.get(Calendar.DAY_OF_MONTH);
        int est_days = days - current_day;
        double cena = rLine.getDouble("cena");
        cena = cena / days;
        cena = cena * est_days;
        cena = valueToPercent.getValue(cena, rLine.getDouble("popust"));

        Calendar calDatumZaduzenja = Calendar.getInstance();
        calDatumZaduzenja.set(Calendar.DAY_OF_MONTH, 1);

        query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, " +
                "paketType, cena, uplaceno, dug, zaduzenOd, zaMesec)" +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("boxID"));
            ps.setString(3, rLine.getString("nazivPaketa"));
            ps.setString(4, dtf.format(new Date()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setString(7, rLine.getString("paketType"));
            ps.setDouble(8, rLine.getDouble("cena"));
            ps.setDouble(9, 0.00);
            ps.setDouble(10, Double.parseDouble(df.format(cena)));
            ps.setString(11, operName);
            ps.setString(12, dtfMesecZaduzenja.format(calDatumZaduzenja.getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static void activateNetServiceBox(JSONObject rLine, ResultSet rs, String operName, database db) {
    }

    private static void activateDTVServiceBox(JSONObject rLine, ResultSet rs, String operName, database db) {
    }

    public static void activateBoxServiceaaa(JSONObject rLine, database db) {
        PreparedStatement ps;
    }

    public static void activateNetService(JSONObject rLine, ResultSet rs, String operName, database db) {

    }

    public static void activateNetServiceBoxNew(JSONObject rLine, ResultSet rs, String operName, database db) {
        PreparedStatement ps;
        String query = "UPDATE ServicesUser set aktivan=true where id=? and userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rs.getInt("id"));
            ps.setInt(2, rs.getInt("userID"));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.MONTH, rLine.getInt("produzenje"));
        calEnd.set(Calendar.DAY_OF_MONTH, 1);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MINUTE, 0);
        calEnd.set(Calendar.MILLISECOND, 0);
        calEnd.set(Calendar.HOUR_OF_DAY, 0);


        query = "UPDATE radcheck set value=? where username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, dtfRadCheck.format(calEnd.getTime()));
            ps.setString(2, rs.getString("UserName"));
            ps.setString(3, "Expiration");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "UPDATE radcheck set value=? WHERE username=? AND attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, "Accept");
            ps.setString(2, rs.getString("UserName"));
            ps.setString(3, "Auth-Type");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        calEnd.add(Calendar.SECOND, -1);
        query = "UPDATE radreply set value=? where username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, dtfRadReply.format(calEnd.getTime()));
            ps.setString(2, rs.getString("UserName"));
            ps.setString(3, "WISPR-Session-Terminate-time");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void activateDTVService(JSONObject rLine, ResultSet rs, String operName, database db) {
        PreparedStatement ps;
    }

    public static void activateDTVServiceBoxNew(JSONObject rLine, ResultSet rs, String opername, database db) {
        PreparedStatement ps;
        try {
            int produzenje = rs.getInt("produzenje");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String query = "UPDATE ServicesUser set aktivan=true where id=? and userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rs.getInt("id"));
            ps.setInt(2, rLine.getInt("userID"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int current_day = cal.get(Calendar.DAY_OF_MONTH);
        int est_days = days - current_day;
        double cena = rLine.getDouble("cena");
        cena = cena / days;
        cena = cena * est_days;

        Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.MONTH, rLine.getInt("produzenje"));
        calEnd.set(Calendar.DAY_OF_MONTH, 1);


        query = "UPDATE DTVKartice set endDate=? WHERE userID=? and idKartica=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, dtf.format(calEnd.getTime()));
            ps.setInt(2, rs.getInt("userID"));
            ps.setInt(3, Integer.valueOf(rs.getString("idDTVCard")));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void activateDTVServiceNew(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        String query = "UPDATE ServicesUser SET  aktivan=? WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setBoolean(1, true);
            ps.setInt(2, rLine.getInt("id"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.MONTH, rLine.getInt("produzenje"));
        calEnd.set(Calendar.DAY_OF_MONTH, 1);


        query = "UPDATE DTVKartice SET endDate=? WHERE idKartica=? and userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, dtf.format(calEnd.getTime()));
            ps.setInt(2, Integer.valueOf(rLine.getInt("idKartica")));
            ps.setInt(3, rLine.getInt("userID"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //zadzenje do kraja meseca
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int current_day = cal.get(Calendar.DAY_OF_MONTH);
        int est_days = days - current_day;
        double cena = rLine.getDouble("cena");
        cena = cena / days;
        cena = cena * est_days;
        cena = valueToPercent.getValue(cena, rLine.getDouble("popust"));
        System.out.println("DUG: " + cena);

        //date zaduzenja
        Calendar calDatumZaduzenja = Calendar.getInstance();
        calDatumZaduzenja.set(Calendar.DAY_OF_MONTH, 1);


        query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, " +
                "paketType, cena, uplaceno, dug, zaduzenOd, zaMesec)" +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("serviceID"));
            ps.setString(3, rLine.getString("nazivPaketa"));
            ps.setString(4, dtf.format(new Date()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setString(7, rLine.getString("paketType"));
            ps.setDouble(8, rLine.getDouble("cena"));
            ps.setDouble(9, 0.00);
            ps.setDouble(10, Double.parseDouble(df.format(cena)));
            ps.setString(11, operName);
            ps.setString(12, dtfMesecZaduzenja.format(calDatumZaduzenja.getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void activateNetServiceNew(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        String query = "UPDATE ServicesUser set aktivan=true where id=? and userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("userID"));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.MONTH, rLine.getInt("produzenje"));
        calEnd.set(Calendar.DAY_OF_MONTH, 1);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MINUTE, 0);
        calEnd.set(Calendar.MILLISECOND, 0);
        calEnd.set(Calendar.HOUR_OF_DAY, 0);


        query = "UPDATE radcheck set value=? where username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, dtfRadCheck.format(calEnd.getTime()));
            ps.setString(2, rLine.getString("userName"));
            ps.setString(3, "Expiration");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "UPDATE radcheck set value=? WHERE username=? AND attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, "Accept");
            ps.setString(2, rLine.getString("userName"));
            ps.setString(3, "Auth-Type");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        calEnd.add(Calendar.SECOND, -1);
        query = "UPDATE radreply set value=? where username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, dtfRadReply.format(calEnd.getTime()));
            ps.setString(2, rLine.getString("userName"));
            ps.setString(3, "WISPR-Session-Terminate-time");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //zadzenje do kraja meseca
        Calendar cal = Calendar.getInstance();
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int current_day = cal.get(Calendar.DAY_OF_MONTH);
        int est_days = days - current_day;
        double cena = rLine.getDouble("cena");
        cena = cena / days;
        cena = cena * est_days;
        cena = valueToPercent.getValue(cena, rLine.getDouble("popust"));

        //date zaduzenja
        Calendar calDatumZaduzenja = Calendar.getInstance();
        calDatumZaduzenja.set(Calendar.DAY_OF_MONTH, 1);

        query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, " +
                "paketType, cena, uplaceno, dug, zaduzenOd, zaMesec)" +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id"));
            ps.setInt(2, rLine.getInt("serviceID"));
            ps.setString(3, rLine.getString("nazivPaketa"));
            ps.setString(4, dtf.format(new Date()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setString(7, rLine.getString("paketType"));
            ps.setDouble(8, rLine.getDouble("cena"));
            ps.setDouble(9, 0.00);
            ps.setDouble(10, Double.parseDouble(df.format(cena)));
            ps.setString(11, operName);
            ps.setString(12, dtfMesecZaduzenja.format(calDatumZaduzenja.getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void produziBOX(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM ServicesUser WHERE box_id=? and userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("userServiceID"));
            ps.setInt(2, rLine.getInt("userID"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (rs.getString("paketType").equals("LINKED_NET")) {
                        produziNETBox(rs.getString("UserName"), rs.getInt("userID"), rs.getInt("produzenje"), rLine.getString("zaMesec"), db);
                    }
                    if (rs.getString("paketType").equals("LINKED_DTV")) {
                        produziDTVBox(rs.getString("idDTVCard"), rs.getInt("userID"), rs.getInt("produzenje"), rLine.getString("zaMesec"), db);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void produziDTVBox(String idKartica, int userID, int produzenje, String zaMesec, database db) {
        PreparedStatement ps;
        PreparedStatement ps_update;
        ResultSet rs;
        Calendar calZaduzenje = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();
        try {
            calZaduzenje.setTime((Date) dtfMesecZaduzenja.parseObject(zaMesec));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        produzenje = produzenje + 1;
        calZaduzenje.set(Calendar.DAY_OF_MONTH, 1);
        calZaduzenje.add(Calendar.MONTH, produzenje);

        String query = "SELECT endDate from DTVKartice WHERE userID=? and idKartica=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setInt(2, Integer.valueOf(idKartica));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    try {
                        calEndTime.setTime((Date) dtfNormalDate.parseObject(rs.getString("endDate")));
                        calEndTime.set(Calendar.DAY_OF_MONTH, 1);
                        if (calZaduzenje.getTime().after(calEndTime.getTime())) {
                            query = "UPDATE DTVKartice SET endDate=? WHERE userID=? and idKartica=?";
                            ps_update = db.conn.prepareStatement(query);
                            ps_update.setString(1, String.valueOf(dtfNormalDate.format(calZaduzenje.getTime())));
                            ps_update.setInt(2, userID);
                            ps_update.setInt(3, Integer.valueOf(idKartica));
                            ps_update.executeUpdate();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void produziNETBox(String userName, int userID, int produzenje, String zaMesec, database db) {
        PreparedStatement ps;
        PreparedStatement ps_update;
        ResultSet rs;

        Calendar calEndTime = Calendar.getInstance();
        Calendar calZaduzenje = Calendar.getInstance();
        try {
            calZaduzenje.setTime((Date) dtfMesecZaduzenja.parseObject(zaMesec));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        produzenje = produzenje + 1;
        calZaduzenje.set(Calendar.DAY_OF_MONTH, 1);
        calZaduzenje.add(Calendar.MONTH, produzenje);

        String query = "SELECT  value FROM radcheck WHERE username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, userName);
            ps.setString(2, "Expiration");
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    try {
                        calEndTime.setTime((Date) dtfRadCheck.parseObject(rs.getString("value")));
                        calEndTime.set(Calendar.DAY_OF_MONTH, 1);
                        if (calZaduzenje.getTime().after(calEndTime.getTime())) {
                            query = "UPDATE radcheck SET value=? where username=? and attribute=?";
                            ps_update = db.connRad.prepareStatement(query);
                            ps_update.setString(1, dtfRadCheck.format(calZaduzenje.getTime()));
                            ps_update.setString(2, userName);
                            ps_update.setString(3, "Expiration");
                            ps_update.executeUpdate();

                            calZaduzenje.set(Calendar.SECOND, -1);
                            query = "UPDATE radreply  SET value=? WHERE username=? and attribute=?";
                            ps_update = db.connRad.prepareCall(query);
                            ps_update.setString(1, dtfRadReply.format(calZaduzenje.getTime()));
                            ps_update.setString(2, userName);
                            ps_update.setString(3, "WISPR-Session-Terminate-time");
                            ps_update.executeUpdate();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void produziDTV(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        PreparedStatement ps_update;
        PreparedStatement ps_DTVKartica;
        ResultSet rs;
        ResultSet rs_DTVKartica;

        Calendar calZaduzenje = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        String query = "SELECT * FROM ServicesUser WHERE userID=? and id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("userID"));
            ps.setInt(2, rLine.getInt("userServiceID"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    ps_DTVKartica = db.conn.prepareStatement("SELECT * from DTVKartice WHERE userID=? and idKartica=?");
                    ps_DTVKartica.setInt(1, rLine.getInt("userID"));
                    ps_DTVKartica.setInt(2, Integer.parseInt(rs.getString("idDTVCard")));
                    rs_DTVKartica = ps_DTVKartica.executeQuery();
                    if (rs_DTVKartica.isBeforeFirst()) {
                        rs_DTVKartica.next();
                        calEndTime.setTime((Date) dtfNormalDate.parseObject(rs_DTVKartica.getString("endDate")));
                        calEndTime.set(Calendar.DAY_OF_MONTH, 1);
                        calZaduzenje.setTime((Date) dtfMesecZaduzenja.parseObject(rLine.getString("zaMesec")));
                        calZaduzenje.set(Calendar.DAY_OF_MONTH, 1);
                        int produzenje = rs.getInt("produzenje");
                        produzenje = produzenje + 1;
                        calZaduzenje.add(Calendar.MONTH, produzenje);
                    } else {
                        return;
                    }
                    if (calZaduzenje.getTime().after(calEndTime.getTime())) {
                        ps_update = db.conn.prepareStatement("UPDATE DTVKartice SET endDate=? WHERE idKartica=? and userID=?");
                        ps_update.setString(1, String.valueOf(dtfNormalDate.format(calZaduzenje.getTime())));
                        ps_update.setString(2, rs.getString("idDTVCard"));
                        ps_update.setInt(3, rLine.getInt("userID"));
                        ps_update.executeUpdate();
                    }


                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public static void produziNET(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        PreparedStatement ps_update;
        ResultSet rs = null;
        int produzenje = 0;
        String username = null;

        Calendar calZaduzenje = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();


        String query = "SELECT * FROM ServicesUser WHERE id=? AND userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("userServiceID"));
            ps.setInt(2, rLine.getInt("userID"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                produzenje = rs.getInt("produzenje");
                produzenje = produzenje + 1;
                username = rs.getString("UserName");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        query = "SELECT value FROM radcheck WHERE username=? and attribute=?";
        try {
            ps = db.connRad.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, "Expiration");
            System.out.println(ps.toString());
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                try {
                    calEndTime.setTime((Date) dtfRadCheck.parseObject(rs.getString("value")));
                    calEndTime.set(Calendar.DAY_OF_MONTH, 1);
                    calZaduzenje.setTime((Date) dtfMesecZaduzenja.parseObject(rLine.getString("zaMesec")));
                    calZaduzenje.set(Calendar.DAY_OF_MONTH, 1);
                    calZaduzenje.add(Calendar.MONTH, produzenje);
                    if (calZaduzenje.getTime().after(calEndTime.getTime())) {
                        ps_update = db.connRad.prepareStatement("UPDATE radcheck SET value=? WHERE username=? AND attribute=?");
                        ps_update.setString(1, dtfRadCheck.format(calZaduzenje.getTime()));
                        ps_update.setString(2, username);
                        ps_update.setString(3, "Expiration");
                        ps_update.executeUpdate();

                        calZaduzenje.set(Calendar.SECOND, -1);
                        ps_update = db.connRad.prepareStatement("UPDATE radreply set value=? WHERE username=? AND attribute=?");
                        ps_update.setString(1, dtfNormalDate.format(calZaduzenje.getTime()));
                        ps_update.setString(2, username);
                        ps_update.setString(3, "WISPR-Session-Terminate-time");
                        ps_update.executeUpdate();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void deleteServiceDTV(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        PreparedStatement psDelete;
        ResultSet rs;

        String DTVKartica;

        String query = "SELECT * FROM ServicesUser WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("serviceId"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                DTVKartica = rs.getString("idDTVCard");
                query = "DELETE FROM DTVKartice WHERE idKartica=? AND userID=?";
                psDelete = db.conn.prepareStatement(query);
                psDelete.setInt(1, Integer.valueOf(DTVKartica));
                psDelete.setInt(2, rLine.getInt("userID"));
                psDelete.executeUpdate();

                query = "DELETE FROM ServicesUser WHERE id=?";
                psDelete = db.conn.prepareStatement(query);
                psDelete.setInt(1, rLine.getInt("serviceId"));
                psDelete.executeUpdate();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void deleteServiceNET(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        PreparedStatement psDelete;
        ResultSet rs;

        String userName;

        String query = "SELECT * FROM ServicesUser WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("serviceId"));
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

                query = "DELETE FROM ServicesUser WHERE id=?";
                psDelete = db.conn.prepareStatement(query);
                psDelete.setInt(1, rLine.getInt("serviceId"));
                psDelete.executeUpdate();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteServiceBOX(JSONObject rLine, String operName, database db) {
        JSONObject rLineDelete;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM ServicesUser WHERE box_id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("serviceId"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (rs.getString("paketType").equals("LINKED_NET")) {
                        rLineDelete = new JSONObject();
                        rLineDelete.put("serviceId", rs.getInt("id"));
                        deleteServiceNET(rLineDelete, operName, db);
                    }
                    if (rs.getString("paketType").equals("LINKED_DTV")) {
                        rLineDelete = new JSONObject();
                        rLine.put("serviceId", rs.getInt("id"));
                        rLine.put("userID", rLine.getInt("userID"));
                        deleteServiceDTV(rLineDelete, operName, db);
                    }

                }
                query = "DELETE FROM ServicesUser WHERE id=?";
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("serviceId"));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static String getDatumIsteka(JSONObject rLine, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM ServicesUser WHERE id=?";
        String datumIsteka = null;
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("serviceID"));
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                if (rs.getString("paketType").equals("BOX")) {
                    datumIsteka = getDatumIstekaBOX(rs.getInt("id"), db);
                }
                if (rs.getString("paketType").equals("DTV")) {
                    datumIsteka = getDatumIstekaDTV(rs.getInt("id"), db);
                }
                if (rs.getString("paketType").equals("NET")) {
                    datumIsteka = getDatumIstekaNET(rs.getInt("id"), db);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datumIsteka;


    }

    private static String getDatumIstekaNET(int id, database db) {
        PreparedStatement ps;
        PreparedStatement psRadius;
        ResultSet rs;
        ResultSet rsRadius;
        String datumIsteka = null;
        String query = "SELECT * FROM ServicesUser WHERE id=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                query = "SELECT value from radcheck WHERE username=? and attribute='Expiration'";
                psRadius = db.connRad.prepareStatement(query);
                psRadius.setString(1, rs.getString("UserName"));
                rsRadius = psRadius.executeQuery();
                if (rsRadius.isBeforeFirst()) {
                    rsRadius.next();
                    datumIsteka = rsRadius.getString("value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime((Date) dtfRadCheck.parseObject(datumIsteka));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        datumIsteka = dtfNormalDate.format(cal.getTime());
        return datumIsteka;

    }

    private static String getDatumIstekaDTV(int id, database db) {
        PreparedStatement ps;
        PreparedStatement psEndDate;
        ResultSet rs;
        ResultSet rsEndDate;
        String datumIsteka = null;
        String query = "SELECT * FROM ServicesUser WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                query = "SELECT endDate FROM DTVKartice WHERE idKartica=? and userID=?";
                psEndDate = db.conn.prepareStatement(query);
                psEndDate.setInt(1, Integer.parseInt(rs.getString("idDTVCard")));
                psEndDate.setInt(2, rs.getInt("userID"));
                rsEndDate = psEndDate.executeQuery();
                if (rsEndDate.isBeforeFirst()) {
                    rsEndDate.next();
                    datumIsteka = rsEndDate.getString("endDate");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datumIsteka;

    }

    private static String getDatumIstekaBOX(int id, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String datumIsteka = null;
        String query = "SELECT * FROM ServicesUser WHERE box_id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (rs.getString("paketType").equals("LINKED_NET")) {
                        datumIsteka = getDatumIstekaNET(rs.getInt("id"), db);
                    }
                    if (rs.getString("paketType").equals("LINKED_DTV")) {
                        datumIsteka = getDatumIstekaDTV(rs.getInt("id"), db);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datumIsteka;

    }


    public static String addService(JSONObject rLine, String operName, database db) {
        PreparedStatement ps;
        Calendar cal = Calendar.getInstance();
        Calendar calZaMesec = Calendar.getInstance();


        try {
            calZaMesec.setTime((Date) dtfMesecZaduzenja.parseObject(rLine.getString("zaMesec")));
        } catch (ParseException e) {
            e.printStackTrace();
            return e.getMessage();
        }


        String query = "INSERT INTO userDebts (id_ServiceUser, id_service, nazivPaketa, datumZaduzenja, userID, popust, " +
                "paketType, cena, uplaceno, datumUplate, dug,  zaduzenOd, zaMesec) VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("id_ServiceUser"));
            ps.setInt(2, rLine.getInt("id_service"));
            ps.setString(3, rLine.getString("nazivPaketa"));
            ps.setString(4, dtfNormalDate.format(cal.getTime()));
            ps.setInt(5, rLine.getInt("userID"));
            ps.setDouble(6, rLine.getDouble("popust"));
            ps.setString(7, rLine.getString("paketType"));
            ps.setDouble(8, rLine.getDouble("cena"));
            ps.setDouble(9, 0.00);
            ps.setString(10, "1000-01-01 00:00:00");
            ps.setDouble(11, valueToPercent.getValue(rLine.getDouble("cena"), rLine.getDouble("popust")));
            ps.setString(12, operName);
            ps.setString(13, dtfMesecZaduzenja.format(calZaMesec.getTime()));
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
            if (rs.isBeforeFirst())
                serviceExist = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serviceExist;

    }
}

