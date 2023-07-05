package kr.co.mayfarm.kkma.dic;

import kr.co.mayfarm.kkma.util.Util;

import java.io.*;

public class ProbDicReader {
    private BufferedReader br = null;
    String line = null;
    ProbDicReader(String fileName , String pluginPath) throws UnsupportedEncodingException, FileNotFoundException {
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(pluginPath+fileName), "UTF-8"));
    }

    public String[] read() throws IOException {
        while(true) {
            if ((this.line = this.br.readLine()) != null) {
                this.line = this.line.trim();
                if (!Util.valid(this.line) || this.line.startsWith("//")) {
                    continue;
                }

                return this.line.split("\t");
            }

            return null;
        }
    }

    public void close() throws IOException {
        if (this.br != null) {
            this.br.close();
        }

    }
}
