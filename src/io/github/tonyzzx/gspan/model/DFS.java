package io.github.tonyzzx.gspan.model;

public class DFS {
    public int from = 0;
    public int to = 0;
    public int fromLabel = 0;
    int eLabel = 0;
    int toLabel = 0;

    public boolean notEqual(DFS dfs) {
        return this.from != dfs.from || this.to != dfs.to || this.fromLabel != dfs.fromLabel || this.eLabel != dfs.eLabel
                || this.toLabel != dfs.toLabel;
    }
}
