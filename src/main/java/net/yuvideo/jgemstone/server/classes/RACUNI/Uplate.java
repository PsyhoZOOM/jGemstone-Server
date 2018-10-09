package net.yuvideo.jgemstone.server.classes.RACUNI;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.yuvideo.jgemstone.server.classes.database;

public class Uplate {

  private database db;
  private String operName;
  private String errorMSG;
  private boolean error;
  private SimpleDateFormat date_format_full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


  public Uplate(String operName, database db) {
    this.db = db;
    this.operName = operName;
  }

  public void novaUplata(int userID, double uplaceno, String opis) {
    PreparedStatement ps;
    String query = "INSERT INTO uplate (datum,  duguje, operater, opis, userID)"
        + "VALUES "
        + "(?,?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      ps.setDouble(2, uplaceno);
      ps.setString(3, operName);
      ps.setString(4, opis);
      ps.setInt(5, userID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }

  public void deleteUplata(int idUplate) {
    PreparedStatement ps;
    String query = "DELETE FROM uplate WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, idUplate);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());

      e.printStackTrace();
    }
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
