/*
Taken from FRODO 2.16 (frodo2.algorithms.XCSPparser)

FRODO: a FRamework for Open/Distributed Optimisation
Copyright (C) 2008-2018  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

FRODO is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FRODO is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


How to contact the authors:
<https://frodo-ai.tech>
 */

package parser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import toolkit.Checker;

/**
 * An XCSP parser that provides convenient methods to extract information from
 * XCSP files
 *
 * @author Thomas Leaute
 * @author Luca CPZ
 * @see frodo2.solutionSpaces.JaCoP.JaCoPxcspParser for parsing intensional
 *      constraints
 */
public class XCSPparser {

	private Element root;

	public XCSPparser(String path) {
		if (Checker.assertive)
			Checker.check(path != null && path.length() > 0, "input path is not valid");

		try {
			Document document = new SAXBuilder().build(new File(path));
			root = document.getRootElement();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the set of agents in the problem.
	 *
	 * @return set of agents
	 */
	public Set<String> getAgents() {
		HashSet<String> agents = new HashSet<String>();

		for (Element var : (List<Element>) root.getChild("agents").getChildren())
			agents.add(var.getAttributeValue("name"));

		return agents;
	}

	/**
	 * Returns whether the input variable is defined as a random variable
	 *
	 * @param var the name of the variable
	 * @return \c true if the input variable is a random variable, \c false if not
	 *         or if the variable is unknown
	 */
	public boolean isRandom(String var) {

		for (Element varElmt : (List<Element>) root.getChild("variables").getChildren())
			if (var.equals(varElmt.getAttributeValue("name")))
				return new String("random").equals(varElmt.getAttributeValue("type"));

		// Variable not found
		return false;
	}

	/**
	 * Returns the name of the agent owning the input variable.
	 *
	 * @param var the name of the variable
	 * @return the owner of the input variable
	 */
	public String getOwner(String var) {

		for (Element varElmt : (List<Element>) root.getChild("variables").getChildren())
			if (varElmt.getAttributeValue("name").equals(var))
				return varElmt.getAttributeValue("agent");

		// The variable was not found
		assert false : "Unknown variable '" + var + "'";
		return null;
	}

	/**
	 * @return for each variable, the name of its owner agent
	 */
	public Map<String, String> getOwners() {

		Map<String, String> out = new HashMap<String, String>(this.getNbrVars());

		for (Element varElmt : (List<Element>) root.getChild("variables").getChildren()) {
			String owner = varElmt.getAttributeValue("agent");
			if (owner != null)
				out.put(varElmt.getAttributeValue("name"), owner);
		}

		return out;
	}

	/**
	 * Extracts the number of variables in the problem
	 *
	 * @return the number of variables in the problem
	 * @warning Ignores variables with no specified owner.
	 */
	public int getNbrVars() {
		return this.getVariables().size();
	}

	/**
	 * Computes the number of variables owned by the input agent
	 *
	 * @param owner name of the agent
	 * @return the number of variables owned by \a owner
	 */
	public int getNbrVars(String owner) {
		int nbrVars = 0;
		for (Element variable : root.getChild("variables").getChildren())
			if (owner.equals(variable.getAttributeValue("agent")))
				nbrVars++;
		return nbrVars;
	}

	/**
	 * @return all variables with a known owner
	 */
	public Set<String> getVariables() {
		Set<String> out = new HashSet<String>();

		for (Element varElmt : (List<Element>) root.getChild("variables").getChildren())
			if (!"random".equals(varElmt.getAttributeValue("type"))) // ignore random variables
				out.add(varElmt.getAttributeValue("name"));

		return out;
	}

	/**
	 * Returns the set of variables owned by a given agent.
	 *
	 * @param owner the name of the agent; if null, returns all variables with no
	 *              specified owner
	 * @return a set of variables owned by <i>owner</i>
	 */
	public Set<String> getVariables(String owner) {

		Set<String> out = new HashSet<String>();

		if (owner != null) {
			for (Element var : (List<Element>) root.getChild("variables").getChildren())
				if (owner.equals(var.getAttributeValue("agent")))
					out.add(var.getAttributeValue("name"));

		} else
			for (Element var : (List<Element>) root.getChild("variables").getChildren())
				if (var.getAttributeValue("agent") == null)
					out.add(var.getAttributeValue("name"));

		return out;
	}

	/**
	 * Extracts the collection of neighbours of a given variable
	 *
	 * @param var the name of the variable
	 * @return a collection of neighbour variables of \a var
	 * @warning Ignores variables with no specified owner.
	 */
	public HashSet<String> getNeighbourVars(String var) {
		return this.getNeighbourVars(var, false);
	}

	/**
	 * Extracts the collection of neighbours of a given variable
	 *
	 * @param var            the name of the variable
	 * @param withAnonymVars if \c false, ignores variables with no specified owner
	 * @return a collection of neighbour variables of \a var
	 */
	public HashSet<String> getNeighbourVars(String var, final boolean withAnonymVars) {
		HashSet<String> out = new HashSet<String>();
		List<String> pending = Arrays.asList(var); // variable(s) whose direct neighbours will be returned
		HashSet<String> done = new HashSet<String>();

		do {
			// Retrieve the next pending variable
			String var2 = pending.remove(0);
			if (!done.add(var2)) // we have already processed this variable
				continue;

			// Go through the list of constraint scopes
			for (Element constraint : root.getChild("constraints").getChildren()) {

				// Check if var2 is in the scope
				String[] scope = constraint.getAttributeValue("scope").trim().split("\\s+");
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >= 0) {

					// Go through the list of variables in the scope
					for (String neighbour : scope) {

						// Check if the neighbour is random
						if (!this.isRandom(neighbour)) // not random
							out.add(neighbour);

						else { // the neighbour is random

							// Add it to the list of neighbours if we are interested in random neighbours
							if (withAnonymVars)
								out.add(neighbour);
						}
					}
				}
			}
		} while (!pending.isEmpty());

		// Remove the variable itself from its list of neighbours
		out.remove(var);

		return out;
	}

	/** @see DCOPProblemInterface#getNbrneighbours(java.lang.String) */
	public int getNbrneighbours(String var) {
		return this.getNbrneighbours(var, false);
	}

	/**
	 * Extracts the number of neighbours of an input variable
	 *
	 * @param var            the variable
	 * @param withAnonymVars if \c false, ignores variables with no specified owner
	 * @return the number of neighbour variables of \a var
	 */
	public int getNbrneighbours(String var, final boolean withAnonymVars) {
		return this.getNeighbourVars(var, withAnonymVars).size();
	}

	/**
	 * Parses the problem description to construct, for each variable owned by the
	 * input agent, its list of neighbours
	 *
	 * @param agent the name of the agent
	 * @return for each of the agent's variables, its collection of neighbours
	 * @warning Ignores variables with no specified owner.
	 */
	public Map<String, HashSet<String>> getNeighbourhoods(String agent) {
		return this.getNeighbourhoods(agent, false, false);
	}

	/**
	 * Parses the problem description to construct, for each variable owned by the
	 * input agent, its list of neighbours with no specified owner
	 *
	 * @param agent the name of the agent
	 * @return for each of the agent's variables, its collection of neighbours with
	 *         no specified owner
	 */
	public Map<String, HashSet<String>> getAnonymneighbourhoods(String agent) {
		return this.getNeighbourhoods(agent, true, true);
	}

	/**
	 * Parses the problem description to construct, for each variable owned by the
	 * input agent, its list of neighbours
	 *
	 * @param agent          the name of the agent
	 * @param withAnonymVars if \c false, ignores variables with no specified owner
	 * @param onlyAnonymVars if \c true, only considers variables with no specified
	 *                       owner (in which case this supersedes \a withAnonymVars)
	 * @return for each of the agent's variables, its collection of neighbours
	 *
	 * @todo Improve the performance by avoiding to call getNeighbourVars on each
	 *       variable, which requires parsing the constraints multiple times.
	 */
	public Map<String, HashSet<String>> getNeighbourhoods(String agent, final boolean withAnonymVars,
			final boolean onlyAnonymVars) {

		// For each variable that this agent owns, a collection of neighbour variables
		Map<String, HashSet<String>> neighbourhoods = new HashMap<String, HashSet<String>>();

		// Go through the list of variables owned by the input agent (or through all
		// variables if the input agent is null)
		for (String var : (agent == null ? this.getVariables() : this.getVariables(agent))) {

			// Get the neighbours of this variable
			HashSet<String> neighbours = this.getNeighbourVars(var, onlyAnonymVars || withAnonymVars);
			neighbourhoods.put(var, neighbours);

			// Remove the non-anonymous variables if required
			if (onlyAnonymVars)
				for (Iterator<String> iter = neighbours.iterator(); iter.hasNext();)
					if (!this.isRandom(iter.next()))
						iter.remove();
		}

		return neighbourhoods;
	}

	/**
	 * Computes the number of neighbouring variables of all variables owned by a
	 * given agent
	 *
	 * @param agent name of the agent
	 * @return for each variable owned by \a agent, its number of neighbouring
	 *         variables
	 * @warning Ignores variables with no specified owner.
	 */
	public Map<String, Integer> getNeighbourhoodsizes(String agent) {

		Map<String, Integer> out = new HashMap<String, Integer>();

		// Go through the list of neighbours of each of the agent's variables
		for (Map.Entry<String, HashSet<String>> neighbourhood : getNeighbourhoods(agent).entrySet())
			out.put(neighbourhood.getKey(), neighbourhood.getValue().size());

		return out;
	}

	/**
	 * Returns the neighbouring agents of the input variable
	 *
	 * @param var the variable
	 * @return the variable's neighbouring agents
	 */
	private HashSet<String> getAgentneighbours(String var) {

		HashSet<String> out = new HashSet<String>();

		LinkedList<String> pending = new LinkedList<String>(); // variable(s) whose direct agent neighbours will be
																// returned
		pending.add(var);
		HashSet<String> done = new HashSet<String>();
		do {
			// Retrieve the next pending variable
			String var2 = pending.poll();
			if (!done.add(var2)) // we have already processed this variable
				continue;

			// Go through the list of constraint scopes
			for (Element constraint : (List<Element>) root.getChild("constraints").getChildren()) {

				// Check if var2 is in the scope
				String[] scope = constraint.getAttributeValue("scope").trim().split("\\s+");
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >= 0) {

					// If the constraint has a specific owner, add it to the set of agents
					String consOwner = constraint.getAttributeValue("agent");
					if ("PUBLIC".equals(consOwner))
						consOwner = null;
					if (consOwner != null)
						out.add(consOwner);

					// Go through the list of variables in the scope
					for (String neighbour : scope) {

						// Check if the neighbour is random
						if (!this.isRandom(neighbour)) { // not random
							String varOwner = this.getOwner(neighbour);
							if (varOwner != null)
								out.add(varOwner);
						}
					}
				}
			}
		} while (!pending.isEmpty());

		// Add the variable's scope if present
		HashSet<String> scope = this.getScope(var);
		if (scope != null)
			out.addAll(scope);

		// Remove the owner agent from the list of neighbours
		out.remove(this.getOwner(var));

		return out;
	}

	/**
	 * Returns the agent scope of the variable
	 *
	 * @param var the variable
	 * @return the agent scope of the variable
	 */
	private HashSet<String> getScope(String var) {

		for (Element varElmt : (List<Element>) this.root.getChild("variables").getChildren()) {
			if (varElmt.getAttributeValue("name").equals(var)) {
				String scope = varElmt.getAttributeValue("scope");
				if (scope == null)
					return null;
				return new HashSet<String>(Arrays.asList(scope.split("\\s+")));
			}
		}

		return null;
	}

	/** @see DCOPProblemInterface#getAgentneighbourhoods(java.lang.String) */
	public Map<String, Collection<String>> getAgentneighbourhoods(String agent) {

		Set<String> vars = this.getVariables();
		Map<String, Collection<String>> out = new HashMap<String, Collection<String>>(vars.size());
		for (String var : vars) {
			String owner = this.getOwner(var);
			if (agent == null || agent.equals(owner) || owner == null && !this.isRandom(var))
				out.put(var, this.getAgentneighbours(var));
		}

		return out;
	}

	/**
	 * This method only makes sense in subclasses of XCSPparser that handle backyard
	 * variables
	 *
	 * @param var variable
	 * @return an empty map
	 */
	public Map<String, Collection<String>> getBackyardneighbourhood(String var) {
		return new HashMap<String, Collection<String>>();
	}

	/**
	 * Extracts the size of the domain of the input variable
	 *
	 * @param var the variable
	 * @return the size of the domain of \a var
	 */
	public int getDomainSize(String var) {

		// Parse the name of the domain
		String domName = null;
		for (Element varElmt : (List<Element>) root.getChild("variables").getChildren()) {
			if (varElmt.getAttributeValue("name").equals(var)) {
				domName = varElmt.getAttributeValue("domain");
				break;
			}
		}
		if (domName == null)
			return -1;

		// Parse the domain size
		for (Element domElmt : (List<Element>) root.getChild("domains").getChildren())
			if (domElmt.getAttributeValue("name").equals(domName))
				return Integer.parseInt(domElmt.getAttributeValue("nbValues"));

		// The domain is not defined
		System.err.println("The domain " + domName + " for variable " + var + " is not defined");
		return -1;
	}

	/**
	 * Extracts the domain of a given variable
	 *
	 * @param var the variable
	 * @return an array of domain values
	 */
	public Number[] getDomain(String var) {
		// TODO
		return null;
	}

}