# gSpan in Java

gSpan is a software package of mining frequent graphs in a graph database.  Given a collection of graphs and a minimum support threshold, gSpan is able to find all of the subgraphs whose frequency is above the threshold.<sup>[1][gSpan]</sup>

This is a Java implementation of frequent sub-graph mining algorithm gSpan. Most of the codes were ported from [gboost][gboost], which is a Matlab / C++ implementation of gSpan. The correctness of result is not guaranteed by the official. However, after countless testing by the author, the results are the same as [gboost][gboost] and [gSpan.Python][gSpan.Python]. What's more, the time of mining is much shorter than the above two implementations.

You may download the lastest `.jar` file from [here](../../releases).

## Documentation

### Graph file format

Below is an example of the format of a text file containing a set of graphs. Each line denodes a vertex (v) or edge (e) with a given label (end of line).

```
t # 0
v 0 2
v 1 2
v 2 2
v 3 3
v 4 2
e 0 1 2
e 0 2 2
e 2 3 3
e 2 4 2
t # 1
v 0 2
v 1 2
v 2 6
e 0 1 2
e 0 2 2
```

### How to run

This program supports 2 ways to run.

1. From the command line.

```
usage: gSpan
 -a,--max-node <arg>   Maximum number of nodes for each sub-graph
 -d,--data <arg>       (Required) File path of data set
 -h,--help             Help
 -i,--min-node <arg>   Minimum number of nodes for each sub-graph
 -r,--result <arg>     File path of result
 -t,--graph-type <arg> Type of graph: directed / undirected (default: undirected)
 -s,--sup <arg>        (Required) Minimum support
 ```

2. Directly run it from an IDE.

In this mode, you can only specify the file path of input data set and the minimum support.

## Reference

- [gSpan][gSpan]

gSpan: Graph-Based Substructure Pattern Mining, by X. Yan and J. Han.

- [gboost][gboost]

Matlab / C++ implementation of gSpan.

- [gSpan.Python][gSpan.Python]

Python implementation of gSpan.

[gSpan]: https://www.cs.ucsb.edu/~xyan/software/gSpan.htm
[gboost]: http://www.nowozin.net/sebastian/gboost/
[gSpan.Python]: https://github.com/betterenvi/gSpan
