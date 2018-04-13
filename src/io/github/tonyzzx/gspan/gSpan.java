package io.github.tonyzzx.gspan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import io.github.tonyzzx.gspan.model.DFSCode;
import io.github.tonyzzx.gspan.model.Edge;
import io.github.tonyzzx.gspan.model.Graph;
import io.github.tonyzzx.gspan.model.History;
import io.github.tonyzzx.gspan.model.PDFS;
import io.github.tonyzzx.gspan.model.Projected;
import io.github.tonyzzx.gspan.model.Vertex;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Vector;

public class gSpan {
	private ArrayList<Graph> TRANS;
	private DFSCode DFS_CODE;
	private DFSCode DFS_CODE_IS_MIN;
	private Graph GRAPH_IS_MIN;

	private long ID;
	private long minsup;
	private long maxpat_min;
	private long maxpat_max;
	private boolean directed;
	private FileWriter os;

	/*
	 * Singular vertex handling stuff [graph][vertexlabel] = count.
	 */
	NavigableMap<Integer, NavigableMap<Integer, Integer>> singleVertex;
	NavigableMap<Integer, Integer> singleVertexLabel;

	public gSpan() {
		TRANS = new ArrayList<>();
		DFS_CODE = new DFSCode();
		DFS_CODE_IS_MIN = new DFSCode();
		GRAPH_IS_MIN = new Graph();

		singleVertex = new TreeMap<>();
		singleVertexLabel = new TreeMap<>();
	}

	public void run(FileReader is, FileWriter _os, long _minsup, long _maxpat_max, long _maxpat_min, boolean _directed)
			throws IOException {
		os = _os;
		ID = 0;
		minsup = _minsup;
		maxpat_min = _maxpat_min;
		maxpat_max = _maxpat_max;
		directed = _directed;

		read(is);
		run_intern();
	}

	private FileReader read(FileReader is) throws IOException {
		BufferedReader read = new BufferedReader(is);
		while (true) {
			Graph g = new Graph(directed);
			read = g.read(read);
			if (g.isEmpty())
				break;
			TRANS.add(g);
		}
		return is;
	}

	private void run_intern() throws IOException {
		/*
		 * In case 1 node subgraphs should also be mined for, do this as
		 * preprocessing step.
		 */
		if (maxpat_min <= 1) {
			/*
			 * Do single node handling, as the normal gspan DFS code based
			 * processing cannot find subgraphs of size |subg|==1. Hence, we
			 * find frequent node labels explicitly.
			 */
			for (int id = 0; id < TRANS.size(); ++id) {
				for (int nid = 0; nid < TRANS.get(id).size(); ++nid) {
					int key = TRANS.get(id).get(nid).label;
					if (singleVertex.get(id) == null) {
						singleVertex.put(id, new TreeMap<>());
					}
					if (singleVertex.get(id).get(key) == null) {
						// number of graphs it appears in
						singleVertexLabel.put(key, Common.getValue(singleVertexLabel.get(key)) + 1);
					}

					singleVertex.get(id).put(key, Common.getValue(singleVertex.get(id).get(key)) + 1);
				}
			}
		}
		/*
		 * All minimum support node labels are frequent 'subgraphs'.
		 * singleVertexLabel[nodelabel] gives the number of graphs it appears
		 * in.
		 */
		for (Entry<Integer, Integer> it : singleVertexLabel.entrySet()) {
			if (it.getValue() < minsup)
				continue;

			int frequent_label = it.getKey();

			/*
			 * Found a frequent node label, report it.
			 */
			Graph g = new Graph(directed);
			Vertex v = new Vertex();
			v.label = frequent_label;
			g.add(v);

			/*
			 * [graph_id] = count for current substructure
			 */
			Vector<Integer> counts = new Vector<>();
			counts.setSize(TRANS.size());
			for (Entry<Integer, NavigableMap<Integer, Integer>> it2 : singleVertex.entrySet()) {
				counts.set(it2.getKey(), it2.getValue().get(frequent_label));
			}
			
			NavigableMap<Integer, Integer> gycounts = new TreeMap<>();
			for (int n = 0; n < counts.size(); ++n)
				gycounts.put(n, counts.get(n));

			report_single(g, gycounts);
		}

		ArrayList<Edge> edges = new ArrayList<>();
		NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();

		for (int id = 0; id < TRANS.size(); ++id) {
			Graph g = TRANS.get(id);
			for (int from = 0; from < g.size(); ++from) {
				if (Misc.get_forward_root(g, g.get(from), edges)) {
					for (Edge it : edges) {
						int key_1 = g.get(from).label;
						NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = root.get(key_1);
						if (root_1 == null) {
							root_1 = new TreeMap<>();
							root.put(key_1, root_1);
						}
						int key_2 = it.elabel;
						NavigableMap<Integer, Projected> root_2 = root_1.get(key_2);
						if (root_2 == null) {
							root_2 = new TreeMap<>();
							root_1.put(key_2, root_2);
						}
						int key_3 = g.get(it.to).label;
						Projected root_3 = root_2.get(key_3);
						if (root_3 == null) {
							root_3 = new Projected();
							root_2.put(key_3, root_3);
						}
						root_3.push(id, it, null);
					}
				}
			}
		}

		for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> fromlabel : root.entrySet()) {
			for (Entry<Integer, NavigableMap<Integer, Projected>> elabel : fromlabel.getValue().entrySet()) {
				for (Entry<Integer, Projected> tolabel : elabel.getValue().entrySet()) {
					/*
					 * Build the initial two-node graph. It will be grown
					 * recursively within project.
					 */
					DFS_CODE.push(0, 1, fromlabel.getKey(), elabel.getKey(), tolabel.getKey());
					project(tolabel.getValue());
					DFS_CODE.pop();
				}
			}
		}
	}

	/*
	 * Special report function for single node graphs.
	 */
	private void report_single(Graph g, NavigableMap<Integer, Integer> ncount) throws IOException {
		int sup = 0;
		for (Entry<Integer, Integer> it : ncount.entrySet()) {
			sup += Common.getValue(it.getValue());
		}

		if (maxpat_max > maxpat_min && g.size() > maxpat_max)
			return;
		if (maxpat_min > 0 && g.size() < maxpat_min)
			return;

		os.write("t # " + ID + " * " + sup + System.getProperty("line.separator"));
		g.write(os);
		ID++;
	}

	private void report(Projected projected, int sup) throws IOException {
		/*
		 * Filter to small/too large graphs.
		 */
		if (maxpat_max > maxpat_min && DFS_CODE.nodeCount() > maxpat_max)
			return;
		if (maxpat_min > 0 && DFS_CODE.nodeCount() < maxpat_min)
			return;

		Graph g = new Graph(directed);
		DFS_CODE.toGraph(g);
		os.write("t # " + ID + " * " + sup + System.getProperty("line.separator"));
		g.write(os);
		++ID;
	}

	/*
	 * Recursive subgraph mining function (similar to subprocedure 1
	 * Subgraph_Mining in [Yan2002]).
	 */
	private void project(Projected projected) throws IOException {
		/*
		 * Check if the pattern is frequent enough.
		 */
		int sup = support(projected);
		if (sup < minsup)
			return;

		/*
		 * The minimal DFS code check is more expensive than the support check,
		 * hence it is done now, after checking the support.
		 */
		if (is_min() == false) {
			return;
		}

		// Output the frequent substructure
		report(projected, sup);

		/*
		 * In case we have a valid upper bound and our graph already exceeds it,
		 * return. Note: we do not check for equality as the DFS exploration may
		 * still add edges within an existing subgraph, without increasing the
		 * number of nodes.
		 */
		if (maxpat_max > maxpat_min && DFS_CODE.nodeCount() > maxpat_max)
			return;

		/*
		 * We just outputted a frequent subgraph. As it is frequent enough, so
		 * might be its (n+1)-extension-graphs, hence we enumerate them all.
		 */
		ArrayList<Integer> rmpath = DFS_CODE.buildRMPath();
		int minlabel = DFS_CODE.get(0).fromlabel;
		int maxtoc = DFS_CODE.get(rmpath.get(0)).to;

		NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> new_fwd_root = new TreeMap<>();
		NavigableMap<Integer, NavigableMap<Integer, Projected>> new_bck_root = new TreeMap<>();
		ArrayList<Edge> edges = new ArrayList<>();

		/*
		 * Enumerate all possible one edge extensions of the current
		 * substructure.
		 */
		for (int n = 0; n < projected.size(); ++n) {

			int id = projected.get(n).id;
			PDFS cur = projected.get(n);
			History history = new History(TRANS.get(id), cur);

			// XXX: do we have to change something here for directed edges?

			// backward
			for (int i = rmpath.size() - 1; i >= 1; --i) {
				Edge e = Misc.get_backward(TRANS.get(id), history.get(rmpath.get(i)), history.get(rmpath.get(0)),
						history);
				if (e != null) {
					int key_1 = DFS_CODE.get(rmpath.get(i)).from;
					NavigableMap<Integer, Projected> root_1 = new_bck_root.get(key_1);
					if (root_1 == null) {
						root_1 = new TreeMap<>();
						new_bck_root.put(key_1, root_1);
					}
					int key_2 = e.elabel;
					Projected root_2 = root_1.get(key_2);
					if (root_2 == null) {
						root_2 = new Projected();
						root_1.put(key_2, root_2);
					}
					root_2.push(id, e, cur);
				}
			}

			// pure forward
			// FIXME: here we pass a too large e.to (== history[rmpath[0]].to
			// into get_forward_pure, such that the assertion fails.
			//
			// The problem is:
			// history[rmpath[0]].to > TRANS[id].size()
			if (Misc.get_forward_pure(TRANS.get(id), history.get(rmpath.get(0)), minlabel, history, edges))
				for (Edge it : edges) {
					int key_1 = maxtoc;
					NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.get(key_1);
					if (root_1 == null) {
						root_1 = new TreeMap<>();
						new_fwd_root.put(key_1, root_1);
					}
					int key_2 = it.elabel;
					NavigableMap<Integer, Projected> root_2 = root_1.get(key_2);
					if (root_2 == null) {
						root_2 = new TreeMap<>();
						root_1.put(key_2, root_2);
					}
					int key_3 = TRANS.get(id).get(it.to).label;
					Projected root_3 = root_2.get(key_3);
					if (root_3 == null) {
						root_3 = new Projected();
						root_2.put(key_3, root_3);
					}
					root_3.push(id, it, cur);
				}
			// backtracked forward
			for (int i = 0; i < rmpath.size(); ++i)
				if (Misc.get_forward_rmpath(TRANS.get(id), history.get(rmpath.get(i)), minlabel, history, edges))
					for (Edge it : edges) {
						int key_1 = DFS_CODE.get(rmpath.get(i)).from;
						NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = new_fwd_root.get(key_1);
						if (root_1 == null) {
							root_1 = new TreeMap<>();
							new_fwd_root.put(key_1, root_1);
						}
						int key_2 = it.elabel;
						NavigableMap<Integer, Projected> root_2 = root_1.get(key_2);
						if (root_2 == null) {
							root_2 = new TreeMap<>();
							root_1.put(key_2, root_2);
						}
						int key_3 = TRANS.get(id).get(it.to).label;
						Projected root_3 = root_2.get(key_3);
						if (root_3 == null) {
							root_3 = new Projected();
							root_2.put(key_3, root_3);
						}
						root_3.push(id, it, cur);
					}
		}

		/*
		 * Test all extended substructures.
		 */
		// backward
		for (Entry<Integer, NavigableMap<Integer, Projected>> to : new_bck_root.entrySet()) {
			for (Entry<Integer, Projected> elabel : to.getValue().entrySet()) {
				DFS_CODE.push(maxtoc, to.getKey(), -1, elabel.getKey(), -1);
				project(elabel.getValue());
				DFS_CODE.pop();
			}
		}

		// forward
		for (Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> from : new_fwd_root.descendingMap()
				.entrySet()) {
			for (Entry<Integer, NavigableMap<Integer, Projected>> elabel : from.getValue().entrySet()) {
				for (Entry<Integer, Projected> tolabel : elabel.getValue().entrySet()) {
					DFS_CODE.push(from.getKey(), maxtoc + 1, -1, elabel.getKey(), tolabel.getKey());
					project(tolabel.getValue());
					DFS_CODE.pop();
				}
			}
		}
	}

	private int support(Projected projected) {
		int oid = 0xffffffff;
		int size = 0;

		for (PDFS cur : projected) {
			if (oid != cur.id) {
				++size;
			}
			oid = cur.id;
		}

		return size;
	}

	private boolean is_min() {
		if (DFS_CODE.size() == 1)
			return (true);

		DFS_CODE.toGraph(GRAPH_IS_MIN);
		DFS_CODE_IS_MIN.clear();

		NavigableMap<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> root = new TreeMap<>();
		ArrayList<Edge> edges = new ArrayList<>();

		for (int from = 0; from < GRAPH_IS_MIN.size(); ++from)
			if (Misc.get_forward_root(GRAPH_IS_MIN, GRAPH_IS_MIN.get(from), edges))
				for (Edge it : edges) {
					int key_1 = GRAPH_IS_MIN.get(from).label;
					NavigableMap<Integer, NavigableMap<Integer, Projected>> root_1 = root.get(key_1);
					if (root_1 == null) {
						root_1 = new TreeMap<>();
						root.put(key_1, root_1);
					}
					int key_2 = it.elabel;
					NavigableMap<Integer, Projected> root_2 = root_1.get(key_2);
					if (root_2 == null) {
						root_2 = new TreeMap<>();
						root_1.put(key_2, root_2);
					}
					int key_3 = GRAPH_IS_MIN.get(it.to).label;
					Projected root_3 = root_2.get(key_3);
					if (root_3 == null) {
						root_3 = new Projected();
						root_2.put(key_3, root_3);
					}
					root_3.push(0, it, null);
				}

		Entry<Integer, NavigableMap<Integer, NavigableMap<Integer, Projected>>> fromlabel = root.firstEntry();
		Entry<Integer, NavigableMap<Integer, Projected>> elabel = fromlabel.getValue().firstEntry();
		Entry<Integer, Projected> tolabel = elabel.getValue().firstEntry();

		DFS_CODE_IS_MIN.push(0, 1, fromlabel.getKey(), elabel.getKey(), tolabel.getKey());

		return (project_is_min(tolabel.getValue()));
	}

	private boolean project_is_min(Projected projected) {
		ArrayList<Integer> rmpath = DFS_CODE_IS_MIN.buildRMPath();
		int minlabel = DFS_CODE_IS_MIN.get(0).fromlabel;
		int maxtoc = DFS_CODE_IS_MIN.get(rmpath.get(0)).to;

		{
			NavigableMap<Integer, Projected> root = new TreeMap<>();
			boolean flg = false;
			int newto = 0;

			for (int i = rmpath.size() - 1; !flg && i >= 1; --i) {
				for (int n = 0; n < projected.size(); ++n) {
					PDFS cur = projected.get(n);
					History history = new History(GRAPH_IS_MIN, cur);
					Edge e = Misc.get_backward(GRAPH_IS_MIN, history.get(rmpath.get(i)), history.get(rmpath.get(0)),
							history);
					if (e != null) {
						int key_1 = e.elabel;
						Projected root_1 = root.get(key_1);
						if (root_1 == null) {
							root_1 = new Projected();
							root.put(key_1, root_1);
						}
						root_1.push(0, e, cur);
						newto = DFS_CODE_IS_MIN.get(rmpath.get(i)).from;
						flg = true;
					}
				}
			}

			if (flg) {
				Entry<Integer, Projected> elabel = root.firstEntry();
				DFS_CODE_IS_MIN.push(maxtoc, newto, -1, elabel.getKey(), -1);
				if (DFS_CODE.get(DFS_CODE_IS_MIN.size() - 1)
						.equals(DFS_CODE_IS_MIN.get(DFS_CODE_IS_MIN.size() - 1)) != true)
					return false;
				return project_is_min(elabel.getValue());
			}
		}

		{
			boolean flg = false;
			int newfrom = 0;
			NavigableMap<Integer, NavigableMap<Integer, Projected>> root = new TreeMap<>();
			ArrayList<Edge> edges = new ArrayList<>();

			for (int n = 0; n < projected.size(); ++n) {
				PDFS cur = projected.get(n);
				History history = new History(GRAPH_IS_MIN, cur);
				if (Misc.get_forward_pure(GRAPH_IS_MIN, history.get(rmpath.get(0)), minlabel, history, edges)) {
					flg = true;
					newfrom = maxtoc;
					for (Edge it : edges) {
						int key_1 = it.elabel;
						NavigableMap<Integer, Projected> root_1 = root.get(key_1);
						if (root_1 == null) {
							root_1 = new TreeMap<>();
							root.put(key_1, root_1);
						}
						int key_2 = GRAPH_IS_MIN.get(it.to).label;
						Projected root_2 = root_1.get(key_2);
						if (root_2 == null) {
							root_2 = new Projected();
							root_1.put(key_2, root_2);
						}
						root_2.push(0, it, cur);
					}
				}
			}

			for (int i = 0; !flg && i < rmpath.size(); ++i) {
				for (int n = 0; n < projected.size(); ++n) {
					PDFS cur = projected.get(n);
					History history = new History(GRAPH_IS_MIN, cur);
					if (Misc.get_forward_rmpath(GRAPH_IS_MIN, history.get(rmpath.get(i)), minlabel, history, edges)) {
						flg = true;
						newfrom = DFS_CODE_IS_MIN.get(rmpath.get(i)).from;
						for (Edge it : edges) {
							int key_1 = it.elabel;
							NavigableMap<Integer, Projected> root_1 = root.get(key_1);
							if (root_1 == null) {
								root_1 = new TreeMap<>();
								root.put(key_1, root_1);
							}
							int key_2 = GRAPH_IS_MIN.get(it.to).label;
							Projected root_2 = root_1.get(key_2);
							if (root_2 == null) {
								root_2 = new Projected();
								root_1.put(key_2, root_2);
							}
							root_2.push(0, it, cur);
						}
					}
				}
			}

			if (flg) {
				Entry<Integer, NavigableMap<Integer, Projected>> elabel = root.firstEntry();
				Entry<Integer, Projected> tolabel = elabel.getValue().firstEntry();
				DFS_CODE_IS_MIN.push(newfrom, maxtoc + 1, -1, elabel.getKey(), tolabel.getKey());
				if (DFS_CODE.get(DFS_CODE_IS_MIN.size() - 1)
						.equals(DFS_CODE_IS_MIN.get(DFS_CODE_IS_MIN.size() - 1)) != true)
					return false;
				return project_is_min(tolabel.getValue());
			}
		}

		return true;
	}
}
