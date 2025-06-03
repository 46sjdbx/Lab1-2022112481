import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomWalkTest {
    private TextGraphAnalysis analysis;
    private static final String TEST_FILE_DIR = "file";

    @BeforeEach
    void setUp() {
        analysis = new TextGraphAnalysis();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("file/random_walk.txt"));
        Files.deleteIfExists(Paths.get("file/graph.txt"));
    }

    @Test
    void testRandomWalkCase1() {
        // 在调用 analysis.randomWalk() 之前没有添加任何节点到图中
        String result = analysis.randomWalk();
        assertEquals("", result, "空图应返回空字符串");
    }

    @Test
    void testRandomWalkCase2() throws IOException {
        // 准备测试数据
        analysis.graph.nodes.add("onlynode");

        // 获取原始目录和目标目录
        Path originalDir = Paths.get("file");
        Path renamedDir = Paths.get("file_temp"); // 临时目录名

        // 保存原始目录是否存在的状态
        boolean originalDirExists = Files.exists(originalDir);

        // 捕获 System.err 输出
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // 如果原始目录存在，将其重命名为临时目录
            if (originalDirExists) {
                Files.move(originalDir, renamedDir);
            }

            // 执行方法（此时 "file" 目录不存在，写入应失败）
            String result = analysis.randomWalk();

            // 验证返回值
            assertEquals("onlynode", result, "单节点图应返回节点本身");

            // 验证错误信息输出
            String errorOutput = new String(errContent.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(errorOutput.contains("Error: Unable to save random walk"),
                    "应打印错误信息到标准错误输出");

            // 验证文件未被创建
            assertFalse(Files.exists(Paths.get("file/random_walk.txt")),
                    "写入失败时文件不应被创建");

        } finally {
            // 恢复标准错误输出
            System.setErr(originalErr);

            // 恢复目录结构
            if (originalDirExists) {
                // 如果临时目录存在，将其重命名回原始目录
                if (Files.exists(renamedDir)) {
                    Files.move(renamedDir, originalDir);
                }
            } else {
                // 如果原始目录原本不存在，删除测试中可能创建的目录
                Files.deleteIfExists(originalDir);
            }
        }
    }



    @Test
    void testRandomWalkCase3() throws IOException {
        String testFile = TEST_FILE_DIR + "/single_node.txt";
        Files.write(Paths.get(testFile), Collections.singletonList("hello"));
        analysis.generateGraphFromFile(testFile);
        String result = analysis.randomWalk();
        assertEquals("hello", result, "输入图只有一个节点");
    }

    @Test
    void testRandomWalkCase4() throws IOException {
        String testFile = TEST_FILE_DIR + "/two_nodes.txt";
        Files.write(Paths.get(testFile), Arrays.asList("A B"));
        analysis.generateGraphFromFile(testFile);
        String result = analysis.randomWalk();
        assertTrue(result.equals("a b") || result.equals("b"),"第二轮因为outEdges.isEmpty()跳出循环");
    }

    @Test
    void testRandomWalkCase5() throws IOException {
        String testFile = TEST_FILE_DIR + "/cycle_graph.txt";
        Files.write(Paths.get(testFile), Arrays.asList("A B B A"));
        analysis.generateGraphFromFile(testFile);
        String result = analysis.randomWalk();
        assertTrue(result.equals("a b a") || result.equals("b a b") || result.equals("b b a b") || result.equals("b b") || result.equals("a b b a")  || result.equals("a b b"),"第二轮因为visitedEdges.contains(edge)跳出循环");
    }
}