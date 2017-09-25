package net.yuvideo.jgemstone.server.classes;

import java.io.Serializable;

/**
 * Created by zoom on 8/8/16.
 */
public class messageS implements Serializable{
    private String message;
    private String username;
    private String password;
    private String action;
    private String function;
    private Boolean alive;
    private String query;
    private Users user_update;
    private Groups group_update;
    private Services services;

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public Groups getGroup_update() {
        return group_update;
    }

    public void setGroup_update(Groups group_update) {
        this.group_update = group_update;
    }

    public Users getUser_update() {
        return user_update;
    }

    public void setUser_update(Users user_update) {
        this.user_update = user_update;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
