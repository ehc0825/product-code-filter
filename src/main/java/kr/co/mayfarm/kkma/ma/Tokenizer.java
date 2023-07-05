package kr.co.mayfarm.kkma.ma;


import kr.co.mayfarm.kkma.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Tokenizer {
    public static final TokenPattern[] PREDEFINED_TOKEN_PATTERN;

    public Tokenizer() {
    }

    public static List<Token> tokenize(String string) {
        if (!Util.valid(string)) {
            return null;
        } else {
            ArrayList<Token> tkList = new ArrayList();
            StringBuffer sb = new StringBuffer(string);
            int strlen = 0;

            for(int ptnlen = PREDEFINED_TOKEN_PATTERN.length; strlen < ptnlen; ++strlen) {
                TokenPattern tkptn = PREDEFINED_TOKEN_PATTERN[strlen];
                tkList.addAll(find(sb, tkptn));
            }

            strlen = string.length();
            boolean[] chkPrednfdPtn = checkFound(strlen, tkList);
            char preCh = 0;
            String temp = "";
            CharSetType presentToken = CharSetType.ETC;
            CharSetType lastToken = CharSetType.ETC;
            int tokenIndex = 0;

            for(int i = 0; i < strlen; ++i) {
                char ch = sb.charAt(i);
                lastToken = presentToken;
                Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
                if (chkPrednfdPtn[i]) {
                    presentToken = CharSetType.EMOTICON;
                } else if (ub != Character.UnicodeBlock.HANGUL_SYLLABLES && ub != Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                    if (ub != Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS && ub != Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                        if ((ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')) {
                            if (ch >= '0' && ch <= '9') {
                                presentToken = CharSetType.NUMBER;
                            } else if (ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n') {
                                if (ub != Character.UnicodeBlock.LETTERLIKE_SYMBOLS && ub != Character.UnicodeBlock.CJK_COMPATIBILITY && ub != Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION && ub != Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS && ub != Character.UnicodeBlock.BASIC_LATIN) {
                                    presentToken = CharSetType.ETC;
                                } else {
                                    presentToken = CharSetType.SYMBOL;
                                }
                            } else {
                                presentToken = CharSetType.SPACE;
                            }
                        } else {
                            presentToken = CharSetType.ENGLISH;
                        }
                    } else {
                        presentToken = CharSetType.HANMUN;
                    }
                } else {
                    presentToken = CharSetType.HANGUL;
                }

                if (i != 0 && (lastToken != presentToken || presentToken == CharSetType.ETC && (temp.length() <= 0 || temp.charAt(temp.length() - 1) != ch) || presentToken == CharSetType.SYMBOL && preCh != ch)) {
                    if (lastToken != CharSetType.EMOTICON) {
                        tkList.add(new Token(temp, lastToken, tokenIndex));
                    }

                    tokenIndex = i;
                    temp = "";
                }

                temp = temp + ch;
                preCh = ch;
            }

            if (Util.valid(temp)) {
                tkList.add(new Token(temp, presentToken, tokenIndex));
            }

            Collections.sort(tkList);
            return tkList;
        }
    }

    private static List<Token> find(StringBuffer sb, TokenPattern tkptn) {
        if (tkptn == null) {
            return null;
        } else {
            ArrayList<Token> tkList = new ArrayList();
            Matcher matcher = tkptn.pattern.matcher(sb);

            while(matcher.find()) {
                tkList.add(new Token(sb.substring(matcher.start(), matcher.end()), tkptn.charSetType, matcher.start()));

                for(int i = matcher.start(); i < matcher.end(); ++i) {
                    sb.setCharAt(i, ' ');
                }
            }

            return tkList;
        }
    }

    private static boolean[] checkFound(int strlen, List<Token> tkList) {
        boolean[] bFound = new boolean[strlen];

        int i;
        for(i = 0; i < strlen; ++i) {
            bFound[i] = false;
        }

        i = 0;

        for(int size = tkList == null ? 0 : tkList.size(); i < size; ++i) {
            Token tk = (Token)tkList.get(i);
            int j = 0;

            for(int jsize = tk.string.length(); j < jsize; ++j) {
                bFound[tk.index + j] = true;
            }
        }

        return bFound;
    }

    static {
        PREDEFINED_TOKEN_PATTERN = new TokenPattern[]{new TokenPattern("[hH][tT][tT][pP]([sS]*)://[a-zA-Z0-9/_.?=%&\\-]+",
                CharSetType.COMBINED), new TokenPattern("[a-zA-Z0-9]+[-][a-zA-Z0-9]+", CharSetType.COMBINED), new TokenPattern("(ㅋ|ㅠ|ㅜ|ㅎ){2,}", CharSetType.EMOTICON), new TokenPattern("(히|흐|크|키|케|캬){2,}", CharSetType.EMOTICON), new TokenPattern("(\\^){3,}", CharSetType.EMOTICON), new TokenPattern("[-]?[0-9]+([,][0-9]{3})*([.][0-9]+)?", CharSetType.NUMBER), new TokenPattern("[(][\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^][']?[)]", CharSetType.EMOTICON), new TokenPattern("[d][\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^][b]", CharSetType.EMOTICON), new TokenPattern("[\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^]([;]+|['\"avVㅗ])?", CharSetType.EMOTICON), new TokenPattern("[(];_;[)]", CharSetType.EMOTICON), new TokenPattern("[(]T[_.~oO\\^]?T[)]", CharSetType.EMOTICON), new TokenPattern("ㅜ[_.]?ㅜ", CharSetType.EMOTICON), new TokenPattern("ㅡ[_.]?ㅜ", CharSetType.EMOTICON), new TokenPattern("ㅜ[_.]?ㅡ", CharSetType.EMOTICON), new TokenPattern("ㅠ[_.]?ㅠ", CharSetType.EMOTICON), new TokenPattern("ㅡ[_.]?ㅠ", CharSetType.EMOTICON), new TokenPattern("ㅠ[_.]?ㅡ", CharSetType.EMOTICON), new TokenPattern("ㅠ[_.]?ㅜ", CharSetType.EMOTICON), new TokenPattern("ㅜ[_.]?ㅠ", CharSetType.EMOTICON), new TokenPattern("[(][-](_|[.])?[-]([;]+|[aㅗ])?[)](zzZ)?", CharSetType.EMOTICON), new TokenPattern("[-](_|[.])?[-]([;]+|[aㅗ]|(zzZ))?", CharSetType.EMOTICON), new TokenPattern("[ㅡ](_|[.])?[ㅡ]([;]+|[aㅗ]|(zzZ))?", CharSetType.EMOTICON), new TokenPattern("[(][>]([.]|_)?[<][)]", CharSetType.EMOTICON), new TokenPattern("[>]([.]|_)?[<]", CharSetType.EMOTICON), new TokenPattern("[(][>]([.]|_)?[>][)]", CharSetType.EMOTICON), new TokenPattern("[>]([.]|_)?[>]", CharSetType.EMOTICON), new TokenPattern("[(][¬]([.]|_)?[¬][)]", CharSetType.EMOTICON), new TokenPattern("[¬]([.]|_)?[¬]", CharSetType.EMOTICON), new TokenPattern("[(]'(_|[.])\\^[)]", CharSetType.EMOTICON), new TokenPattern("'(_|[.])\\^", CharSetType.EMOTICON), new TokenPattern("\\^(_|[.])[~]", CharSetType.EMOTICON), new TokenPattern("[~](_|[.])\\^", CharSetType.EMOTICON), new TokenPattern("[(][.][_][.][)]", CharSetType.EMOTICON), new TokenPattern("[(]['][_]['][)]", CharSetType.EMOTICON), new TokenPattern("[(][,][_][,][)]", CharSetType.EMOTICON), new TokenPattern("[(][X][_][X][)]", CharSetType.EMOTICON), new TokenPattern("[O][_.][o]", CharSetType.EMOTICON), new TokenPattern("[o][_.][O]", CharSetType.EMOTICON), new TokenPattern("m[(]_ _[)]m", CharSetType.EMOTICON)};
    }
}
