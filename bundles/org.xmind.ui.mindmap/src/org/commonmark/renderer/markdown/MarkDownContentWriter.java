package org.commonmark.renderer.markdown;

import java.io.IOException;

public class MarkDownContentWriter {
    private final Appendable buffer;
    private char lastChar;

    public MarkDownContentWriter(Appendable out) {
        buffer = out;
    }

    public void write(String s) {
        append(s);
    }

    public void write(char c) {
        append(c);
    }

    private void append(String s) {
        try {
            buffer.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int length = s.length();
        if (length != 0) {
            lastChar = s.charAt(length - 1);
        }
    }

    private void append(char c) {
        try {
            buffer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lastChar = c;
    }

    public void line() {
        if (lastChar != 0 && lastChar != '\n') {
            append("\n");
        }
    }

    public void whitespace() {
        if (lastChar != 0 && lastChar != ' ') {
            append(' ');
        }
    }

}
