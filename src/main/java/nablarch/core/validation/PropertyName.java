package nablarch.core.validation;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * バリデーション対象プロパティの論理名を表わすアノテーション。
 * <p/>
 * バリデーションエラー時に表示されるメッセージなどで使用される。<br>
 * 属性を指定しない場合はプロパティ名が設定されないため、必ずどちらかの属性を設定すること。
 *
 * @author Koichi Asano
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface PropertyName {

    /**プロパティ名 */
    String value() default "";

    /**
     * プロパティ名を表わすメッセージID。<br/>
     * この属性が設定されていた場合、このメッセージIDに対応する文字列がプロパティ名として使用される。
     * @see nablarch.core.message.MessageUtil#getStringResource(String)
     */
    String messageId() default "";
}
