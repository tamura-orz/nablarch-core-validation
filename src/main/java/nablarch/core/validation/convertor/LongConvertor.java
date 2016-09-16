package nablarch.core.validation.convertor;

import nablarch.core.util.annotation.Published;


/**
 * 値をLongに変換するクラス。<br/>
 * 
 * 本クラスで変換するプロパティには、必ずDigitsアノテーションを付与しておく必要がある。
 * 本クラスでは、Digitsアノテーションの属性を下記の通り使用する。</br>
 * <table border=1>
 *     <tr bgcolor="#cccccc">
 *         <td>Digitsアノテーションの属性名</td><td>説明</td>
 *     </tr>
 *     <tr>
 *         <td>integer</td><td>整数部桁数上限値。18以上の数値を指定できない。</td>
 *     </tr>
 *     <tr>
 *         <td>fraction</td><td>小数部桁数上限値。0のみ指定可能。</td>
 *     </tr>
 *     <tr>
 *         <td>commaSeparated</td><td>trueの場合、入力値が3桁区切り文字で編集されていてもよい。（区切り文字は省略可。）
 *         　　　　　　　　　　　　　</br>falseの場合、入力値が3桁区切り文字で編集されていてはいけない。</td>
 *     </tr>
 *     <tr>
 *         <td>messageId</td><td>変換失敗時のメッセージID。</td>
 *     </tr>
 * </table>
 * </br>
 * 本クラスが行うバリデーションの仕様は{@link IntegerConvertor}と同様である。</br>
 * 
 * @author Koichi Asano
 * @see Digits
 * @see IntegerConvertor
 */
@Published(tag = "architect")
public class LongConvertor extends IntegerConvertor {

    /**
     * {@inheritDoc}
     */
    public Class<?> getTargetClass() {
        return Long.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Number convertToPropertyType(String numberString) {
        return Long.valueOf(numberString);
    }

    @Override
    protected void checkDigit(Digits digit) throws IllegalArgumentException {
        if (digit.fraction() > 0) {
            throw new IllegalArgumentException("Fraction value was specified.");
        }

        if (digit.integer() >= 19) {
            // 19桁以上はエラー
            throw new IllegalArgumentException("length was invalid. "
                    + "integer length must be less than or equal to 18 digit. "
                    + "specified value:" + digit.integer());
        }
    }
}
