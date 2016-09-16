package nablarch.core.validation.validator.unicode;

import java.util.BitSet;

/**
 * リテラル文字列による許容文字集合定義クラス。<br/>
 * 定義したい文字集合の要素が、Unicodeコードポイント上に散在する場合、
 * 範囲指定による集合定義は煩雑になるおそれがある。
 * そのような場合には、本クラスを利用することで簡便に文字集合を定義できる。
 * <pre>
 * {@code
 * // 1と3を許可
 * LiteralCharsetDef oneAndThree = new LiteralCharsetDef();
 * oneAndThree.setAllowedCharacters("13");
 * }
 * </pre>
 *
 * @author T.Kawasaki
 */
public class LiteralCharsetDef extends CharsetDefSupport {

    /** 許容可否 */
    private BitSet bitSet;

    /** {@inheritDoc} */
    public boolean contains(int codePoint) {
        return bitSet.get(codePoint);
    }

    /**
     * 許容文字を設定する。
     * 許容文字がBMPの範囲外にあり表示や入力に難がある場合は、U+n表記を使用するとよい。
     * 例えば、ホッケ(U+29E3D)の場合は&#x005C;uD867&#x005C;uDE3Dのように入力する。
     *
     * @param allowedCharacters 許容文字
     * @return 本インスタンス自身
     */
    public LiteralCharsetDef setAllowedCharacters(String allowedCharacters) {
        bitSet = new BitSet(allowedCharacters.length());
        addAllowedCharacters(allowedCharacters);
        return this;
    }

    /**
     * 許容文字を追加する。
     *
     * @param allowedCharacters 許容文字
     */
    private void addAllowedCharacters(String allowedCharacters) {
        for (int i = 0; i < allowedCharacters.length(); i++) {
            int codePoint = allowedCharacters.codePointAt(i);
            bitSet.set(codePoint);
            // サロゲートペアのチェック
            if (Character.isHighSurrogate(allowedCharacters.charAt(i))) {
                i++;   // LowSurrogateを飛ばす
            }
        }
    }
}
