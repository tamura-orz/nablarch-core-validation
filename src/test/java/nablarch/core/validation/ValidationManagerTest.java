package nablarch.core.validation;

import nablarch.core.ThreadContext;
import nablarch.core.cache.BasicStaticDataCache;
import nablarch.core.message.Message;
import nablarch.core.message.MessageNotFoundException;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.message.StringResource;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * {@link ValidationManager}のテストクラス。
 */
public class ValidationManagerTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "nablarch/core/validation/validation-manager.xml");

    /** test Connection */
    private ValidationManager manager;

    private static final String[][] MESSAGES = {
        { "User.id", "ja","ID", "en","ID"},
        { "User.name", "ja","名前", "en","Name"},
        { "User.age", "ja","年齢", "en","Age"},
        { "StringArrayValueHolder.code", "ja","コード", "en","code"},
        { "MSG00001", "ja","{0}の値が不正です。","en","{0} value is invalid."},
        { "MSG00011","ja","{0}は必ず入力してください。","en","{0} is required."},
        { "MSG00021","ja","{0}は{2}文字以下で入力してください。","en","{0} cannot be greater than {2} characters."},
        { "MSG00022","ja","{0}は{1}文字以上{2}文字以下で入力してください。","en","{0} is not in the range {1} through {2}."},
        { "MSG00023","ja","{0}は{1}文字で入力してください。","en","{0} cannot be length {1}."},
        { "MSG00031","ja","{0}は整数{1}桁で入力してください。","en","{0} length must be under {1}."},
        { "MSG00032","ja","{0}は整数部{1}桁、少数部{2}桁で入力してください。","en","{0} must be {1}-digits and {1}-digits decimal integer part."},
        { "MSG00051","ja","{0}は{2}以下で入力してください。","en","{0} cannot be greater than {2}."},
        { "MSG00052","ja","{0}は{1}以上{2}以下で入力してください。","en","{0} is not in the range {1} through {2}."},
        { "MSG00053","ja","{0}は{1}以上{2}以下で入力してください。","en","{0} is not in the range {1} through {2}."},
        { "PROP0001","ja","名前","en","Name"},
        { "PROP0002","ja","ユーザ氏名","en","User Name"},
        { "PROP0003","ja","備考","en","Remarks"},
       };

    @BeforeClass
    public static void classSetup() throws Exception {
    }

    @Before
    public void setUp() {
        MockStringResourceHolder mock = repositoryResource.getComponent("stringResourceHolder");
        mock.setMessages(MESSAGES);

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"200"});

        BasicStaticDataCache<StringResource> msgCache = repositoryResource.getComponent("stringResourceCache");
        msgCache.initialize();
        BasicStaticDataCache cache = repositoryResource.getComponent("validationManager.formDefinitionCache");
        cache.initialize();
        manager =  repositoryResource.getComponent("validationManager");
        manager.initialize();

    }

    private void setUpEntityPropertyNameMode() {
        MockStringResourceHolder mock = repositoryResource.getComponent("stringResourceHolder");
        mock.setMessages(MESSAGES);

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});

        BasicStaticDataCache<StringResource> msgCache = repositoryResource.getComponent("stringResourceCache");
        msgCache.initialize();
        BasicStaticDataCache cache = repositoryResource.getComponent("validationManager2.formDefinitionCache");
        cache.initialize();
        manager =  repositoryResource.getComponent("validationManager2");
        manager.initialize();
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * バリデーションOKの値の場合、変換されたオブジェクトの属性に値が設定されていること。
     */
    @Test
    public void testValidateAndConvertValidationSuccess() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"00000001"});
        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"30"});

        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);
        User user = result.createObject();
        assertTrue(result.isValid());
        assertEquals("00000001", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(new BigDecimal(30l), user.getAge());
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * バリデーションNGとなる項目があった場合、メッセージが設定されていること。
     * また、オブジェクトの生成は失敗する事。
     * ただし、強制オブジェクト変換はバリデーションNGでも行えること。
     */
    @Test
    public void testValidateAndConvertValidationFail() {

        setUpEntityPropertyNameMode();
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"0000001"});
        params.put("name", new String[]{"123456789"});
        params.put("age", new String[]{"101"});

        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);

        assertFalse(result.isValid());
        try {
            result.createObject();
            fail("例外が発生するはず。");
        } catch (IllegalStateException e) {
            // OK
        }

        Map<String, ValidationResultMessage> messages = new HashMap<String, ValidationResultMessage>();
        for (Message message : result.getMessages()) {
            ValidationResultMessage validationMessage = (ValidationResultMessage) message;
            messages.put(validationMessage.getPropertyName(), validationMessage);
        }
        ThreadContext.setLanguage(Locale.JAPANESE);

        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00023", "IDは8文字で入力してください。", "id"));
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00021", "名前は8文字以下で入力してください。", "name"));
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00052", "年齢は0以上100以下で入力してください。", "age"));

        User dirtyUser = result.createDirtyObject();
        assertEquals("0000001", dirtyUser.getId());
        assertEquals("123456789", dirtyUser.getName());
        assertEquals(BigDecimal.valueOf(101), dirtyUser.getAge());

    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * 文字列→数値変換で失敗する場合、正しくメッセージが設定されていること。
     */
    @Test
    public void testValidateAndConvertConversionFail() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"00000001"});
        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"abc"});

        setUpEntityPropertyNameMode();
        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);
        assertFalse(result.isValid());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat(new ValidationContextMatcher.ValidationContextWrapper(
                result),
                ValidationContextMatcher.containsMessage("MSG00031",
                        "年齢は整数3桁で入力してください。", "age"));

    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * 必須の項目全てを未入力とした場合、必須エラーが全てあがること。
     */
    @Test
    public void testValidateAndConvertValidationRequired() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{""});
        params.put("name", new String[]{""});
        params.put("age", new String[]{""});

        setUpEntityPropertyNameMode();
        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);
        assertFalse(result.isValid());

        ThreadContext.setLanguage(Locale.JAPANESE);
        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(result);
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage("MSG00011", "IDは必ず入力してください。", "id"));
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage("MSG00011", "名前は必ず入力してください。", "name"));
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage("MSG00011", "年齢は必ず入力してください。", "age"));
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * プレフィックすを指定した場合でも正しく変換できること。
     */
    @Test
    public void testValidateAndConvertValidationWithPrefixSuccess() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("user.id", new String[]{"00000001"});
        params.put("user.name", new String[]{"テストユーザ"});
        params.put("user.age", new String[]{"30"});

        ValidationContext<User> result = manager.validateAndConvert("user", User.class, params, null);
        User user = result.createObject();
        assertTrue(result.isValid());
        assertEquals("00000001", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(new BigDecimal("30"), user.getAge());
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * プレフィックス指定でバリデーションエラーとなる値を設定した場合、正しくエラーが発生すること。
     */
    @Test
    public void testValidateAndConvertValidationWithPrefixFail() {

        ThreadContext.setLanguage(Locale.JAPANESE);

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("user.id", new String[]{"0000001"});
        params.put("user.name", new String[]{"123456789"});
        params.put("user.age", new String[]{"101"});

        setUpEntityPropertyNameMode();
        ValidationContext<User> result = manager.validateAndConvert("user", User.class, params, null);

        assertFalse(result.isValid());

        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00023", "IDは8文字で入力してください。", "user.id"));
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00021", "名前は8文字以下で入力してください。", "user.name"));
        assertThat(contextWrapper,
                ValidationContextMatcher.containsMessage(
                        "MSG00052", "年齢は0以上100以下で入力してください。", "user.age"));

        User object = result.createDirtyObject();
        assertEquals("0000001", object.getId());
        assertEquals("123456789", object.getName());
        assertEquals(new BigDecimal("101"), object.getAge());
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * プレフィックス指定で必須バリデーションエラーとなる値を設定した場合、正しくエラーが発生すること。
     */
    @Test
    public void testValidateAndConvertValidationWithPrefixRequired() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("user.id", new String[]{""});
        params.put("user.name", new String[]{""});
        params.put("user.age", new String[]{""});

        setUpEntityPropertyNameMode();
        ValidationContext<User> result = manager.validateAndConvert("user", User.class, params, null);
        assertFalse(result.isValid());

        ThreadContext.setLanguage(Locale.JAPANESE);
        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage(
                "MSG00011", "IDは必ず入力してください。", "user.id"));
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage(
                "MSG00011", "名前は必ず入力してください。", "user.name"));
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage(
                "MSG00011", "年齢は必ず入力してください。", "user.age"));
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * サイズ2以上の配列でもOKであること。
     */
    @Test
    public void testValidateAndConvertStringArrayParameter() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("codes", new String[]{"a", "b", "c"});

        ValidationContext<StringArrayValueHolder> result = manager.validateAndConvert("", StringArrayValueHolder.class,
                params, null);
        assertTrue(result.isValid());

        StringArrayValueHolder holder = result.createObject();
        assertArrayEquals(new String[]{"a", "b", "c"}, holder.getAcceptedCodes());
    }

    /**
     * {@link ValidationManager#initialize()}のテスト。
     * <br/>
     *
     * {@link Convertor}実装クラスの返却する変換対象クラスがnullの場合、
     * 例外が発生すること。
     */
    @Test
    public void testInitializeConvertedTypeIsNull() {
        Convertor Convertor = new Convertor() {

            public <T> boolean isConvertible(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName, Object values,
                    Annotation format) {
                return false;
            }

            public Class<?> getTargetClass() {
                return null;
            }

            public <T> Object convert(ValidationContext<T> context,
                    String propertyName, Object values, Annotation format) {
                return null;
            }
        };
        List<Convertor> Convertors = new ArrayList<Convertor>();
        Convertors.add(Convertor);
        manager.setConvertors(Convertors);
        try {
            manager.initialize();
            fail("例外が発生するはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    containsString("Convertor target class was not specified."));
        }
    }

    /**
     * {@link ValidationManager#initialize()}のテスト。
     * <br/>
     * 同一の{@link Convertor}が複数設定されている場合は、
     * 初期化処理で例外が発生すること。
     */
    @Test
    public void testInitializeConvertedTypeIsConflicted() {
        Convertor convertor = new Convertor() {

            public <T> boolean isConvertible(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName, Object values,
                    Annotation format) {
                return false;
            }

            public Class<?> getTargetClass() {
                return Integer.class;
            }

            public <T> Object convert(ValidationContext<T> context,
                    String propertyName, Object values, Annotation format) {
                return null;
            }
        };
        List<Convertor> Convertors = new ArrayList<Convertor>();
        Convertors.add(convertor);
        Convertors.add(convertor);
        manager.setConvertors(Convertors);
        try {
            manager.initialize();
            fail("例外が発生するはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    containsString("Convertor target class was conflicted."));
        }
    }

    /**
     * {@link ValidationManager#initialize()}のテスト。
     * <br/>
     * {@link Validator}クラスから取得出来るAnnotationクラスがnullの場合、
     * 例外が発生すること。
     */
    @Test
    public void testInitializePostValidatorAnnotationClassIsNull() {
        DirectCallableValidator validator = new DirectCallableValidator() {

            public Class<? extends Annotation> getAnnotationClass() {
                return null;
            }

            public <T> boolean validate(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName,
                    Annotation annotation, Object value) {
                return false;
            }

            public <T> boolean validate(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName,
                    Map<String, Object> params, Object value) {
                return false;
            }

        };
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        manager.setValidators(validators);
        try {
            manager.initialize();
            fail("例外が発生するはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString(
                    "Validator's annotation class was not specified."));
        }
    }

    /**
     * {@link ValidationManager#initialize()}のテスト。
     * <br/>
     * {@link Validator}クラスから取得出来るAnnotationクラスに、
     * {@link Validation}アノテーションが設定されていない場合、
     * 例外が発生すること。
     */
    @Test
    public void testInitializePostValidatorAnnotationClassIsNotAnnotated() {
        DirectCallableValidator validator = new DirectCallableValidator() {

            public Class<? extends Annotation> getAnnotationClass() {
                return TestAnnotation.class;
            }

            public <T> boolean validate(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName,
                    Annotation annotation, Object value) {
                return false;
            }

            public <T> boolean validate(ValidationContext<T> context,
                    String propertyName, Object propertyDisplayName,
                    Map<String, Object> params, Object value) {
                return false;
            }

        };
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        manager.setValidators(validators);
        try {
            manager.initialize();
            fail("例外が発生するはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString(
                    "Validator's annotation class was not annotated."));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * {@link ValidationManager}の初期化が完了していない場合、
     * 例外が発生すること。
     *
     */
    @Test
    public void testValidateAndConvertWithoutInitialize() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"test"});

        manager = new ValidationManager();
        try {
            manager.validateAndConvert("user", User.class, params, null);
            fail("例外が発生するはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString(
                    "ValidationManager was not initialized."));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * 変換対象のオブジェクトの属性に設定されている変換クラス({@link Convertor}実装クラス）が、
     * {@link ValidationManager}に設定されていない場合、例外が発生すること。
     */
    @Test
    public void testValidateConvertorNotRegistered() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        manager.setConvertors(new ArrayList<Convertor>());
        manager.initialize();

        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"30"});

        try {
            manager.validateAndConvert("", User.class, params, null);
            fail("例外が発生するはず");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), containsString(
                    "Property type was not supported."));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * 変換対象のオブジェクトの属性に設定されているバリデーション用のアノテーションが
     * {@link ValidationManager}に設定されていない場合、例外が発生すること。
     */
    @Test
    public void testValidateValidatorNotRegistered() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        manager.setValidators(new ArrayList<Validator>());
        manager.initialize();

        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"30"});

        try {
            manager.validateAndConvert("", User.class, params, null);
            fail("例外が発生するはず");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), containsString(
                    "Validation annotation was not supported."));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * 属性に対応する項目名がメッセージテーブルに存在しない場合、
     * プロパティ名がメッセージのプレースホルダに埋め込まれること。
     */
    @Test
    public void testNoPropertyMessageIdEntity() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        manager.initialize();

        params.put("id", new String[]{""});
        params.put("name", new String[]{""});

        ValidationContext<NoPropertyMessageIdEntity> result = manager.validateAndConvert("",
                NoPropertyMessageIdEntity.class, params, null);
        assertFalse(result.isValid());

        ThreadContext.setLanguage(Locale.JAPANESE);
        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(result);
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage("MSG00011", "idは必ず入力してください。", "id"));
        assertThat(contextWrapper, ValidationContextMatcher.containsMessage("MSG00011", "nameは必ず入力してください。", "name"));
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * バリデーションが失敗した場合でも、{@link ValidationContext#createDirtyObject()}を呼び出すことにより、
     * 強制的にオブジェクトに変換できること。
     */
    @Test
    public void testValidateCreateDirtyObject() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"0000001"});
        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"30"});

        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);
        assertFalse(result.isValid());

        User user = result.createDirtyObject();
        assertEquals("0000001", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(new BigDecimal(30l), user.getAge());
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * バリデーションが失敗した場合でも、{@link ValidationContext#createDirtyObject()}を呼び出すことにより、
     * 強制的にオブジェクトに変換できること。
     * この際に変換に失敗する項目があった場合、その項目はnullとなること。
     */
    @Test
    public void testValidateCreateDirtyObjectConvertFailed() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"0000001"});
        params.put("name", new String[]{"テストユーザ"});
        params.put("age", new String[]{"aaa"});

        ValidationContext<User> result = manager.validateAndConvert("", User.class, params, null);
        assertFalse(result.isValid());

        User user = result.createDirtyObject();
        assertEquals("0000001", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertNull(user.getAge());
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)} ()}のテスト。
     * <br/>
     * バリデーションが失敗した場合でも、{@link ValidationContext#createDirtyObject()}を呼び出すことにより、
     * 強制的にオブジェクトに変換できること。
     * この際に変換に失敗する項目があった場合、その項目はnullとなること。
     */
    @Test
    public void testPropertyNameWithValue() {

        {
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１"});
            params.put("bd1", new String[]{"1.1"});
            params.put("overridedBd", new String[]{""});
            params.put("overridedBd2", new String[]{""});


            ValidationContext<PropertyNameEntity> result = manager.validateAndConvert("", PropertyNameEntity.class,
                    params, null);
            assertTrue(result.isValid());

            PropertyNameEntity entity = result.createDirtyObject();
            assertEquals("名前０００００１", entity.getName());
            assertEquals(new BigDecimal("1.1"), entity.getBd1());
        }

        {
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１１"});
            params.put("bd1", new String[]{"1.1"});
            params.put("overridedBd", new String[]{""});
            params.put("overridedBd2", new String[]{""});

            ValidationContext<PropertyNameEntity> result = manager.validateAndConvert("", PropertyNameEntity.class,
                    params, null);

            assertFalse(result.isValid());
            assertEquals(1, result.getMessages().size());
            assertEquals("名前は8文字以下で入力してください。", result.getMessages().get(0).formatMessage(Locale.JAPANESE));
            assertEquals("名前 cannot be greater than 8 characters.",
                    result.getMessages().get(0).formatMessage(Locale.ENGLISH));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * {@link PropertyName}に項目名を識別するメッセージIDを指定している場合。
     * エラー時には想定通り、項目名がメッセージに埋め込まれること。
     */
    @Test
    public void testPropertyNameWithMessageId() {

        {
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１"});

            ValidationContext<PropertyNameWithMessageIdEntity> result = manager.validateAndConvert("",
                    PropertyNameWithMessageIdEntity.class, params, null);
            assertTrue(result.isValid());

            PropertyNameWithMessageIdEntity entity = result.createDirtyObject();
            assertEquals("名前０００００１", entity.getName());
        }

        {
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１１"});

            ValidationContext<PropertyNameWithMessageIdEntity> result = manager.validateAndConvert("",
                    PropertyNameWithMessageIdEntity.class, params, null);

            assertFalse(result.isValid());
            assertEquals(1, result.getMessages().size());
            assertEquals("名前は8文字以下で入力してください。", result.getMessages().get(0).formatMessage(Locale.JAPANESE));
            assertEquals("Name cannot be greater than 8 characters.",
                    result.getMessages().get(0).formatMessage(Locale.ENGLISH));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     */
    @Test
    public void testPropertyNameExtendedClass() {

        {
            //******************************************************************
            // 正常系
            //******************************************************************
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１"});
            params.put("remarks", new String[]{"備考０００００００１"});

            // @Digits(integer=1, fraction=1)
            // @NumberRange(min=-2, max=2)
            // になるはず
            params.put("bd1", new String[]{"-1.1"});
            // @Digits(integer=2, fraction=2)
            // @NumberRange(min=-21, max=21)
            // になるはず
            params.put("overridedBd", new String[]{"-11.11"});
            // @Digits(integer=4, fraction=4)
            // @NumberRange(min=-22, max=22)
            // になるはず
            params.put("overridedBd2", new String[]{"-11.1111"});

            ValidationContext<ExtendedEntity> result = manager.validateAndConvert("", ExtendedEntity.class, params,
                    null);
            assertTrue(result.isValid());

            ExtendedEntity entity = result.createDirtyObject();
            assertEquals("名前０００００１", entity.getName());
            assertEquals("備考０００００００１", entity.getRemarks());
            assertEquals(new BigDecimal("-1.1"), entity.getBd1());
            assertEquals(new BigDecimal("-11.11"), entity.getOverridedBd());
            assertEquals(new BigDecimal("-11.1111"), entity.getOverridedBd2());

        }

        {
            //******************************************************************
            // 期待したメッセージが設定されることを確認
            // PropertyNameに項目名を直接埋め込んでいるため、
            // 言語に関係なく埋め込まれた項目名が設定されていること。
            //******************************************************************
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１１"});
            params.put("remarks", new String[]{"備考０００００００１１"});

            // @Digits(integer=1, fraction=1)
            // @NumberRange(min=-2, max=2)
            // になるはず
            params.put("bd1", new String[]{"-3"});
            // @Digits(integer=2, fraction=2)
            // @NumberRange(min=-21, max=21)
            // になるはず
            params.put("overridedBd", new String[]{"-22.0"});
            // @Digits(integer=4, fraction=4)
            // @NumberRange(min=-22, max=22)
            // になるはず
            params.put("overridedBd2", new String[]{"-23.0"});

            ValidationContext<ExtendedEntity> result = manager.validateAndConvert("", ExtendedEntity.class, params,
                    null);

            assertFalse(result.isValid());
            List<Message> messages = result.getMessages();
            assertEquals(5, messages.size());

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "ユーザ氏名は8文字以下で入力してください。", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "備考は10文字以下で入力してください。", "remarks"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "BigDecimal1は-2以上2以下で入力してください。", "bd1"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "オーバライドしたBigDecimal1は-21以上21以下で入力してください。", "overridedBd"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "オーバライドしたBigDecimal2は-22以上22以下で入力してください。", "overridedBd2"));

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "ユーザ氏名 cannot be greater than 8 characters.", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "備考 cannot be greater than 10 characters.", "remarks"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "BigDecimal1 is not in the range -2 through 2.", "bd1"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "オーバライドしたBigDecimal1 is not in the range -21 through 21.", "overridedBd"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00052", "オーバライドしたBigDecimal2 is not in the range -22 through 22.", "overridedBd2"));
        }

        {
            //******************************************************************
            // コンバータの設定が想定通り効く(継承したクラスに書かれたアノテーションで置き換えられる)ことを確認
            //******************************************************************
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１１"});
            params.put("remarks", new String[]{"備考０００００００１１"});

            // @Digits(integer=1, fraction=1)
            // @NumberRange(min=-2, max=2)
            // になるはず
            params.put("bd1", new String[]{"-1.11"});
            // @Digits(integer=2, fraction=2)
            // @NumberRange(min=-21, max=21)
            // になるはず
            params.put("overridedBd", new String[]{"-11.111"});
            // @Digits(integer=4, fraction=4)
            // @NumberRange(min=-22, max=22)
            // になるはず
            params.put("overridedBd2", new String[]{"-11.11111"});

            ValidationContext<ExtendedEntity> result = manager.validateAndConvert("", ExtendedEntity.class, params,
                    null);

            assertFalse(result.isValid());
            List<Message> messages = result.getMessages();
            assertEquals(5, messages.size());

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "ユーザ氏名は8文字以下で入力してください。", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "備考は10文字以下で入力してください。", "remarks"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "BigDecimal1は整数部1桁、少数部1桁で入力してください。", "bd1"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "オーバライドしたBigDecimal1は整数部2桁、少数部2桁で入力してください。", "overridedBd"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "オーバライドしたBigDecimal2は整数部4桁、少数部4桁で入力してください。", "overridedBd2"));

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "ユーザ氏名 cannot be greater than 8 characters.", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "備考 cannot be greater than 10 characters.", "remarks"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "BigDecimal1 must be 1-digits and 1-digits decimal integer part.", "bd1"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "オーバライドしたBigDecimal1 must be 2-digits and 2-digits decimal integer part.", "overridedBd"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00032", "オーバライドしたBigDecimal2 must be 4-digits and 4-digits decimal integer part.", "overridedBd2"));
        }

        {
            //******************************************************************
            // 正常系
            //******************************************************************
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１"});
            params.put("remarks", new String[]{"備考０００００００１"});

            ValidationContext<PropertyNameWithMessageIdExtendedEntity> result = manager.validateAndConvert("",
                    PropertyNameWithMessageIdExtendedEntity.class, params, null);
            assertTrue(result.isValid());

            PropertyNameWithMessageIdExtendedEntity entity = result.createDirtyObject();
            assertEquals("名前０００００１", entity.getName());
            assertEquals("備考０００００００１", entity.getRemarks());
        }

        {
            //******************************************************************
            // 期待したメッセージが設定されることを確認
            // PropertyNameに項目名識別するメッセージIDを埋め込んでいる場合、
            // 言語に対応した項目名が設定されること。
            //******************************************************************
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("name", new String[]{"名前０００００１１"});
            params.put("remarks", new String[]{"備考０００００００１１"});

            ValidationContext<PropertyNameWithMessageIdExtendedEntity> result = manager.validateAndConvert("",
                    PropertyNameWithMessageIdExtendedEntity.class, params, null);


            assertFalse(result.isValid());

            List<Message> messages = result.getMessages();
            assertEquals(2, messages.size());

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "ユーザ氏名は8文字以下で入力してください。", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "備考は10文字以下で入力してください。", "remarks"));

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "User Name cannot be greater than 8 characters.", "name"));
            assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                    ValidationContextMatcher.containsMessage(
                            "MSG00021", "Remarks cannot be greater than 10 characters.", "remarks"));
        }
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * PropertyNameの指定がない場合
     */
    @Test
    public void testPropertyMessageIdWasNotFound() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"0000"});

        ValidationContext<PropertyNameNotFoundEntity> result = manager.validateAndConvert("",
                PropertyNameNotFoundEntity.class, params, null);
        assertFalse(result.isValid());

        assertEquals("idは8文字で入力してください。", result.getMessages().get(0).formatMessage(Locale.JAPANESE));
    }

    /**
     * {@link ValidationManager#validateAndConvert(String, Class, Map, String)}のテスト。
     * PropertyNameの指定がない場合
     */
    @Test
    public void testPropertyMessageIdWasNotFoundUsePropertyNameAsMessageId() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[]{"0000"});

        setUpEntityPropertyNameMode();
        try {
	        manager.validateAndConvert("",
	                PropertyNameNotFoundEntity.class, params, null);
	        fail("例外が発生するはず");
        } catch (MessageNotFoundException e) {
        	// OK
        }
    }


    static @interface TestAnnotation {

    }

    public static class User {
        private String id;
        private String name;
        private BigDecimal age;

        public User(Map<String, Object> props) {
            id = (String) props.get("id");
            name = (String) props.get("name");
            age = (BigDecimal) props.get("age");
        }

        public String getId() {
            return id;
        }

        @Required
        @Length(min = 8, max = 8)
        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        @Required
        @Length(max = 8)
        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAge() {
            return age;
        }

        @Required
        @NumberRange(min = 0, max = 100)
        @Digits(integer = 3)
        public void setAge(BigDecimal age) {
            this.age = age;
        }
    }

    public static class StringArrayValueHolder {
        public StringArrayValueHolder(Map<String, Object> params) {
            codes = (String[]) params.get("codes");
        }

        private String[] codes;

        public void setCodes(String[] acceptedCodes) {
            this.codes = acceptedCodes;
        }

        public String[] getAcceptedCodes() {
            return codes;
        }
    }

    public static class NoPropertyMessageIdEntity {

        private String id;
        private String name;

        public NoPropertyMessageIdEntity(Map<String, Object> props) {
            id = (String) props.get("id");
            name = (String) props.get("name");
        }

        public String getId() {
            return id;
        }

        @Required
        @Length(min = 8, max = 8)
        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        @Required
        @Length(max = 8)
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class PropertyNameEntity {

        private String name;
        private BigDecimal bd1;
        private BigDecimal overridedBd;
        private BigDecimal overridedBd2;

        public PropertyNameEntity(Map<String, Object> props) {
            name = (String) props.get("name");
            bd1 = (BigDecimal) props.get("bd1");
            overridedBd = (BigDecimal) props.get("overridedBd");
            overridedBd2 = (BigDecimal) props.get("overridedBd2");
        }

        public String getName() {
            return name;
        }

        @PropertyName("名前")
        @Required
        @Length(max = 8)
        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getBd1() {
            return bd1;
        }

        @PropertyName("BigDecimal1")
        @Digits(integer=1, fraction=1)
        @NumberRange(min=-2, max=2)
        public void setBd1(BigDecimal bd1) {
            this.bd1 = bd1;
        }
        public BigDecimal getOverridedBd() {
            return overridedBd;
        }

        @Digits(integer=2, fraction=2)
        @NumberRange(min=-11, max=11)
        public void setOverridedBd(BigDecimal overridedBd) {
            this.overridedBd = overridedBd;
        }

        public BigDecimal getOverridedBd2() {
            return overridedBd2;
        }


        @Digits(integer=3, fraction=3)
        @NumberRange(min=-12, max=12)
        public void setOverridedBd2(BigDecimal overridedBd2) {
            this.overridedBd2 = overridedBd2;
        }


    }

    public static class PropertyNameWithMessageIdEntity {

        private String name;

        public PropertyNameWithMessageIdEntity(Map<String, Object> props) {
            name = (String) props.get("name");
        }

        public String getName() {
            return name;
        }

        @PropertyName(messageId = "PROP0001")
        @Required
        @Length(max = 8)
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ExtendedEntity extends PropertyNameEntity {

        // 子クラスしか持っていないプロパティ
        private String remarks;

        public ExtendedEntity(Map<String, Object> props) {
            super(props);
            this.remarks = (String) props.get("remarks");
        }

        @PropertyName("ユーザ氏名")
        public void setName(String name) {
            super.setName(name);
        }

        @Required
        @Length(max = 10)
        @PropertyName("備考")
        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getRemarks() {
            return remarks;
        }

        // NumberRange のみオーバライド
        @PropertyName("オーバライドしたBigDecimal1")
        @Override
        @NumberRange(min=-21, max=21)
        public void setOverridedBd(BigDecimal overridedBd) {
            super.setOverridedBd(overridedBd);
        }

        // NumberRangeとDigitsをオーバライド
        @PropertyName("オーバライドしたBigDecimal2")
        @Override
        @Digits(integer=4, fraction=4)
        @NumberRange(min=-22, max=22)
        public void setOverridedBd2(BigDecimal overridedBd2) {
            super.setOverridedBd2(overridedBd2);
        }
    }

    public static class PropertyNameWithMessageIdExtendedEntity extends PropertyNameWithMessageIdEntity {

        // 子クラスしか持っていないプロパティ
        private String remarks;

        public PropertyNameWithMessageIdExtendedEntity(Map<String, Object> props) {
            super(props);
            this.remarks = (String) props.get("remarks");
        }

        @PropertyName(messageId = "PROP0002")
        public void setName(String name) {
            super.setName(name);
        }

        @Required
        @Length(max = 10)
        @PropertyName(messageId = "PROP0003")
        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getRemarks() {
            return remarks;
        }
    }


    public static class PropertyNameNotFoundEntity {

        private String id;

        public String getId() {
            return id;
        }

        @PropertyName(messageId = "NOTFOUND")
        @Required
        @Length(min = 8, max = 8)
        public void setId(String id) {
            this.id = id;
        }
    }
}
