package nablarch.core.validation.domain;

import nablarch.core.validation.domain.sample.Domain;
import nablarch.core.validation.domain.sample.DomainType;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link DomainValidationHelper}のテスト。
 * <p/>
 * ドメイン名指定のバリデーション処理に対するテストは、{@link DomainValidationTest}にて実施。
 * 
 * @author Kiyohito Itoh
 */
public class DomainValidationHelperTest {

    /**
     * 存在しないドメインを表すアノテーションのFQCNが指定された場合、例外メッセージが出ること。
     */
    @Test
    public void testNotFoundDomainClassFQCN() {
        try {
            new DomainValidationHelper().setDomainAnnotation("not-found-fqcn");
            fail("must be thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("class not found. fqcn = [not-found-fqcn]"));
        }
    }

    private static final Domain TEST_DOMAIN = new Domain() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Domain.class;
        }
        @Override
        public DomainType value() {
            return DomainType.SCORE;
        }
    };

    private @interface InvalidDomain {
        DomainType type();
    }

    private static final InvalidDomain INVALID_DOMAIN = new InvalidDomain() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return InvalidDomain.class;
        }
        @Override
        public DomainType type() {
            return DomainType.SCORE;
        }
    };

    /**
     * ドメインを表すアノテーションのFQCNが指定されていない状態で処理した場合、例外メッセージが出ること。
     */
    @Test
    public void testNotSetDomainClassProperty() {
        try {
            new DomainValidationHelper().getDomainAnnotation();
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("must be set domainAnnotation property."));
        }
        try {
            new DomainValidationHelper().isDomainAnnotation(TEST_DOMAIN);
            fail("must be thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("must be set domainAnnotation property."));
        }
    }

    /**
     * value属性を持たない不正なドメインを表すアノテーションが指定されてた場合、例外メッセージが出ること。
     */
    @Test
    public void testInvalidDomainNotHaveValueAttribute() {
        DomainValidationHelper helper = new DomainValidationHelper();
        helper.setDomainAnnotation(InvalidDomain.class.getName());
        try {
            helper.getConvertorAnnotation(INVALID_DOMAIN);
            fail("must be thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("method invoking failed. "
                                        + "annotation =[nablarch.core.validation.domain.DomainValidationHelperTest$InvalidDomain], "
                                        + "method = [value]"));
        }
        try {
            helper.getValidatorAnnotations(INVALID_DOMAIN);
            fail("must be thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("method invoking failed. "
                                        + "annotation =[nablarch.core.validation.domain.DomainValidationHelperTest$InvalidDomain], "
                                        + "method = [value]"));
        }
    }

    /**
     * ドメインEnumのアノテーション取得に失敗した場合、例外メッセージが出ること。
     */
    @Test
    public void testFailedToGetAnnotations() {
        DomainType domainType = null;
        try {
            DomainValidationHelper.getConvertorAnnotation(domainType);
            fail("must be thrown IllegalArgumentException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("It failed to get annotations. targetEnum = [null]"));
        }
    }
}
