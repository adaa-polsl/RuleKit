/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.exampleset.SortedExampleSetEx;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a Kaplan-Meier estimator of survival function.
 * @author Adam Gudys
 *
 */
public class KaplanMeierEstimator implements Serializable {
	
	/** Serialization id. */
	private static final long serialVersionUID = -6949465091584014494L;
	
	/** Array of survival estimator points */
	//protected ArrayList<SurvInfo> survInfo = new ArrayList<SurvInfo>();

    public static final int NotAssigned = Integer.MIN_VALUE;

    public double[] times;
    public int[] eventsCounts;
    public int[] censoredCounts;
    public int[] atRiskCounts;
    public double[] probabilities;

    public int filled = 0;


    /**
	 * Adds a new time point to the estimator.
	 * @param time Survival time.
	 * @param probability Survival probability.
	 */
    /*
	public void addSurvInfo(double time, double probability) {
		survInfo.add(new SurvInfo(time, probability));
	}
	*/

	/**
	 * Creates empty instance.
	 */
	public KaplanMeierEstimator() {}

	/**
	 * Generates survival estimator function from survival data.
	 * @param data Example set with attribute of {@link SurvivalRule#SURVIVAL_TIME_ROLE}.
	 */
    public KaplanMeierEstimator(IExampleSet data) {
        this(data, new IntegerBitSet(data.size(), true));
    }

    /**
     * Converts estimator to the text.
     * @return Estimator in the text form.
     */
    public String save() {
    	StringBuilder sb = new StringBuilder();
    	/*
    	sb.append(survInfo.size() + ":");
    	for (SurvInfo si: survInfo) {
    		sb.append(si.time + " " + si.probability + " " );
    	}
    	 */
        sb.append(filled + ":");
        for (int i = 0; i < filled; ++i) {
            sb.append(times[i] + " " + probabilities[i] + " " );
        }
    	return sb.toString();
    }
    
    /**
     * Loads estimator from the text.
     * @param s Estimator in the text form.
     */
    public void load(String s) {
    	int idx = s.indexOf(':');
    	filled = Integer.parseInt(s.substring(0, idx));
    	s = s.substring(idx + 1);

        String[] numbers = s.split(" ");
        int num_idx = 0;

       this.reserve(filled);

        Arrays.fill(atRiskCounts, NotAssigned);
        Arrays.fill(censoredCounts, NotAssigned);
        Arrays.fill(eventsCounts, NotAssigned);

    	/*
    	survInfo = new ArrayList<SurvInfo>(count);

    	for (int i = 0; i < count; ++i) {
    		
    		survInfo.add(new SurvInfo(
				Double.parseDouble(numbers[num_idx++]),
				Double.parseDouble(numbers[num_idx++])
			));
    	}
    	*/

        for (int i = 0; i < filled; ++i) {
            times[i] = Double.parseDouble(numbers[num_idx++]);
            probabilities[i] = Double.parseDouble(numbers[num_idx++]);
        }
    }
    
    /**
	 * Generates survival estimator function from survival data.
	 * @param data Example set with attribute of {@link SurvivalRule#SURVIVAL_TIME_ROLE}.
	 * @param indices Indices of the examples to be taken into account when building the estimator. 
	 */
    public KaplanMeierEstimator(IExampleSet data, Set<Integer> indices) {

        SortedExampleSetEx set = (data instanceof SortedExampleSetEx) ? (SortedExampleSetEx)data : null;
        if (set == null) {
            throw new IllegalArgumentException("RegressionRules support only SortedExampleSetEx example sets");
        }

		this.reserve(indices.size());

        Arrays.fill(atRiskCounts, NotAssigned);
        Arrays.fill(censoredCounts, NotAssigned);
        Arrays.fill(eventsCounts, NotAssigned);
        Arrays.fill(probabilities, NotAssigned);

        int atRiskCount = indices.size();

        Iterator<Integer> it = indices.iterator();
        int i = 0;

        int eventsAtTimeCount = 0;
        int censoredAtTimeCount = 0;
        double prev_t = -1;

        while (it.hasNext()) {
            int id = it.next();
            double t = set.survivalTimes[id];

            // time point has changed - add surv info
            if (t != prev_t && prev_t > 0) {
                times[filled] = prev_t;
                eventsCounts[filled] = eventsAtTimeCount;
                censoredCounts[filled] = censoredAtTimeCount;
                atRiskCounts[filled] = atRiskCount;

                ++filled;

                atRiskCount -= eventsAtTimeCount + censoredAtTimeCount;

                eventsAtTimeCount = 0;
                censoredAtTimeCount = 0;
            }

            if (set.labels[id] == 1) {
                ++eventsAtTimeCount;
            } else {
                ++censoredAtTimeCount;
            }

            prev_t = t;
        }

        times[filled] = prev_t;
        eventsCounts[filled] = eventsAtTimeCount;
        censoredCounts[filled] = censoredAtTimeCount;
        atRiskCounts[filled] = atRiskCount;

        ++filled;

        this.calculateProbability();
    }

    protected void reserve(int size) {
        times = new double[size];
        atRiskCounts = new int[size];
        censoredCounts = new int[size];
        eventsCounts = new int[size];
        probabilities = new double[size];
    }
    
    /**
     * Average several estimators.
     * @param estimators Array of estimators to be averaged.
     * @return Average estimator.
     */
    public static KaplanMeierEstimator average(KaplanMeierEstimator[] estimators) {
        //get unique times from all estimators
    	SortedSet<Double> uniqueTime = new TreeSet<Double>();
        for (KaplanMeierEstimator e : estimators) {
            uniqueTime.addAll(e.getTimes());
        }

        // get averaged estimator
        KaplanMeierEstimator avgKm = new KaplanMeierEstimator();
        avgKm.reserve(uniqueTime.size());
        avgKm.filled = uniqueTime.size();

        Arrays.fill(avgKm.atRiskCounts, NotAssigned);
        Arrays.fill(avgKm.censoredCounts, NotAssigned);
        Arrays.fill(avgKm.eventsCounts, NotAssigned);

        // average probabilities for all time points
        Iterator<Double> it= uniqueTime.iterator();
        for (int i = 0; i < avgKm.times.length; ++i) {
        	double t = it.next();
            double p = 0;

        	for (KaplanMeierEstimator e: estimators) {
        		p += e.getProbabilityAt(t);
        	}
            avgKm.times[i] = t;
            avgKm.probabilities[i] = p / estimators.length;
        }

        return avgKm;
    }

    /**
     * Extracts time points from estimator.
     * @return Array of time points.
     */
    public ArrayList<Double> getTimes() {            
    	/*
        ArrayList<Double> times = new ArrayList<Double>(survInfo.size());
    	for (SurvInfo si : survInfo) {
            times.add(si.getTime());
        }
    	 */
        ArrayList<Double> out = new ArrayList<Double>();
        for (int i = 0; i < filled; ++i) {
            out.add(times[i]);
        }
    	return out;
    }
    
    /**
     * Calculates survival probability at given time.
     * @param time Time.
     * @return Survival probability.
     */
    public double getProbabilityAt(double time) {

        int idx = Arrays.binarySearch(times, 0, filled, time);

        if (idx >= 0) {
            return probabilities[idx];
        }

        idx = ~idx;

        if (idx == filled) {
            return probabilities[filled - 1];
        }

        if (idx == 0) {
            return 1.0;
        }

        double p = probabilities[idx - 1];
        assert (p != Double.NaN);
        return p;
    }


    /**
     * Gets number of events at given time point.
     * @param time Time.
     * @return Number of events.
     */
    public int getEventsCountAt(double time) {
        int idx = Arrays.binarySearch(times, 0, filled, time);
        if (idx >= 0) {
            return eventsCounts[idx];
        }
         
        return 0;
    }
    	
    /**
     * Gets risk at given time.
     * @param time Time.
     * @return Risk.
     */
    public int getRiskSetCountAt(double time) {
        int idx = Arrays.binarySearch(times, 0, filled, time);

        if (idx < 0) {
            idx = ~idx;
            if (idx == filled) {
              --idx;
            }
        }

        return atRiskCounts[idx];
    }
    
    /**
     * Creates reveresed K-M estimator.
     * @return Reversed estimator.
     */
    public KaplanMeierEstimator reverse() {
    	 KaplanMeierEstimator revKm = new KaplanMeierEstimator();

         revKm.filled = filled;
         revKm.probabilities = probabilities.clone();
         revKm.times = times.clone();
         revKm.atRiskCounts = atRiskCounts.clone();
         // switch places
         revKm.censoredCounts = eventsCounts.clone();
         revKm.eventsCounts = censoredCounts.clone();

         revKm.calculateProbability();
         return revKm;
    }
    
    /**
     * Fills the probabilities in K-M estimator.
     */
    protected void calculateProbability() {
        for (int i = 0; i < filled; i++) {
        //    assert probabilities[i] == SurvInfo.NotAssigned;
            probabilities[i] = (atRiskCounts[i] - eventsCounts[i]) / (double)atRiskCounts[i];
            if (i > 0) {
                probabilities[i] *= probabilities[i-1];
            }

        //    assert probabilities[i] >= 0 && probabilities[i] <= 1;
        }
    }
    
    /**
     * Class wrapping some survival information.
     * @author Adam Gudys
     *
     */
    class SurvInfo  implements Serializable {
        
		private static final long serialVersionUID = 8276994296125818327L;

		public static final int NotAssigned = Integer.MIN_VALUE;

        protected double time;
        private int eventsCount = NotAssigned;
        private int censoredCount = NotAssigned;
        private int atRiskCount = NotAssigned;
        private double probability = NotAssigned;
   	        
        public double getTime() { return time; }
        public void setTime(double v) { time = v; }
        
    	public int getEventsCount() { return eventsCount; }
		public void setEventsCount(int eventsCount) { this.eventsCount = eventsCount; }
		
		public int getCensoredCount() { return censoredCount; }
		public void setCensoredCount(int censoredCount) { this.censoredCount = censoredCount; }

		public int getAtRiskCount() { return atRiskCount; }
		public void setAtRiskCount(int atRisk) { this.atRiskCount = atRisk; }

		public double getProbability() { return probability; }
		public void setProbability(double probability) { this.probability = probability; }
        
        public SurvInfo(double time) {
            this.time = time;
        }
        
        public SurvInfo(double time, int eventsCount, int censoredCount) {
            this.time = time;
            this.eventsCount = eventsCount;
            this.censoredCount = censoredCount;
        }
        
        public SurvInfo(double time, double probability) {
            this(time);
        	this.probability = probability;
        }
    }
    
    /**
     * Class for comparing two survival information objects either by time or by probability.
     * @author Adam Gudys
     *
     */
    static class SurvInfoComparer implements Comparator<SurvInfo> {
        private By by;

        public SurvInfoComparer(By by) {
            this.by = by;
        }

        public enum By {
            TimeAsc,
            ProbabilityDesc
        }

		@Override
		public int compare(SurvInfo x, SurvInfo y) {
			if (this.by == By.TimeAsc) {
                return Double.compare(x.getTime(), y.getTime());
            } else { //if (this.by == By.ProbabilityDesc) {
                return Double.compare(y.getProbability(), x.getProbability());
            }
		}
    }
}
