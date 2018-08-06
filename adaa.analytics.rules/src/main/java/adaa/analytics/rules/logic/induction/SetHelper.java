package adaa.analytics.rules.logic.induction;

import java.util.Set;

public class SetHelper {
	/**
	 * Calculates size of intersection of two sets.
	 * @param A First set.
	 * @param B Second set.
	 * @return Intersection size.
	 */
	public static <T> int intersectionSize(Set<T> A, Set<T> B) {
		int size = 0;
		for (T a: A) {
			if (B.contains(a)) {
				++size;
			}
		}
		return size;
	}
	
	/**
	 * Calculates size of difference of two sets.
	 * @param A First set.
	 * @param B Second set.
	 * @return Difference size.
	 */
	public static <T> int differenceSize(Set<T> A, Set<T> B) {
		int size = 0;
		for (T a: A) {
			if (!B.contains(a)) {
				++size;
			}
		}
		return size;
	}
}
