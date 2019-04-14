package factorgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dcop.DecisionVariable;
import dcop.Function;
import toolkit.Checker;
import toolkit.Mathematics;
import toolkit.Utils;

public class FunctionNode {

	private Function function;
	private VariableNode[] neighbours;

	// list of all possible values of the arguments of this.function
	private List<List<Integer>> D;

	// last produced messages from this FunctionNode to neighbour VariableNodes
	private HashMap<VariableNode, HashMap<Integer, Float>> R;

	public FunctionNode(Function function) {
		if (Checker.assertive)
			Checker.check(function != null, "input Function is null");

		this.function = function;
		DecisionVariable[] decisionVariables = function.getDecisionVariables();
		neighbours = new VariableNode[decisionVariables.length];
		R = new HashMap<VariableNode, HashMap<Integer, Float>>();
		D = new ArrayList<List<Integer>>();

		/*
		 * for every DecisionVariable x that is argument of this.function, x's
		 * VariableNode is a neighbour of this FunctionNode
		 */
		for (int i = 0; i < decisionVariables.length; i++) {
			neighbours[i] = decisionVariables[i].getVariableNode();
			neighbours[i].addNeighbour(this);

			// initialise to 0 the R messages from node to this VariableNode
			R.put(neighbours[i], Utils.getZeroMessages(decisionVariables[i].getDomain()));

			// D.add(Arrays.stream(decisionVariables[i].getDomain()).boxed().collect(Collectors.toList()));
			D.add(Utils.arr2List(decisionVariables[i].getDomain()));
		}

		// compute the space of joint assignments to the variables of this.function
		D = Mathematics.computeCombinations(D);
	}

	@Override
	public String toString() {
		return function.toString();
	}

	public Function getFunction() {
		return function;
	}

	public VariableNode[] getNeighbours() {
		return this.neighbours;
	}

	public HashMap<Integer, Float> getR(VariableNode vn) {
		return R.get(vn);
	}

	protected boolean setR(VariableNode vn, int d, float r) {
		HashMap<Integer, Float> Rnode = R.get(vn);

		Rnode.put(d, r);
		return R.put(vn, Rnode) != null;
	}

	/**
	 * Returns the index of vn.getDecisionVariable() in this.function
	 *
	 * @param vn a VariableNode
	 * @return the index of vn's variable in this.function, -1 otherwise
	 */
	public int getVarIndex(VariableNode vn) {
		for (int i = 0; i < neighbours.length; i++)
			if (vn.equals(neighbours[i]))
				return i;
		return -1;
	}

	/**
	 * Returns the value of this.function with the given arguments.
	 *
	 * @param d the arguments, that is, the values assigned to each DecisionVariable
	 *          in this.function
	 * @return evaluation of d with this.function
	 */
	public float evaluate(Number[] d) {
		return function.evaluate(d);
	}

	public double sendRMessageTo(VariableNode i) {
		setR(i, i.getX(), Float.NEGATIVE_INFINITY);

		int idx = getVarIndex(i);
		Float curr, sigma;

		for (List<Integer> l : D) {
			sigma = evaluate(l.toArray(new Integer[l.size()]));

			for (Integer d_k : l)
				if (l.indexOf(d_k) != idx)
					sigma = Utils.checkedSum(sigma, neighbours[l.indexOf(d_k)].getQ(this).get(d_k));

			curr = R.get(i).get(l.get(idx));
			curr = curr != null ? Math.max(sigma, curr) : sigma;

			setR(i, l.get(idx), curr);
		}

		return R.get(i).get(i.getX());
	}

}