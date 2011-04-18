package org.biomart.processors.fields;

import java.util.Map;
import org.biomart.common.exceptions.TechnicalException;

/**
 *
 * @author jhsu
 */
public final class BooleanField extends BaseField<Boolean> {
    public BooleanField(String label) {
        super(Boolean.class, label, null);
    }

    public BooleanField(String label, Map<String,String> choices) {
        super(Boolean.class, label);
    }

    @Override
    public Boolean parseValue(String str) throws TechnicalException {
        return Boolean.parseBoolean(str);
    }
}
