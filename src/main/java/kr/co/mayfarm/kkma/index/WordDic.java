package kr.co.mayfarm.kkma.index;

import kr.co.mayfarm.kkma.util.Timer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class WordDic  extends HashSet<String> {
    int maxLen = -2147483648;
    int minLen = 2147483647;

    public WordDic(String fileName) {
        this.load(fileName);
    }

    public void load(String fileName) {
        System.out.println("Loading " + fileName);
        Timer timer = new Timer();
        timer.start();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            for(String line = null; (line = br.readLine()) != null; super.add(line)) {
                int len = line.length();
                if (len > this.maxLen) {
                    this.maxLen = len;
                }

                if (len < this.minLen) {
                    this.minLen = len;
                }
            }
        } catch (IOException var9) {
            System.err.println("Loading Error!");
        } finally {
            timer.stop();
            System.out.println("Loaded " + timer.getInterval() + "secs");
        }

    }
}
