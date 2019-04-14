package problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import algorithm.MaxSum;
import dcop.Agent;
import dcop.DecisionVariable;
import dcop.Function;
import factorgraph.FactorGraph;
import function.SimpleGraphColouringFunction;
import function.WeightedGraphColouringFunction;

public class GraphColouring {

	/**
	 * Taken from Figure 2b and section 5 of 'Decentralised Coordination of Low-Power Embedded
	 * Devices Using the Max-Sum Algorithm', Farinelli et al., AAMAS 2008.
	 */
	@SuppressWarnings("unused")
	public static FactorGraph getExample() {
		int[] colourDomain = new int[] {1, 2}; // e.g. 1 - red, 2 - blue

		Agent[] agents = new Agent[] {
				new Agent("Brown", new DecisionVariable[] {new DecisionVariable(colourDomain, "x1")}),
				new Agent("Jones", new DecisionVariable[] {new DecisionVariable(colourDomain, "x2")}),
				new Agent("Smith", new DecisionVariable[] {new DecisionVariable(colourDomain, "x3")})
		};

		// Weighted functions
		@SuppressWarnings("serial")
		List<Map<Integer, Float>> preferences = new ArrayList<Map<Integer, Float>>(){{
			add(new HashMap<Integer, Float>(){{
				put(1,  0.1f);
				put(2, -0.1f);
			}});

			add(new HashMap<Integer, Float>(){{
				put(1, -0.1f);
				put(2,  0.1f);
			}});
		}};

		Function[] weightedFunctions = new Function[] {
				new WeightedGraphColouringFunction(new DecisionVariable[] {
						agents[0].getDecisionVariables()[0],
						agents[1].getDecisionVariables()[0]
				}, "F1", 0, preferences.get(0)),
				new WeightedGraphColouringFunction(new DecisionVariable[] {
						agents[0].getDecisionVariables()[0],
						agents[1].getDecisionVariables()[0],
						agents[2].getDecisionVariables()[0]
				}, "F2", 1, preferences.get(1)),
				new WeightedGraphColouringFunction(new DecisionVariable[] {
						agents[1].getDecisionVariables()[0],
						agents[2].getDecisionVariables()[0]
				}, "F3", 1, preferences.get(1)) // agent Smith's index is 1 here
		};

		// Unweighted functions
		Function[] functions = new Function[] {
				new SimpleGraphColouringFunction(new DecisionVariable[] {
						agents[0].getDecisionVariables()[0],
						agents[1].getDecisionVariables()[0]
				}, "F1"),
				new SimpleGraphColouringFunction(new DecisionVariable[] {
						agents[0].getDecisionVariables()[0],
						agents[1].getDecisionVariables()[0],
						agents[2].getDecisionVariables()[0]
				}, "F2"),
				new SimpleGraphColouringFunction(new DecisionVariable[] {
						agents[1].getDecisionVariables()[0],
						agents[2].getDecisionVariables()[0]
				}, "F3") // agent Smith's index is 1 here
		};

		return new FactorGraph(agents, functions);
	}

	public static void main (String[] args) {
		MaxSum instance = new MaxSum(getExample());
		long executionTime = System.currentTimeMillis();
		//instance.solve();
		instance.solveSynchronous();
		executionTime = System.currentTimeMillis() - executionTime;
		System.out.println(String.format("%sExecution time: %d ms\n", instance.getResults(), executionTime));
		System.out.println("Solution:\n" + instance.getSolution());
	}

}