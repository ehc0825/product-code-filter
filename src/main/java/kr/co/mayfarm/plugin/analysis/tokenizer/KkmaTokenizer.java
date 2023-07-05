package kr.co.mayfarm.plugin.analysis.tokenizer;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import kr.co.mayfarm.kkma.ma.*;

import java.io.IOException;

import java.util.*;
public class KkmaTokenizer extends Tokenizer {

    public static final String NAME = "kkma_tokenizer";
    private static final Set<String> consonantSet = new HashSet<>();

    private List<Token> tokens;

    private int idx;

    private final MorphemeAnalyzer morphemeAnalyzer;
    private final CharTermAttribute charTermAttribute;
    private final OffsetAttribute offsetAttribute;
    private final TypeAttribute typeAttribute;


    public KkmaTokenizer(MorphemeAnalyzer morphemeAnalyzer) {
        this.morphemeAnalyzer = morphemeAnalyzer;
        this.typeAttribute = addAttribute(TypeAttribute.class);
        this.charTermAttribute = addAttribute(CharTermAttribute.class);
        this.offsetAttribute = addAttribute(OffsetAttribute.class);
        idx = 0;
        tokens = null;
    }
    static {
        consonantSet.add("ㄱ");
        consonantSet.add("ㄲ");
        consonantSet.add("ㄴ");
        consonantSet.add("ㄷ");
        consonantSet.add("ㄸ");
        consonantSet.add("ㄹ");
        consonantSet.add("ㅁ");
        consonantSet.add("ㅂ");
        consonantSet.add("ㅃ");
        consonantSet.add("ㅅ");
        consonantSet.add("ㅆ");
        consonantSet.add("ㅇ");
        consonantSet.add("ㅈ");
        consonantSet.add("ㅉ");
        consonantSet.add("ㅊ");
        consonantSet.add("ㅋ");
        consonantSet.add("ㅌ");
        consonantSet.add("ㅍ");
        consonantSet.add("ㅎ");
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();
        // read document & tagging
        if (tokens == null) {
            try {
                tokens = doTokenize(getDocument());
            } catch (Exception e) {
                e.printStackTrace();
            }
            idx = 0;
        }

        if (tokens != null && tokens.size() > idx) {
            Token token = tokens.get(idx);
            typeAttribute.setType(token.getMorph());
            charTermAttribute.append(token.getStr());
            offsetAttribute.setOffset(token.getPosition(), token.getPosition() + token.getStr().length());
            idx++;
            return true;
        }

        initValues();
        return false;
    }


    @Override
    public final void reset() throws IOException {
        super.reset();
        initValues();
    }

    private void initValues() {
        tokens = null;
    }

    private String getDocument() throws IOException {
        StringBuilder doc = new StringBuilder();
        char[] tmp = new char[1024];
        int len;
        while ((len = input.read(tmp)) != -1) {
            doc.append(new String(tmp, 0, len));
        }
        return doc.toString();
    }

    private List<Token> doTokenize(String document) throws Exception {
        List<MExpression> mExpressionList = morphemeAnalyzer.analyze(document);
        if(null != mExpressionList) {
            mExpressionList = morphemeAnalyzer.postProcess(mExpressionList);
            mExpressionList = morphemeAnalyzer.leaveJustBest(mExpressionList);

            List<Sentence> sentenceList = morphemeAnalyzer.divideToSentences(mExpressionList);
            List<Token> tempToken = new ArrayList<>();
            for (int i = 0; i < sentenceList.size(); i++) {
                Sentence sentence = sentenceList.get(i);
                for (int j = 0; j < sentence.size(); j++) {
                    int lastIndex = 0;
                    if(!tempToken.isEmpty()) {
                        Token token = tempToken.get(tempToken.size()-1);
                        lastIndex = token.getPosition() + token.getStr().length();
                    }
                    tempToken.addAll(parseSentence(sentence.get(j),lastIndex-1));
                }
            }
            return tempToken;
        }
        else {
            return null;
        }
    }

    private List<Token> parseSentence(Eojeol eojeol, int lastIndex) {
        List<Token> tokenList = new ArrayList<>();
        boolean indexFIx = false;
        int startIndex = eojeol.getStartIndex();
        if(eojeol.getStartIndex() < lastIndex) {
            startIndex = lastIndex;
            indexFIx = true;
        }
        for(Morpheme morpheme : eojeol) {
            Token token = new Token();
            List<String> stringList = Arrays.asList(morpheme.toString().split("/"));
            token.setStr(stringList.get(1));
            token.setMorph(stringList.get(2));
            tokenList.add(token);
            if(indexFIx) {
                token.setPosition(startIndex);
                startIndex += token.getStr().length();
            }
            else {
                token.setPosition(morpheme.getIndex());
            }
            if(token.getMorph().equals("ETD") && consonantSet.contains(token.getStr())){
                token.setPosition(token.getPosition()-1);
            }
        }
        return tokenList;
    }

}
