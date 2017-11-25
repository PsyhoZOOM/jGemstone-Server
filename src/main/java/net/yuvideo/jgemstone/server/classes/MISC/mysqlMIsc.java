package net.yuvideo.jgemstone.server.classes.MISC;

import net.yuvideo.jgemstone.server.classes.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by PsyhoZOOM@gmail.com on 7/25/17.
 */
public class mysqlMIsc {
    public static int findNextFreeID(database db) {
        int id = 0;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT id from users";

        try {
            ps = db.conn.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                int prevID = 0;
                int currentID = 0;
                while (rs.next()) {
                    //treba pronaci prvi slobodan id
                    //uzeti trenutni id
                    //uporediti za prethodnim id
                    //ako je trenutni id > prethodnog id+2 vrati pretodni id+1


                    currentID = rs.getInt("id");
                    if (currentID >= (prevID + 2)) {
                        id = prevID + 1;
                        return id;
                    } else {
                        prevID = currentID;
                        id = currentID + 1;
                    }

                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;

    }
}
