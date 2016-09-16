package nablarch.core.validation;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import nablarch.core.message.Message;
import nablarch.core.message.MessageUtil;
import nablarch.core.message.StringResource;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;


/**
 * バリデーションの実行時に使用するユーティリティクラス。<br/>
 * 全てのメソッドは{@link SystemRepository}から"validationManager"という名前で取得した{@link ValidationManager}に処理を委譲する。
 * 
 * @author Koichi Asano
 *
 */
public final class ValidationUtil {

    /**
     * 隠蔽コンストラクタ。
     */
    private ValidationUtil() {
        
    }
    /**
     * {@link SystemRepository}に定義されている{@link ValidationManager}の名称。
     */
    private static final String VALIDATION_MANAGER_NAME = "validationManager";

    /**
     * {@link SystemRepository}から{@link ValidationManager}を取得する。
     * @return {@link SystemRepository}から取得した{@link ValidationManager}
     * @throws IllegalStateException {@link ValidationManager}を取得できなかった場合
     */
    private static ValidationManager getManager() {
        ValidationManager validationManager = SystemRepository.get(VALIDATION_MANAGER_NAME);
        if (validationManager == null) {
            throw new IllegalStateException("can't get ValidationManager instance from System Repository."
                    + "check configuration. key=[" + VALIDATION_MANAGER_NAME + "]");
        }
        return validationManager;
    }

    /**
     * バリデーション対象のプロパティを指定してバリデーションを行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context バリデーションコンテキスト
     * @param propertyNames バリデーション対象とするプロパティ名の配列
     */
    @Published
    public static <T> void validate(ValidationContext<T> context, String[] propertyNames) {
        getManager().validate(context, propertyNames);
    }
    
    /**
     * 対象のプロパティについて、指定したアノテーションクラスに従ったバリデーションを行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T>          バリデーション結果で取得できる型
     * @param context      バリデーションコンテキスト
     * @param propertyName バリデーション対象とするプロパティ名
     * @param annotation   バリデーション用のアノテーションクラス
     * @param params       バリデーション用のアノテーションパラメータ
     */
    @Published(tag = "architect")
    public static <T> void validate(ValidationContext<T> context, String propertyName, Class<? extends Annotation> annotation, Map<String, Object> params) {
        getManager().validate(context, propertyName, annotation, params);
    }
    
    /**
     * 対象のプロパティについて、指定したアノテーションクラスに従ったバリデーションを行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T>          バリデーション結果で取得できる型
     * @param context      バリデーションコンテキスト
     * @param propertyName バリデーション対象とするプロパティ名
     * @param annotation   バリデーション用のアノテーションクラス
     */
    @Published(tag = "architect")
    @SuppressWarnings("unchecked")
    public static <T> void validate(ValidationContext<T> context, String propertyName, Class<? extends Annotation> annotation) {
        getManager().validate(context, propertyName, annotation, Collections.EMPTY_MAP);
    }    

    /**
     * バリデーション対象としないプロパティを指定してバリデーションを行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context バリデーションコンテキスト
     * @param propertyNames バリデーション対象としないプロパティ名の配列
     */
    @Published
    public static <T> void validateWithout(ValidationContext<T> context, String[] propertyNames) {
        getManager().validateWithout(context, propertyNames);
    }

    /**
     * すべてのプロパティについてバリデーションを行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context バリデーションコンテキスト
     */
    @Published
    public static <T> void validateAll(ValidationContext<T> context) {
        validateWithout(context, new String[0]);
    }
    
    /**
     * リクエストのバリデーションと変換を行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param targetClass バリデーション対象のフォームクラス
     * @param params バリデーション対象のデータ
     * @param validateFor targetClassのバリデーション対象メソッドに付与した{@link ValidateFor}の値
     * @return バリデーション結果の入ったバリデーションコンテキスト
     * 
     */
    @Published
    public static <T> ValidationContext<T> validateAndConvertRequest(
            Class<T> targetClass, Map<String, ?> params, String validateFor) {
        ValidationContext<T> result = getManager().validateAndConvert("", targetClass, params, validateFor);
        return result;
    }

    /**
     * リクエストのバリデーションと変換を行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param targetClass バリデーション対象のフォームクラス
     * @param request リクエスト
     * @param validateFor targetClassのバリデーション対象メソッドに付与した{@link ValidateFor}の値
     * @return バリデーション結果の入ったバリデーションコンテキスト
     */
    @Published
    public static <T> ValidationContext<T> validateAndConvertRequest(
            Class<T> targetClass, Validatable<?> request, String validateFor) {
        return validateAndConvertRequest("", targetClass, request.getParamMap(), validateFor);
    }
    
    /**
     * リクエストのバリデーションと変換を行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param prefix リクエストパラメータ名のプレフィクス
     * @param targetClass バリデーション対象のフォームクラス
     * @param params バリデーション対象のデータ
     * @param validateFor targetClassのバリデーション対象メソッドに付与した{@link ValidateFor}の値
     * @return バリデーション結果の入ったバリデーションコンテキスト
     * 
     */
    @Published
    public static <T> ValidationContext<T> validateAndConvertRequest(
            String prefix, Class<T> targetClass, Map<String, ?> params, String validateFor) {
        ValidationContext<T> result = getManager().validateAndConvert(prefix, targetClass, params, validateFor);
        return result;
    }

    /**
     * リクエストのバリデーションと変換を行う。
     * <p/>
     * バリデーション結果は{@link ValidationContext}に保持される。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param prefix リクエストパラメータ名のプレフィクス
     * @param targetClass バリデーション対象のフォームクラス
     * @param request リクエスト
     * @param validateFor targetClassのバリデーション対象メソッドに付与した{@link ValidateFor}の値
     * @return バリデーション結果の入ったバリデーションコンテキスト
     */
    @Published
    public static <T> ValidationContext<T> validateAndConvertRequest(
            String prefix, Class<T> targetClass, Validatable<?> request, String validateFor) {
        return validateAndConvertRequest(prefix, targetClass, request.getParamMap(), validateFor);
    }

    /**
     * 特定のプロパティに対するバリデーションエラーメッセージを作成する。
     * 
     * @param fullPropertyName プレフィクスを含むプロパティ名
     * @param messageId エラーメッセージのメッセージID
     * @param options メッセージフォーマットのテンプレート文字列に埋め込む値
     * @return 特定のプロパティに対するバリデーションエラーメッセージ
     */
    @Published
    public static Message createMessageForProperty(String fullPropertyName, String messageId, Object... options) {

        StringResource resource = MessageUtil.getStringResource(messageId);
        return new ValidationResultMessage(fullPropertyName, resource, options);
    }
}
