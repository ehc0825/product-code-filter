package kr.co.mayfarm.kkma.ma;


import java.util.ArrayList;

public class Sentence extends ArrayList<Eojeol> {
    public Sentence() {
    }

    public boolean add(Eojeol e) {
        Eojeol ej = e.removeIncorrectlyCombinedEojeol();
        if (ej != null) {
            super.add(ej);
        }

        return super.add(e);
    }

    public String getSentence() {
        StringBuffer sb = new StringBuffer();
        Eojeol eojeol = null;
        String temp = null;
        int i = 0;

        for(int stop = this.size(); i < stop; ++i) {
            eojeol = (Eojeol)this.get(i);
            temp = eojeol.exp;
            if (i > 0) {
                sb.append(" ");
            }

            sb.append(temp);
        }

        return sb.toString();
    }
}
