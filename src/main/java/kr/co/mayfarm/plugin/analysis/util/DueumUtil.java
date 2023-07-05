package kr.co.mayfarm.plugin.analysis.util;

public class DueumUtil {

    private static final char[] CHOSUNGS = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
    };
    private static final char[] JUNGSUNGS = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ',
            'ㅢ', 'ㅣ'
    };
    // 두음법칙 적용할 중성
    private static final char[] DUEUM_JUNGSUNGS = {
            'ㅑ', 'ㅒ', 'ㅕ', 'ㅖ', 'ㅛ', 'ㅠ', 'ㅣ'
    };

    // 0xAC00
    private static final int BASE_CODE = 44032;
    // 중성의 개수 * 종성의 개수
    private static final int NEXT_CHOSUNG_CODE = 588;
    // 종성의 개수
    private static final int NEXT_JUNGSUNG_CODE = 28;

    private DueumUtil() {}

    public static String convert(String term) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char ch : term.toCharArray()) {
            if (ch != '\u0000' && isKoreanCharacter(ch)) {
                ch = getDueum(ch);
            }
            stringBuilder.append(ch);
        }

        return stringBuilder.toString();
    }

    private static boolean isKoreanCharacter(char ch) {
        boolean chain = ch >= 'ㄱ' && ch <= 'ㅎ';

        chain |= ch >= 'ㅏ' && ch <= 'ㅣ';
        chain |= ch >= '가' && ch <= '힣';

        return chain;
    }

    private static char getDueum(char ch) {
        int base = ch - BASE_CODE;
        int chosungIdx = base / NEXT_CHOSUNG_CODE;
        int jungsungIdx = (base - NEXT_CHOSUNG_CODE * chosungIdx) / NEXT_JUNGSUNG_CODE;

        if (CHOSUNGS[chosungIdx] == 'ㄹ') {
            base -= (3 * NEXT_CHOSUNG_CODE);
            chosungIdx -= 3;
        }
        if (CHOSUNGS[chosungIdx] == 'ㄴ') {
            for (char dueumJungsung : DUEUM_JUNGSUNGS) {
                if (dueumJungsung == JUNGSUNGS[jungsungIdx]) {
                    base += 9 * NEXT_CHOSUNG_CODE;
                    chosungIdx += 9;
                }
            }
        }

        return (char) (base + BASE_CODE);
    }

}
