package kr.co.mayfarm.kkma.ma;


import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.util.Util;

public class Eojeol extends MorphemeList {
    String exp;

    private Eojeol() {
        this.exp = null;
    }

    public Eojeol(MCandidate mc) {
        this.exp = null;
        this.exp = mc.getExp();
        this.addAll(mc);
        this.firstMorp = mc.firstMorp;
        this.lastMorp = mc.lastMorp;
    }

    public Eojeol(MExpression me) {
        this((MCandidate)me.get(0));
    }

    public String getExp() {
        return this.exp;
    }

    public boolean isLastMorpOf(String morp, long tag) {
        int i;
        Morpheme mp;
        if (this.lastMorp.isTag(POSTag.JX)) {
            for(i = this.size() - 1; i > 0; --i) {
                mp = (Morpheme)this.get(i);
                if (!mp.isTag(POSTag.JX)) {
                    if (mp.isTagOf(POSTag.J | POSTag.EM)) {
                        return mp.isMorpOf(morp, tag);
                    }

                    return this.lastMorp.isMorpOf(morp, tag);
                }
            }
        } else if (this.lastMorp.isTagOf(POSTag.S)) {
            for(i = this.size() - 1; i > 0; --i) {
                mp = (Morpheme)this.get(i);
                if (!mp.isTag(POSTag.S)) {
                    return mp.isMorpOf(morp, tag);
                }
            }
        }

        return this.lastMorp.isMorpOf(morp, tag);
    }

    public boolean containsTagOf(long tag) {
        int i = 0;

        for(int size = this.size(); i < size; ++i) {
            Morpheme mp = (Morpheme)this.get(i);
            if (mp.isTagOf(tag)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEnding() {
        return this.lastMorp.isTagOf(POSTag.EF);
    }

    Eojeol removeIncorrectlyCombinedEojeol() {
        if (this.size() < 2) {
            return null;
        } else {
            Morpheme mp1 = (Morpheme)this.get(0);
            Morpheme mp2 = (Morpheme)this.get(1);
            if (mp1.isTagOf(POSTag.MA) && mp2.isTagOf(POSTag.VV | POSTag.VC | POSTag.VX) || mp1.isTagOf(POSTag.MD) && mp2.isTagOf(POSTag.N)) {
                Eojeol ej = new Eojeol();
                ej.exp = mp1.string;
                ej.add(mp1);
                this.exp = this.exp.substring(ej.exp.length());
                this.remove(mp1);
                return ej;
            } else {
                for(int i = 1; i < this.size(); ++i) {
                    mp1 = (Morpheme)this.get(i - 1);
                    mp2 = (Morpheme)this.get(i);
                    if (mp1.isTag(POSTag.ECS) && mp2.isTagOf(POSTag.VP)) {
                        Eojeol ej = new Eojeol();
                        int idx = 0;

                        for(int j = 0; j < i; ++j) {
                            if (j == 0) {
                                idx = ((Morpheme)this.get(0)).index;
                            }

                            ej.add((Morpheme)this.get(0));
                            this.remove(0);
                        }

                        ej.exp = this.exp.substring(0, mp2.index - idx);
                        this.exp = this.exp.substring(mp2.index - idx);
                        return ej;
                    }
                }

                return null;
            }
        }
    }

    public String toString() {
        return Util.getTabbedString(this.getExp(), 4, 16) + "=> [" + super.toString() + "]";
    }
}
