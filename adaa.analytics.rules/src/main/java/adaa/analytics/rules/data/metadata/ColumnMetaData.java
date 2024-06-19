package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ColumnMetaData implements Serializable, IAttribute {

    private String name;
    private EColumnType colType;
    private String role;
    private NominalMapping mapping = new NominalMapping();
    private IExampleSet owner;
    private Map<EStatisticType, Double> statistics = new HashMap<>();

    public ColumnMetaData(@NotNull String columnName, @NotNull EColumnType columnType, @NotNull EColumnRole role, List<String> values, IExampleSet owner) {
        this(columnName, columnType, role.name(),values,owner);
    }

    public ColumnMetaData(@NotNull String columnName, @NotNull EColumnType columnType, @NotNull String role, List<String> values, IExampleSet owner) {
        this.name = columnName;
        this.colType = columnType;
        this.role = role;
        this.owner = owner;
        if (values!=null) {
            fillValueSet(values);
        }
    }

    public ColumnMetaData(@NotNull String columnName, @NotNull EColumnType columnType) {
        this(columnName, columnType, EColumnRole.regular,null,null);
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTableIndex() {
        if (owner == null) {
            throw new NullPointerException(String.format("Column %s has no owner", name));
        }

        return owner.getColumnIndex(name);
    }

    public boolean isNominal() {
        return colType == EColumnType.NOMINAL;
    }

    public boolean isNumerical() {
        return colType == EColumnType.NUMERICAL;
    }

    public boolean isDate() {
        return colType == EColumnType.DATE;
    }


    public void setRole(String role) {
        this.role = role;

        if(owner == null)
            return;

        if(role.equals(SurvivalRule.SURVIVAL_TIME_ROLE) ||
                (role.equals(EColumnRole.label.name()) &&
                        owner.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE) != null)) {

            IAttribute aLabel = owner.getAttributes().getLabel();
            if(aLabel == null)
                return;
            if(aLabel.isNominal()) {
                if(aLabel.getMapping().getValues().size() != 2) {
                    throw new IllegalArgumentException(String.format("Label attribute has to have 2 possible values {0, 1}. It has %d", aLabel.getMapping().getValues().size()));
                }
                String v0 = aLabel.getMapping().getValue(0);
                String v1 = aLabel.getMapping().getValue(1);
                if(v0.equals("1") && v1.equals("0")) {
                    aLabel.getMapping().clear();
                    aLabel.getMapping().mapString("0");
                    aLabel.getMapping().mapString("1");
                }
                else if(!v0.equals("0") || !v1.equals("1")) {
                    throw new IllegalArgumentException(String.format("Label attribute has wrong possible values {0, 1}. It has {%s, %s}",
                            v0, v1));
                }
            }
        }
    }

    public void setStatistic(EStatisticType statType, double value) {
        statistics.put(statType, value);
    }

    public double getStatistic(EStatisticType statType) {

        if (owner == null) {
            throw new NullPointerException(String.format("Column %s has no owner", name));
        }

        if (!statistics.containsKey(statType)) {
            owner.recalculateStatistics(statType, name);
        }

        return statistics.get(statType);
    }

    public void recalculateStatistics() {

        if (owner == null) {
            throw new NullPointerException(String.format("Column %s has no owner", name));
        }

        for (EStatisticType statType : EStatisticType.values()) {
            owner.recalculateStatistics(statType, name);
        }
    }

    public void setOwner(DataTable owner) {
        this.owner = owner;
    }

    IExampleSet getOwner() {
        return owner;
    }

    private void fillValueSet(List<String> values) {

        mapping.clear();
        if (values == null)
            return;

        int maxValues = Integer.MAX_VALUE;
        for (String value : values) {
            if (value == null) {
                continue;
            }

            mapping.addValue(value);
            maxValues--;
            if (maxValues == 0) {
                break;
            }
        }
    }

    public ColumnMetaData cloneWithNewOwner(DataTable newOwner) {
        ColumnMetaData cloned = this.clone();
        cloned.owner = newOwner;
        return cloned;
    }

    @Override
    public ColumnMetaData clone() {
        ColumnMetaData cloned = null;
        try {
            cloned = (ColumnMetaData) super.clone();
            cloned.mapping = mapping.clone();
            cloned.statistics = new HashMap<>();
            cloned.statistics.putAll(statistics);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColumnMetaData)) {
            return false;
        }
        ColumnMetaData cmd = (ColumnMetaData) o;
        return cmd.name.equals(name);
    }

    @Override
    public String getAsString(double value) {
        if (Double.isNaN(value)) {
            return "?";
        }

        if (isNominal()) {
            return getMapping().getValue((int) value);
        }else {
            return ""+value;
        }

    }

    public EColumnType getColumnType() {
        return colType;
    }

    public INominalMapping getMapping() {
        return mapping;
    }

}
