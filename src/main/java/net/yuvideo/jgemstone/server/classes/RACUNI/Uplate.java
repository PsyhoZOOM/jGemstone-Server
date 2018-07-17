package net.yuvideo.jgemstone.server.classes.RACUNI;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServiceData;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.SQL.UserDebts;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class Uplate {

  private database db;
  private String operName;
  private String errorMSG;
  private boolean error;
  private final SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


  public Uplate(String operName, database db) {
    this.db = db;
    this.operName = operName;
  }

  public void uplati(int idUsluge, double uplaceno, String mestoUplate) {

    UserDebts debt = getDebt(idUsluge);
    UserDebts debtSaobracaj = new UserDebts();
    BigDecimal uplacenoBig = new BigDecimal(String.valueOf(uplaceno));

    if (debt.getPaketType().equals("FIX")) {
      debtSaobracaj = getFixSaobracaj(debt.getId_ServiceUser());
      uplatiFixUslugu(debt, debtSaobracaj, uplacenoBig);
      if (isError()) {
        return;
      }
    } else if (debt.getPaketType().equals("BOX")) {
      boolean haveFix = checkIfHaveFix(debt.getId_ServiceUser());
      if (isError()) {
        return;
      }
      if (haveFix) {
        debtSaobracaj = getFixSaobracaj(debt.getId_ServiceUser());
        if (isError()) {
          return;
        }

        uplatiFixUslugu(debt, debtSaobracaj, new BigDecimal(String.valueOf(uplaceno)));
        if (isError()) {
          return;
        }

      }
    } else {
      uplatiUslugu(debt, uplaceno);
      if (isError()) {
        return;
      }
    }

    logUplate(debt, uplaceno, mestoUplate);
    ServicesFunctions servicesFunction = new ServicesFunctions(db);
    ServiceData serviceData = servicesFunction.getServiceData(debt.getId_ServiceUser());
    if (serviceData.isBoxService()) {
      servicesFunction.activateBoxService(serviceData.getId(), serviceData.getEndDate(),
          serviceData.getProduzenje(), getOperName());
      if (servicesFunction.isError()) {
        setError(true);
        setErrorMSG(servicesFunction.getErrorMSG());
        return;
      }

    } else {
      if (serviceData.getPaketType() != null) {
        servicesFunction.activateService(serviceData.getId(), getOperName());
        servicesFunction.produziService(serviceData.getId(), serviceData.getEndDate(),
            serviceData.getProduzenje(), getOperName(), debt.isSkipProduzenje());
      }
      if (servicesFunction.isError()) {
        setError(true);
        setErrorMSG(servicesFunction.getErrorMSG());
        return;
      }

    }


  }

  private void uplatiUslugu(UserDebts userDebts, double uplaceno) {
    uplaceno += userDebts.getUplaceno();
    PreparedStatement ps;
    String query = "UPDATE userDebts SET uplaceno =? WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setDouble(1, uplaceno);
      ps.setInt(2, userDebts.getId());
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }

  private boolean checkIfHaveFix(int box_id) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT FIKSNA_TEL FROM servicesUser WHERE box_id=?";
    boolean haveBox = false;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, box_id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          String fiksnaTel = rs.getString("FIKSNA_TEL").trim();
          if (!fiksnaTel.isEmpty() || fiksnaTel != null) {
            haveBox = true;
          }
        }
      }
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return haveBox;

  }

  private UserDebts getFixSaobracaj(int idService) {
    PreparedStatement ps;
    ResultSet rs;
    UserDebts userDebtsFixSaobracaj = new UserDebts();
    String query = "SELECT * FROM userDebts WHERE nazivPaketa LIKE 'Saobra%' and id_ServiceUser=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idService);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        userDebtsFixSaobracaj.setId(rs.getInt("id"));
        userDebtsFixSaobracaj.setId_ServiceUser(rs.getInt("id_ServiceUser"));
        userDebtsFixSaobracaj.setDatumZaduzenja(rs.getString("datumZaduzenja"));
        userDebtsFixSaobracaj.setUserID(rs.getInt("userID"));
        userDebtsFixSaobracaj.setPopust(rs.getDouble("popust"));
        userDebtsFixSaobracaj.setPaketType(rs.getString("paketType"));
        userDebtsFixSaobracaj.setCena(rs.getDouble("cena"));
        userDebtsFixSaobracaj.setUplaceno(rs.getDouble("uplaceno"));
        userDebtsFixSaobracaj.setDatumUplate(rs.getString("datumUplate"));
        userDebtsFixSaobracaj.setDug(rs.getDouble("dug"));
        userDebtsFixSaobracaj.setOperater(rs.getString("operater"));
        userDebtsFixSaobracaj.setZaduzenOd(rs.getString("zaduzenOd"));
        userDebtsFixSaobracaj.setZaMesec(rs.getString("zaMesec"));
        userDebtsFixSaobracaj.setSkipProduzenje(rs.getBoolean("skipProduzenje"));
        userDebtsFixSaobracaj.setPdv(rs.getDouble("PDV"));
        userDebtsFixSaobracaj.setKolicina(rs.getInt("kolicina"));
        userDebtsFixSaobracaj.setjMere(rs.getString("jMere"));

      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
      return null;
    }
    return userDebtsFixSaobracaj;
  }

  private void uplatiFixUslugu(UserDebts debtPaket, UserDebts debtSaobracaj, BigDecimal uplaceno) {
    double insertUplacenoPaket;
    double inserTuplacenoSaobracaj;
    BigDecimal zaUplatuPaket = new BigDecimal(String.valueOf(debtPaket.getDug()));

    BigDecimal ukupnoUplaceno = new BigDecimal(String.valueOf(debtPaket.getUplaceno()));
    ukupnoUplaceno = ukupnoUplaceno
        .add(new BigDecimal(String.valueOf(debtSaobracaj.getUplaceno())));
    ukupnoUplaceno = ukupnoUplaceno.add(uplaceno);

    if (ukupnoUplaceno.doubleValue() >= zaUplatuPaket.doubleValue()) {
      ukupnoUplaceno = ukupnoUplaceno.subtract(zaUplatuPaket);
      insertUplacenoPaket = zaUplatuPaket.doubleValue();
      inserTuplacenoSaobracaj = ukupnoUplaceno.doubleValue();
    } else {
      insertUplacenoPaket = ukupnoUplaceno.doubleValue();
      inserTuplacenoSaobracaj = 0;
    }

    PreparedStatement ps;
    String query = "UPDATE userDebts set uplaceno =? where id=?";
    //UPDATE PAKET FIX UPLACENO
    try {
      ps = db.conn.prepareStatement(query);
      ps.setDouble(1, insertUplacenoPaket);
      ps.setInt(2, debtPaket.getId());
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
      return;
    }

    //UPADTE PAKET FIX SAOBRACAJ UPLACENO
    try {
      ps = db.conn.prepareStatement(query);
      ps.setDouble(1, inserTuplacenoSaobracaj);
      ps.setInt(2, debtSaobracaj.getId());
      if (debtSaobracaj.getId() != 0) {
        ps.executeUpdate();
      }
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
      return;
    }

  }


  private UserDebts getDebt(int idUsluge) {
    UserDebts userDebts = new UserDebts();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM userDebts WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idUsluge);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        userDebts.setId(rs.getInt("id"));
        userDebts.setId_ServiceUser(rs.getInt("id_ServiceUser"));
        userDebts.setNazivPaketa(rs.getString("nazivPaketa"));
        userDebts.setDatumZaduzenja(rs.getString("datumZaduzenja"));
        userDebts.setUserID(rs.getInt("userID"));
        userDebts.setPopust(rs.getDouble("popust"));
        userDebts.setPaketType(rs.getString("paketType"));
        userDebts.setCena(rs.getDouble("cena"));
        userDebts.setUplaceno(rs.getDouble("uplaceno"));
        userDebts.setDatumUplate(rs.getString("datumUplate"));
        userDebts.setDug(rs.getDouble("dug"));
        userDebts.setOperater(rs.getString("operater"));
        userDebts.setZaduzenOd(rs.getString("zaduzenOd"));
        userDebts.setZaMesec(rs.getString("zaMesec"));
        userDebts.setSkipProduzenje(rs.getBoolean("skipProduzenje"));
        userDebts.setPdv(rs.getDouble("PDV"));
        userDebts.setKolicina(rs.getInt("kolicina"));
        userDebts.setjMere(rs.getString("jMere"));
      }
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
      return null;
    }
    return userDebts;
  }


  private void logUplate(UserDebts userDebts, double uplaceno, String mestoUplate) {
    //UPLATA LOG SVAKE UPLATE
    JSONObject logUplate = new JSONObject();
    logUplate.put("uplaceno", uplaceno);
    logUplate.put("id", userDebts.getId());
    logUplate.put("nazivPaketa", userDebts.getNazivPaketa());
    logUplate.put("operater", getOperName());
    logUplate.put("userID", userDebts.getUserID());
    logUplate.put("id_ServiceUser", userDebts.getId_ServiceUser());
    logUplate.put("mestoUplate", mestoUplate);
    logUplate.put("zaMesec", userDebts.getZaMesec());

    ServicesFunctions.uplataLOG(logUplate, db);
  }


  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

}
