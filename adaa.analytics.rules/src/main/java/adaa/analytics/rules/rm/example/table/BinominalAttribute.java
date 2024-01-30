package adaa.analytics.rules.rm.example.table;

public class BinominalAttribute extends NominalAttribute {
    private static final long serialVersionUID = 2932687830235332221L;
    private INominalMapping nominalMapping = (INominalMapping) new BinominalMapping();

    BinominalAttribute(String name) {
        super(name, 6);
    }

    private BinominalAttribute(BinominalAttribute a) {
        super(a);
        this.nominalMapping = a.nominalMapping;
    }

    public Object clone() {
        return new BinominalAttribute(this);
    }

    public INominalMapping getMapping() {
        return this.nominalMapping;
    }

    public void setMapping(INominalMapping newMapping) {
        this.nominalMapping = newMapping;
    }

    public boolean isDateTime() {
        return false;
    }
}
