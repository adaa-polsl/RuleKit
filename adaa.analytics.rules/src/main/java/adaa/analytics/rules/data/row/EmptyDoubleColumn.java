package adaa.analytics.rules.data.row;

import tech.tablesaw.api.DoubleColumn;

public class EmptyDoubleColumn extends DoubleColumn {

    public EmptyDoubleColumn() {
        super("");
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }
}
