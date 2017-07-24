package net.yuvideo.jgemstone.server.classes.IPTV;

import net.yuvideo.jgemstone.server.classes.database;
import org.json.JSONObject;

/**
 * Created by PsyhoZOOM@gmail.com on 8/16/17.
 */
public class IPTVFunctions {


    public static JSONObject add_account(JSONObject rLine, database db) {
        StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
        JSONObject jsonObject = restAPI2.saveUSER(rLine);
        return jsonObject;
    }

    public static Boolean checkUserBussy(String STB_MAC, database db) {
        StalkerRestAPI2 restAPI2 = new StalkerRestAPI2(db);
        return restAPI2.checkUser(STB_MAC);
    }
}
