package nablarch.core.validation.domain.sample;

import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.domain.DomainValidator;

import java.lang.annotation.Annotation;
import java.util.Map;

public class DirectCallableDomainValidator extends DomainValidator implements DirectCallableValidator {

    @Override
    public <T> boolean validate(final ValidationContext<T> context,
            final String propertyName, final Object propertyDisplayName,
            final Map<String, Object> params, final Object value) {
        return validate(context, propertyName, propertyDisplayName,
                new Domain() {
                    @Override
                    public DomainType value() {
                        return (DomainType) params.get("value");
                    }
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Domain.class;
                    }
                }, value);
    }
}
