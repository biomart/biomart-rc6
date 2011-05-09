package org.biomart.common.utils;

import com.google.common.base.Ascii;
import com.google.common.base.Function;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.biomart.common.constants.OutputConstants;

/*
 * Takes in OutputStream and Function objects.
 * Whenever data is written to it, the Function will be called
 * to process data line by line. Result will be written to the
 * OutputStream.
 */
public class CallbackOutputStream extends OutputStream implements OutputConstants {
    private static final int BUFFER_SIZE = 8000;

    private final Function<String,String> callback;

    // keep track of current row
    private final byte[] buffer;
    private int bufPos;

    private int total = 0;

    final private OutputStream out;

    public CallbackOutputStream(OutputStream out, Function<String,String> callback) {
        buffer = new byte[BUFFER_SIZE];
        bufPos = 0;
        this.out = out;
        this.callback = callback;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public void write(int b) throws IOException {
        b &= 0xff; // force argument to one byte
        switch(b) {
            case Ascii.LF:
                byte[] temp = Arrays.copyOfRange(buffer, 0, bufPos);
                write(temp);
                bufPos = 0;
                break;
            default:
                buffer[bufPos++] = (byte)b;
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String row = new String(bytes).trim();

        if ("".equals(row)) {
            return;
        }

        String results = callback.apply(row);

        out.write(results.getBytes());

        total++;
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        int n = Math.min(len+off, bytes.length);
        for (int i=off; i<n; i++) {
            byte b = bytes[i];
            this.write(b);
        }
    }
}