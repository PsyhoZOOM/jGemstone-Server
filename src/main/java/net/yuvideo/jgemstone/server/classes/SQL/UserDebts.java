package net.yuvideo.jgemstone.server.classes.SQL;

public class UserDebts {

  //VARS
  int id;
  int id_ServiceUser;
  int userID;
  int kolicina;

  double popust = 0;
  double cena = 0;
  double uplaceno = 0;
  double dug = 0;
  double pdv = 0;


  boolean skipProduzenje;

  String nazivPaketa;
  String datumZaduzenja;
  String paketType;
  String datumUplate;
  String operater;
  String zaduzenOd;
  String zaMesec;
  String jMere;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId_ServiceUser() {
    return id_ServiceUser;
  }

  public void setId_ServiceUser(int id_ServiceUser) {
    this.id_ServiceUser = id_ServiceUser;
  }

  public int getUserID() {
    return userID;
  }

  public void setUserID(int userID) {
    this.userID = userID;
  }

  public int getKolicina() {
    return kolicina;
  }

  public void setKolicina(int kolicina) {
    this.kolicina = kolicina;
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

  public double getUplaceno() {
    return uplaceno;
  }

  public void setUplaceno(double uplaceno) {
    this.uplaceno = uplaceno;
  }

  public double getDug() {
    return dug;
  }

  public void setDug(double dug) {
    this.dug = dug;
  }

  public double getPdv() {
    return pdv;
  }

  public void setPdv(double pdv) {
    this.pdv = pdv;
  }

  public boolean isSkipProduzenje() {
    return skipProduzenje;
  }

  public void setSkipProduzenje(boolean skipProduzenje) {
    this.skipProduzenje = skipProduzenje;
  }

  public String getNazivPaketa() {
    return nazivPaketa;
  }

  public void setNazivPaketa(String nazivPaketa) {
    this.nazivPaketa = nazivPaketa;
  }

  public String getDatumZaduzenja() {
    return datumZaduzenja;
  }

  public void setDatumZaduzenja(String datumZaduzenja) {
    this.datumZaduzenja = datumZaduzenja;
  }

  public String getPaketType() {
    return paketType;
  }

  public void setPaketType(String paketType) {
    this.paketType = paketType;
  }

  public String getDatumUplate() {
    return datumUplate;
  }

  public void setDatumUplate(String datumUplate) {
    this.datumUplate = datumUplate;
  }

  public String getOperater() {
    return operater;
  }

  public void setOperater(String operater) {
    this.operater = operater;
  }

  public String getZaduzenOd() {
    return zaduzenOd;
  }

  public void setZaduzenOd(String zaduzenOd) {
    this.zaduzenOd = zaduzenOd;
  }

  public String getZaMesec() {
    return zaMesec;
  }

  public void setZaMesec(String zaMesec) {
    this.zaMesec = zaMesec;
  }

  public String getjMere() {
    return jMere;
  }

  public void setjMere(String jMere) {
    this.jMere = jMere;
  }
}
