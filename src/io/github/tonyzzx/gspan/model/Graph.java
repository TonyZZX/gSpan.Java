package io.github.tonyzzx.gspan.model;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Graph extends ArrayList<Vertex> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int edge_size = 0;
	public boolean directed = false;

	public Graph() {
	}

	public Graph(boolean directed) {
		this.directed = directed;
	};

	public int vertex_size() {
		return size();
	}

	public void buildEdge() {
		String buf = null;
		NavigableMap<String, Integer> tmp = new TreeMap<>();

		int id = 0;
		for (int from = 0; from < size(); ++from) {
			for (Edge it : this.get(from).edge) {
				if (directed || from <= it.to)
					buf = from + " " + it.to + " " + it.elabel;
				else
					buf = it.to + " " + from + " " + it.elabel;

				// Assign unique id's for the edges.
				if (tmp.get(buf) == null) {
					it.id = id;
					tmp.put(buf, id);
					++id;
				} else {
					it.id = tmp.get(buf);
				}
			}
		}

		edge_size = id;
	}

	public BufferedReader read(BufferedReader is) throws IOException {
		ArrayList<String> result = new ArrayList<>();
		String line = null;

		clear();

		while ((line = is.readLine()) != null) {
			result.clear();
			String[] splitRead = line.split(" ");
			for (String str : splitRead) {
				result.add(str);
			}

			if (result.isEmpty()) {
				// do nothing
			} else if (result.get(0).equals("t")) {
				if (!this.isEmpty()) { // use as delimiter
					break;
				}
			} else if (result.get(0).equals("v") && result.size() >= 3) {
				// int id = Integer.parseInt(result.get(1));
				Vertex vex = new Vertex();
				vex.label = Integer.parseInt(result.get(2));
				this.add(vex);
			} else if (result.get(0).equals("e") && result.size() >= 4) {
				int from = Integer.parseInt(result.get(1));
				int to = Integer.parseInt(result.get(2));
				int elabel = Integer.parseInt(result.get(3));

				if (this.size() <= from || this.size() <= to) {
					System.out.println("Format Error:  define vertex lists before edges");
					return null;
				}

				this.get(from).push(from, to, elabel);

				if (directed == false) {
					this.get(to).push(to, from, elabel);
				}
			}
		}

		buildEdge();

		return is;
	}

	public FileWriter write(FileWriter os) throws IOException {
		String buf = null;
		NavigableSet<String> tmp = new TreeSet<>(new Comparator<String>() {
			// 边结果排序
			@Override
			public int compare(String o1, String o2) {
				String[] split1 = o1.split(" ");
				String[] split2 = o2.split(" ");
				if (Integer.parseInt(split1[0]) == Integer.parseInt(split2[0])) {
					if (Integer.parseInt(split1[1]) == Integer.parseInt(split2[1])) {
						return Integer.parseInt(split1[2]) - Integer.parseInt(split2[2]);
					} else {
						return Integer.parseInt(split1[1]) - Integer.parseInt(split2[1]);
					}
				} else {
					return Integer.parseInt(split1[0]) - Integer.parseInt(split2[0]);
				}
			}
		});

		for (int from = 0; from < size(); ++from) {
			os.write("v " + from + " " + this.get(from).label + System.getProperty("line.separator"));

			for (Edge it : this.get(from).edge) {
				if (directed || from <= it.to) {
					buf = from + " " + it.to + " " + it.elabel;
				} else {
					buf = it.to + " " + from + " " + it.elabel;
				}
				tmp.add(buf);
			}
		}

		for (String it : tmp) {
			os.write("e " + it + System.getProperty("line.separator"));
		}

		os.flush();
		return os;
	}

	public void check() {
		/*
		 * Check all indices
		 */
		for (int from = 0; from < size(); ++from) {
			System.out.println(
					"check vertex " + from + ", label " + this.get(from).label + System.getProperty("line.separator"));

			for (Edge it : this.get(from).edge) {
				System.out.println("   check edge from " + it.from + " to " + it.to + ", label " + it.elabel
						+ System.getProperty("line.separator"));
				assert (it.from >= 0 && it.from < size());
				assert (it.to >= 0 && it.to < size());
			}
		}
	}

	public void resize(int size) {
		while (this.size() < size) {
			this.add(new Vertex());
		}
	}
}
