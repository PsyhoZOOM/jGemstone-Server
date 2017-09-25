package net.yuvideo.jgemstone.server.classes;

/**
 * Created by zoom on 1/23/17.
 */
public class valueToPercent {
    public static Double getValue(Double value, double percentage) {
        double percent = value - (value * percentage / 100);
        return percent;

    }

    public static Double getPercentage(double value, Double oFvalue) {
        double percentage = value / oFvalue;
        double result = percentage * 100;
        return result;
    }
}
