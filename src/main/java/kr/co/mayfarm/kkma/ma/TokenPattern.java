package kr.co.mayfarm.kkma.ma;


import java.util.regex.Pattern;

public class TokenPattern {
    Pattern pattern = null;
    CharSetType charSetType = null;

    TokenPattern(String strPattern, CharSetType charSetType) {
        this.pattern = Pattern.compile(strPattern);
        this.charSetType = charSetType;
    }
}
