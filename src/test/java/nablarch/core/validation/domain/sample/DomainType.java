package nablarch.core.validation.domain.sample;

import java.lang.annotation.Annotation;
import java.util.List;

import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.domain.DomainDefinition;
import nablarch.core.validation.domain.DomainValidationHelper;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import nablarch.core.validation.validator.unicode.SystemChar;

public enum DomainType implements DomainDefinition {

    FREE_TEXT, // String

    @Length(min=10, max=10)
    USER_ID, // String, String[]

    @Length(min=0, max=40)
    @SystemChar
    NAME, // String

    DATE, // String

    STATUS, // String

    REGISTERED, // Boolean

    @Digits(integer=3, fraction=2)
    ESTIMATE, // BigDecimal

    @Required
    REQUIRED,
    
    @NumberRange(min=0, max=100)
    @Digits(integer=3)
    SCORE; // Integer
    

    @Override
    public Annotation getConvertorAnnotation() {
        return DomainValidationHelper.getConvertorAnnotation(this);
    }

    @Override
    public List<Annotation> getValidatorAnnotations() {
        return DomainValidationHelper.getValidatorAnnotations(this);
    }
}
