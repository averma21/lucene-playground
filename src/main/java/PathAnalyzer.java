import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class PathAnalyzer extends Analyzer {

    private final Version matchVersion;

    private final int INDEX_ORIGINAL_TERM;

    /**
     * Creates a new {@link PathAnalyzer}
     *
     * @param matchVersion
     *            Lucene version to match See
     *            {@link #matchVersion above}
     */
    public PathAnalyzer(Version matchVersion) {
        this(matchVersion, false);
    }

    /**
     * Create a new {@link PathAnalyzer} with configurable flag to preserve
     * original term being analyzed too.
     * @param matchVersion Lucene version to match See {@link #matchVersion above}
     * @param indexOriginalTerm flag to setup analyzer such that
     *                              {@link WordDelimiterFilter#PRESERVE_ORIGINAL}
     *                              is set to oonfigure word delimeter
     */
    public PathAnalyzer(Version matchVersion, boolean indexOriginalTerm) {
        this.matchVersion = matchVersion;
        INDEX_ORIGINAL_TERM = indexOriginalTerm?WordDelimiterFilter.PRESERVE_ORIGINAL:0;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
        TokenStream tok = new LowerCaseFilter(matchVersion, src);
        tok = new WordDelimiterFilter(tok,
                WordDelimiterFilter.GENERATE_WORD_PARTS
                        | WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE
                        | this.INDEX_ORIGINAL_TERM
                        | WordDelimiterFilter.GENERATE_NUMBER_PARTS, null);
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
                char[] buffer = charTermAttr.buffer();
                boolean couldBePath = false;
                for (char c : buffer) {
                    if (c == '/') {
                        couldBePath = true;
                    }
                }
                if (couldBePath)
                    return true;
            }

            return false;
        }
    }
}
