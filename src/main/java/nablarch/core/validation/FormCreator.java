package nablarch.core.validation;

import java.util.Map;

/**
 * フォームを生成するインタフェース。
 * 
 * @author Koichi Asano
 *
 */
public interface FormCreator {

    /**
     * フォームを作成する。
     * 
     * @param <T> 作成するフォームの型
     * @param targetClass フォームのクラス
     * @param propertyValues フォームのプロパティにセットする値のマップ
     * @param formValidationDefinition FormValidationDefinition
     * @return 生成し、プロパティがセットされたフォーム
     */
    <T> T create(Class<T> targetClass, Map<String, Object> propertyValues, FormValidationDefinition formValidationDefinition);
}
