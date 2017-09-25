package net.yuvideo.jgemstone.server.classes;

import java.io.Serializable;

/**
 * Created by zoom on 9/1/16.
 */
public class Groups implements Serializable{
    int id;
    int br;
    String GroupName;
    String Cena;
    int Prepaid;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBr() {
        return br;
    }

    public void setBr(int br) {
        this.br = br;
    }

    public String getNaziv() {
        return GroupName;
    }

    public void setGroupName(String GroupName) {
        this.GroupName = GroupName;
    }

    public String getCena() {
        return Cena;
    }

    public void setCena(String cena) {
        Cena = cena;
    }

    public int getPrepaid() {
        return Prepaid;
    }

    public void setPrepaid(int prepaid) {
        Prepaid = prepaid;
    }
}
