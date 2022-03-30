package adaa.analytics.rules.logic.quality;

public interface IQualityModifier {
    public double modifyQuality(double quality, String attr, double p, double new_p);
}
