package nablarch.core.validation.domain.sample;

import nablarch.common.code.validator.CodeValue;
import nablarch.common.date.YYYYMMDD;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.domain.DomainDefinition;
import nablarch.core.validation.domain.DomainValidationHelper;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.unicode.SystemChar;

import java.lang.annotation.Annotation;
import java.util.List;

public enum DomainType implements DomainDefinition {

    FREE_TEXT, // String

    @Length(min=10, max=10)
    USER_ID, // String, String[]

    @Length(min=0, max=40)
    @SystemChar
    NAME, // String

    @YYYYMMDD(allowFormat="yyyy-MM-dd")
    DATE, // String

    @CodeValue(codeId="0002", pattern="PATTERN1")
    STATUS, // String

    REGISTERED, // Boolean

    @Digits(integer=3, fraction=2)
    ESTIMATE, // BigDecimal

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
