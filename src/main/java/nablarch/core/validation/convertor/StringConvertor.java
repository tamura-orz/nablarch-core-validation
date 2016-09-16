package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;
import java.util.List;

import nablarch.core.util.Builder;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Convertor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 値をStringに変換するクラス。
 * <br/>
 * <p/>
 * Stringに変換可能なオブジェクトは、下記クラスのみ
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link String}[]</li>
 * </ul>
 *
 * 
 * <p>
 * <b>トリム仕様</b>
 * <p>
 * 変換可否の判定および変換の際に値の前後の空白文字のトリムを行うか否かを選択することができる。<br/>
 * トリムは、trimPolicyプロパティに設定されたポリシーにしたがって行う。<br/>
 * ポリシーとして設定可能な値を以下に示す。<br/>
 * <ul>
 * <li><b>"trimAll"</b>: すべての文字に対してトリムを行う場合に設定するポリシー </li>
 * <li><b>"noTrim"</b>: すべての文字に対してトリムを行わない場合に設定するポリシー </li>
 * </ul>
 * trimPolicyプロパティにポリシーが設定されなかった場合はトリムを行わない（"noTrim"が設定された場合と同様の動作となる）。
 * </p>
 * </p>
 * 
 * @author Koichi Asano
 */
public class StringConvertor implements Convertor {

    /**
     * コンストラクタ。
     */
    @Published(tag = "architect")
    public StringConvertor() {
    }
    
    /** アノテーションの有無に関係なく、必ずすべての文字に対してトリムを行うポリシー */
    private static final String TRIM_ALL = "trimAll";

    /** トリムを行わないポリシー */
    private static final String NO_TRIM = "noTrim";

    /**
     * 変換失敗時のデフォルトのエラーメッセージのメッセージID。
     */
    private String conversionFailedMessageId;

    /**
     * 変換対象の値にnullを許可するか否か。
     */
    private boolean allowNullValue = true;

    /**
     * ネストするコンバータ。
     */
    private List<ExtendedStringConvertor> extendedStringConvertors;
    
    /** トリムポリシー */
    private String trimPolicy = null;
    
    /**
     * トリムポリシーを設定する。
     * 
     * @param trimPolicy トリムポリシー
     */
    public void setTrimPolicy(String trimPolicy) {
        
        if (StringUtil.isNullOrEmpty(trimPolicy)) {
            throw new IllegalArgumentException(Builder.concat(
                    "invalid property value was specified."
                  , " 'trimPolicy' property must not be empty."
                  , " supported trim policy name=[\""
                  , TRIM_ALL, "\", \"", NO_TRIM, "\"]."));
        }
        
        if (!(TRIM_ALL.equals(trimPolicy) || NO_TRIM.equals(trimPolicy))) {
            throw new IllegalArgumentException(Builder.concat(
                    "invalid property value was specified."
                  , " '", trimPolicy , "' was not supported trim policy name."
                  , " supported trim policy name=[\""
                  , TRIM_ALL, "\", \"", NO_TRIM, "\"]."));
        }
        this.trimPolicy = trimPolicy;
    }
    
    /**
     * 拡張StringConvertorのリストを設定する。
     *
     * @param extendedStringConvertors 拡張StringConvertorのリスト
     */
    public void setExtendedStringConvertors(List<ExtendedStringConvertor> extendedStringConvertors) {

        this.extendedStringConvertors = extendedStringConvertors;
    }

    /**
     * 変換失敗時のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * デフォルトメッセージの例 : "{0}が正しくありません"
     *
     * @param conversionFailedMessageId 変換失敗時のデフォルトのエラーメッセージのメッセージID
     */
    public void setConversionFailedMessageId(String conversionFailedMessageId) {

        this.conversionFailedMessageId = conversionFailedMessageId;
    }

    /**
     * 変換対象の値にnullを許可するか否かを設定する。
     * <p/>
     * 設定を省略した場合、nullが許可される。
     *
     * @param allowNullValue nullを許可するか否か。許可する場合は、true
     */
    public void setAllowNullValue(boolean allowNullValue) {

        this.allowNullValue = allowNullValue;
    }

    /**
     * {@inheritDoc}
     */
    public <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation format) {

        if (value == null) {
            return null;
        } else if (value instanceof String[]) {
            value = ((String[]) value)[0];
        }
        
        // トリム
        String str = applyTrimPolicy((String) value, format);
        
        // 拡張コンバータ
        Convertor nestedConvertor = format == null ? null : getConvertorRelatedToFormat(format);
        return nestedConvertor == null ? str : nestedConvertor.convert(context, propertyName, str, format);
    }

    /**
     * formatに関連付けられているExtendedStringConvertor実装クラスを返す。
     *
     * @param format フォーマットを指定するアノテーション。
     * @return formatに関連付けられているExtendedStringConvertor実装クラス。
     */
    private Convertor getConvertorRelatedToFormat(Annotation format) {

        for (ExtendedStringConvertor extended : extendedStringConvertors) {
            if (format.annotationType() == extended.getTargetAnnotation()) {
                return extended;
            }
        }
        throw new IllegalArgumentException("convertor related to " + format.annotationType() + " isn't specified.");
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getTargetClass() {

        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean isConvertible(ValidationContext<T> context,
                                     String propertyName, Object propertyDisplayName, Object value,
                                     Annotation format) {

        boolean convertible = false;
        if (value == null && allowNullValue) {
            return true;
        } else if (value instanceof String) {
            convertible = true;
        } else if (value instanceof String[]) {
            if (((String[]) value).length == 1) {
                value = ((String[]) value)[0];
                convertible = true;
            }
        }

        if (!convertible) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                                                        conversionFailedMessageId, propertyDisplayName);
            return false;
        }
        
        // 拡張コンバータ
        Convertor nestedConvertor = format == null ? null : getConvertorRelatedToFormat(format);
        
        if (nestedConvertor == null) {
            return true;
        } else {
            // トリム
            value = applyTrimPolicy((String) value, format);
            return nestedConvertor.isConvertible(context, propertyName, propertyDisplayName, value, format);
        }
    }        
    
    /**
     * trimPolicyプロパティに設定されたポリシーにしたがってトリムを実行する。
     * <p/>
     * <ul>
     * <li>ポリシーとして<b>"trimAll"</b>が設定された場合は、すべての文字に対してトリムを行う。</li>
     * <li>ポリシーとして<b>"noTrim"</b>が設定された場合、またはポリシーが設定されなかった場合は、トリムを行わない。</li>
     * </ul>
     * 
     * @param value トリム対象の文字列
     * @param annotation アノテーション
     * @return トリム後の文字列
     */
    @Published(tag = "architect")
    protected String applyTrimPolicy(String value, Annotation annotation) {
        if (TRIM_ALL.equals(trimPolicy)) {
            return trim(value);
        } else {
            return value;
        }
    }
    
    /**
     * トリムを実行する。
     * <p/>
     * Java標準の{@link String#trim()}を使用してトリムを行った文字列を返却する。
     * 
     * @param value トリム対象の文字列
     * @return トリム後の文字列
     */
    @Published(tag = "architect")
    protected String trim(String value) {
        return value.trim();
    }
}
