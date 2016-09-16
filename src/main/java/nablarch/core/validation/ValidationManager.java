package nablarch.core.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nablarch.core.cache.StaticDataCache;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.MessageNotFoundException;
import nablarch.core.message.StringResourceHolder;
import nablarch.core.repository.IgnoreProperty;
import nablarch.core.repository.initialization.Initializable;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.creator.MapConstructorFormCreator;
import nablarch.core.validation.domain.DomainValidationHelper;


/**
 * バリデーションとデータの変換を行うクラス。<br/>
 * 実際のバリデーションとデータの変換はバリデータとコンバータに委譲する。
 *
 * @author Koichi Asano
 */
public class ValidationManager implements Initializable {

    /** ロガー。 */
    private static final Logger LOGGER = LoggerManager
            .get(ValidationManager.class);

    /** フォーム配列サイズキー文字列の最大長のデフォルト値(999まで指定可能) */
    private static final int DEFAULT_SIZE_KEY_MAX_LENGTH = 3;

    /** FormValidationDefinitionを保持するStaticDataCache。 */
    private StaticDataCache<FormValidationDefinition> formDefinitionCache;

    /** フォームの生成クラス。 */
    private FormCreator formCreator = new MapConstructorFormCreator();

    /** コンバータのリスト。 */
    private List<Convertor> convertors;

    /** バリデータのリスト。 */
    private List<Validator> validators;

    /** フォームのプロパティ名をデフォルトのメッセージIDとして使用するかどうかの設定値。 */
    private boolean useFormPropertyNameAsMessageId = false;

    /** コンバータのマップ。 */
    private Map<Class<?>, Convertor> convertorMap;

    /** バリデータのマップ。 */
    private Map<Class<? extends Annotation>, Validator> validatorMap;

    /** フォーム配列サイズ文字列の最大長。 */
    private int formArraySizeValueMaxLength = DEFAULT_SIZE_KEY_MAX_LENGTH;

    /** ValidationTargetアノテーションのsizeKeyに不正な長さを指定した際のエラーメッセージID。 */
    private String invalidSizeKeyMessageId;

    /**
     * FormValidationDefinitionをキャッシュするStaticDataCacheをセットする。
     *
     * @param formDefinitionCache FormValidationDefinitionを保持するStaticDataCache
     */
    public void setFormDefinitionCache(
            StaticDataCache<FormValidationDefinition> formDefinitionCache) {
        this.formDefinitionCache = formDefinitionCache;
    }

    /**
     * フォームの生成クラスをセットする。<br/>
     * セットしなかった場合、MapConstructorFormCreatorが使用される。
     *
     * @param formCreator フォームの生成クラス
     */
    public void setFormCreator(FormCreator formCreator) {
        this.formCreator = formCreator;
    }

    /**
     * メッセージリソースをセットする。
     *
     * @param stringResourceHolder メッセージリソース
     * @deprecated 本プロパティは、仕様変更に伴い使用しなくなりました。(値を設定しても、意味が無い)
     */
    @IgnoreProperty("MessageUtilのメッセージリソースを使用するよう仕様変更を行ったため本プロパティは廃止")
    @Deprecated
    public void setMessageResource(StringResourceHolder stringResourceHolder) {
    }

    /**
     * コンバータのリストをセットする。
     *
     * @param convertors セットするコンバータのリスト
     */
    public void setConvertors(List<Convertor> convertors) {
        this.convertors = convertors;
    }

    /**
     * バリデータのリストをセットする。
     *
     * @param validators バリデータのリスト
     */
    public void setValidators(
            List<Validator> validators) {
        this.validators = validators;
    }

    /**
     * フォームのプロパティ名をデフォルトのメッセージIDとして使用するかどうかの設定値を設定する。
     *
     * @param useFormPropertyNameAsMessageId
     *         フォームのプロパティ名をデフォルトのメッセージIDとして使用するかどうかの設定値。
     */
    public void setUseFormPropertyNameAsMessageId(
            boolean useFormPropertyNameAsMessageId) {
        this.useFormPropertyNameAsMessageId = useFormPropertyNameAsMessageId;
    }

    
    /**
     * フォーム配列サイズ文字列の最大長を設定する。
     * 
     * @param formArraySizeKeyMaxLength フォーム配列サイズ文字列の最大長 
     */
    public void setFormArraySizeValueMaxLength(int formArraySizeKeyMaxLength) {
        this.formArraySizeValueMaxLength = formArraySizeKeyMaxLength;
    }

    /**
     * ValidationTargetアノテーションのsizeKeyに不正な長さを指定した際のエラーメッセージIDを設定する。
     * @param invalidSizeKeyLengthMessageId ValidationTargetアノテーションのsizeKeyに不正な長さを指定した際のエラーメッセージID
     */
    public void setInvalidSizeKeyMessageId(
            String invalidSizeKeyLengthMessageId) {
        this.invalidSizeKeyMessageId = invalidSizeKeyLengthMessageId;
    }

    /**
     * {@inheritDoc}
     *
     * @see nablarch.core.repository.initialization.Initializable#initialize()
     */
    public void initialize() {

        Map<Class<?>, Convertor> convertorMap = new HashMap<Class<?>, Convertor>();
        for (Convertor convertor : convertors) {
            Class<?> targetClass = convertor.getTargetClass();
            if (targetClass == null) {
                throw new IllegalStateException("Convertor target class was not specified. "
                        + "convertor class = " + convertor.getClass().getName());
            }
            if (convertorMap.containsKey(targetClass)) {
                throw new IllegalStateException("Convertor target class was conflicted. "
                        + "convertor class = " + convertor.getClass().getName()
                        + ", target class = " + targetClass.getName());
            }
            convertorMap.put(targetClass, convertor);
        }
        this.convertorMap = Collections.unmodifiableMap(convertorMap);

        Map<Class<? extends Annotation>, Validator> postMap = new HashMap<Class<? extends Annotation>, Validator>();
        for (Validator validator : validators) {
            Class<? extends Annotation> annotationClass = validator.getAnnotationClass();
            if (annotationClass == null) {
                throw new IllegalStateException("Validator's annotation class was not specified. "
                        + "validator class = " + validator.getClass().getName());
            }

            Validation annotation = annotationClass.getAnnotation(Validation.class);
            if (annotation == null) {
                throw new IllegalStateException("Validator's annotation class was not annotated. "
                        + "validator class = " + validator.getClass().getName());
            }
            postMap.put(annotationClass, validator);
        }
        this.validatorMap = Collections.unmodifiableMap(postMap);
    }

    /**
     * バリデーションと値の変換を行う。
     *
     * @param <T>         バリデーション結果で取得できる型
     * @param prefix      Mapに入ったキーのプレフィクス
     * @param targetClass バリデーション対象のフォームのクラス
     * @param params      バリデーション対象のデータ
     * @param validateFor バリデーション対象メソッド
     * @return バリデーション結果の入ったValidationContext
     */
    @Published(tag = "architect")
    public <T> ValidationContext<T> validateAndConvert(String prefix, Class<T> targetClass,
            Map<String, ?> params, String validateFor) {

        String innerPrefix;
        if (StringUtil.isNullOrEmpty(prefix)) {
            innerPrefix = "";
        } else {
            innerPrefix = prefix + ".";
        }

        if (validatorMap == null) {
            throw new IllegalStateException("ValidationManager was not initialized.");
        }

        FormValidationDefinition formValidationDefinition = formDefinitionCache.getValue(targetClass);
        ValidationContext<T> context = createValidationContext(targetClass, params, innerPrefix, validateFor);

        if (validateFor != null) {
            List<Method> validateForMethods = formValidationDefinition.getValidateForMethods(validateFor);
            for (Method m : validateForMethods) {
                try {
                    m.invoke(formValidationDefinition, context);
                } catch (Exception e) {
                    throw new RuntimeException("ValidateFor method invocation failed. "
                            + "targetClass = " + targetClass.getName()
                            + ", method = " + m.getName(), e);
                }
            }
        } else {
            validateAndConvertAllProperty(context, formValidationDefinition);
        }

        return context;
    }

    /**
     * {@link ValidationContext}を生成する。
     *
     * @param targetClass バリデーション対象のフォームのクラス
     * @param params      バリデーション対象のデータ
     * @param innerPrefix Mapに入ったキーのプレフィクス
     * @param <T>         バリデーション結果で取得できる型
     * @param validateFor バリデーション対象メソッド
     * @return {@link ValidationContext}
     */
    @Published(tag = "architect")
    public <T> ValidationContext<T> createValidationContext(Class<T> targetClass,
            Map<String, ?> params, String innerPrefix, String validateFor) {
        return new ValidationContext<T>(innerPrefix, targetClass, formCreator, params, validateFor);
    }

    /**
     * フォームのバリデーションと変換を行う。
     *
     * @param <T>                        バリデーション結果で取得できる型
     * @param context                    ValidationContext
     * @param formValidationDefinition FormValidationDefinition
     */
    protected <T> void validateAndConvertAllProperty(ValidationContext<T> context,
            FormValidationDefinition formValidationDefinition) {
        for (Map.Entry<String, PropertyValidationDefinition> entry : formValidationDefinition.getPropertyValidationDefinitions().entrySet()) {
            validateAndConvertProperty(context, formValidationDefinition, entry.getValue());
        }
    }

    /**
     * プロパティに対するバリデーションと変換を行う。
     *
     * @param <T>         バリデーション結果で取得できる型
     * @param context     ValidationContext
     * @param formDef FormValidationDefinition
     * @param propertyDef PropertyValidationDefinition
     */
    protected <T> void validateAndConvertProperty(ValidationContext<T> context,
            FormValidationDefinition formDef, PropertyValidationDefinition propertyDef) {
        String propertyName = propertyDef.getName();

        if (context.isProcessed(propertyName)) {
            return;
        }

        context.setPropertyProcessed(propertyName);

        Object values = context.getParameters(propertyName);

        Annotation convertorFormatAnnotation = propertyDef.getConvertorFormatAnnotation();

        Object converted;
        Object propertyDisplayName = null;

        if (convertorFormatAnnotation instanceof ValidationTarget) {

            ValidationTarget validationSpec = (ValidationTarget) convertorFormatAnnotation;

            if (propertyDef.getType().isArray()) {
                Class<?> type = propertyDef.getType().getComponentType();
                
                int len = validationSpec.size();

                if (len == 0) {
                    String sizeKey = validationSpec.sizeKey();
                    Object formArraySizeValue = (Object) context
                            .getParameters(sizeKey);
                    if (formArraySizeValue instanceof String) {
                        String lenStr = (String) formArraySizeValue;
                        len = validateSizeValue(context, sizeKey, lenStr);
                        
                    } else if (formArraySizeValue instanceof String[]) {
                        String lenStr = ((String[]) formArraySizeValue)[0];
                        len = validateSizeValue(context, sizeKey, lenStr);
                    } else {
                        // String でも String[] でもサイズキーが取得できなければ、例外送出。
                        String valueType = formArraySizeValue != null ? formArraySizeValue.getClass().getName() : null;
                        throw new IllegalArgumentException("sizeKey value type was invalid."
                                + " property = " + context.getPrefix() + sizeKey
                                + ", value = " + formArraySizeValue
                                + ", value type = " + valueType);
                    }

                }

                ValidationContext<?>[] contextArray = new ValidationContext<?>[len];
                propertyDisplayName = context.getPrefix() + propertyDef.getName();
                boolean failed = false;
                
                // 初めに全ての配列をバリデーション
                for (int i = 0; i < len; i++) {
                    String childPrefix = context.getPrefix() + propertyDef.getName() + "[" + i + "]";
                    contextArray[i] = validateAndConvert(childPrefix, type, context.getParams(), context.getValidateFor());
                    if (!contextArray[i].isValid()) {
                        failed = true;
                    }
                }
                
                if (failed) {
                    // 1つでも失敗していたら、エラー扱いとする。
                    for (int i = 0; i < len; i++) {
                        context.addMessages(contextArray[i].getMessages());
                    }
                    converted = null;
                } else {
                    // 成功した場合、配列オブジェクト生成
                    Object array = Array.newInstance(type, len);
                    for (int i = 0; i < len; i++) {
                        Array.set(array, i, contextArray[i].createObject());
                    }
                    converted = array;
                }
                
            } else {
                // 再帰的な変換を実施
                String childPrefix = context.getPrefix() + propertyDef.getName();
    
                propertyDisplayName = childPrefix;
                Object converted1;
                ValidationContext<?> childContext = validateAndConvert(childPrefix, propertyDef.getType(), context.getParams(), context.getValidateFor());
                if (childContext.isValid()) {
                    converted1 = childContext.createObject();
                } else {
                    converted1 = null;
                    context.addMessages(childContext.getMessages());
                }
                
                converted = converted1;
            }
        } else {
            propertyDisplayName = createPropertyDisplayNameObject(context, propertyDef);
            // 値の変換を実施
            Convertor convertor = convertorMap.get(propertyDef.getType());
    
            if (convertor == null) {
                throw new UnsupportedOperationException("Property type was not supported. "
                        + " type = " + propertyDef.getType()
                        + ", targetClass = " + context.getTargetClass().getName()
                        + ", propertyName = " + propertyName);
            }
    
            Annotation format = getFormatAnnotation(convertorFormatAnnotation);
            if (!convertor.isConvertible(context, propertyName, propertyDisplayName, values, format)) {
                return;
            }
    
            converted = convertor.convert(context, propertyName, values, format);
        }
        context.putConvertedValue(propertyName, converted);

        // バリデーションを実施
        for (Annotation annotation : propertyDef.getValidatorAnnotations()) {
            Validator validator = validatorMap.get(annotation.annotationType());
            if (validator == null) {
                throw new UnsupportedOperationException("Validation annotation was not supported. "
                        + "Validation annotation = " + annotation.annotationType().getName()
                        + ", targetClass = " + context.getTargetClass().getName()
                        + ", propertyName = " + propertyName);
            }
            Object convertedValue = context.getConvertedValue(propertyName);

            if (!validator.validate(context, propertyName, propertyDisplayName, annotation, convertedValue)) {
                return;
            }
        }
    }
    
    /** ドメイン定義によるバリデーションをサポートするヘルパークラス */
    private DomainValidationHelper domainValidationHelper;

    /**
     * ドメイン定義によるバリデーションをサポートするヘルパークラスを設定する。
     * @param domainValidationHelper ドメイン定義によるバリデーションをサポートするヘルパークラス
     */
    public void setDomainValidationHelper(DomainValidationHelper domainValidationHelper) {
        this.domainValidationHelper = domainValidationHelper;
    }

    /**
     * {@link Convertor}に渡すフォーマットを指定するアノテーションを取得する。
     * <p/>
     * 指定されたコンバータのアノテーションがドメイン定義の場合は、ドメイン定義に指定されたコンバータアノテーションを返す。
     * それ以外の場合は、指定されたコンバータのアノテーションをそのまま返す。
     * 
     * @param convertorFormatAnnotation コンバータのアノテーション
     * @return {@link Convertor}に渡すフォーマットを指定するアノテーション
     */
    protected Annotation getFormatAnnotation(Annotation convertorFormatAnnotation) {
        return domainValidationHelper != null && domainValidationHelper.isDomainAnnotation(convertorFormatAnnotation)
                ? domainValidationHelper.getConvertorAnnotation(convertorFormatAnnotation)
                : convertorFormatAnnotation;
    }

    /**
     * 指定されたバリデーションアノテーションに沿ったバリデーション処理を行う。
     * 
     * @param <T>          バリデーション結果で取得できる型
     * @param context      ValidationContext
     * @param propertyName バリデーション対象のプロパティ名
     * @param annotation   バリデーションアノテーションクラス
     * @param params       バリデーションアノテーションのパラメータ
     */
    public <T> void validate(ValidationContext<T> context, String propertyName, Class<? extends Annotation> annotation, Map<String, Object> params) {
        Validator validator = validatorMap.get(annotation);
        if (validator == null) {
            throw new UnsupportedOperationException("Validation annotation was not supported. "
                    + "Validation annotation = " + annotation.getClass().getName()
                    + ", targetClass = " + context.getTargetClass().getName()
                    + ", propertyName = " + propertyName);
        }
        if (!(validator instanceof DirectCallableValidator)) {
            throw new UnsupportedOperationException(
                "a Validator must implement 'DirectCallableValidator' "
              + "if you want to call it in program code. : " + validator.getClass().getName() 
            );
        }
        DirectCallableValidator directCallable = (DirectCallableValidator) validator;
        FormValidationDefinition formValidationDefinition = formDefinitionCache.getValue(context.getTargetClass());
        PropertyValidationDefinition propertyDef = formValidationDefinition.getPropertyValidationDefinition(propertyName);
        Object propertyDispName = createPropertyDisplayNameObject(context, propertyDef);
        Object convertedValue = context.getConvertedValue(propertyName);
        if (!directCallable.validate(context, propertyName, propertyDispName, params, convertedValue)) {
            return;
        }
    }

    /**
     * サイズキー文字列をバリデーションし、値を取得する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName サイズキーのプロパティ名
     * @param lenStr サイズ文字列
     * @return サイズキー文字列のパース結果。
     */
    private <T> int validateSizeValue(ValidationContext<T> context,
            String propertyName, String lenStr) {
        int len;
        if (lenStr.length() > formArraySizeValueMaxLength) {
            // ValidationTarget#sizeKey が不正(長さが異常)だった場合、メッセージを詰めて終わらす。
            addInvalidSizeKeyMessage(context, propertyName);
            return 0;
        }
        
        try {
            len = Integer.parseInt(lenStr);
        } catch (NumberFormatException e) {
            addInvalidSizeKeyMessage(context, propertyName);
            return 0;
        }
        return len;
    }

    /**
     * サイズキーが不正であるエラーメッセージを設定。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName サイズキーのプロパティ名
     */
    private <T> void addInvalidSizeKeyMessage(ValidationContext<T> context,
            String propertyName) {
        context.addResultMessage(propertyName, invalidSizeKeyMessageId);
    }

    /**
     * プロパティの表示名を表すオブジェクトを作成する。
     *
     * @param <T>         バリデーション結果で取得できる型
     * @param context     ValidationContext
     * @param propertyDef PropertyValidationDefinition
     * @return プロパティの表示名を表すオブジェクト
     */
    protected <T> Object createPropertyDisplayNameObject(ValidationContext<T> context,
            PropertyValidationDefinition propertyDef) {

        // プロパティ名を使用する設定が有効になっていた場合、プロパティ名をメッセージIDとして使用する。
        if (useFormPropertyNameAsMessageId) {
            return context.getMessage(propertyDef.getNameWithClass());
        }

        // @PropertyName に value が指定されていた場合、そのままその文字列を使用する。
        if (!StringUtil.isNullOrEmpty(propertyDef.getDefaultDisplayName())) {
            return propertyDef.getDefaultDisplayName();
        }

        // @PropertyName に messageId が指定されていた場合、対応するメッセージを使用する。
        if (!StringUtil.isNullOrEmpty(propertyDef.getMessageId())) {

            try {
                return context.getMessage(propertyDef.getMessageId());
            } catch (MessageNotFoundException e) {
                LOGGER.logWarn("message was not found."
                        + " message id = " + propertyDef.getMessageId()
                        , e);
            }
        }

        // 上記のいずれにもあてはまらない場合は、プロパティ名をそのまま使用する。
        return propertyDef.getName();

    }

    /**
     * バリデーション対象のプロパティを指定してバリデーションを行う。
     *
     * @param <T>           バリデーション結果で取得できる型
     * @param context       ValidationContext
     * @param propertyNames バリデーション対象とするプロパティ名の配列
     */
    @Published(tag = "architect")
    public <T> void validate(ValidationContext<T> context, String[] propertyNames) {
        FormValidationDefinition formValidationDefinition = formDefinitionCache.getValue(
                context.getTargetClass());
        for (String propertyName : propertyNames) {
            PropertyValidationDefinition propertyDef = formValidationDefinition.getPropertyValidationDefinition(
                    propertyName);
            validateAndConvertProperty(context, formValidationDefinition, propertyDef);
        }
    }

    /**
     * バリデーション対象外のプロパティを指定してバリデーションを行う。
     *
     * @param <T>           バリデーション結果で取得できる型
     * @param context       ValidationContext
     * @param propertyNames バリデーション対象としないプロパティ名の配列
     */
    @Published(tag = "architect")
    public <T> void validateWithout(ValidationContext<T> context, String[] propertyNames) {
        FormValidationDefinition formValidationDefinition = formDefinitionCache.getValue(
                context.getTargetClass());
        Set<String> properties = new HashSet<String>();
        properties.addAll(Arrays.asList(propertyNames));
        List<String> targetProperties = new ArrayList<String>();
        for (String key : formValidationDefinition.getPropertyValidationDefinitions().keySet()) {
            if (!properties.contains(key)) {
                targetProperties.add(key);
            }
        }

        validate(context, targetProperties.toArray(new String[targetProperties.size()]));
    }
}
