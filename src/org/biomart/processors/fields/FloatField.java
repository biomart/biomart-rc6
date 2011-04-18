package org.biomart.processors.fields;

import java.util.Map;
import org.biomart.common.exceptions.TechnicalException;

/**
 *
 * @author jhsu
 */
public final class FloatField extends BaseField<Float> {
    public FloatField(String label) {
        super(Float.class, label, null);
    }

    public FloatField(String label, Map<String,String> choices) {
        super(Float.class, label);
    }

    @Override
    public Float parseValue(String str) throws TechnicalException {
        return Float.parseFloat(str);
    }
}
