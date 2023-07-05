package kr.co.mayfarm.plugin.analysis.util;

import kr.co.mayfarm.plugin.analysis.plugin.KkmaAnalysisPlugin;

import java.io.*;

import java.util.HashMap;
import java.util.Map;

public class HanjaUtil {




    public static String convertToKor(StringBuilder input, Map<Character,Character> hanjaMap) {
        char[] inputStringArray = convertToUnicodeCharArray(input.toString());
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder tempKor = new StringBuilder();
        for (int i = 0; i < inputStringArray.length; i++) {
            if (null != hanjaMap.get(inputStringArray[i])) {
                tempKor.append(hanjaMap.get(inputStringArray[i]));
            } else {
                appendKor(tempKor, stringBuilder);
                stringBuilder.append(inputStringArray[i]);
                tempKor = new StringBuilder();
            }
        }
        appendKor(tempKor, stringBuilder);
        return decodeUnicode(stringBuilder.toString());
    }
    

    private static void appendKor(StringBuilder tempKor, StringBuilder stringBuilder) {
        if (tempKor.length() > 0) {
            tempKor = new StringBuilder(DueumUtil.convert(tempKor.toString()));
            stringBuilder.append(tempKor);
        }
    }



    public static Map<Character, Character> loadHanjaDic(String pluginPath) {
        Map<Character, Character> hanjaMap = new HashMap<>();
        try (BufferedReader br =
                     new BufferedReader(new FileReader(pluginPath + "/" + KkmaAnalysisPlugin.NAME + "/hanja.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] hanjaArray = line.split(",");
                if (hanjaArray.length >= 2) {
                    char hanja = (char) Integer.parseInt(hanjaArray[0].replace("\\u", ""), 16);
                    char unicode = (char) Integer.parseInt(hanjaArray[1].replace("\\u", ""), 16);
                    hanjaMap.put(hanja, unicode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hanjaMap;
    }


    private static String[] convertToUnicodeArray(String str) {
        int length = str.length();
        String[] unicodeArray = new String[length];

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            unicodeArray[i] = String.format("\\u%04X", (int) c);
        }

        return unicodeArray;
    }

    private static char[] convertToUnicodeCharArray(String str) {
        int length = str.length();
        char[] unicodeArray = new char[length];

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            unicodeArray[i] = c;
        }

        return unicodeArray;
    }


    public static String decodeUnicode(String in) {
        String out = in;
        int index;
        index = out.indexOf("\\u");
        while(index > -1)
        {
            int length = out.length();
            if(index > (length-6))break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = out.substring(numStart, numFinish);
            int number = Integer.parseInt(substring,16);
            String stringStart = out.substring(0, index);
            String stringEnd   = out.substring(numFinish);
            out = stringStart + ((char)number) + stringEnd;
            index = out.indexOf("\\u");
        }
        return out;
    }


    private HanjaUtil() {

    }
}
