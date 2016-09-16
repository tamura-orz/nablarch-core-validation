package nablarch.core.validation;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * バリデーション用のアノテーションであることを表わすアノテーション。
 * 
 * @author Koichi Asano
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Published(tag = "architect")
public @interface Validation {

}
