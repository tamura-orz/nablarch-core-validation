package nablarch.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "データタイプ{パターン}"形式のフォーマット仕様を保持するクラス。
 * 
 * @author Kiyohito Itoh
 */
public class FormatSpec {

    /** 元々のフォーマット文字列 */
    private final String format;

    /** "データタイプ{パターン}"形式のデータタイプ */
    private final String dataType;

    /** "データタイプ{パターン}"形式のパターン */
    private final String pattern;

    /** パターンのフォーマット */
    private final String formatOfPattern;

    /** パターンの付加情報 */
    private final String additionalInfoOfPattern;

    /** パターンの末尾に付加情報を含める場合のセパレータ */
    private final String patternSeparator;

    /**
     * コンストラクタ。
     * @param format 元々のフォーマット文字列
     * @param dataType "データタイプ{パターン}"形式のデータタイプ
     * @param pattern "データタイプ{パターン}"形式のパターン
     * @param formatOfPattern パターンのフォーマット
     * @param additionalInfoOfPattern パターンの付加情報
     * @param patternSeparator パターンの末尾に付加情報を含める場合のセパレータ
     */
    public FormatSpec(String format, String dataType, String pattern,
                       String formatOfPattern, String additionalInfoOfPattern,
                       String patternSeparator) {
        this.format = format;
        this.dataType = dataType;
        this.pattern = pattern;
        this.formatOfPattern = formatOfPattern;
        this.additionalInfoOfPattern = additionalInfoOfPattern;
        this.patternSeparator = patternSeparator;
    }

    /**
     * 元々のフォーマット文字列を取得する。
     * @return 元々のフォーマット文字列
     */
    public String getFormat() {
        return format;
    }

    /**
     * "データタイプ{パターン}"形式のデータタイプを取得する。
     * @return "データタイプ{パターン}"形式のデータタイプ
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * "データタイプ{パターン}"形式のパターンを取得する。
     * @return "データタイプ{パターン}"形式のパターン
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * パターンのフォーマットを取得する。
     * @return パターンのフォーマット
     */
    public String getFormatOfPattern() {
        return formatOfPattern;
    }

    /**
     * パターンの付加情報を取得する。
     * @return パターンの付加情報
     */
    public String getAdditionalInfoOfPattern() {
        return additionalInfoOfPattern;
    }

    /**
     * パターンの末尾に付加情報を含める場合のセパレータを取得する。
     * @return パターンの末尾に付加情報を含める場合のセパレータ
     */
    public String getPatternSeparator() {
        return patternSeparator;
    }

    /**
     * "データタイプ{パターン}"形式の文字列を返す。
     * @return "データタイプ{パターン}"形式の文字列
     */
    @Override
    public String toString() {
        return String.format("%s{%s%s%s}", dataType,
                              StringUtil.hasValue(formatOfPattern) ? formatOfPattern : "",
                              StringUtil.hasValue(patternSeparator) ? patternSeparator : "",
                              StringUtil.hasValue(additionalInfoOfPattern) ? additionalInfoOfPattern : "");
    }

    /** "データタイプ{パターン}"形式のパターン */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(.+?)(\\{(.+?)\\}(.*?))?$");

    /**
     * "データタイプ{パターン}"形式のフォーマット仕様を保持する{@link FormatSpec}を生成する。
     * @param format "データタイプ{パターン}"形式のフォーマット文字列
     * @param patternSeparator パターンの末尾に付加情報を含める場合のセパレータ
     * @return {@link FormatSpec}
     */
    public static FormatSpec valueOf(String format, String patternSeparator) {

        if (StringUtil.isNullOrEmpty(format)) {
            throw new IllegalArgumentException("format is null or blank.");
        }

        Matcher m = FORMAT_PATTERN.matcher(format);
        m.matches();

        String dataType = m.group(1);
        String pattern = m.group(3);
        String formatOfPattern = null;
        String additionalInfoOfPattern = null;
        if (StringUtil.hasValue(pattern)) {
            int separatorIndex = StringUtil.hasValue(patternSeparator)
                                            ? pattern.indexOf(patternSeparator) : -1;
            if (separatorIndex == -1) {
                formatOfPattern = pattern;
            } else {
                formatOfPattern = pattern.substring(0, separatorIndex);
                additionalInfoOfPattern = pattern.substring(separatorIndex + 1);
            }
        }

        return new FormatSpec(format, trim(dataType), trim(pattern),
                               trim(formatOfPattern), trim(additionalInfoOfPattern), patternSeparator);
    }

    /**
     * 指定された文字列をトリムする。
     * @param str 文字列
     * @return トリム後の文字列
     */
    private static String trim(String str) {
        return str != null ? str.trim() : null;
    }
}
