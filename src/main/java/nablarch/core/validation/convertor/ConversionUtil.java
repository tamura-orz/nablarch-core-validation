package nablarch.core.validation.convertor;

import java.lang.reflect.Array;
import java.text.DecimalFormatSymbols;

import nablarch.core.util.FormatSpec;
import nablarch.core.validation.ValidationContext;
import nablarch.fw.ExecutionContext;

/**
 * コンバータのユーティリティクラス。
 * @author Kiyohito Itoh
 */
public final class ConversionUtil {

    /** 隠蔽コンストラクタ */
    private ConversionUtil() {
    }

    /**
     * 変換前文字列のMapからプロパティに対するフォーマット仕様を取得する。
     * <pre>
     * 
     * フォーマット仕様
     *   キー: プロパティ名＋"_nablarch_formatSpec"
     *   値  : "データタイプ{パターン}"形式のフォーマット文字列
     * パターンのセパレータ
     *   キー: name属性の値＋"_nablarch_formatSpec_separator"
     *   値  : パターンのセパレータ
     * 
     * </pre>
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @return プロパティに対するフォーマット仕様。存在しない場合はnull
     */
    public static <T> FormatSpec getFormatSpec(ValidationContext<T> context, String propertyName) {
        Object format = getSingleParameter(context, propertyName + "_" + ExecutionContext.FW_PREFIX + "formatSpec");
        if (format == null) {
            return null;
        }
        Object separator = getSingleParameter(context, propertyName + "_" + ExecutionContext.FW_PREFIX + "formatSpec_separator");
        return FormatSpec.valueOf(format.toString(), separator != null ? separator.toString() : null);
    }

    /**
     * 変換前文字列のMapから単一値を取得する。
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @return 単一値。存在しない場合はnull
     */
    private static <T> Object getSingleParameter(ValidationContext<T> context, String propertyName) {
        Object values = context.getParameters(propertyName);
        if (values == null || Array.getLength(values) == 0) {
            return null;
        }
        return Array.get(values, 0);
    }

    /**
     * 指定されたシンボルを正規表現の形式にエスケープする。
     * @param symbol シンボル
     * @return エスケープ後の文字列
     */
    public static String escapeForRegex(char symbol) {
        switch (symbol) {
            case '.':
                return "\\.";
            default:
                return String.valueOf(symbol);
        }
    }

    /**
     * 言語に応じた数字を{@link Number}に変換可能な数字に変換する。
     * <pre>
     * 変換内容は下記のとおり。
     * ・小数点をドットに変換する。
     * ・1000の区切り文字を削除する。
     * </pre>
     * @param number 数字
     * @param symbols 数字に使用されている小数点や1000の区切り文字を提供する{@link java.text.DecimalFormatSymbols}
     * @return {@link Number}に変換可能な数字
     */
    public static String convertToNumber(String number, DecimalFormatSymbols symbols) {
        number = number.replace(String.valueOf(symbols.getGroupingSeparator()), "");
        char point = symbols.getDecimalSeparator();
        return point == '.' ? number : number.replace(point, '.');
    }
}
