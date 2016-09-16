package nablarch.core.validation;

import java.util.Map;

import nablarch.core.util.annotation.Published;

/**
 * {@link Validator} をメソッド内の直接呼び出しに対応させる場合に実装する
 * インターフェース。
 */
@Published(tag = "architect")
public interface DirectCallableValidator extends Validator {
    /**
     * バリデーションを実行する。<br/>
     * 対応するチェックの結果がNGであった場合、ValidationContextにエラーメッセージを追加し、falseを返す。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context バリデーションコンテキスト
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param params バリデーション処理に対するパラメータを格納したMap (アノテーションの属性と同内容)
     * @param value バリデーション対象の値
     * 
     * @return バリデーションに通った場合true
     */    
    <T> boolean validate(ValidationContext<T> context, String propertyName, Object propertyDisplayName, Map<String, Object> params, Object value);
}
