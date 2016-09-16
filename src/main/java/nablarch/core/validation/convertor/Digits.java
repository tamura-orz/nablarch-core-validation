package nablarch.core.validation.convertor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ConversionFormat;


/**
 * 数値フォーマット指定を表わすアノテーション。
 * <p>
 *   {@link BigDecimalConvertor}, {@link LongConvertor}, {@link IntegerConvertor}を使用する場合は、
 *   本アノテーションの設定が必須である。<br>
 *　 <br>
 *   整数部1桁、小数部なしの場合は次のようにsetterに設定する。
 *   <pre>
 *     {@code @PropertyName("認証失敗回数")}
 *     {@code @Required}
 *     {@code @NumberRange(min = 0, max = 9)}
 *     {@code @Digits(integer = 1, fraction = 0)
 *     public void setFailedCount(Integer failedCount) {
 *         this.failedCount = failedCount;
 *     }}
 *   </pre>
 *   バリデーションの詳細は、各コンバータの仕様を参照。
 * </p>
 *
 * @author Koichi Asano
 * @see NumberConvertorSupport
 * @see BigDecimalConvertor
 * @see LongConvertor
 * @see IntegerConvertor
 */
@ConversionFormat
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface Digits {
    /**
     * 整数部桁数。
     */
    int integer();

    /**
     * 小数部桁数
     */
    int fraction() default 0;

    /**
     * カンマ編集可否。
     * <p>
     *   デフォルトは{@code true}(カンマ編集可)
     * </p>
     */
    boolean commaSeparated() default true;

    /**
     * 変換失敗時のメッセージID。
     */
    String messageId() default "";

}
