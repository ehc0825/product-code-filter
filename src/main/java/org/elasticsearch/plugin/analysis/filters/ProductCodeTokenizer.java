package org.elasticsearch.plugin.analysis.filters;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.plugin.analysis.util.CodeAnalyzerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductCodeTokenizer extends Tokenizer {

    private final CharTermAttribute charAttr = this.addAttribute(CharTermAttribute.class);
    private final CodeAnalyzerUtil codeAnalyzerUtil = new CodeAnalyzerUtil();

    protected List<String> tokens = new ArrayList<>();

    protected String stringToTokenize;

    protected int position = 0;

    public ProductCodeTokenizer()
    {
        super(DEFAULT_TOKEN_ATTRIBUTE_FACTORY);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(position >= tokens.size())
        {
            position = 0;
            return false;
        }
        else {
            String token = tokens.get(position);
            charAttr.setEmpty().append(token);
            charAttr.setLength(token.length());
            position++;
            return true;
        }
    }
}
