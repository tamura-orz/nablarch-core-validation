package nablarch.core.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.message.StringResource;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;


/**
 * バリデーション実行中の情報を保持するクラス。
 *
 * @param <T> バリデーション結果で取得できる型
 * @see Message
 *
 * @author Koichi Asano
 *
 */
public class ValidationContext<T> {

    /**
     * バリデーション対象のプレフィクス。<br/>
     * このプレフィクスにマッチする入力値のみバリデーションの対象とする。
     */
    private String prefix;
    /**
     * バリデーション結果メッセージのリスト。
     */
    private List<Message> messages;

    /**
     * 変換前文字列のMap。
     */
    private Map<String, ?> params;

    /**
     * 変換後オブジェクトのマップ。
     */
    private Map<String, Object> convertedValues;
    /**
     * バリデーションの対象クラス。
     */
    private Class<T> targetClass;

    /**
     * バリデーション対象メソッド。
     */
    private String validateFor;

    /**
     * EntityCreator。
     */
    private FormCreator formCreator;

    /**
     * バリデーション結果がvalidでないプロパティの名前
     */
    private Set<String> invalidPropertyNames;

    /**
     * バリデーション実行済みのプロパティ名のセット。
     */
    private Set<String> processedProperties;

    /**
     * {@code ValidationContext}オブジェクトを生成する。
     *
     * @param prefix バリデーション対象のプレフィクス
     * @param targetClass バリデーション対象のクラス
     * @param formCreator FormCreator
     * @param params パラメータのMap
     * @param validateFor バリデーション対象メソッド
     */
    @Published(tag = "architect")
    public ValidationContext(String prefix, Class<T> targetClass,
                             FormCreator formCreator, Map<String, ?> params, String validateFor) {
        this.prefix = prefix;
        this.targetClass = targetClass;
        this.formCreator = formCreator;
        this.params = params;
        this.validateFor = validateFor;

        processedProperties = new HashSet<String>();
        messages = new ArrayList<Message>();
        convertedValues = new HashMap<String, Object>();
        invalidPropertyNames = new HashSet<String>();
    }

    /**
     * メッセージを追加する。
     * 
     * @param messageId メッセージID
     * @param params メッセージに埋め込む値
     */
    @Published
    public void addMessage(String messageId, Object... params) {
        messages.add(MessageUtil.createMessage(MessageLevel.ERROR, messageId, params));
    }


    /**
     * メッセージを追加する。
     * 
     * @param messages メッセージのリスト
     */
    @Published(tag = "architect")
    public void addMessages(List<Message> messages) {
        
        this.messages.addAll(messages);
        
        // validでないプロパティの名前を追加する。
        for (Message message : messages) {
            if (message instanceof ValidationResultMessage) {
                
                String propertyName = ((ValidationResultMessage) message).getPropertyName();
                
                if (propertyName.startsWith(prefix)) { // prefixを取り除く。
                    propertyName = propertyName.substring(prefix.length());
                }
                
                // プロパティ名の上位階層から順に追加する。
                // 例："aaa.bbb.ccc" -> ["aaa", "aaa.bbb", "aaa.bbb.ccc"]
                //     "bbb"や"ccc"は追加しない。
                int endIndex = 0;
                while (endIndex != -1) {
                    endIndex = propertyName.indexOf('.', endIndex);
                    if (endIndex != -1) {
                        invalidPropertyNames.add(propertyName.substring(0, endIndex));
                        endIndex++;
                    }
                }
                invalidPropertyNames.add(propertyName);
            }
        }
    }
    
    /**
     * バリデーション結果を追加する。
     * 
     * @param propertyName プロパティ名
     * @param messageId バリデーション結果メッセージのメッセージID
     * @param params メッセージのオプションパラメータ
     * @throws IllegalArgumentException プロパティ名が{@code null}または空文字だった場合
     */
    @Published(tag = "architect")
    public void addResultMessage(String propertyName, String messageId, Object... params) {
        if (StringUtil.isNullOrEmpty(propertyName)) {
            throw new IllegalArgumentException("property name was not specified");
        }
        StringResource message = getMessage(messageId);
        ValidationResultMessage resultMessage = new ValidationResultMessage(prefix + propertyName, message, params);
        messages.add(resultMessage);
        
        // validでないプロパティの名前を追加する。
        invalidPropertyNames.add(propertyName);
    }

    /**
     * メッセージIDに対応するメッセージを取得する。
     * 
     * @param messageId メッセージID
     * @return メッセージIDに対応するメッセージ
     */
    @Published(tag = "architect")
    public StringResource getMessage(String messageId) {
        return MessageUtil.getStringResource(messageId);
    }

    /**
     * フォームオブジェクトを生成する。
     * 
     * @return フォームオブジェクト
     * @throws IllegalStateException フォームオブジェクトにバリデーションエラーのプロパティがある場合
     */
    @Published
    public T createObject() {
        if (!isValid()) {
            throw new IllegalStateException("Validation context is not valid.");
        }
        return formCreator.create(targetClass, convertedValues, null);
    }

    /**
     * フォームオブジェクトを生成する。
     * <p/>
     * {@link #createObject()}と異なり、生成前にフォームオブジェクトにバリデーションエラーがあるかチェックしない。<br/>
     * そのため、バリデーションエラーがあるプロパティもフォームオブジェクトに設定される。<br/>
     * ただし、プロパティをフォームオブジェクトのプロパティの型に変換できない場合は設定されない。
     * 
     * @return フォームオブジェクト
     */
    @Published(tag = "architect")
    public T createDirtyObject() {
        return formCreator.create(targetClass, convertedValues, null);
    }

    /**
     * プロパティ名に対応するプレフィクス付き文字列の配列を取得する。
     * 
     * @param propertyName プロパティ名
     * @return プロパティ名に対応するプレフィクス付き文字列の配列
     */
    @Published(tag = "architect")
    public Object getParameters(String propertyName) {
        
        return params.get(prefix + propertyName);
    }

    /**
     * フォームオブジェクトのプロパティの型に変換したプロパティを追加する。
     * 
     * @param propertyName 追加するプロパティ名
     * @param value 変換したプロパティの値
     */
    public void putConvertedValue(String propertyName, Object value) {
        convertedValues.put(propertyName, value);
    }

    /**
     * フォームオブジェクトのプロパティの型に変換したプロパティを取得する。
     * <p/>
     * プロパティにバリデーションエラーがある場合も変換した値を返す。
     * 変換できない場合、プロパティが見つからない場合は{@code null}を返す。
     * 
     * @param propertyName 取得するプロパティ名
     * @return 変換したプロパティの値
     */
    @Published
    public Object getConvertedValue(String propertyName) {
        return convertedValues.get(propertyName);
    }

    /**
     * 変換対象のフォームクラスを取得する。
     *
     * @return 変換対象のフォームクラス
     */
    public Class<T> getTargetClass() {
        return targetClass;
    }

    /**
     * バリデーション結果メッセージのリストを取得する。
     * 
     * @return バリデーション結果メッセージのリスト
     */
    @Published
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * バリデーションエラーがないかどうかを取得する。
     * 
     * @return バリデーションエラーがない場合は{@code true}
     */
    @Published
    public boolean isValid() {
        return messages.isEmpty();
    }

    /**
     * バリデーションエラーがある場合に、
     * バリデーション結果メッセージを保持した{@link ApplicationException}を送出する。
     * <p/>
     * バリデーションエラーのプロパティがない場合、本メソッドは何もしない。
     *
     * @throws ApplicationException バリデーションエラーのプロパティがある場合
     */
    @Published
    public void abortIfInvalid() throws ApplicationException {
        if (!isValid()) {
            throw new ApplicationException(getMessages());
        }
    }

    /**
     * 指定されたプロパティにバリデーションエラーがあるかどうか判定する。
     * <p/>
     * バリデーション対象でないプロパティ名が指定された場合は{@code false}を返す。
     * 
     * @param propertyName プロパティ名
     * @return 指定されたプロパティにバリデーションエラーがある場合は{@code true}
     */
    @Published
    public boolean isInvalid(String propertyName) {
        return invalidPropertyNames.contains(propertyName);
    }

    /**
     * バリデーション済みプロパティのセットにプロパティを追加する。
     * @param propertyName 追加するプロパティ名
     */
    public void setPropertyProcessed(String propertyName) {
        processedProperties.add(propertyName);
    }

    /**
     * バリデーション済みプロパティか否か判定する。
     * @param propertyName プロパティ名
     * @return 指定したプロパティがバリデーション済みである場合{@code true}
     */
    public boolean isProcessed(String propertyName) {
        return processedProperties.contains(propertyName);
    }

    /**
     * バリデーション対象のプレフィクスを取得する。
     * @return バリデーション対象のプレフィクス
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * プロパティの値が文字列のMapを取得する。
     * @return プロパティの値が文字列のMap
     */
    public Map<String, ?> getParams() {
        return params;
    }

    /**
     * バリデーション対象メソッドを取得する。 
     * @return バリデーション対象メソッド
     */
    public String getValidateFor() {
        return validateFor;
    }

}
