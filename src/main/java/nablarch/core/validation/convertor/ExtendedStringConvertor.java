package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Convertor;

/**
 * {@link StringConvertor}にてString変換後、さらに追加で変換を行うコンバータが実装するインタフェース。
 *
 * @author Tomokazu Kagawa
 */
@Published(tag = "architect")
public interface ExtendedStringConvertor extends Convertor {

    /**
     * 対応するアノテーションのクラスを取得する。
     *
     * @return 対応するアノテーションのクラス
     */
    Class<? extends Annotation> getTargetAnnotation();
}
