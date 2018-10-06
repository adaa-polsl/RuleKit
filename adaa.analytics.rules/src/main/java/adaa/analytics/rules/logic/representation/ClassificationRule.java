package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.Set;

public class ClassificationRule extends Rule {
	/**
	 * 
	 */
	private static final long serialVersionUID = -809625670611500594L;

	public ClassificationRule() {
		super();
	}
	
	public ClassificationRule(ClassificationRule ref) {
		super(ref);
	}
	
	public ClassificationRule(Rule ref) {
		super(ref);
	}
	
	public ClassificationRule(CompoundCondition premise, ElementaryCondition consequence) {
		super(premise, consequence);
	}

	/**
	 * Gets identifiers of examples covered by the rule. 
	 * @param set Example set.
	 * @param ids Collection of input identifiers.
	 * @return Collection of output identifiers.
	 */
	public Covering covers(ExampleSet set, Set<Integer> ids) {
		Covering covered = new Covering();
		
		for (int id : ids) {
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			boolean consequenceAgree = this.getConsequence().evaluate(ex);
			if (consequenceAgree) {
				covered.weighted_P += w;
			} else {
				covered.weighted_N += w;
			}
			
			if (this.getPremise().evaluate(ex)) {
				if (consequenceAgree) {
					covered.positives.add(id);
					covered.weighted_p += w;
				} else {
					covered.negatives.add(id);
					covered.weighted_n += w;
				}
			} 
		}
		return covered;
	}
	
	
	public Covering covers(ExampleSet set) {
		Covering covered = new Covering();
		covers(set, covered, covered.positives, covered.negatives);
		return covered;
	}
	
	public void covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives) {

		int id = 0;
		for (Example ex : set) {
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			boolean consequenceAgree = this.getConsequence().evaluate(ex);
			if (consequenceAgree) {
				ct.weighted_P += w;
			} else {
				ct.weighted_N += w;
			}
			
			if (this.getPremise().evaluate(ex)) {
				if (consequenceAgree) {
					positives.add(id);
					ct.weighted_p += w;
				} else {
					negatives.add(id);
					ct.weighted_n += w;
				}
			}
			++id;
		}
	}
}
