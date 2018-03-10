package net.yuvideo.jgemstone.server.classes.USERS;

import net.yuvideo.jgemstone.server.classes.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserFunc {

    public static boolean deleteUser(int userId, database db) {
        String query = "DELETE FROM users WHERE id=?";
        PreparedStatement ps;
        boolean deleted = true;

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            deleted = false;
            e.printStackTrace();
        }

        //delete from Services_user
        query = "DELETE FROM Services_User, user_debts,  WHERE  userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {

            deleted = false;
            e.printStackTrace();
        }

        query = "DELETE FROM ugovori_korisnik WHERE userID=?";

        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            deleted = false;
            e.printStackTrace();
        }


        query = "DELETE FROM uplate WHERE userID=?";
        try {
            ps = db.conn.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            deleted = false;
            e.printStackTrace();
        }


        return deleted;


    }
}
