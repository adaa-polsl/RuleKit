package adaa.analytics.rules.data;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ColumnMetaData implements Cloneable {

    private String name;
	private EColumnType colType;
	private String role;
	private NominalMapping mapping = new NominalMapping();
	private DataTable owner;
	private Map<EStatisticType, Double> statistics = new HashMap<>();

	public ColumnMetaData(@NotNull String columnName, @NotNull EColumnType columnType, @NotNull String role, List<String> values, DataTable owner) {

		this.name = columnName;
		this.colType = columnType;
		this.role = role;
		this.owner = owner;
		fillValueSet(values);
	}

	public ColumnMetaData(@NotNull String columnName, @NotNull EColumnType columnType) {

		this.name = columnName;
		this.colType = columnType;
		this.role = EColumnRole.regular.name();
		this.owner = null;
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
		if(owner == null) {
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

	public EColumnType getColumnType() {
		return colType;
	}

	public NominalMapping getMapping() {
		return mapping;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public DataTable getOwner() {
		return owner;
	}

	public void setStatistic(EStatisticType statType, double value) {
		statistics.put(statType, value);
	}

	public double getStatistic(EStatisticType statType) {

		if(owner == null) {
			throw new NullPointerException(String.format("Column %s has no owner", name));
		}

		if(!statistics.containsKey(statType)) {
			owner.recalculateStatistics(statType, name);
		}

		return statistics.get(statType);
	}

	public void recalculateStatistics(EStatisticType statType) {

		if(owner == null) {
			throw new NullPointerException(String.format("Column %s has no owner", name));
		}

		owner.recalculateStatistics(statType, name);
	}

	public void recalculateStatistics() {

		if(owner == null) {
			throw new NullPointerException(String.format("Column %s has no owner", name));
		}

		for(EStatisticType statType : EStatisticType.values()) {
			owner.recalculateStatistics(statType, name);
		}
	}

	public boolean isNominalMappingEqual(ColumnMetaData colMetaData) {
		return mapping.equal(colMetaData.getMapping());
	}

	private void fillValueSet(List<String> values) {

		mapping.clear();
		if(values == null)
			return;

		int maxValues =  Integer.MAX_VALUE;
		for (int i=0 ; i<values.size() ; i++) {
			String value = values.get(i);
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
		return Objects.hash(name, colType, role, mapping);
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ColumnMetaData)) {
			return false;
		}
		ColumnMetaData cmd = (ColumnMetaData) o;
		if(!cmd.name.equals(name)) {
			return false;
		}
		if(!cmd.colType.equals(colType)) {
			return false;
		}
		if(!cmd.role.equals(role)) {
			return false;
		}
		return cmd.mapping.equals(mapping);
	}
}
