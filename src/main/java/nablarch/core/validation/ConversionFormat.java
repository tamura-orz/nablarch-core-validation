package nablarch.core.validation;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンバータに使用するアノテーションを表すアノテーション。
 * <p>
 *   コンバータにフォーマット情報を指定する
 *   {@link nablarch.core.validation.convertor.Digits}
 *   のようなアノテーションに対し、本アノテーションを設定する。
 * </p>
 * 
 * @author Koichi Asano
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Published(tag = "architect")
public @interface ConversionFormat {

}
