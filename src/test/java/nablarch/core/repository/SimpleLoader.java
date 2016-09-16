package nablarch.core.repository;

import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * テスト用に、簡易的に{@link SystemRepository}を初期化する為の{@link ObjectLoader}実装クラス。
 *
 * @author T.Kawasaki
 */
public class SimpleLoader extends HashMap<String, Object> implements ObjectLoader {

    /**
     * コンポーネントを追加する。
     *
     * @param name      コンポーネント名
     * @param component コンポーネント
     * @return 本インスタンス
     */
    public SimpleLoader add(String name, Object component) {
        put(name, component);
        return this;
    }

    /** 現在のインスタンスの状態で{@link SystemRepository}を初期化する。 */
    public void register() {
        SystemRepository.load(this);
    }

    /** {@inheritDoc} */
    public Map<String, Object> load() {
        return this;
    }
}
