package nablarch.core.validation.domain;

import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.Validator;
import nablarch.core.validation.domain.sample.Domain;
import nablarch.core.validation.domain.sample.DomainType;
import nablarch.core.validation.domain.sample.SampleForm;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link DomainValidator}のテスト。
 * <p/>
 * ドメイン名指定のバリデーション処理に対するテストは、{@link DomainValidationTest}にて実施。
 * 
 * @author Kiyohito Itoh
 */
public class DomainValidatorTest {

    /**
     * 初期化なしに使用された場合、例外メッセージが出ること。
     */
    @Test
    public void testNotInitialized() {
        try {
            new DomainValidator().validate(null, null, null, null, null);
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("DomainValidator was not initialized."));
        }
    }

    /**
     * アノテーションクラスを返さないバリデータが指定された場合、初期化処理で例外メッセージが出ること。
     */
    @Test
    public void testNoAnnotationClassValidator() {
        DomainValidator validator = new DomainValidator();
        validator.setValidators(Arrays.<Validator>asList(new NoAnnotationClassValidator()));
        try {
            validator.initialize();
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Validator's annotation class was not specified. "
                                        + "validator class = nablarch.core.validation.domain.DomainValidatorTest$NoAnnotationClassValidator"));
        }
    }

    /**
     * アノテーションクラスを返さないバリデータが指定された場合、初期化処理で例外メッセージが出ること。
     */
    @Test
    public void testInvalidAnnotationValidator() {
        DomainValidator validator = new DomainValidator();
        validator.setValidators(Arrays.<Validator>asList(new InvalidAnnotationValidator()));
        try {
            validator.initialize();
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Validator's annotation class was not annotated. "
                                        + "validator class = nablarch.core.validation.domain.DomainValidatorTest$InvalidAnnotationValidator"));
        }
    }

    /**
     * {@link DomainValidationHelper}を設定されていない場合、例外メッセージが出ること。
     */
    @Test
    public void testNotSetDomainValidationHelper() {

        DomainValidator validator = new DomainValidator();
        validator.setValidators(new ArrayList<Validator>());
        validator.initialize();

        ValidationContext<SampleForm> context = new ValidationContext<SampleForm>("", SampleForm.class, null, null, "");
        try {
            validator.validate(context, "testProp", null, new Domain() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Domain.class;
                }

                @Override
                public DomainType value() {
                    return DomainType.SCORE; // added NumberRange annotation
                }
            }, null);
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("must be set domainValidationHelper property."));
        }
    }

    /**
     * サポートしていないバリデーション用のアノテーションクラスが指定された場合、例外メッセージが出ること。
     */
    @Test
    public void testUnsupportedValidationAnnotation() {

        DomainValidator validator = new DomainValidator();
        validator.setValidators(new ArrayList<Validator>());
        validator.setDomainValidationHelper(new DomainValidationHelper() {{
            setDomainAnnotation(Domain.class.getName());
        }});
        validator.initialize();

        ValidationContext<SampleForm> context = new ValidationContext<SampleForm>("", SampleForm.class, null, null, "");
        try {
            validator.validate(context, "testProp", null, new Domain() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Domain.class;
                }

                @Override
                public DomainType value() {
                    return DomainType.SCORE; // added NumberRange annotation
                }
            }, null);
            fail("must be thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("Validation annotation was not supported. "
                                        + "Validation annotation = nablarch.core.validation.validator.NumberRange"
                                        + ", targetClass = nablarch.core.validation.domain.sample.SampleForm"
                                        + ", propertyName = testProp"));
        }
    }

    private static final class NoAnnotationClassValidator implements Validator {
        @Override
        public Class<? extends Annotation> getAnnotationClass() {
            return null;
        }
        @Override
        public <T> boolean validate(ValidationContext<T> context,
                String propertyName, Object propertyDisplayName,
                Annotation annotation, Object value) {
            throw new UnsupportedOperationException("not happen");
        }
    }

    private static final class InvalidAnnotationValidator implements Validator {
        @Override
        public Class<? extends Annotation> getAnnotationClass() {
            return Test.class;
        }
        @Override
        public <T> boolean validate(ValidationContext<T> context,
                String propertyName, Object propertyDisplayName,
                Annotation annotation, Object value) {
            throw new UnsupportedOperationException("not happen");
        }
    }
}
