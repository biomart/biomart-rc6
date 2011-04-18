package org.biomart.processors;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.ArrayUtils;
import org.biomart.common.constants.OutputConstants;
import org.biomart.processors.annotations.DefaultValue;
import org.biomart.processors.annotations.Required;
import org.biomart.processors.annotations.UserDefined;
import org.biomart.processors.fields.StringField;
import org.biomart.queryEngine.Query;

/**
 *
 * @author jhsu
 *
 * CSV Processor
 *
 */
public class CSV extends ProcessorImpl {
    @UserDefined
    @DefaultValue(",")
    @Required
    private StringField delimiter = new StringField("Delimiter");

    @UserDefined
    @DefaultValue("\"")
    @Required
    private StringField quote = new StringField("Quote");

    private class CsvOutputStream extends FilterOutputStream implements OutputConstants {
        private static final int BUFFER_SIZE = 8000;
        private final CSVWriter writer;
        private final byte[] buffer;
        private int bufPos;

        public CsvOutputStream(OutputStream out, char delimiter, char quote) throws IOException {
            super(out);
            writer = new CSVWriter(new OutputStreamWriter(out), delimiter, quote);
            buffer = new byte[BUFFER_SIZE];
            bufPos = 0;
        }

        @Override
        public void write(int b) throws IOException {
            b &= 0xff; // force argument to one byte
            switch(b) {
                case NEWLINE:
                    writeResults();
                    break;
                default:
                    buffer[bufPos++] = (byte)b;
            }
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            int n = Math.min(len+off, bytes.length);
            for (int i=off; i<n; i++) {
                byte b = bytes[i];
                this.write(b);
            }
        }

        @Override
        public void close() throws IOException {
            writeResults();
            writer.close();
            super.close();
        }

        private void writeResults() throws IOException {
            byte[] temp = ArrayUtils.subarray(this.buffer, 0, this.bufPos);
            String row = new String(temp);
            writer.writeNext(row.split(DELIMITER));
            writer.flush();
            bufPos = 0;
        }
    }

    @Override
    public void beforeQuery(Query query, OutputStream outputHandle) throws IOException  {
        this.out = new CsvOutputStream(outputHandle, delimiter.value.charAt(0), quote.value.charAt(0));
    }
}
