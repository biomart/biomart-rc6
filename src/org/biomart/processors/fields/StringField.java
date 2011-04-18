package org.biomart.processors.fields;

import java.util.Map;
import org.biomart.common.exceptions.TechnicalException;

/**
 *
 * @author jhsu
 */
public final class StringField extends BaseField<String> {
    public StringField(String label) {
        super(String.class, label, null);
    }

    public StringField(String label, Map<String,String> choices) {
        super(String.class, label);
    }

    @Override
    public String parseValue(String str) throws TechnicalException {
        return str;
    }
}
