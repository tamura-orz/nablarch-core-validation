package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;

import nablarch.core.validation.Convertor;
import nablarch.core.validation.ValidationContext;

/**
 * 値をString配列に変換するクラス。
 * 
 * @author Koichi Asano
 *
 */
public class StringArrayConvertor implements Convertor {

    /**
     * {@inheritDoc}
     */
    public <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation format) {

        String[] values = (String[]) value;

        return values;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getTargetClass() {
        return String[].class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean isConvertible(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, Object value,
            Annotation format) {
        // String 配列以外許容しない。
        // それ以外の値を指定する場合、プログラムバグ以外にあり得ないため実行時例外を送出。
        if (value != null && !(value instanceof String[])) {
            throw new IllegalArgumentException("Convert type was not supported. type = " + value.getClass().getName());
        }
        return true;
    }
}
