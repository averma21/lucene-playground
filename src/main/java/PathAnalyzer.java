import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class PathAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream tok = new LowerCaseFilter(src);
        tok = new PathFilter(tok);
        return new TokenStreamComponents(src, tok);
    }

    public static class PathFilter extends TokenFilter {

        private CharTermAttribute charTermAttr;

        PathFilter(TokenStream input) {
            super(input);
            this.charTermAttr = addAttribute(CharTermAttribute.class);
        }

        @Override
        public boolean incrementToken() throws IOException {

            while (input.incrementToken()) {
                boolean couldBePath = charTermAttr.toString().contains("_");
                if (couldBePath)
                    return true;
            }

            return false;
        }
    }
}
