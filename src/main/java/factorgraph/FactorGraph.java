package factorgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import dcop.Agent;
import dcop.DecisionVariable;
import dcop.Function;
import toolkit.Checker;

public class FactorGraph {

	private Agent[] agents;
	private Function[] functions;
	private VariableNode[] variableNodes;
	private FunctionNode[] functionNodes;
	private Map<FunctionNode, HashSet<VariableNode>> edges;

	public FactorGraph(Agent[] agents, Function[] functions) {
		if (Checker.assertive) {
			Checker.check(agents != null && agents.length > 0, "agents array is null");
			Checker.check(agents != null && functions.length > 0, "functions array is null");
		}

		this.agents = agents;
		this.functions = functions;
		edges = new HashMap<FunctionNode, HashSet<VariableNode>>();

		// add variable nodes
		LinkedHashSet<VariableNode> agentsVariableNodes = new LinkedHashSet<VariableNode>();
		for (Agent a : agents)
			for (DecisionVariable x : a.getDecisionVariables())
				agentsVariableNodes.add(x.getVariableNode());
		variableNodes = agentsVariableNodes.toArray(new VariableNode[agentsVariableNodes.size()]);

		// add function nodes
		FunctionNode fn;
		for (Function f : functions) {
			fn = new FunctionNode(f);
			// add edges
			for (DecisionVariable x : f.getDecisionVariables())
				insertEdge(fn, x.getVariableNode());
		}
		functionNodes = edges.keySet().toArray(new FunctionNode[0]);
	}

	private void insertEdge(FunctionNode a, VariableNode b) {
		HashSet<VariableNode> h = edges.get(a);
		if (h == null)
			h = new HashSet<VariableNode>();
		h.add(b);
		edges.put(a, h); // the Set class manages duplicates
	}

	public Agent[] getAgents() {
		return agents;
	}

	public Function[] getFunctions() {
		return functions;
	}

	public VariableNode[] getVariableNodes() {
		return variableNodes;
	}

	public FunctionNode[] getFunctionNodes() {
		return functionNodes;
	}

	public Map<FunctionNode, HashSet<VariableNode>> getEdges() {
		return edges;
	}

	public int getNumberOfNodes() {
		return variableNodes.length + functionNodes.length;
	}

}