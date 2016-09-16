package nablarch.core.validation.validator.unicode;

import java.util.BitSet;

/**
 * 許容文字かどうかの判定結果をキャッシュする{@link CharsetDef}実装クラス。<br/>
 * 他の{@link CharsetDef}実装クラスにラップして使用することで、
 * 判定処理に要する処理速度を改善できる。
 *
 * @author T.Kawasaki
 */
public class CachingCharsetDef extends CharsetDefSupport {

    /**
     * 文字（コードポイント）の最大数。
     * {@link BitSet}はスレッドセーフでないため、初期状態で十分な容量を確保し
     * 使用領域に拡張が発生しないようにする。
     */
    private static final int MAX = Character.MAX_CODE_POINT - Character.MIN_CODE_POINT + 1;

    /** 許可文字のキャッシュ */
    private final BitSet allowed = new BitSet(MAX);

    /** 不許可許可文字のキャッシュ */
    private final BitSet rejected = new BitSet(MAX);

    /** 実際の許容文字定義 */
    private CharsetDef charsetDef;

    /**
     * 許容文字集合定義を設定する。
     *
     * @param charsetDef 許容文字集合定義
     */
    public void setCharsetDef(CharsetDef charsetDef) {
        this.charsetDef = charsetDef;
    }

    /** {@inheritDoc} */
    public synchronized boolean contains(int codePoint) {
        // キャッシュから取得を試みる。
        if (allowed.get(codePoint)) {
            return true;
        } else if (rejected.get(codePoint)) {
            return false;
        }
        // 委譲先に問い合わせ
        CharsetDef delegate = getDelegate();
        boolean result = delegate.contains(codePoint);
        // 問い合わせ結果をキャッシュする。
        if (result) {
            allowed.set(codePoint);
        } else {
            rejected.set(codePoint);
        }
        return result;
    }

    /**
     * 委譲先の許容文字集合定義を取得する。
     *
     * @return 許容文字集合定義
     * @throws IllegalStateException 委譲先の許容文字集合定義が設定されていな場合
     */
    private CharsetDef getDelegate() throws IllegalStateException {
        if (charsetDef == null) {
            throw new IllegalStateException(
                    "charsetDef must be set.");
        }
        return charsetDef;
    }

}
