import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class CustomTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final int length = 10;
    private char [] buf = new char[length];
    private int bufPos = -1;
    private int bufReadableLength = 0;
    private boolean startFreshPath = true;

    private char getNext() throws IOException {
        if (bufPos == -1) {
            bufReadableLength = input.read(buf);
            if (bufReadableLength == -1)
                return '\0';
            bufPos = 0;
        }
        char c = buf[bufPos++];
        if (bufPos == bufReadableLength)
            bufPos = -1;
        return c;
    }

    @Override
    public boolean incrementToken() throws IOException {
        char c;
        StringBuilder builder = new StringBuilder("");
        if (startFreshPath) {
            termAtt.setEmpty();
            while (true) {
                c = getNext();
                if (c == '/' || c == '\0')
                    break;
            }
            startFreshPath = false;
        }
        while (true){
            c = getNext();
            if (c == '\0' || c == '/' || c == ' ')
                break;
            builder.append(c);
        }
        if (builder.length() == 0)
            return false;
        String newAtt = termAtt.toString() + "/" + builder.toString();
        clearAttributes();
        termAtt.append(newAtt);
        if (c == ' ')
            startFreshPath = true;
        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        buf = new char[length];
        bufPos = -1;
        bufReadableLength = 0;
        startFreshPath = true;
    }

    public static void main(String[] args) throws Exception {
        CustomTokenizer ct = new CustomTokenizer();
        ct.setReader(new StringReader("abc/de/fg/hij/k/l"));
        ct.reset();
        while (ct.incrementToken()) {
            System.out.println(ct.termAtt.toString());
        }
        ct.close();
        ct.setReader(new StringReader("Hey there /fg/hij how /was/that"));
        ct.reset();
        while (ct.incrementToken()) {
            System.out.println(ct.termAtt.toString());
        }
    }

}
