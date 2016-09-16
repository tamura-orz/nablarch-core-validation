package nablarch.core.validation;

import java.lang.annotation.Annotation;
import nablarch.core.util.annotation.Published;

/**
 * 値のバリデーションを行うクラスが実装すべきメソッドを定義したインタフェース。
 * 
 * @author Koichi Asano
 *
 */
@Published(tag = "architect")
public interface Validator {

    /**
     * 対応するアノテーションのクラスを取得する。
     * 
     * @return 対応するアノテーションのクラス
     */
    Class<? extends Annotation> getAnnotationClass();

    /**
     * バリデーションを実行する。<br/>
     * 対応するチェックの結果がNGであった場合、ValidationContextにエラーメッセージを追加し、falseを返す。
     * 
     * @param <T> バリデーション結果で取得できる型 
     * @param context バリデーションコンテキスト
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param annotation アノテーション
     * @param value バリデーション対象の値
     * 
     * @return バリデーションに通った場合true
     */
    <T> boolean validate(ValidationContext<T> context, String propertyName, Object propertyDisplayName, Annotation annotation, Object value);
}
