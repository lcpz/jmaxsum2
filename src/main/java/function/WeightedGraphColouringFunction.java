package function;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import dcop.DecisionVariable;
import dcop.Function;
import toolkit.Checker;

public class WeightedGraphColouringFunction extends Function {

	// index of the decisionVariable belonging to the agent that holds this function
	private int agentVariableIdx;

	// associated agent's preferences of each domain value of its decision variable
	private Map<Integer, Float> agentPreferences;

	/**
	 * Every such function is associated to exactly 1 agent, which is identified by
	 * the decision variable of index i in decisionVariables.
	 *
	 * @param decisionVariables array of decision variables that are arguments of
	 *                          this function
	 * @param id                the function string identifier
	 * @param i                 the index of the decision variable of the agent
	 *                          associated with this function
	 */
	public WeightedGraphColouringFunction(DecisionVariable[] decisionVariables, String id, int i) {
		super(decisionVariables, id);

		if (Checker.assertive) {
			Checker.check(decisionVariables.length > 0, "decision variables array is null");
			Checker.check(i >= 0, "agent's variable index is negative");
			Checker.check(i < decisionVariables.length, "agent's variable index is out of bounds");
		}

		agentVariableIdx = i;
		initPreferences();
	}

	public WeightedGraphColouringFunction(DecisionVariable[] decisionVariables, String id, int i,
			Map<Integer, Float> m) {
		this(decisionVariables, id, i);
		agentPreferences = m;
	}

	/**
	 * Initialise associated agent's preferences randomly.
	 */
	private void initPreferences() {
		int[] domain = decisionVariables[agentVariableIdx].getDomain();

		agentPreferences = new HashMap<Integer, Float>();

		for (int i = 0; i < domain.length; i++)
			agentPreferences.put(domain[i], ThreadLocalRandom.current().nextFloat() * 1e-6f);
	}

	/**
	 * Equation 18 in 'Decentralised Coordination of Low-Power Embedded Devices
	 * Using the Max-Sum Algorithm', Farinelli et al., AAMAS 2008.
	 */
	@Override
	public float evaluate(Number[] d) {
		// this agent's preference on its current variable value x_m
		int x_m = decisionVariables[agentVariableIdx].getVariableNode().getX();
		float u = agentPreferences.get(x_m);

		/*
		 * the sum of all x_m OP x_i, where x_i is any other variable in this function,
		 * and x_m OP x_i = 1 if x_m = x_i, 0 otherwise
		 */
		for (int i = 0; i < d.length; i++)
			if (i != agentVariableIdx && d[i].intValue() == x_m)
				u -= d[i].intValue();

		return u;
	}

}