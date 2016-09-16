package nablarch.core.validation;

import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.StringResource;
import nablarch.core.util.annotation.Published;

import static nablarch.core.util.Builder.concat;

/**
 * バリデーション結果のメッセージを保持するクラス。
 * 
 * @author Koichi Asano
 *
 */
@Published
public class ValidationResultMessage extends Message {

    /**
     * バリデーション対象のプロパティ名。
     */
    private String propertyName;
    
    /**
     * {@code ValidationResultMessage}オブジェクトを構築する。
     * <p/>
     * メッセージの通知レベルは{@link MessageLevel#ERROR}が指定される。
     * 
     * @param propertyName バリデーション対象のプロパティ名
     * @param message バリデーション結果のメッセージ
     * @param parameters メッセージのオプションパラメータ
     */
    public ValidationResultMessage(String propertyName, StringResource message,
            Object[] parameters) {
        super(MessageLevel.ERROR, message, parameters);
        this.propertyName = propertyName;
    }

    /**
     * バリデーション対象のプロパティ名を取得する。
     * 
     * @return バリデーション対象のプロパティ名
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * このオブジェクトと等価であるかを返す。
     * <p/>
     * {@code obj}が以下の条件を全て満たす場合{@code true}を返す。
     * <ul>
     *     <li>{@code null}ではないこと。</li>
     *     <li>このオブジェクトと同じ型であること。</li>
     *     <li>メッセージIDが同値であること。</li>
     *     <li>バリデーション対象のプロパティ名が同値であること。</li>
     * </ul>
     * @return このオブジェクトと等価である場合{@code true}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ValidationResultMessage another = (ValidationResultMessage) obj;
        return getMessageId().equals(another.getMessageId())
                && getPropertyName().equals(another.getPropertyName());
    }

    /**
     * このオブジェクトのハッシュコード値を返す。
     * @return ハッシュコード値。メッセージIDとバリデーション対象プロパティが同値のオブジェクトは、同じハッシュコード値を返す。
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getMessageId().hashCode();
        hash = hash * 31 + getPropertyName().hashCode();
        return hash;
    }

    /**
     * このオブジェクトの文字列表現を返す。
     * @return メッセージIDとバリデーション対象プロパティを記載した文字列
     */
    @Override
    public String toString() {
        return concat(
                "messageId=[", getMessageId(), "] ",
                "propertyName=[", getPropertyName(), "]"
                );
    }


}
