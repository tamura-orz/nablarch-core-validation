package nablarch.core.validation;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 階層構造を持つFormをバリデーションすることを表すアノテーション。
 * <p>
 * 本アノテーションでは階層構造に応じて、3つの使い方を提供する。
 * <ul>
 *   <li>
 *     <a href="#example">Formの親子関係が1対1の場合</a>
 *   </li>
 *   <li>
 *     <a href="#example2">Formの親子関係が1対多で、子の数が固定の場合</a>
 *   </li>
 *   <li>
 *     <a href="#example3">Formの親子関係が1対多で、子の数が可変の場合</a>
 *   </li>
 * </ul>
 *
 * <h3 id="example">Formの親子関係が1対1の場合の例を以下に示す。</h3>
 * <pre>
 * public class ExampleForm {
 *
 *    // 子Formのプロパティを追加する。
 *    private User user;
 *
 *    public ExampleForm(Map<String, Object> params) {
 *        user = (User) params.get("user");
 *    }
 *
 *    // 子Formを設定するセッタに、本アノテーションを設定する。
 *    {@code @ValidationTarget}
 *    public void setUser(User user) {
 *        this.user = user;
 *    }
 *
 *   // getterは省略
 *
 * }
 * </pre>
 *
 * <h3 id="example2">Formの親子関係が1対多で、子の数が固定の場合の例を以下に示す。</h3>
 * <pre>
 * public class ExampleForm {
 *
 *    // Form のプロパティに配列を追加する。
 *    private Address[] addressArray;
 *
 *    public ExampleForm(Map<String, Object> params) {
 *        addressArray =  (Address[]) params.get("addressArray");
 *    }
 *
 *    // getterは省略
 *
 *    // 固定の配列長を{@link #size()}属性に設定する。
 *    {@code @ValidationTarget(size = 3)}
 *    public void setAddressArray(Address[] addressArray) {
 *        this.addressArray = addressArray;
 *    }
 * }
 * </pre>
 *
 * <h3 id="example3">Formの親子関係が1対多で、子の数が可変の場合の例を以下に示す。</h3>
 * ※子の数が可変の場合は、{@link #sizeKey()}を使用し、可変長項目をリクエストパラメータで送る必要がある。
 * <pre>
 * public class ExampleForm {
 *
 *    // Formのプロパティに配列長を表すプロパティを追加する。
 *    private Address[] addressArray;
 *    private Integer addressArraySize;
 *
 *    public ExampleForm(Map<String, Object> params) {
 *        addressArray =  (Address[]) params.get("addressArray");
 *        addressArraySize = (Integer) params.get("addressArraySize");
 *    }
 *
 *    // getterは省略
 *
 *    {@code @Digits(integer=1)}
 *    {@code @Required}
 *    {@code @PropertyName("Address配列長")}
 *    public void setAddressArraySize(Integer addressArraySize) {
 *        this.addressArraySize = addressArraySize;
 *    }
 *
 *    // Form の配列のセッタに @ValidationTarget を設定する。
 *    // @ValidationTarget の{@link #sizeKey()}属性に、配列長を表すプロパティ名を設定する。
 *    {@code @ValidationTarget(sizeKey="addressArraySize")}
 *    public void setAddressArray(Address[] addressArray) {
 *        this.addressArray = addressArray;
 *    }
 * }
 * </pre>
 *
 * @author Koichi Asano 
 *
 */
@ConversionFormat
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface ValidationTarget {

    /** 配列のサイズ */
    int size() default 0;

    /** 可変長配列のサイズ */
    String sizeKey() default "";
}
