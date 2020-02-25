import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

public class Playground {

    private static Version luceneVersion = Version.LUCENE_8_4_1;
    private final Directory indexDirectory;
    private static final String INDEX_DIRECTORY = "luceneIndex";
    private Analyzer analyzer;

    Playground() throws Exception {
        indexDirectory = SimpleFSDirectory.open(Paths.get(INDEX_DIRECTORY));
        analyzer = new StandardAnalyzer();
    }

    private void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private void printTokens(String fieldName, String text) throws IOException {
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

    void indexDocument(Document document) throws Exception {
        IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer));
        writer.addDocument(document);
        writer.flush();
        writer.commit();
        writer.close();
    }

    Document createDocument(String name, String value) throws IOException {
        Document document = new Document();
        document.add(new TextField(name, value, Field.Store.YES));
        printTokens(name, value);
        return document;
    }

    void search(String field, String text) throws Exception {
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

    void termSearch(String field, String text) throws Exception {
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query q1 = new TermQuery(new Term(field, text));
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

    private void pathSearch() throws Exception {
        Document document = createDocument("text", "Hi! this is the path /content/vegas/tower");
        indexDocument(document);
        search("text", "/content");
        search("text", "/content/vegas");
        search("text", "/content/vegas/tower");
    }

    private void pathSearch(String path, List<String> searchTerms, Analyzer analyzer) throws Exception {
        Analyzer oldAnalyzer = this.analyzer;
        setAnalyzer(analyzer);
        Document document = createDocument("text", path);
        indexDocument(document);
        for (String term : searchTerms) {
            termSearch("text", term);
        }
        setAnalyzer(oldAnalyzer);
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File(INDEX_DIRECTORY));
        Playground playground = new Playground();
        //playground.pathSearch();
        playground.pathSearch("Hi! this is the path /content/vegas/tower", new ArrayList<String>() {{
            add("/content");
            add("/content/vegas");
            add("/content/vegas/tower");
            add("/content/tower1/vegas");
        }}, new PathAnalyzer());
        playground.pathSearch("/content/dam/wknd/en/magazine/skitouring/Page%20Move%20Classes.jpg", new ArrayList<String>() {{
            add("/content/dam/wknd/en/magazine/skitouring/Page%20Move%20Classes.jpg");
        }}, new PathAnalyzer());
        // you can't know if node name is car%20wheel.jpg or car%20wheel.jpg" or car%20wheel.jpg">link<
        playground.pathSearch("Hey this is a <a href=\"/content/car%20wheel.jpg\">link</a>", new ArrayList<String>() {{
            add("/content/car%20wheel.jpg");
        }}, new PathAnalyzer());
    }

}
