package net.yuvideo.jgemstone.server.classes.FIX;

import java.io.Serializable;

/**
 * Created by PsyhoZOOM@gmail.com on 8/29/17.
 */
public class CSVData implements Serializable {
    int id;
    String account;
    String from;
    String to;
    String country;
    String description;
    String connectTime;
    String chargedTimeMinSec;
    int chargedTimeSec;
    double chargedAmountRSD;
    String serviceName;
    int chargedQuantity;
    String serviceUnit;
    String customerID;
    String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(String connectTime) {
        this.connectTime = connectTime;
    }

    public String getChargedTimeMinSec() {
        return chargedTimeMinSec;
    }

    public void setChargedTimeMinSec(String chargedTimeMinSec) {
        this.chargedTimeMinSec = chargedTimeMinSec;
    }

    public int getChargedTimeSec() {
        return chargedTimeSec;
    }

    public void setChargedTimeSec(int chargedTimeSec) {
        this.chargedTimeSec = chargedTimeSec;
    }

    public double getChargedAmountRSD() {
        return chargedAmountRSD;
    }

    public void setChargedAmountRSD(double chargedAmountRSD) {
        this.chargedAmountRSD = chargedAmountRSD;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getChargedQuantity() {
        return chargedQuantity;
    }

    public void setChargedQuantity(int chargedQuantity) {
        this.chargedQuantity = chargedQuantity;
    }

    public String getServiceUnit() {
        return serviceUnit;
    }

    public void setServiceUnit(String serviceUnit) {
        this.serviceUnit = serviceUnit;
    }

}
