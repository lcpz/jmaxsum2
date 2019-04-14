package factorgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import dcop.DecisionVariable;
import toolkit.Checker;
import toolkit.Utils;

public class VariableNode {

	private Integer x; // current solution x_i
	private Float z; // current marginal function z_i (x_i)

	private DecisionVariable decisionVariable;
	private Set<FunctionNode> neighbours;

	// last produced messages from this VariableNode to neighbour FunctionNodes
	private HashMap<FunctionNode, HashMap<Integer, Float>> Q;

	public VariableNode(DecisionVariable decisionVariable) {
		if (Checker.assertive)
			Checker.check(decisionVariable != null, "input DecisionVariable is null");

		this.decisionVariable = decisionVariable;
		neighbours = new HashSet<FunctionNode>();

		// x is randomly initialised
		int[] domain = decisionVariable.getDomain();
		this.x = domain[new Random().nextInt(domain.length)];

		z = Float.NEGATIVE_INFINITY;
		Q = new HashMap<FunctionNode, HashMap<Integer, Float>>();
	}

	@Override
	public String toString() {
		return decisionVariable.toString();
	}

	public Integer getX() {
		return x;
	}

	public Float getZ() {
		return z;
	}

	public void setX(Integer newX) {
		this.x = newX;
	}

	public void setZ(Float newZ) {
		this.z = newZ;
	}

	public DecisionVariable getDecisionVariable() {
		return decisionVariable;
	}

	public Set<FunctionNode> getNeighbours() {
		return this.neighbours;
	}

	protected boolean addNeighbour(FunctionNode node) {
		// initialise to 0 the R messages from node to this VariableNode
		Q.put(node, Utils.getZeroMessages(decisionVariable.getDomain()));

		return neighbours.add(node);
	}

	public HashMap<Integer, Float> getQ(FunctionNode fn) {
		return Q.get(fn);
	}

	protected boolean setQ(FunctionNode fn, int d, float q) {
		HashMap<Integer, Float> Qnode = Q.get(fn);
		Qnode.put(d, q);
		return Q.put(fn, Qnode) != null;
	}

	public double sendQMessageTo(FunctionNode j) {
		int[] domain = decisionVariable.getDomain();
		float q = 0, alpha = 0;

		for (int d : domain) {
			q = 0;
			for (FunctionNode fn : getNeighbours())
				if (!fn.equals(j))
					q = Utils.checkedSum(q, fn.getR(this).get(d));
			alpha += q;
			setQ(j, d, q);
		}

		alpha /= domain.length;

		for (int d : domain)
			setQ(j, d, Q.get(j).get(d) - alpha);

		return Q.get(j).get(x);
	}
}