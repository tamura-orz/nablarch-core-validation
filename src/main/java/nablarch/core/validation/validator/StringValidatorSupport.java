package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.DirectCallableValidator;


/**
 * 文字列のValidatorの作成を助けるサポートクラス。
 * 
 * @param <A> 対応するアノテーションの型
 * 
 * @author Koichi Asano
 *
 */
public abstract class StringValidatorSupport<A extends Annotation>
implements DirectCallableValidator {

    /**
     * コンストラクタ。
     */
    @Published(tag = "architect")
    protected StringValidatorSupport() {
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> boolean validate(ValidationContext<T> context, String propertyName, 
            Object propertyDisplayName, Annotation annotation, Object value) {

        if (value == null) {
            return true;
        } else if (value instanceof String) {
            String strValue = (String) value;
            return validateSingleValue(context, propertyName, propertyDisplayName, (A) annotation, strValue);
        } else if (value instanceof String[]) {
            for (String strValue : (String[]) value) {
                if (!validateSingleValue(context, propertyName, propertyDisplayName, (A) annotation, strValue)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalArgumentException("unsupported property type was specified."
                    + " property name = " + propertyName + ","
                    + " property message id  = " + propertyDisplayName + ","
                    + " property type = " + value.getClass().getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */    
    public <T> boolean validate(ValidationContext<T> context,
                                String               propertyName,
                                Object               propertyDisplayName,
                                Map<String, Object>  params,
                                Object               value) {
        return validate(context, propertyName, propertyDisplayName, createAnnotation(params), value);
    }
    
    /**
     * 指定されたパラメータを属性値とするアノテーションオブジェクトを作成する。
     * @param params アノテーションの属性値を格納したMap
     * @return アノテーション
     */
    @Published(tag = "architect")
    public abstract A createAnnotation(Map<String, Object> params);

    /**
     * 1つの入力値に対するバリデーションを行う。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context バリデーションコンテキスト
     * @param propertyName プロパティ名
     * @param propertyDisplayObject プロパティの表示名オブジェクト
     * @param annotation アノテーション
     * @param value バリデーション対象の値
     * 
     * @return バリデーションに通った場合true
     */
    @Published(tag = "architect")
    public abstract <T> boolean validateSingleValue(ValidationContext<T> context, String propertyName,
            Object propertyDisplayObject, A annotation, String value);

}
