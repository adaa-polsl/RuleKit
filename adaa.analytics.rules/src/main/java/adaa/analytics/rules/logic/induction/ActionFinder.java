package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;
import org.renjin.repackaged.guava.collect.ImmutableSet;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;
import java.util.logging.Level;

public class ActionFinder extends AbstractFinder {
	
	private Set<Integer> uncoveredNegatives;
	private ClassificationFinder classificationFinder;
	void setUncoveredNegatives(Set<Integer> toSet) {
		uncoveredNegatives = toSet;
	}
	
	public Set<Integer> getUncoveredNegatives() {
		return uncoveredNegatives;
	}

	public ActionFinder(ActionInductionParameters params) {
		super(new ActionInductionParameters(params));
		classificationFinder = new ClassificationFinder(params);
	}

	public ExampleSet preprocess(ExampleSet trainSet) {
		return classificationFinder.preprocess(trainSet);
	}

	private void log(String msg, Level level) {
		if (msg.endsWith(System.lineSeparator()))
			Logger.log(msg, level);
		else
			Logger.log(msg + System.lineSeparator(), level);
	}

	private ConditionBase getBestElementaryCondition(
			Set<ElementaryCondition> conditions,
			ExampleSet trainSet,
			Set<Integer> positives,
			Rule rule) {

		ConditionEvaluation best = new ConditionEvaluation();
		log("Entering getBestElementaryCondition()", Level.FINEST);
		for (ConditionBase cond : conditions) {
			//extend rule with condition
			rule.getPremise().addSubcondition(cond);

			Covering cov = new Covering();
			rule.covers(trainSet, cov, cov.positives, cov.negatives);
			Covering covAll = new Covering();
			rule.covers(trainSet, covAll);
			Set<Integer> intersection = new HashSet<>(cov.positives);
			intersection.retainAll(positives);
			double quality = params.getInductionMeasure().calculate(trainSet, covAll);
			log("Asessing condition " + cond + " at quality= " + quality, Level.FINEST );
			if (/*cov.weighted_p*/ intersection.size() >= params.getMinimumCovered()
					&&
					((Double.compare(quality, best.quality) > 0) || (Double.compare(quality, best.quality) == 0 && cov.weighted_p > best.covered )))
			{
				rule.setCoveringInformation(cov);
				best.quality = quality;
				best.condition = cond;
				best.covered = cov.weighted_p;
			}
			
			//clean it up
			rule.getPremise().removeSubcondition(cond);
		}
		log("Adding condition " + best.condition + " at quality " + best.quality, Level.FINE);
		log("Exiting getBestElementaryCondition()", Level.FINEST);
		return best.condition;
	}

	private void getElementaryConditionForAttribute(
			ExampleSet trainSet,
			Set<Integer> coveredByRule,
			Set<ElementaryCondition> conditions,
			String attributeName) {
		
		Attribute attribute = trainSet.getAttributes().get(attributeName);
		Set<Double> attributeValues = new HashSet<>();
		
		if (attribute.isNominal()) {

			//We take only attribute values present in Dr - already covered examples
			for (int id : coveredByRule) {
				
				Example ex = trainSet.getExample(id);
				double value = ex.getValue(attribute);
				//ignore missing
				if (Double.isNaN(value))
					continue;

				attributeValues.add(value);
			}
			
			for (double val : attributeValues) {
				conditions.add(
						new ElementaryCondition(
								attributeName, 
								new SingletonSet(val, attribute.getMapping().getValues())));
			}
			
		} else {
			//numerical attribute - have to find midpoints
			attributeValues = new TreeSet<>();
			for (int id : coveredByRule) {
				
				Example ex = trainSet.getExample(id);
				double val = ex.getValue(attribute);
				if (Double.isNaN(val)) continue;
				attributeValues.add(val);
			}
			
			HashSet<Double> midPoints = new HashSet<>();
			attributeValues.stream().reduce( (a,b) -> {midPoints.add((a + b) /2.0); return b;});

			for (double midPoint : midPoints) {
				
				conditions.add(new ElementaryCondition(attributeName, Interval.create_le(midPoint)));
				conditions.add(new ElementaryCondition(attributeName, Interval.create_geq(midPoint)));
			}
							
		}
	}
	
	private Action buildAction(ElementaryCondition left, ElementaryCondition right) throws Exception {
		
		return new Action(left.getAttribute(), left.getValueSet(), right == null ? null : right.getValueSet());
	}

	/**
	 * Adds elementary actions to the rule premise until termination conditions are fulfilled.
	 *
	 * @param rule Rule to be grown.
	 * @param dataset Training set.
	 * @param uncovered Collection of examples yet uncovered by the model by the source of the action rule
	 * @return Number of conditions added.
	 */
	public int grow(
			final Rule rule,
			final ExampleSet dataset,
			final Set<Integer> uncovered) {

		Logger.log("AbstractFinder.grow()\n", Level.FINE);

		int initialConditionsCount = rule.getPremise().getSubconditions().size();

		// get current covering
		Set<Integer> covered = new IntegerBitSet(dataset.size());
		covered.addAll(rule.getCoveredPositives());
		covered.addAll(rule.getCoveredNegatives());

		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());

		Set<Attribute> allowedAttributes = new TreeSet<>(new AttributeComparator());
		for (Attribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}

		// add conditions to rule
		boolean carryOn = true;

		do {
			ElementaryCondition condition = induceCondition(
					rule, dataset, uncovered, covered, allowedAttributes);

			if (condition != null) {
				carryOn = tryAddCondition(rule,condition, dataset, covered, conditionCovered);

				if (params.getMaxGrowingConditions() > 0) {
					if (rule.getPremise().getSubconditions().size() - initialConditionsCount >=
							params.getMaxGrowingConditions() * dataset.getAttributes().size()) {
						carryOn = false;
					}
				}
			} else {
				carryOn = false;
			}

		} while (carryOn);

		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);

		return addedConditionsCount;
	}

	/***
	 * Makes an attempt to add the condition to the rule.
	 *
	 * @param rule Rule to be updated.
	 * @param condition Condition to be added.
	 * @param trainSet Training set.
	 * @param covered Set of examples covered by the rules.
	 * @param conditionCovered Bit vector of examples covered by the condition.
	 * @return Flag indicating whether condition has been added successfully.
	 */
	public boolean tryAddCondition(
			final Rule rule,
			final ConditionBase condition,
			final ExampleSet trainSet,
			final Set<Integer> covered,
			final IntegerBitSet conditionCovered) {

		boolean carryOn = true;
		boolean add = false;
		ContingencyTable ct = new ContingencyTable();

		if (condition != null) {
			conditionCovered.clear();
			condition.evaluate(trainSet, conditionCovered);

			ct.weighted_P = rule.getWeighted_P();
			ct.weighted_N = rule.getWeighted_N();

			if (trainSet.getAttributes().getWeight() != null) {
				// calculate weights

			} else {
				ct.weighted_p = rule.getCoveredPositives().calculateIntersectionSize(conditionCovered);
				ct.weighted_n = rule.getCoveredNegatives().calculateIntersectionSize(conditionCovered);
			}

			// analyse stopping criteria
			double adjustedMinCov = Math.min(
					params.getMinimumCovered(),
					Math.max(1.0, 0.2 * ct.weighted_P));

			if (ct.weighted_p < adjustedMinCov) {
				if (rule.getPremise().getSubconditions().size() == 0) {
					// special case of empty rule - add condition anyway
					//		add = true;
				}
				carryOn = false;
			} else {
				// exact rule
				if (ct.weighted_n == 0) {
					carryOn = false;
				}
				add = true;
			}

			// update coverage if condition was added
			if (add) {
				rule.getPremise().getSubconditions().add(condition);

				covered.retainAll(conditionCovered);
				rule.getCoveredPositives().retainAll(conditionCovered);
				rule.getCoveredNegatives().retainAll(conditionCovered);

				rule.setWeighted_p(ct.weighted_p);
				rule.setWeighted_n(ct.weighted_n);

				((ActionRule)rule).calculatePValue(trainSet, (ClassificationMeasure)params.getInductionMeasure());


				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: "
						+ rule.getPremise().toString() + " " + rule.printStats() + "\n", Level.FINER);
			}
		}
		else {
			carryOn = false;
		}

		return carryOn;
	}

	@Override
	protected ElementaryCondition induceCondition(
			Rule rule,
			ExampleSet trainSet, 
			Set<Integer> uncoveredByRuleset, //uncovered positives
			Set<Integer> coveredByRule,
			Set<Attribute> allowedAttributes,
			Object... extraParams) {
		
		ActionRule aRule = rule instanceof ActionRule ? (ActionRule)rule : null;
		
		if (aRule == null)
			return null;
		
		Rule posRule = aRule.getLeftRule();
		Rule negRule = aRule.getRightRule();

		ConditionBase _best = classificationFinder.induceCondition(posRule, trainSet, uncoveredByRuleset, coveredByRule, allowedAttributes, extraParams);

		/*Set<ElementaryCondition> conds = this.generateElementaryConditions(trainSet, allowedAttributes, coveredByRule);
		ConditionBase _best = this.getBestElementaryCondition(conds, trainSet, uncoveredByRuleset, posRule);
*/
		if (_best == null)
			return null;
		
		ElementaryCondition best = (ElementaryCondition)_best;
		
		Attribute usedAttribute = trainSet.getAttributes().get(best.getAttribute());
		if (usedAttribute.isNominal()){
			allowedAttributes.remove(usedAttribute);
		}

		//if the attribute is stable, the action must be of form (attribute, x -> x);
		if (((ActionInductionParameters)params).stableAttributes.contains(usedAttribute.getName())) {
			return new Action(usedAttribute.getName(), best.getValueSet(), best.getValueSet());
		}

		Set<Integer> coveredByNegRule = negRule.covers(trainSet).positives;		
		Set<ElementaryCondition> conditionsForNegativeRule = new HashSet<>();

/*
		See if we can use classificationFinder even more

		ConditionBase _otherBest = classificationFinder.induceCondition(negRule, trainSet, <>,
				coveredByNegRule, Sets.newHashSet(usedAttribute), extraParams);
*/
		this.getElementaryConditionForAttribute(trainSet, coveredByNegRule, conditionsForNegativeRule, best.getAttribute());
		
		ActionInductionParameters actionParams = (ActionInductionParameters)params;
		Set<ElementaryCondition> toInduceForNegRule;
		
		if (usedAttribute.isNumerical()) {
			toInduceForNegRule = actionParams.getActionFindingParameters().getRangeStrategy(best, trainSet).filter(conditionsForNegativeRule);
		} else {
			toInduceForNegRule = conditionsForNegativeRule;
		}
		ConditionBase otherBest = this.getBestElementaryCondition(toInduceForNegRule, trainSet, uncoveredNegatives, negRule);

		/*if (best.equals(otherBest)) {
			return null;
		}*/
		
		Action proposedAction;
		try {
			proposedAction = this.buildAction(best, (ElementaryCondition)otherBest);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if(aRule.getPremise().getSubconditions().contains(proposedAction)) {
			return null;
		}
		return proposedAction;
	}
	
	private double calculateActionQuality(Covering covering, IQualityMeasure measure) {
		return measure.calculate(covering.weighted_p, covering.weighted_n, covering.weighted_P, covering.weighted_N);
	}

	public void prune(Rule rule_, final ExampleSet trainSet, Set<Integer> uncoveredPositives) {
		ActionRule rule = rule_ instanceof ActionRule ? (ActionRule)rule_ : null;
		if (rule == null) {
			throw new RuntimeException("Not an actionrule in actionrule pruning!");
		}

		Rule source = rule.getLeftRule();
		Covering lCov = source.covers(trainSet);
		source.setCoveringInformation(lCov);
		source.setCoveredPositives(new IntegerBitSet(trainSet.size()));
		source.getCoveredPositives().addAll(lCov.positives);
		source.setCoveredNegatives(new IntegerBitSet(trainSet.size()));
		source.getCoveredNegatives().addAll(lCov.negatives);

		Rule target = rule.getRightRule();

		Covering rCov = target.covers(trainSet);
		target.setCoveredPositives(new IntegerBitSet(trainSet.size()));
		target.getCoveredPositives().addAll(rCov.positives);
		target.setCoveredNegatives(new IntegerBitSet(trainSet.size()));
		target.getCoveredNegatives().addAll(rCov.negatives);

		this.classificationFinder.prune(source, trainSet, uncoveredPositives);
		this.classificationFinder.prune(target, trainSet, uncoveredPositives);

		CompoundCondition newPremise = new CompoundCondition();
		for (ConditionBase cond : source.getPremise().getSubconditions()) {
			if (cond instanceof ElementaryCondition) {
				ElementaryCondition conditionLeft = (ElementaryCondition)cond;

				ElementaryCondition conditionRight = target.getPremise().getSubconditions()
						.stream()
						.map(ElementaryCondition.class::cast)
						.filter(x -> x.getAttribute().equals(conditionLeft.getAttribute()))
						.findFirst()
						.orElse(null);

				newPremise.addSubcondition(new Action(conditionLeft, conditionRight));
			}
		}
		for (ConditionBase cond : target.getPremise().getSubconditions()) {
			if (cond instanceof ElementaryCondition) {
				ElementaryCondition conditionRight = (ElementaryCondition)cond;

				ElementaryCondition condLeft = source.getPremise().getSubconditions()
						.stream()
						.filter(ElementaryCondition.class::isInstance)
						.map(ElementaryCondition.class::cast)
						.filter(x -> x.getAttribute().equals(conditionRight.getAttribute()))
						.findFirst()
						.orElse(null);

				if (condLeft == null) {
					newPremise.addSubcondition(new Action(null, conditionRight));
				}
			}
		}
		ActionCovering aCov = new ActionCovering();
		rule.setPremise(newPremise);
		rule.covers(trainSet, aCov, aCov.positives, aCov.negatives);
		rule.setCoveringInformation(aCov);
		rule.getCoveredPositives().addAll(aCov.positives);
		rule.getCoveredNegatives().addAll(aCov.negatives);
	}

	//@Override
	public Covering prune3(final Rule rule_, final ExampleSet trainSet) {
		
		log("Entering ActionFinder.prune()", Level.FINEST);
		ActionRule rule = rule_ instanceof ActionRule ? (ActionRule)rule_ : null;
		
		if (rule == null) {
			throw new RuntimeException("Not an actionrule in actionrule pruning!");
		}
		
		// check preconditions
		if (Double.isNaN(rule.getWeighted_p()) || Double.isNaN(rule.getWeighted_n()) ||
			Double.isNaN(rule.getWeighted_P()) || Double.isNaN(rule.getWeighted_N()) ) {
			throw new IllegalArgumentException();
		}
		
		int maskLength = (trainSet.size() + Long.SIZE - 1) / Long.SIZE;
		//during growing, nil action never will be constructed (?)
		int maskCount = rule.getPremise().getSubconditions().size();
		
		Rule leftRule = rule.getLeftRule();
		Rule rightRule = rule.getRightRule();
		
		Covering lCov = leftRule.covers(trainSet);
		leftRule.setCoveringInformation(lCov);

		rightRule.setCoveringInformation(rightRule.covers(trainSet));

		long[] masksLeft = new long[maskCount * maskLength];
		long[] labelMaskLeft = new long[maskLength];
		
		int maskCountRight =/* maskCount;//*/rightRule.getPremise().getSubconditions().size();
		long[] masksRight = new long[maskCountRight * maskLength];
		long[] labelMaskRight = new long[maskLength];
		
		for (int i = 0; i < trainSet.size(); i++) {
			
			Example ex = trainSet.getExample(i);
			
			int wordId = i / Long.SIZE;
			int wordOffset = i % Long.SIZE;
			
			if (leftRule.getConsequence().evaluate(ex)) {
				labelMaskLeft[wordId] |= 1L << wordOffset;
			}
			
			if (rightRule.getConsequence().evaluate(ex)) {
				labelMaskRight[wordId] |= 1L << wordOffset;
			}
			
			for (int m = 0; m < maskCount; ++m) {
				ConditionBase cnd = leftRule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(ex)) {
					masksLeft[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
			
			for (int m = 0; m < maskCountRight; m++) {
				ConditionBase cnd = rightRule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(ex)) {
					masksRight[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
		}
		
		Map<ConditionBase, Integer> condToMaskLeft = new HashMap<>();
		Map<ConditionBase, Integer> condToMaskRight = new HashMap<>();
		Set<ConditionBase> presentCondLeft = new HashSet<>();
		Set<ConditionBase> presentCondRight = new HashSet<>();

		
		for (int i = 0; i < maskCount; i++) {
			ConditionBase cndLeft = leftRule.getPremise().getSubconditions().get(i);
			condToMaskLeft.put(cndLeft, i);
			presentCondLeft.add(cndLeft);
			
			if (i < maskCountRight) {
				ConditionBase cndRight = rightRule.getPremise().getSubconditions().get(i);
				condToMaskRight.put(cndRight, i);
				presentCondRight.add(cndRight);
			}
		}

		Covering covering = rule.covers(trainSet);
		Covering coveringL = new Covering();
		rule.getLeftRule().covers(trainSet, coveringL);
		Covering coveringR = new Covering();
		rule.getRightRule().covers(trainSet,coveringR);

		double initialQualityR = params.getPruningMeasure().calculate(trainSet, coveringL);
		double initialQualityL = params.getPruningMeasure().calculate(trainSet, coveringR);
		boolean climbing = true;
		
		while(climbing) {
			
			ConditionBase toRemove = null;
			ConditionBase toRemoveAlreadyNil = null;
			double bestQualityAlreadyNil = Double.NEGATIVE_INFINITY;
			double bestQualityLeft = Double.NEGATIVE_INFINITY;
			double bestQualityRight = Double.NEGATIVE_INFINITY;
			boolean looseCondition = false;
			boolean alreadyNilPruning = false;
			long correctionWord = ~(0L);
			long correction = maskLength * Long.SIZE - trainSet.size();
			for (ConditionBase cnd_ : rule.getPremise().getSubconditions()) {
				
				Action cnd = cnd_ instanceof Action ? (Action)cnd_ : null;
				if (cnd == null) {
					throw new RuntimeException("Impossible at that phase");
				}
				
				if (!cnd.isPrunable()) continue;

				presentCondLeft.remove(cnd.getLeftCondition());
				if (!cnd.getActionNil()) {
					presentCondRight.remove(cnd.getRightCondition());
				}
				
				double pLeft = 0.0, nLeft = 0.0;
				double pRight = 0.0, nRight = 0.0;



				for (int wordId = 0; wordId < maskLength; ++wordId) {
					long word = ~(0L);
					long labelWord = labelMaskLeft[wordId];
					if (wordId == maskLength - 1) {
						correctionWord = ~(0L);
						correction = maskLength * Long.SIZE - trainSet.size();
						assert correction < Long.SIZE;
						correctionWord <<= correction;
						labelWord <<= correction;
						word &= correctionWord;
					}

					// iterate over all present conditions
					for (ConditionBase other : presentCondLeft) {
						int m = condToMaskLeft.get(other);
						word &= masksLeft[m * maskLength + wordId];
					}

					// no weighting - use popcount
					if (trainSet.getAttributes().getWeight() == null) {
						pLeft += Long.bitCount(word & labelWord);
						nLeft += Long.bitCount(word & ~labelWord);
					} else {
						long posWord = word & labelWord;
						long negWord = word & ~labelWord;
						for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
							if ((posWord & (1L << wordOffset)) != 0) {
								pLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((negWord & (1L << wordOffset)) != 0) {
								nLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
						}
					}
					
					word = ~(0L);
					labelWord = labelMaskRight[wordId];
					//double qualityRight = Double.NEGATIVE_INFINITY;
					if (wordId == maskLength - 1) {
						correctionWord = ~(0L);
						correction = maskLength * Long.SIZE - trainSet.size();
						assert correction < Long.SIZE;
						correctionWord <<= correction;
						labelWord <<= correction;
						word &= correctionWord;
					}

					for (ConditionBase other : presentCondRight) {
						int m = condToMaskRight.get(other);
						word &= masksRight[m * maskLength + wordId];
					}

					if (trainSet.getAttributes().getWeight() == null) {
						pRight += Long.bitCount(word & labelWord);
						nRight += Long.bitCount(word & ~labelWord);
					} else {
						long posWord = word & labelWord;
						long negWord = word & ~labelWord;
						for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
							if ((posWord & (1L << wordOffset)) != 0) {
								pRight += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((negWord & (1L << wordOffset)) != 0) {
								nRight += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
						}
					}
				}

				presentCondLeft.add(cnd.getLeftCondition());
				if (!cnd.getActionNil()){
					presentCondRight.add(cnd.getRightCondition());
				}

				log("Assesing " + cnd + " during rule pruning", Level.FINEST);
				if (cnd.getActionNil()) {
					log("note that this is already nil action", Level.FINEST);
				}
				double qualityRight = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
						pRight, nRight, rightRule.getWeighted_P(), rightRule.getWeighted_N());
				
				double qualityLeft = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
						pLeft, nLeft, leftRule.getWeighted_P(), leftRule.getWeighted_N());

				log("When removing " + cnd.getLeftCondition() + " quality = " + qualityLeft, Level.FINEST);
				if (!cnd.getActionNil()) {
					log("When removing " + cnd.getRightCondition() + " quality = " + qualityRight, Level.FINEST);
				}

				if (cnd.getActionNil()) {
					if (qualityLeft >= bestQualityLeft ) {
						bestQualityAlreadyNil = qualityLeft;
						toRemoveAlreadyNil = cnd;
						alreadyNilPruning = true;
					}
				} else {
					if (Double.compare(qualityRight, bestQualityRight) >= 0) {
						looseCondition = false;
						alreadyNilPruning = false;
						bestQualityRight = qualityRight;
						toRemove = cnd;
						if (Double.compare(qualityLeft, bestQualityLeft) >= 0) {
							bestQualityLeft = qualityLeft;
							looseCondition = true;
						}
					}
				}
			}
			
			Action act = (Action)toRemove;
			
			if (act == null) {
				climbing = false;
				continue;
			}
			
			boolean leftBetter = bestQualityLeft >= initialQualityL && looseCondition;
			boolean rightBetter = bestQualityRight >= initialQualityR;
			
			if (alreadyNilPruning && bestQualityAlreadyNil > initialQualityL) {
				
				initialQualityL = bestQualityLeft;
				presentCondLeft.remove(((Action)toRemoveAlreadyNil).getLeftCondition());
				rule.getPremise().removeSubcondition(toRemoveAlreadyNil);
				log("Already nil removal " + toRemoveAlreadyNil + "at quality " + bestQualityAlreadyNil, Level.FINEST);
			} else if (rightBetter) {
				initialQualityR = bestQualityRight;
				presentCondRight.remove(((Action)toRemove).getRightCondition());
				
				if (leftBetter) {
					
					initialQualityL = bestQualityLeft;
					presentCondLeft.remove(((Action)toRemove).getLeftCondition());
					rule.getPremise().removeSubcondition(toRemove);
					log("Removing " + (Action)toRemove + "at quality left " + bestQualityLeft + " quality right " + bestQualityRight, Level.FINEST);
				} else {
				
					rule.getPremise().removeSubcondition(toRemove);

					//act.setActionNil(true);
					Action withRemovedTargetSide = new Action(act.getAttribute(), act.getLeftValue(), null);
					rule.getPremise().addSubcondition(withRemovedTargetSide);
				}
			} else  {
				climbing = false;
			}
			
			if (rule.getPremise().getSubconditions().size() == 1) {
				climbing = false;
			}
		}
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation(covering);
		
		double weight = calculateActionQuality(covering, params.getVotingMeasure());
		rule.setWeight(weight);
		log("rule pruned: " + rule , Level.FINEST);
		log("exiting ActionFinder.prune", Level.FINEST);
		return covering;
	}
}
