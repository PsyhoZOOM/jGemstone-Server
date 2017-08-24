package classes.IPTV;

import classes.database;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by PsyhoZOOM@gmail.com on 8/16/17.
 */
public class IPTVFunctions {

    public static boolean activateNewService(JSONObject rLine, database db) {
        StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
        LocalDateTime date = LocalDateTime.now();
        date = date.plusMonths(rLine.getInt("produzenje"));
        String endDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString();

        JSONObject obj = restAPI2.setEndDate(rLine, endDate);


        //izbrisati
        return false;

    }

    public static void add_account(JSONObject rLine, database db) {
        StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
        restAPI2.saveUSER(rLine);
    }

    public static Boolean checkUserBussy(String STB_MAC, database db) {
        StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
        return restAPI2.checkUser(STB_MAC);
    }
}
