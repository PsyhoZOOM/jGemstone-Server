package net.yuvideo.jgemstone.server.classes;

import java.io.Serializable;

/**
 * Created by zoom on 9/29/16.
 */
public class user_debts implements Serializable {

  private int id;
  private String userName;
  private int serviceId;
  private int userID;
  private String dateDebt;
  private double debth;
  private String paymenthDate;
  private String operName;
  private double payed;
  private double popust;
  private String zaMesec;
  private double debtTotal;


  public int getUserID() {
    return userID;
  }

  public void setUserID(int userID) {
    this.userID = userID;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getServiceId() {
    return serviceId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public String getDateDebt() {
    return dateDebt;
  }

  public void setDateDebt(String dateDebt) {
    this.dateDebt = dateDebt;
  }

  public double getDebth() {
    return debth;
  }

  public void setDebth(double debth) {
    this.debth = debth;
  }

  public String getPaymenthDate() {
    return paymenthDate;
  }

  public void setPaymenthDate(String paymenthDate) {
    this.paymenthDate = paymenthDate;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public double getPayed() {
    return payed;
  }

  public void setPayed(double payed) {
    this.payed = payed;
  }

  public double getPopust() {
    return popust;
  }

  public void setPopust(double popust) {
    this.popust = popust;
  }

  public double getDebtTotal() {
    return debtTotal;
  }

  public void setDebtTotal(double debtTotal) {
    this.debtTotal = debtTotal;
  }
}
