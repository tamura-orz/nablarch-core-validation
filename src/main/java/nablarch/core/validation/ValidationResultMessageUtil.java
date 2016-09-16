package nablarch.core.validation;


import nablarch.core.util.annotation.Published;

/**
 * 業務エラー時のメッセージ生成をサポートするユーティリティクラス。
 * 
 * @author Koichi Asano
 *
 */
public final class ValidationResultMessageUtil {

    /**
     * 隠蔽コンストラクタ。
     */
    private ValidationResultMessageUtil() {
        
    }

    /**
     * バリデーション結果メッセージを{@link ValidationContext}に追加する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context {@link ValidationContext}
     * @param propertyName プロパティ名
     * @param messageId エラーメッセージのメッセージID
     * @param displayPropertyName プロパティの表示名オブジェクト
     * @param params オプションパラメータ
     */
    @Published(tag = "architect")
    public static <T> void addResultMessage(ValidationContext<T> context, String propertyName, String messageId, Object displayPropertyName, Object... params) {
        Object[] propertyAdded = new Object[params.length + 1];
        propertyAdded[0] = displayPropertyName;
        System.arraycopy(params, 0, propertyAdded, 1, params.length);
        context.addResultMessage(propertyName, messageId, propertyAdded);
    }
}
