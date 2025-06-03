import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TextGraphAnalysis类用于文本图分析相关操作.
 * 它管理一个有向图对象，包含对图的构建、分析等功能。
 */
public class TextGraphAnalysis {

  final DirectedGraph graph;
  private static final Logger logger = Logger.getLogger(TextGraphAnalysis.class.getName());

  /**
   * 构造函数，初始化有向图对象.
   */
  public TextGraphAnalysis() {
    graph = new DirectedGraph();
  }

  /**
   * 从指定文件读取内容并生成有向图.
   *
   * @param filePath 文件路径
   * @throws IOException 文件读取异常
   */
  public void generateGraphFromFile(String filePath) throws IOException {
    // 将路径限制在当前工作目录下
    Path baseDir = Paths.get(System.getProperty("user.dir"));
    Path path = baseDir.resolve(filePath).normalize();

    // 确保最终路径仍在工作目录内
    if (!path.startsWith(baseDir)) {
      throw new SecurityException("Access to files outside working directory is not allowed");
    }

    StringBuilder text = new StringBuilder();
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        text.append(line).append(' ');
      }
    }
    String[] words = cleanText(text.toString()).split("\\s+");
    for (String word : words) {
      graph.nodes.add(word); // 直接访问DirectedGraph的nodes集合（需设置为public或添加addNode方法）
    }
    for (int i = 0; i < words.length - 1; i++) {
      graph.addEdge(words[i], words[i + 1]);
    }
  }

  /**
   * 清理文本，去除非字母字符并转换为小写.
   *
   * @param text 原始文本
   * @return 清理后的文本
   */
  private String cleanText(String text) {
    // 去除文本中所有的标点符号、数字等非字母字符，只保留字母和空格，并转换为小写
    return text.replaceAll("[^a-zA-Z\\s]", " ")
               .toLowerCase()
               .replaceAll("\\s+", " ")
               .trim();
  }

  /**
   * 显示有向图的邻接表，并写入文件.
   */
  public void showDirectedGraph() {
    // 获取邻接表
    Map<String, Map<String, Integer>> adjacencyMap = graph.getAdjacencyList();

    // 使用 Files.newBufferedWriter 创建带缓冲的写入流
    try (BufferedWriter writer = Files.newBufferedWriter(
            Paths.get("file/graph.txt"),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {

      for (Map.Entry<String, Map<String, Integer>> entry : adjacencyMap.entrySet()) {
        String from = entry.getKey();
        Map<String, Integer> outEdges = entry.getValue();
        for (Map.Entry<String, Integer> edge : outEdges.entrySet()) {
          String to = edge.getKey();
          int weight = edge.getValue();
          System.out.println(from + " -> " + to + " (weight: " + weight + ")");
          writer.write(from + " " + to + " " + weight + "\n");
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write graph to file", e);
      System.err.println("Error: Unable to save graph. Check logs for details.");
    }
  }

  /**
   * 查询两个单词之间的桥接词.
   *
   * @param word1 起始单词
   * @param word2 结束单词
   * @return 桥接词列表字符串，或错误信息
   */
  public String queryBridgeWords(String word1, String word2) {
    if (!graph.getNodes().contains(word1) || !graph.getNodes().contains(word2)) {
      return "No word1 or word2 in the graph!";
    }
    List<String> bridgeWords = new ArrayList<>();
    Map<String, Integer> outEdges1 = graph.getOutEdges(word1);
    for (String intermediate : outEdges1.keySet()) {
      if (graph.getOutEdges(intermediate).containsKey(word2)) {
        bridgeWords.add(intermediate);
      }
    }
    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    }
    StringBuilder result = new StringBuilder();
    int size = bridgeWords.size();
    result.append("The bridge words from ")
            .append(word1)
            .append(" to ")
            .append(word2)
            .append(size == 1 ? " is " : " are ");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        if (i == bridgeWords.size() - 1) {
          result.append(" and ");
        } else {
          result.append(", ");
        }
      }
      result.append(bridgeWords.get(i));
    }
    return result.append('.').toString();
  }

  /**
   * 根据桥接词生成新文本.
   * 该方法接收一个输入文本，清理并分割成单词数组。
   * 然后，对于每个单词，查找其桥接词（即该单词和下一个单词之间的中介单词）。
   * 如果存在桥接词，则随机选择一个桥接词插入到新文本中。
   *
   * @param inputText 输入的原始文本
   * @return 根据桥接词生成的新文本
   */

  public String generateNewText(String inputText) {
    String[] words = cleanText(inputText).split("\\s+");
    StringBuilder newText = new StringBuilder();

    SecureRandom random = new SecureRandom();
    List<String> bridgeWords = new ArrayList<>();
    for (int i = 0; i < words.length; i++) {
      newText.append(words[i]);
      if (i < words.length - 1) {
        // 每次循环开始前清空bridgeWords
        bridgeWords.clear();
        Map<String, Integer> outEdges1 = graph.getOutEdges(words[i]);
        for (String intermediate : outEdges1.keySet()) {
          if (graph.getOutEdges(intermediate).containsKey(words[i + 1])) {
            bridgeWords.add(intermediate);
          }
        }
        if (!bridgeWords.isEmpty()) {
          newText.append(' ').append(bridgeWords.get(random.nextInt(bridgeWords.size())));
        }
        newText.append(' ');
      }
    }
    return newText.toString();
  }

  /**
   * 计算最短路径（单点或两点模式）.
   *
   * @param word1 起始单词
   * @param word2 目标单词（可选，单点模式时为null）
   * @return 最短路径信息字符串
   */
  public String calcShortestPath(String word1, String word2) {
    word1 = cleanText(word1).trim();
    boolean singleNodeMode = (word2 == null || word2.trim().isEmpty());
    word2 = singleNodeMode ? null : cleanText(word2).trim();

    if (!graph.getNodes().contains(word1)) {
      return "No \"" + word1 + "\" in the graph!";
    }

    if (!singleNodeMode && !graph.getNodes().contains(word2)) {
      return "No \"" + word2 + "\" in the graph!";
    }

    // 计算初始容量
    Set<String> allNodes = graph.getNodes();
    int nodeSize = allNodes.size();
    int mapCapacity = (int) (nodeSize / 0.75) + 1;
    // 初始化dist和prev
    Map<String, Integer> dist = new HashMap<>(mapCapacity);
    Map<String, List<String>> prev = new HashMap<>(mapCapacity);
    // 初始化PriorityQueue
    PriorityQueue<Node> pq
            = new PriorityQueue<>(nodeSize, Comparator.comparingInt(n -> n.distance));

    for (String node : allNodes) {
      dist.put(node, Integer.MAX_VALUE);
      Map<String, Integer> outEdges = graph.getOutEdges(node); // 明确缓存结果
      prev.put(node, new ArrayList<>(outEdges.size()));
    }
    dist.put(word1, 0);
    pq.add(new Node(word1, 0));

    while (!pq.isEmpty()) {
      Node current = pq.poll();
      String u = current.word;

      if (current.distance > dist.get(u)) {
        continue;
      }

      for (Map.Entry<String, Integer> edge : graph.getOutEdges(u).entrySet()) {
        String v = edge.getKey();
        int weight = edge.getValue();
        int newDist = dist.get(u) + weight;

        if (newDist < dist.get(v)) {
          dist.put(v, newDist);
          prev.get(v).clear();
          prev.get(v).add(u);
          pq.add(new Node(v, newDist));
        } else if (newDist == dist.get(v)) {
          prev.get(v).add(u);
        }
      }
    }

    StringBuilder result = new StringBuilder();

    if (singleNodeMode) {
      result.append("Shortest paths from \"").append(word1).append("\":\n");
      for (String node : graph.getNodes()) {
        if (node.equals(word1)) {
          continue;
        }

        if (dist.get(node) == Integer.MAX_VALUE) {
          result.append("No path to \"").append(node).append("\"\n");
          continue;
        }

        List<List<String>> allPaths = new ArrayList<>();
        LinkedList<String> currentPath = new LinkedList<>();
        currentPath.addFirst(node);
        backtrack(word1, node, prev, currentPath, allPaths);

        result.append(formatPaths(word1, node, dist.get(node), allPaths));
      }
    } else {
      if (dist.get(word2) == Integer.MAX_VALUE) {
        return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
      }

      List<List<String>> allPaths = new ArrayList<>();
      LinkedList<String> currentPath = new LinkedList<>();
      currentPath.addFirst(word2);
      backtrack(word1, word2, prev, currentPath, allPaths);

      result.append(formatPaths(word1, word2, dist.get(word2), allPaths));
    }

    return result.toString();
  }


  /**
   * 回溯路径，查找所有最短路径.
   *
   * @param start   起始节点
   * @param current 当前节点
   * @param prev    前驱节点映射
   * @param path    当前路径
   * @param allPaths 所有路径集合
   */
  private void backtrack(String start, String current,
                        Map<String, List<String>> prev,
                        LinkedList<String> path,
                        List<List<String>> allPaths) {
    if (current.equals(start)) {
      allPaths.add(new ArrayList<>(path));
      return;
    }

    for (String pred : prev.get(current)) {
      path.addFirst(pred);
      backtrack(start, pred, prev, path, allPaths);
      path.removeFirst();
    }
  }

  /**
   * 格式化路径信息.
   *
   * @param from   起始节点
   * @param to     目标节点
   * @param distance 路径长度
   * @param paths  路径列表
   * @return 格式化后的字符串
   */
  private String formatPaths(String from, String to, int distance,
                            List<List<String>> paths) {
    StringBuilder sb = new StringBuilder();
    sb.append("Shortest path(s) from \"").append(from)
      .append("\" to \"").append(to).append("\" (length: ")
      .append(distance).append("):\n");

    for (int i = 0; i < paths.size(); i++) {
      sb.append("Path ").append(i + 1).append(": ");
      sb.append(String.join(" -> ", paths.get(i)));
      sb.append('\n');
    }

    return sb.toString();
  }

  /**
   * 计算单词的PageRank值.
   *
   * @param word 目标单词
   * @return PageRank值，若单词不存在则返回0
   */
  public double calPageRank(String word) {
    if (!graph.getNodes().contains(word)) {
      return 0;
    }
    double d = 0.85;

    // 获取节点集合及数量
    Set<String> nodes = graph.getNodes();
    int nodeCount = nodes.size();
    int mapCapacity = (int) (nodeCount / 0.75) + 1; // 计算初始容量

    Map<String, Double> pr = new HashMap<>(mapCapacity);
    Map<String, Integer> wordFrequency = new HashMap<>(mapCapacity);

    // 计算词频
    for (String node : nodes) {
      wordFrequency.put(node, wordFrequency.getOrDefault(node, 0) + 1);
      Map<String, Integer> outEdges = graph.getOutEdges(node);
      for (Map.Entry<String, Integer> edge : outEdges.entrySet()) {
        String neighbor = edge.getKey();
        int weight = edge.getValue(); // 直接获取值，无需二次查找
        wordFrequency.put(neighbor, wordFrequency.getOrDefault(neighbor, 0) + weight);
      }
    }

    // 初始化PageRank
    int totalFrequency = wordFrequency.values().stream().mapToInt(Integer::intValue).sum();
    for (String node : graph.getNodes()) {
      pr.put(node, (double) wordFrequency.get(node) / totalFrequency);
    }

    // 迭代计算PageRank
    for (int i = 0; i < 100; i++) {
      Map<String, Double> newpr = new HashMap<>(mapCapacity);
      double sinkpr = 0;

      // 计算出度为0的节点PR总和
      for (String node : graph.getNodes()) {
        if (graph.getOutEdges(node).isEmpty()) {
          sinkpr += pr.get(node);
        }
      }
      Set<String> allNodes = graph.getNodes();
      int totalNodes = allNodes.size();  // 缓存节点总数
      for (String node : allNodes) {
        double sum = 0;
        for (String inNode : allNodes) {
          Map<String, Integer> outEdges = graph.getOutEdges(inNode);
          if (outEdges.containsKey(node)) {
            sum += pr.get(inNode) / outEdges.size();
          }
        }
        newpr.put(node, (1 - d) / totalNodes
                + d * (sum + sinkpr / totalNodes));
      }
      pr = newpr;
    }
    return pr.get(word);
  }

  /**
   * 执行随机游走并生成路径.
   *
   * @return 随机游走路径字符串
   */
  public String randomWalk() {
    // 创建一个 SecureRandom 实例，用于生成安全的随机数
    SecureRandom random = new SecureRandom();
    // 获取图中的所有节点，并将其存储在列表 nodes 中
    List<String> nodes = new ArrayList<>(graph.getNodes());
    System.out.println(graph.getNodes().size());
    // 如果节点列表为空，返回空字符串
    //——————————————————————————————————————————————————————————————————————————————————————
    if (nodes.isEmpty()) {
      return "";
    }
    //——————————————————————————————————————————————————————————————————————————————————————
    // 随机选择一个节点作为起始节点
    // random.nextInt(nodes.size()) 生成一个介于 0（包括）和 nodes.size()（不包括）之间的随机整数。
    String startNode = nodes.get(random.nextInt(nodes.size()));
    // 使用 StringBuilder 初始化路径字符串，以起始节点开始
    StringBuilder path = new StringBuilder(startNode);
    // 将当前节点设置为起始节点
    String current = startNode;
    // 创建一个 HashSet ,存储已经访问过的边
    Set<String> visitedEdges = new HashSet<>();
    //————————————————————————————————————————————————————————————————————————————————————————
    // 进入一个无限循环，直到遇到终止条件
    while (true) {
      // 获取当前节点的所有出边及其权重，并存储在 map outEdges 中
      Map<String, Integer> outEdges = graph.getOutEdges(current);
      // 如果当前节点没有出边，退出循环
      // ——————————————————————————————————————————————————————————————————————————————————————
      if (outEdges.isEmpty()) {
        break;
      }
      //———————————————————————————————————————————————————————————————————————————————————————
      // 将出边中的所有邻居节点提取到列表 neighbors 中
      List<String> neighbors = new ArrayList<>(outEdges.keySet());
      // 随机选择一个邻居节点作为下一个节点
      String next = neighbors.get(random.nextInt(neighbors.size()));
      // 生成一条从当前节点到下一个节点的边
      String edge = current + "->" + next;
      //———————————————————————————————————————————————————————————————————————————————————————
      // 如果这条边已经被访问过，退出循环
      if (visitedEdges.contains(edge)) {
        break;
      }
      //———————————————————————————————————————————————————————————————————————————————————————
      // 将这条边添加到已访问边的集合中
      visitedEdges.add(edge);
      // 将下一个节点添加到路径字符串中
      path.append(' ').append(next);
      // 更新当前节点为下一个节点
      current = next;
    }
    //—————————————————————————————————————————————————————————————————————————————————————————
    // 尝试将随机游走生成的路径写入文件 "random_walk.txt"
    try (BufferedWriter writer = Files.newBufferedWriter(
            // 指定文件路径
            Paths.get("file/random_walk.txt"),
            // 使用 UTF_8 编码
            StandardCharsets.UTF_8,
            // 如果文件不存在则创建，如果存在则清空文件内容
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      // 将路径写入文件
      writer.write(path.toString());
      //————————————————————————————————————————————————————————————————————————————————————————
    } catch (IOException e) {
      // 如果写入过程中发生异常，记录错误日志
      //logger.log(Level.SEVERE, "Failed to write random walk to file", e);
      // 在标准错误流中打印错误信息
      System.err.println("Error: Unable to save random walk. Check logs for details.");
    }
    // 返回随机游走生成的路径字符串
    //—————————————————————————————————————————————————————————————————————————————————————————————
    return path.toString();
  }


  static class Node {

    String word;
    int distance;

    Node(String word, int distance) {
      this.word = word;
      this.distance = distance;
    }
  }

  /**
   * 主函数，用于命令行交互.
   *
   * @param args 命令行参数
   */
  public static void main(String[] args) {
    TextGraphAnalysis analysis = new TextGraphAnalysis();

    // 显式指定 UTF-8 编码
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
    String filePath;

    if (args.length > 0) {
      filePath = args[0];
    } else {
      filePath = "file/Easy Test.txt";
    }

    try {
      analysis.generateGraphFromFile(filePath);
    } catch (IOException e) {
      System.out.println("读取文件时出错: " + e.getMessage());
      return;
    }


    while (true) {
      System.out.println("\n请选择操作:");
      System.out.println("1. 展示有向图");
      System.out.println("2. 查询桥接词");
      System.out.println("3. 根据桥接词生成新文本");
      System.out.println("4. 计算两个单词之间的最短路径");
      System.out.println("5. 计算单词的 PageRank");
      System.out.println("6. 随机游走");
      System.out.println("7. 退出");

      int choice = scanner.nextInt();
      scanner.nextLine(); // 消耗换行符

      switch (choice) {
        case 1:
          analysis.showDirectedGraph();
          break;
        case 2:
          System.out.print("请输入两个英文单词 (用空格分隔): ");
          String[] words = scanner.nextLine().split(" ");
          if (words.length == 2) {
            System.out.println(analysis.queryBridgeWords(words[0], words[1]));
          } else {
            System.out.println("输入格式错误，请输入两个英文单词。");
          }
          break;
        case 3:
          System.out.print("请输入一行新文本: ");
          String inputText = scanner.nextLine();
          System.out.println(analysis.generateNewText(inputText));
          break;
        case 4:
          System.out.print("请输入一个或两个英文单词 (用空格分隔): ");
          String[] s = scanner.nextLine().trim().split("\\s+");
          if (s.length == 1) {
            System.out.println(analysis.calcShortestPath(s[0], null));
          } else if (s.length == 2) {
            System.out.println(analysis.calcShortestPath(s[0], s[1]));
          } else {
            System.out.println("输入格式错误，请输入1个或2个英文单词。");
          }
          break;
        case 5:
          System.out.print("请输入一个英文单词: ");
          String word = scanner.nextLine();
          System.out.println("PageRank 值: " + analysis.calPageRank(word));
          break;
        case 6:
          System.out.println("随机游走路径: " + analysis.randomWalk());
          break;
        case 7:
          System.out.println("退出程序。");
          return;
        default:
          System.out.println("无效的选择，请重新输入。");
      }
    }
  }
}
