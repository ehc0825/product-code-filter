package kr.co.mayfarm.kkma.dic;


import org.elasticsearch.SpecialPermission;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SimpleDicFileReader implements SimpleDicReader {
    BufferedReader br;

    public SimpleDicFileReader(String fileName) {
        BufferedReader bufferedReader;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        bufferedReader = AccessController.doPrivileged((PrivilegedAction<BufferedReader>) () -> {
            try {
                return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        });
        this.br = bufferedReader;
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
