package net.yuvideo.jgemstone.server.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zoom on 2/27/17.
 */
public class md5_digiest {
    String original;
    StringBuffer sb = new StringBuffer();

    public md5_digiest(String original) {
        this.original = original;
    }


    public String get_hash() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(original.getBytes());
        byte[] digest = md.digest();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }

        //System.out.println("original:" + original);
        //System.out.println("digested(hex):" + sb.toString());
        return sb.toString();
    }
}
