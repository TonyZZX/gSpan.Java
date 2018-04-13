package io.github.tonyzzx.gspan.model;

import java.util.ArrayList;

public class DFSCode extends ArrayList<DFS> {
    private static final long serialVersionUID = 1L;
    private ArrayList<Integer> rmPath;

    public DFSCode() {
        rmPath = new ArrayList<>();
    }

    public void push(int from, int to, int fromLabel, int eLabel, int toLabel) {
        DFS d = new DFS();
        d.from = from;
        d.to = to;
        d.fromLabel = fromLabel;
        d.eLabel = eLabel;
        d.toLabel = toLabel;
        this.add(d);
    }

    public void pop() {
        this.remove(this.size() - 1);
    }

    public void toGraph(Graph g) {
        g.clear();

        for (DFS it : this) {
            g.resize(Math.max(it.from, it.to) + 1);

            if (it.fromLabel != -1)
                g.get(it.from).label = it.fromLabel;
            if (it.toLabel != -1)
                g.get(it.to).label = it.toLabel;

            g.get(it.from).push(it.from, it.to, it.eLabel);
            if (!g.directed)
                g.get(it.to).push(it.to, it.from, it.eLabel);
        }

        g.buildEdge();
    }

    public ArrayList<Integer> buildRMPath() {
        rmPath.clear();

        int old_from = -1;

        for (int i = size() - 1; i >= 0; --i) {
            if (this.get(i).from < this.get(i).to && // forward
                    (rmPath.isEmpty() || old_from == this.get(i).to)) {
                rmPath.add(i);
                old_from = this.get(i).from;
            }
        }

        return rmPath;
    }

    /**
     * Return number of nodes in the graph.
     * @return number of nodes in the graph
     */
    public int countNode() {
        int nodeCount = 0;

        for (DFS it : this)
            nodeCount = Math.max(nodeCount, Math.max(it.from, it.to) + 1);

        return nodeCount;
    }
}
