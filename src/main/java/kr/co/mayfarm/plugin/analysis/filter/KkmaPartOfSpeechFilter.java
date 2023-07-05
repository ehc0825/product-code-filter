package kr.co.mayfarm.plugin.analysis.filter;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import kr.co.mayfarm.plugin.analysis.enums.PosType;

import java.util.Set;

public class KkmaPartOfSpeechFilter extends FilteringTokenFilter {
    public static final String NAME = "kkma_pos_filter";



    private final Set<PosType> stopTagSet;

    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    public KkmaPartOfSpeechFilter(TokenStream tokenStream, Set<PosType> stopTagSet) {
        super(tokenStream);
        this.stopTagSet = stopTagSet;
    }

    @Override
    protected boolean accept() {
        final PosType posType = PosType.find(typeAttribute.type());
        return !stopTagSet.contains(posType);
    }



}
