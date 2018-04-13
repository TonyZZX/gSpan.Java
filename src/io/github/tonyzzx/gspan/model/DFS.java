package io.github.tonyzzx.gspan.model;

public class DFS {
	public int from = 0;
	public int to = 0;
	public int fromlabel = 0;
	public int elabel = 0;
	public int tolabel = 0;

	public boolean equals(DFS dfs) {
		if (this.from == dfs.from && this.to == dfs.to && this.fromlabel == dfs.fromlabel && this.elabel == dfs.elabel
				&& this.tolabel == dfs.tolabel) {
			return true;
		} else {
			return false;
		}
	}
}
