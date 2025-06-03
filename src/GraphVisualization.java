import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * This class provides a visualization of a directed weighted graph using JGraphX.
 */
public class GraphVisualization {

  /**
   * The main method to run the graph visualization.
   * It reads graph data from a file, creates a graph, and visualizes it.
   */
  public static void main(String[] args) {
    // 创建有向加权图
    Graph<String, DefaultWeightedEdge> graph
            = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    try (BufferedReader reader = new BufferedReader(new FileReader("file/graph.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 解析每行数据
        String[] parts = line.split(" ");
        String from = parts[0];
        String to = parts[1];
        int weight = Integer.parseInt(parts[2]);

        // 添加节点
        graph.addVertex(from);
        graph.addVertex(to);
        // 添加边并设置权重
        DefaultWeightedEdge edge = graph.addEdge(from, to);
        graph.setEdgeWeight(edge, weight);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 使用 JGraphX 进行可视化
    JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);

    // 设置节点样式为椭圆
    Object[] vertices = graphAdapter.getChildVertices(graphAdapter.getDefaultParent());
    for (Object vertex : vertices) {
      graphAdapter.getModel().setStyle(
          vertex, mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_ELLIPSE + ";"
                      + mxConstants.STYLE_FILLCOLOR + "=lightblue;"
                      + mxConstants.STYLE_STROKECOLOR + "=black;");
    }

    // 设置边的标签为权重
    Object[] edges = graphAdapter.getChildEdges(graphAdapter.getDefaultParent());
    for (Object edge : edges) {
      Object source = graphAdapter.getModel().getTerminal(edge, true);
      Object target = graphAdapter.getModel().getTerminal(edge, false);
      String sourceVertex = (String) graphAdapter.getModel().getValue(source);
      String targetVertex = (String) graphAdapter.getModel().getValue(target);
      DefaultWeightedEdge jgraphtEdge = graph.getEdge(sourceVertex, targetVertex);
      double weight = graph.getEdgeWeight(jgraphtEdge);
      graphAdapter.getModel().setValue(edge, String.valueOf(weight));
      graphAdapter.getModel().setStyle(
          edge, mxConstants.STYLE_LABEL_BACKGROUNDCOLOR + "=white;"
                      + mxConstants.STYLE_FONTCOLOR + "=black;");
    }

    // 执行圆形布局
    mxCircleLayout layout = new mxCircleLayout(graphAdapter);
    layout.execute(graphAdapter.getDefaultParent());

    // 创建图形组件
    mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
    JFrame frame = new JFrame("Directed Graph Visualization.");
    frame.getContentPane().add(graphComponent);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
