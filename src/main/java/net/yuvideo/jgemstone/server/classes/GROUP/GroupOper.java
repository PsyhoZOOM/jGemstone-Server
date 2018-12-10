package net.yuvideo.jgemstone.server.classes.GROUP;

import com.sun.jersey.json.impl.provider.entity.JSONObjectProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.RestoreAction;
import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

public class GroupOper {

  int id;
  String groupName;
  int oper;

  database db;
  boolean error;
  String errorMSG;

  ArrayList<GroupOper> groupOperArrayList = new ArrayList<>();
  JSONObject groupJSON = new JSONObject();


  public GroupOper() {
  }

  public GroupOper(database db) {
    this.db = db;
    getGroups();
  }

  private void getGroups() {
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM groups group by groupName";

    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        while (rs.next()) {
          GroupOper group = new GroupOper();
          group.setId(rs.getInt("id"));
          group.setGroupName(rs.getString("groupName"));
          groupOperArrayList.add(group);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    getJSONGroup();

  }

  private JSONObject getJSONGroup() {
    for (int i = 0; i < groupOperArrayList.size(); i++) {
      JSONObject object = new JSONObject();
      object.put("id", groupOperArrayList.get(i).getId());
      object.put("groupName", groupOperArrayList.get(i).getGroupName());
      object.put("operaters", getJSONGroupOpers(groupOperArrayList.get(i).getGroupName()));
      groupJSON.put(String.valueOf(i), object);

    }
    return groupJSON;

  }

  private JSONObject getJSONGroupOpers(String groupName) {
    JSONObject operaters = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM groups WHERE groupName=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, groupName);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject object = new JSONObject();
          object.put("operID", rs.getInt("id"));
          operaters.put(String.valueOf(i), object);
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

    return operaters;
  }

  private String getOperName(int operater) {
    String operUserName = "";
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT username FROM operateri WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, operater);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        rs.next();
        operUserName = rs.getString("username");
      }
      ps.close();
      rs.close();

    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
    return operUserName;
  }

  public JSONObject getAllOperaters() {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM operateri";

    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject oper = new JSONObject();
          oper.put("id", rs.getInt("id"));
          oper.put("username", rs.getString("username"));
          oper.put("adresa", rs.getString("adresa"));
          oper.put("telefon", rs.getString("telefon"));
          oper.put("komentar", rs.getString("komentar"));
          oper.put("aktivan", rs.getBoolean("aktivan"));
          oper.put("ime", rs.getString("ime"));
          oper.put("type", rs.getString("type"));
          oper.put("typeNo", rs.getInt("typeNo"));
          object.put(String.valueOf(i), oper);
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

  public JSONObject getAvOpers() {
    JSONObject object = new JSONObject();
    PreparedStatement ps;
    ResultSet rs;
    String query = "SELECT * FROM operateri WHERE operGroup NOT IN (select id FROM groups)";

    try {
      ps = db.conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.isBeforeFirst()) {
        int i = 0;
        while (rs.next()) {
          JSONObject oper = new JSONObject();
          oper.put("id", rs.getInt("id"));
          oper.put("username", rs.getString("username"));
          object.put(String.valueOf(i), oper);
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


  public JSONObject getGroupOperaters(int groupID) {
    PreparedStatement ps;
    ResultSet rs;
    JSONObject  opers = new JSONObject();
    String query = "SELECT * FROM operateri WHERE operGroup=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, groupID);
      rs = ps.executeQuery();
      if(rs.isBeforeFirst()){
        int i =0;
        while (rs.next()){
          JSONObject oper = new JSONObject();
          oper.put("id", rs.getInt("id"));
          oper.put("username", rs.getString("username"));
          opers.put(String.valueOf(i), oper);
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
    return opers;
  }

  public void addOperToGroup(int groupID, int operID) {
    PreparedStatement ps;
    String query = "UPDATE operateri set operGroup=? WHERE id=?";

    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, groupID);
      ps.setInt(2, operID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }

  }

  public void removeOperaterFromGroup(int operID) {
    PreparedStatement ps;
    String query = "UPDATE operateri set operGroup=0 WHERE id=?";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setInt(1, operID);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setErrorMSG(e.getMessage());
      setError(true);
      e.printStackTrace();
    }
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public int getOper() {
    return oper;
  }

  public void setOper(int oper) {
    this.oper = oper;
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

  public ArrayList<GroupOper> getGroupOperArrayList() {
    return groupOperArrayList;
  }

  public void setGroupOperArrayList(
      ArrayList<GroupOper> groupOperArrayList) {
    this.groupOperArrayList = groupOperArrayList;
  }

  public JSONObject getGroupJSON() {
    return groupJSON;
  }

  public void setGroupJSON(JSONObject groupJSON) {
    this.groupJSON = groupJSON;
  }


  public void addGroup(String groupName) {
    PreparedStatement ps;
    String query = "INSERT INTO groups (groupName) VALUES (?)";
    try {
      ps = db.conn.prepareStatement(query);
      ps.setString(1, groupName);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      setError(true);
      setErrorMSG(e.getMessage());
      e.printStackTrace();
    }
  }
}
