package net.yuvideo.jgemstone.server.classes.USERS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import net.yuvideo.jgemstone.server.classes.MESTA.MestaFuncitons;
import net.yuvideo.jgemstone.server.classes.RACUNI.UserRacun;
import net.yuvideo.jgemstone.server.classes.SERVICES.ServicesFunctions;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class UserFunc {

  private database db;
  private String operName;
  private boolean error;
  private String errorMSG;


  public UserFunc(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }


  public void deleteUser(int userId) {

    String query;
    PreparedStatement ps = null;

    ServicesFunctions servicesFunctions = new ServicesFunctions(db, operName);

    query = "SELECT id from servicesUser WHERE userID=?";
    try {
      ps= db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.isBeforeFirst()){
        while (rs.next()){
          servicesFunctions.deleteService(rs.getInt("id"), userId);
        }
      }
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }


    //delete from Services_user
    query = "DELETE FROM servicesUser  WHERE  userID=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());

      e.printStackTrace();
    }

    query = "DELETE FROM ugovori_korisnik WHERE userID=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    query = "DELETE FROM zaduzenja  WHERE userID=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    query =  "DELETE FROM uplate WHERE userID=?";
    try {
      ps =db.conn.prepareStatement(query);
      ps.executeUpdate();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

    query = "DELETE FROM users WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userId);
      ps.executeUpdate();
    } catch (Exception e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }

    try {
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }


  }


  public JSONObject getAllUsers(JSONObject rLine) {
    String query = "SELECT * FROM users WHERE  ime LIKE ? or id LIKE ? or jBroj LIKE ? or nazivFirme LIKE ? or mesto LIKE ? ";
    PreparedStatement ps;
    ResultSet rs = null;
    String userSearch;
    if (!rLine.has("username")) {
      userSearch = "%";
    } else {
      userSearch = "%" + rLine.getString("username") + "%";
    }
    if (rLine.has("advancedSearch")) {
      try {
        ps = db.conn.prepareStatement(rLine.getString("advancedSearch"));
        rs = ps.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      try {
        ps = db.conn.prepareStatement(query);
        ps.setString(1, userSearch);
        ps.setString(2, userSearch);
        ps.setString(3, userSearch);
        ps.setString(4, userSearch);
        ps.setString(5, userSearch);
        rs = ps.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    JSONObject jUsers = new JSONObject();
    JSONObject jObj;

    int i = 0;
    try {
      UserRacun userRacun = new UserRacun(db, operName);
      while (rs.next()) {

        try {
          jObj = new JSONObject();
          jObj.put("dug", userRacun.getUkupanDug(rs.getInt("id")));
          jObj.put("id", rs.getInt("id"));
          jObj.put("fullName", rs.getString("ime"));
          jObj.put("mesto", rs.getString("mesto"));
          jObj.put("adresa", (rs.getString("adresa")));
          jObj.put("adresaRacuna", rs.getString("adresaRacuna"));
          jObj.put("mestoRacuna", rs.getString("mestoRacuna"));
          jObj.put("brLk", rs.getString("brlk"));
          jObj.put("datumRodjenja", rs.getString("datumRodjenja"));
          jObj.put("telFixni", rs.getString("telFiksni"));
          jObj.put("telMobilni", rs.getString("telMobilni"));
          jObj.put("JMBG", rs.getString("JMBG"));
          jObj.put("komentar", rs.getString("komentar"));
          jObj.put("postBr", rs.getString("postbr"));
          jObj.put("jBroj", rs.getString("jBroj"));
          jObj.put("jAdresa", rs.getString("jAdresa"));
          jObj.put("jAdresaBroj", rs.getString("jAdresaBroj"));
          jObj.put("jMesto", rs.getString("jMesto"));
          MestaFuncitons mestaFuncitons = new MestaFuncitons(db);
          jObj.put("adresaUsluge",
              mestaFuncitons.getNazivAdrese(rs.getString("jMesto"), rs.getString("jAdresa")));
          jObj.put("mestoUsluge", mestaFuncitons.getNazivMesta(rs.getString("jMesto")));

          //FIRMA
          jObj.put("firma", rs.getBoolean("firma"));
          jObj.put("nazivFirme", rs.getString("nazivFirme"));
          jObj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
          jObj.put("kontaktOsobaTel", rs.getString("kontaktOsobaTel"));
          jObj.put("kodBanke", rs.getString("kodBanke"));
          jObj.put("PIB", rs.getString("PIB"));
          jObj.put("maticniBroj", rs.getString("maticniBroj"));
          jObj.put("tekuciRacun", rs.getString("tekuciRacun"));
          jObj.put("fax", rs.getString("fax"));
          jObj.put("adresaFirme", rs.getString("adresaFirme"));
          jObj.put("mestoFirme", rs.getString("mestoFirme"));
          jObj.put("email", rs.getString("email"));
          jObj.put("datumKreiranja", rs.getString("datumKreiranja"));

          //provera advancesSearch  filter da li postoji servis
          if (rLine.has("advancedSearch") && rLine.has("chkNET") || rLine.has("chkKTV") || rLine.has("chkIPTV") || rLine.has("chkFIX")){
            boolean haveService=false;


            ServicesFunctions servicesFunctions = new ServicesFunctions(db, operName);

              haveService = servicesFunctions.isUserHaveGlobalService(rLine,  rs.getInt("id"));

            if (haveService){
              jUsers.put(String.valueOf(i), jObj);
            }else continue;



          }else {
            jUsers.put(String.valueOf(i), jObj);
          }
          i++;
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return jUsers;
  }

  public JSONObject getUplateUser(int userID) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM uplate WHERE userID=? ORDER BY id DESC";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject uplate = new JSONObject();
          uplate.put("id", rs.getInt("id"));
          uplate.put("userID", rs.getInt("userID"));
          uplate.put("datum", rs.getString("datum"));
          uplate.put("duguje", rs.getDouble("duguje"));
          uplate.put("potrazuje", rs.getDouble("potrazuje"));
          uplate.put("operater", rs.getString("operater"));
          uplate.put("opis", rs.getString("opis"));
          object.put(String.valueOf(i), uplate);
          i++;
        }
      }

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return object;
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
}
