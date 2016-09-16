package nablarch.core.validation.validator.unicode;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.validator.CharacterLimitationValidator;

/**
 * システム許容文字のみからなる文字列であるかをチェックするクラス。
 * <p>
 *   {@link SystemChar}アノテーションで設定された許容文字であるかをバリデーションする。
 *
 *   <p>
 *     <b>使用するための設定</b>
 *   </p>
 *   許容文字集合 "smallLetter" を次のように定義する。
 *   許容文字集合の定義方法は、範囲指定やリテラル指定などいくつかあるので、詳細は{@link nablarch.core.validation.validator.unicode}パッケージのjavadocを参照。
 *   <pre>
 *     {@code <component name="smallLetter" class="nablarch.core.validation.validator.unicode.}{@link RangedCharsetDef}{@code ">
 *         <property name="startCodePoint" value="U+0061" />
 *         <property name="endCodePoint" value="U+007A" />
 *         <property name="messageId" value="MSG00002" />
 *     </component>}
 *   </pre>
 *   本バリデータを使用するには許容文字集合とメッセージIDのデフォルト値を設定する必要がある。
 *   次の例では上で定義した許容文字集合 "smallLetter" をデフォルトの許容文字集合に、
 *   メッセージID "MSG90001" をデフォルトのメッセージIDに設定する。
 *   <pre>
 *     {@code <component class="nablarch.core.validation.validator.unicode.SystemCharValidator">
 *         <!-- 定義した許容文字集合を設定 -->
 *         <property name="defaultCharsetDef" ref="smallLetter"/>
 *         <property name="messageId" value="MSG90001"/>
 *     </component>}
 *   </pre>
 *   
 *   <p>
 *     <b>プロパティごとの設定</b>
 *   </p>
 *   プロパティごとのバリデーションの内容を設定するには{@link SystemChar}アノテーションで設定する。
 *   バリデーション内容として、別途定義された許容文字集合 "passCharacter"、メッセージID "PASSWORD" を設定する場合は
 *   {@link SystemChar}アノテーションを次のように記述する。
 *   <pre>
 *     {@code @PropertyName("パスワード")}
 *     {@code @SystemChar(charsetDef = "passCharacter", messageId = "PASSWORD")
 *     public void setConfirmPassword(String confirmPassword) {
 *         this.confirmPassword = confirmPassword;
 *     }}
 *   </pre>
 *
 *   <p>
 *     <b>使用されるメッセージIDの優先順位</b>
 *   </p>
 *   メッセージIDは{@link SystemChar}, {@link CharsetDef}, {@link SystemCharValidator}のそれぞれで設定できるが、
 *   使用するメッセージIDの優先順は{@link SystemChar}, {@link CharsetDef}, {@link SystemCharValidator}の順となる。
 *   例えば、全てにメッセージIDが指定されていた場合は{@link SystemChar}で指定されているメッセージIDが使用される。
 * </p>
 *
 * @author T.Kawasaki
 */
public class SystemCharValidator extends CharacterLimitationValidator<SystemChar> {

    /**
     * サロゲートペアを許容するかどうか。
     * デフォルトは{@code false}(許容しない）。
     */
    private boolean allowSurrogatePair = false;

    /** デフォルトの許容文字集合定義 */
    private CharsetDef defaultCharsetDef;

    /** {@inheritDoc} */
    @Override
    protected boolean isValid(SystemChar annotation, String value) {
        CharsetDef def = getCharsetDefFrom(annotation);
        return CharsetDefValidationUtil.isValid(
                def,                               // 許容される文字集合の定義
                value,                             // バリデーション対象文字列
                annotation.allowLineSeparator(),   // 改行コードを許容するか
                allowSurrogatePair);               // サロゲートペアを許容するか
    }

    /**
     * アノテーションから許容文字集合定義クラスを取得する。
     * <p>
     * アノテーションにて許容文字集合定義の名称が指定されていない場合、
     * デフォルトの許容文字集合を使用する。
     * 明示的に指定されている場合はリポジトリから取得する。
     * </p>
     * @param annotation アノテーション
     * @return 許容文字集合定義クラス
     */
    private CharsetDef getCharsetDefFrom(SystemChar annotation) {
        // 許容文字集合の名称
        String charsetDefName = annotation.charsetDef();
        boolean useDefaultCharsetDef = StringUtil.isNullOrEmpty(charsetDefName);
        // 許容文字集合
        return useDefaultCharsetDef
                ? defaultCharsetDef
                : CharsetDefValidationUtil.lookUp(charsetDefName);
    }
    
    /** {@inheritDoc}
     * <p>
     * 指定された{@link SystemChar}がメッセージIDをを持つ場合は、
     * そのメッセージIDを返却する。
     * {@link SystemChar}がメッセージIDを持たない場合は、
     * {@link CharsetDef}が持つメッセージIDを返却する。
     * </p>
     */
    @Override
    protected String getMessageIdFromAnnotation(SystemChar annotation) {
        String messageId = annotation.messageId();
        if (!StringUtil.isNullOrEmpty(messageId)) {
            return messageId;
        }
        CharsetDef charsetDef = getCharsetDefFrom(annotation);
        return charsetDef.getMessageId();
    }

    /** {@inheritDoc} */
    public Class<? extends Annotation> getAnnotationClass() {
        return SystemChar.class;
    }

    /**
     * サロゲートペアを許容するかどうかを設定する。
     * <p>
     * デフォルトでは許容しない。サロゲートペアを許容すると、
     * 文字列のchar数({@link String#length()})と
     * コードポイント数({@link String#codePointCount(int, int)})が
     * 合致しない文字列を扱うことになり、影響が非常に広範に渡るため
     * デフォルトでは許容しない設定となっている。
     * </p>
     *
     * @param allowSurrogatePair サロゲートペアを許容する場合、真
     * @see String#codePointCount(int, int)
     * @see String#length()
     */
    public void setAllowSurrogatePair(boolean allowSurrogatePair) {
        this.allowSurrogatePair = allowSurrogatePair;
    }

    /**
     * デフォルトの許容文字集合定義を設定する。
     * <p>
     * {@link SystemChar#charsetDef()}が設定されていない場合、
     * この許容文字集合定義が使用される。
     * </p>
     *
     * @param defaultCharsetDef デフォルトの許容文字集合定義
     */
    public void setDefaultCharsetDef(CharsetDef defaultCharsetDef) {
        this.defaultCharsetDef = defaultCharsetDef;
    }

    /** {@inheritDoc} */
    @Override
    public SystemChar createAnnotation(final Map<String, Object> params) {
        return new SystemChar() {
            public Class<? extends Annotation> annotationType() {
                return SystemChar.class;
            }

            public String charsetDef() {
                String charsetDef = (String) params.get("charsetDef");
                return (charsetDef == null) ? ""
                                            : charsetDef;
            }

            public boolean allowLineSeparator() {
                Boolean allowLineSeparator = (Boolean) params.get("allowLineSeparator");
                return (allowLineSeparator == null) ? false
                                                    : allowLineSeparator;
            }
            
            public String messageId() {
                String messageId = (String) params.get("messageId");
                return (messageId == null) ? ""
                                           : messageId;
            }            
        };
    }
}
