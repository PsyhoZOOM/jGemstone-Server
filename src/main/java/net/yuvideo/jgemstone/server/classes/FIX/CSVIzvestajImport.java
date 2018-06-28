package net.yuvideo.jgemstone.server.classes.FIX;

import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class CSVIzvestajImport {

  private database db;
  private boolean error = false;
  private String errorMsg;

  public CSVIzvestajImport(database db) {
    this.db = db;
  }

  private boolean checkIfExist(String filename) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT fileName from csv where fileName=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, filename);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        ps.close();
        rs.close();
        System.out.println(String.format("%s postoji", filename));
        return true;
      }
    } catch (SQLException e) {
      errorMsg = e.getMessage();
      error = true;
      e.printStackTrace();
    }
    return false;
  }

  public void importFile(JSONObject rLine) {
    JSONObject jObj = new JSONObject();
    CsvReader csvReader = null;
    PreparedStatement ps = null;
    String query =
        "INSERT INTO csv (account,  `from`, `to`, country, description, connectTime, chargedTimeMS, "
            + "chargedTimeS, chargedAmountRSD, serviceName, chargedQuantity, serviceUnit, customerID, fileName)"
            + "VALUES"
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    for (String key : rLine.keySet()) {
      //ako postoji fajl  u bazi onda nema potrebe za importom, nastavljamo dalje
      if (checkIfExist(key)) {
        return;
      }
      try {
        csvReader = new CsvReader(new StringReader((String) rLine.getString(key)));
        csvReader.setDelimiter(',');
        csvReader.readHeaders();
        ps = db.conn.prepareStatement(query);

        while (csvReader.readRecord()) {
          //ako je csv fajl  na kraju prekinuti import
          if (csvReader.get("Account").equals("SUBTOTAL") || csvReader.get("Account").isEmpty()
              || csvReader.get("Service Name").equals("Payments")) {
            break;
          }
          if (Double.parseDouble(csvReader.get("Charged Amount, RSD")) < 0) {
            continue;
          }
          String filename = key;
          String customerID = key.substring(key.lastIndexOf("-"));
          customerID = customerID.replace("-customer", "");
          customerID = customerID.replace(".csv", "");

          ps.setString(1, csvReader.get("Account"));
          ps.setString(2, csvReader.get("From"));
          ps.setString(3, csvReader.get("To"));
          if (csvReader.get("Country").isEmpty()) {
            ps.setString(4, "Lokalni poziv");
          } else {
            ps.setString(4, csvReader.get("Country"));
          }
          ps.setString(5, csvReader.get("Description"));
          ps.setString(6, csvReader.get("Connect Time"));
          ps.setString(7, csvReader.get("Charged Time, min:sec"));
          ps.setInt(8, Integer.parseInt(csvReader.get("Charged Time, sec.")));
          ps.setDouble(9, Double.parseDouble(csvReader.get("Charged Amount, RSD")));
          ps.setString(10, csvReader.get("Service Name"));
          ps.setInt(11, Integer.parseInt(csvReader.get("Charged quantity")));
          ps.setString(12, csvReader.get("Service unit"));
          ps.setString(13, customerID);
          ps.setString(14, filename);

          ps.executeUpdate();


        }

        ps.close();
        error = false;
      } catch (FileNotFoundException e) {
        error = true;
        errorMsg = e.getMessage();
        e.printStackTrace();
      } catch (SQLException e) {
        errorMsg = e.getMessage();
        error = true;
        e.printStackTrace();
      } catch (IOException e) {
        errorMsg = e.getMessage();
        error = true;
        e.printStackTrace();
      }

    }
  }


  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
}
