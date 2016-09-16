package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;


/**
 *   数値の範囲をチェックするクラス。
 *   <p>
 *   入力された値が、{@link NumberRange}アノテーションのプロパティに設定された値の範囲内であるかチェックする。
 *   </p>
 *   <p>
 *     <b>使用するための設定</b>
 *   </p>
 *     本バリデータを使用するためにはデフォルトのエラーメッセージIDを指定する必要がある。
 *   <pre>
 *      {@code <component name="numberRangeValidator" class="nablarch.core.validation.validator.NumberRangeValidator">
 *          <property name="maxMessageId" value="MSGXXXXXX"/>
 *          <property name="maxAndMinMessageId" value="MSGXXXXXX"/>
 *          <property name="minMessageId" value="MSGXXXXXX"/>
 *      </component>
 *      }
 *   </pre>
 *
 *   <p>
 *     <b>プロパティの設定</b>
 *   </p>
 *   範囲チェックをしたいプロパティのセッタに{@link NumberRange}アノテーションを次のように設定する。
 *   <pre>
 *   入力値が1以上10以下の範囲内であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = 1, max = 10)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *
 *   入力値が-1.5以上1.5以下の範囲内であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = -1.5, max = 1.5)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *
 *   入力値が0以上であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = 0)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *   </pre>
 *
 * @author Koichi Asano
 *
 */
public class NumberRangeValidator implements DirectCallableValidator {
    /**
     * バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    private String maxMessageId;

    /**
     * バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String maxAndMinMessageId;

    /**
     * バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String minMessageId;

    /**
     * バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。<br/>
     * 例 : "{0}は{2}以下で入力してください。"
     * 
     * @param maxMessageId バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    public void setMaxMessageId(String maxMessageId) {
        this.maxMessageId = maxMessageId;
    }
    
    /**
     * バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}以上{2}以下で入力してください。"
     * @param maxAndMinMessageId バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMaxAndMinMessageId(String maxAndMinMessageId) {
        this.maxAndMinMessageId = maxAndMinMessageId;
    }
    /**
     * バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}以上で入力してください。"
     * @param minMessageId バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMinMessageId(String minMessageId) {
        this.minMessageId = minMessageId;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> getAnnotationClass() {
        return NumberRange.class;
    }

    /**
     * {@inheritDoc}
     */
    public <T> boolean validate(ValidationContext<T> context, String propertyName,
            Object propertyDisplayName, Annotation annotation, Object value) {
        
        if (value == null) {
            return true;
        }

        Number num = (Number) value;
        NumberRange range = (NumberRange) annotation;
        if (range.min() > Double.NEGATIVE_INFINITY) {
            if (num.doubleValue() < range.min()) {
                addMessage(context, propertyName, propertyDisplayName, range);
                return false;
            }
        }

        if (range.max() < Double.POSITIVE_INFINITY) {
            if (num.doubleValue() > range.max()) {
                addMessage(context, propertyName, propertyDisplayName, range);
                return false;
            }
        }

        return true;
    }

    /**
     * エラーメッセージを設定する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param range NumberRangeアノテーション
     */
    private <T> void addMessage(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, NumberRange range) {
        String messageId;
        if (range.messageId().length() > 0) {
            messageId = range.messageId();
        } else {
            if (range.min() > Double.NEGATIVE_INFINITY && range.max() < Double.POSITIVE_INFINITY) {
                messageId = maxAndMinMessageId;
            } else if (range.min() > Double.NEGATIVE_INFINITY) {
                messageId = minMessageId;
            } else {
                messageId = maxMessageId;
            }
        }
        ValidationResultMessageUtil.addResultMessage(context, propertyName, messageId, propertyDisplayName, range.min(), range.max());
    }

    /**{@inheritDoc}*/
    public <T> boolean validate(ValidationContext<T>       context,
                                String                     propertyName,
                                Object                     propertyDisplayName,
                                final Map<String, Object>  params,
                                Object                     value) {

        NumberRange annotation = new NumberRange() {

            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }

            public double min() {
                Double min = (Double) params.get("min");
                return (min == null) ? Double.NEGATIVE_INFINITY
                                     : min;
            }

            public double max() {
                Double max = (Double) params.get("max");
                return (max == null) ? Double.POSITIVE_INFINITY
                                     : max;
            }

            public String messageId() {
                String messageId = (String) params.get("messageId");
                return StringUtil.isNullOrEmpty(messageId) ? ""
                                                           : messageId;
            }
        };

        return validate(context, propertyName, propertyDisplayName, annotation, value);
    }
}
