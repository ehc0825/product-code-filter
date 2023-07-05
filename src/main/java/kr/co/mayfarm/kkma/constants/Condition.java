package kr.co.mayfarm.kkma.constants;

import kr.co.mayfarm.kkma.util.Hangul;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class Condition extends Hangul {
    public static final String[] COND_ARR = new String[]{"ㅣ겹", "모음", "자음", "양성", "음성", "사오", "사옵", "시오", "오", "으라", "으리", "으시", "아", "었", "겠", "려", "ㄴ", "ㄹ", "ㅁ", "ㅂ", "-ㅂ", "-ㄹ", "-ㅎ", "-ㅅ", "하", "가다", "오다", "ENG", "체언", "관형어", "부사어", "서술어", "EC", "F", "생략"};
    public static final Hashtable<String, Long> COND_HASH = new Hashtable();
    public static final Hashtable<Long, String> COND_NUM_HASH = new Hashtable();
    public static final long YI_DB;
    public static final long MOEUM;
    public static final long JAEUM;
    public static final long YANGSEONG;
    public static final long EUMSEONG;
    public static final long SAO;
    public static final long SAOP;
    public static final long SIO;
    public static final long OH;
    public static final long ERA;
    public static final long ERI;
    public static final long ESI;
    public static final long AH;
    public static final long EUT;
    public static final long GET;
    public static final long LYEO;
    public static final long NIEUN;
    public static final long MIEUM;
    public static final long LIEUL;
    public static final long BIEUB;
    public static final long MINUS_BIEUB;
    public static final long MINUS_LIEUL;
    public static final long MINUS_HIEUT;
    public static final long MINUS_SIOT;
    public static final long HA;
    public static final long GADA;
    public static final long ODA;
    public static final long ENG;
    public static final long N;
    public static final long D;
    public static final long A;
    public static final long V;
    public static final long EC;
    public static final long F;
    public static final long SHORTEN;
    public static final long MINUS_JA_SET;
    public static final long SET_FOR_UN;

    public Condition() {
    }

    private static final long getCondNum(int i) {
        return 1L << i;
    }

    public static long getCondNum(String cond) {
        try {
            return (Long)COND_HASH.get(cond);
        } catch (Exception var2) {
            System.err.println("[" + cond + "] 정의되지 않은 조건입니다.");
            return 0L;
        }
    }

    public static long getCondNum(String[] conds) {
        long l = 0L;
        int i = 0;

        for(int size = conds == null ? 0 : conds.length; i < size; ++i) {
            l |= getCondNum(conds[i]);
        }

        return l;
    }

    public static String getCond(long condNum) {
        return condNum == 0L ? null : (String)COND_NUM_HASH.get(new Long(condNum));
    }

    public static List<String> getCondList(long encCondNum) {
        List<String> ret = new ArrayList();
        int i = 0;

        for(int stop = COND_ARR.length; i < stop; ++i) {
            if ((encCondNum & getCondNum(i)) > 0L) {
                ret.add(COND_ARR[i]);
            }
        }

        return ret;
    }

    public static String getCondStr(long encCondNum) {
        StringBuffer sb = new StringBuffer();
        List<String> condList = getCondList(encCondNum);
        int i = 0;

        for(int size = condList.size(); i < size; ++i) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append((String)condList.get(i));
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    public static final List<String> getCondList() {
        List<String> condList = new ArrayList();
        List<Long> condNumList = new ArrayList(COND_NUM_HASH.keySet());
        Collections.sort(condNumList);
        int i = 0;

        for(int size = condNumList.size(); i < size; ++i) {
            condList.add(COND_NUM_HASH.get(condNumList.get(i)));
        }

        return condList;
    }

    public static final boolean checkAnd(long havingCond, long checkingCond) {
        return (havingCond & checkingCond) == checkingCond;
    }

    public static final boolean checkOr(long havingCond, long checkingCond) {
        return (havingCond & checkingCond) > 0L;
    }

    static {
        long conditionNum = 0L;
        int i = 0;

        for(int stop = COND_ARR.length; i < stop; ++i) {
            conditionNum = 1L << i;
            COND_HASH.put(COND_ARR[i], new Long(conditionNum));
            COND_NUM_HASH.put(new Long(conditionNum), COND_ARR[i]);
        }

        YI_DB = getCondNum("ㅣ겹");
        MOEUM = getCondNum("모음");
        JAEUM = getCondNum("자음");
        YANGSEONG = getCondNum("양성");
        EUMSEONG = getCondNum("음성");
        SAO = getCondNum("사오");
        SAOP = getCondNum("사옵");
        SIO = getCondNum("시오");
        OH = getCondNum("오");
        ERA = getCondNum("으라");
        ERI = getCondNum("으리");
        ESI = getCondNum("으시");
        AH = getCondNum("아");
        EUT = getCondNum("었");
        GET = getCondNum("겠");
        LYEO = getCondNum("려");
        NIEUN = getCondNum("ㄴ");
        MIEUM = getCondNum("ㅁ");
        LIEUL = getCondNum("ㄹ");
        BIEUB = getCondNum("ㅂ");
        MINUS_BIEUB = getCondNum("-ㅂ");
        MINUS_LIEUL = getCondNum("-ㄹ");
        MINUS_HIEUT = getCondNum("-ㅎ");
        MINUS_SIOT = getCondNum("-ㅅ");
        HA = getCondNum("하");
        GADA = getCondNum("가다");
        ODA = getCondNum("오다");
        ENG = getCondNum("ENG");
        N = getCondNum("체언");
        D = getCondNum("관형어");
        A = getCondNum("부사어");
        V = getCondNum("서술어");
        EC = getCondNum("EC");
        F = getCondNum("F");
        SHORTEN = getCondNum("생략");
        MINUS_JA_SET = MINUS_LIEUL | MINUS_HIEUT | MINUS_SIOT;
        SET_FOR_UN = JAEUM | MOEUM | YANGSEONG | EUMSEONG;
    }
}
