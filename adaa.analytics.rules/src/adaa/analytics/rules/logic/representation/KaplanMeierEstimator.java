package adaa.analytics.rules.logic.representation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

public class KaplanMeierEstimator {
	
	protected ArrayList<SurvInfo> survInfo = new ArrayList<SurvInfo>();
	
	public void addSurvInfo(double time, double probability) {
		survInfo.add(new SurvInfo(time, probability));
	}
		
	public KaplanMeierEstimator() {}
	
    public KaplanMeierEstimator(ExampleSet data) {
    	Attribute survTime = data.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE); 
		Attribute survStat = data.getAttributes().getSpecial(SurvivalRule.SURVIVAL_STATUS_ROLE);
		if (survStat == null) {
			survStat = data.getAttributes().getLabel();
		}
		
		this.survInfo.ensureCapacity(data.size());
        SurvInfo info[] = new SurvInfo[data.size()];	
        	
        int j = 0;
        for (Example e : data) {
        	double t = e.getValue(survTime);
        	boolean isCensored = (e.getValue(survStat) == 0);
        	
        	int eventsCount = isCensored ? 0 : 1;
        	info[j] = new SurvInfo(t, eventsCount, 1 - eventsCount);
        	++j;
        }
        
        Arrays.sort(info, new SurvInfoComparer(SurvInfoComparer.By.TimeAsc));
        assert info[0].getTime() <= info[info.length - 1].getTime();
        int atRiskCount = data.size();

        int idx = 0;
        while (idx < info.length) {
            double t = info[idx].getTime();
            int startIdx = idx;
            
            while (idx < info.length && info[idx].getTime() == t) {
                idx++;
            }

            int eventsAtTimeCount = 0;
            int censoredAtTimeCount = 0;
            for (int i = startIdx; i < idx; i++) {
                if (info[i].getEventsCount() == 1) {
                    eventsAtTimeCount++;
                } else {
                    assert (info[i].getCensoredCount() == 1);
                    censoredAtTimeCount++;                       
                }
            }

            SurvInfo si = new SurvInfo(t, eventsAtTimeCount, censoredAtTimeCount);
            si.setAtRiskCount(atRiskCount);

            this.survInfo.add(si);

            atRiskCount -= eventsAtTimeCount;
            atRiskCount -= censoredAtTimeCount;
        }

        this.calculateProbability();
    }
    
    public String save() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(survInfo.size() + ":");
    	for (SurvInfo si: survInfo) {
    		sb.append(si.time + " " + si.eventsCount + " " + si.censoredCount + " " + si.atRiskCount + ";" );
    	}
    	return sb.toString();
    }
    
    public void load(String s) {
    	int idx = s.indexOf(':');
    	int count = Integer.parseInt(s.substring(0, idx));
    	s = s.substring(idx);
    	
    	survInfo = new ArrayList<SurvInfo>(count);
    	
    	String[] parts = s.split(";");
    	
    	for (int i = 0; i < count; ++i) {
    		String[] numbers = parts[i].split(" ");
    		
    		survInfo.set(i, new SurvInfo(
				Double.parseDouble(numbers[0]),
				Integer.parseInt(numbers[1]),
				Integer.parseInt(numbers[2]),
				Integer.parseInt(numbers[3])
				));
    	}
    	
    	this.calculateProbability();
    }
     
    public KaplanMeierEstimator(ExampleSet data, Set<Integer> indices) {
		Attribute survTime = data.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE); 
		Attribute survStat = data.getAttributes().getSpecial(SurvivalRule.SURVIVAL_STATUS_ROLE);
		if (survStat == null) {
			survStat = data.getAttributes().getLabel();
		}
		
		this.survInfo.ensureCapacity(indices.size());
        SurvInfo[] info = new SurvInfo[indices.size()];	
        
        int j = 0;
        for (int id : indices) {
        	Example e = data.getExample(id);
        	double t = e.getValue(survTime);
        	boolean isCensored = (e.getValue(survStat) == 0);
        	
        	int eventsCount = isCensored ? 0 : 1;
        	info[j] = new SurvInfo(t, eventsCount, 1 - eventsCount);
        	++j;
        }
        
        Arrays.sort(info, new SurvInfoComparer(SurvInfoComparer.By.TimeAsc));
        assert info[0].getTime() <= info[info.length - 1].getTime();
        int atRiskCount = indices.size();

        int idx = 0;
        while (idx < info.length) {
            double t = info[idx].getTime();
            int startIdx = idx;
            
            while (idx < info.length && info[idx].getTime() == t) {
                idx++;
            }

            int eventsAtTimeCount = 0;
            int censoredAtTimeCount = 0;
            for (int i = startIdx; i < idx; i++) {
                if (info[i].getEventsCount() == 1) {
                    eventsAtTimeCount++;
                } else {
                    assert (info[i].getCensoredCount() == 1);
                    censoredAtTimeCount++;                       
                }
            }

            SurvInfo si = new SurvInfo(t, eventsAtTimeCount, censoredAtTimeCount);
            si.setAtRiskCount(atRiskCount);

            this.survInfo.add(si);

            atRiskCount -= eventsAtTimeCount;
            atRiskCount -= censoredAtTimeCount;
        }

        this.calculateProbability();
    }
    
    public static KaplanMeierEstimator average(KaplanMeierEstimator[] estimators) {
        //get unique times from all estimators
    	SortedSet<Double> uniqueTime = new TreeSet<Double>();
        for (KaplanMeierEstimator e : estimators) {
            uniqueTime.addAll(e.getTimes());
        }
        
        double[] time = new double[uniqueTime.size()];
        double[] probabilities = new double[uniqueTime.size()];
        
        // average probabilities for all time points
        Iterator<Double> t = uniqueTime.iterator();
        for (int i = 0; i < time.length; ++i) {
        	time[i] = t.next();
        	double p = 0;
        	for (KaplanMeierEstimator e: estimators) {
        		p+= e.getProbabilityAt(time[i]);
        		
        	}
        	probabilities[i] = p / estimators.length;
        }
        
        // get averaged estimator
        KaplanMeierEstimator avgKm = new KaplanMeierEstimator();
        for (int i = 0; i < time.length; i++) {
            avgKm.addSurvInfo(time[i], probabilities[i]);
        }

        return avgKm;
    }

    public ArrayList<Double> getTimes() {            
    	ArrayList<Double> times = new ArrayList<Double>(survInfo.size());
    	for (SurvInfo si : survInfo) {
            times.add(si.getTime());
        }
    	return times;
    }
    
    public double getProbabilityAt(double time) {
        int idx = Collections.binarySearch(survInfo, new SurvInfo(time), new SurvInfoComparer(SurvInfoComparer.By.TimeAsc));
    	
        if (idx >= 0) {
            return this.survInfo.get(idx).getProbability();
        }

        // bitwise complement of the index of the next element that is larger than item
        // or, if there is no larger element, the bitwise complement of Count
        idx = ~idx;

        int n = this.survInfo.size();
        if (idx == n) {
            return this.survInfo.get(n - 1).getProbability();
        }

        if (idx == 0) {
            return 1.0;
        }

        double p = this.survInfo.get(idx - 1).getProbability();
        assert (p != Double.NaN);
        return p;
    }
    
    public int getEventsCountAt(double time) {
    	 int idx = Collections.binarySearch(survInfo, new SurvInfo(time), new SurvInfoComparer(SurvInfoComparer.By.TimeAsc));
     	
         if (idx >= 0) {
             return this.survInfo.get(idx).getEventsCount();
         }
         
         return 0;
    }
    	
    public int getRiskSetCountAt(double time) {
    	 int idx = Collections.binarySearch(survInfo, new SurvInfo(time), new SurvInfoComparer(SurvInfoComparer.By.TimeAsc));
      	
         if (idx >= 0) {
             return this.survInfo.get(idx).getAtRiskCount();
         }
   	
        // bitwise complement of the index of the next element that is larger than item
        // or, if there is no larger element, the bitwise complement of Count
        idx = ~idx;

        int n = this.survInfo.size();
        if (idx == n) {
            return this.survInfo.get(n - 1).getAtRiskCount();
        }

        return this.survInfo.get(idx).getAtRiskCount();
    }
    
    
    public KaplanMeierEstimator reverse() {
    	KaplanMeierEstimator revKm = new KaplanMeierEstimator();
    	for (int i = 0; i < this.survInfo.size(); i++) {
    		SurvInfo si = this.survInfo.get(i);
    		SurvInfo revSi = new SurvInfo(si.getTime(), si.getCensoredCount(), si.getEventsCount());
            revSi.setAtRiskCount(si.getAtRiskCount());    

    		revKm.survInfo.add(revSi);
    	}
         revKm.calculateProbability();
         return revKm;
    }
    
    protected void calculateProbability() {
        
    	//Debug.Assert(new HashSet<double>(this.survInfo.Select(s => s.Time)).Count == this.survInfo.Count);
        
        for (int i = 0; i < this.survInfo.size(); i++) {
            SurvInfo si = this.survInfo.get(i);
            
            assert (int)si.getProbability() == SurvInfo.NotAssigned;

            si.setProbability((si.getAtRiskCount() - si.getEventsCount()) / ((double)si.getAtRiskCount()));
            if (i > 0) {
                si.setProbability(si.getProbability() * this.survInfo.get(i - 1).getProbability());
            }

           assert si.getProbability() >= 0 && si.getProbability() <= 1;
        }           
    }
    
    /**
     * Class wrapping some survival information.
     * @author Adam
     *
     */
    class SurvInfo  {
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
        
        public SurvInfo(double time, int eventsCount, int censoredCount, int atRiskCount) {
            this(time, eventsCount, censoredCount);
            this.atRiskCount = atRiskCount;
        }
    	
        public SurvInfo(double time, double probability) {
            this(time);
        	this.probability = probability;
        }
    }
    
    /**
     * Class for comparing two survival information objects either by time or by probability.
     * @author Adam
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
