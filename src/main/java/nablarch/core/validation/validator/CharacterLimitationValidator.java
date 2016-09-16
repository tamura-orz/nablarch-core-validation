package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;



/**
 * 有効な文字以外が含まれないかをチェックするバリデータの作成を助けるサポートクラス。
 * 
 * @param <A> 対応するアノテーションの型
 * 
 * @author Koichi Asano
 *
 */
public abstract class CharacterLimitationValidator<A extends Annotation> extends StringValidatorSupport<A> {

    /**
     * コンストラクタ。
     */
    @Published(tag = "architect")
    protected CharacterLimitationValidator() {
    }

    /**
     * 有効文字以外が入力された場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String messageId;

    /**
     * 有効文字以外が入力された場合のデフォルトのエラーメッセージのメッセージIDを設定する。
     * 
     * @param messageId 有効文字以外が入力された場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override 
    public <T> boolean validateSingleValue(ValidationContext<T> context,
            String propertyName, Object propertyDisplayObject,
            A annotation, String value) {
        if (!isValid(annotation, value)) {
            String messageIdFromAnnotation = getMessageIdFromAnnotation(annotation);
            if (!StringUtil.isNullOrEmpty(messageIdFromAnnotation)) {
                ValidationResultMessageUtil.addResultMessage(context, propertyName, messageIdFromAnnotation, propertyDisplayObject);
            } else {
                ValidationResultMessageUtil.addResultMessage(context, propertyName, messageId, propertyDisplayObject);
            }
            return false;
        }
        return true;
    }
    /**
     * 有効文字以外が入力されていないかをチェックする。
     * @param annotation アノテーション
     * @param value バリデーション対象の値
     * @return 有効文字以外が入力されていない場合true
     */
    @Published(tag = "architect")
    protected abstract boolean isValid(A annotation, String value);

    /**
     * アノテーションからメッセージIDを取得する。
     * 
     * @param annotation メッセージIDを取得するアノテーション
     * @return アノテーションから取得したメッセージID
     */
    @Published(tag = "architect")
    protected abstract String getMessageIdFromAnnotation(A annotation);
}
