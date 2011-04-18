package org.biomart.processors.fields;

import java.util.Map;
import org.biomart.common.exceptions.TechnicalException;

/**
 *
 * @author jhsu
 */
public final class IntegerField extends BaseField<Integer> {
    public IntegerField(String label) {
        super(Integer.class, label, null);
    }

    public IntegerField(String label, Map<String,String> choices) {
        super(Integer.class, label);
    }

    @Override
    public Integer parseValue(String str) throws TechnicalException {
        return Integer.parseInt(str);
    }
}
