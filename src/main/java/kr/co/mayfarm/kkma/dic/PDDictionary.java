package kr.co.mayfarm.kkma.dic;

import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.constants.Path;
import kr.co.mayfarm.kkma.ma.Morpheme;
import kr.co.mayfarm.kkma.util.Timer;
import kr.co.mayfarm.kkma.util.Util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class PDDictionary {
    private static final Hashtable<Long, Float> LNPR_POS = new Hashtable(50);
    private static final Hashtable<String, Float> LNPR_MORP = new Hashtable(80000);
    private static final Hashtable<String, Float> LNPR_MORPS_G_EXP = new Hashtable(70000);
    private static final Hashtable<String, Float> LNPR_POS_G_EXP = new Hashtable(70000);
    private static final Hashtable<String, Float> LNPR_POS_G_MORP_INTRA = new Hashtable(60000);
    private static final Hashtable<String, Float> LNPR_POS_G_MORP_INTER = new Hashtable(520000);
    private static final float MIN_LNPR_POS = -9.0F;
    private static final float MIN_LNPR_MORP = -18.0F;
    public PDDictionary() {
    }

    private static final void loadLnprPos(String fileName, String pluginPath) {
        ProbDicReader dr = null;

        try {
            dr = new ProbDicReader(fileName,pluginPath);
            String[] arr = null;

            while((arr = dr.read()) != null) {
                long pos = POSTag.getTagNum(arr[0]);
                float lnpr = Float.parseFloat(arr[1]);
                LNPR_POS.put(pos, lnpr);
            }

            dr.close();
        } catch (Exception var6) {
            var6.printStackTrace();
            System.err.println(dr.line);
            System.err.println("Loading error: " + fileName);
        }

    }

    private static final void loadLnprMorp(String fileName,String pluginPath) {
       ProbDicReader dr = null;

        try {
            dr = new ProbDicReader(fileName,pluginPath);
            String[] arr = null;

            while((arr = dr.read()) != null) {
                String exp = arr[0];
                long pos = POSTag.getTagNum(arr[1]);
                float lnpr = Float.parseFloat(arr[2]);
                LNPR_MORP.put(exp + ":" + pos, lnpr);
            }

            dr.close();
        } catch (Exception var7) {
            var7.printStackTrace();
            System.err.println(dr.line);
            System.err.println("Loading error: " + fileName);
        }

    }

    private static final void loadLnprPosGExp(String fileName,String pluginPath) {
       ProbDicReader dr = null;

        try {
            dr = new ProbDicReader(fileName,pluginPath);
            String[] arr = null;

            while((arr = dr.read()) != null) {
                String exp = arr[0];
                long pos = POSTag.getTagNum(arr[1]);
                float lnpr = Float.parseFloat(arr[2]);
                LNPR_POS_G_EXP.put(pos + "|" + exp, lnpr);
            }

            dr.close();
        } catch (Exception var7) {
            var7.printStackTrace();
            System.err.println(dr.line);
            System.err.println("Loading error: " + fileName);
        }

    }

    private static final void loadLnprMorpsGExp(String fileName,String pluginPath) {
        ProbDicReader dr = null;

        try {
            dr = new ProbDicReader(fileName,pluginPath);
            String[] arr = null;

            while((arr = dr.read()) != null) {
                String morps = arr[0];
                float lnpr = Float.parseFloat(arr[1]);
                LNPR_MORPS_G_EXP.put(morps, lnpr);
            }

            dr.close();
        } catch (Exception var5) {
            var5.printStackTrace();
            System.err.println(dr.line);
            System.err.println("Loading error: " + fileName);
        }

    }

    private static final void loadLnprPosGMorp(String fileName, Hashtable<String, Float> probMap, String pluginPath) {
        ProbDicReader dr = null;

        try {
            dr = new ProbDicReader(fileName,pluginPath);
            String[] arr = null;

            while((arr = dr.read()) != null) {
                long prevPos;
                if (arr.length == 4) {
                    prevPos = POSTag.getTagNum(arr[0]);
                    String exp = arr[1];
                    long pos = POSTag.getTagNum(arr[2]);
                    float lnpr = Float.parseFloat(arr[3]);
                    probMap.put(getKey(prevPos, exp, pos), lnpr);
                } else if (arr.length == 3) {
                    prevPos = POSTag.getTagNum(arr[0]);
                    long pos = POSTag.getTagNum(arr[1]);
                    float lnpr = Float.parseFloat(arr[2]);
                    probMap.put(getKey(prevPos, (String)null, pos), lnpr);
                }
            }

            dr.close();
        } catch (Exception var10) {
            var10.printStackTrace();
            System.err.println(dr.line);
            System.err.println("Loading error: " + fileName);
        }

    }

    static final String getKey(long prevPos, String exp, long pos) {
        return prevPos + "|" + exp + ":" + pos;
    }

    public static float getLnprPos(long pos) {
        Float lnpr = (Float)LNPR_POS.get(getPrTag(pos));
        return lnpr == null ? -9.0F : lnpr;
    }

    private static float getLnprMorp(String exp, long pos) {
        Float lnpr = (Float)LNPR_MORP.get(exp + ":" + getPrTag(pos));
        return lnpr == null ? -18.0F : lnpr;
    }

    public static float getLnprPosGExp(String exp, long pos) {
        Float lnpr = (Float)LNPR_POS_G_EXP.get(getPrTag(pos) + "|" + exp);
        if (lnpr == null) {
            return pos == POSTag.NNG ? UNPDDictionary.getProb2(exp) : getLnprPos(pos);
        } else {
            return lnpr;
        }
    }

    public static float getLnprMorpsGExp(Morpheme preMp, Morpheme curMp) {
        return getLnprMorpsGExp(preMp.getString(), preMp.getTagNum(), curMp.getString(), curMp.getTagNum());
    }

    public static float getLnprMorpsGExp(String prevMorp, long prevPos, String curMorp, long curPos) {
        String key = prevMorp + "/" + getTag(prevPos) + "+" + curMorp + "/" + getTag(curPos);
        Float lnpr = (Float)LNPR_MORPS_G_EXP.get(key);
        return lnpr == null ? 1.0F : lnpr;
    }

    private static String getTag(long pos) {
        if ((POSTag.VX & pos) > 0L) {
            return "VX";
        } else if ((POSTag.EC & pos) > 0L) {
            return "EC";
        } else {
            return (POSTag.EF & pos) > 0L ? "EF" : POSTag.getTag(pos);
        }
    }

    public static float getLnprPosGMorpIntra(long prevPos, String exp, long pos) {
        return getLnprPosGMorp(LNPR_POS_G_MORP_INTRA, prevPos, exp, pos);
    }

    public static float getLnprPosGMorpInter(long prevPos, String exp, long pos) {
        return getLnprPosGMorp(LNPR_POS_G_MORP_INTER, prevPos, exp, pos);
    }

    private static float getLnprPosGMorp(Hashtable<String, Float> lnprMap, long prevPos, String exp, long pos) {
        Float lnpr = (Float)lnprMap.get(getKey(getPrTag(prevPos), exp, getPrTag(pos)));
        if (lnpr == null && (getLnprMorp(exp, pos) < -14.0F || (POSTag.S & prevPos) > 0L || (POSTag.NNP & pos) > 0L)) {
            lnpr = (Float)lnprMap.get(getKey(getPrTag(prevPos), (String)null, getPrTag(pos)));
        }

        return lnpr == null ? -18.0F : lnpr;
    }

    public static long getPrTag(long tag) {
        if (((POSTag.NNA | POSTag.UN) & tag) > 0L) {
            return POSTag.NNA;
        } else if (((POSTag.NNM | POSTag.NNB) & tag) > 0L) {
            return POSTag.NNB;
        } else if ((POSTag.VX & tag) > 0L) {
            return POSTag.VX;
        } else if ((POSTag.MD & tag) > 0L) {
            return POSTag.MD;
        } else if ((POSTag.EP & tag) > 0L) {
            return POSTag.EP;
        } else if ((POSTag.EF & tag) > 0L) {
            return POSTag.EF;
        } else {
            return (POSTag.EC & tag) > 0L ? POSTag.EC : tag;
        }
    }

    public static float getLnpr(String morps) {
        float lnpr = 0.0F;
        String[] arr = morps.trim().split("[+]");
        ArrayList<Morpheme> mpList = new ArrayList();
        String[] var4 = arr;
        int var5 = arr.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String temp = var4[var6];
            if (temp.equals(" ")) {
                mpList.add(new Morpheme(" ", POSTag.S));
            } else {
                String[] arr2 = temp.split("[/]");
                mpList.add(new Morpheme(arr2[1], POSTag.getTagNum(arr2[2])));
            }
        }

        Morpheme preMp = null;
        boolean spacing = false;
        System.out.println(morps);
        System.out.println(String.format("\tmorp%22s%10s%10s%10s%10s", "PosGExp", "spacing", "PosGMorp", "Pos", "lnpr"));
        Iterator var14 = mpList.iterator();

        while(var14.hasNext()) {
            Morpheme curMp = (Morpheme)var14.next();
            if (curMp.getString().equals(" ")) {
                spacing = true;
            } else {
                float lnprPosGExp = getLnprPosGExp(curMp.getString(), curMp.getTagNum());
                float lnprPosGMorp = 0.0F;
                float lnprPos = 0.0F;
                if (preMp != null) {
                    if (spacing) {
                        lnprPosGMorp = getLnprPosGMorpInter(preMp.getTagNum(), curMp.getString(), curMp.getTagNum());
                    } else {
                        float tempLnpr = getLnprMorpsGExp(preMp, curMp);
                        if (tempLnpr <= 0.0F) {
                            lnprPosGExp = tempLnpr - getLnprPosGExp(preMp.getString(), preMp.getTagNum());
                            lnprPosGMorp = 0.0F;
                            System.out.println("\t\t" + preMp + "+" + curMp + "\t" + tempLnpr);
                        } else {
                            lnprPosGMorp = getLnprPosGMorpIntra(preMp.getTagNum(), curMp.getString(), curMp.getTagNum());
                        }
                    }

                    lnprPos = getLnprPos(preMp.getTagNum());
                }

                lnpr += lnprPosGExp;
                lnpr += lnprPosGMorp;
                System.out.println("\t" + Util.getTabbedString(curMp.getSmplStr(), 4, 16) + String.format("%10.3f%10s%10.3f%10.3f%10.3f", lnprPosGExp, spacing, lnprPosGMorp, lnprPos, lnpr));
                spacing = false;
                preMp = curMp;
            }
        }

        return lnpr;
    }

    static {
        System.out.println("Prob Dic Loading!");
        Timer timer = new Timer();
        timer.start();
        loadLnprPos("/dic/prob/lnpr_pos.dic", Path.PLUGIN_PATH);
        System.out.println(LNPR_POS.size() + " loaded!");
        loadLnprMorp("/dic/prob/lnpr_morp.dic",Path.PLUGIN_PATH);
        System.out.println(LNPR_MORP.size() + " loaded!");
        loadLnprPosGExp("/dic/prob/lnpr_pos_g_exp.dic",Path.PLUGIN_PATH);
        System.out.println(LNPR_POS_G_EXP.size() + " loaded!");
        loadLnprMorpsGExp("/dic/prob/lnpr_morps_g_exp.dic",Path.PLUGIN_PATH);
        loadLnprPosGMorp("/dic/prob/lnpr_pos_g_morp_intra.dic", LNPR_POS_G_MORP_INTRA,Path.PLUGIN_PATH);
        System.out.println(LNPR_POS_G_MORP_INTRA.size() + " loaded!");
        loadLnprPosGMorp("/dic/prob/lnpr_pos_g_morp_inter.dic", LNPR_POS_G_MORP_INTER,Path.PLUGIN_PATH);
        System.out.println(LNPR_POS_G_MORP_INTER.size() + " loaded!");
        timer.stop();
        System.out.println("(Loading time : " + timer.getInterval() + " secs!");
    }

}
