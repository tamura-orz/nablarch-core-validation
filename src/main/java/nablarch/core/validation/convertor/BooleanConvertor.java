package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;

import nablarch.core.validation.Convertor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 値をBooleanに変換するクラス。
 * 
 * @author TIS
 */
public class BooleanConvertor implements Convertor {

    /**
     * 変換失敗時のデフォルトのエラーメッセージのメッセージID。
     */
    private String conversionFailedMessageId;

    /**
     * 変換対象の値にnullを許可するか否か。
     */
    private boolean allowNullValue = true;

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
            return Boolean.FALSE;
            
        } else if (value instanceof String[]) {
            value = ((String[]) value)[0];
        }
        
        Boolean ret = Boolean.parseBoolean(value.toString());

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getTargetClass() {
        return Boolean.class;
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

        if (value != null && value.toString().matches("[tT][rR][uU][eE]|[fF][aA][lL][sS][eE]")) {
            convertible = true;
        } else {
            convertible = false;
        }
        
        if (!convertible) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                                                        conversionFailedMessageId, propertyDisplayName);
        }
        
        return convertible;
    }
}
