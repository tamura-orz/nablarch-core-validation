package nablarch.core.validation;

import static nablarch.core.validation.ValidationContextMatcher.containsMessage;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.cache.BasicStaticDataCache;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.message.StringResource;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import nablarch.core.validation.validator.unicode.SystemChar;
import nablarch.fw.Request;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


/**
 * {@link ValidationUtil}のテストクラス。
 */
public class ValidationUtilTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "nablarch/core/validation/mock-validation-manager.xml");

    private static final String[][] CODE_NAMES = {
            {"0001", "01", "2", "en", "Male", "M", "01:Male", "0001-01-en"},
            {"0001", "02", "1", "en", "Female", "F", "02:Female", "0001-02-en"},
            {"0002", "01", "1", "en", "Initial State", "Initial", "", "0002-01-en"},
            {"0002", "02", "2", "en", "Waiting For Batch Start", "Waiting", "", "0002-02-en"},
            {"0002", "03", "3", "en", "Batch Running", "Running", "", "0002-03-en"},
            {"0002", "04", "4", "en", "Batch Execute Completed", "Completed", "", "0002-04-en"},
            {"0002", "05", "5", "en", "Batch Result Checked", "Checked", "", "0002-05-en"},
            {"0001", "01", "2", "ja", "男性", "男", "01:Male", "0001-01-ja"},
            {"0001", "02", "1", "ja", "女性", "女", "02:Female", "0001-02-ja"},
            {"0002", "01", "1", "ja", "初期状態", "初期", "", "0002-01-ja"},
            {"0002", "02", "2", "ja", "処理開始待ち", "待ち", "", "0002-02-ja"},
            {"0002", "03", "3", "ja", "処理実行中", "実行", "", "0002-03-ja"},
            {"0002", "04", "4", "ja", "処理実行完了", "完了", "", "0002-04-ja"},
            {"0002", "05", "5", "ja", "処理結果確認完了", "確認", "", "0002-05-ja"},
    };

    private static final String[][] CODE_PATTERNS = {
            {"0001", "01", "1", "0", "0"},
            {"0001", "02", "1", "0", "0"},
            {"0002", "01", "1", "0", "0"},
            {"0002", "02", "1", "0", "0"},
            {"0002", "03", "0", "1", "0"},
            {"0002", "04", "0", "1", "0"},
            {"0002", "05", "1", "0", "0"},
    };

    private static final String[][] MESSAGES = {
            {"PROP0001", "ja", "ID", "en", "ID"},
            {"PROP0002", "ja", "名前", "en", "Name"},
            {"PROP0003", "ja", "年齢", "en", "Age"},
            {"PROP0004", "ja", "性別", "en", "gender"},
            {"MSG00001", "ja", "{0}の値が不正です。", "en", "{0} value is invalid."},
            {"MSG00011", "ja", "{0}は必ず入力してください。", "en", "{0} is required."},
            {"MSG00021", "ja", "{0}は{2}文字以下で入力してください。", "en", "{0} cannot be greater than {2} characters."},
            {"MSG00022", "ja", "{0}は{1}文字以上{2}文字以下で入力してください。", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00023", "ja", "{0}は{1}文字で入力してください。", "en", "{0} length must be {1}."},
            {"MSG00031", "ja", "{0}は整数{1}桁で入力してください。", "en", "{0} length must be under {1}."},
            {"MSG00041", "ja", "{0}は整数{1}桁で入力してください。", "en", "{0} length must be under {1}."},
            {"MSG00042", "ja", "{0}は整数部{1}桁、少数部{2}桁で入力してください。", "en",
                    "{0} must be {1}-digits and {1}-digits decimal integer part."},
            {"MSG00051", "ja", "{0}は{2}以下で入力してください。", "en", "{0} cannot be greater than {2}."},
            {"MSG00052", "ja", "{0}は{1}以上{2}以下で入力してください。", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00053", "ja", "{0}は{1}以上{2}以下で入力してください。", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00061", "ja", "項目間バリデーションエラーメッセージ。", "en", "inter property check error message."},
            {"MSG00071", "ja", "入力値が不正です。", "en", "input value is invalid."},
            {"MSG00081", "ja", "サイズキーが不正です。", "en", "size key is invalid."},
            {"MSG00091", "ja", "エラーメッセージサンプル１。", "en", "sample error message1."},
            {"MSG00092", "ja", "エラーメッセージサンプル２ [{0}][{1}]。", "en", "sample error message2  [{0}][{1}]."},
            {"MSG00093", "ja", "エラーメッセージサンプル３ [{0,number,#.00}]。", "en", "sample error message3  [{0,number,#.00}]."},
            {"MSG00094", "ja", "エラーメッセージサンプル４。", "en", "sample error message4."},
            {"MSG00095", "ja", "コードテーブルに無い不正な入力値です。", "en", "an illegal input."},
    };

    /**
     * 各テストメソッドのセットアップ処理。
     * <br/>
     * 本テストクラスで使用するためのリポジトリを構築する。
     */
    @Before
    public void setUpRepository() {
        repositoryResource.getComponentByType(MockStringResourceHolder.class)
                          .setMessages(MESSAGES);

        BasicStaticDataCache cache =
                (BasicStaticDataCache) repositoryResource.getComponent("validationManager.formDefinitionCache");
        cache.initialize();
        ValidationManager manager = repositoryResource.getComponentByType(ValidationManager.class);
        manager.initialize();
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map)}のテスト。
     * <br/>
     * リポジトリが不正な場合は、エラーとなること。
     */
    @Test
    public void testGetValidationManagerFail() {
        // 不正なリポジトリを構築するために、リポジトリの状態をクリアにする。
        SystemRepository.clear();
        try {
            ValidationUtil.validateAndConvertRequest("", User.class, new HashMap<String, String[]>(), "validateAll");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("validationManager"));
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * 指定したバリデーション対象のメソッドでバリデーションが行われていること。
     */
    @Test
    public void testValidateAndConvertValidUser() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        // idは正しくない値だが、ValidateForアノテーションで指定したメソッドではバリデーション対象外。
        params.put("id", new String[] {"0"});
        params.put("name", new String[] {"テストユーザ"});
        params.put("age", new String[] {"100"});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(User.class, params, "insert");
        assertTrue("バリデーションはOK", result.isValid());
        assertFalse("idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));

        User user = result.createObject();

        // バリデーション対象外の値はセットされない。
        assertNull(user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(Long.valueOf(100), user.getAge());
    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map, String)}のテスト。
     * <br/>
     * 指定したバリデーション対象のメソッドで、指定したプレフィックスで始まる値のバリデーションが行われていること。
     */
    @Test
    public void testValidateAndConvertWithPrefixValidUser() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("user.id", new String[] {"123456789"});
        params.put("user.name", new String[] {"テストユーザ"});
        params.put("user.age", new String[] {"30"});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest("user", User.class, params, "insert");

        assertTrue("バリデーションはOK", result.isValid());
        assertFalse("idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));

        User user = result.createObject();

        // バリデーション対象外の値はセットされない。
        assertNull(user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(Long.valueOf(30), user.getAge());


        List<Message> messageList = new ArrayList<Message>();
        messageList.add(new ValidationResultMessage("hoge.hoge", new StringResource() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getValue(Locale locale) {
                return "message";
            }
        }, new Object[0]));
        result.addMessages(messageList);
        assertThat("メッセージが増えていること", result.getMessages()
                                          .size(), is(1));
        ValidationResultMessage message = (ValidationResultMessage) result.getMessages()
                                                                          .get(0);
        assertThat(message.getPropertyName(), is("hoge.hoge"));
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * 指定したプロパティのバリデーションのみ行われ、バリデーションエラーの情報が取得出来ること。
     */
    @Test
    public void testValidateAndConvertInvalidUser() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"123456789"});
        params.put("name", new String[] {"123456789"});
        params.put("age", new String[] {"101"});

        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(User.class, params, "insert");
        assertFalse("バリデーションはNG", result.isValid());
        assertFalse("idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("id"));
        assertTrue("nameプロパティのバリデーションはinvalid", result.isInvalid("name"));
        assertTrue("ageプロパティのバリデーションはinvalid", result.isInvalid("age"));

        ThreadContext.setLanguage(Locale.JAPANESE);
        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        assertThat(contextWrapper, containsMessage("MSG00021",
                "名前は8文字以下で入力してください。", "name"));
        assertThat(contextWrapper, containsMessage("MSG00052",
                "年齢は0以上100以下で入力してください。", "age"));
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * 指定された項目以外がバリデーションされ、エラー情報が正しく設定されていること。
     */
    @Test
    public void testValidateWithout() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"123456789"});
        params.put("age", new String[] {"101"});

        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(User.class, params, "without");

        assertFalse(result.isValid());
        assertFalse("idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("id"));
        assertTrue("nameプロパティのバリデーションはinvalid", result.isInvalid("name"));
        assertTrue("ageプロパティのバリデーションはinvalid", result.isInvalid("age"));

        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat(contextWrapper, containsMessage("MSG00021",
                "名前は8文字以下で入力してください。", "name"));
        assertThat(contextWrapper, containsMessage("MSG00052",
                "年齢は0以上100以下で入力してください。", "age"));

    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * すべての項目がバリデーションされ、エラー情報が正しく設定されていること。
     */
    @Test
    public void testValidateAll() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"12345678"});
        params.put("age", new String[] {"101"});

        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(User.class, params, "validateAll");

        assertFalse(result.isValid());
        assertTrue("idプロパティのバリデーションはinvalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertTrue("ageプロパティのバリデーションはinvalid", result.isInvalid("age"));

        ValidationContextMatcher.ValidationContextWrapper contextWrapper = new ValidationContextMatcher.ValidationContextWrapper(
                result);
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat(contextWrapper, containsMessage("MSG00023",
                "IDは8文字で入力してください。", "id"));
        assertThat(contextWrapper, containsMessage("MSG00052",
                "年齢は0以上100以下で入力してください。", "age"));
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * バリデーション処理中に例外が発生した場合、RuntimeExceptionが送出されること。
     */
    @Test(expected = RuntimeException.class)
    public void testValidateFail() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"100"});

        ValidationUtil.validateAndConvertRequest(User.class, params, "fail");
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map, String)}のテスト。
     * <br/>
     * バリデーション処理中に任意のメッセージIDを設定した場合、そのメッセージが設定されていること。
     */
    @Test
    public void testValidateAddMessage() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"100"});

        ValidationContext<User> context = ValidationUtil.validateAndConvertRequest(User.class, params, "addMessage");
        assertFalse(context.isValid());

        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat("メッセージID及びメッセージIDが設定されていること。",
                new ValidationContextMatcher.ValidationContextWrapper(context), containsMessage(
                        "MSG00061", "項目間バリデーションエラーメッセージ。"));
    }

    @Test
    public void testManualValidation() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"百"});
        params.put("gender", new String[] {"男性"});

        // 宣言されたバリデーションルールが行えること
        ValidationContext<User> context = ValidationUtil.validateAndConvertRequest(User.class, params, "manual");
        assertFalse(context.isValid());
        assertEquals(1, context.getMessages()
                               .size());

        ThreadContext.setLanguage(Locale.JAPANESE);

        assertThat(
                "アノテーションベースのバリデーションが行われていること。",
                new ValidationContextMatcher.ValidationContextWrapper(context),
                containsMessage("MSG00031", "年齢は整数3桁で入力してください。", "age")
        );

        params.put("age", new String[] {"100"});
        params.put("gender", new String[] {"01"});
        context = ValidationUtil.validateAndConvertRequest(User.class, params, "manual");
        assertTrue(context.isValid());


        params.put("age", new String[] {""});
        params.put("gender", new String[] {""});
        context = ValidationUtil.validateAndConvertRequest(User.class, params, "manual2");
        assertFalse(context.isValid());
        assertEquals(2, context.getMessages()
                               .size());

        assertThat(
                "アノテーションベースのバリデーションが行われていること。",
                new ValidationContextMatcher.ValidationContextWrapper(context),
                containsMessage("MSG00011", "年齢は必ず入力してください。", "age")
        );
        assertThat(
                "追加のバリデーションが実施されていること。",
                new ValidationContextMatcher.ValidationContextWrapper(context),
                containsMessage("MSG00011", "性別は必ず入力してください。", "gender")
        );
    }

    @Test
    public void testValidateWithNotProperlyStructuredValidators1() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"百"});
        params.put("gender", new String[] {"男性"});

        try {
            ValidationUtil.validateAndConvertRequest(User.class, params, "erroneous1");
            fail();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause()
                               .getCause();
            assertTrue(cause instanceof UnsupportedOperationException);
            assertTrue(cause.getMessage()
                            .startsWith("Validation annotation was not supported."));
        }
    }

    @Test
    public void testValidateWithNotProperlyStructuredValidators2() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"百"});
        params.put("gender", new String[] {"男性"});

        try {
            ValidationUtil.validateAndConvertRequest(User.class, params, "erroneous2");
            fail();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause()
                               .getCause();
            assertTrue(cause instanceof UnsupportedOperationException);
            assertTrue(cause.getMessage()
                            .startsWith(
                                    "a Validator must implement 'DirectCallableValidator' if you want to call it in program code."
                            ));
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト。
     * <br/>
     * バリデーション処理中に任意のメッセージIDとプロパティ名を指定した場合で、
     * プロパティ名がnullの場合例外が発生すること。
     */
    @Test
    public void testValidateNullPropertyResultMessage() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"0000001"});
        params.put("name", new String[] {"test"});
        params.put("age", new String[] {"100"});

        try {
            ValidationUtil.validateAndConvertRequest(User.class, params,
                    "nullPropertyName");
            fail("例外が発生するはず");
        } catch (RuntimeException e) {
            assertTrue(e.getCause()
                        .getCause() instanceof IllegalArgumentException);
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト。
     * <br/>
     * バリデーションメソッド名にnullを指定した場合、例外が発生すること。
     */
    @Test(expected = RuntimeException.class)
    public void testCreateFail() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("id", new String[] {"00000000"});
        ValidationContext<InstantiationFailEntity> result = ValidationUtil.validateAndConvertRequest(
                InstantiationFailEntity.class, params, null);

        result.createObject();
    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map)}のテスト。
     * <br/>
     * 全ての項目がバリデーションOKの場合、入力値がObjectの各属性に設定されること。
     */
    @Test
    public void testValidateAndConvertWithoutPrefixAndMethod() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        // 全ての項目にバリデーションOKな値を設定
        params.put("id", new String[] {"00000000"});
        params.put("name", new String[] {"テストユーザ"});
        params.put("age", new String[] {"100"});
        params.put("gender", new String[] {"01"});
        params.put("systemChar1", new String[] {""});
        params.put("systemChar2", new String[] {""});
        params.put("systemChar3", new String[] {""});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest("", User.class, params,
                "validateAll");
        User user = result.createObject();

        assertTrue("バリデーションは、OKとなる。", result.isValid());
        assertFalse("idプロパティのバリデーションはnot invalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));

        // 各属性に入力値が設定される。
        assertEquals("00000000", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(Long.valueOf(100), user.getAge());

    }

    /**
     * {@link ValidationUtil#validateAndConvert(Class, Map)}のテスト。
     * <br/>
     * バリデーション対象の項目の中で、１項目のみバリデーションエラーとなる場合、
     * エラーメッセージ、エラー項目が正しく取得できること。
     */
    @Test
    public void testValidateAndConvertFailWithoutPrefixAndMethod() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        // idは正しくない値だが、ValidateForアノテーションで指定したメソッドではバリデーション対象外。
        params.put("id", new String[] {"0000000"});       // バリデーションエラーとなる項目
        params.put("name", new String[] {"テストユーザ"});
        params.put("age", new String[] {"100"});
        params.put("gender", new String[] {"01"});
        params.put("systemChar1", new String[] {""});
        params.put("systemChar2", new String[] {""});
        params.put("systemChar3", new String[] {""});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest("", User.class, params,
                "validateAll");

        assertFalse("バリデーションエラーとなる", result.isValid());
        assertTrue("idプロパティのバリデーションはinvalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));

        // バリデーション対象外の値はセットされない。
        assertEquals(1, result.getMessages()
                              .size());

        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat(new ValidationContextMatcher.ValidationContextWrapper(result),
                containsMessage("MSG00023", "IDは8文字で入力してください。", "id"));

        try {
            result.abortIfInvalid();
            fail("とおらない。");
        } catch (ApplicationException e) {
            List<Message> messages = e.getMessages();
            assertThat(messages.size(), is(1));
            assertThat(messages.get(0)
                               .formatMessage(), is("IDは8文字で入力してください。"));
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト。
     * <br/>
     * 全ての項目が正常値の場合。バリデーションがOKで、生成されたオブジェクトからは期待した値が取得出来ること。
     */
    @Test
    public void testValidateAndConvertWithoutMethod() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("user.id", new String[] {"00000000"});
        params.put("user.name", new String[] {"テストユーザ"});
        params.put("user.age", new String[] {"100"});
        params.put("user.gender", new String[] {"01"});
        params.put("user.systemChar1", new String[] {""});
        params.put("user.systemChar2", new String[] {""});
        params.put("user.systemChar3", new String[] {""});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest("user", User.class, params,
                "validateAll");
        User user = result.createObject();

        assertTrue(result.isValid());
        assertFalse("idプロパティのバリデーションはnot invalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));
        assertEquals("00000000", user.getId());
        assertEquals("テストユーザ", user.getName());
        assertEquals(Long.valueOf(100), user.getAge());

        try {
            result.abortIfInvalid();
        } catch (ApplicationException ignored) {
            fail("バリデーションエラーはないので、ここは通らない。");
        }

    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト
     * <br/>
     * バリデーションエラーとなった場合、想定通りメッセージが設定されていること。
     */
    @Test
    public void testValidateAndConvertFailWithoutMethod() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        // idは正しくない値だが、ValidateForアノテーションで指定したメソッドではバリデーション対象外。
        params.put("user.id", new String[] {"1234567"});
        params.put("user.name", new String[] {"テストユーザ"});
        params.put("user.age", new String[] {"100"});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(
                "user", User.class, params, "validateAll");
        assertFalse(result.isValid());
        assertTrue("idプロパティのバリデーションはinvalid", result.isInvalid("id"));
        assertFalse("nameプロパティのバリデーションはnot invalid", result.isInvalid("name"));
        assertFalse("ageプロパティのバリデーションはnot invalid", result.isInvalid("age"));

        ThreadContext.setLanguage(Locale.JAPANESE);
        assertThat(new ValidationContextMatcher.ValidationContextWrapper(result), containsMessage(
                "MSG00023", "IDは8文字で入力してください。", "user.id"));
    }

    /**
     * {@link ValidationUtil#validateAll(String, Class, Map)}のテスト
     * <br/>
     * 再帰的にバリデーションを実行できること。<br/>
     *
     * @ValidationTarget#validateForが指定された場合、指定されたバリデーションが呼ばれること。<br/>
     */
    @Test
    public void testRecursive() {

        // RecursivePropertyEntity の User 型 child プロパティには
        // @ValidationTarget(validateFor="insert") アノテーションがついている。
        {
            // 正常動作
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.child.id", new String[] {"00000001"});
            params.put("prefix.child.name", new String[] {"テストユーザ"});
            params.put("prefix.child.age", new String[] {"100"});

            ValidationContext<RecursivePropertyEntity> result
                    = ValidationUtil.validateAndConvertRequest(prefix, RecursivePropertyEntity.class, params, "child");

            assertTrue(result.isValid());
            assertFalse("childプロパティのバリデーションはnot invalid", result.isInvalid("child"));
            assertFalse("child.idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("child.id"));
            assertFalse("child.nameプロパティのバリデーションはnot invalid", result.isInvalid("child.name"));
            assertFalse("child.ageプロパティのバリデーションはnot invalid", result.isInvalid("child.age"));

            RecursivePropertyEntity entity = result.createObject();
            // validateForがchildの場合、Userクラスの "insert" を使うので、idはnull
            assertNull(entity.getChild()
                             .getId());
            assertEquals("テストユーザ", entity.getChild()
                                         .getName());
            assertEquals(Long.valueOf(100), entity.getChild()
                                                  .getAge());
        }
        {
            // バリデーションエラー動作
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.child.id", new String[] {"00000001"});
            params.put("prefix.child.name", new String[] {"テストユーザ"});
            params.put("prefix.child.age", new String[] {"a"});

            ValidationContext<RecursivePropertyEntity> context = ValidationUtil.validateAndConvertRequest(prefix,
                    RecursivePropertyEntity.class, params, "child");

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00031",
                                    "年齢は整数3桁で入力してください。", "prefix.child.age"));

            ValidationContext<RecursivePropertyEntity> result = getValidationContext();
            assertTrue("childプロパティのバリデーションはinvalid", result.isInvalid("child"));
            assertFalse("child.idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("child.id"));
            assertFalse("child.nameプロパティのバリデーションはnot invalid", result.isInvalid("child.name"));
            assertTrue("child.ageプロパティのバリデーションはinvalid", result.isInvalid("child.age"));
        }
    }

    /**
     * {@link ValidationUtil#validateAll(Class, Map)}のテスト
     * <br/>
     * 再帰的にバリデーションを実行できること。<br/>
     *
     * @ValidationTarget#validateForが指定された場合、指定されたバリデーションが呼ばれること。<br/>
     */
    @Test
    public void testRecursiveWithoutPrefix() {
        {
            // プレフィクスなし版のメソッドのテスト
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("child.id", new String[] {"00000001"});
            params.put("child.name", new String[] {"テストユーザ"});
            params.put("child.age", new String[] {"100"});

            ValidationContext<RecursivePropertyEntity> result
                    = ValidationUtil.validateAndConvertRequest(RecursivePropertyEntity.class, params, "child");

            assertTrue(result.isValid());
            assertFalse("childプロパティのバリデーションはnot invalid", result.isInvalid("child"));
            assertFalse("child.idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("child.id"));
            assertFalse("child.nameプロパティのバリデーションはnot invalid", result.isInvalid("child.name"));
            assertFalse("child.ageプロパティのバリデーションはnot invalid", result.isInvalid("child.age"));

            RecursivePropertyEntity entity = result.createObject();
            // validateForがchildの場合、Userクラスの "insert" を使うので、idはnull
            assertNull(entity.getChild()
                             .getId());
            assertEquals("テストユーザ", entity.getChild()
                                         .getName());
            assertEquals(Long.valueOf(100), entity.getChild()
                                                  .getAge());
        }

    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト
     * <br/>
     * 配列(固定幅)のプロパティにも再帰的にバリデーションを実行できること。<br/>
     *
     * @ValidationTarget#validateForが指定された場合、指定されたバリデーションが呼ばれること。<br/>
     */
    @Test
    public void testRecursiveArray() {

        // RecursivePropertyEntity の User 型 children プロパティには
        // @ValidationTarget(size=3, validateFor="insert") アノテーションがついている。
        {
            // 正常動作
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            params.put("prefix.children[2].age", new String[] {"3"});

            ValidationContext<RecursivePropertyEntity> result
                    = ValidationUtil.validateAndConvertRequest(prefix, RecursivePropertyEntity.class, params,
                    "children");

            assertTrue(result.isValid());
            for (int i = 0; i < 3; i++) {
                assertFalse("children[" + i + "]プロパティのバリデーションはnot invalid", result.isInvalid("children[" + i + "]"));
                assertFalse("children[" + i + "].idプロパティのバリデーションは対象外のためnot invalid",
                        result.isInvalid("children[" + i + "].id"));
                assertFalse("children[" + i + "].nameプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].name"));
                assertFalse("children[" + i + "].ageプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].age"));
            }

            RecursivePropertyEntity entity = result.createObject();
            assertNull(entity.getChildren()[0].getId());
            assertEquals("テストユーザ１", entity.getChildren()[0].getName());
            assertEquals(Long.valueOf(1), entity.getChildren()[0].getAge());

            assertNull(entity.getChildren()[1].getId());
            assertEquals("テストユーザ２", entity.getChildren()[1].getName());
            assertEquals(Long.valueOf(2), entity.getChildren()[1].getAge());

            assertNull(entity.getChildren()[2].getId());
            assertEquals("テストユーザ３", entity.getChildren()[2].getName());
            assertEquals(Long.valueOf(3), entity.getChildren()[2].getAge());
        }
        {
            // エラー動作(バリデーションエラーの場合)
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            // このプロパティでエラー
            params.put("prefix.children[2].age", new String[] {"a"});


            ValidationContext<RecursivePropertyEntity> context = ValidationUtil.validateAndConvertRequest(prefix,
                    RecursivePropertyEntity.class, params, "children");

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00031",
                                    "年齢は整数3桁で入力してください。", "prefix.children[2].age"));

            ValidationContext<RecursivePropertyEntity> result = getValidationContext();
            for (int i = 0; i < 2; i++) {
                assertFalse("children[" + i + "]プロパティのバリデーションはnot invalid", result.isInvalid("children[" + i + "]"));
                assertFalse("children[" + i + "].idプロパティのバリデーションは対象外のためnot invalid",
                        result.isInvalid("children[" + i + "].id"));
                assertFalse("children[" + i + "].nameプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].name"));
                assertFalse("children[" + i + "].ageプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].age"));
            }
            assertTrue("children[2]プロパティのバリデーションはinvalid", result.isInvalid("children[2]"));
            assertFalse("children[2].idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("children[2].id"));
            assertFalse("children[2].nameプロパティのバリデーションはnot invalid", result.isInvalid("children[2].name"));
            assertTrue("children[2].ageプロパティのバリデーションはinvalid", result.isInvalid("children[2].age"));
        }
        {
            // エラー動作(配列長が短い場合)
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000001"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});


            ValidationContext<RecursivePropertyEntity> context = ValidationUtil.validateAndConvertRequest(prefix,
                    RecursivePropertyEntity.class, params, "children");

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00011",
                                    "名前は必ず入力してください。"
                                    , "prefix.children[2].name"));
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00011",
                                    "年齢は必ず入力してください。"
                                    , "prefix.children[2].age"));

            ValidationContext<RecursivePropertyEntity> result = getValidationContext();
            for (int i = 0; i < 2; i++) {
                assertFalse("children[" + i + "]プロパティのバリデーションはnot invalid", result.isInvalid("children[" + i + "]"));
                assertFalse("children[" + i + "].idプロパティのバリデーションは対象外のためnot invalid",
                        result.isInvalid("children[" + i + "].id"));
                assertFalse("children[" + i + "].nameプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].name"));
                assertFalse("children[" + i + "].ageプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].age"));
            }
            assertTrue("children[2]プロパティのバリデーションはinvalid", result.isInvalid("children[2]"));
            assertFalse("children[2].idプロパティのバリデーションは対象外のためnot invalid", result.isInvalid("children[2].id"));
            assertTrue("children[2].nameプロパティのバリデーションはinvalid", result.isInvalid("children[2].name"));
            assertTrue("children[2].ageプロパティのバリデーションはinvalid", result.isInvalid("children[2].age"));
        }
        {
            // 正常動作(配列長が長い場合、指定以降のプロパティは無視する)
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            params.put("prefix.children[2].age", new String[] {"3"});
            params.put("prefix.children[3].id", new String[] {"00000004"});
            params.put("prefix.children[3].name", new String[] {"テストユーザ４"});
            params.put("prefix.children[3].age", new String[] {"4"});


            ValidationContext<RecursivePropertyEntity> result
                    = ValidationUtil.validateAndConvertRequest(prefix, RecursivePropertyEntity.class, params,
                    "children");

            assertTrue(result.isValid());
            for (int i = 0; i < 4; i++) {
                assertFalse("children[" + i + "]プロパティのバリデーションはnot invalid", result.isInvalid("children[" + i + "]"));
                assertFalse("children[" + i + "].idプロパティのバリデーションは対象外のためnot invalid",
                        result.isInvalid("children[" + i + "].id"));
                assertFalse("children[" + i + "].nameプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].name"));
                assertFalse("children[" + i + "].ageプロパティのバリデーションはnot invalid",
                        result.isInvalid("children[" + i + "].age"));
            }

            RecursivePropertyEntity entity = result.createObject();
            assertNull(entity.getChildren()[0].getId());
            assertEquals("テストユーザ１", entity.getChildren()[0].getName());
            assertEquals(Long.valueOf(1), entity.getChildren()[0].getAge());

            assertNull(entity.getChildren()[1].getId());
            assertEquals("テストユーザ２", entity.getChildren()[1].getName());
            assertEquals(Long.valueOf(2), entity.getChildren()[1].getAge());

            assertNull(entity.getChildren()[2].getId());
            assertEquals("テストユーザ３", entity.getChildren()[2].getName());
            assertEquals(Long.valueOf(3), entity.getChildren()[2].getAge());
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト
     * <br/>
     * 2つ以上のEntityに同時に再帰的バリデーションを実行できること。<br/>
     */
    @Test
    public void testRecursiveTwoEntity() {

        {
            // 正常動作
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            params.put("prefix.children[2].age", new String[] {"3"});

            params.put("prefix.child.id", new String[] {"00000001"});
            params.put("prefix.child.name", new String[] {"テストユーザ"});
            params.put("prefix.child.age", new String[] {"100"});

            ValidationContext<RecursivePropertyEntity> result
                    = ValidationUtil.validateAndConvertRequest(prefix, RecursivePropertyEntity.class, params, "both");


            RecursivePropertyEntity entity = result.createObject();
            // validateForがchildの場合、Userクラスの "insert" を使うので、idはnull
            assertNull(entity.getChild()
                             .getId());
            assertEquals("テストユーザ", entity.getChild()
                                         .getName());
            assertEquals(Long.valueOf(100), entity.getChild()
                                                  .getAge());

            assertNull(entity.getChildren()[0].getId());
            assertEquals("テストユーザ１", entity.getChildren()[0].getName());
            assertEquals(Long.valueOf(1), entity.getChildren()[0].getAge());

            assertNull(entity.getChildren()[1].getId());
            assertEquals("テストユーザ２", entity.getChildren()[1].getName());
            assertEquals(Long.valueOf(2), entity.getChildren()[1].getAge());

            assertNull(entity.getChildren()[2].getId());
            assertEquals("テストユーザ３", entity.getChildren()[2].getName());
            assertEquals(Long.valueOf(3), entity.getChildren()[2].getAge());
        }
    }


    // 以下TODO

    /**
     * {@link ValidationUtil#validateAndConvert(String, Class, Map)}のテスト
     * <br/>
     * 配列(動的幅)のプロパティにも再帰的にバリデーションを実行できること。<br/>
     *
     * @ValidationTarget#validateForが指定された場合、指定されたバリデーションが呼ばれること。<br/>
     */
    @Test
    public void testRecursiveArrayWithSizeKey() {
        {
            // プレフィクスをString[]で指定する場合の正常系。
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("prefix.childrenSizeKey", new String[] {"3"});
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            params.put("prefix.children[2].age", new String[] {"3"});

            ValidationContext<ArrayWithSizeKeyForm> result
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");

            assertTrue(result.isValid());
            ArrayWithSizeKeyForm form = result.createObject();

            assertEquals(3, form.getChildren().length);
            assertEquals("テストユーザ１", form.getChildren()[0].getName());
            assertEquals("テストユーザ２", form.getChildren()[1].getName());
            assertEquals("テストユーザ３", form.getChildren()[2].getName());
        }
        {
            // プレフィクスをString[]で、sizeKeyの限界を超えた場合。
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("prefix.childrenSizeKey", new String[] {"10"});

            ValidationContext<ArrayWithSizeKeyForm> context
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
            assertEquals(1, context.getMessages()
                                   .size());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00081",
                                    "サイズキーが不正です。"
                                    , "prefix.childrenSizeKey"));
        }
        {
            // サイズキーがString[]で数値じゃない場合
            String prefix = "prefix";
            Map<String, String[]> params = new HashMap<String, String[]>();

            params.put("prefix.childrenSizeKey", new String[] {"a"});

            ValidationContext<ArrayWithSizeKeyForm> context
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
            assertEquals(1, context.getMessages()
                                   .size());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00081",
                                    "サイズキーが不正です。"
                                    , "prefix.childrenSizeKey"));
        }

        {
            // プレフィクスをStringで指定する場合の正常系。
            String prefix = "prefix";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("prefix.childrenSizeKey", "3");
            params.put("prefix.children[0].id", new String[] {"00000001"});
            params.put("prefix.children[0].name", new String[] {"テストユーザ１"});
            params.put("prefix.children[0].age", new String[] {"1"});
            params.put("prefix.children[1].id", new String[] {"00000002"});
            params.put("prefix.children[1].name", new String[] {"テストユーザ２"});
            params.put("prefix.children[1].age", new String[] {"2"});
            params.put("prefix.children[2].id", new String[] {"00000003"});
            params.put("prefix.children[2].name", new String[] {"テストユーザ３"});
            params.put("prefix.children[2].age", new String[] {"3"});

            ValidationContext<ArrayWithSizeKeyForm> result
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");

            assertTrue(result.isValid());
            ArrayWithSizeKeyForm form = result.createObject();

            assertEquals(3, form.getChildren().length);
            assertEquals("テストユーザ１", form.getChildren()[0].getName());
            assertEquals("テストユーザ２", form.getChildren()[1].getName());
            assertEquals("テストユーザ３", form.getChildren()[2].getName());
        }
        {
            // プレフィクスをStringで指定する際に、sizeKeyの限界を超えた場合。
            String prefix = "prefix";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("prefix.childrenSizeKey", "10");

            ValidationContext<ArrayWithSizeKeyForm> context
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
            assertEquals(1, context.getMessages()
                                   .size());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00081",
                                    "サイズキーが不正です。"
                                    , "prefix.childrenSizeKey"));
        }
        {
            // サイズキーがStringで数値じゃない場合
            String prefix = "prefix";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("prefix.childrenSizeKey", "a");

            ValidationContext<ArrayWithSizeKeyForm> context
                    = ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
            assertEquals(1, context.getMessages()
                                   .size());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(context)
                    , ValidationContextMatcher
                            .containsMessage("MSG00081",
                                    "サイズキーが不正です。"
                                    , "prefix.childrenSizeKey"));
        }
        {
            // サイズキーがStringでもString[]でもない場合
            String prefix = "prefix";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("prefix.childrenSizeKey", 1);
            try {
                ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
                fail("例外が発生するはず");
            } catch (RuntimeException e) {
                // OK
            }

            params.put("prefix.childrenSizeKey", null);
            try {
                ValidationUtil.validateAndConvertRequest(prefix, ArrayWithSizeKeyForm.class, params, "children");
                fail("例外が発生するはず");
            } catch (RuntimeException e) {
                // OK
            }

        }
    }

    /**
     * {@link ValidationUtil#validateAndConvertRequest(String, Class, Validatable, String)} のテスト
     */
    @Test
    public void testValidateAndConvertRequest() {

        {
            // 正常系
            String prefix = "prefix";
            final Map<String, Object> params = new HashMap<String, Object>() {
                {

                    put("prefix.childrenSizeKey", new String[] {"3"});
                    put("prefix.children[0].id", new String[] {"00000001"});
                    put("prefix.children[0].name", new String[] {"テストユーザ１"});
                    put("prefix.children[0].age", new String[] {"1"});
                    put("prefix.children[1].id", new String[] {"00000002"});
                    put("prefix.children[1].name", new String[] {"テストユーザ２"});
                    put("prefix.children[1].age", new String[] {"2"});
                    put("prefix.children[2].id", new String[] {"00000003"});
                    put("prefix.children[2].name", new String[] {"テストユーザ３"});
                    put("prefix.children[2].age", new String[] {"3"});
                }
            };

            class RequestDummy implements Request<Object>, Validatable<Object> {

                public Object getParam(String name) {
                    return null;
                }

                public Map<String, Object> getParamMap() {
                    return params;
                }

                public String getRequestPath() {
                    return null;
                }

                public Request<Object> setRequestPath(
                        String requestPath) {
                    return null;
                }

            }
            Validatable<Object> request = new RequestDummy();

            ValidationContext<ArrayWithSizeKeyForm> result = ValidationUtil.validateAndConvertRequest(prefix,
                    ArrayWithSizeKeyForm.class, request, "children");

            assertTrue(result.isValid());
            ArrayWithSizeKeyForm form = result.createObject();

            assertEquals(3, form.getChildren().length);
            assertEquals("テストユーザ１", form.getChildren()[0].getName());
            assertEquals("テストユーザ２", form.getChildren()[1].getName());
            assertEquals("テストユーザ３", form.getChildren()[2].getName());

        }
        {
            // 異常系（中身はオーバロードしたvalidateAndConvertRequestと一緒なので、1パターンだけにします。）
            String prefix = "prefix";
            final Map<String, Object> params = new HashMap<String, Object>() {
                {

                    put("prefix.childrenSizeKey", new String[] {"3"});
                    put("prefix.children[0].id", new String[] {"00000001"});
                    put("prefix.children[0].name", new String[] {"テストユーザ１"});
                    put("prefix.children[0].age", new String[] {"1"});
                    put("prefix.children[1].id", new String[] {"00000002"});
                    put("prefix.children[1].name", new String[] {"テストユーザ２"});
                    put("prefix.children[1].age", new String[] {"2"});
                    put("prefix.children[2].id", new String[] {"00000003"});
                    put("prefix.children[2].name", new String[] {"テストユーザ３"});
                    // ここでバリデーションエラー
                    put("prefix.children[2].age", new String[] {"a"});
                }
            };

            class RequestDummy implements Request<Object>, Validatable<Object> {

                public Object getParam(String name) {
                    return null;
                }

                public Map<String, Object> getParamMap() {
                    return params;
                }

                public String getRequestPath() {
                    return null;
                }

                public Request<Object> setRequestPath(String requestPath) {
                    return null;
                }

            }
            Validatable<Object> request = new RequestDummy();

            ValidationContext<ArrayWithSizeKeyForm> result = ValidationUtil.validateAndConvertRequest(prefix,
                    ArrayWithSizeKeyForm.class, request, "children");

            assertFalse(result.isValid());

            assertThat(
                    new ValidationContextMatcher.ValidationContextWrapper(result)
                    , ValidationContextMatcher
                            .containsMessage("MSG00031",
                                    "年齢は整数3桁で入力してください。", "prefix.children[2].age"));
        }
    }

    /**
     * {@link ValidationUtil#validateAndConvertRequest(Class, Validatable, String)} のテスト
     */
    @Test
    public void testValidateAndConvertRequestWithoutPrefix() {

        {
            // 正常系
            final Map<String, Object> params = new HashMap<String, Object>() {
                {

                    put("childrenSizeKey", new String[] {"3"});
                    put("children[0].id", new String[] {"00000001"});
                    put("children[0].name", new String[] {"テストユーザ１"});
                    put("children[0].age", new String[] {"1"});
                    put("children[1].id", new String[] {"00000002"});
                    put("children[1].name", new String[] {"テストユーザ２"});
                    put("children[1].age", new String[] {"2"});
                    put("children[2].id", new String[] {"00000003"});
                    put("children[2].name", new String[] {"テストユーザ３"});
                    put("children[2].age", new String[] {"3"});
                }
            };

            class RequestDummy implements Request<Object>, Validatable<Object> {

                public Object getParam(String name) {
                    return null;
                }

                public Map<String, Object> getParamMap() {
                    return params;
                }

                public String getRequestPath() {
                    return null;
                }

                public Request<Object> setRequestPath(
                        String requestPath) {
                    return null;
                }

            }
            Validatable<Object> request = new RequestDummy();

            ValidationContext<ArrayWithSizeKeyForm> result = ValidationUtil.validateAndConvertRequest(
                    ArrayWithSizeKeyForm.class, request, "children");

            assertTrue(result.isValid());
            ArrayWithSizeKeyForm form = result.createObject();

            assertEquals(3, form.getChildren().length);
            assertEquals("テストユーザ１", form.getChildren()[0].getName());
            assertEquals("テストユーザ２", form.getChildren()[1].getName());
            assertEquals("テストユーザ３", form.getChildren()[2].getName());

        }
    }

    @Test
    public void testSystemCharAnnotation() {
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("systemChar1", new String[] {"0"});
        input.put("systemChar2", new String[] {"1"});
        input.put("systemChar3", new String[] {"あ"});
        ValidationContext<User> result = ValidationUtil.validateAndConvertRequest(User.class, input,
                "validateSystemChar");
        List<Message> messages = result.getMessages();
        assertThat("systemCharの3項目がエラーになる", messages.size(), is(3));

        for (Message message : messages) {
            ValidationResultMessage validationResultMessage = (ValidationResultMessage) message;
            if ("systemChar1".equals(validationResultMessage.getPropertyName())) {
                // systemChar1は、アノテーションにメッセージIDを設定しているので、
                // アノテーションのメッセージIDが設定されている。
                assertThat(validationResultMessage.getMessageId(), is("MSG00091"));
            } else if ("systemChar2".equals(validationResultMessage.getPropertyName())) {
                // systemChar2は、アノテーションにメッセージIDを設定していないので、
                // リポジトリのCharsetDefのメッセージIDが設定されている。
                assertThat(validationResultMessage.getMessageId(), is("MSG00093"));
            } else if ("systemChar3".equals(validationResultMessage.getPropertyName())) {
                // systemChar3は、アノテーション、CharsetDefともにメッセージIDが設定されていないので、
                // SystemCharValidatorに設定されたメッセージIDとなる。
                assertThat(validationResultMessage.getMessageId(), is("MSG00094"));
            } else {
                fail("ここにはこない。");
            }
        }

        // メッセージを追加
        List<Message> messageList = new ArrayList<Message>();
        messageList.add(new Message(MessageLevel.ERROR, new StringResource() {
            @Override
            public String getId() {
                return "";
            }

            @Override
            public String getValue(Locale locale) {
                return "message";
            }
        }));
        result.addMessages(messageList);
        assertThat("メッセージが増えていること", result.getMessages()
                                          .size(), is(4));
    }

    @Test
    public void testCreateMessageForProperty() {
        {
            // オプションパラメータなし、プロパティ名はプレフィクスなし。
            Message message = ValidationUtil.createMessageForProperty("exampleProperty", "MSG00091");

            assertTrue(message instanceof ValidationResultMessage);
            ValidationResultMessage vMessage = (ValidationResultMessage) message;

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("sample error message1.", vMessage.formatMessage());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals("エラーメッセージサンプル１。", vMessage.formatMessage());
            assertEquals("exampleProperty", vMessage.getPropertyName());
        }

        {
            // オプションパラメータなし、プロパティ名はプレフィクス＋プロパティ名
            Message message = ValidationUtil.createMessageForProperty("test.exampleProperty", "MSG00091");

            assertTrue(message instanceof ValidationResultMessage);
            ValidationResultMessage vMessage = (ValidationResultMessage) message;

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("sample error message1.", vMessage.formatMessage());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals("エラーメッセージサンプル１。", vMessage.formatMessage());
            assertEquals("test.exampleProperty", vMessage.getPropertyName());
        }

        {
            // オプションパラメータ２つ、プロパティ名はプレフィクス＋プロパティ名
            Message message = ValidationUtil.createMessageForProperty("test.exampleProperty",
                    "MSG00092",
                    MessageUtil.getStringResource("PROP0001"),
                    MessageUtil.getStringResource("PROP0002"));

            assertTrue(message instanceof ValidationResultMessage);
            ValidationResultMessage vMessage = (ValidationResultMessage) message;

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("sample error message2  [ID][Name].", vMessage.formatMessage());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals("エラーメッセージサンプル２ [ID][名前]。", vMessage.formatMessage());
            assertEquals("test.exampleProperty", vMessage.getPropertyName());
        }

        {
            // オプションパラメータ１つ、メッセージフォーマットが効いていること。
            Message message = ValidationUtil.createMessageForProperty("test.exampleProperty",
                    "MSG00093",
                    BigDecimal.valueOf(10));

            assertTrue(message instanceof ValidationResultMessage);
            ValidationResultMessage vMessage = (ValidationResultMessage) message;

            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("sample error message3  [10.00].", vMessage.formatMessage());
            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals("エラーメッセージサンプル３ [10.00]。", vMessage.formatMessage());
            assertEquals("test.exampleProperty", vMessage.getPropertyName());
        }

    }

    /**
     * テストで使用するBeanオブジェクト。
     */
    public static class User {

        private String id;

        private String name;

        private Long age;

        private String gender;

        private String systemChar1;

        private String systemChar2;

        private String systemChar3;

        public User(Map<String, Object> props) {
            id = (String) props.get("id");
            name = (String) props.get("name");
            age = (Long) props.get("age");
            gender = (String) props.get("gender");
            systemChar1 = (String) props.get("systemChar1");
            systemChar2 = (String) props.get("systemChar2");
            systemChar3 = (String) props.get("systemChar3");
        }

        public String getId() {
            return id;
        }

        @PropertyName(messageId = "PROP0001")
        @Required
        @Length(min = 8, max = 8)
        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        @PropertyName(messageId = "PROP0002")
        @Required
        @Length(max = 8)
        public void setName(String name) {
            this.name = name;
        }

        public Long getAge() {
            return age;
        }

        @PropertyName(messageId = "PROP0003")
        @Required
        @NumberRange(min = 0, max = 100)
        @Digits(integer = 3)
        public void setAge(Long age) {
            this.age = age;
        }

        @PropertyName(messageId = "PROP0004")
        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getGender() {
            return this.gender;
        }

        @SystemChar(charsetDef = "char1", messageId = "MSG00091")
        public void setSystemChar1(String systemChar1) {
            this.systemChar1 = systemChar1;
        }

        @SystemChar(charsetDef = "char2")
        public void setSystemChar2(String systemChar2) {
            this.systemChar2 = systemChar2;
        }

        @SystemChar(charsetDef = "char3")
        public void setSystemChar3(String systemChar3) {
            this.systemChar3 = systemChar3;
        }

        private static final String[] INSERT_PARAMS = new String[] {"name", "age"};

        @ValidateFor({"insert", "child", "children", "both"})
        public static void validateForInsert(ValidationContext<User> context) {
            ValidationUtil.validate(context, INSERT_PARAMS);
        }

        // 2度同じプロパティに対してバリデーションが実行されないことを確認するためのメソッド
        @ValidateFor("insert")
        public static void validateForInsert2(ValidationContext<User> context) {
            ValidationUtil.validate(context, INSERT_PARAMS);
        }

        private static final String[] WITHOUT_PARAM = new String[] {"id"};

        @ValidateFor("without")
        public static void validateForWithout(ValidationContext<User> context) {
            ValidationUtil.validateWithout(context, WITHOUT_PARAM);
        }

        @ValidateFor("fail")
        public static void validateForFail(ValidationContext<User> context) {
            throw new RuntimeException("fail!!!");
        }

        private static final String[] INVALID_PARAM = new String[] {"invalidParamName"};

        @ValidateFor("invalid")
        public static void validateForHidden(ValidationContext<User> context) {
            ValidationUtil.validate(context, INVALID_PARAM);
        }

        @ValidateFor("addMessage")
        public static void validateForAddMessage(ValidationContext<User> context) {
            context.addMessage("MSG00061");
        }

        @ValidateFor("nullPropertyName")
        public static void validateForNullPropertyName(ValidationContext<User> context) {
            context.addResultMessage(null, "MSG00061");
        }

        @ValidateFor("validateAll")
        public static void validateAll(ValidationContext<User> context) {
            ValidationUtil.validateAll(context);
        }

        @ValidateFor("validateSystemChar")
        public static void validateSystemChar(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"systemChar1", "systemChar2", "systemChar3"});
        }

        @ValidateFor("manual")
        public static void validateManually(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"name", "age", "gender"});
        }

        @ValidateFor("manual2")
        public static void validateManually2(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"name", "age", "gender"});
            // 必須バリデーションを追加実行
            ValidationUtil.validate(context, "gender", Required.class);
        }

        @ValidateFor("erroneous1")
        public static void erroneousValidation1(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"name", "age", "gender"});
            // 対応するValidatorが登録されていない。
            ValidationUtil.validate(context, "name", Erroneous1.class);
        }

        @ValidateFor("erroneous2")
        public static void erroneousValidation2(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"name", "age", "gender"});
            // 対応するValidatorが直接呼び出しに対応していない。
            ValidationUtil.validate(context, "name", Erroneous2.class);
        }
    }


    public static class InstantiationFailEntity {

        private String id;

        public InstantiationFailEntity() {
            throw new RuntimeException("test");
        }

        @PropertyName(messageId = "PROP0001")
        @Required
        @Length(min = 8, max = 8)
        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static class RecursivePropertyEntity {

        private User child;

        private User[] children;

        public User getChild() {
            return child;
        }

        public RecursivePropertyEntity(Map<String, Object> props) {
            this.child = (User) props.get("child");
            this.children = (User[]) props.get("children");
        }

        @ValidationTarget()
        public void setChild(User child) {
            this.child = child;
        }

        public User[] getChildren() {
            return children;
        }

        @ValidationTarget(size = 3)
        public void setChildren(User[] children) {
            this.children = children;
        }

        @ValidateFor("child")
        public static void validateForChild(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"child"});
        }

        @ValidateFor("children")
        public static void validateForChildren(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"children"});
        }

        @ValidateFor("both")
        public static void validateForMulti(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"child", "children"});
        }
    }

    public static class ArrayWithSizeKeyForm {

        private User[] children;

        public ArrayWithSizeKeyForm(Map<String, Object> props) {
            this.children = (User[]) props.get("children");
        }

        public User[] getChildren() {
            return children;
        }

        @ValidationTarget(sizeKey = "childrenSizeKey")
        public void setChildren(User[] children) {
            this.children = children;
        }

        @ValidateFor("children")
        public static void validateForChildren(ValidationContext<User> context) {
            ValidationUtil.validate(context, new String[] {"children"});
        }
    }

    public static class RecursivePropertyWithoutSizeEntity {

        private User[] children;

        public User[] getChildren() {
            return children;
        }

        @ValidationTarget(sizeKey = "childrenNum")
        public void setChildren(User[] children) {
            this.children = children;
        }

        @ValidateFor("insert")
        public static void validateForAddMessage(ValidationContext<User> context) {

        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ValidationContext<T> getValidationContext() {
        return ((MockValidationManager) SystemRepository.get("validationManager")).getValidationContext();
    }


}

