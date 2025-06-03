import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("checkstyle:Indentation")
class QueryBridgeWordsTest {
    private TextGraphAnalysis analysis;

    // 在每个测试方法执行前初始化图数据
    @BeforeEach
    void setup() {
        analysis = new TextGraphAnalysis();
        try {
            analysis.generateGraphFromFile("file/Easy Test.txt");
        } catch (IOException e) {
            fail("图初始化失败: " + e.getMessage());
        }
    }

    @Test
    public void testQueryBridgeWordsCase1() {
        String word1 = "the";
        String word2 = "carefully";

        String expected = "The bridge words from the to carefully is scientist.";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }

    @Test
    public void testQueryBridgeWordsCase2() {
        String word1 = "scientist";
        String word2 = "analyzed";
        String expected = "The bridge words from scientist to analyzed are carefully and quickly.";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }

    @Test
    public void testQueryBridgeWordsCase3() {
        String word1 = "dog";
        String word2 = "the";
        String expected = "No word1 or word2 in the graph!";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }

    @Test
    public void testQueryBridgeWordsCase4() {
        String word1 = "data";
        String word2 = "dog";
        String expected = "No word1 or word2 in the graph!";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }

    @Test
    public void testQueryBridgeWordsCase5() {
        String word1 = "student";
        String word2 = "teacher";
        String expected = "No word1 or word2 in the graph!";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }

    @Test
    public void testQueryBridgeWordsCase6() {
        String word1 = "the";
        String word2 = "data";
        String expected = "No bridge words from the to data!";
        assertEquals(expected, analysis.queryBridgeWords(word1, word2));
    }


    // 测试generateGraphFromFile方法的异常情况
    @Test
    public void testGenerateGraphFromFileIOException() {
        TextGraphAnalysis analysis = new TextGraphAnalysis();
        assertThrows(IOException.class, () -> {
            // 传入一个不存在的文件路径来触发异常
            analysis.generateGraphFromFile("nonexistent_file.txt");
        });
    }

}
