package dcop;

import toolkit.Checker;

public abstract class Function {

	protected DecisionVariable[] decisionVariables;
	protected String id;

	public Function(DecisionVariable[] decisionVariables, String id) {
		if (Checker.assertive) {
			Checker.check(decisionVariables.length > 0, "decision variables array is null");
			Checker.check(id != null && id.length() > 0, "id string is null or empty");
		}

		this.decisionVariables = decisionVariables;
		this.id = id;
	}

	public DecisionVariable[] getDecisionVariables() {
		return decisionVariables;
	}

	@Override
	public String toString() {
		return id;
	}

	public abstract float evaluate(Number[] d);

}