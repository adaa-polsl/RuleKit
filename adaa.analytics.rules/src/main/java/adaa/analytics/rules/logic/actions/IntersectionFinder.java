package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import adaa.analytics.rules.logic.representation.Interval;

public class IntersectionFinder {

	public IntersectionFinder() {
		// TODO Auto-generated constructor stub
	}

	
	public List<Interval> calculateAllIntersectionsOf(List<Interval> intervals) {

		//create mandatory limits
		double left = intervals.stream().min(Comparator.comparing(Interval::getLeft)).map(Interval::getLeft).orElseThrow(()->new RuntimeException("Empty interval list!"));
		double right = intervals.stream().max(Comparator.comparing(Interval::getRight)).map(Interval::getRight).orElseThrow(()->new RuntimeException("Empty interva list!"));
		
		Interval lowerBound = Interval.create_le(left);
		Interval upperBound = Interval.create_geq(right);
		
		// finds all intersection point : each with each
		Set<Double> points = new TreeSet<Double>();
		for (int i = 0; i < intervals.size() - 1; i++) {
			int offset = 1;
			Interval curr = intervals.get(i);
			Interval next = intervals.get(i + offset);

			points.add(curr.getLeft());
			points.add(curr.getRight());

			while (curr.intersects(next)) {

				Interval intersection = (Interval) curr.getIntersection(next);
				points.add(intersection.getLeft());
				points.add(intersection.getRight());

				offset++;
				if (i + offset >= intervals.size()) {
					break;
				}
				next = intervals.get(i + offset);
			}
		}
		points.add(intervals.get(intervals.size()-1).getLeft());
		points.add(intervals.get(intervals.size()-1).getRight());
		// construction of new intervals = all intersections
		List<Double> pts = points.stream().distinct().collect(Collectors.toList());
		List<Interval> result = new ArrayList<Interval>(pts.size());
		
		result.add(lowerBound);
		for (int i = 0; i < points.size() - 1; i++) {
			
			result.add(new Interval(pts.get(i), pts.get(i + 1), true, true));
		}
		result.add(upperBound);
		
		return result;
	}

}
