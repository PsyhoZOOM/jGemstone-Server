package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 11/3/17.
 */


public class FaktureFunct {

    public boolean hasFirma = false;
    private int userID;
    private database db;

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
     *
     * @param userID id korisnika
     * @param db     database class
     */
    public FaktureFunct(int userID, database db) {
        this.db = db;
        this.userID = userID;
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
                int brFakture = getNextFakturaNo();

            } else {
                //ako nema firmu zatvaramo sql i zavrsavamo sa fakturama
                rs.close();
                ps.close();
                hasFirma = false;
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getNextFakturaNo() {
        PreparedStatement ps;
        ResultSet rs;
        String query = "";


        return 0;
    }


}
