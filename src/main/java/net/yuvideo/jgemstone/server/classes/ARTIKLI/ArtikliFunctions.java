package net.yuvideo.jgemstone.server.classes.ARTIKLI;

import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by PsyhoZOOM@gmail.com on 1/30/18.
 */
public class ArtikliFunctions {
    private final database db;
    private final String operName;
    private boolean haveError = false;
    private String error = "";

    private JSONObject artikli;
    private DecimalFormat df = new DecimalFormat("0.00");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ArtikliFunctions(database db, String operName) {
        this.operName = operName;
        this.db = db;
    }

    public void addArtikl(JSONObject rLine) {
        PreparedStatement ps;
        int lastID = 0;
        String query = "INSERT INTO Artikli (naziv, proizvodjac, model, serijski, pon, mac, dobavljac, brDokumenta, nabavnaCena, " +
                "jMere, kolicina, opis, datum, operName, idMagacin) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, rLine.getString("naziv"));
            ps.setString(2, rLine.getString("proizvodjac"));
            ps.setString(3, rLine.getString("model"));
            ps.setString(4, rLine.getString("serijski"));
            ps.setString(5, rLine.getString("pon"));
            ps.setString(6, rLine.getString("mac"));
            ps.setString(7, rLine.getString("dobavljac"));
            ps.setString(8, rLine.getString("brDokumenta"));
            ps.setDouble(9, Double.valueOf(df.format(rLine.getDouble("nabavnaCena"))));
            ps.setString(10, rLine.getString("jMere"));
            ps.setInt(11, rLine.getInt("kolicina"));
            ps.setString(12, rLine.getString("opis"));
            ps.setString(13, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            ps.setString(14, operName);
            ps.setInt(15, rLine.getInt("idMagacin"));

            lastID = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                lastID = rs.getInt(1);
            ps.close();
            rs.close();

        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

        //SETTING UNIQUE_ID
        query = "UPDATE Artikli SET uniqueID=? WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, lastID);
            ps.setInt(2, lastID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

        artikliTrackingNew(rLine, lastID);


    }

    private void artikliTrackingNew(JSONObject rLine, int idArtikle) {
        PreparedStatement ps;
        String query = "INSERT INTO ArtikliTracking (date, message, operName, source, destination, kolicina, artikalID, artikalNaziv, destinationID, uniqueID) " +
                "VALUES " +
                "(?,?,?,?,?,?,?,?,?,?) ";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, String.valueOf(LocalDateTime.now().format(dateTimeFormatter)));
            ps.setString(2, String.format("Novi Artikal %s od dobavljaca %s, kolicina: %d, zaduzen magacin %s.. ",
                    String.format("%s %s %s", rLine.getString("naziv"),
                            rLine.getString("proizvodjac"),
                            rLine.getString("model")),
                    rLine.getString("dobavljac"),
                    rLine.getInt("kolicina"),
                    getMagacinInfo(rLine.getInt("idMagacin")).getString("naziv")
            ));
            ps.setString(3, operName);
            ps.setString(4, rLine.getString("dobavljac"));
            ps.setString(5, getMagacinInfo(rLine.getInt("idMagacin")).toString());
            ps.setInt(6, rLine.getInt("kolicina"));
            ps.setInt(7, idArtikle);
            ps.setString(8, rLine.getString("naziv"));
            ps.setInt(9, rLine.getInt("idMagacin"));
            ps.setInt(10, idArtikle);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void editArtikl(JSONObject rLine) {
        PreparedStatement ps;
        String query = "UPDATE Artikli SET naziv=?, proizvodjac=?, model=?, serijski=?, pon=?, mac=?, dobavljac=?, brDokumenta=?, " +
                "nabavnaCena=?, jMere=?, kolicina=?, opis=?, operName=?  WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("naziv"));
            ps.setString(2, rLine.getString("proizvodjac"));
            ps.setString(3, rLine.getString("model"));
            ps.setString(4, rLine.getString("serijski"));
            ps.setString(5, rLine.getString("pon"));
            ps.setString(6, rLine.getString("mac"));
            ps.setString(7, rLine.getString("dobavljac"));
            ps.setString(8, rLine.getString("brDokumenta"));
            ps.setDouble(9, rLine.getDouble("nabavnaCena"));
            ps.setString(10, rLine.getString("jMere"));
            ps.setDouble(11, rLine.getInt("kolicina"));
            ps.setString(12, rLine.getString("opis"));
            ps.setString(13, operName);
            ps.setInt(14, rLine.getInt("id"));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

    }

    public void deleteArtikl(int id) {
        PreparedStatement ps;
        String query = "DELETE FROM Artikli where id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            haveError = false;
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

    }

    public boolean isError() {
        return haveError;
    }

    public String getErrorMessage() {
        return error;
    }

    public JSONObject getArtikles() {
        return artikli;
    }

    public void getAllArtikles() {
        JSONObject jsonObject = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM Artikli";
        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject artObj = new JSONObject();
                    artObj.put("id", rs.getInt("id"));
                    artObj.put("naziv", rs.getString("naziv"));
                    artObj.put("proizvodjac", rs.getString("proizvodjac"));
                    artObj.put("model", rs.getString("model"));
                    artObj.put("serijski", rs.getString("serijski"));
                    artObj.put("pon", rs.getString("pon"));
                    artObj.put("mac", rs.getString("mac"));
                    artObj.put("dobavljac", rs.getString("dobavljac"));
                    artObj.put("brDokumenta", rs.getString("brDokumenta"));
                    artObj.put("nabavnaCena", rs.getDouble("nabavnaCena"));
                    artObj.put("jMere", rs.getString("jMere"));
                    artObj.put("kolicina", rs.getInt("kolicina"));
                    artObj.put("opis", rs.getString("opis"));
                    artObj.put("datum", rs.getString("datum"));
                    artObj.put("operName", rs.getString("operName"));
                    artObj.put("idMagacin", rs.getInt("idMagacin"));
                    artObj.put("isUser", rs.getBoolean("isuSer"));
                    artObj.put("uniqueID", rs.getInt("uniqueID"));
                    jsonObject.put(String.valueOf(i), artObj);
                    i++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.artikli = jsonObject;
    }

    public void searchArtikles(JSONObject rLine) {
        JSONObject jsonObject = new JSONObject();

        PreparedStatement ps;
        ResultSet rs;
        String query;
        if (rLine.getInt("idMagacin") == 0) {
            query = "SELECT * FROM Artikli WHERE naziv LIKE ? AND proizvodjac LIKE ? AND model LIKE ? AND serijski LIKE ? " +
                    "AND pon LIKE ? AND mac LIKE ? AND dobavljac LIKE ? AND brDokumenta LIKE ? AND opis LIKE ?";
        } else {
            query = "SELECT * FROM Artikli WHERE naziv LIKE ? AND proizvodjac LIKE ? AND model LIKE ? AND serijski LIKE ? AND pon LIKE ? " +
                    "AND mac LIKE ? AND dobavljac LIKE ? AND brDokumenta LIKE ? AND opis LIKE ? AND idMagacin LIKE ?";
        }

        try {
            ps = db.conn.prepareStatement(query);
            ps.setString(1, rLine.getString("naziv") + "%");
            ps.setString(2, rLine.getString("proizvodjac") + "%");
            ps.setString(3, rLine.getString("model") + "%");
            ps.setString(4, rLine.getString("serijski") + "%");
            ps.setString(5, rLine.getString("pon") + "%");
            ps.setString(6, rLine.getString("mac") + "%");
            ps.setString(7, rLine.getString("dobavljac") + "%");
            ps.setString(8, rLine.getString("brDokumenta") + "%");
            ps.setString(9, rLine.getString("opis") + "%");
            if (rLine.getInt("idMagacin") == 0) {

            } else {
                ps.setInt(10, rLine.getInt("idMagacin"));
            }
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {

                    jsonObject.put(String.valueOf(i), setArtikal(rs));
                    i++;
                }
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }


        this.artikli = jsonObject;
    }


    public JSONObject getArtikal(int id) {
        JSONObject artikal = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM Artikli WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                artikal = setArtikal(rs);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            this.haveError = true;
            this.error = e.getMessage();
            e.printStackTrace();
        }

        return artikal;

    }


    private JSONObject setArtikal(ResultSet rs) {
        JSONObject artObj = new JSONObject();
        try {
            artObj.put("id", rs.getInt("id"));
            artObj.put("naziv", rs.getString("naziv"));
            artObj.put("proizvodjac", rs.getString("proizvodjac"));
            artObj.put("model", rs.getString("model"));
            artObj.put("serijski", rs.getString("serijski"));
            artObj.put("pon", rs.getString("pon"));
            artObj.put("mac", rs.getString("mac"));
            artObj.put("dobavljac", rs.getString("dobavljac"));
            artObj.put("brDokumenta", rs.getString("brDokumenta"));
            artObj.put("nabavnaCena", rs.getDouble("nabavnaCena"));
            artObj.put("jMere", rs.getString("jMere"));
            artObj.put("kolicina", rs.getInt("kolicina"));
            artObj.put("opis", rs.getString("opis"));
            artObj.put("datum", rs.getString("datum"));
            artObj.put("operName", rs.getString("operName"));
            artObj.put("idMagacin", rs.getInt("idMagacin"));
            artObj.put("uniqueID", rs.getInt("uniqueID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return artObj;

    }


    public void zaduziArtikalUser(JSONObject rLine) {
        JSONObject artikal = getArtikal(rLine.getInt("artikalID"));
        PreparedStatement ps;
        ResultSet rs;
        String query;

        if (artikal.getInt("kolicina") == 0) {
            return;
        }

        if (artikal.getInt("kolicina") == 1) {
            query = "UPDATE Artikli SET idMagacin = ? , isUser=? WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("destUserID"));
                ps.setBoolean(2, true);
                ps.setInt(3, artikal.getInt("id"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                haveError = true;
                error = e.getMessage();
                e.printStackTrace();
            }

            artikliTrackingUser(rLine, artikal);

        } else if (artikal.getInt("kolicina") > 1) {
            //napravi nov
            //wee need to subdivide sourceKolicna from kolicina;
            //create new Artikal in dest Magacin
            query = "INSERT INTO Artikli " +
                    "(naziv, proizvodjac, model, serijski, pon, mac, dobavljac, brDokumenta, jMere, kolicina, nabavnaCena, opis, datum, operName, idMagacin, isUser, uniqueID) " +
                    "VALUES " +
                    "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, artikal.getString("naziv"));
                ps.setString(2, artikal.getString("proizvodjac"));
                ps.setString(3, artikal.getString("model"));
                ps.setString(4, artikal.getString("serijski"));
                ps.setString(5, artikal.getString("pon"));
                ps.setString(6, artikal.getString("mac"));
                ps.setString(7, artikal.getString("dobavljac"));
                ps.setString(8, artikal.getString("brDokumenta"));
                ps.setString(9, artikal.getString("jMere"));
                ps.setInt(10, rLine.getInt("kolicina"));
                ps.setDouble(11, artikal.getDouble("nabavnaCena"));
                ps.setString(12, artikal.getString("opis"));
                ps.setString(13, String.valueOf(LocalDateTime.now().format(dateTimeFormatter)));
                ps.setString(14, operName);
                ps.setInt(15, rLine.getInt("destUserID"));
                ps.setBoolean(16, true);
                ps.setInt(17, artikal.getInt("uniqueID"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                error = e.getMessage();
                haveError = true;
                e.printStackTrace();
            }

            //subdivide source artikal kolicina of dst artikal kolicina :)
            int kolicina = artikal.getInt("kolicina") - rLine.getInt("kolicina");

            //UPDATE source artikal
            query = "UPDATE Artikli SET kolicina=? WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, kolicina);
                ps.setInt(2, artikal.getInt("id"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                error = e.getMessage();
                haveError = true;
                e.printStackTrace();
            }


            //ARTIKLI TRACKING
            artikliTrackingUser(rLine, artikal);

        }
    }


    public void zaduziArtikalMag(JSONObject rLine) {
        JSONObject artikal = getArtikal(rLine.getInt("artikalID"));
        if (artikal.getInt("kolicina") == 0)
            return;

        PreparedStatement ps;
        String query;

        if (artikal.getInt("kolicina") == 1) {
            //premesti idMagacin
            query = "UPDATE Artikli set idMagacin = ? WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, rLine.getInt("destMagID"));
                ps.setInt(2, artikal.getInt("id"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                this.haveError = true;
                this.error = e.getMessage();
                e.printStackTrace();
            }


            //ARITKLI TRACKING
            artikliTrackingMag(rLine, artikal);
        }

        if (artikal.getInt("kolicina") > 1) {
            //napravi nov
            //wee need to subdivide sourceKolicna from kolicina;
            //create new Artikal in dest Magacin
            query = "INSERT INTO Artikli " +
                    "(naziv, proizvodjac, model, serijski, pon, mac, dobavljac, brDokumenta, jMere, kolicina, nabavnaCena, opis, datum, operName, idMagacin, isUser, uniqueID) " +
                    "VALUES " +
                    "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setString(1, artikal.getString("naziv"));
                ps.setString(2, artikal.getString("proizvodjac"));
                ps.setString(3, artikal.getString("model"));
                ps.setString(4, artikal.getString("serijski"));
                ps.setString(5, artikal.getString("pon"));
                ps.setString(6, artikal.getString("mac"));
                ps.setString(7, artikal.getString("dobavljac"));
                ps.setString(8, artikal.getString("brDokumenta"));
                ps.setString(9, artikal.getString("jMere"));
                ps.setInt(10, rLine.getInt("kolicina"));
                ps.setDouble(11, artikal.getDouble("nabavnaCena"));
                ps.setString(12, artikal.getString("opis"));
                ps.setString(13, String.valueOf(LocalDateTime.now().format(dateTimeFormatter)));
                ps.setString(14, operName);
                ps.setInt(15, rLine.getInt("destMagID"));
                ps.setBoolean(16, false);
                ps.setInt(17, artikal.getInt("uniqueID"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                error = e.getMessage();
                haveError = true;
                e.printStackTrace();
            }

            //subdivide source artikal kolicina of dst artikal kolicina :)
            int kolicina = artikal.getInt("kolicina") - rLine.getInt("kolicina");

            //UPDATE source artikal
            query = "UPDATE Artikli SET kolicina=? WHERE id=?";
            try {
                ps = db.conn.prepareStatement(query);
                ps.setInt(1, kolicina);
                ps.setInt(2, artikal.getInt("id"));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                error = e.getMessage();
                haveError = true;
                e.printStackTrace();
            }


            //ARTIKLI TRACKING
            artikliTrackingMag(rLine, artikal);
        }

    }


    private void artikliTrackingMag(JSONObject rLine, JSONObject artikal) {
        PreparedStatement ps;
        String query = "INSERT INTO ArtikliTracking (sourceID, destinationID, source, destination, kolicina ,date, message, isUser, operName, artikalID, artikalNaziv, uniqueID)" +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("sourceMagID"));
            ps.setInt(2, rLine.getInt("destMagID"));
            ps.setString(3, String.valueOf(getMagacinInfo(rLine.getInt("sourceMagID"))));
            ps.setString(4, String.valueOf(getMagacinInfo(rLine.getInt("destMagID"))));
            ps.setInt(5, rLine.getInt("kolicina"));
            ps.setString(6, String.valueOf(LocalDateTime.now().format(dateTimeFormatter)));
            ps.setString(7, String.format("%s zaduzio %s sa %s kolicina: %d",
                    getMagacinInfo(rLine.getInt("sourceMagID")),
                    getMagacinInfo(rLine.getInt("destMagID")),
                    artikal,
                    rLine.getInt("kolicina")
            ));
            ps.setBoolean(8, rLine.getBoolean("isUser"));
            ps.setString(9, operName);
            ps.setInt(10, artikal.getInt("id"));
            ps.setString(11, artikal.getString("naziv"));
            ps.setInt(12, artikal.getInt("uniqueID"));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }

    }


    private void artikliTrackingUser(JSONObject rLine, JSONObject artikal) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PreparedStatement ps;
        String query = "INSERT INTO ArtikliTracking (sourceID, destinationID, source, destination, kolicina ,date, message, isUser, operName, artikalID, artikalNaziv, uniqueID)" +
                "VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, rLine.getInt("sourceMagID"));
            ps.setInt(2, rLine.getInt("destUserID"));
            ps.setString(3, String.valueOf(getMagacinInfo(rLine.getInt("sourceMagID"))));
            ps.setString(4, String.valueOf(getUserInfo(rLine.getInt("destUserID"))));
            ps.setInt(5, rLine.getInt("kolicina"));
            ps.setString(6, String.valueOf(LocalDateTime.now().format(dateTimeFormatter)));
            ps.setString(7, String.format("%s zaduzio %s sa %s kolicina: %d",
                    getMagacinInfo(rLine.getInt("sourceMagID")),
                    getUserInfo(rLine.getInt("destUserID")),
                    artikal,
                    rLine.getInt("kolicina")
            ));
            ps.setBoolean(8, true);
            ps.setString(9, operName);
            ps.setInt(10, artikal.getInt("id"));
            ps.setString(11, artikal.getString("naziv"));
            ps.setInt(12, artikal.getInt("uniqueID"));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            haveError = true;
            error = e.getMessage();
            e.printStackTrace();
        }
    }


    private JSONObject getMagacinInfo(int id) {
        JSONObject object = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM Magacin WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                object.put("id", rs.getInt("id"));
                object.put("naziv", rs.getString("naziv"));
                object.put("opis", rs.getString("opis"));
                object.put("glavniMagacin", rs.getBoolean("glavniMagacin"));
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return object;
    }


    private JSONObject getUserInfo(int id) {
        JSONObject obj = new JSONObject();
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM users WHERE id=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                obj.put("id", rs.getInt("id"));
                obj.put("ime", rs.getString("ime"));
                obj.put("adresa", rs.getString("adresa"));
                obj.put("mesto", rs.getString("mesto"));
                obj.put("nazivFirme", rs.getString("nazivFirme"));
                obj.put("kontaktOsoba", rs.getString("kontaktOsoba"));
                obj.put("adresaFirme", rs.getString("adresaFirme"));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            error = e.getMessage();
            haveError = true;
            e.printStackTrace();
        }
        return obj;

    }


    public JSONObject getArtikliTracking(int ArtikalID, int magID, int uniqueID) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM ArtikliTracking WHERE uniqueID=? AND sourceID=? OR uniqueID=? AND destinationID=? ";
        JSONObject obj = new JSONObject();

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, uniqueID);
            ps.setInt(2, magID);
            ps.setInt(3, uniqueID);
            ps.setInt(4, magID);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    JSONObject art = new JSONObject();
                    art.put("id", rs.getInt("id"));
                    art.put("sourceID", rs.getInt("sourceID"));
                    art.put("destinationID", rs.getInt("destinationID"));
                    art.put("date", rs.getString("date"));
                    art.put("message", rs.getString("message"));
                    art.put("isUser", rs.getBoolean("isUser"));
                    art.put("operName", rs.getString("operName"));
                    art.put("source", rs.getString("source"));
                    art.put("destination", rs.getString("destination"));
                    art.put("kolicina", rs.getInt("kolicina"));
                    art.put("artikalID", rs.getInt("artikalID"));
                    art.put("artikalNaziv", rs.getString("artikalNaziv"));
                    art.put("uniqueID", rs.getInt("uniqueID"));
                    obj.put(String.valueOf(i), art);
                    i++;
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            obj.put("ERROR", e.getMessage());
            e.printStackTrace();
        }

        return obj;
    }




}
