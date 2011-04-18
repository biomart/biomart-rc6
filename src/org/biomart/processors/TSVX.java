package org.biomart.processors;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.biomart.common.constants.OutputConstants;
import org.biomart.objects.objects.Attribute;
import org.biomart.processors.annotations.ContentType;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;

/**
 *
 * @author jhsu
 *
 * HTML processor. It also takes care of dynamic URL creation.
 * the logic for adding attributes to the URL is that all (n) placeholders in the URL
 * for a given attribute x are present as subsequent n columns of the TSV resultset in
 * exactly the same order as they appear in the URL.
 */
@ContentType("text/html")
public class TSVX extends ProcessorImpl {
    @Override
    public void beforeQuery(Query query, OutputStream out) throws IOException {
        this.out = new HtmlOutputStream(query, out);
    }

    private class HtmlOutputStream extends FilterOutputStream implements OutputConstants {
        private static final int BUFFER_SIZE = 255;
        private final String DYNAMIC_ATTR_PATTERN = "%[a-zA-Z0-9_]+%";
        private final String PRIMARY_ATTR_PATTERN = "%s%";

        // Pre-defined HTML for URL injection
        private final byte[][] html = {
            {0x3c,0x61,0x20,0x74,0x61,0x72,0x67,0x65,0x74,0x3d, // <a target="_blank" href="
                     0x22,0x5f,0x62,0x6c,0x61,0x6e,0x6b,0x22,0x20,0x68,0x72,0x65,0x66,0x3d,0x22},
            {0x22,0x3e}, // ">
            {0x3c,0x2f,0x61,0x3e}, // </a>
            {0x3c,0x61,0x20,0x68,0x72,0x65,0x66,0x3d,0x22}, // <a href="

        };

        private boolean header;
        private boolean hasLinks = false;
        private boolean[] positions;
        private String[] links;
        private byte[] buffer = new byte[BUFFER_SIZE];
        private int bufPos = 0; // position in buffer
        private int currPos = 0; // position in results columns
        private boolean begin = true; // beginning of new row
        private boolean bufferStarted = false; // started writing to buffer
        private String currInjectionUrl = null; // temp holder for URL injection
        private byte[] currInjectionText = null;
        private boolean primaryInjection = true; // need to inject primary attribute (still)

        public HtmlOutputStream(Query query, OutputStream out) throws IOException {
            super(out);

            List<QueryElement> attributes = query.getOriginalAttributeOrder();
            int n = attributes.size();

            // Track a list of positions and their corresponding links
            this.positions = new boolean[n];
            this.links = new String[n];

            // If header==1, then we should treat first row as header and bypass URL injection
            if ("1".equals(query.getHeader())) {
                this.header = true;
            } else {
                this.header = false;
            }

            // Figure out which column needs URL injection
            for (int i=0; i< attributes.size(); i++) {
                QueryElement element = attributes.get(i);
                String url = ((Attribute)element.getElement()).getLinkOutUrl();
                if (url != null && !"".equals(url)) {
                    this.hasLinks = true;
                    this.links[i] = url;
                    this.positions[i] = true;
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            // If there are no links, just write to OutputStream
            if (!this.hasLinks) {
                out.write(b);
                return;
            }

            boolean doPrint = true;
            b &= 0xff; // force argument to one byte

            if (this.begin) {
                startBufferIfNeeded();
            }

            switch(b) {
                case NEWLINE:
                    if (!this.bufferStarted) {
                        break;
                    }
                case TAB:
                    // Write the link-out URL using value from buffer
                    if (this.bufferStarted)  {
                        if (!this.injectLink()) { // has not written to stream, waiting for more data to replace
                            this.currPos++;
                            doPrint = false;
                            break;
                        }
                    }

                    if (b==NEWLINE) break; // If coming from newline, skip the rest of case

                    this.currPos++;
                    out.write(b);
                    doPrint = false;
                    startBufferIfNeeded();
                    break;
            }

            this.begin = false;

            if (!doPrint) return;

            if (this.bufferStarted) {
                if (this.bufPos < this.buffer.length) {
                    this.buffer[this.bufPos++] = (byte)b;
                }
            } else {
                if (b==NEWLINE) {
                    this.header = false;
                    this.currPos = 0;
                    this.begin = true;
                }
                out.write(b);
            }
        }

        private void startBufferIfNeeded() {
            if (this.positions[this.currPos]) {
                this.bufferStarted = true;
            }
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            for (byte b : bytes) {
                this.write(b);
            }
        }

        @Override
        public void close() throws IOException {
            if (this.bufferStarted)
                this.injectLink();
            super.close();
        }

        // returns true if has written
        private boolean injectLink() throws IOException {
            byte[] temp = ArrayUtils.subarray(this.buffer, 0, this.bufPos);
            String value = new String(temp);

            if (ERROR_STRING.equals(value)) {
                out.write(temp);
                return true;
            }

            if (this.currInjectionUrl == null) {
                this.currInjectionUrl = this.links[this.currPos];
                this.currInjectionText = temp;
            }

            // put in current value
            if (this.primaryInjection) {
                this.currInjectionUrl = this.currInjectionUrl.replaceAll(PRIMARY_ATTR_PATTERN, value);
                this.primaryInjection = false;
            } else {
                this.currInjectionUrl = this.currInjectionUrl.replaceFirst(DYNAMIC_ATTR_PATTERN, value);
            }

            // reset buffer
            this.bufPos = 0;

            if (!this.currInjectionUrl.matches(".*?" + DYNAMIC_ATTR_PATTERN + ".*?")) {

                this.primaryInjection = true;

                if (this.header || this.currInjectionText.length == 0) {
                    out.write(this.currInjectionText);
                } else{
                    if (this.currInjectionUrl.startsWith("http://") || this.currInjectionUrl.startsWith("https://")) {
                        out.write(this.html[0]);
                    } else {
                        out.write(this.html[3]);
                    }

                    out.write(this.currInjectionUrl.getBytes());
                    out.write(this.html[1]);
                    out.write(this.currInjectionText);
                    out.write(this.html[2]);
                }

                this.bufferStarted = false;
                this.currInjectionUrl = null; // reset URL
                return true;
            }

            return false;
        }
    }

}
