package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 必須入力をチェックするクラス。
 * <p>
 *   {@link Required}アノテーションが設定されたプロパティに値が入力されているかをチェックする。
 *
 *   <p>
 *     <b>使用するための設定</b>
 *   </p>
 *   本バリデータを使用するためにはデフォルトのメッセージIDを指定する必要がある。
 *   <pre>
 *     {@code <component class="nablarch.core.validation.validator.RequiredValidator">
 *         <property name="messageId" value="MSG90001"/>
 *     </component>}
 *   </pre>
 *
 *   <p>
 *     <b>必須プロパティの設定</b>
 *   </p>
 *   必須入力チェックをしたいプロパティのセッタに{@link Required}アノテーションを次のように設定する。
 *   <pre>
 *     {@code @PropertyName("パスワード")}
 *     {@code @Required
 *     public void setConfirmPassword(String confirmPassword) {
 *         this.confirmPassword = confirmPassword;
 *     }}
 *   </pre>
 * </p>
 *
 * @author Koichi Asano
 *
 */
public class RequiredValidator implements DirectCallableValidator {

    /**
     * デフォルトのエラーメッセージのメッセージID。
     */
    private String messageId;

    /**
     * デフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は必ず入力してください。"
     * @param messageId エラーメッセージのデフォルトのメッセージID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> getAnnotationClass() {
        return Required.class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName,
            Annotation annotation, Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() == 0) {
                addMessage(context, propertyName, propertyDisplayName, annotation);
                return false;
            }
        } else if (value instanceof String[]) {
            String[] array = (String[]) value;

            if (array.length == 0) {
                addMessage(context, propertyName, propertyDisplayName, annotation);
                return false;
            }
        } else {
            if (value == null) {
                addMessage(context, propertyName, propertyDisplayName, annotation);
                return false;
            }
        }
        return true;
    }

    /**
     * エラーメッセージを追加する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context コンテキスト
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param annotation アノテーション
     */
    private <T> void addMessage(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, Annotation annotation) {
        Required required = (Required) annotation;
        String selectedMessageId;
        if (required.messageId().length() > 0) {
            selectedMessageId = required.messageId();
        } else {
            selectedMessageId = messageId;
        }
        ValidationResultMessageUtil.addResultMessage(context, propertyName, selectedMessageId, propertyDisplayName);
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T>      context,
                                String                    propertyName,
                                Object                    propertyDisplayName,
                                final Map<String, Object> params,
                                Object                    value) {
        
        Required annotation = new Required() {
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }
            public String messageId() {
                String messageId = (String) params.get("messageId");
                return (messageId == null) ? ""
                                           : messageId;
            }
        };
    
        return validate(context, propertyName, propertyDisplayName, annotation, value);
    }
}
