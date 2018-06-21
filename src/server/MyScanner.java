import java.io.IOException;
import java.io.InputStream;

public class MyScanner {
    InputStream input;
    String spaceSymbols = " \t\r\n";
    String digits = "0123456789";

    MyScanner(InputStream input) {
        this.input = input;
    }

    private char nextChar() throws IOException {
        while (input.available() == 0) {
            Thread.yield();
        }
        return (char) input.read();
    }

    String nextWord() {
        char c;
        StringBuilder word = new StringBuilder();
        try {
            do {
                c = nextChar();
            } while (spaceSymbols.indexOf(c) != -1);
            do {
                word.append(c);
                c = nextChar();
            } while (spaceSymbols.indexOf(c) == -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return word.toString();
    }

    int nextInt() {
        return Integer.parseInt(nextWord());
    }
    double nextDouble() {
        return Double.parseDouble(nextWord());
    }
}
