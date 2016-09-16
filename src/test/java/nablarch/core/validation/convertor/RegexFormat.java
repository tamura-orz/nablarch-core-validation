package nablarch.core.validation.convertor;

import nablarch.core.validation.ConversionFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 正規表現による変換フォーマット指定を表すアノテーション。
 * 
 * @author Koichi Asano
 *
 */
@ConversionFormat
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegexFormat {
    /**
     * フォーマット指定。
     */
    String value() default "";

    /**
     * 変換失敗時のメッセージID。
     */
    String messageId() default "";
}
