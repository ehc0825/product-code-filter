package kr.co.mayfarm.kkma.dic;

import java.io.*;

public class RawDicFileReader implements RawDicReader{
    BufferedReader br = null;

    public RawDicFileReader(String fileName) throws UnsupportedEncodingException, FileNotFoundException {
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
    }

    public String readLine() throws IOException {
        return this.br.readLine();
    }

    public void cleanup() throws IOException {
        if (this.br != null) {
            this.br.close();
        }

    }
}
