package nablarch.core.validation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Validation;


/**
 * 数値型のプロパティが指定した数値の範囲内であるかをチェックするアノテーション。
 * <p>
 *   バリデーションの内容と設定については{@link NumberRangeValidator}を参照。
 * </p>
 * @author Koichi Asano
 *
 */
@Validation
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface NumberRange {

    /**
     * 数値の最小値。
     */
    double min() default Double.NEGATIVE_INFINITY;
    /**
     * 数値の最大値。
     */
    double max() default Double.POSITIVE_INFINITY;
    /**
     * 数値が範囲に入らなかった場合のメッセージID。<br/>
     * 指定しなかった場合、{@link NumberRangeValidator}で設定されたデフォルトのメッセージIDが使用される。
     */
    String messageId() default "";
}
