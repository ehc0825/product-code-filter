package kr.co.mayfarm.kkma.index;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class KeywordList extends ArrayList<Keyword> {
    int docLen = 0;
    private Hashtable<String, Keyword> table = null;

    public KeywordList(List<Keyword> list) {
        this.table = new Hashtable();
        this.addAll(list);
    }

    public void addAll(List<Keyword> list) {
        int i = 0;

        int size;
        Keyword keyword;
        for(size = list.size(); i < size; ++i) {
            keyword = (Keyword)list.get(i);
            Keyword org = null;
            org = (Keyword)this.table.get(keyword.getKey());
            this.docLen += keyword.getCnt();
            if (org == null) {
                this.table.put(keyword.getKey(), keyword);
                this.add(keyword);
            } else {
                org.increaseCnt(keyword.getCnt());
            }
        }

        i = 0;

        for(size = this.size(); i < size; ++i) {
            keyword = (Keyword)this.get(i);
            keyword.setFreq((double)keyword.getCnt() / (double)this.docLen);
        }

    }

    public int getDocLen() {
        return this.docLen;
    }
}
