package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.utils.Tools;

import java.util.List;

public class PerformanceResult {

    private String name;

    private double value;

    public PerformanceResult(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public PerformanceResult() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public static String toString(List<PerformanceResult> list) {
        StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "Performance [");
        for (PerformanceResult mp :list) {
            result.append(Tools.getLineSeparator() + "-----");
            result.append(mp.getName()+": "+mp.getValue());
        }
        result.append(Tools.getLineSeparator() + "]");
        return result.toString();
    }
}
