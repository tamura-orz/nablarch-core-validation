package nablarch.core.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import nablarch.core.util.ObjectUtil;



/**
 * バリデーションに必要なプロパティの情報を保持するクラス。
 * 
 * @author Koichi Asano
 *
 */
public class PropertyValidationDefinition {

    /**
     * クラス名。
     */
    private String simpleClassName;

    /**
     * プロパティ名。
     */
    private String name;

    /**
     * デフォルトの表示名。
     */
    private String defaultDisplayName;

    /**
     * プロパティを表わすメッセージID。
     */
    private String messageId;
    /**
     * プロパティの型。
     */
    private Class<?> type;
    /**
     * 変換フォーマットアノテーション。
     */
    private Annotation convertorFormatAnnotation;
    /**
     * バリデーションアノテーションのリスト。
     */
    private List<Annotation> validatorAnnotations;

    /**
     * コンストラクタ。
     * @param formClass フォームのクラス
     * @param setter プロパティのセッタメソッド
     * @param overrideMethodDefinition オーバライドしたメソッドの定義
     */
    public PropertyValidationDefinition(Class<?> formClass, Method setter, PropertyValidationDefinition overrideMethodDefinition) {
        name = ObjectUtil.getPropertyNameFromSetter(setter);
        type = setter.getParameterTypes()[0];

        simpleClassName = formClass.getSimpleName();

        validatorAnnotations = new ArrayList<Annotation>();

        messageId = null;
        
        for (Annotation annotation : setter.getAnnotations()) {
            if (annotation instanceof PropertyName) {
                PropertyName propertyName = (PropertyName) annotation;
                defaultDisplayName = propertyName.value();
                messageId = propertyName.messageId();
                continue;
            }
            
            Class<? extends Annotation> annotationClass = annotation.annotationType();
            
            if (annotationClass.getAnnotation(ConversionFormat.class) != null) {
                convertorFormatAnnotation = annotation;
            }
            
            if (annotationClass.getAnnotation(Validation.class) != null) {
                validatorAnnotations.add(annotation);
            }
        }

        // コンバータの条件が指定されていなかった場合、オーバライドしたメソッドからコピーする
        if (convertorFormatAnnotation == null && overrideMethodDefinition != null) {
            convertorFormatAnnotation = overrideMethodDefinition.getConvertorFormatAnnotation();
        }
        
        // バリデーションの条件が指定されていなかった場合、オーバライドしたメソッドからコピーする
        if (validatorAnnotations.isEmpty() && overrideMethodDefinition != null) {
            validatorAnnotations = overrideMethodDefinition.getValidatorAnnotations();
        }
    }

    /**
     * プロパティ名を取得する。
     * @return プロパティ名
     */
    public String getName() {
        return name;
    }

    /**
     * クラス名をつけたプロパティ名を取得する。
     * 
     * @return クラス名をつけたプロパティ名
     */
    public String getNameWithClass() {
        return simpleClassName + "." + name;
    }

    /**
     * プロパティを表わすメッセージIDを取得する。
     * @return プロパティを表わすメッセージID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * デフォルトの表示名を取得する。
     * @return デフォルトの表示名
     */
    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    /**
     * プロパティの型を取得する。
     * @return プロパティの型
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * コンバータフォーマットアノテーションを取得する。
     * @return コンバータフォーマットアノテーション
     */
    public Annotation getConvertorFormatAnnotation() {
        return convertorFormatAnnotation;
    }

    /**
     * バリデータアノテーションのリストを取得する。
     * @return バリデータアノテーションのリスト
     */
    public List<Annotation> getValidatorAnnotations() {
        return validatorAnnotations;
    }
}
