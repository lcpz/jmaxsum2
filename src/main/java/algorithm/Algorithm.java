package algorithm;

import factorgraph.FactorGraph;
import factorgraph.VariableNode;
import toolkit.Checker;

public abstract class Algorithm {

	protected FactorGraph graph;

	public Algorithm(FactorGraph graph) {
		if (Checker.assertive)
			Checker.check(graph != null &&
			graph.getEdges().size() > 0 &&
			graph.getFunctionNodes().length > 0 &&
			graph.getVariableNodes().length > 0,
			"input factor graph is null");

		this.graph = graph;
	}

	public FactorGraph getGraph() {
		return graph;
	}

	public String getSolution() {
		String s = new String();
		VariableNode[] vars = graph.getVariableNodes();
		for (int i = 0; i < vars.length; i++)
			s = s.concat(String.format("\n%s = %d", vars[i], vars[i].getX()));
		return s;
	}

	public abstract void solve();

	public abstract String getResults();

}