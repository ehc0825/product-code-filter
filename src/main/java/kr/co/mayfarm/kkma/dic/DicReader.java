package kr.co.mayfarm.kkma.dic;

import java.io.IOException;

public interface DicReader {
    String readLine() throws IOException;

    void cleanup() throws IOException;
}
