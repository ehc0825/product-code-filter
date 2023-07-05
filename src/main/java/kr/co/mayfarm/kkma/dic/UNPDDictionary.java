package kr.co.mayfarm.kkma.dic;

import kr.co.mayfarm.kkma.constants.POSTag;
import kr.co.mayfarm.kkma.constants.Path;
import kr.co.mayfarm.kkma.util.Timer;
import kr.co.mayfarm.kkma.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class UNPDDictionary {
    private static final float DEFAULT_PROB = -30.0F;
    private static final Hashtable<Character, Float> PROB_AT_NOUN_HASH = new Hashtable();
    public UNPDDictionary() {

    }

    public static final void load(String fileName) {
        System.out.println("Loading " + fileName);
        Timer timer = new Timer();
        timer.start();
        String line = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            while((line = br.readLine()) != null) {
                if (Util.valid(line) && !line.startsWith("//")) {
                    line = line.trim();
                    char ch = line.charAt(0);
                    float lnpr = Float.parseFloat(line.substring(1).trim());
                    PROB_AT_NOUN_HASH.put(ch, lnpr);
                }
            }

            br.close();
        } catch (Exception var9) {
            var9.printStackTrace();
            System.err.println(line);
            System.err.println("Unable to load probability dictionary!!");
        } finally {
            timer.stop();
            System.out.println(PROB_AT_NOUN_HASH.size() + " values are loaded. (Loading time( " + timer.getInterval() + " secs)");
        }

    }

    private static final float getProbAtNoun(char ch) {
        Float lnpr = (Float)PROB_AT_NOUN_HASH.get(ch);
        return lnpr != null ? lnpr : -30.0F;
    }

    public static float getProb(String str) {
        if (!Util.valid(str)) {
            return 1.4E-45F;
        } else {
            float prob = 0.0F;
            int i = 0;

            for(int len = str.length(); i < len; ++i) {
                char ch = str.charAt(i);
                prob += getProbAtNoun(ch);
            }

            return prob;
        }
    }

    public static float getProb2(String str) {
        if (!Util.valid(str)) {
            return 1.4E-45F;
        } else {
            float prob = 0.0F;
            int i = 0;

            for(int len = str.length(); i < len; ++i) {
                char ch = str.charAt(i);
                prob += getProbAtNoun(ch);
                if (i > 0) {
                    prob -= PDDictionary.getLnprPos(POSTag.NNA);
                }
            }

            return prob;
        }
    }

    static {
        load(Path.PLUGIN_PATH+"/dic/prob/lnpr_syllable_uni_noun.dic");
    }

}
