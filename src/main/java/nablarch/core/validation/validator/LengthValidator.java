package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;


/**
 * 文字列長をチェックするクラス。
 * 
 * @author Koichi Asano
 *
 */
public class LengthValidator extends StringValidatorSupport<Length> {

    /**
     * 最大文字列長を越えるエラーが発生した際に、最小文字列が指定されていなかった場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String maxMessageId;

    /**
     * 最大文字列長を越えるエラーが発生した際に、最小文字列が指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String maxAndMinMessageId;

    /**
     * 固定桁数の文字列チェック(maxとminに同じ値を設定した場合)でエラーが発生した際のデフォルトのメッセージID。
     */
    private String fixLengthMessageId;

    /**
     * 最大文字列長を越えるエラーが発生した際に、最小文字列が指定されていなかった場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{2}文字以下で入力してください。"
     * 
     * @param maxMessageId 最大文字列長を越えるエラーメッセージのデフォルトのメッセージID
     */
    public void setMaxMessageId(String maxMessageId) {
        this.maxMessageId = maxMessageId;
    }
    
    /**
     * 最大文字列長を越えるエラーが発生した際に、最小文字列が指定されていた場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}文字以上{2}文字以下で入力してください。"
     * @param maxAndMinMessageId 最大文字列長を越えるエラーが発生した際に、最小文字列が指定されていた場合のメッセージのデフォルトのメッセージID
     */
    public void setMaxAndMinMessageId(String maxAndMinMessageId) {
        this.maxAndMinMessageId = maxAndMinMessageId;
    }
    
    /**
     * 固定桁数の文字列チェック(maxとminに同じ値を設定した場合)でエラーが発生した際のデフォルトのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}文字で入力してください。"
     * @param fixLengthMessageId 固定桁数の文字列チェック(maxとminに同じ値を設定した場合)でエラーが発生した際のデフォルトのメッセージID
     */
    public void setFixLengthMessageId(String fixLengthMessageId) {
        this.fixLengthMessageId = fixLengthMessageId;
    }
    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> getAnnotationClass() {
        return Length.class;
    }

    /**
     * {@inheritDoc}<br/>
     * 文字列長チェックのバリデーションを行なう。
     * 
     */
    public <T> boolean validateSingleValue(ValidationContext<T> context, String propertyName,
            Object propertyDisplayName, Length length, String value) {
        // 文字列長 0 は @Required で防ぐ前提であるため、無条件で許可する
        // 例えば文字列長が 0 (入力なし) または 8 のみを許可するために使用する
        if (length.min() > 0 && value.length() != 0) {
            if (value.length() < length.min()) {
                addMessage(context, propertyName, propertyDisplayName, length);
                return false;
            }
        }

        if (length.max() > 0) {
            if (value.length() > length.max()) {
                addMessage(context, propertyName, propertyDisplayName, length);
                return false;
            }
        }
        return true;
    }

    /**
     * 最大文字列長以上のエラーメッセージを設定する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param length Lengthアノテーション
     */
    private <T> void addMessage(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, Length length) {
        String messageId;
        if (length.messageId().length() > 0) {
            messageId = length.messageId();
        } else {
            if (length.min() == length.max()) {
                messageId = fixLengthMessageId;
            } else if (length.min() > 0) {
                messageId = maxAndMinMessageId;
            } else {
                messageId = maxMessageId;
            }
        }
        ValidationResultMessageUtil.addResultMessage(context, propertyName, messageId, propertyDisplayName, length.min(), length.max());
    }

    @Override
    public Length createAnnotation(final Map<String, Object> params) {
        return  new Length() {
            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }
            public int min() {
                Integer min = (Integer) params.get("min");
                return (min == null || min < 0) ? 0
                                                : min;
            }

            public int max() {
                Integer max = (Integer) params.get("max");
                if (max == null) {
                    throw new IllegalArgumentException(
                    "max must be assigned to execute the validation of @Length."
                    );
                }
                return max;
            }

            public String messageId() {
                String messageId = (String) params.get("messageId");
                return (messageId == null) ? ""
                                           : messageId;
            }
        };
    }
}
