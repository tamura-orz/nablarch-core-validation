package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import nablarch.core.util.FormatSpec;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Convertor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;


/**
 * 数値のコンバータの作成を助けるサポートクラス。</br>
 * 数値のコンバータは変換前にバリデーションを行うが、各コンバータが行う共通バリデーションは当クラスにて行う。
 * 共通バリデーションの仕様は次の通りである。
 * <ul>
 *     <li>allowNullValueがfalseの時に、入力値がnullでないこと。</li>
 *     <li>入力値がNumber、String、String配列のいずれかのインスタンスであること。</li>
 *     <li>入力値がString配列である場合、要素数が1であること。</li>
 * </ul>
 *
 * <p>
 * <b>トリム仕様</b>
 * <p>
 * 値の前後の空白文字はトリムしてから変換可否の判定および変換を行う。<br/>
 * </p>
 * </p>
 *
 * @author Koichi Asano
 * @see Digits
 * @see BigDecimalConvertor
 * @see LongConvertor
 * @see IntegerConvertor
 */
public abstract class NumberConvertorSupport implements Convertor {

    /**
     * 小数部を指定しなかった場合の桁数不正時のデフォルトのエラーメッセージのメッセージID。
     */
    private String invalidDigitsIntegerMessageId;

    /**
     * 小数部を指定した場合の桁数不正時のデフォルトのエラーメッセージのメッセージID
     */
    private String invalidDigitsFractionMessageId;

    /**
     * 入力値に複数の文字列が設定された場合のデフォルトのエラーメッセージのメッセージIDを設定する。
     */
    private String multiInputMessageId;

    /** 変換対象の値にnullを許可するか否か */
    private boolean allowNullValue = true;

    /** 数字をあらわす正規表現 */
    private static final Pattern NUMBER = Pattern.compile("[0-9]");

    /**
     * 小数部を指定しなかった場合の桁数不正時のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * デフォルトメッセージの例 : "{0}には{1}桁以下の数値を入力してください。"
     *
     * @param invalidDigitsMessageId 小数部を指定しなかった場合の桁数不正時のデフォルトのエラーメッセージのメッセージID
     */
    public void setInvalidDigitsIntegerMessageId(String invalidDigitsMessageId) {
        this.invalidDigitsIntegerMessageId = invalidDigitsMessageId;
    }

    /**
     * 小数部を指定した場合の桁数不正時のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * デフォルトメッセージの例 : "{0}には整数部{1}桁以下、小数部{2}桁以下の数値を入力してください。"
     *
     * @param invalidDigitsFractionMessageId 小数部を指定した場合の桁数不正時のデフォルトのエラーメッセージのメッセージID
     */
    public void setInvalidDigitsFractionMessageId(
            String invalidDigitsFractionMessageId) {
        this.invalidDigitsFractionMessageId = invalidDigitsFractionMessageId;
    }

    /**
     * 入力値に複数の文字列が設定された場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * デフォルトメッセージの例 : "{0}の値が不正です。"
     *
     * @param multiInputMessageId 入力値に複数の文字列が設定された場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMultiInputMessageId(String multiInputMessageId) {
        this.multiInputMessageId = multiInputMessageId;
    }

    /**
     * 変換対象の値にnullを許可するか否かを設定する。
     *
     * 設定を省略した場合、nullが許可される。
     *
     * @param allowNullValue nullを許可するか否か。許可する場合は、true
     */
    public void setAllowNullValue(boolean allowNullValue) {
        this.allowNullValue = allowNullValue;
    }

    /**
     * 変換可否チェックのパターン。
     */
    private static Map<String, Pattern> patterns = new ConcurrentHashMap<String, Pattern>();

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean isConvertible(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, Object value,
            Annotation format) {
        // チェック対象の値の型が正しいか
        boolean isConvertibleType
                = (value == null && allowNullValue)
               || (value instanceof Number)
               || (value instanceof String)
               || (value instanceof String[] && ((String[]) value).length == 1);
        if (!isConvertibleType) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                    multiInputMessageId, propertyDisplayName);
            return false;
        }

        if (value == null) {
            // nullの場合は以降の処理は行わない。
            // nullを許可している場合のみ、ここまで処理がくる。
            return true;
        }

        if (!(format instanceof Digits)) {
            throw new IllegalArgumentException(
                    "Must specify @Digits annotation."
                            + "property = " + propertyName);
        }
        Digits digits = (Digits) format;
        DecimalFormatSymbols symbols = getDecimalFormatSymbols(context, propertyName);

        // チェック対象の値がパターンに合致しているか
        if (!isPatternMatched(digits, symbols, value)) {
            ValidationResultMessageUtil.addResultMessage(
                    context, propertyName, getMessageId(digits),
                    propertyDisplayName, digits.integer(),
                    digits.fraction());
            return false;
        }
        return true;
    }

    /**
     * バリデーション対象の値がパターンにマッチするかチェックする。
     *
     * @param digits アノテーション
     * @param symbols リクエストパラメータから判定した DecimalFormatSymbols
     * @param value バリデーション対象の値
     * @return パターンに合致する場合 true
     */
    private boolean isPatternMatched(Digits digits, DecimalFormatSymbols symbols, Object value) {
        Pattern pattern = getPattern(digits, symbols);
        String str = convertToString(value);

        if (!pattern.matcher(str).matches()) {
            // パターンに合致しない
            return false;
        }

        // 空文字列でなく、かつ数字が含まれていない場合は、変換不可とする。
        // 空文字列の場合は、convert時にnullが返却されるため、変換可として問題ない。
        return str.isEmpty() || NUMBER.matcher(str).find();
    }

    /**
     * 設定されたアノテーションにあったエラーメッセージのメッセージIDを選択する。
     *
     * @param digits アノテーション
     * @return 設定されたアノテーションにあったエラーメッセージのメッセージID
     */
    private String getMessageId(Digits digits) {
        boolean hasOwnMessageId = digits.messageId().length() != 0;
        if (hasOwnMessageId) {
            return digits.messageId();
        }
        return digits.fraction() == 0 // 整数か？
                ? invalidDigitsIntegerMessageId
                : invalidDigitsFractionMessageId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation format) {

        String str = convertToString(value);
        if (StringUtil.isNullOrEmpty(str)) {
            return null;
        }

        DecimalFormatSymbols symbols = getDecimalFormatSymbols(context, propertyName);
        try {
            return convertToPropertyType(convertToNumber(str, symbols));
        } catch (NumberFormatException ignore) {
            // 万が一、Numberへの変換に失敗した場合にも実行時例外が送出されないように修正。
            return null;
        }
    }

    /**
     * トリムおよびフォーマットを行った文字列を、プロパティの型のオブジェクトへ変換する。
     *
     * @param numberString トリムおよびフォーマットを行った文字列
     * @return プロパティの型のオブジェクト（数値型）
     */
    @Published(tag = "architect")
    protected abstract Number convertToPropertyType(String numberString);

    /**
     * パラメータのオブジェクトを文字列に変換する。
     *
     * 以下に該当しないオブジェクトの場合には、nullを返却する。
     * <ul>
     *     <li>String配列</li>
     *     <li>String</li>
     * </ul>
     *
     * 値の前後の空白文字はトリムする。
     *
     * @param value 変換対象のオブジェクト
     * @return 変換後の文字列
     */
    protected String convertToString(Object value) {
        String str;
        if (value instanceof String) {
            str = (String) value;
        } else if (value instanceof BigDecimal) {
        	str = ((BigDecimal) value).toPlainString();
        } else if (value instanceof Number) {
            str = value.toString();
        } else if (value instanceof String[]) {
            String[] arg = (String[]) value;
            if (arg.length == 1) {
                str = arg[0];
            } else {
                return null;
            }
        } else {
            return null;
        }

        // トリム
        return trim(str);
    }

    /**
     * チェックに使用する正規表現パターンを取得する。
     *
     * @param digits Digits
     * @param symbols 小数点に使用する文字や1000の区切り文字を提供する{@link java.text.DecimalFormatSymbols}
     * @return パターンを取得する。
     */
    private Pattern getPattern(Digits digits, DecimalFormatSymbols symbols) {

        String key = digits + symbols.toString();
        if (patterns.containsKey(key)) {
            return patterns.get(key);
        }

        synchronized (patterns) {
            // 事前にcontainsを見ているため、究極的なタイミングでないと到達しない。
            // よって分岐網羅できない。
            if (!patterns.containsKey(key)) {
                patterns.put(key, createPattern(digits, symbols));
            }
        }

        return patterns.get(key);
    }

    /**
     * Digitsに対応する正規表現を作成する。
     *
     * @param digits 数値フォーマット指定のアノテーション
     * @param symbols 小数点に使用する文字や1000の区切り文字を提供する{@link java.text.DecimalFormatSymbols}
     * @return フォーマットを表わすパターン
     */
    @Published(tag = "architect")
    protected abstract Pattern createPattern(Digits digits, DecimalFormatSymbols symbols);

    /**
     * プロパティに対する{@link java.text.DecimalFormatSymbols}を取得する。
     * <p/>
     * ConversionUtil#getFormatSpec(ValidationContext, String)を呼び出し、
     * 10進数に対する有効なフォーマット仕様(decimal)が取得できた場合は、
     * フォーマット仕様に設定された言語を使用して生成した{@link java.text.DecimalFormatSymbols}を返す。
     * <p/>
     * プロパティに対する有効なフォーマット仕様が存在しない場合は、
     * {@link #getDefaultDecimalFormatSymbols()}を呼び出し、
     * デフォルトの{@link java.text.DecimalFormatSymbols}を返す。
     *
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @return プロパティに対する{@link java.text.DecimalFormatSymbols}
     */
    protected <T> DecimalFormatSymbols getDecimalFormatSymbols(ValidationContext<T> context, String propertyName) {
        FormatSpec formatSpec = ConversionUtil.getFormatSpec(context, propertyName);
        if (formatSpec == null || !"decimal".equals(formatSpec.getDataType())) {
            return getDefaultDecimalFormatSymbols();
        }
        String language = formatSpec.getAdditionalInfoOfPattern();
        if (!StringUtil.hasValue(language)) {
            return getDefaultDecimalFormatSymbols();
        }
        Locale locale = new Locale(language);
        return new DecimalFormatSymbols(locale);
    }

    /**
     * デフォルトの{@link java.text.DecimalFormatSymbols}を返す。<br/>
     * デフォルト実装では日本語に対する{@link java.text.DecimalFormatSymbols}(小数点の文字=ドット、1000の区切り文字=カンマ)を返す。
     * @return 日本語に対する{@link java.text.DecimalFormatSymbols}(小数点の文字=ドット、1000の区切り文字=カンマ)
     */
    protected DecimalFormatSymbols getDefaultDecimalFormatSymbols() {
        return new DecimalFormatSymbols(Locale.JAPANESE);
    }

    /**
     * 言語に応じた数字を{@link Number}に変換可能な数字に変換する。
     * <p/>
     * ConversionUtil#convertToNumber(String, DecimalFormatSymbols)に処理を委譲する。
     *
     * @param number 数字
     * @param symbols 数字に使用されている小数点や1000の区切り文字を提供する{@link java.text.DecimalFormatSymbols}
     * @return {@link Number}に変換可能な数字
     */
    protected String convertToNumber(String number, DecimalFormatSymbols symbols) {
        return ConversionUtil.convertToNumber(number, symbols);
    }

    /**
     * トリムを実行する。
     * @param value トリム対象の文字列
     * @return トリム後の文字列
     */
    protected String trim(String value) {
        return value.trim();
    }
}
