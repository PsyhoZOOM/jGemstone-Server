package net.yuvideo.jgemstone.server.classes.SERVICES;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class ServiceData {

  private database db;

  private int id;
  private int id_service;
  private int box_id;
  private int userID;
  private int produzenje;
  private int DTVPaket;
  private int fiksnaTelPaketID;
  private int dtv_main;

  private double popust;
  private double cena;
  private double pdv;

  private boolean obracun;
  private boolean aktivan;
  private boolean newService;
  private boolean linkedService;
  private boolean boxService;
  private boolean error;

  private String nazivPaketa;
  private String date_added;
  private String operName;
  private String brojUgovora;
  private String dateActivated;
  private String idDTVCard;
  private String userName;
  private String groupName;
  private String fiksnaTel;
  private String iptvMac;
  private String paketType;
  private String endDate;
  private String opis;
  private String komentar;
  private String errorMSG;

  public ServiceData(database db, String operName) {
    this.db = db;
    this.operName = operName;

  }

  /**
   * Setting database
   */
  public ServiceData(database db) {
    this.db = db;
  }


  public ServiceData() {

  }

  /**
   * Getting service data from database servicesUsers
   *
   * @param serviceID integer
   * @return ServiceData
   */
  public ServiceData getData(int serviceID) {
    ServiceData serviceData = new ServiceData();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser WHERE ID=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, serviceID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        serviceData.setId(rs.getInt("id"));
        serviceData.setId_service(rs.getInt("id_service"));
        serviceData.setNazivPaketa(rs.getString("nazivPaketa"));
        serviceData.setDate_added(rs.getString("date_added"));
        serviceData.setDtv_main(rs.getInt("dtv_main"));
        serviceData.setUserID(rs.getInt("userID"));
        serviceData.setOperName(rs.getString("operName"));
        serviceData.setPopust(rs.getDouble("popust"));
        serviceData.setCena(rs.getDouble("cena"));
        serviceData.setObracun(rs.getBoolean("obracun"));
        serviceData.setBrojUgovora(rs.getString("brojUgovora"));
        serviceData.setAktivan(rs.getBoolean("aktivan"));
        serviceData.setDateActivated(rs.getString("date_activated"));
        serviceData.setProduzenje(rs.getInt("produzenje"));
        serviceData.setNewService(rs.getBoolean("newService"));
        serviceData.setIdDTVCard(rs.getString("idDTVCard"));
        serviceData.setUserName(rs.getString("UserName"));
        serviceData.setGroupName(rs.getString("GroupName"));
        serviceData.setFiksnaTel(rs.getString("FIKSNA_TEL"));
        serviceData.setFiksnaTelPaketID(rs.getInt("FIKSNA_TEL_PAKET_ID"));
        serviceData.setIptvMac(rs.getString("IPTV_MAC"));
        serviceData.setLinkedService(rs.getBoolean("linkedService"));
        serviceData.setBoxService(rs.getBoolean("BOX_service"));
        serviceData.setPaketType(rs.getString("paketType"));
        serviceData.setEndDate(rs.getString("endDate"));
        serviceData.setPdv(rs.getDouble("PDV"));
        serviceData.setOpis(rs.getString("opis"));
        serviceData.setKomentar(rs.getString("komentar"));

      }
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return serviceData;
  }

  public JSONObject getDataJSON(int serviceID) {
    ServiceData data = getData(serviceID);
    JSONObject object = new JSONObject();
    object.put("id", data.getId());
    object.put("id_service", data.getId_service());
    object.put("date_added", data.getDate_added());
    object.put("nazivPaketa", data.getNazivPaketa());
    object.put("dtv_main", data.getDtv_main());
    object.put("userID", data.getUserID());
    object.put("operName", data.getOperName());
    object.put("popust", data.getPopust());
    object.put("cena", data.getCena());
    object.put("obracun", data.isObracun());
    object.put("brojUgovora", data.getBrojUgovora());
    object.put("aktivan", data.isAktivan());
    object.put("date_activated", data.getDateActivated());
    object.put("produzenje", data.getProduzenje());
    object.put("newService", data.isNewService());
    object.put("idDTVCard", data.getIdDTVCard());
    object.put("userName", data.getUserName());
    object.put("groupName", data.getGroupName());
    object.put("FIKSNA_TEL", data.getFiksnaTel());
    object.put("FIKSNA_TEL_PAKET_ID", data.getFiksnaTelPaketID());
    object.put("IPTV_MAC", data.getIptvMac());
    object.put("linkedService", data.isLinkedService());
    object.put("BOX_service", data.isBoxService());
    object.put("paketType", data.getPaketType());
    object.put("endDate", data.getEndDate());
    object.put("pdv", data.getPdv());
    object.put("opis", data.getOpis());
    object.put("komentar", data.getKomentar());

    return object;


  }


  public int getDtv_main() {
    return dtv_main;
  }

  public void setDtv_main(int dtv_main) {
    this.dtv_main = dtv_main;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId_service() {
    return id_service;
  }

  public void setId_service(int id_service) {
    this.id_service = id_service;
  }

  public int getBox_id() {
    return box_id;
  }

  public void setBox_id(int box_id) {
    this.box_id = box_id;
  }

  public int getUserID() {
    return userID;
  }

  public void setUserID(int userID) {
    this.userID = userID;
  }

  public int getProduzenje() {
    return produzenje;
  }

  public void setProduzenje(int produzenje) {
    this.produzenje = produzenje;
  }

  public int getDTVPaket() {
    return DTVPaket;
  }

  public void setDTVPaket(int DTVPaket) {
    this.DTVPaket = DTVPaket;
  }

  public int getFiksnaTelPaketID() {
    return fiksnaTelPaketID;
  }

  public void setFiksnaTelPaketID(int fiksnaTelPaketID) {
    this.fiksnaTelPaketID = fiksnaTelPaketID;
  }

  public double getPopust() {
    return popust;
  }

  public void setPopust(double popust) {
    this.popust = popust;
  }

  public double getCena() {
    return cena;
  }

  public void setCena(double cena) {
    this.cena = cena;
  }

  public double getPdv() {
    return pdv;
  }

  public void setPdv(double pdv) {
    this.pdv = pdv;
  }

  public boolean isObracun() {
    return obracun;
  }

  public void setObracun(boolean obracun) {
    this.obracun = obracun;
  }

  public boolean isAktivan() {
    return aktivan;
  }

  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }

  public boolean isNewService() {
    return newService;
  }

  public void setNewService(boolean newService) {
    this.newService = newService;
  }

  public boolean isLinkedService() {
    return linkedService;
  }

  public void setLinkedService(boolean linkedService) {
    this.linkedService = linkedService;
  }

  public boolean isBoxService() {
    return boxService;
  }

  public void setBoxService(boolean boxService) {
    this.boxService = boxService;
  }

  public String getNazivPaketa() {
    return nazivPaketa;
  }

  public void setNazivPaketa(String nazivPaketa) {
    this.nazivPaketa = nazivPaketa;
  }

  public String getDate_added() {
    return date_added;
  }

  public void setDate_added(String date_added) {
    this.date_added = date_added;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getBrojUgovora() {
    return brojUgovora;
  }

  public void setBrojUgovora(String brojUgovora) {
    this.brojUgovora = brojUgovora;
  }

  public String getDateActivated() {
    return dateActivated;
  }

  public void setDateActivated(String dateActivated) {
    this.dateActivated = dateActivated;
  }

  public String getIdDTVCard() {
    return idDTVCard;
  }

  public void setIdDTVCard(String idDTVCard) {
    this.idDTVCard = idDTVCard;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getFiksnaTel() {
    return fiksnaTel;
  }

  public void setFiksnaTel(String fiksnaTel) {
    this.fiksnaTel = fiksnaTel;
  }

  public String getIptvMac() {
    return iptvMac;
  }

  public void setIptvMac(String iptvMac) {
    this.iptvMac = iptvMac;
  }

  public String getPaketType() {
    return paketType;
  }

  public void setPaketType(String paketType) {
    this.paketType = paketType;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getOpis() {
    return opis;
  }

  public void setOpis(String opis) {
    this.opis = opis;
  }

  public String getKomentar() {
    return komentar;
  }

  public void setKomentar(String komentar) {
    this.komentar = komentar;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }
}
