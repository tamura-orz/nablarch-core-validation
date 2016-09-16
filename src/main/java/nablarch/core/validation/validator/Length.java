package nablarch.core.validation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Validation;

/**
 * 指定された範囲内の文字列長であることを表すアノテーション。
 * <br/>
 * 入力値がnull又は空文字の場合は、validと判定する。
 *
 * @author Koichi Asano
 */
@Validation
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface Length {

    /** 文字列の最小長 */
    int min() default 0;

    /** 文字列の最大長 */
    int max();

    /**
     * 文字列が指定範囲になかった場合のメッセージID。<br/>
     * 指定しなかった場合、デフォルトが使用される。
     */
    String messageId() default "";
}
