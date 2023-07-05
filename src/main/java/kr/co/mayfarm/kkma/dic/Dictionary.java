package kr.co.mayfarm.kkma.dic;

import kr.co.mayfarm.kkma.constants.Condition;
import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.constants.Path;
import kr.co.mayfarm.kkma.ma.MCandidate;
import kr.co.mayfarm.kkma.ma.MExpression;
import kr.co.mayfarm.kkma.ma.Morpheme;
import kr.co.mayfarm.kkma.util.Hangul;
import kr.co.mayfarm.kkma.util.StringSet;
import kr.co.mayfarm.kkma.util.Timer;
import kr.co.mayfarm.kkma.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Dictionary {
    private static Dictionary dictionary = null;
    static boolean isLoading = false;
    private Hashtable<String, MExpression> table = new Hashtable(530000);
    private List<MExpression> meList = null;
    private Hashtable<String, String[]> compNounTable = new Hashtable();
    private HashSet<String> verbStemSet = new HashSet();
    private List<String> userDicPath = new ArrayList<>();
    private int maxLen = 0;
    static final StringSet MO_SET1 = new StringSet(new String[]{"ㅏ", "ㅓ", "ㅐ", "ㅔ"});
    static final StringSet MO_SET2 = new StringSet(new String[]{"ㅗ", "ㅜ", "ㅡ"});

    public static final synchronized  Dictionary getInstance() {
        if (!isLoading && dictionary == null) {
            isLoading = true;
            dictionary = new Dictionary();
            isLoading = false;
        }

        return dictionary;
    }
    
    public static  final synchronized Dictionary getInstance(List<String> userDicPath) {
        if (!isLoading && dictionary == null) {
            isLoading = true;
            dictionary = new Dictionary(userDicPath);
            isLoading = false;
        }
        return dictionary;
    }

    public Dictionary(List<String> userDicPath) {
        this.userDicPath = userDicPath;
        createDictionary();
    }
    
    public Dictionary() {
        createDictionary();
    }

    private void createDictionary() {
        Timer timer = new Timer();

        try {
            timer.start();
            this.loadDic();
        } catch (Exception var6) {
            var6.printStackTrace();
        } finally {
            timer.stop();
            timer.printMsg("Dictionary Loading Time");
            System.out.println("Loaded Item " + this.table.size());
        }
    }

    public static void reload() {
        if (!isLoading && dictionary != null) {
            Timer timer = new Timer();

            try {
                System.out.println("reloading");
                timer.start();
                dictionary.clear();
                dictionary.loadDic();
            } catch (Exception var5) {
                var5.printStackTrace();
            } finally {
                timer.stop();
                timer.printMsg("Dictionary Loading Time");
                System.out.println("Loaded Item " + dictionary.table.size());
            }
        }

    }

    public static void reload(List<DicReader> dicReadList) {
        if (!isLoading && dictionary != null) {
            Timer timer = new Timer();

            try {
                System.out.println("reloading");
                timer.start();
                dictionary.clear();

                for(int i = 0; i < dicReadList.size(); ++i) {
                    dictionary.load((DicReader)dicReadList.get(i));
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            } finally {
                timer.stop();
                timer.printMsg("Dictionary Loading Time");
                System.out.println("Loaded Item " + dictionary.table.size());
            }
        }

    }

    public void clear() {
        this.table.clear();
        this.compNounTable.clear();
        this.verbStemSet.clear();
        this.maxLen = 0;
    }

    public static MCandidate getVerbBasicMC(String string, String posTag) throws Exception {
        String stem = null;
        if (string.charAt(string.length() - 1) == '다') {
            stem = string.substring(0, string.length() - 1);
        } else {
            stem = string;
        }

        MCandidate mCandidate = new MCandidate(stem, posTag);
        mCandidate.setCandDicLen((byte)stem.length());
        mCandidate.setExp(stem);
        return mCandidate;
    }

    public static List<MCandidate> getVerbExtendedMC(MCandidate mCandidate) {
        List<MCandidate> ret = new ArrayList();
        String stem = mCandidate.getExp();
        int stemLen = stem.length();
        String preStem = stem.substring(0, stemLen - 1);
        char lastCh = stem.charAt(stemLen - 1);
        char mo = 0;
        Hangul lastHg = Hangul.split(lastCh);
        Hangul preLastHg = null;
        char preLastCh;
        if (stemLen > 1) {
            preLastCh = stem.charAt(stemLen - 2);
            preLastHg = Hangul.split(preLastCh);
        } else {
            preLastCh = 0;
        }

        String exp = null;
        MCandidate mCandidateClone = null;
        if (!lastHg.hasJong() && lastHg.cho != 12622) {
            if (lastHg.jung != 12623 && lastHg.jung != 12624) {
                if (lastHg.jung == 12627) {
                    mCandidateClone = mCandidate.copy();
                    mCandidateClone.add(new Morpheme("어", POSTag.ECS));
                    mCandidateClone.setExp(stem);
                    mCandidateClone.clearHavingCondition();
                    mCandidateClone.initHavingCond(stem);
                    mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
                    mCandidateClone.setRealDicLen((byte)stem.length());
                    ret.add(mCandidateClone);
                    mCandidateClone = mCandidate.copy();
                    mCandidateClone.add(new Morpheme("어", POSTag.EFN));
                    mCandidateClone.setExp(stem);
                    mCandidateClone.clearHavingCondition();
                    mCandidateClone.initHavingCond(stem);
                    mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
                    mCandidateClone.setRealDicLen((byte)stem.length());
                    ret.add(mCandidateClone);
                }
            } else {
                mCandidateClone = mCandidate.copy();
                mCandidateClone.add(new Morpheme("아", POSTag.ECS));
                mCandidateClone.setExp(stem);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(stem);
                mCandidateClone.addHavingCond(Condition.MOEUM | Condition.YANGSEONG | Condition.AH);
                mCandidateClone.setRealDicLen((byte)stem.length());
                ret.add(mCandidateClone);
                mCandidateClone = mCandidate.copy();
                mCandidateClone.add(new Morpheme("아", POSTag.EFN));
                mCandidateClone.setExp(stem);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(stem);
                mCandidateClone.addHavingCond(Condition.MOEUM | Condition.YANGSEONG | Condition.AH);
                mCandidateClone.setRealDicLen((byte)stem.length());
                ret.add(mCandidateClone);
            }
        }

        if (lastCh == '찮' || lastCh == '잖') {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄴ');
            mCandidateClone.setExp(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            mCandidateClone.setExp(exp);
            ret.add(mCandidateClone);
        }

        if (lastCh == '하') {
            mCandidateClone = mCandidate.copy();
            exp = preStem + "했";
            mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + "해";
            mCandidateClone.add(new Morpheme("어", POSTag.ECS));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + "해";
            mCandidateClone.add(new Morpheme("어", POSTag.EFN));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            if (mCandidate.isTagOf(POSTag.VA | POSTag.VXA)) {
                mCandidateClone = mCandidate.copy();
                exp = preStem + "치";
                mCandidateClone.add(new Morpheme("지", POSTag.ECS));
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
            }
        } else if (!lastHg.hasJong() && lastHg.jung == 12643) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', 'ㅆ');
            mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', ' ');
            mCandidateClone.add(new Morpheme("어", POSTag.ECS));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅕ', ' ');
            mCandidateClone.add(new Morpheme("어", POSTag.EFN));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.MOEUM | Condition.EUMSEONG | Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (!lastHg.hasJong() && MO_SET1.contains(lastHg.jung)) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅆ');
            mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (lastCh == '르') {
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            if (preLastCh == '따') {
                exp = preStem + "랐";
                mCandidateClone.add(new Morpheme("았", POSTag.EPT));
                mCandidateClone.addHavingCond(Condition.EUT);
            } else if (preLastCh == '푸') {
                exp = stem + "렀";
                mCandidateClone.add(new Morpheme("었", POSTag.EPT));
                mCandidateClone.addHavingCond(Condition.EUT);
            } else {
                mo = getMoeum(lastHg, preLastHg);
                exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, 'ㅆ');
                if (mo == 12623) {
                    mCandidateClone.add(new Morpheme("았", POSTag.EPT));
                } else {
                    mCandidateClone.add(new Morpheme("었", POSTag.EPT));
                }

                mCandidateClone.addHavingCond(Condition.EUT);
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            if (preLastCh == '따') {
                exp = preStem + "라";
                mCandidateClone.add(new Morpheme("아", POSTag.ECS));
                mCandidateClone.addHavingCond(Condition.AH);
            } else if (preLastCh == '푸') {
                exp = stem + "러";
                mCandidateClone.add(new Morpheme("어", POSTag.ECS));
                mCandidateClone.addHavingCond(Condition.AH);
            } else {
                mo = getMoeum(lastHg, preLastHg);
                exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, ' ');
                if (mo == 12623) {
                    mCandidateClone.add(new Morpheme("아", POSTag.ECS));
                    mCandidateClone.addHavingCond(Condition.AH);
                } else {
                    mCandidateClone.add(new Morpheme("어", POSTag.ECS));
                    mCandidateClone.addHavingCond(Condition.AH);
                }
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            if (preLastCh == '따') {
                exp = preStem + "라";
                mCandidateClone.add(new Morpheme("아", POSTag.EFN));
                mCandidateClone.addHavingCond(Condition.AH);
            } else if (preLastCh == '푸') {
                exp = stem + "러";
                mCandidateClone.add(new Morpheme("어", POSTag.EFN));
                mCandidateClone.addHavingCond(Condition.AH);
            } else {
                mo = getMoeum(lastHg, preLastHg);
                exp = stem.substring(0, stemLen - 2) + Hangul.combine(preLastHg.cho, preLastHg.jung, 'ㄹ') + Hangul.combine(lastHg.cho, mo, ' ');
                if (mo == 12623) {
                    mCandidateClone.add(new Morpheme("아", POSTag.EFN));
                    mCandidateClone.addHavingCond(Condition.AH);
                } else {
                    mCandidateClone.add(new Morpheme("어", POSTag.EFN));
                    mCandidateClone.addHavingCond(Condition.AH);
                }
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (!lastHg.hasJong() && lastHg.jung == 12641) {
            mo = getMoeum(lastHg, preLastHg);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            exp = preStem + Hangul.combine(lastHg.cho, mo, 'ㅆ');
            if (mo == 12623) {
                mCandidateClone.add(new Morpheme("았", POSTag.EPT));
                mCandidateClone.addHavingCond(Condition.EUT);
            } else {
                mCandidateClone.add(new Morpheme("었", POSTag.EPT));
                mCandidateClone.addHavingCond(Condition.EUT);
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            exp = preStem + Hangul.combine(lastHg.cho, mo, ' ');
            if (mo == 12623) {
                mCandidateClone.add(new Morpheme("아", POSTag.ECS));
                mCandidateClone.addHavingCond(Condition.AH);
            } else {
                mCandidateClone.add(new Morpheme("어", POSTag.ECS));
                mCandidateClone.addHavingCond(Condition.AH);
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            exp = preStem + Hangul.combine(lastHg.cho, mo, ' ');
            if (mo == 12623) {
                mCandidateClone.add(new Morpheme("아", POSTag.EFN));
                mCandidateClone.addHavingCond(Condition.AH);
            } else {
                mCandidateClone.add(new Morpheme("어", POSTag.EFN));
                mCandidateClone.addHavingCond(Condition.AH);
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (!lastHg.hasJong() && MO_SET2.contains(lastHg.jung)) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), 'ㅆ');
            if (lastHg.jung == 12636) {
                mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            } else {
                mCandidateClone.add(new Morpheme("았", POSTag.EPT));
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), ' ');
            if (lastHg.jung == 12636) {
                mCandidateClone.add(new Morpheme("어", POSTag.ECS));
            } else {
                mCandidateClone.add(new Morpheme("아", POSTag.ECS));
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, getMoeum(lastHg, preLastHg), ' ');
            if (lastHg.jung == 12636) {
                mCandidateClone.add(new Morpheme("어", POSTag.EFN));
            } else {
                mCandidateClone.add(new Morpheme("아", POSTag.EFN));
            }

            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (!lastHg.hasJong() && lastHg.jung == 12634) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', ' ');
            mCandidateClone.add(new Morpheme("어", POSTag.ECS));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', ' ');
            mCandidateClone.add(new Morpheme("어", POSTag.EFN));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, 'ㅙ', 'ㅆ');
            mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        }

        if ("갑겁겹곱굽깁깝껍꼽납눕답덥돕둡땁떱랍럽렵롭립맙맵밉볍섭쉽습엽줍쭙춥탑".indexOf(lastCh) > -1) {
            char bChar = Hangul.combine(lastHg.cho, lastHg.jung, ' ');
            if (lastCh == '럽') {
                mCandidateClone = mCandidate.copy();
                exp = preStem + '런';
                mCandidateClone.add(new Morpheme("ㄴ", POSTag.ETD));
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
            }

            mCandidateClone = mCandidate.copy();
            if (lastHg.jung == 12631) {
                mo = 12632;
                mCandidateClone.add(new Morpheme("아", POSTag.ECS));
            } else {
                mo = 12637;
                mCandidateClone.add(new Morpheme("어", POSTag.ECS));
            }

            exp = preStem + bChar + Hangul.combine('ㅇ', mo, ' ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            if (lastHg.jung == 12631) {
                mo = 12632;
                mCandidateClone.add(new Morpheme("아", POSTag.EFN));
            } else {
                mo = 12637;
                mCandidateClone.add(new Morpheme("어", POSTag.EFN));
            }

            exp = preStem + bChar + Hangul.combine('ㅇ', mo, ' ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.AH);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.clearHavingCondition();
            if (lastHg.jung == 12631) {
                mo = 12632;
                mCandidateClone.add(new Morpheme("았", POSTag.EPT));
            } else {
                mo = 12637;
                mCandidateClone.add(new Morpheme("었", POSTag.EPT));
            }

            exp = preStem + bChar + Hangul.combine('ㅇ', mo, 'ㅆ');
            mCandidateClone.setExp(exp);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.EUT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + bChar + '우';
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.MINUS_BIEUB);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + bChar + '운';
            mCandidateClone.add(new Morpheme("ㄴ", POSTag.ETD));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.add(new Morpheme("ㄹ", POSTag.ETD));
            exp = preStem + bChar + '울';
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            mCandidateClone.add(new Morpheme("ㅁ", POSTag.ETN));
            exp = preStem + bChar + '움';
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if ("젓짓긋낫붓잇".indexOf(lastCh) > -1) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.MINUS_SIOT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (lastHg.jong == 12599) {
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄹ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.addHavingCond(Condition.MINUS_SIOT);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        } else if (!lastHg.hasJong() || lastHg.jong == 12601 || lastHg.jong == 12622) {
            mCandidateClone = mCandidate.copy();
            mCandidateClone.add(new Morpheme("ㄴ", POSTag.ETD));
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄴ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄹ');
            mCandidateClone.add(new Morpheme("ㄹ", POSTag.ETD));
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setRealDicLen((byte)exp.length());
            ret.add(mCandidateClone);
            if (lastHg.jong == 12601) {
                mCandidateClone = mCandidate.copy();
                exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㄻ');
                mCandidateClone.add(new Morpheme("ㅁ", POSTag.ETN));
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
                mCandidateClone = mCandidate.copy();
                exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.addHavingCond(Condition.MINUS_LIEUL);
                mCandidateClone.setCandDicLen((byte)exp.length());
                ret.add(mCandidateClone);
            } else if (lastHg.jong == 12622) {
                mCandidateClone = mCandidate.copy();
                exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, ' ');
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.addHavingCond(Condition.MINUS_HIEUT);
                mCandidateClone.setCandDicLen((byte)exp.length());
                ret.add(mCandidateClone);
                mCandidateClone = mCandidate.copy();
                mCandidateClone.clearHavingCondition();
                exp = preStem + Hangul.combine(lastHg.cho, 'ㅐ', ' ');
                if (mo == 12623) {
                    mCandidateClone.add(new Morpheme("아", POSTag.ECS));
                    mCandidateClone.addHavingCond(Condition.AH);
                } else {
                    mCandidateClone.add(new Morpheme("어", POSTag.ECS));
                    mCandidateClone.addHavingCond(Condition.AH);
                }

                mCandidateClone.setExp(exp);
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
                mCandidateClone = mCandidate.copy();
                mCandidateClone.clearHavingCondition();
                exp = preStem + Hangul.combine(lastHg.cho, 'ㅐ', ' ');
                if (mo == 12623) {
                    mCandidateClone.add(new Morpheme("아", POSTag.EFN));
                    mCandidateClone.addHavingCond(Condition.AH);
                } else {
                    mCandidateClone.add(new Morpheme("어", POSTag.EFN));
                    mCandidateClone.addHavingCond(Condition.AH);
                }

                mCandidateClone.setExp(exp);
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
            } else {
                mCandidateClone = mCandidate.copy();
                mCandidateClone.add(new Morpheme("ㅁ", POSTag.ETN));
                exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅁ');
                mCandidateClone.setExp(exp);
                mCandidateClone.clearHavingCondition();
                mCandidateClone.initHavingCond(exp);
                mCandidateClone.setRealDicLen((byte)exp.length());
                ret.add(mCandidateClone);
            }

            mCandidateClone = mCandidate.copy();
            exp = preStem + Hangul.combine(lastHg.cho, lastHg.jung, 'ㅂ');
            mCandidateClone.setExp(exp);
            mCandidateClone.clearHavingCondition();
            mCandidateClone.addHavingCond(Condition.BIEUB);
            mCandidateClone.initHavingCond(exp);
            mCandidateClone.setCandDicLen((byte)exp.length());
            ret.add(mCandidateClone);
        }

        return ret;
    }

    static char getMoeum(Hangul lastHg, Hangul preLastHg) {
        char mo = 0;
        char mo1 = lastHg.jung;
        if (mo1 == 12631) {
            mo = 12632;
        } else if (mo1 == 12636) {
            if (lastHg.cho == 12621) {
                mo = 12627;
            } else {
                mo = 12637;
            }
        } else if (mo1 == 12641) {
            if (preLastHg != null && Hangul.MO_POSITIVE_SET.contains(preLastHg.jung)) {
                mo = 12623;
            } else {
                mo = 12627;
            }
        }

        return mo;
    }

    private void add(String exp, MCandidate mc) throws Exception {
        mc.calcHashCode();
        mc.calcLnprOfTagging();
        MExpression me = this.get(exp);
        if (me == null) {
            me = new MExpression(exp, mc);
            float lnprOfSpacing = SpacingPDDictionary.getProb(exp);
            mc.setLnprOfSpacing(lnprOfSpacing);
            me.setLnprOfSpacing(lnprOfSpacing);
            this.table.put(exp, me);
            if (this.maxLen < exp.length()) {
                this.maxLen = exp.length();
            }
        } else {
            mc.setLnprOfSpacing(me.getLnprOfSpacing());
            me.add(mc);
        }

    }

    public boolean containVerbStem(String exp) {
        return this.verbStemSet.contains(exp);
    }

    public boolean containNoun(String exp) {
        MExpression me = (MExpression)this.table.get(exp);
        if (me == null) {
            return false;
        } else {
            Iterator var3 = me.iterator();

            MCandidate mc;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                mc = (MCandidate)var3.next();
            } while(mc.size() != 1 || !mc.isFirstTagOf(POSTag.NNA));

            return true;
        }
    }

    private synchronized MExpression get(String exp) {
        return (MExpression)this.table.get(exp);
    }

    public synchronized MExpression getMExpression(String exp) {
        MExpression ret = this.get(exp);
        return ret == null ? null : ret.copy();
    }

    public synchronized String[] getCompNoun(String noun) {
        return (String[])this.compNounTable.get(noun);
    }

    private void load(SimpleDicReader simpleDicReader) throws Exception {
        String line = null;

        try {
            String[] strArrTemp = null;

            while((line = simpleDicReader.readLine()) != null) {
                if (Util.valid(line) && !line.trim().startsWith("//")) {
                    line = line.trim();
                    String exp = null;
                    String mpInfo = null;
                    String condInfo = null;
                    if (line.indexOf(59) > 0) {
                        strArrTemp = line.split(";");
                        mpInfo = strArrTemp[0];
                        if (strArrTemp.length > 1) {
                            condInfo = strArrTemp[1];
                        }
                    } else {
                        mpInfo = line;
                    }

                    exp = mpInfo.split("/")[0];
                    String atl = null;
                    String hcl = null;
                    String ccl = null;
                    String ecl = null;
                    String compResult = null;
                    if (condInfo != null) {
                        StringTokenizer st = new StringTokenizer(condInfo, "#&@￢%$", true);
                        while(st.hasMoreTokens()) {
                            String token = st.nextToken();
                            if (token.equals("#")) {
                                token = st.nextToken().trim();
                                atl = token.substring(1, token.length() - 1);
                            } else if (token.equals("&")) {
                                token = st.nextToken().trim();
                                hcl = token.substring(1, token.length() - 1);
                            } else if (token.equals("@")) {
                                token = st.nextToken().trim();
                                ccl = token.substring(1, token.length() - 1);
                            } else if (token.equals("￢")) {
                                token = st.nextToken().trim();
                                ecl = token.substring(1, token.length() - 1);
                            } else if (token.equals("$")) {
                                token = st.nextToken().trim();
                                compResult = token.substring(1, token.length() - 1);
                            }
                        }
                    }

                    MCandidate mCandidate = MCandidate.create(exp, mpInfo, atl, hcl, ccl, ecl);

                    this.add(mCandidate.getExp(), mCandidate);
                    if (mCandidate.isTagOf(POSTag.V | POSTag.XSV | POSTag.XSA)) {
                        this.verbStemSet.add(exp);
                        List<MCandidate> mcList = getVerbExtendedMC(mCandidate);
                        int i = 0;

                        for(int size = mcList.size(); i < size; ++i) {
                            MCandidate mc = (MCandidate)mcList.get(i);
                            this.add(mc.getExp(), mc);
                        }
                    } else if (Util.valid(compResult)) {
                        this.compNounTable.put(exp, compResult.split("[+]"));
                    }
                }
            }
        } catch (Exception var20) {
            System.err.println(line);
            var20.printStackTrace();
            throw var20;
        } finally {
            simpleDicReader.cleanup();
        }

    }

    private void load(RawDicReader rawDicReader) throws Exception {
        String line = null;

        try {
            String[] arr = null;
            String string = null;
            String temp = null;

            while((line = rawDicReader.readLine()) != null) {
                if (Util.valid(line) && !line.startsWith("//")) {
                    line = line.trim();
                    arr = line.split(":");
                    string = arr[0];
                    if (arr.length >= 2) {
                        arr = arr[1].split(";");
                        int i = 0;

                        for(int stop = arr.length; i < stop; ++i) {
                            temp = arr[i].trim();
                            this.add(string, MCandidate.create(string, temp.substring(1, temp.length() - 1)));
                        }
                    }
                }
            }
        } catch (Exception var11) {
            System.err.println(line);
            var11.printStackTrace();
            throw var11;
        } finally {
            rawDicReader.cleanup();
        }

    }

    private void load(DicReader dicReader) throws Exception {
        if (dicReader instanceof SimpleDicReader) {
            this.load((SimpleDicReader)dicReader);
        } else {
            if (!(dicReader instanceof RawDicReader)) {
                throw new Exception("Unknown dictionary reader type.");
            }

            this.load((RawDicReader)dicReader);
        }

    }

    void loadSimple(String fileName) throws Exception {
        System.out.println("Loading " + fileName);
        Timer timer = new Timer();
        timer.start();

        try {
            this.load((SimpleDicReader)(new SimpleDicFileReader(fileName)));
        } finally {
            timer.stop();
            System.out.println("Loaded " + timer.getInterval() + "secs");
        }

    }

    private void loadRaw(String fileName) throws Exception {
        System.out.println("Loading " + fileName);
        Timer timer = new Timer();
        timer.start();

        try {
            this.load((RawDicReader)(new RawDicFileReader(fileName)));
        } catch (Exception var7) {
            throw var7;
        } finally {
            timer.stop();
            System.out.println("Loaded " + timer.getInterval() + "secs");
        }

    }

    protected void loadDic() throws Exception {
        if(userDicPath.size()!=0) {
            for(String path : userDicPath) {
                this.loadSimple(path);
            }
        }
        this.loadSimple(Path.PLUGIN_PATH +"/dic/00nng.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/01nnp.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/02nnb.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/03nr.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/04np.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/05comp.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/06slang.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/10verb.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/11vx.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/12xr.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/20md.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/21ma.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/30ic.dic");
        this.loadSimple(Path.PLUGIN_PATH+"/dic/40x.dic");
        this.loadRaw(Path.PLUGIN_PATH+"/dic/50josa.dic");
        this.loadRaw(Path.PLUGIN_PATH+"/dic/51eomi.dic");
        this.loadRaw(Path.PLUGIN_PATH+"/dic/52raw.dic");
    }

    public void printToFile(String fileName) {
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileOutputStream(new File(fileName)));
            ArrayList<MExpression> list = new ArrayList(this.table.values());
            Collections.sort(list);
            int i = 0;

            for(int stop = list.size(); i < stop; ++i) {
                MExpression me = (MExpression)list.get(i);
                pw.println(me);
                pw.flush();
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }

        }

    }

    public List<MExpression> getAsList() {
        if (this.table == null) {
            return null;
        } else {
            if (this.meList == null) {
                this.meList = new ArrayList(this.table.values());
            }

            return this.meList;
        }
    }

    public List<MCandidate> search(String str) {
        Timer timer = new Timer();
        timer.start();
        List<MCandidate> ret = new ArrayList();
        this.getAsList();

        for(int i = 0; i < this.meList.size(); ++i) {
            MExpression me = (MExpression)this.meList.get(i);
            if (me.getExp().indexOf(str) > -1) {
                ret.addAll(me);
            }
        }

        timer.printMsg(ret.size() + " candidates found.");
        timer.stop();
        return ret;
    }

    public List<Morpheme> getWordList() {
        ArrayList<Morpheme> morpList = new ArrayList();
        Iterator var2 = this.table.values().iterator();

        while(var2.hasNext()) {
            MExpression me = (MExpression)var2.next();
            Iterator var4 = me.iterator();

            while(var4.hasNext()) {
                MCandidate mc = (MCandidate)var4.next();
                if (mc.size() == 1) {
                    morpList.add(mc.get(0));
                }
            }
        }

        return morpList;
    }
}
