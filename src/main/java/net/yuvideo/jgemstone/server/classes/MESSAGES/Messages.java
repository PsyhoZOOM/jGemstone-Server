package net.yuvideo.jgemstone.server.classes.MESSAGES;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class Messages {

  int sourceID;
  int destinationID;
  String source;
  String destination;
  String message;
  String date;
  Image img;
  boolean error;
  String errorMSG;
  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
  database db;
  private int id;

  public Messages(database db) {
    this.db = db;
  }


  public JSONObject getMessageForUser(int userID, boolean readAll) {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query;
    if (readAll) {
      query = "SELECT * FROM Messages  WHERE  destination = ? GROUP BY source ORDER by time desc limit 100";
    } else {
      query = "SELECT * FROM Messages WHERE destination =? and isRead = ? GROUP BY source ORDER BY time DESC LIMIT 100";
    }
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, userID);
      if (readAll) {
        ps.setBoolean(2, readAll);
      }

      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject mess = new JSONObject();
          mess.put("id", rs.getInt("id"));
          mess.put("sourceID", rs.getInt("source"));
          mess.put("sourceName", getOperName(rs.getInt("source")));
          mess.put("sourceImage", getOperImage(rs.getInt("source")));
          mess.put("destinationID", rs.getInt("destination"));
          mess.put("destinationName", getOperName(rs.getInt("destination")));
          mess.put("destinationImage", getOperImage(rs.getInt("destination")));
          mess.put("time", rs.getString("time"));
          object.put(String.valueOf(i), mess);
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

  private void sendMessage(int source, int destination, String message) {
    PreparedStatement ps;
    String query = "INSERT INTO Messages (source, destination, time, message) VALUES (?,?,?,?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, source);
      ps.setInt(2, destination);
      ps.setString(3, LocalDateTime.now().format(dtf));
      ps.setString(4, message);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }

  private Blob getOperImage(int id) {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT imaget FROM operater where id=?";
    Blob image = null;
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        image = rs.getBlob("image");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return image;
  }

  private String getOperName(int id) {
    String username = "";
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT username FROM operater WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, id);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        username = rs.getString("username");
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());

      e.printStackTrace();
    }
    return username;
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getSourceID() {
    return sourceID;
  }

  public void setSourceID(int sourceID) {
    this.sourceID = sourceID;
  }

  public int getDestinationID() {
    return destinationID;
  }

  public void setDestinationID(int destinationID) {
    this.destinationID = destinationID;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Image getImg() {
    return img;
  }

  public void setImg(Image img) {
    this.img = img;
  }

  public database getDb() {
    return db;
  }

  public void setDb(database db) {
    this.db = db;
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
