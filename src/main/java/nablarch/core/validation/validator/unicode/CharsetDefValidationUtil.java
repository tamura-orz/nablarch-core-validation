package nablarch.core.validation.validator.unicode;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;

/**
 * 許容文字集合定義を用いたバリデーション用のユーティリティクラス。
 *
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public final class CharsetDefValidationUtil {

    /** プライベートコンストラクタ */
    private CharsetDefValidationUtil() {
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     * バリデーション対象文字がサロゲートペアまたは改行コードを含む場合は、非許容文字列と判定する。
     *
     * @param charsetDefName 許容される文字集合定義の名称
     *                       ({@link SystemRepository}に登録されていること)
     * @param value          バリデーション対象の文字列
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     * @throws IllegalArgumentException 許容される文字集合定義の名称が
     *                                  {@link SystemRepository}に登録されていない場合。
     */
    public static boolean isValid(String charsetDefName, String value)
            throws IllegalArgumentException {
        return isValid(charsetDefName, value, false);
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     * バリデーション対象文字がサロゲートペアを含む場合は非許容文字列と判定する。
     *
     * @param charsetDefName     許容される文字集合定義の名称
     *                           ({@link SystemRepository}に登録されていること)
     * @param value              バリデーション対象の文字列
     * @param allowLineSeparator 改行コードを許容するか
     *                           （改行コードと認識するのは、\r(CR)と\n(LF)）
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     * @throws IllegalArgumentException 許容される文字集合定義の名称が
     *                                  {@link SystemRepository}に登録されていない場合。
     */
    public static boolean isValid(String charsetDefName,
                                  String value,
                                  boolean allowLineSeparator) throws IllegalArgumentException {

        return isValid(charsetDefName, value, allowLineSeparator, false);
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     *
     * @param charsetDefName     許容される文字集合定義の名称
     *                           ({@link SystemRepository}に登録されていること)
     * @param value              バリデーション対象の文字列
     * @param allowLineSeparator 改行コードを許容するか
     *                           (改行コードと認識するのは、\r(CR)と\n(LF)）
     * @param allowSurrogatePair サロゲートペアを許容するか
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     * @throws IllegalArgumentException 許容される文字集合定義の名称が
     *                                  {@link SystemRepository}に登録されていない場合
     */
    public static boolean isValid(String charsetDefName,
                                  String value,
                                  boolean allowLineSeparator,
                                  boolean allowSurrogatePair) throws IllegalArgumentException {

        CharsetDef charsetDef = lookUp(charsetDefName);
        return isValid(charsetDef, value, allowLineSeparator, allowSurrogatePair);
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     * バリデーション対象文字がサロゲートペアまたは改行コードを含む場合は、
     * 非許容文字列と判定する。
     *
     * @param charsetDef 許容される文字集合の定義
     * @param value      バリデーション対象の文字列
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     */
    public static boolean isValid(CharsetDef charsetDef, String value) {
        return isValid(charsetDef, value, false);
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     * バリデーション対象文字がサロゲートペアを含む場合は非許容文字列と判定する。
     *
     * @param charsetDef         許容される文字集合の定義
     * @param value              バリデーション対象の文字列
     * @param allowLineSeparator 改行コードを許容するか
     *                           (改行コードと認識するのは、\r(CR)と\n(LF)）
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     */
    public static boolean isValid(CharsetDef charsetDef, String value, boolean allowLineSeparator) {
        return isValid(charsetDef, value, allowLineSeparator, false);
    }

    /**
     * 文字列が許容されるかどうか判定する。<br/>
     * バリデーション対象の文字列の各文字が、許容される文字集合に含まれていることを確認する。
     *
     * @param charsetDef         許容される文字集合の定義
     * @param value              バリデーション対象の文字列
     * @param allowLineSeparator 改行コードを許容するか
     *                           (改行コードと認識するのは、\r(CR)と\n(LF)）
     * @param allowSurrogatePair サロゲートペアを許容するか
     * @return バリデーション対象の全文字が許容される場合、{@code true}
     */
    public static boolean isValid(CharsetDef charsetDef,
                                  String value,
                                  boolean allowLineSeparator,
                                  boolean allowSurrogatePair) {

        for (int i = 0, length = value.length(); i < length; i++) {

            int codePoint = value.codePointAt(i);      // バリデーション対象のコードポイント

            // ----- サロゲートペアのチェック ----- //
            boolean isSurrogatePair = false;
            boolean isLast = (i == length - 1);
            if (!isLast && Character.isHighSurrogate(value.charAt(i))) {
                isSurrogatePair = true;
                i++;    // LowSurrogateをスキップ
            }
            if (isSurrogatePair && !allowSurrogatePair) {
                return false; // サロゲートは許容しない(デフォルト）
            }

            // ----- 改行コードチェック ----- //
            if (isLineSeparator(codePoint)) {
                if (allowLineSeparator) {
                    continue;  // 改行を許容する（次の文字へ）
                }
                return false;  // 改行を許容しない
            }

            // ----- 許容文字かどうか判定 ----- //
            if (!charsetDef.contains(codePoint)) {
                return false;  // 許容されない文字が発見されたら終了
            }
        }
        return true;
    }

    /**
     * 指定されたコードポイントが改行コード（U+000DまたはU+000A)かどうか判定する。
     *
     * @param codePoint コードポイント
     * @return 指定されたコードポイントが改行コードの場合、{@code true}
     */
    private static boolean isLineSeparator(int codePoint) {
        return codePoint == 0x0D || codePoint == 0x0A;
    }

    /**
     * 許容文字集合定義の名称をキーにシステムリポジトリからインスタンスを取得する。
     *
     * @param name 名称
     * @return インスタンス
     * @throws IllegalArgumentException システムリポジトリに登録されていない場合
     */
    static CharsetDef lookUp(String name) throws IllegalArgumentException {
        CharsetDef def = SystemRepository.get(name);
        if (def == null) {
            throw new IllegalArgumentException(
                    "specified CharsetDef is not registered in SystemRepository. "
                            + "name=[" + name + "]");
        }
        return def;
    }
}
