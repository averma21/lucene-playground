import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.path.PathHierarchyTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TokenizerPlayground {

    private static void printPathTokens(String text) throws IOException {
        Tokenizer tokenizer = new PathHierarchyTokenizer();
        Reader reader = new StringReader(text);
        tokenizer.setReader(reader);
        tokenizer.reset();
        System.out.println("==========================================");
        System.out.println("Tokens from {" + text + "} are:");
        while (tokenizer.incrementToken()) {
            CharTermAttribute cta = tokenizer.getAttribute(CharTermAttribute.class);
            System.out.println(cta.toString());
        }
        System.out.println("==========================================");
        tokenizer.close();
    }

    public static void main(String[] args) throws Exception {

        printPathTokens("ad");
        printPathTokens("a/b/c");
        printPathTokens("/a/b/c");
        printPathTokens("/a d/b/c");
        printPathTokens("hello there /a d/b/c");
        printPathTokens("/a/b/c and /d/e");
    }

}
