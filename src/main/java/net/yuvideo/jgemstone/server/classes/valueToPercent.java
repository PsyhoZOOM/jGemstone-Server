package net.yuvideo.jgemstone.server.classes;

import java.text.DecimalFormat;

/**
 * Created by zoom on 1/23/17.
 */
public class valueToPercent {

  static DecimalFormat df = new DecimalFormat("0.00");

  public static Double getValue(Double value, double percentage) {
    double percent = value - (value * percentage / 100);
    return percent;

  }

  public static Double getPercentage(double value, Double oFvalue) {
    double percentage = value / oFvalue;
    double result = percentage * 100;
    return result;
  }

  public static Double getDiffValue(double value, double pdv) {
    double percen = value - (value * pdv / 100);
    return value - percen;
  }

  public static Double getValueOfPercentAdd(double value, double perc) {
    double result = (perc / 100 + 1) * value - value;
    return Double.valueOf(df.format(result));
  }

  public static Double getValueOfPercentSub(double value, double perc) {
    double result = value - (value / (perc / 100 + 1));
    return Double.valueOf(df.format(result));
  }
}
