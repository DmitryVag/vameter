import java.io.IOException;
import java.io.InputStream;

// Some kind of a scanner to process an imput stream.
// The difference between this scanner and the original one is
// that it doesn't stop reading when the stream's finished.
// It is useful for communicating with Aduino because the data
// is not sent momentally and usual scranner stops in the middle
// of a string.

public class MyScanner {
    InputStream input;
    String spaceSymbols = " \t\r\n";
    String digits = "0123456789";

    MyScanner(InputStream input) {
        this.input = input;
    }

    // This metod waits while the input stream is empty,
    // so if the board is disconneccted the thread will stop
    // in an infinite loop.
    private char nextChar() throws IOException {
        while (input.available() == 0) {
            Thread.yield();
        }
        return (char) input.read();
    }

    
    // "Word" is a string surrounded by space symbols
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
