import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Text;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

public class Playground {

    private static Version luceneVersion = Version.LUCENE_8_4_1;

    static void printTokensFromStandardTokenizer(String text) throws IOException {
        Reader r = new StringReader(text);
        final StandardTokenizer src = new StandardTokenizer();
        src.setReader(r);
        src.reset();
        System.out.println("Tokens from {" + text + "} using SA are:");
        while (src.incrementToken()) {
            CharTermAttribute cta = src.getAttribute(CharTermAttribute.class);
            System.out.println(cta.toString());
        }
    }

    static private void indexDocument(Document document) throws Exception {
        Analyzer analyzer  = new StandardAnalyzer();
        IndexWriter writer = new IndexWriter(new MMapDirectory(Path.of("/lucene-mmap")), new IndexWriterConfig(analyzer));
        writer.addDocument(document);
        writer.close();
    }

    static public Document createDocument(String name, String value) {
        Document document = new Document();
        addField(document, name, value);
        return document;
    }

    static private void addField(Document document, String name, String value) {
        IndexableField field = new TextField(name, new StringReader(value));
        field.fieldType();
        document.add(field);
    }

    public static void main(String[] args) throws Exception {
        String testString = "The path /p1/p2/p3 references /c1/c2";
        printTokensFromStandardTokenizer(testString);
        printTokensFromStandardTokenizer(testString.replace('/','_'));
        Document document = createDocument("name", "India");
        addField(document, "description", "India, officially the Republic of India, is a country in South" +
                "Asia. It is the seventh-largest country by area, the second-most populous country, and the most" +
                "populous democracy in the world.");
        addField(document, "type", "country");
        indexDocument(document);
        document = createDocument("name", "Pacific Ocean");
        addField(document, "description", "The Pacific Ocean is the largest and deepest of Earth's oceanic" +
                "divisions. It extends from the Arctic Ocean in the north to the Southern Ocean in the south and is" +
                "bounded by the continents of Asia and Australia in the west and the Americas in the east.");
        addField(document, "type", "ocean");
    }

}
