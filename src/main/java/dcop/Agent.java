package dcop;

import toolkit.Checker;

public class Agent {

	protected String id;
	protected DecisionVariable[] decisionVariables;

	public Agent(String id, DecisionVariable[] decisionVariables) {
		if (Checker.assertive)
			Checker.check(decisionVariables.length > 0, "decision variables array is null");

		this.id = id;
		this.decisionVariables = decisionVariables;
	}

	public String getId() {
		return id;
	}

	public DecisionVariable[] getDecisionVariables() {
		return decisionVariables;
	}

}