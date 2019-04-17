package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.Interval;

import java.util.*;
import java.util.stream.Collectors;

public class IntersectionFinder {

	public IntersectionFinder() {
		// TODO Auto-generated constructor stub
	}

	
	public List<Interval> calculateAllIntersectionsOf(List<Interval> intervals) {

		//create mandatory limits
		Interval leftmost = intervals.stream().min(Comparator.comparing(Interval::getLeft)).map(Interval.class::cast).orElseThrow(() -> new RuntimeException("Empty interval list"));
		Interval rightmost = intervals.stream().min(Comparator.comparing(Interval::getRight)).map(Interval.class::cast).orElseThrow(() -> new RuntimeException("Empty interval list"));
		
		
		
		double left = intervals.stream().min(Comparator.comparing(Interval::getLeft)).map(Interval::getLeft).orElseThrow(()->new RuntimeException("Empty interval list!"));
		double right = intervals.stream().max(Comparator.comparing(Interval::getRight)).map(Interval::getRight).orElseThrow(()->new RuntimeException("Empty interva list!"));
		
		Interval lowerBound = Interval.create_le(left);
		Interval upperBound = Interval.create_geq(right);
		
		if (Double.compare(leftmost.getLeft(), Interval.MINUS_INF) == 0) {
			lowerBound = leftmost;
		}
		
		if (Double.compare(rightmost.getRight(), Interval.INF) == 0) {
			upperBound = rightmost;
		}
		
		
		// finds all intersection point : each with each
		Set<Double> points = new TreeSet<Double>();
		points.add(Interval.MINUS_INF);
		points.add(Interval.INF);
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
		
	//	result.add(lowerBound);
		if (pts.get(0).equals(Interval.MINUS_INF)) {
			result.add(Interval.create_le(pts.get(1)));
		} else {
			result.add(Interval.create_le(pts.get(0)));
		}
		
		for (int i = 1; i < points.size() - 2; i++) {
			
			result.add(new Interval(pts.get(i), pts.get(i + 1), true, true));
		}
		
		if (pts.get(pts.size() - 1).equals(Interval.INF)) {
			result.add(Interval.create_geq(pts.get(pts.size()-2)));
		} else {
			result.add(Interval.create_geq(pts.get(pts.size()-1)));
		}
	//	result.add(upperBound);
		
		return result;
	}

}
