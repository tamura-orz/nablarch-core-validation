package nablarch.core.validation;

import java.util.Map;

import nablarch.core.util.annotation.Published;

/**
 * ValidationUtil でバリデーション可能なオブジェクトが実装するインタフェース。<br />
 * バリデーション対象のパラメータを Map で取得するメソッドを持つ。
 *
 *
 * @author Koichi Asano 
 *
 * @param <TParam> パラメータの型。
 */
@Published(tag = "architect")
public interface Validatable<TParam> {

    /**
     * バリデーションを行うパラメータのMapを返す。
     *
     * @return バリデーションを行うパラメータのMap
     */
    Map<String, TParam> getParamMap();
}
