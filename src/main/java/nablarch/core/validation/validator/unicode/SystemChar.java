package nablarch.core.validation.validator.unicode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Validation;

/**
 * システム許容文字で構成された文字列であることを表わすアノテーション。
 * <p>
 *   バリデーションの内容と設定については{@link SystemCharValidator}を参照。
 * </p>
 * @author T.Kawasaki
 */
@Validation
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface SystemChar {

    /**
     *  システム許容文字以外が含まれた場合に出力するメッセージID。
     *  <p>
     *    指定がない場合は{@link SystemChar#charsetDef}で設定されているメッセージIDを使用する。
     *  </p>
     */
    String messageId() default "";

    /**
     * 許容文字集合定義の名称。
     * <p>
     *   設定でcharsetDefをコンポーネント定義した名前を指定する。
     * </p>
     */
    String charsetDef() default "";

    /**
     * 改行コードを許容するかどうか。
     * デフォルトは{@code false}（許容しない）。
     */
    boolean allowLineSeparator() default false;
}
