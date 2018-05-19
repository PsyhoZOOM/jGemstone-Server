package net.yuvideo.jgemstone.server.classes;


/**
 * Created by zoom on 1/23/17.
 */
public class valueToPercent {

  public static Double getPDVOfValue(double value, double perc){
    double pdv = value * perc / 100;
    return  pdv;
  }

  public static Double getPDVOfSum(double value, double perc){
    double pdv = (value * perc) / (100 + perc);
    return pdv ;
  }
}
