package nablarch.core.validation.sample.create;

import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;

import java.lang.annotation.Annotation;
import java.util.Map;

public class SampleCheckValidator implements DirectCallableValidator {

    private String allow0001MessageId;
    private String deny0001MessageId;

    public void setAllow0001MessageId(String messageId) {
        this.allow0001MessageId = messageId;
    }

    public void setDeny0001MessageId(String deny0001MessageId) {
        this.deny0001MessageId = deny0001MessageId;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return SampleCheck.class;
    }

    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {

        SampleCheck check = (SampleCheck) annotation;

        String strValue = (String) value;
        if (check.allow0001()) {
            if ("0001".equals(strValue) 
                    || "0002".equals(strValue)
                    || "0003".equals(strValue)) {
                return true;
            } else {
                context.addResultMessage(propertyName, allow0001MessageId,
                        propertyDisplayName);
                return false;
            }
        } else {
            if ("0002".equals(strValue) 
                    || "0003".equals(strValue)) {
                return true;
            } else {
                context.addResultMessage(propertyName, deny0001MessageId,
                        propertyDisplayName);
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T>      context,
                                String                    propertyName,
                                Object                    propertyDisplayName,
                                final Map<String, Object> params,
                                Object                    value) {
        
        SampleCheck annotation = new SampleCheck() {
            public Class<? extends Annotation> annotationType() {
                return SampleCheck.class;
            }
            public boolean allow0001() {
                Boolean allow0001 = (Boolean) params.get("allow0001");
                return (allow0001 == null) ? false
                                           : allow0001;
            }
        };
    
        return validate(context, propertyName, propertyDisplayName, annotation, value);
    }

}
