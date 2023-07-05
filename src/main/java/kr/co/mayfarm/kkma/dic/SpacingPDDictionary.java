package kr.co.mayfarm.kkma.dic;

import kr.co.mayfarm.kkma.constants.Path;
import kr.co.mayfarm.kkma.util.Timer;
import kr.co.mayfarm.kkma.util.Util;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class SpacingPDDictionary {
    private static final float DEFAULT_PROB = (float)Math.log(0.5D);
    private static final Hashtable<String, float[]> PROB_HASH = new Hashtable();

    public SpacingPDDictionary() {

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
                    String[] arr = line.split("\t");
                    float[] lnProb = new float[]{Float.parseFloat(arr[2]), Float.parseFloat(arr[3])};
                    PROB_HASH.put(getKey(arr[0], arr[1]), lnProb);
                }
            }

            br.close();
        } catch (Exception var9) {
            var9.printStackTrace();
            System.err.println(line);
            System.err.println("Unable to load probability dictionary!!");
        } finally {
            timer.stop();
            System.out.println(PROB_HASH.size() + " values are loaded. (Loading time( " + timer.getInterval() + " secs)");
        }

    }

    public static final float getProb(char ch1, char ch2, boolean hasSpace) {
        float[] probs = (float[])PROB_HASH.get(getKey(ch1, ch2));
        if (probs != null) {
            return hasSpace ? probs[0] : probs[1];
        } else {
            return DEFAULT_PROB;
        }
    }

    public static float getProb(String str) {
        if (!Util.valid(str)) {
            return 1.4E-45F;
        } else {
            float prob = 0.0F;
            str = str.trim().replaceAll("[ \t]+", " ");
            int i = 0;

            for(int len = str.length() - 1; i < len; ++i) {
                boolean hasSpace = false;
                char ch1 = str.charAt(i);
                char ch2 = str.charAt(i + 1);
                if (ch2 == ' ') {
                    ch2 = str.charAt(i + 1);
                    ++i;
                    hasSpace = true;
                }

                float fTemp = getProb(ch1, ch2, hasSpace);
                prob += fTemp;
            }

            return prob;
        }
    }

    private static String getKey(String syllable1, String syllable2) {
        return syllable1 + syllable2;
    }

    private static String getKey(char syllable1, char syllable2) {
        return syllable1 + "" + syllable2;
    }


    static {
        load(Path.PLUGIN_PATH+"/dic/prob/lnpr_syllable_bi.dic");
    }

}
