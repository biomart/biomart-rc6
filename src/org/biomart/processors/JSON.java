package org.biomart.processors;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.biomart.common.constants.OutputConstants;
import org.biomart.common.exceptions.BioMartException;
import org.biomart.objects.objects.Attribute;
import org.biomart.processors.annotations.ContentType;
import org.biomart.processors.annotations.DefaultValue;
import org.biomart.processors.annotations.Required;
import org.biomart.processors.annotations.UserDefined;
import org.biomart.processors.fields.BooleanField;
import org.biomart.processors.fields.StringField;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Document;

/**
 *
 * @author jhsu
 *
 * CSV Processor
 *
 */
@ContentType("application/json")
public class JSON extends ProcessorImpl {
    @UserDefined
    @DefaultValue("false")
    @Required
    private BooleanField streaming = new BooleanField("Stream results");

    @UserDefined
    @DefaultValue("")
    private StringField callback = new StringField("Name of callback function");

    private final List<String> header = new ArrayList<String>();

    // For non-streaming requests
    private OutputStream originalOut;
    private OutputStream dataOut;

    private boolean isJsonp;

    private class JsonOutputStream extends FilterOutputStream implements OutputConstants {
        private static final int BUFFER_SIZE = 8000;

        // keep track of current row
        private final byte[] buffer;
        private int bufPos;

        private int total = 0;

        public JsonOutputStream(OutputStream out) throws IOException {
            super(out);
            buffer = new byte[BUFFER_SIZE];
            bufPos = 0;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public void write(int b) throws IOException {
            b &= 0xff; // force argument to one byte
            switch(b) {
                case NEWLINE:
                    byte[] temp = ArrayUtils.subarray(this.buffer, 0, this.bufPos);
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

            String[] columns = row.split(DELIMITER);
            Object value = new LinkedHashMap<String,Object>();
            int i = 0;

            for (String curr : columns) {
                ((Map)value).put(header.get(i), curr);
                i++;
            }

            if (streaming.value) {
                Map map = new HashMap();
                map.put("data", value);
                value = map;
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonRow = mapper.writeValueAsString(value);

            out.write(jsonRow.getBytes());

            if (streaming.value) {
                out.write(NEWLINE);
            } else {
                out.write(',');
            }

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

    @Override
    public void preprocess(Document queryXML) {
        // No need to print header
        queryXML.getRootElement().setAttribute("header", "false");

        // If is JSONP, then we need to wrap results in callback function at the end
        isJsonp = !streaming.value && !"".equals(callback.value);
    }

    @Override
    public void beforeQuery(Query query, OutputStream outputHandle) throws IOException  {

        // store data if not streaming
        if (!streaming.value) {
            originalOut = outputHandle;
            outputHandle = dataOut = new ByteArrayOutputStream();
        }

        out = new JsonOutputStream(outputHandle);

        List<QueryElement> attributes = query.getOriginalAttributeOrder();

        // Store the display names
        for (QueryElement element : attributes) {
            String displayName = ((Attribute)element.getElement()).getDisplayName();
            header.add(displayName);
        }
    }

    @Override
    public void afterQuery() {
        if (streaming.value) {
            return;
        }

        String results = dataOut.toString();
        int total = ((JsonOutputStream)out).getTotal();

        try {
            out.flush();
            out.close();

            if (isJsonp) {
                originalOut.write((callback.value + "(").getBytes());
            }

            // Write header
            originalOut.write(("{\"total\":" + total + ",\"data\":[").getBytes());

            // Remove last trailing comma
            originalOut.write(results.substring(0, results.length()-1).getBytes());

            // End
            originalOut.write("]}".getBytes());

            if (isJsonp) {
                originalOut.write(");".getBytes());
            }

        } catch(IOException e) {
            throw new BioMartException("Error occurred during JSON output", e);
        }
    }
}
