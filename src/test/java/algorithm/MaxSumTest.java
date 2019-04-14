package algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dcop.Agent;
import dcop.DecisionVariable;
import dcop.Function;
import factorgraph.FactorGraph;
import factorgraph.FunctionNode;
import factorgraph.VariableNode;
import problem.GraphColouring;

@DisplayName("testing MaxSum algorithm correctness")
public class MaxSumTest {

	private static FactorGraph graph;

	@Test
	@DisplayName("creating the factor graph of a small graph colouring problem")
	void test1() {
		graph = GraphColouring.getExample();
		Agent[] agents = graph.getAgents();
		Function[] functions = graph.getFunctions();

		assertTrue(graph != null);
		assertEquals(agents.length, graph.getAgents().length);

		VariableNode[] vnodes = graph.getVariableNodes();

		// assert number of nodes
		assertEquals(agents.length, vnodes.length);
		assertEquals(agents.length, graph.getFunctionNodes().length);

		int i;

		// assert decision nodes and relative variables
		for (i = 0; i < vnodes.length; i++)
			assertTrue(vnodes[i].getDecisionVariable().equals(agents[i].getDecisionVariables()[0]));

		boolean fcheck;

		// assert function nodes
		for (FunctionNode fnode : graph.getFunctionNodes()) {
			fcheck = false;
			for (i = 0; i < functions.length; i++)
				if (fnode.getFunction().equals(functions[i])) {
					fcheck = true;
					break;
				}
			assertTrue(fcheck);
		}

		// assert number of edges
		i = 0;
		int numEdges = 0;
		for (Function f : functions)
			numEdges += f.getDecisionVariables().length;
		Map<FunctionNode, HashSet<VariableNode>> edges = graph.getEdges();
		for (HashSet<VariableNode> set : edges.values())
			i += set.size();
		assertEquals(numEdges, i);

		// assert neighbourhoods
		for (Agent a : agents)
			for (DecisionVariable v : a.getDecisionVariables()) {
				assertTrue(v.getVariableNode().getNeighbours() != null);
				for (FunctionNode fn : v.getVariableNode().getNeighbours())
					assertTrue(fn.getNeighbours() != null);
			}
	}

	@Test
	@DisplayName("executing Synchronous MaxSum on the created graph")
	void test2() {
		new MaxSum(graph).solveSynchronous();
	}

	@Test
	@DisplayName("executing Asynchronous MaxSum on the created graph")
	void test3() {
		new MaxSum(graph).solve();
	}
}