package io.github.tonyzzx.gspan.model;

import java.util.ArrayList;

public class Vertex {
    public int label;
    public ArrayList<Edge> edge;

    public Vertex() {
        edge = new ArrayList<>();
    }

    public void push(int from, int to, int eLabel) {
        Edge e = new Edge();
        e.from = from;
        e.to = to;
        e.eLabel = eLabel;
        edge.add(e);
    }
}
