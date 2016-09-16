package nablarch.core.validation;

import java.util.List;

import nablarch.core.cache.StaticDataLoader;


/**
 * フォームに紐付けられたバリデーションの設定をロードするクラス。<br/>
 * オンデマンドロードのみに対応する。
 * 
 * @author Koichi Asano
 *
 */
public class FormValidationDefinitionLoader implements StaticDataLoader<FormValidationDefinition> {

    /**
     * {@inheritDoc}
     */
    public Object getId(FormValidationDefinition value) {
        // オンデマンドロードの対応のみなので、使用されない。
        return value.getClass();
    }

    /**
     * {@inheritDoc}
     */
    public Object generateIndexKey(String indexName,
            FormValidationDefinition value) {
        // オンデマンドロードの対応のみなので、使用されない。
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getIndexNames() {
        // オンデマンドロードの対応のみなので、使用されない。
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public FormValidationDefinition getValue(Object id) {
        
        return new FormValidationDefinition((Class<?>) id);
    }

    /**
     * {@inheritDoc}
     */
    public List<FormValidationDefinition> getValues(String indexName,
            Object key) {
        // オンデマンドロードの対応のみなので、使用されない。
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<FormValidationDefinition> loadAll() {
        // オンデマンドロードの対応のみなので、使用されない。
        return null;
    }
}
