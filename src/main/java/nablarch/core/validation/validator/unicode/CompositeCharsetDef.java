package nablarch.core.validation.validator.unicode;

import java.util.List;

/**
 * 複数の{@link CharsetDef}の組み合わせによる許容文字集合定義クラス。<br/>
 * 本クラスでは、他の許容文字集合定義の組み合わせにより文字集合を定義できる。
 * <pre>
 * {@literal
 * <!-- ２つの許容文字集合定義を組み合わせ -->
 * <component name="composite" class="nablarch.core.validation.validator.unicode.CompositeCharsetDef">\
 *   <property name="charsetDefList">
 *     <list>
 *       <component-ref name="asciiWithoutControlCode"/>
 *       <component-ref name="zenkakuKatakanaCharset"/>
 *     </list>
 *   </property>
 * </component>
 * <!-- ASCII -->
 * <component name="asciiWithoutControlCode" class="nablarch.core.validation.validator.unicode.RangedCharsetDef">
 *   <property name="startCodePoint" value="U+0020" />
 *   <property name="endCodePoint" value="U+007E" />
 * </component>
 * <!-- 全角カタカナ -->
 * <component name="zenkakuKatakanaCharset"
 *   class="nablarch.core.validation.validator.unicode.CompositeCharsetDef">
 *   <property name="charsetDefList">
 *     <list>
 *       <component-ref name="zenkakuKatakanaCharsDef" />
 *       <component-ref name="zenkakuSpaceDef" />
 *     </list>
 *   </property>
 * </component>
 * }
 * </pre>
 *
 * @author T.Kawasaki
 */
public class CompositeCharsetDef extends CharsetDefSupport {

    /** 許容文字集合定義のリスト */
    private List<? extends CharsetDef> definitions;

    /** {@inheritDoc} */
    public boolean contains(int codePoint) {
        for (CharsetDef e : definitions) {
            if (e.contains(codePoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 許容文字集合定義のリストを設定する。
     *
     * @param definitions 許容文字集合定義のリスト
     */
    public void setCharsetDefList(List<? extends CharsetDef> definitions) {
        this.definitions = definitions;
    }
}
