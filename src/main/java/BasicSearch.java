import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

public class BasicSearch {

    public static void main(String[] args) throws Exception {
        Playground playground = new Playground();
        Document document = playground.createDocument("name", "India");
        document.add(new TextField("description", "India, officially the Republic of India, is a country in South\" +\n" +
                "                \"Asia. It is the seventh-largest country by area, the second-most populous country, and the most\" +\n" +
                "                \"populous democracy in the world.", Field.Store.YES));
        playground.indexDocument(document);
        document = playground.createDocument("name", "Pacific Ocean");
        document.add(new TextField("description", "The Pacific Ocean is the largest and deepest of Earth's oceanic divisions." +
                "It extends from the Arctic Ocean in the north to the Southern Ocean in the south and is bounded by the continents of Asia" +
                "and Australia in the west and the Americas in the east.", Field.Store.YES));
        playground.indexDocument(document);
        playground.search("description", "west");
    }

}
