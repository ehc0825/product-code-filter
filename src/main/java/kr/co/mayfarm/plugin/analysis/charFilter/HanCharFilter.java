package kr.co.mayfarm.plugin.analysis.charFilter;

import org.apache.lucene.analysis.charfilter.BaseCharFilter;
import kr.co.mayfarm.plugin.analysis.util.HanjaUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HanCharFilter extends BaseCharFilter {

    public static final String NAME = "han_char_filter";

    private Reader transformedInput;
    private final Map<Character,Character> hanJaMap;

    public HanCharFilter(Reader reader, String pluginPath) {
        super(reader);
        this.hanJaMap = HanjaUtil.loadHanjaDic(pluginPath);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (transformedInput == null) {
            fill();
        }

        return transformedInput.read(cbuf, off, len);
    }

    private void fill() throws IOException {
        StringBuilder buffered = new StringBuilder();
        char [] temp = new char [1024];
        for (int cnt = input.read(temp); cnt > 0; cnt = input.read(temp)) {
            buffered.append(temp, 0, cnt);
        }
        transformedInput = new StringReader(doFiltering(buffered));
    }

    private String doFiltering(StringBuilder buffered) throws UnsupportedEncodingException {
        return HanjaUtil.convertToKor(buffered, hanJaMap);
    }
}