package io.github.tonyzzx.gspan;

import java.util.ArrayList;

import io.github.tonyzzx.gspan.model.Edge;
import io.github.tonyzzx.gspan.model.Graph;
import io.github.tonyzzx.gspan.model.History;
import io.github.tonyzzx.gspan.model.Vertex;

public class Misc {
	/*
	 * graph $B$N(B vertex $B$+$i$O$($k(B edge $B$rC5$9(B $B$?$@$7(B,
	 * fromlabel <= tolabel $B$N@-<A$rK~$?$9(B.
	 */
	public static boolean get_forward_root(Graph g, Vertex v, ArrayList<Edge> result) {
		result.clear();
		for (Edge it : v.edge) {
			assert (it.to >= 0 && it.to < g.size());
			if (v.label <= g.get(it.to).label)
				result.add(it);
		}

		return (!result.isEmpty());
	}

	/*
	 * get_backward (graph, e1, e2, history); e1 (from1, elabel1, to1) e2
	 * (from2, elabel2, to2) to2 . from1 $B$K7R$,$k$+$I$&$+$7$i$Y$k(B.
	 * 
	 * (elabel1 < elabel2 || (elabel == elabel2 && tolabel1 < tolabel2)
	 * $B$N>r7o$r$_$?$9(B. (elabel1, to1) $B$N$[$&$,@h$KC5:w$5$l$k$Y$-(B
	 */
	public static Edge get_backward(Graph graph, Edge e1, Edge e2, History history) {
		if (e1 == e2)
			return null;

		assert (e1.from >= 0 && e1.from < graph.size());
		assert (e1.to >= 0 && e1.to < graph.size());
		assert (e2.to >= 0 && e2.to < graph.size());

		for (Edge it : graph.get(e2.to).edge) {
			if (history.hasEdge(it.id))
				continue;

			if ((it.to == e1.from) && ((e1.elabel < it.elabel)
					|| (e1.elabel == it.elabel) && (graph.get(e1.to).label <= graph.get(e2.to).label))) {
				return it;
			}
		}

		return null;
	}

	/*
	 * get_forward_pure () e (from, elabel, to) to $B$+$i7R$,$k(B edge
	 * $B$rJV$9(B $B$?$@$7(B, minlabel $B$h$jBg$-$$$b$N$K$7$+$$$+$J$$(B
	 * (DFS$B$N@)Ls(B) $B$^$?(B, $B$$$^$^$G8+$?(B vertex
	 * $B$K$O@B$+$J$$(B (backward $B$N$d$/$a(B)
	 */
	public static boolean get_forward_pure(Graph graph, Edge e, int minlabel, History history, ArrayList<Edge> result) {
		result.clear();

		assert (e.to >= 0 && e.to < graph.size());

		/*
		 * Walk all edges leaving from vertex e.to.
		 */
		for (Edge it : graph.get(e.to).edge) {
			/*
			 * -e. [e.to] -it. [it.to]
			 */
			assert (it.to >= 0 && it.to < graph.size());
			if (minlabel > graph.get(it.to).label || history.hasVertex(it.to))
				continue;

			result.add(it);
		}

		return (!result.isEmpty());
	}

	/*
	 * get_forward_pure () e1 (from1, elabel1, to1) from $B$+$i7R$,$k(B edge
	 * e2(from2, elabel2, to2) $B$rJV$9(B.
	 * 
	 * minlabel <= elabel2, (elabel1 < elabel2 || (elabel == elabel2 && tolabel1
	 * < tolabel2) $B$N>r7o$r$_$?$9(B. (elabel1, to1)
	 * $B$N$[$&$,@h$KC5:w$5$l$k$Y$-(B $B$^$?(B, $B$$$^$^$G8+$?(B vertex
	 * $B$K$O@B$+$J$$(B (backward $B$N$d$/$a(B)
	 */
	public static boolean get_forward_rmpath(Graph graph, Edge e, int minlabel, History history,
			ArrayList<Edge> result) {
		result.clear();
		assert (e.to >= 0 && e.to < graph.size());
		assert (e.from >= 0 && e.from < graph.size());
		int tolabel = graph.get(e.to).label;

		for (Edge it : graph.get(e.from).edge) {
			int tolabel2 = graph.get(it.to).label;
			if (e.to == it.to || minlabel > tolabel2 || history.hasVertex(it.to))
				continue;

			if (e.elabel < it.elabel || (e.elabel == it.elabel && tolabel <= tolabel2))
				result.add(it);
		}

		return (!result.isEmpty());
	}
}
