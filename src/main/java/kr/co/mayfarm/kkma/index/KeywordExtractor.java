package kr.co.mayfarm.kkma.index;

import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.constants.Path;

import kr.co.mayfarm.kkma.ma.*;
import kr.co.mayfarm.kkma.util.StringSet;
import kr.co.mayfarm.kkma.util.Util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KeywordExtractor extends MorphemeAnalyzer {
    static WordDic UOMDic = null;
    static WordDic ChemFormulaDic = null;
    static WordDic CompNounDic = null;
    static WordDic VerbNounDic = null;
    static WordDic JunkWordDic = null;
    static WordDic VerbJunkWordDic = null;
    static final int MAX_UOM_SIZE = 7;
    public static final StringSet MULTIPLYERS;
    public static final StringSet RANGE_INDICATOR;
    public static final String STD_UOM_CONNECTOR = "*";
    public KeywordExtractor() {
    }

    public KeywordList extractKeyword(JProgressBar progressBar, JLabel label, String string, boolean onlyNoun) {
        KeywordList ret = null;
        String line = null;
        int offset = 0;
        String[] strArr = string.split("\n");
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(strArr.length);
            progressBar.setStringPainted(true);
            label.setText("0");
        }

        int lineNo = 0;

        for(int len = strArr.length; lineNo < len; ++lineNo) {
            line = strArr[lineNo];
            if (Util.valid(line)) {
                KeywordList keywordList = this.extractKeyword(line, onlyNoun);
                if (offset > 0) {
                    int i = 0;

                    for(int size = keywordList.size(); i < size; ++i) {
                        Keyword keyword = (Keyword)keywordList.get(i);
                        keyword.setIndex(offset + keyword.getIndex());
                    }
                }

                if (keywordList != null && keywordList.size() > 0) {
                    if (ret == null) {
                        ret = new KeywordList(keywordList);
                    } else {
                        ret.addAll(keywordList);
                    }
                }
            }

            if (progressBar != null) {
                progressBar.setValue(lineNo + 1);
                label.setText(lineNo + 1 + "");
            }

            offset += line.length() + 1;
        }

        if (progressBar != null) {
            progressBar.setStringPainted(false);
        }

        return ret;
    }

    public KeywordList extractKeyword(String string, boolean onlyNoun) {
        ArrayList ret = new ArrayList();

        try {
            List<MExpression> meList = this.leaveJustBest(this.postProcess(this.analyze(string)));
            Morpheme mp = null;
            MCandidate mc = null;
            MExpression me = null;
            Keyword keyword = null;
            List<Morpheme> mpList = new ArrayList();
            int endIdx = 0;

            int startIdx;
            int tempLen;
            for(startIdx = meList == null ? 0 : meList.size(); endIdx < startIdx; ++endIdx) {
                me = (MExpression)meList.get(endIdx);
                mc = (MCandidate)me.get(0);
                int jSize = mc.size();
                if (jSize == 1) {
                    mp = (Morpheme)mc.get(0);
                    mp.setString(me.getExp());
                    mpList.add(mp);
                } else {
                    for(tempLen = 0; tempLen < jSize; ++tempLen) {
                        mpList.add(mc.get(tempLen));
                    }
                }
            }

            String tempName;
            for(endIdx = mpList.size() - 1; endIdx > 0; --endIdx) {
                for(startIdx = Math.max(endIdx - 7, 0); startIdx < endIdx; ++startIdx) {
                    tempName = "";

                    for(tempLen = startIdx; tempLen <= endIdx; ++tempLen) {
                        tempName = tempName + ((Morpheme)mpList.get(tempLen)).getString();
                    }

                    if (UOMDic.contains(tempName)) {
                        while(startIdx < endIdx) {
                            mpList.remove(startIdx + 1);
                            --endIdx;
                        }

                        mp = (Morpheme)mpList.get(startIdx);
                        mp.setString(tempName);
                        mp.setCharSet(CharSetType.COMBINED);
                        mp.setTag(POSTag.NNM);
                    } else if (ChemFormulaDic.contains(tempName)) {
                        while(startIdx < endIdx) {
                            mpList.remove(startIdx + 1);
                            --endIdx;
                        }

                        mp = (Morpheme)mpList.get(startIdx);
                        mp.setString(tempName);
                        mp.setCharSet(CharSetType.COMBINED);
                        mp.setTag(POSTag.UN);
                    } else if (CompNounDic.contains(tempName)) {
                        while(startIdx < endIdx) {
                            mpList.remove(startIdx + 1);
                            --endIdx;
                        }

                        if (!JunkWordDic.contains(tempName)) {
                            mp = (Morpheme)mpList.get(startIdx);
                            mp.setString(tempName);
                            mp.setCharSet(CharSetType.COMBINED);
                            mp.setTag(POSTag.NNG);
                            mp.setComposed(true);
                        }
                    }
                }
            }

            endIdx = 0;

            for(startIdx = mpList.size(); endIdx < startIdx; ++endIdx) {
                mp = (Morpheme)mpList.get(endIdx);
                mp.setString(mp.getString().toLowerCase());
                if ((!onlyNoun || mp.isTagOf(POSTag.N)) && !JunkWordDic.contains(mp.getString())) {
                    if (mp.isTagOf(POSTag.UN) && mp.getCharSet() == CharSetType.ENGLISH) {
                        keyword = new Keyword(mp);
                        keyword.setString(keyword.getString().toLowerCase());
                        ret.add(keyword);
                    } else if (!mp.isTagOf(POSTag.V)) {
                        if (mp.isTagOf(POSTag.NP)) {
                        }

                        keyword = new Keyword(mp);
                        ret.add(keyword);
                    } else {
                        tempName = mp.getString();
                        tempLen = tempName.length();
                        char ch = tempName.charAt(tempLen - 1);
                        if (tempLen > 2 && (ch == '하' || ch == '되') && VerbNounDic.contains(tempName = tempName.substring(0, tempLen - 1))) {
                            keyword = new Keyword(mp);
                            keyword.setString(tempName);
                            keyword.setTag(POSTag.NNG);
                            ret.add(keyword);
                        } else {
                            keyword = new Keyword(mp);
                            ret.add(keyword);
                        }
                    }
                }
            }

            Morpheme mp0 = null;
            Morpheme mp1 = null;
            tempName = null;
            Morpheme mp3 = null;
            int i = 0;
            int size = mpList.size();

            for(boolean var16 = false; i < size; ++i) {
                mp0 = (Morpheme)mpList.get(i);
                i = 0;
                if (i + 1 < size && mp0.isTagOf(POSTag.NN) && (mp1 = (Morpheme)mpList.get(i + 1)).isTagOf(POSTag.NN) && mp0.getIndex() + mp0.getString().length() == mp1.getIndex()) {
                    Morpheme mp2;
                    if (i + 2 < size && (mp2 = (Morpheme)mpList.get(i + 2)).isTagOf(POSTag.NN) && mp1.getIndex() + mp1.getString().length() == mp2.getIndex()) {
                        if (i + 3 < size && (mp3 = (Morpheme)mpList.get(i + 3)).isTagOf(POSTag.NN) && mp2.getIndex() + mp2.getString().length() == mp3.getIndex()) {
                            keyword = new Keyword(mp0);
                            keyword.setComposed(true);
                            keyword.setString(mp0.getString() + mp1.getString() + mp2.getString() + mp3.getString());
                            ret.add(keyword);
                            ++i;
                        } else {
                            keyword = new Keyword(mp0);
                            keyword.setComposed(true);
                            keyword.setString(mp0.getString() + mp1.getString() + mp2.getString());
                            ret.add(keyword);
                        }

                        ++i;
                    } else {
                        keyword = new Keyword(mp0);
                        keyword.setComposed(true);
                        keyword.setString(mp0.getString() + mp1.getString());
                        ret.add(keyword);
                    }

                    ++i;
                }

                i += i;
            }

            for(i = 0; i < ret.size(); ++i) {
                keyword = (Keyword)ret.get(i);
                if (keyword.isTagOf(POSTag.XP | POSTag.XS | POSTag.VX) || JunkWordDic.contains(mp.getString())) {
                    ret.remove(i);
                    --i;
                }
            }

            List<Keyword> cnKeywordList = new ArrayList();
            String[] cnKeywords = null;
            i = 0;

            for(size = ret.size(); i < size; ++i) {
                Keyword k = (Keyword)ret.get(i);
                if (k.isComposed() && (cnKeywords = this.dic.getCompNoun(k.getString())) != null) {
                    int addIdx = 0;
                    int j = 0;

                    for(int len = cnKeywords.length; j < len; ++j) {
                        if (!JunkWordDic.contains(cnKeywords[j])) {
                            Keyword newKeyword = new Keyword(k);
                            newKeyword.setVocTag("E");
                            newKeyword.setString(cnKeywords[j]);
                            newKeyword.setComposed(false);
                            newKeyword.setIndex(k.getIndex() + addIdx);
                            addIdx += newKeyword.getString().length();
                            cnKeywordList.add(newKeyword);
                        }
                    }
                }
            }

            ret.addAll(cnKeywordList);
            Collections.sort(ret, new Comparator<Keyword>() {
                public int compare(Keyword o1, Keyword o2) {
                    return o1.getIndex() == o2.getIndex() ? o1.getString().length() - o2.getString().length() : o1.getIndex() - o2.getIndex();
                }
            });
        } catch (Exception var23) {
            System.err.println(string);
            var23.printStackTrace();
        }

        return new KeywordList(ret);
    }

    public KeywordList removeJunkWord(KeywordList keywordList) {
        int i = 0;

        for(int size = keywordList == null ? 0 : keywordList.size(); i < size; ++i) {
        }

        return keywordList;
    }

    public Keyword getCompositeNoun(MCandidate mc) {
        Keyword ret = null;
        if (mc != null && mc.size() >= 2) {
            int nnCnt = 0;

            for(int i = 0; i < mc.size(); ++i) {
                Morpheme mp = (Morpheme)mc.get(i);
                if (mp.isTagOf(POSTag.NN)) {
                    if (ret == null) {
                        ret = new Keyword(mp);
                        ret.setComposed(true);
                        ++nnCnt;
                    } else {
                        if (nnCnt == 0) {
                            return null;
                        }

                        ret.setString(ret.getString() + mp.getString());
                        ++nnCnt;
                    }
                } else {
                    if (ret != null && nnCnt > 1) {
                        return ret;
                    }

                    nnCnt = 0;
                }
            }

            if (nnCnt == 0) {
                return null;
            } else {
                return ret;
            }
        } else {
            return null;
        }
    }

    public static String getFormatedUOMValues(String inputString) {
        String resultString = "";
        List<Token> list = Tokenizer.tokenize(inputString);
        Token token = null;

        for(int i = 0; i < list.size(); ++i) {
            token = (Token)list.get(i);
            if (token.isCharSetOf(CharSetType.NUMBER)) {
                resultString = resultString + token.getString();
            } else if (isUOMConnector(token.getString())) {
                resultString = resultString + "*";
            } else if (!token.getString().equals(" ") && !token.getString().equals("\t")) {
                resultString = resultString + token.getString();
            }
        }

        return resultString;
    }

    private static boolean isUOMConnector(String uomCon) {
        return MULTIPLYERS.contains(uomCon);
    }

    private static boolean isUOMConnector2(String uomCon) {
        return MULTIPLYERS.contains(uomCon) || RANGE_INDICATOR.contains(uomCon);
    }


    static {
        UOMDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/UOM.dic");
        ChemFormulaDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/ChemFormula.dic");
        CompNounDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/CompNoun.dic");
        VerbNounDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/VerbNoun.dic");
        JunkWordDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/JunkWord.dic");
        VerbJunkWordDic = new WordDic(Path.PLUGIN_PATH+"/dic/ecat/VerbJunkWord.dic");
        MULTIPLYERS = new StringSet(new String[]{"*", "x", "X", "×", "Ⅹ"});
        RANGE_INDICATOR = new StringSet(new String[]{"-", "±", "~", "+"});
    }
}
