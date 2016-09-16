package nablarch.core.validation.validator.unicode;

import nablarch.core.util.annotation.Published;

/**
 * {@link CharsetDef}実装クラスをサポートするクラス。
 * <p/>
 * 本クラスは、{@link CharsetDef}の許容文字範囲外だった場合に使用するメッセージを取得する
 * メッセージIDを保持する機能のみを提供する。
 *
 * @author hisaaki sioiri
 */
@Published(tag = "architect")
public abstract class CharsetDefSupport implements CharsetDef {

    /** メッセージID */
    private String messageId;

    /**
     * {@inheritDoc}
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 文字種チェックでエラーが発生した際にデフォルトで使用するメッセージIDを設定する。
     * @param messageId 文字種チェックでエラーが発生した際にデフォルトで使用するメッセージID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
