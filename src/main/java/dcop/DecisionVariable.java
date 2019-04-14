package dcop;

import factorgraph.VariableNode;
import toolkit.Checker;

public class DecisionVariable {

	private int[] domain;
	private VariableNode variableNode;
	private String id;

	public DecisionVariable(int[] domain, String id) {
		if (Checker.assertive)
			Checker.check(domain.length > 0, "variable domain array is null");

		this.domain = domain;
		variableNode = new VariableNode(this);
		this.id = id;
	}

	public int[] getDomain() {
		return domain;
	}

	public VariableNode getVariableNode() {
		return variableNode;
	}

	@Override
	public String toString() {
		return id;
	}

}