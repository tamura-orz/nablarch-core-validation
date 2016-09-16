package nablarch.core.validation;

import java.lang.annotation.Annotation;

public class ErroneousValidator implements Validator {

    public Class<? extends Annotation> getAnnotationClass() {
        return Erroneous2.class;
    }

    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {
        return false;
    }

}
