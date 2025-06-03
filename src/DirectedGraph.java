import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a directed graph using an adjacency list.
 */
public class DirectedGraph {

  /**
   * Stores the adjacency list of the graph.
   * The adjacency list is a hash map where the key is a node
   * in the graph, and the value is another hash map. This inner hash map's key is another node that
   * the first node points to, and the value is the weight of the edge
   * (i.e., the number of edges fromnode from to node to).
   */
  private final Map<String, Map<String, Integer>> adjacencyList;
  public  final Set<String> nodes; // 显式维护节点集合

  /**
   * Constructs a new directed graph.
   */
  public DirectedGraph() {
    adjacencyList = new HashMap<>();
    nodes = new HashSet<>(); // 初始化节点集合
  }

  /**
   * Adds an edge from node from to node to.
   *
   * @param from the source node
   * @param to the target node
   */
  public void addEdge(String from, String to) {
    // 显式添加起点和终点到节点集合
    nodes.add(from);
    nodes.add(to);

    adjacencyList.computeIfAbsent(from, k -> new HashMap<>());
    adjacencyList.get(from).put(to, adjacencyList.get(from).getOrDefault(to, 0) + 1);
  }


  /**
   * Returns the adjacency list of the graph.
   *
   * @return the adjacency list
   */
  public Map<String, Map<String, Integer>> getAdjacencyList() {
    return adjacencyList;
  }

  /**
   * Returns all nodes in the graph, including those that are only endpoints of edges.
   *
   * @return a set of all nodes
   */
  public Set<String> getNodes() {
    return new HashSet<>(nodes); // 直接返回节点集合
  }


  /**
   * Returns the out edges of a given node.
   *
   * @param node the node to get out edges from
   * @return a map of out edges with their weights
   */
  public Map<String, Integer> getOutEdges(String node) {
    return adjacencyList.getOrDefault(node, new HashMap<>());
  }

}
