package net.yuvideo.jgemstone.server.classes.DTV;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class DTVPaketFunctions {

  database db;
  private boolean error;
  private String errorMSG;
  private String operName;


  public DTVPaketFunctions(database db, String opername) {
    setOperName(opername);
    this.db = db;
  }

  public boolean deleteDTVPaket(int id) {
    boolean checkUserServices = checkUserDTVNaziv(getNazivPaketa(id));
    if (checkUserServices) {
      setErrorMSG(String.format("Paket ne može biti izbrisan. Promenite paket kod korisnika."));
      setError(true);
      return false;
    }

    boolean checkBox = checkBoxPaketContainDTVPaket(id);
    if (checkBox) {
      String boxName = getBoxName(id);
      setError(true);
      setErrorMSG(
          String.format("Paket ne može biti izbrisan. Paket se nalazi u box Paketu %s", boxName));
      return false;
    }


    boolean deleted = false;
    PreparedStatement ps;
    String query = "DELETE FROM digitalniTVPaketi WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      deleted = true;
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return deleted;
  }

  private String getBoxName(int id) {
    String boxName = "";
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT naziv from paketBox WHERE DTV_id =?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        boxName = rs.getString("naziv");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return boxName;
  }


  private boolean checkBoxPaketContainDTVPaket(int idDTVPaket) {
    boolean exist = false;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM paketBox WHERE DTV_id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idDTVPaket);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        exist = true;
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return exist;
  }

  private String getNazivPaketa(int id) {
    PreparedStatement ps;
    ResultSet rs;
    String naziv = "";
    String query = "SELECT naziv FROM digitalniTVPaketi where id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        naziv = rs.getString("naziv");
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return naziv;

  }

  public void addDTVPaket(JSONObject rLine) {
    boolean naziv = checkDTVPaketNaziv(rLine.getString("naziv"));
    if (naziv) {
      setError(true);
      setErrorMSG(String.format("Paket %s već postoji!", rLine.getString("naziv")));
      return;
    }
    PreparedStatement ps;
    String query =
        "INSERT INTO digitalniTVPaketi (naziv, cena, idPaket, opis, pdv, dodatak, dodatnaKartica) VALUES "
            + "(?, ?, ?, ?, ?, ?, ?)";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, rLine.getString("naziv"));
      ps.setDouble(2, rLine.getDouble("cena"));
      ps.setInt(3, rLine.getInt("idPaket"));
      ps.setString(4, rLine.getString("opis"));
      ps.setDouble(5, rLine.getDouble("pdv"));
      ps.setBoolean(6, rLine.getBoolean("dodatak"));
      ps.setBoolean(7, rLine.getBoolean("dodatnaKartica"));
      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  private boolean checkUserDTVNaziv(String naziv) {
    boolean exist = false;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM servicesUser where nazivPaketa = ? and paketType LIKE '%DTV%'";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, naziv);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        exist = true;
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return exist;

  }

  private boolean checkDTVPaketNaziv(String naziv) {
    boolean exist = false;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT naziv FROM digitalniTVPaketi where naziv=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, naziv);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        exist = true;
      }

      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return exist;
  }

  public void editDTVPaket(JSONObject rLine) {
    PreparedStatement ps;
    String query = "UPDATE digitalniTVPaketi SET cena=?, idPaket=?, opis=?, pdv=?, naziv=?, dodatak=?, dodatnaKartica=? WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setDouble(1, rLine.getDouble("cena"));
      ps.setInt(2, rLine.getInt("idPaket"));
      ps.setString(3, rLine.getString("opis"));
      ps.setDouble(4, rLine.getDouble("pdv"));
      ps.setString(5, rLine.getString("naziv"));
      ps.setBoolean(6, rLine.getBoolean("dodatak"));
      ps.setBoolean(7, rLine.getBoolean("dodatnaKartica"));
      ps.setInt(8, rLine.getInt("id"));

      ps.executeUpdate();
      ps.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


  }


  public JSONObject getAllCards() {
    JSONObject allKartice = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM DTVKartice";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject kartica = new JSONObject();
          kartica.put("endDate", rs.getString("endDate"));
          kartica.put("freezeDate", rs.getString("freezeDate"));
          kartica.put("id", rs.getInt("id"));
          kartica.put("idKartica", rs.getInt("idKartica"));
          kartica.put("paketID", rs.getInt("paketID"));
          kartica.put("userID", rs.getString("userID"));
          allKartice.put(String.valueOf(i), kartica);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();

    }
    return allKartice;
  }

  public JSONObject getCard(int idKartica) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM DTVKartice WHERE idKartica=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idKartica);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        object.put("id", rs.getInt("id"));
        object.put("endDate", rs.getString("endDate"));
        object.put("freezeDate", rs.getString("freezeDate"));
        object.put("idKartica", rs.getInt("idKartica"));
        object.put("paketID", rs.getInt("paketID"));
        object.put("userID", rs.getInt("userID"));
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return object;
  }

  public int getPacketID(int id) {
    int paketID = 0;
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT idPaket FROM digitalniTVPaketi WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        paketID = rs.getInt("idPaket");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return paketID;
  }

  public JSONObject getDTVAddonCards() {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM digitalniTVPaketi WHERE dodatnaKartica=true";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject dtvCard = new JSONObject();
          dtvCard.put("id", rs.getInt("id"));
          dtvCard.put("naziv", rs.getString("naziv"));
          dtvCard.put("cena", rs.getDouble("cena"));
          dtvCard.put("idPaket", rs.getInt("idPaket"));
          dtvCard.put("opis", rs.getString("opis"));
          dtvCard.put("prekoracenje", rs.getInt("prekoracenje"));
          dtvCard.put("pdv", rs.getDouble("pdv"));
          dtvCard.put("dodatak", rs.getBoolean("dodatak"));
          dtvCard.put("dodatnaKartica", rs.getBoolean("dodatnaKartica"));
          object.put(String.valueOf(i), dtvCard);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return object;
  }

  public JSONObject getAllCAS() {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM casPaket";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject cas = new JSONObject();
          cas.put("id", rs.getInt("id"));
          cas.put("code", rs.getInt("code"));
          cas.put("paketID", rs.getInt("paketID"));
          object.put(String.valueOf(i), cas);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
    return object;
  }

  public void addCASCode() {
    PreparedStatement ps;
    String query = "INSERT INTO casPaket set paketID=0, code=0";
    try {
      ps = db.conn.prepareStatement(query);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  public void deleteCASCode(int id) {
    PreparedStatement ps;
    String query = "DELETE FROM casPaket WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public void updateCode(int id, int code) {
    PreparedStatement ps;
    String query = "UPDATE casPaket set code=? where id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, code);
      ps.setInt(2, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }

  public void updatePaketID(int id, int paketID) {
    PreparedStatement ps;
    String query = "UPDATE casPaket set paketID=? where id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, paketID);
      ps.setInt(2, id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public JSONObject getDTVAddons() {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM digitalniTVPaketi WHERE dodatak=true group by idPaket desc";
    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject dtvPaket = new JSONObject();
          dtvPaket.put("id", rs.getString("id"));
          dtvPaket.put("naziv", rs.getString("naziv"));
          dtvPaket.put("cena", rs.getDouble("cena"));
          dtvPaket.put("idPaket", rs.getInt("idPaket"));
          dtvPaket.put("opis", rs.getString("opis"));
          dtvPaket.put("prekoracenje", rs.getInt("prekoracenje"));
          dtvPaket.put("dodatak", rs.getBoolean("dodatak"));
          dtvPaket.put("pdv", rs.getDouble("pdv"));
          object.put(String.valueOf(i), dtvPaket);
          i++;
        }
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    return object;
  }

  public JSONObject getDTVAddon(int id) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM digitalniTVPaketi WHERE dodatak=true and id=? group by idPaket desc";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        object.put("id", rs.getInt("id"));
        object.put("naziv", rs.getString("naziv"));
        object.put("cena", rs.getDouble("cena"));
        object.put("idPaket", rs.getInt("idPaket"));
        object.put("opis", rs.getString("opis"));
        object.put("prekoracenje", rs.getInt("prekoracenje"));
        object.put("pdv", rs.getDouble("pdv"));
        object.put("dodatak", rs.getBoolean("dodatak"));
        object.put("dodatnaKartica", rs.getBoolean("dodatnaKartica"));
      }
      ps.close();
      rs.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    return object;
  }

  public DTVPaketData getDTVAddonData(int id) {
    JSONObject addonPaket = getDTVAddon(id);
    DTVPaketData paket = new DTVPaketData();
    paket.setId(addonPaket.getInt("id"));
    paket.setNaziv(addonPaket.getString("naziv"));
    paket.setCena(addonPaket.getDouble("cena"));
    paket.setIdPaket(addonPaket.getInt("idPaket"));
    paket.setOpis(addonPaket.getString("opis"));
    paket.setPrekoracenje(addonPaket.getInt("prekoracenje"));
    paket.setPdv(addonPaket.getDouble("pdv"));
    paket.setDodatak(addonPaket.getBoolean("dodatak"));
    paket.setDodatnaKartica(addonPaket.getBoolean("dodatnaKartica"));
    return paket;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }
}
