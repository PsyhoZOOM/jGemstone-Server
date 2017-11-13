package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

/**
 * Created by PsyhoZOOM@gmail.com on 11/3/17.
 */


public class FaktureFunct {

    private final LocalDate zaGodinu;
    private final String operName;
    public boolean hasFirma = false;
    private int userID;
    private database db;
    private int brFakture;
    private int idFakture;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dfYM = new SimpleDateFormat("yyyy-MM");

    /**
     * /**
     * <p>
     * 1.      Proveriti da li je korisnik userID firma <br>
     * 1.1     Ako ne, return; <br>
     * 1.2     Ako da.. <br>
     * 1.2.1       Pogledati dali ima fakture za ovu godinu,
     * ako nema
     * napraviti novu fakturu za ovu godinu sa brojem 1
     * importovati data
     * ako ima
     * uzeti id fakture i importovati data
     * </p>
     * @param userID ; id korisnika
     * @param zaGodinu ; godina-broj fakture
     * @param operater
     * @param db ;     database class
     */
    public FaktureFunct(int userID, LocalDate zaGodinu, String operater, database db) {
        this.db = db;
        this.userID = userID;
        this.zaGodinu = zaGodinu;
        this.operName = operater;
        checkFirma();
    }


    /**
     * provera da li korisnik ima firmu
     * ako nema hasFirma = false i zavrsavamo
     * ako ima firmu pravimo fakturu
     */

    private void checkFirma() {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM users WHERE id=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                rs.next();
                hasFirma = true;

                //provera faktura za ovu godinu
                brFakture = checkFakturaExist();
                
            } else {
                //ako nema firmu zatvaramo sql i zavrsavamo sa fakturama
                rs.close();
                ps.close();
                hasFirma = false;
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertFaktura() {
        PreparedStatement ps;
        String query = "INSERT INTO fakture (userID, brojFakture, datum, dateCreated)" +
                "VALUES" +
                "(?,?,?,?)";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setString(2, String.valueOf(brFakture));
            ps.setString(3, dfYM.format(LocalDate.now()));
            ps.setString(4, df.format(LocalDate.now()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void insertFaktureData(ResultSet rs) {
        PreparedStatement ps;
        String query = "UPDATE FAKTURE DATA SET br=?, naziv=?, jedMere=?, kolicina=?, cenaBezPDV=?, pdv=?," +
                " idFakture=?, operater=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, String.valueOf(brFakture));
            ps.setString(2, rs.getString("nazivPaketa"));
            ps.setString(3, "kom.");
            ps.setDouble(4, rs.getDouble("kolicina"));
            ps.setDouble(5, rs.getDouble("cenaBezPDV"));
            ps.setDouble(6, rs.getDouble("pdv"));
            ps.setInt(7, rs.getInt("idFakture"));
            ps.setString(8, operName);
            //TODO insert faktura

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int checkFakturaExist() {
        int broj = 1;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM fakture WHERE userID=? AND datum=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setString(2, dfYM.format(this.zaGodinu));
            rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                broj = Integer.valueOf(rs.getString("brojFakture"));
                idFakture = rs.getInt("id");
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return broj;
    }


    public void createFakturu(ResultSet rs, database db) {
        if (brFakture > 1) {
            insertFaktureData(rs);
        } else {
            insertFaktura();
            brFakture = checkFakturaExist();
            insertFaktureData(rs);
        }
    }
}
