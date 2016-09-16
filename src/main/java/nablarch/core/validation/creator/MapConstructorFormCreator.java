package nablarch.core.validation.creator;

import java.lang.reflect.Constructor;
import java.util.Map;

import nablarch.core.validation.FormCreator;
import nablarch.core.validation.FormValidationDefinition;


/**
 * Mapを引数に取るコンストラクタを使用してフォームを生成するクラス。<br/>
 * このストラテジを選択することで、リフクレクションを用いる場合と比較して高速なフォームの生成が行える。
 * 
 * @author Koichi Asano
 *
 */
public class MapConstructorFormCreator implements FormCreator {

    /**
     * {@inheritDoc}
     */
    public <T> T create(Class<T> targetClass,
            Map<String, Object> propertyValues, FormValidationDefinition formValidationDefinition) {
        
        try {
            Constructor<T> constructor = targetClass.getConstructor(Map.class);
            return constructor.newInstance(propertyValues);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity creation failed. " 
                    + "form class name = [" + targetClass.getName() + "] .", e);
        }
    }
}
