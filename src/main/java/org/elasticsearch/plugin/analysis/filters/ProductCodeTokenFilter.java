package org.elasticsearch.plugin.analysis.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.plugin.analysis.util.ProductCodeParseUtil;

import java.io.IOException;

public class ProductCodeTokenFilter extends TokenFilter {


    private final CharTermAttribute charAttr = this.addAttribute(CharTermAttribute.class);

    private final ProductCodeParseUtil productParseUtil= new ProductCodeParseUtil();


    protected ProductCodeTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(this.input.incrementToken())
        {
            String productCode =this.productParseUtil.productCode(this.charAttr.toString());
            this.charAttr.setEmpty().append(productCode);
            return true;
        }
        else
        {
            return false;
        }
    }
}
