package nablarch.core.validation.validator.unicode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.toHexString;

/**
 * Unicodeコードポイントの範囲による許容文字集合定義クラス。<br/>
 * コードポイントの開始位置と終了位置の範囲内が許容文字の集合となる。
 * コードポイントは、Unicode標準の U+n 表記で指定する。
 * <p>
 * 例えば、制御文字を除くASCII文字を定義したい場合、以下のようにプロパティを設定する。
 * <pre>
 * // \u0020-\u007E
 * {@code
 * Charset asciiWithoutControlCode = new RangedCharsetDef();
 * asciiWithoutControlCode.setStartCodePoint("U+0020");
 * asciiWithoutControlCode.setEndCodePoint("U+007F");
 * }
 * </pre>
 * <p/>
 * コンポーネント設定ファイルに定義する場合、以下の記述が等価となる。
 * <pre>
 * {@literal
 * <component name="asciiWithoutControlCode" class="nablarch.core.validation.validator.unicode.RangedCharsetDef">
 *   <property name="startCodePoint" value="U+0020" />
 *   <property name="endCodePoint" value="U+007F" />
 * </component>
 * }
 * </pre>
 * <p/>
 * 実行例を以下に示す。
 * <pre>
 * asciiWithoutControlCode.contains("abc012"); // -> true
 * asciiWithoutControlCode.contains("\t");     // -> false
 * </pre>
 * </p>
 *
 * @author T.Kawasaki
 */
public class RangedCharsetDef extends CharsetDefSupport {

    /** インスタンス変数が初期化前であることを示すための定数 */
    private static final int NOT_SET_YET = -1;

    /** 開始位置 */
    private int start = NOT_SET_YET;

    /** 終了位置 */
    private int end = NOT_SET_YET;

    /**
     * 開始位置のコードポイントを設定する。
     *
     * @param start 開始位置(U+n表記)
     * @throws IllegalArgumentException コードポイントが範囲外の場合
     * @throws IllegalStateException    開始終了位置の大小関係が逆転している場合
     */
    public void setStartCodePoint(String start) throws IllegalArgumentException, IllegalStateException {
        int cp = toCodePoint(start);
        checkRange(cp);
        this.start = cp;
        checkRelation();
    }

    /**
     * 終了位置のコードポイントを設定する。
     *
     * @param end 終了位置(U+n表記)
     * @throws IllegalArgumentException コードポイントが範囲外の場合
     * @throws IllegalStateException    開始終了位置の大小関係が逆転している場合
     */
    public void setEndCodePoint(String end) throws IllegalArgumentException, IllegalStateException {
        int cp = toCodePoint(end);
        checkRange(cp);
        this.end = cp;
        checkRelation();
    }


    /** コードポイント記法の正規表現 (U+n) */
    private static final Pattern UNICODE_CODE_POINT_EXP = Pattern.compile("U\\+([0-9A-Fa-f]{4,8})");

    /**
     * 文字列（U+n表記）をコードポイントに変換する。
     *
     * @param s 文字列
     * @return コードポイント
     */
    private int toCodePoint(String s) {
        Matcher m = UNICODE_CODE_POINT_EXP.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "code point must match U+n unicode notation.  value=[" + s + "]");
        }
        String group = m.group(1);
        return Integer.parseInt(group, 16);
    }

    /**
     * コードポイントの範囲をチェックする。
     *
     * @param codePoint チェック対象となるコードポイント
     * @throws IllegalArgumentException コードポイントが範囲外の場合
     */
    private void checkRange(int codePoint) throws IllegalArgumentException {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException(
                    "invalid code point. [" + toHexString(codePoint) + "]");
        }
    }

    /**
     * 開始終了位置の関係をチェックする。
     *
     * @throws IllegalStateException 開始終了位置の大小関係が逆転している場合
     */
    private void checkRelation() throws IllegalStateException {
        if (start == NOT_SET_YET || end == NOT_SET_YET) {
            return;  // プロパティ設定が未完なので、チェックしない
        }
        // 大小関係をチェック(start <= endであること)
        if (start > end) {
            throw new IllegalStateException(
                    "startCodePoint must be smaller than endCodePoint. "
                            + "start=[" + toHexString(start) + "] "
                            + "end=[" + toHexString(end) + "]");
        }
    }

    /** {@inheritDoc} */
    public boolean contains(int codePoint) {
        return start <= codePoint && codePoint <= end;
    }
}
