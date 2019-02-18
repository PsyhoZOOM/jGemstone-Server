package net.yuvideo.jgemstone.server.classes.Ugovori;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;

public class Ugovori {
  boolean error;
  String errorMSG;
  database db;
  String operName;
  boolean exist= false;

  public Ugovori(database db, String operName) {
    this.db = db;
    this.operName = operName;
  }

  public void deleteUserUgovor(int id, int userID, String brojUgovora){
    boolean serviceExist = checkIfUgovorIsConnectedToServices(userID, brojUgovora);
    if (serviceExist){
      exist =true;
      return;
    }
    else {
      PreparedStatement ps;
      String query = "DELETE FROM ugovori_korisnik WHERE id=?";
      try {
        ps =db.conn.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        setErrorMSG(e.getMessage());
        setError(true);
        e.printStackTrace();
      }
    }

  }

  private boolean checkIfUgovorIsConnectedToServices(int userID,
      String brojUgovora) {
    boolean exist=false;
    String query = "SELECT * FROM servicesUser WHERE userID=? and brojUgovora=?";
    try {
      PreparedStatement ps= db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      ps.setString(2, brojUgovora);
      ResultSet rs  = ps.executeQuery();
      if (rs.isBeforeFirst())
        exist = true;
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return exist;

  }


  public boolean isExist() {
    return exist;
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
