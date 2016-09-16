package nablarch.core.validation.domain;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ConversionFormat;
import nablarch.core.validation.Validation;

/**
 * ドメイン定義によるバリデーションをサポートするヘルパークラス。
 * 
 * @author Kiyohito Itoh
 */
@Published(tag = "architect")
public class DomainValidationHelper {

    /** ドメインを表すアノテーションのクラス */
    private Class<? extends Annotation> domainAnnotation;

    /**
     * PJ毎に作成するドメインを表すアノテーションのFQCNを設定する。
     * <p/>
     * 「ドメイン定義を表すEnum」と「ドメインを表すアノテーション」を1対1でPJ毎に作成し、
     * 本プロパティに「ドメインを表すアノテーション」のFQCNを設定する。
     * 
     * @param fqcn PJ毎に作成するドメインを表すアノテーションのFQCN
     */
    @SuppressWarnings("unchecked")
    public void setDomainAnnotation(String fqcn) {
        try {
            domainAnnotation = (Class<? extends Annotation>) Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("class not found. fqcn = [" + fqcn + "]", e);
        }
    }

    /**
     * ドメインを表すアノテーションのクラスを取得する。
     * <p/>
     * ドメインを表すアノテーションのクラスが設定されていない場合は、{@link IllegalStateException}を送出する。
     * 
     * @return ドメインを表すアノテーションのクラス
     */
    public Class<? extends Annotation> getDomainAnnotation() {
        if (domainAnnotation == null) {
            throw new IllegalStateException("must be set domainAnnotation property.");
        }
        return domainAnnotation;
    }

    /**
     * 指定されたアノテーションがドメインを表すアノテーションであるか否かを判定する。
     * @param annotation アノテーション
     * @return 指定されたアノテーションがドメインを表すアノテーションである場合はtrue、それ以外はfalse
     */
    public boolean isDomainAnnotation(Annotation annotation) {
        return annotation != null && getDomainAnnotation().isAssignableFrom(annotation.annotationType());
    }

    /**
     * ドメイン定義に指定されたコンバータのアノテーションを取得する。
     * @param annotation ドメインを表すアノテーション
     * @return ドメイン定義に指定されたコンバータのアノテーション。コンバータのアノテーションが指定されていない場合はnull
     */
    public Annotation getConvertorAnnotation(Annotation annotation) {
        return getDomainDefinition(annotation).getConvertorAnnotation();
    }

    /**
     * ドメイン定義に指定されたバリデータのアノテーションを取得する。
     * @param annotation ドメインを表すアノテーション
     * @return ドメイン定義に指定されたバリデータのアノテーション
     */
    public List<Annotation> getValidatorAnnotations(Annotation annotation) {
        return getDomainDefinition(annotation).getValidatorAnnotations();
    }

    /**
     * アノテーションのvalue属性に指定された値を取得する。
     * <p/>
     * アノテーションの属性に指定された値が取得できない場合は、 {@link IllegalArgumentException}を送出する。
     * 
     * @param annotation アノテーション
     * @return アノテーションのvalue属性に指定された値
     */
    protected DomainDefinition getDomainDefinition(Annotation annotation) {
        try {
            return (DomainDefinition) domainAnnotation.getMethod("value").invoke(annotation);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "method invoking failed. annotation =[" + domainAnnotation.getName() + "], method = [value]", e);
        }
    }

    /**
     * ドメイン定義に指定されたコンバータのアノテーションを取得する。
     * <p/>
     * {@link DomainDefinition#getConvertorAnnotation()}の実装にて本メソッドを使用する。
     * 実装例を以下に示す。
     * <pre>
     * public Annotation getConvertorAnnotation() {
     *     return DomainValidationHelper.getConvertorAnnotation(this);
     * }
     * </pre>
     * @param domainEnum ドメイン定義
     * @return ドメイン定義に指定されたコンバータのアノテーション。コンバータのアノテーションが指定されていない場合はnull
     */
    public static Annotation getConvertorAnnotation(Enum<?> domainEnum) {
        List<Annotation> annotations = getAnnotations(domainEnum, ConversionFormat.class);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    /**
     * ドメイン定義に指定されたバリデータのアノテーションを取得する。
     * <p/>
     * {@link DomainDefinition#getValidatorAnnotations()}の実装にて本メソッドを使用する。
     * 実装例を以下に示す。
     * <pre>
     * public List<Annotation> getValidatorAnnotations() {
     *     return DomainValidationHelper.getValidatorAnnotations(this);
     * }
     * </pre>
     * @param domainEnum ドメイン定義
     * @return ドメイン定義に指定されたバリデータのアノテーション
     */
    public static List<Annotation> getValidatorAnnotations(Enum<?> domainEnum) {
        return getAnnotations(domainEnum, Validation.class);
    }

    /**
     * Enumに指定されたアノテーションを取得する。
     * @param targetEnum Enum
     * @param targetAnnotationClass 取得対象のアノテーションを表すクラス
     * @return Enumに指定されたアノテーション
     */
    private static List<Annotation> getAnnotations(Enum<?> targetEnum, Class<? extends Annotation> targetAnnotationClass) {

        Annotation[] allAnnotations;
        try {
            allAnnotations = targetEnum.getClass().getField(targetEnum.name()).getAnnotations();
        } catch (Exception e) {
            throw new IllegalStateException(
                "It failed to get annotations. targetEnum = [" + targetEnum + "]", e);
        }

        List<Annotation> annotations = new ArrayList<Annotation>();
        for (Annotation annotation : allAnnotations) {
            if (annotation.annotationType().getAnnotation(targetAnnotationClass) != null) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }
}
