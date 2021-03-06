package nablarch.core.validation.domain.sample;

import nablarch.core.validation.ConversionFormat;
import nablarch.core.validation.Validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConversionFormat
@Validation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Domain {
    /**
     * Domainの種類
     */
    DomainType value();
}
