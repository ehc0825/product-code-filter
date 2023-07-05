package kr.co.mayfarm.kkma.ma;


import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.constants.Symbol;
import kr.co.mayfarm.kkma.util.Util;

import java.util.ArrayList;

public class Morpheme  extends Token {
    protected long infoEnc = 0L;
    ArrayList<String> compNounList = null;

    protected Morpheme() {
    }

    public Morpheme(String string, int index) {
        this.index = index;
        this.string = string;
        this.charSet = CharSetType.HANGUL;
        this.infoEnc = POSTag.UN;
    }

    public Morpheme(String string, long tagNum) {
        this.string = string;
        this.charSet = CharSetType.HANGUL;
        this.infoEnc = tagNum;
    }

    public Morpheme(String string, String tag, String compType) {
        this.string = string;
        this.charSet = CharSetType.HANGUL;
        this.infoEnc = POSTag.getTagNum(tag);
        this.setComposed(compType);
    }

    public Morpheme(Token token) {
        this.index = token.index;
        this.string = token.string;
        this.charSet = token.charSet;
        if (token.isCharSetOf(CharSetType.HANGUL)) {
            this.infoEnc = POSTag.UN;
        } else if (token.isCharSetOf(CharSetType.NUMBER)) {
            this.infoEnc = POSTag.NR;
        } else if (!token.isCharSetOf(CharSetType.ENGLISH) && !token.isCharSetOf(CharSetType.COMBINED)) {
            if (token.isCharSetOf(CharSetType.HANMUN)) {
                this.infoEnc = POSTag.OH;
            } else if (token.isCharSetOf(CharSetType.EMOTICON)) {
                this.infoEnc = POSTag.EMO;
            } else {
                this.infoEnc = Symbol.getSymbolTag(token.string);
            }
        } else {
            this.infoEnc = POSTag.OL;
        }

    }

    public Morpheme(Morpheme mp) {
        this.index = mp.index;
        this.string = mp.string;
        this.charSet = mp.charSet;
        this.infoEnc = mp.infoEnc;
    }

    public String getTag() {
        return POSTag.getTag(this.getTagNum());
    }

    public void setTag(String tag) {
        this.setTag(POSTag.getTagNum(tag));
    }

    public void setTag(long tagNum) {
        this.infoEnc = this.infoEnc & -9223372036854775808L | 9223372036854775807L & tagNum;
    }

    public long getTagNum() {
        return this.infoEnc & 9223372036854775807L;
    }

    public boolean isComposed() {
        return this.infoEnc < 0L;
    }

    public String getComposed() {
        return this.isComposed() ? "C" : "S";
    }

    public void setComposed(boolean composed) {
        if (composed) {
            this.infoEnc |= -9223372036854775808L;
        } else {
            this.infoEnc &= 9223372036854775807L;
        }

    }

    public void setComposed(String compType) {
        this.setComposed(Util.valid(compType) && compType.equals("C"));
    }

    public boolean isTag(long tagNum) {
        return this.getTagNum() == tagNum;
    }

    public boolean isTagOf(long tagNum) {
        return (this.infoEnc & 9223372036854775807L & tagNum) > 0L;
    }

    public boolean isMorpOf(String morp, long tagNum) {
        return (morp == null || morp.equals(this.string)) && this.isTagOf(tagNum);
    }

    public void append(Morpheme mp) {
        if (mp.isTag(POSTag.XSM)) {
            this.setTag(POSTag.MAG);
        } else if (!mp.isTag(POSTag.EFR)) {
            this.setTag(mp.getTagNum());
        } else if (mp.isTag(POSTag.EFR) && this.isTagOf(POSTag.EC) && (this.string.equals("아") || this.string.equals("어") || this.string.equals("구") || this.string.equals("고"))) {
            this.setTag(POSTag.EFN);
        }

        this.string = this.string + mp.string;
        this.setComposed(false);
    }

    public Morpheme copy() {
        Morpheme copy = new Morpheme();
        copy.string = this.string;
        copy.charSet = this.charSet;
        copy.index = this.index;
        copy.infoEnc = this.infoEnc;
        return copy;
    }

    static Morpheme create(String source) {
        Morpheme ret = null;
        if (source.startsWith("/")) {
            ret = new Morpheme("/", "SY", (String)null);
        } else {
            String[] arr = source.split("/");
            ret = new Morpheme(arr[0], arr[1], arr.length > 2 ? arr[2] : null);
        }

        return ret;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.index + "/" + this.string + "/" + this.getTag() + (this.isComposed() ? "/C" : ""));
        return sb.toString();
    }

    public String getSmplStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.string + "/" + this.getTag());
        return sb.toString();
    }

    public String getSmplStr2() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.string + "/" + this.getTag() + (this.isComposed() ? "/C" : ""));
        return sb.toString();
    }

    String getEncStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.string + "/" + this.infoEnc);
        return sb.toString();
    }
}
