package toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Mathematics {

	/**
	 * Creates the Cartesian product of the sets in input.
	 *
	 * Source: https://stackoverflow.com/a/714256
	 *
	 * See also: Guava.Sets.cartesianProduct
	 *
	 * @param sets the sets from which create the Cartesian product
	 * @return the Cartesian product
	 */
	public static Set<Set<Object>> cartesianProduct(Set<?>... sets) {
	    if (sets.length < 2)
	        throw new IllegalArgumentException(
					"Can't have a product of fewer than two sets (got " +
	                sets.length + ")");

	    return _cartesianProduct(0, sets);
	}

	// internal operation of cartesianProduct
	private static Set<Set<Object>> _cartesianProduct(int index, Set<?>... sets) {
	    Set<Set<Object>> ret = new HashSet<Set<Object>>();
	    if (index == sets.length) {
	        ret.add(new HashSet<Object>());
	    } else {
	        for (Object obj : sets[index]) {
	            for (Set<Object> set : _cartesianProduct(index+1, sets)) {
	                set.add(obj);
	                ret.add(set);
	            }
	        }
	    }
	    return ret;
	}

    /**
     * Compute all combinations of the given list of lists.
     *
     * It assumes that all the lists have the same generic type.
     *
     * Source: https://codereview.stackexchange.com/a/67922
     *
     * @param lists list of lists to be combined
     * @return the list of combinations
     */
    public static <T> List<List<T>> computeCombinations(List<List<T>> lists) {
        List<List<T>> currentCombinations = Arrays.asList(Arrays.asList());
        for (List<T> list : lists)
            currentCombinations = appendElements(currentCombinations, list);
        return currentCombinations;
    }

    // internal operation of computeCombinations
    private static <T> List<List<T>> appendElements(List<List<T>> combinations, List<T> extraElements) {
        return combinations.stream().flatMap(oldCombination
                -> extraElements.stream().map(extra -> {
                    List<T> combinationWithExtra = new ArrayList<>(oldCombination);
                    combinationWithExtra.add(extra);
                    return combinationWithExtra;
                }))
				.collect(Collectors.toList());
	}

}