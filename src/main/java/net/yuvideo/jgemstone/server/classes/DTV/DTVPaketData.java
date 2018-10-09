package net.yuvideo.jgemstone.server.classes.DTV;

public class DTVPaketData {

  int id_service;
  String naziv;
  double cena;
  int idPaket;
  String opis;
  int prekoracenje;
  double pdv;
  boolean dodatak;
  boolean dodatnaKartica;
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId_service() {
    return id_service;
  }

  public void setId_service(int id_service) {
    this.id_service = id_service;
  }

  public String getNaziv() {
    return naziv;
  }

  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }

  public double getCena() {
    return cena;
  }

  public void setCena(double cena) {
    this.cena = cena;
  }

  public int getIdPaket() {
    return idPaket;
  }

  public void setIdPaket(int idPaket) {
    this.idPaket = idPaket;
  }

  public String getOpis() {
    return opis;
  }

  public void setOpis(String opis) {
    this.opis = opis;
  }

  public int getPrekoracenje() {
    return prekoracenje;
  }

  public void setPrekoracenje(int prekoracenje) {
    this.prekoracenje = prekoracenje;
  }

  public double getPdv() {
    return pdv;
  }

  public void setPdv(double pdv) {
    this.pdv = pdv;
  }

  public boolean isDodatak() {
    return dodatak;
  }

  public void setDodatak(boolean dodatak) {
    this.dodatak = dodatak;
  }

  public boolean isDodatnaKartica() {
    return dodatnaKartica;
  }

  public void setDodatnaKartica(boolean dodatnaKartica) {
    this.dodatnaKartica = dodatnaKartica;
  }
}
