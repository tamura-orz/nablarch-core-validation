package nablarch.core.validation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Validation;


/**
 * 必須入力を表わすアノテーション。
 * <p>
 *   バリデーションの内容と設定については{@link RequiredValidator}を参照。
 * </p>
 * @author Koichi Asano
 *
 */
@Validation
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface Required {
    /**
     * メッセージID。
     * <p>
     *   指定しなかった場合、{@link RequiredValidator}で設定されたデフォルトのメッセージIDが使用される。
     * </p>
     */
    String messageId() default "";
}
