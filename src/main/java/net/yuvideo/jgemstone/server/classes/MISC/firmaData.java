package net.yuvideo.jgemstone.server.classes.MISC;

import net.yuvideo.jgemstone.server.classes.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 1/24/18.
 */
public class firmaData {
    int id;
    int userID;
    String nazivFirme;
    String kontaktOsoba;
    String kodBanke;
    String PIB;
    String maticniBroj;
    String tekuciRacun;
    String fax;
    String adresaFirme;
    private boolean isError;
    private String error;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getNazivFirme() {
        return nazivFirme;
    }

    public void setNazivFirme(String nazivFirme) {
        this.nazivFirme = nazivFirme;
    }

    public String getKontaktOsoba() {
        return kontaktOsoba;
    }

    public void setKontaktOsoba(String kontaktOsoba) {
        this.kontaktOsoba = kontaktOsoba;
    }

    public String getKodBanke() {
        return kodBanke;
    }

    public void setKodBanke(String kodBanke) {
        this.kodBanke = kodBanke;
    }

    public String getPIB() {
        return PIB;
    }

    public void setPIB(String PIB) {
        this.PIB = PIB;
    }

    public String getMaticniBroj() {
        return maticniBroj;
    }

    public void setMaticniBroj(String maticniBroj) {
        this.maticniBroj = maticniBroj;
    }

    public String getTekuciRacun() {
        return tekuciRacun;
    }

    public void setTekuciRacun(String tekuciRacun) {
        this.tekuciRacun = tekuciRacun;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAdresaFirme() {
        return adresaFirme;
    }

    public void setAdresaFirme(String adresaFirme) {
        this.adresaFirme = adresaFirme;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void getFirmaData(int userID, database db) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM firme where userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                this.userID = rs.getInt("userID");
                this.id = rs.getInt("id");
                this.nazivFirme = rs.getString("nazivFirme");
                this.kodBanke = rs.getString("kodBanke");
                this.PIB = rs.getString("PIB");
                this.maticniBroj = rs.getString("maticniBroj");
                this.tekuciRacun = rs.getString("tekuciRacun");
                this.fax = rs.getString("fax");
                this.adresaFirme = rs.getString("adresaFirme");
                this.kontaktOsoba = rs.getString("kontaktOsoba");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            this.isError = true;
            this.error = e.getMessage();
            e.printStackTrace();
        }


    }
}
