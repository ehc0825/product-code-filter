package kr.co.mayfarm.kkma.ma;


import kr.co.mayfarm.kkma.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MExpression extends ArrayList<MCandidate> implements Comparable<MExpression> {
    String exp;
    float lnprOfSpacing;
    private static final int PRUNE_SIZE = 12;

    MExpression(String exp) {
        this.exp = null;
        this.lnprOfSpacing = 0.0F;
        this.exp = exp;
    }

    public MExpression(String exp, MCandidate mc) throws Exception {
        this(exp);
        this.add(mc);
    }

    MExpression(MCandidate mc) throws Exception {
        this(mc.getExp());
        this.add(mc);
    }

    public boolean add(MCandidate mc) {
        return mc != null && !this.contains(mc) ? super.add(mc) : false;
    }

    public boolean add2(MCandidate mc) {
        return mc != null && !this.contains(mc) ? super.add(mc) : false;
    }

    public String getExp() {
        return this.exp;
    }

    void setIndex(int index) {
        int i = 0;

        for(int size = this.size(); i < size; ++i) {
            ((MCandidate)this.get(i)).setIndex(index);
        }

    }

    public String toString() {
        StringBuffer sb = new StringBuffer(this.exp + Util.LINE_SEPARATOR);
        sb.append(String.format("\t %4s%4s%4s%4s%8s%8s%8s%8s%8s%8s", "rdl", "cdl", "siz", "dic", "spcing", "tging", "lnpr", "spcing!", "tging!", "lnpr!"));
        sb.append(Util.LINE_SEPARATOR);
        int i = 0;

        for(int stop = this.size(); i < stop; ++i) {
            sb.append("\t{" + this.get(i) + "};" + Util.LINE_SEPARATOR);
        }

        return sb.toString();
    }

    public String toSmplStr() {
        StringBuffer sb = new StringBuffer(this.exp + Util.LINE_SEPARATOR);
        int i = 0;

        for(int stop = this.size(); i < stop; ++i) {
            sb.append("\t{" + ((MCandidate)this.get(i)).toSimpleStr() + "};" + Util.LINE_SEPARATOR);
        }

        return sb.toString();
    }

    String getEncStr() {
        StringBuffer sb = new StringBuffer(this.exp + ":");
        int i = 0;

        for(int stop = this.size(); i < stop; ++i) {
            if (i > 0) {
                sb.append(";");
            }

            sb.append(((MCandidate)this.get(i)).getEncStr());
        }

        return sb.toString();
    }

    MExpression derive(MExpression meToAppend) {
        MExpression ret = new MExpression(this.exp + meToAppend.exp);
        MCandidate mcThis = null;
        MCandidate mcToAppend = null;
        int jStop = meToAppend.size();
        int i = 0;

        for(int iStop = this.size(); i < iStop; ++i) {
            mcThis = (MCandidate)this.get(i);

            for(int j = 0; j < jStop; ++j) {
                mcToAppend = (MCandidate)meToAppend.get(j);
                ret.add(mcThis.derive(mcToAppend));
            }
        }

        ret.prune();
        return ret;
    }

    void prune() {
        int size = this.size();
        if (size >= 2) {
            int maxDicLen = -1;
            int expLen = this.exp.length();
            this.sort();
            MCandidate mcBest = (MCandidate)this.get(0);
            float bestLnpr = mcBest.getLnpr();
            boolean uncomplete = mcBest.candDicLen > 0 || expLen > mcBest.getDicLenWithCand();
            if (uncomplete || size >= 12) {
                int pruneIdx = 1;
                int i = this.size();

                while(pruneIdx < i) {
                    MCandidate mcToPrune = (MCandidate)this.get(pruneIdx);
                    int tempDicLen = mcToPrune.getDicLenWithCand();
                    if (uncomplete && mcToPrune.getDicLenOnlyCand() == 0 && pruneIdx < 12) {
                        ++pruneIdx;
                    } else {
                        if (mcToPrune.getLnpr() < bestLnpr - 6.0F || tempDicLen < maxDicLen) {
                            break;
                        }

                        ++pruneIdx;
                    }
                }

                i = pruneIdx;

                for(int stop = this.size(); i < stop && (!uncomplete || i != stop - 1 || ((MCandidate)this.get(pruneIdx)).realDicLen != 0 || ((MCandidate)this.get(pruneIdx)).getExp().length() >= 10); ++i) {
                    this.remove(pruneIdx);
                }

            }
        }
    }

    void pruneWithPrev(MExpression mePrev) throws Exception {
        if (mePrev != null) {
            int thisMESize = this.size();
            int preMESize = mePrev.size();
            if (preMESize != 0) {
                for(int i = 0; i < thisMESize; ++i) {
                    MCandidate mcThis = (MCandidate)this.get(i);
                    mcThis.numOfApndblMC = 0;

                    for(int j = 0; j < preMESize; ++j) {
                        MCandidate preMC = (MCandidate)mePrev.get(j);
                        if (preMC.isApndblWithSpace(mcThis) || preMC.isApndbl(mcThis)) {
                            ++mcThis.numOfApndblMC;
                            break;
                        }
                    }

                    if (mcThis.numOfApndblMC == 0) {
                        this.remove(i);
                        --i;
                        --thisMESize;
                    }
                }

            }
        }
    }

    void pruneWithNext(MExpression nextME) throws Exception {
        int thisMESize = this.size();
        int nextMESize = nextME.size();
        if (nextMESize != 0) {
            for(int i = 0; i < thisMESize; ++i) {
                MCandidate thisMC = (MCandidate)this.get(i);
                thisMC.numOfApndblMC = 0;

                for(int j = 0; j < nextMESize; ++j) {
                    MCandidate nextMC = (MCandidate)nextME.get(j);
                    if (thisMC.isApndblWithSpace(nextMC)) {
                        ++thisMC.numOfApndblMC;
                        break;
                    }
                }

                if (thisMC.numOfApndblMC == 0 && this.size() > 1) {
                    this.remove(i);
                    --i;
                    --thisMESize;
                }
            }

        }
    }

    MExpression[] divideHeadTailAt(String headStr, int headIndex, String tailStr, int tailIndex) throws Exception {
        MExpression[] ret = new MExpression[2];
        MExpression headME = ret[0] = new MExpression(headStr);
        MExpression tailME = ret[1] = new MExpression(tailStr);
        int j = 0;

        for(int stop = this.size(); j < stop; ++j) {
            MCandidate[] mcHeadTail = ((MCandidate)this.get(j)).divideHeadTailAt(headStr, headIndex, tailStr, tailIndex);
            if (mcHeadTail != null) {
                headME.add(mcHeadTail[0]);
                tailME.add(mcHeadTail[1]);
            }
        }

        return ret;
    }

    void merge(MExpression mExp) {
        int i = 0;

        for(int stop = mExp.size(); i < stop; ++i) {
            this.add((MCandidate)mExp.get(i));
        }

        this.prune();
    }

    List<MExpression> split() throws Exception {
        if (this.size() == 0) {
            return null;
        } else {
            ArrayList<MExpression> ret = new ArrayList();
            MCandidate mc = (MCandidate)this.get(0);
            List<MCandidate> splitedMCList = mc.split();
            int splitedMCSize = splitedMCList.size();

            int size;
            for(size = 0; size < splitedMCSize; ++size) {
                ret.add(new MExpression((MCandidate)splitedMCList.get(size)));
            }

            size = this.size();
            if (size > 1) {
                String preExpWithSpace = mc.geExpStrWithSpace();

                for(int i = 1; i < size; ++i) {
                    mc = (MCandidate)this.get(i);
                    String curExpWithSpace = mc.geExpStrWithSpace();
                    if (preExpWithSpace.equals(curExpWithSpace)) {
                        splitedMCList = mc.split();

                        for(int j = 0; j < splitedMCSize; ++j) {
                            ((MExpression)ret.get(j)).add((MCandidate)splitedMCList.get(j));
                        }
                    }
                }
            }

            return ret;
        }
    }

    boolean isOneEojeol() {
        return this.size() > 0 && ((MCandidate)this.get(0)).getSpaceCnt() == 0;
    }

    void sort() {
        Collections.sort(this);
    }

    void sortByLnpr() {
        Collections.sort(this, new Comparator<MCandidate>() {
            public int compare(MCandidate mc1, MCandidate mc2) {
                if (mc1.getLnpr() > mc2.getLnpr()) {
                    return -1;
                } else {
                    return mc1.getLnpr() < mc2.getLnpr() ? 1 : 0;
                }
            }
        });
    }

    void sortByBestLnpr() {
        Collections.sort(this, new Comparator<MCandidate>() {
            public int compare(MCandidate mc1, MCandidate mc2) {
                if (mc1.getBestLnpr() > mc1.getBestLnpr()) {
                    return -1;
                } else {
                    return mc1.getBestLnpr() > mc1.getBestLnpr() ? 1 : 0;
                }
            }
        });
    }

    public int compareTo(MExpression comp) {
        return this.exp.compareTo(comp.exp);
    }

    String getCommonHead() {
        MCandidate mc = (MCandidate)this.get(0);
        int spaceCnt = mc.getSpaceCnt();
        if (spaceCnt < 1) {
            return null;
        } else {
            int size = this.size();

            for(int i = spaceCnt - 1; i >= 0; --i) {
                String maxCommonHead = mc.getExp(i);
                if (this.getExp().length() - maxCommonHead.length() >= 2) {
                    for(int j = 1; j < size; ++j) {
                        if (((MCandidate)this.get(j)).getHead(maxCommonHead) == null) {
                            maxCommonHead = null;
                            break;
                        }
                    }

                    if (maxCommonHead != null && maxCommonHead.length() > 1) {
                        if (!mc.isUNBfrOrAftrIthSpace(i)) {
                            return maxCommonHead;
                        }

                        maxCommonHead = null;
                    }
                }
            }

            return null;
        }
    }

    boolean isComplete() throws Exception {
        return this.size() > 0 && ((MCandidate)this.get(0)).isComplete();
    }

    boolean isOneEojeolCheckable() {
        if (this.size() == 1) {
            MCandidate mc = (MCandidate)this.get(0);
            Morpheme mp = mc.firstMorp;
            if (mc.size() == 1 && (mp.isCharSetOf(CharSetType.NUMBER) || mp.isCharSetOf(CharSetType.ENGLISH) || mp.isCharSetOf(CharSetType.COMBINED))) {
                return true;
            }
        }

        return false;
    }

    public MExpression copy() {
        MExpression copy = new MExpression(this.exp);
        int i = 0;

        for(int stop = this.size(); i < stop; ++i) {
            copy.add(((MCandidate)this.get(i)).copy());
        }

        return copy;
    }

    public void leaveJustBest() {
        for(int i = this.size() - 1; i > 0; --i) {
            this.remove(i);
        }

    }

    public void setBestPrevMC(MExpression mePrev) {
        int i = 0;

        for(int size = this.size(); i < size; ++i) {
            MCandidate mcCur = (MCandidate)this.get(i);
            if (mePrev == null) {
                mcCur.setBestPrevMC((MCandidate)null);
            } else {
                int j = 0;

                for(int jSize = mePrev.size(); j < jSize; ++j) {
                    MCandidate mcPrev = (MCandidate)mePrev.get(j);
                    mcCur.setBestPrevMC(mcPrev);
                }
            }
        }

    }

    public boolean isNotHangul() {
        return this.size() == 1 && ((MCandidate)this.get(0)).isNotHangul();
    }

    public MCandidate getBest() {
        return (MCandidate)this.get(0);
    }

    public float getLnprOfSpacing() {
        return this.lnprOfSpacing;
    }

    public void setLnprOfSpacing(float lnprOfSpacing) {
        this.lnprOfSpacing = lnprOfSpacing;
    }
}
