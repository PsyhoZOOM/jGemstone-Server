package net.yuvideo.jgemstone.server.classes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
	 * /
	 **
	 * <p>
	 * 1. Proveriti da li je korisnik userID firma <br>
	 * 1.1 Ako ne, return; <br>
	 * 1.2 Ako da.. <br>
	 * 1.2.1 Pogledati dali ima fakture za ovu godinu, ako nema napraviti novu
	 * fakturu za ovu godinu sa brojem 1 importovati data ako ima uzeti id
	 * fakture i importovati data
	 * </p>
	 *
	 * @param userID ; id korisnika
	 * @param zaGodinu ; godina-broj fakture
	 * @param operater
	 * @param db ; database class
	 */
	public FaktureFunct(int userID, LocalDate zaGodinu, String operater, database db) {
		this.db = db;
		this.userID = userID;
		this.zaGodinu = zaGodinu;
		this.operName = operater;
		checkFirma();
	}

	/**
	 * provera da li korisnik ima firmu ako nema hasFirma = false i zavrsavamo
	 * ako ima firmu pravimo fakturu
	 */
	private void checkFirma() {
		PreparedStatement ps;
		ResultSet rs;
		String query = "SELECT * FROM users WHERE id=? AND firma=true";

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

	private void insertFaktureData(ResultSet rs) {
		PreparedStatement ps;
		String query = "INSERT INTO faktureData (br, naziv, jedMere, kolicina, "
				+ "cenaBezPDV, pdv,  operater, userID, datum, godina, mesec ) "
				+ "VALUES "
				+ "(?,?,?,?,?,?,?,?,?,?,?)";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setString(1, String.valueOf(brFakture));
            ps.setString(2, String.format("%s - %s", rs.getString("nazivPaketa"), getMonthName(this.zaGodinu.format(DateTimeFormatter.ofPattern("MM")))));
            ps.setString(3, "kom.");
			ps.setDouble(4, 1);
			ps.setDouble(5, rs.getDouble("dug"));
			ps.setDouble(6, rs.getDouble("PDV"));
			ps.setString(7, operName);
			ps.setInt(8, rs.getInt("userID"));
			ps.setString(9, this.zaGodinu.format(DateTimeFormatter.ofPattern("yyyy-MM")));
			ps.setString(10, this.zaGodinu.format(DateTimeFormatter.ofPattern("yyyy")));
			ps.setString(11, this.zaGodinu.format(DateTimeFormatter.ofPattern("MM")));

			ps.executeUpdate();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private int checkFakturaExist() {
		int broj = 1;
		PreparedStatement ps;
		ResultSet rs;
		String query = "SELECT count(distinct mesec), mesec, br FROM faktureData WHERE userID=? AND godina=? group by mesec asc";

		try {
			ps = db.conn.prepareStatement(query);
			ps.setInt(1, userID);
			ps.setString(2, this.zaGodinu.format(DateTimeFormatter.ofPattern("yyyy")));
			rs = ps.executeQuery();

			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					if (rs.getInt(1) > 0) {
						if (rs.getInt(2) == Integer.
								valueOf(this.zaGodinu.
										format(DateTimeFormatter.
												ofPattern("MM")))) {
							broj = rs.getInt(3);
						}else if (rs.getInt(2) 
								< Integer.valueOf(this.zaGodinu.format(DateTimeFormatter.ofPattern("MM")))){
							broj = rs.getInt(3)+1;
							
						}

					} 
					
				}
			}else{
				broj = 1;
			}

			ps.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return broj;
	}

	public void createFakturu(ResultSet rs) {
		insertFaktureData(rs);
	}

    private String getMonthName(String month) {
        String monthName;
        switch (month) {
            case "01":
                monthName = "Januar";
                break;
            case "02":
                monthName = "Februar";
                break;
            case "03":
                monthName = "Mart";
                break;
            case "04":
                monthName = "April";
                break;
            case "05":
                monthName = "Maj";
                break;
            case "06":
                monthName = "Jun";
                break;
            case "07":
                monthName = "Jul";
                break;
            case "08":
                monthName = "Avgust";
                break;
            case "09":
                monthName = "Septembar";
                break;
            case "10":
                monthName = "Oktobar";
                break;
            case "11":
                monthName = "Novembar";
                break;
            case "12":
                monthName = "Decembar";
                break;

            default:
                monthName = "";

        }
        return monthName;
    }
}
