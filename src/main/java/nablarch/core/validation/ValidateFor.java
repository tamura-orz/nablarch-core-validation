package nablarch.core.validation;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * バリデーションを実装するメソッドに付与するアノテーション。
 *
 * <pre>
 * このアノテーションを付与したバリデーションメソッドは、{@link ValidationUtil}を使用して呼び出す。
 *
 *     public class UserForm {
 *
 *          ...
 *
 *         {@code @ValidateFor("update")
 *         public static void validateForUpdate(ValidationContext<UserForm> context) {
 *             ValidationUtil.validateAll(context);
 *         }}
 *     }
 *
 *     // 上のバリデーションメソッドを呼び出す場合
 *     // @ValidateForアノテーションで指定した"update"を指定してValidationUtilを呼び出す。
 *     {@code ValidationContext<UserForm> context =
 *         ValidationUtil.validateAndConvertRequest("user", UserForm.class, req, "update");}
 * </pre>
 * 
 * @author Koichi Asano
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface ValidateFor {

    /**
     * バリデーション対象名。
     * <p/>
     * {@link ValidationUtil}でバリデーションメソッドを指定する名称
     */
    String[] value();
}
