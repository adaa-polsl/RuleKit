package adaa.analytics.rules.logic.induction;

public class RegressionActionInductionParameters extends ActionInductionParameters {
    public enum RegressionOrder {
        BETTER,
        WORSE,
        ANY
    }

    protected RegressionOrder regressionOrder;

    protected boolean overlappingConsequencesAllowed;

    public RegressionActionInductionParameters(ActionFindingParameters params) {
        super(params);
    }


    public RegressionOrder getRegressionOrder() {
        return regressionOrder;
    }

    public void setRegressionOrder(RegressionOrder regressionOrder) {
        this.regressionOrder = regressionOrder;
    }

    public boolean getCanOverlapConsequences() { return overlappingConsequencesAllowed; }

    public void setCanOverlapConsequences(boolean value) {overlappingConsequencesAllowed = value;}
}
