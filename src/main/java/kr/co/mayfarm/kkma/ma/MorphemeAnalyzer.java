package kr.co.mayfarm.kkma.ma;

import kr.co.mayfarm.kkma.constants.Condition;
import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.dic.Dictionary;
import kr.co.mayfarm.kkma.util.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MorphemeAnalyzer {
    protected Dictionary dic = null;
    PrintWriter logger = null;
    boolean doLogging = false;
    public static final boolean DEBUG = "DO_DEBUG".equals(System.getProperty("DO_DEBUG"));
    public MorphemeAnalyzer() {
        this.dic = new Dictionary();
    }

    public MorphemeAnalyzer(List<String> userDicPath) {
        this.dic = new Dictionary(userDicPath);
    }

    public List<MExpression> analyze(String string) throws Exception {
        if (!Util.valid(string)) {
            return null;
        } else {
            string = string.trim();
            ArrayList<MExpression> ret = new ArrayList();
            List<Token> tokenList = Tokenizer.tokenize(string);
            MExpression mePrev = null;
            MExpression meCur = null;
            int i = 0;

            for(int stop = tokenList.size(); i < stop; ++i) {
                Token token = (Token)tokenList.get(i);
                if (!token.isCharSetOf(CharSetType.SPACE)) {
                    List<MExpression> meList = this.analyze(mePrev, token);
                    int j = 0;

                    for(int jStop = meList.size(); j < jStop; ++j) {
                        meCur = (MExpression)meList.get(j);
                        if (mePrev != null) {
                            mePrev.pruneWithNext(meCur);
                        }

                        ret.add(meCur);
                        mePrev = meCur;
                    }
                }
            }

            return ret;
        }
    }

    private List<MExpression> analyze(MExpression mePrev, Token token) throws Exception {
        List<MExpression> ret = new ArrayList();
        if (!token.isCharSetOf(CharSetType.HANGUL)) {
            ret.add(new MExpression(token.string, new MCandidate(token)));
            return ret;
        } else {
            String string = token.string;
            int strlen = string.length();
            MExpression meHeadTemp = null;
            MExpression meTailTemp = null;
            MExpression meNew = null;
            MExpression[] meArr = new MExpression[strlen];
            String substr = null;
            String tail = null;
            int tailCutPos = 1;

            int stop;
            for(int firstOffset = token.index; tailCutPos <= strlen; ++tailCutPos) {
                substr = string.substring(0, tailCutPos);
                MExpression meCur = meArr[tailCutPos - 1] = this.getMExpression(substr, firstOffset);
                meCur.pruneWithPrev(mePrev);

                for(stop = 1; stop < tailCutPos; ++stop) {
                    this.writeLog("==========================================[" + substr + "]");
                    tail = substr.substring(stop, tailCutPos);
                    meTailTemp = this.getMExpression(tail, firstOffset + stop);
                    meHeadTemp = meArr[stop - 1];
                    meNew = meHeadTemp.derive(meTailTemp);
                    meNew.pruneWithPrev(mePrev);
                    this.writeLog("[     HEAD ] " + meHeadTemp);
                    this.writeLog("[     TAIL ] " + meTailTemp);
                    this.writeLog("[GENERATED ] " + meNew);
                    this.writeLog("[   STORED ] " + meCur);
                    meCur.merge(meNew);
                    this.writeLog("[   MERGED ] " + meCur);
                    this.writeLog("==================================================");
                }

                if (tailCutPos != strlen && (meCur.isComplete() || tailCutPos >= 11) && strlen > 5 && meCur.size() > 3 && tailCutPos > 4) {
                    String strHead = meCur.getCommonHead();
                    if (strHead != null) {
                        this.writeLog("[COMMON HEAD]==============" + strHead);
                        int headLen = strHead.length();
                        String tailStr = meCur.getExp().substring(headLen);
                        MExpression[] meHeadTail = meCur.divideHeadTailAt(strHead, firstOffset, tailStr, firstOffset + headLen);
                        MExpression headME = meHeadTail[0];
                        ret.add(headME);
                        mePrev = headME;
                        this.writeLog(ret);
                        MExpression[] newExps = new MExpression[tailCutPos - headLen];
                        int j = headLen;

                        for(stop = 0; j < tailCutPos; ++stop) {
                            meHeadTail = meArr[j].divideHeadTailAt(strHead, token.index, tailStr.substring(0, stop + 1), token.index + j);
                            newExps[stop] = meHeadTail[1];
                            ++j;
                        }

                        string = string.substring(strHead.length());
                        strlen = string.length();
                        meArr = new MExpression[strlen];
                        j = 0;

                        for(stop = newExps.length; j < stop; ++j) {
                            meArr[j] = newExps[j];
                        }

                        tailCutPos = tailStr.length();
                        firstOffset += strHead.length();
                    }
                }
            }

            if (tailCutPos > 1) {
                ret.add(meArr[meArr.length - 1]);
            }

            int i = 0;

            for(stop = ret.size(); i < stop; ++i) {
                MExpression me = (MExpression)ret.get(i);
                if (me.size() == 0 || ((MCandidate)me.get(0)).getDicLenOnlyReal() == 0) {
                    me.add(new MCandidate(me.exp, token.index));
                }
            }

            return ret;
        }
    }

    private MExpression getMExpression(String string, int index) throws Exception {
        MExpression ret = this.dic.getMExpression(string);
        if (ret == null) {
            MCandidate mc = new MCandidate(string, index);
            ret = new MExpression(string, mc);
            ret.setLnprOfSpacing(mc.lnprOfSpacing);
        } else {
            ret.setIndex(index);
        }

        return ret;
    }

    public List<MExpression> postProcess(List<MExpression> melAnalResult) throws Exception {
        MExpression me1 = null;
        MExpression me2 = null;
        MExpression me3 = null;

        int i;
        MCandidate mc2;
        for(i = 1; i < melAnalResult.size(); ++i) {
            me1 = (MExpression)melAnalResult.get(i - 1);
            me2 = (MExpression)melAnalResult.get(i);
            if (!me2.isComplete() || me1.isOneEojeolCheckable() || me2.isOneEojeolCheckable()) {
                MCandidate mc1;
                if (me1.isNotHangul() && me2.isNotHangul()) {
                    mc1 = (MCandidate)me1.get(0);
                    mc2 = (MCandidate)me2.get(0);
                    if (mc1.firstMorp.index + mc1.getExp().length() == mc2.firstMorp.index) {
                        me1.exp = me1.exp + me2.exp;
                        mc1.addAll(mc2);
                        mc1.setExp(me1.exp);
                        melAnalResult.remove(i);
                        --i;
                    }
                } else if (me1.isNotHangul()) {
                    mc1 = (MCandidate)me1.get(0);
                    mc2 = (MCandidate)me2.get(0);
                    if (mc1.firstMorp.index + mc1.getExp().length() == mc2.firstMorp.index) {
                        me3 = me1.derive(me2);
                        melAnalResult.remove(i - 1);
                        melAnalResult.remove(i - 1);
                        melAnalResult.add(i - 1, me3);
                        --i;
                    }
                } else {
                    me3 = me1.derive(me2);
                    if (me3.isOneEojeol()) {
                        melAnalResult.remove(i - 1);
                        melAnalResult.remove(i - 1);
                        melAnalResult.add(i - 1, me3);
                        --i;
                    }
                }
            }
        }

        me1 = (MExpression)melAnalResult.get(0);
        i = 0;

        int size;
        for(boolean var8 = false; i < (size = me1.size()) && size > 1; ++i) {
            mc2 = (MCandidate)me1.get(i);
            if (mc2.cclEnc != Condition.ENG && mc2.cclEnc != 0L || mc2.firstMorp.isTagOf(POSTag.J | POSTag.E | POSTag.XS)) {
                me1.remove(i);
                --i;
            }
        }

        this.setBestPrevMC(melAnalResult);
        return melAnalResult;
    }

    private void setBestPrevMC(List<MExpression> meList) {
        MExpression mePrev = null;
        MExpression meCurr = null;
        int idx = 0;

        for(int size = meList.size(); idx < size; ++idx) {
            meCurr = (MExpression)meList.get(idx);
            meCurr.setBestPrevMC(mePrev);
            mePrev = meCurr;
        }

        idx = meList.size() - 1;
        MExpression me = (MExpression)meList.get(idx);
        me.sortByBestLnpr();
        MCandidate mc = (MCandidate)me.get(0);
        --idx;

        while(mc != null && idx >= 0) {
            mc = mc.prevBestMC;
            me = (MExpression)meList.get(idx);
            me.remove(mc);
            me.add(0, mc);
            --idx;
        }

    }

    public List<MExpression> leaveJustBest(List<MExpression> meList) throws Exception {
        for(int i = 0; i < meList.size(); ++i) {
            MExpression me = (MExpression)meList.get(i);
            MCandidate mc = (MCandidate)me.get(0);
            me.clear();
            me.add(mc);
        }

        List<MExpression> tempMEList = new ArrayList();
        Iterator var6 = meList.iterator();

        while(var6.hasNext()) {
            MExpression me = (MExpression)var6.next();
            tempMEList.addAll(me.split());
        }

        meList.clear();
        meList.addAll(tempMEList);
        return meList;
    }

    public List<Sentence> divideToSentences(List<MExpression> melAnalResult) throws Exception {
        List<Sentence> ret = new ArrayList();
        MExpression me1 = null;
        Eojeol eojeol = null;
        Eojeol prevEojeol = null;
        Sentence sentence = null;

        for(int i = 0; i < melAnalResult.size(); ++i) {
            if (sentence == null) {
                sentence = new Sentence();
            }

            me1 = (MExpression)melAnalResult.get(i);
            if (prevEojeol != null && ((MCandidate)me1.get(0)).isTagOf(POSTag.S) && prevEojeol.getStartIndex() + prevEojeol.exp.length() == ((MCandidate)me1.get(0)).firstMorp.index) {
                eojeol.add(((MCandidate)me1.get(0)).firstMorp);
                eojeol.exp = eojeol.exp + me1.exp;
            } else {
                eojeol = new Eojeol(me1);
                sentence.add(eojeol);
                prevEojeol = eojeol;
            }

            if (eojeol.isEnding()) {
                if (i < melAnalResult.size() - 1) {
                    for(; i < melAnalResult.size() - 1; ++i) {
                        me1 = (MExpression)melAnalResult.get(i + 1);
                        if (!me1.getExp().startsWith(".") && !me1.getExp().startsWith(",") && !me1.getExp().startsWith("!") && !me1.getExp().startsWith("?") && !me1.getExp().startsWith(";") && !me1.getExp().startsWith("~") && !me1.getExp().startsWith(")") && !me1.getExp().startsWith("]") && !me1.getExp().startsWith("}")) {
                            break;
                        }

                        if (eojeol.firstMorp.index + eojeol.exp.length() == ((MCandidate)me1.get(0)).firstMorp.index) {
                            eojeol.add(((MCandidate)me1.get(0)).firstMorp);
                            eojeol.exp = eojeol.exp + me1.exp;
                        } else {
                            sentence.add(new Eojeol(me1));
                        }
                    }
                }

                ret.add(sentence);
                sentence = null;
                prevEojeol = null;
            }
        }

        if (sentence != null && sentence.size() > 0) {
            ret.add(sentence);
        }

        return ret;
    }

    public void createLogger(String fileName) {
        try {
            System.out.println("DO LOGGING!!");
            if (fileName == null) {
                this.logger = new PrintWriter(System.out, true);
            } else {
                this.logger = new PrintWriter(new FileWriter(fileName), true);
            }

            this.doLogging = true;
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public void closeLogger() {
        if (this.doLogging && this.logger != null) {
            this.logger.close();
        }

        this.doLogging = false;
    }

    private void writeLog(Object obj) {
        if (DEBUG) {
            if (this.logger != null) {
                this.logger.println(obj);
            } else {
                System.out.println(obj);
            }
        }

    }
}
