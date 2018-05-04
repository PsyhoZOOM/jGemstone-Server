package net.yuvideo.jgemstone.server.classes;

import java.io.Serializable;

/**
 * Created by zoom on 9/7/16.
 */
public class Uplate implements Serializable {

  int id;
  int br;
  String username;
  String grupa;
  String datum_uplate;
  String za_mesec;
  String godina;
  String uplaceno;
  String operater;


  public String getGodina() {
    return godina;
  }

  public void setGodina(String godina) {
    this.godina = godina;
  }

  public int getBr() {
    return br;
  }

  public void setBr(int br) {
    this.br = br;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getGrupa() {
    return grupa;
  }

  public void setGrupa(String grupa) {
    this.grupa = grupa;
  }

  public String getDatum_uplate() {
    return datum_uplate;
  }

  public void setDatum_uplate(String datum_uplate) {
    this.datum_uplate = datum_uplate;
  }

  public String getZa_mesec() {
    return za_mesec;
  }

  public void setZa_mesec(String za_mesec) {
    this.za_mesec = za_mesec;
  }

  public String getUplaceno() {
    return uplaceno;
  }

  public void setUplaceno(String uplaceno) {
    if (uplaceno.isEmpty()) {
      uplaceno = "0.00";
    }
    this.uplaceno = uplaceno;

  }

  public String getOperater() {
    return operater;
  }

  public void setOperater(String operater) {
    this.operater = operater;
  }
}
