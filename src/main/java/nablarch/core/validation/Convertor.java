package nablarch.core.validation;

import java.lang.annotation.Annotation;

import nablarch.core.util.annotation.Published;

/**
 * 入力値から対応するプロパティの型に変換するインタフェース。
 * 
 * @author Koichi Asano
 *
 */
@Published(tag = "architect")
public interface Convertor {

    /**
     * 変換対象のクラスを取得する。
     * 
     * @return 変換対象のクラス
     */
    Class<?> getTargetClass();

    /**
     * 変換可否のプレチェックを行う。<br/>
     * 変換できない文字列であった場合、エラーメッセージをValidationContextに追加し、falseを返却する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param value 変換可否のプレチェックを行う値
     * @param format フォーマットを指定するアノテーション（指定がない場合null)
     * @return 変換できる場合true
     */
    <T> boolean isConvertible(ValidationContext<T> context, String propertyName, Object propertyDisplayName, Object value, Annotation format);

    /**
     * 変換を行う。<br/>
     * 変換に失敗した場合、ValidationContextにエラー内容を設定する。
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @param value 変換する値(データ型は様々な形式がありえる。)
     * @param format フォーマットを指定するアノテーション（指定がない場合null)
     * @return 変換結果のオブジェクト
     */
    <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation format);
}
