package org.biomart.processors;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import org.apache.commons.lang.ArrayUtils;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.processors.annotations.ContentType;
import org.biomart.processors.annotations.DefaultValue;
import org.biomart.processors.annotations.Required;
import org.biomart.processors.annotations.UserDefined;
import org.biomart.processors.fields.BaseField;
import org.biomart.queryEngine.Query;
import org.jdom.Document;

/**
 *
 * @author jhsu
 */
public class ProcessorImpl implements ProcessorInterface {
    protected OutputStream out;
    protected String contentType = "text/plain";

    protected ProcessorImpl() {}

    @Override
    public void preprocess(Document queryXML) {
        // nothing
    }

    @Override
    public void beforeQuery(Query query, OutputStream out) throws IOException {
        this.out = out;
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        return this.out;
    }

    @Override
    public void afterQuery() throws IOException {}

    @Override
    public final boolean accepts(String[] accepts) {
        boolean accepted = false;
        Class clazz = this.getClass();
        ContentType type = (ContentType)clazz
                .getAnnotation(ContentType.class);

        if (type != null && accepts != null) {
            contentType = type.value()[0];

            for (String curr : accepts) {
                if (ArrayUtils.contains(type.value(), curr)) {
                    contentType = curr;
                    accepted = true;
                    break;
                }
            }
        }

        return accepted;
    }

    @Override
    public final String getContentType() {
        return contentType;
    }

    @Override
    public final boolean isUserDefined(String name) {
        try {
            return this.getClass().getDeclaredField(name).isAnnotationPresent(UserDefined.class);
        } catch (NoSuchFieldException e) {
            // nothing
        }
        return false;
    }

    @Override
    public final boolean isRequired(String name) {
        try {
            return this.getClass().getDeclaredField(name).isAnnotationPresent(Required.class);
        } catch (NoSuchFieldException e) {
            // nothing
        }
        return false;
    }

    @Override
    public final String getDefaultValueForField(String name) {
        try {
            DefaultValue atn = this.getClass().getDeclaredField(name).getAnnotation(DefaultValue.class);
            if (atn != null) {
                return atn.value();
            }
        } catch (NoSuchFieldException e) {
            // nothing
        }
        return null;
    }

    @Override
    public final String[] getFieldNames() {
        Field[] fields = this.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i=0; i<fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    @Override
    public final void setFieldValue(String name, String value) {
        try {
            Field fld = this.getClass().getDeclaredField(name);
            fld.setAccessible(true);
            BaseField field = (BaseField)fld.get(this);
            field.setValue(value);
        } catch (Exception e) {
            throw new ValidationException("Cannot set value for field " + name, e);
        }
    }
}
