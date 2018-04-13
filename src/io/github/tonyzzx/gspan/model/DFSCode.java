package io.github.tonyzzx.gspan.model;

import java.util.ArrayList;

public class DFSCode extends ArrayList<DFS> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Integer> rmpath;

	public DFSCode() {
		rmpath = new ArrayList<>();
	}

	public void push(int from, int to, int fromlabel, int elabel, int tolabel) {
		DFS d = new DFS();
		d.from = from;
		d.to = to;
		d.fromlabel = fromlabel;
		d.elabel = elabel;
		d.tolabel = tolabel;
		this.add(d);
	}

	public void pop() {
		this.remove(this.size() - 1);
	}

	public boolean toGraph(Graph g) {
		g.clear();

		for (DFS it : this) {
			g.resize(Math.max(it.from, it.to) + 1);

			if (it.fromlabel != -1)
				g.get(it.from).label = it.fromlabel;
			if (it.tolabel != -1)
				g.get(it.to).label = it.tolabel;

			g.get(it.from).push(it.from, it.to, it.elabel);
			if (g.directed == false)
				g.get(it.to).push(it.to, it.from, it.elabel);
		}

		g.buildEdge();

		return (true);
	}

	public ArrayList<Integer> buildRMPath() {
		rmpath.clear();

		int old_from = -1;

		for (int i = size() - 1; i >= 0; --i) {
			if (this.get(i).from < this.get(i).to && // forward
					(rmpath.isEmpty() || old_from == this.get(i).to)) {
				rmpath.add(i);
				old_from = this.get(i).from;
			}
		}

		return rmpath;
	}

	/*
	 * Return number of nodes in the graph.
	 */
	public int nodeCount() {
		int nodecount = 0;

		for (DFS it : this)
			nodecount = Math.max(nodecount, Math.max(it.from, it.to) + 1);

		return (nodecount);
	}
}
