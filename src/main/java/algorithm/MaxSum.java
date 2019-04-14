package algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import dcop.DecisionVariable;
import factorgraph.FactorGraph;
import factorgraph.FunctionNode;
import factorgraph.VariableNode;

/**
 * Standard Max-Sum algorithm.
 *
 * @author Luca CPZ
 */
public class MaxSum extends Algorithm {

	public static final double iterationsNumberMultiplier = 100.0;

	private long iterationsNumber;
	private int convergenceNumber;

	private volatile int terminatedNodes;
	private String lastResults;

	// results of a MaxSum iteration
	protected class Iteration {

		public boolean converged;

		public String R = new String();
		public String Q = new String();
		public String ZX = new String();

		public String customStr = new String();

		public void setR(FunctionNode fn, VariableNode vn, double r) {
			R = String.format("%sR(%s, %s) = %.3f\n", R, fn, vn, r);
		}

		public void setQ(VariableNode vn, FunctionNode fn, double q) {
			Q = String.format("%sQ(%s, %s) = %.3f\n", Q, vn, fn, q);
		}

		public void setZX(DecisionVariable v, Float z, Integer x) {
			ZX = String.format("%sZ(%s) = %.3f, %s = %d\n", ZX, v, z, v, x);
		}

	}

	// results of the last execution
	protected List<Iteration> results;

	/**
	 * @param graph the factor graph representation of the DCOP instance
	 */
	public MaxSum(FactorGraph graph) {
		super(graph);
		int numAgents = graph.getAgents().length;
		iterationsNumber = Math.round(numAgents * iterationsNumberMultiplier);
		convergenceNumber = ThreadLocalRandom.current().nextInt(numAgents, numAgents * 2 + 1);
	}

	public void setConvergenceNumber(int i) {
		if (i > 0)
			convergenceNumber = i;
	}

	public void setIterationsNumber(int i) {
		if (i > 0)
			iterationsNumber = i;
	}

	public synchronized boolean addIteration(Iteration i) {
		return results.add(i);
	}

	public synchronized int terminated() {
		return ++terminatedNodes;
	}

	@Override
	public void solve() {
		terminatedNodes = 0;
		results = new ArrayList<Iteration>();

		List<Callable<Void>> taskList = new ArrayList<Callable<Void>>();
		// thread pool size as recommended in 'Java Concurrency in Practice' book
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

		for (FunctionNode fn : graph.getFunctionNodes())
			taskList.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					solveFunctionNode(fn);
					terminated();
					return null;
				}
			});

		for (VariableNode vn : graph.getVariableNodes())
			taskList.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					solveVariableNode(vn);
					terminated();
					return null;
				}
			});

		try {
			executor.invokeAll(taskList);
			executor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
			executor.shutdownNow();
		}
	}

	private void solveFunctionNode(FunctionNode fn) {
		long myIterationsNumber = iterationsNumber;

		/* variables used to check convergence */
		Map<VariableNode, Integer> convergenceMap = new HashMap<VariableNode, Integer>();
		boolean converged = false; // early stopping condition
		Integer xRepetitions;

		VariableNode[] neighbours = fn.getNeighbours();
		int[] lastX = new int[neighbours.length];
		int i;
		Iteration iteration;

		while (myIterationsNumber-- > 0 && !converged) {
			iteration = new Iteration();

			for (i = 0; i < neighbours.length; i++) {
				/* get previous solution values */
				lastX[i] = neighbours[i].getX();

				/* propagate messages */
				iteration.setR(fn, neighbours[i], fn.sendRMessageTo(neighbours[i]));

				/* store convergence condition */
				if (neighbours[i].getX().equals(lastX[i])) { // solution has not changed
					// increase by 1 the number of iterations in which $x_i$ has not changed
					xRepetitions = convergenceMap.get(neighbours[i]);
					if (xRepetitions != null)
						convergenceMap.put(neighbours[i], xRepetitions + 1);
					else
						convergenceMap.put(neighbours[i], 1);
				} else if (neighbours[i].getX() != null) // solution has changed
					// reset convergenceMap and set new {z,x} values
					convergenceMap.put(neighbours[i], 0);
			}

			/* check convergence each convergenceNumber iterations */
			if (iterationsNumber % convergenceNumber == 0)
				converged = isConverged(convergenceMap);

			/* store results of this iteration */
			iteration.converged = converged;
			iteration.customStr = " - " + fn.toString();
			addIteration(iteration); // synchronised
		}

	}

	private void solveVariableNode(VariableNode vn) {
		long myIterationsNumber = iterationsNumber;
		Object[] zx;

		/* variables used to check convergence */
		Map<VariableNode, Integer> convergenceMap = new HashMap<VariableNode, Integer>();
		boolean converged = false; // early stopping condition
		Integer xRepetitions;
		Iteration iteration;

		while (myIterationsNumber-- > 0 && !converged) {
			iteration = new Iteration();

			for (FunctionNode fn : vn.getNeighbours()) {
				/* variable-to-function messages */
				iteration.setQ(vn, fn, vn.sendQMessageTo(fn));

				/* compute new z and x */
				zx = computeZX(vn); // zx[0] for z, zx[1] for x

				/* store convergence condition */
				if (vn.getX().equals(zx[1])) { // solution has not changed
					// increase by 1 the number of iterations in which $x_{vn}$ has not changed
					xRepetitions = convergenceMap.get(vn);
					if (xRepetitions != null)
						convergenceMap.put(vn, xRepetitions + 1);
					else
						convergenceMap.put(vn, 1);
				} else if (zx[1] != null) { // solution has changed
					// reset convergenceMap and set new {z,x} values
					convergenceMap.put(vn, 0);
					vn.setZ((Float) zx[0]);
					vn.setX((Integer) zx[1]);

					/* create results of this iteration */
					iteration.setZX(vn.getDecisionVariable(), (Float) zx[0], (Integer) zx[1]);
				}

			}

			/* check convergence each convergenceNumber iterations */
			if (myIterationsNumber % convergenceNumber == 0)
				converged = isConverged(convergenceMap);

			/* store results of this iteration */
			iteration.converged = converged;
			iteration.customStr = " - " + vn.toString();
			addIteration(iteration); // synchronised
		}
	}

	/**
	 * Execute Max-Sum synchronously.
	 *
	 * More precisely, the agents are processed sequentially in insertion order.
	 */
	public void solveSynchronous() {
		/* variables used to check convergence */
		Map<VariableNode, Integer> convergenceMap = new HashMap<VariableNode, Integer>();
		boolean converged = false; // early stopping condition
		Integer xRepetitions;

		/* variables used to store results */
		results = new ArrayList<Iteration>();
		Iteration iteration;
		Object[] zx;

		while (iterationsNumber-- > 0 && !converged) {
			iteration = new Iteration();

			/* function-to-variable messages */
			for (FunctionNode fn : graph.getFunctionNodes())
				for (VariableNode vn : fn.getNeighbours())
					iteration.setR(fn, vn, fn.sendRMessageTo(vn));

			for (VariableNode vn : graph.getVariableNodes()) {
				/* variable-to-function messages */
				for (FunctionNode fn : vn.getNeighbours())
					iteration.setQ(vn, fn, vn.sendQMessageTo(fn));

				/* compute new z and x */
				zx = computeZX(vn); // zx[0] for z, zx[1] for x

				/* store convergence condition */
				if (vn.getX().equals(zx[1])) { // solution has not changed
					// increase by 1 the number of iterations in which $x_{vn}$ has not changed
					xRepetitions = convergenceMap.get(vn);
					if (xRepetitions != null)
						convergenceMap.put(vn, xRepetitions + 1);
					else
						convergenceMap.put(vn, 1);
				} else if (zx[1] != null) { // solution has changed
					// reset convergenceMap and set new {z,x} values
					convergenceMap.put(vn, 0);
					vn.setZ((Float) zx[0]);
					vn.setX((Integer) zx[1]);
				}

				/* create results of this iteration */
				iteration.setZX(vn.getDecisionVariable(), (Float) zx[0], (Integer) zx[1]);
			}

			/* check convergence each convergenceNumber iterations */
			if (iterationsNumber % convergenceNumber == 0)
				converged = isConverged(convergenceMap);

			/* store results of this iteration */
			iteration.converged = converged;
			results.add(iteration);
		}
	}

	/**
	 * Compute Z and X, given the input variable node
	 *
	 * @param vn a variable node
	 * @return new marginal distribution (z) and solution (x) of vn
	 */
	private Object[] computeZX(VariableNode vn) {
		Float newZ = Float.NEGATIVE_INFINITY, sum;
		Integer newX = null;

		// exhaustive search of $\argmax_{x_{vn}} z_{vn} (x_{vn})$
		for (Integer d : vn.getDecisionVariable().getDomain()) {
			sum = 0f;

			for (FunctionNode fn : vn.getNeighbours())
				sum += fn.getR(vn).get(d);

			if (sum > newZ) {
				newZ = sum;
				newX = d;
			}
		}

		return new Object[] { newZ, newX };
	}

	private boolean isConverged(Map<VariableNode, Integer> convergenceMap) {
		for (Integer i : convergenceMap.values())
			if (i < convergenceNumber)
				return false;
		return true;
	}

	@Override
	public String getResults() {
		lastResults = new String();

		// TODO print to s the graph dot structure and {iterations,convergence}Number

		for (Iteration it : results) {
			lastResults = String.format("\n%s:: Iteration %d%s\n", lastResults, results.indexOf(it) + 1, it.customStr);
			if (it.R.length() > 0)
				lastResults = lastResults.concat(String.format("\n%s", it.R));
			if (it.Q.length() > 0)
				lastResults = lastResults.concat(String.format("\n%s", it.Q));
			if (it.ZX.length() > 0)
				lastResults = lastResults.concat(String.format("\n%s", it.ZX));
			if (it.converged)
				lastResults = lastResults.concat(
						String.format("\nSolution unchanged in last %s iterations, stopping\n\n", convergenceNumber));
			else
				lastResults = lastResults.concat("\n");
		}

		return lastResults;
	}

	public boolean printToFile(File f) {
		// TODO put lastResults to f
		return true;
	}

	public boolean plotFactorGraph() {
		// TODO
		// see how frodo2 uses graphviz
		// also, save it as png, pdf or tikz image
		return true;
	}

}