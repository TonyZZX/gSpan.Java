package io.github.tonyzzx.gspan.model;

import java.util.ArrayList;

public class Vertex {
	public int label;
	public ArrayList<Edge> edge;

	public Vertex() {
		edge = new ArrayList<>();
	}

	public void push(int from, int to, int elabel) {
		Edge e = new Edge();
		e.from = from;
		e.to = to;
		e.elabel = elabel;
		edge.add(e);
	}
}
