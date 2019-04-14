package function;

import java.util.HashSet;
import java.util.Set;

import dcop.DecisionVariable;
import dcop.Function;
import toolkit.Checker;

public class SimpleGraphColouringFunction extends Function {

	public SimpleGraphColouringFunction(DecisionVariable[] decisionVariables, String id) {
		super(decisionVariables, id);
		if (Checker.assertive)
			Checker.check(decisionVariables.length > 0, "decision variables array is null");
	}

	/**
	 * In a simple graph colouring problem, the utility function returns 1 if the
	 * decision variables have all different values (no conflicts), and negative
	 * infinity otherwise.
	 */
	@Override
	public float evaluate(Number[] d) {
		// BitSet is more efficient with positive domains
		Set<Number> foundNumbers = new HashSet<Number>();

		for (Number num : d) {
			if (foundNumbers.contains(num))
				return Float.NEGATIVE_INFINITY;
			foundNumbers.add(num);
		}

		return 1;
	}

}