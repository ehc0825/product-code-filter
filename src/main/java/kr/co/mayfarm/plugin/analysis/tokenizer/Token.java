package kr.co.mayfarm.plugin.analysis.tokenizer;

public class Token {

    private String str;
    private String morph;
    private int position;

    public String getStr() {
        return str;
    }

    public String getMorph() {
        return morph;
    }

    public int getPosition(){
        return position;
    }

    public void setStr(String str){
        this.str = str;
    }

    public void setMorph(String morph) {
        this.morph = morph;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
