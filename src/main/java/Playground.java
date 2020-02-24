import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Playground {

    private static Version luceneVersion = Version.LUCENE_8_4_1;
    private final Directory indexDirectory;
    private static final String INDEX_DIRECTORY = "luceneIndex";
    private Analyzer analyzer;

    public Playground() throws Exception {
        indexDirectory = SimpleFSDirectory.open(Path.of(INDEX_DIRECTORY));
        analyzer = new StandardAnalyzer();
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    void printTokens(String fieldName, String text) throws IOException {
        final TokenStream src = analyzer.tokenStream(fieldName, text);
        src.reset();
        System.out.println("==========================================");
        System.out.println("Tokens from {" + text + "} are:");
        while (src.incrementToken()) {
            CharTermAttribute cta = src.getAttribute(CharTermAttribute.class);
            System.out.println(cta.toString());
        }
        System.out.println("==========================================");
        src.close();
    }

    private void indexDocument(Document document) throws Exception {
        IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer));
        writer.addDocument(document);
        writer.flush();
        writer.commit();
        writer.close();
    }

    public Document createDocument(String name, String value) throws IOException {
        Document document = new Document();
        value = value.replace("/", "_");
        document.add(new TextField(name, value, Field.Store.YES));
        printTokens(name, value);
        return document;
    }

    private void search(String field, String text) throws Exception {
        QueryBuilder bldr = new QueryBuilder(analyzer);
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query q1 = bldr.createPhraseQuery(field, text);
        TopDocs topDocs = searcher.search(q1, 100);
        System.out.println("==========================================");
        System.out.println("Searching for {" + field + ":" + text + "} got " + topDocs.totalHits);
        for (ScoreDoc doc : topDocs.scoreDocs) {
            System.out.println("Score: " + doc.score);
            int docidx = doc.doc;
            Document docRetrieved = searcher.doc(docidx);
            System.out.println("Value :" + docRetrieved.getField(field).stringValue());
        }
        System.out.println("==========================================");
        reader.close();
    }

    private void  testTokenization() throws IOException {
        String testString = "The path /p1/p2/p3 references /c1/c2";
        printTokens("test", testString);
        printTokens("test2", testString.replace('/','_'));
    }

    private void basicSearch() throws Exception {
        Document document = createDocument("name", "India");
        document.add(new TextField("description", "India, officially the Republic of India, is a country in South\" +\n" +
                "                \"Asia. It is the seventh-largest country by area, the second-most populous country, and the most\" +\n" +
                "                \"populous democracy in the world.", Field.Store.YES));
        indexDocument(document);
        document = this.createDocument("name", "Pacific Ocean");
        document.add(new TextField("description", "The Pacific Ocean is the largest and deepest of Earth's oceanic divisions." +
                "It extends from the Arctic Ocean in the north to the Southern Ocean in the south and is bounded by the continents of Asia" +
                "and Australia in the west and the Americas in the east.", Field.Store.YES));
        indexDocument(document);
        search("description", "west");
    }

    private void pathSearch() throws Exception {
        Document document = createDocument("text", "Hi! this is the path /content/vegas/tower");
        indexDocument(document);
        search("text", "/content");
        search("text", "/content/vegas");
        search("text", "/content/vegas/tower");
    }

    private void pathSearch2() throws Exception {
        setAnalyzer(new PathAnalyzer());
        Document document = createDocument("text", "Hi! this is the path /content/vegas/tower");
        indexDocument(document);
        document = createDocument("text", "Hi! no path!");
        indexDocument(document);
        search("text", "_content");
//        search("text", "/content/vegas");
//        search("text", "/content/vegas/tower");
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File(INDEX_DIRECTORY));
        Playground playground = new Playground();
        playground.pathSearch2();
    }

}
