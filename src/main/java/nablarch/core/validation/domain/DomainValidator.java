package nablarch.core.validation.domain;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.core.repository.initialization.Initializable;
import nablarch.core.validation.Validation;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.Validator;

/**
 * ドメイン定義にしたがってバリデーションを行うバリデータ。
 *
 * @author kawasima
 * @author Kiyohito Itoh
 */
public class DomainValidator implements Validator, Initializable {

    /** ドメイン定義によるバリデーションをサポートするヘルパークラス */
    private DomainValidationHelper domainValidationHelper;

    /** バリデータのリスト。 */
    private List<Validator> validators;

    /** バリデータのマップ。 */
    private Map<Class<? extends Annotation>, Validator> validatorMap;

    @Override
    public void initialize() {
        final Map<Class<? extends Annotation>, Validator> postMap = new HashMap<Class<? extends Annotation>, Validator>();
        for (Validator validator : validators) {
            final Class<? extends Annotation> annotationClass = validator.getAnnotationClass();
            if (annotationClass == null) {
                throw new IllegalStateException("Validator's annotation class was not specified. "
                        + "validator class = " + validator.getClass().getName());
            }
            final Validation annotation = annotationClass.getAnnotation(Validation.class);
            if (annotation == null) {
                throw new IllegalStateException("Validator's annotation class was not annotated. "
                        + "validator class = " + validator.getClass().getName());
            }
            postMap.put(annotationClass, validator);
        }
        this.validatorMap = Collections.unmodifiableMap(postMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return domainValidationHelper.getDomainAnnotation();
    }

    @Override
    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {

        if (validatorMap == null) {
            throw new IllegalStateException("DomainValidator was not initialized.");
        }

        for (Annotation anno : getDomainValidationHelper().getValidatorAnnotations(annotation)) {
            final Validator validator = validatorMap.get(anno.annotationType());
            if (validator == null) {
                throw new UnsupportedOperationException("Validation annotation was not supported. "
                        + "Validation annotation = " + anno.annotationType().getName()
                        + ", targetClass = " + context.getTargetClass().getName()
                        + ", propertyName = " + propertyName);
            }
            if (!validator.validate(context, propertyName, propertyDisplayName, anno, value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * ドメインを表すアノテーションのクラスを取得する。
     * <p/>
     * ドメインを表すアノテーションのクラスが設定されていない場合は、{@link IllegalStateException}を送出する。
     * 
     * @return ドメインを表すアノテーションのクラス
     */
    protected DomainValidationHelper getDomainValidationHelper() {
        if (domainValidationHelper == null) {
            throw new IllegalStateException("must be set domainValidationHelper property.");
        }
        return domainValidationHelper;
    }

    /**
     * ドメイン定義によるバリデーションをサポートするヘルパークラスを設定する。
     * @param domainValidationHelper ドメイン定義によるバリデーションをサポートするヘルパークラス
     */
    public void setDomainValidationHelper(DomainValidationHelper domainValidationHelper) {
        this.domainValidationHelper = domainValidationHelper;
    }

    /**
     * バリデータのリストを設定する。
     * @param validators バリデータのリスト
     */
    public void setValidators(final List<Validator> validators) {
        this.validators = validators;
    }
}
