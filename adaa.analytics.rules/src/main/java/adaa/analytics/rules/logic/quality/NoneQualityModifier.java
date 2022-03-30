package adaa.analytics.rules.logic.quality;

public class NoneQualityModifier implements IQualityModifier {
    @Override
    public double modifyQuality(double quality, String attr, double p, double new_p) {
        return quality;
    }
}
