package nablarch.core.validation.domain;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.core.message.Message;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationManager;
import nablarch.core.validation.ValidationResultMessage;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.Validator;
import nablarch.core.validation.domain.sample.DirectCallableDomainValidator;
import nablarch.core.validation.domain.sample.SampleForm;
import nablarch.core.validation.domain.sample.TestForm;
import nablarch.core.validation.domain.sample.UnusedDomainSampleForm;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * ドメイン名を指定したバリデーションに関連するクラスを結合したテスト。
 * <p/>
 * ドメイン名指定のバリデーション処理を網羅的にテストする。
 *
 * @author Kiyohito Itoh
 */
public class DomainValidationTest {

    @ClassRule
    public static SystemRepositoryResource repositoryResource = new SystemRepositoryResource("nablarch/core/validation/domain/sample/domain-validation-test.xml");

    private static final String[][] CODE_NAMES = {
        { "0002", "01", "1", "ja", "初期状態", "初期", "", "0002-01-ja" },
        { "0002", "02", "2", "ja", "処理開始待ち", "待ち", "", "0002-02-ja" },
        { "0002", "03", "3", "ja", "処理実行中", "実行", "", "0002-03-ja" },
        { "0002", "04", "4", "ja", "処理実行完了", "完了", "", "0002-04-ja" },
        { "0002", "05", "5", "ja", "処理結果確認完了", "確認", "", "0002-05-ja" },
    };

    private static final String[][] CODE_PATTERNS = {
        { "0002", "01", "1", "0", "0" },
        { "0002", "02", "1", "0", "0" },
        { "0002", "03", "1", "1", "0" },
        { "0002", "04", "1", "1", "0" },
        { "0002", "05", "1", "0", "0" },
    };

    /**
     * 各テストメソッドのセットアップ処理。
     * <br/>
     * 本テストクラスで使用するためのリポジトリを構築する。
     */
    @Before
    public void setUpRepository() {
        ValidationManager manager = repositoryResource.getComponent("validationManager");
        manager.initialize();
    }

    private static final String[][] MESSAGES;

    static {
        int length = 15;
        String[][] messages = new String[length][];
        for (int i = 0; i < length; i++) {
            String id = "M" + StringUtil.lpad(String.valueOf(i + 1), 3, '0');
            // {"M001", "ja", "M001-ja", "en", "M001-en"}
            messages[i] = new String[] {id, "ja", id + "-ja", "en", id + "-en"};
        }
        MESSAGES = messages;
    }

    @Before
    public void setUp() throws Exception {
        MockStringResourceHolder resourceHolder = SystemRepository.get("stringResourceHolder");
        resourceHolder.setMessages(MESSAGES);
    }

    /**
     * パラメータがない場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testNoParametersUsingDomain() {
        testNoParameters(SampleForm.class);
    }

    /**
     * パラメータがない場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testNoParametersUnusedDomain() {
        testNoParameters(UnusedDomainSampleForm.class);
    }

    private static void testNoParameters(Class<?> formClass) {
        ValidationContext context = ValidationUtil.validateAndConvertRequest(
                                                        "sample", formClass,
                                                        new HashMap<String, String[]>() {{
                                                            put("sample.sampleDtosSize", new String[] {"3"}); // sizeKeyは必須。
                                                        }},
                                                        "test");
        assertFalse(context.isValid());
        assertMessages(context.getMessages(),"sample.freeText", "M001",
                                             "sample.userId", "M001",
                                             // "sample.userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.name", "M001",
                                             "sample.date", "M001",
                                             "sample.status", "M001",
                                             "sample.status2", "M001",
                                             "sample.registered", "M006",
                                             "sample.estimate", "M005",
                                             "sample.score", "M005",

                                             "sample.sampleDto.freeText", "M001",
                                             "sample.sampleDto.userId", "M001",
                                             // "sample.sampleDto.userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.sampleDto.name", "M001",
                                             "sample.sampleDto.date", "M001",
                                             "sample.sampleDto.status", "M001",
                                             "sample.sampleDto.status2", "M001",
                                             "sample.sampleDto.registered", "M006",
                                             "sample.sampleDto.estimate", "M005",
                                             "sample.sampleDto.score", "M005",

                                             "sample.fixedLengthSampleDtos[0].freeText", "M001",
                                             "sample.fixedLengthSampleDtos[0].userId", "M001",
                                             // "sample.fixedLengthSampleDtos[0].userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.fixedLengthSampleDtos[0].name", "M001",
                                             "sample.fixedLengthSampleDtos[0].date", "M001",
                                             "sample.fixedLengthSampleDtos[0].status", "M001",
                                             "sample.fixedLengthSampleDtos[0].status2", "M001",
                                             "sample.fixedLengthSampleDtos[0].registered", "M006",
                                             "sample.fixedLengthSampleDtos[0].estimate", "M005",
                                             "sample.fixedLengthSampleDtos[0].score", "M005",

                                             "sample.fixedLengthSampleDtos[1].freeText", "M001",
                                             "sample.fixedLengthSampleDtos[1].userId", "M001",
                                             // "sample.fixedLengthSampleDtos[1].userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.fixedLengthSampleDtos[1].name", "M001",
                                             "sample.fixedLengthSampleDtos[1].date", "M001",
                                             "sample.fixedLengthSampleDtos[1].status", "M001",
                                             "sample.fixedLengthSampleDtos[1].status2", "M001",
                                             "sample.fixedLengthSampleDtos[1].registered", "M006",
                                             "sample.fixedLengthSampleDtos[1].estimate", "M005",
                                             "sample.fixedLengthSampleDtos[1].score", "M005",

                                             "sample.variableLengthSampleDtos[0].freeText", "M001",
                                             "sample.variableLengthSampleDtos[0].userId", "M001",
                                             // "sample.variableLengthSampleDtos[0].userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.variableLengthSampleDtos[0].name", "M001",
                                             "sample.variableLengthSampleDtos[0].date", "M001",
                                             "sample.variableLengthSampleDtos[0].status", "M001",
                                             "sample.variableLengthSampleDtos[0].status2", "M001",
                                             "sample.variableLengthSampleDtos[0].registered", "M006",
                                             "sample.variableLengthSampleDtos[0].estimate", "M005",
                                             "sample.variableLengthSampleDtos[0].score", "M005",

                                             "sample.variableLengthSampleDtos[1].freeText", "M001",
                                             "sample.variableLengthSampleDtos[1].userId", "M001",
                                             // "sample.variableLengthSampleDtos[1].userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.variableLengthSampleDtos[1].name", "M001",
                                             "sample.variableLengthSampleDtos[1].date", "M001",
                                             "sample.variableLengthSampleDtos[1].status", "M001",
                                             "sample.variableLengthSampleDtos[1].status2", "M001",
                                             "sample.variableLengthSampleDtos[1].registered", "M006",
                                             "sample.variableLengthSampleDtos[1].estimate", "M005",
                                             "sample.variableLengthSampleDtos[1].score", "M005",

                                             "sample.variableLengthSampleDtos[2].freeText", "M001",
                                             "sample.variableLengthSampleDtos[2].userId", "M001",
                                             // "sample.variableLengthSampleDtos[2].userIds", "XXX", StringArrayConvertor(String[])はパラメータがなくてもエラーとならない。
                                             "sample.variableLengthSampleDtos[2].name", "M001",
                                             "sample.variableLengthSampleDtos[2].date", "M001",
                                             "sample.variableLengthSampleDtos[2].status", "M001",
                                             "sample.variableLengthSampleDtos[2].status2", "M001",
                                             "sample.variableLengthSampleDtos[2].registered", "M006",
                                             "sample.variableLengthSampleDtos[2].estimate", "M005",
                                             "sample.variableLengthSampleDtos[2].score", "M005");
    }

    /**
     * 全てのパラメータが空文字の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testAllEmptyValuesUsingDomain() {
        testAllEmptyValues(SampleForm.class);
    }

    /**
     * 全てのパラメータが空文字の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testAllEmptyValuesUnusedDomain() {
        testAllEmptyValues(UnusedDomainSampleForm.class);
    }

    private void testAllEmptyValues(Class<? extends TestForm> formClass) {
        ValidationContext context = validate(formClass);
        assertFalse(context.isValid());
        assertMessages(context.getMessages(),"sample.userId", "M007",
                                             "sample.registered", "M006",

                                             "sample.sampleDto.userId", "M007",
                                             "sample.sampleDto.registered", "M006",

                                             "sample.fixedLengthSampleDtos[0].userId", "M007",
                                             "sample.fixedLengthSampleDtos[0].registered", "M006",

                                             "sample.fixedLengthSampleDtos[1].userId", "M007",
                                             "sample.fixedLengthSampleDtos[1].registered", "M006",

                                             "sample.variableLengthSampleDtos[0].userId", "M007",
                                             "sample.variableLengthSampleDtos[0].registered", "M006",

                                             "sample.variableLengthSampleDtos[1].userId", "M007",
                                             "sample.variableLengthSampleDtos[1].registered", "M006",

                                             "sample.variableLengthSampleDtos[1].userId", "M007",
                                             "sample.variableLengthSampleDtos[1].registered", "M006");
    }

    /**
     * 必須パラメータのみ指定された場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testOnlyRequiredValuesUsingDomain() {
        testOnlyRequiredValues(SampleForm.class);
    }

    /**
     * 必須パラメータのみ指定された場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testOnlyRequiredValuesUnusedDomain() {
        testOnlyRequiredValues(UnusedDomainSampleForm.class);
    }

    private void testOnlyRequiredValues(Class<? extends TestForm> formClass) {
        ValidationContext<? extends TestForm> context = validate(formClass,
                                                         "sample.userId", "1234567890",
                                                         "sample.registered", "false", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.sampleDto.userId", "1234567891",
                                                         "sample.sampleDto.registered", "true", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.fixedLengthSampleDtos[0].userId", "1234567892",
                                                         "sample.fixedLengthSampleDtos[0].registered", "false", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.fixedLengthSampleDtos[1].userId", "1234567893",
                                                         "sample.fixedLengthSampleDtos[1].registered", "true", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.variableLengthSampleDtos[0].userId", "1234567894",
                                                         "sample.variableLengthSampleDtos[0].registered", "false", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.variableLengthSampleDtos[1].userId", "1234567895",
                                                         "sample.variableLengthSampleDtos[1].registered", "true", // BooleanConvertorはRequiredがなくてもパラメータ必須。

                                                         "sample.variableLengthSampleDtos[2].userId", "1234567896",
                                                         "sample.variableLengthSampleDtos[2].registered", "false"); // BooleanConvertorはRequiredがなくてもパラメータ必須。
        assertTrue(context.isValid());
        TestForm form = context.createObject();
        assertThat(form.getFreeText(), is(""));
        assertThat(form.getUserId(), is("1234567890"));
        assertThat(form.getUserIds().length, is(1));
        assertThat(form.getUserIds()[0], is(""));
        assertThat(form.getName(), is(""));
        assertThat(form.getDate(), is(""));
        assertThat(form.getStatus(), is(""));
        assertThat(form.getStatus2(), is(""));
        assertFalse(form.getRegistered());
        assertNull(form.getEstimate());
        assertNull(form.getScore());

        assertThat(form.getSampleDto().getFreeText(), is(""));
        assertThat(form.getSampleDto().getUserId(), is("1234567891"));
        assertThat(form.getSampleDto().getUserIds().length, is(1));
        assertThat(form.getSampleDto().getUserIds()[0], is(""));
        assertThat(form.getSampleDto().getName(), is(""));
        assertThat(form.getSampleDto().getDate(), is(""));
        assertThat(form.getSampleDto().getStatus(), is(""));
        assertThat(form.getSampleDto().getStatus2(), is(""));
        assertTrue(form.getSampleDto().getRegistered());
        assertNull(form.getSampleDto().getEstimate());
        assertNull(form.getSampleDto().getScore());

        assertThat(form.getFixedLengthSampleDtos()[0].getFreeText(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserId(), is("1234567892"));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds().length, is(1));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds()[0], is(""));
        assertThat(form.getFixedLengthSampleDtos()[0].getName(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[0].getDate(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[0].getStatus(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[0].getStatus2(), is(""));
        assertFalse(form.getFixedLengthSampleDtos()[0].getRegistered());
        assertNull(form.getFixedLengthSampleDtos()[0].getEstimate());
        assertNull(form.getFixedLengthSampleDtos()[0].getScore());

        assertThat(form.getFixedLengthSampleDtos()[1].getFreeText(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserId(), is("1234567893"));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds().length, is(1));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds()[0], is(""));
        assertThat(form.getFixedLengthSampleDtos()[1].getName(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[1].getDate(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[1].getStatus(), is(""));
        assertThat(form.getFixedLengthSampleDtos()[1].getStatus2(), is(""));
        assertTrue(form.getFixedLengthSampleDtos()[1].getRegistered());
        assertNull(form.getFixedLengthSampleDtos()[1].getEstimate());
        assertNull(form.getFixedLengthSampleDtos()[1].getScore());

        assertThat(form.getVariableLengthSampleDtos()[0].getFreeText(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserId(), is("1234567894"));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds().length, is(1));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds()[0], is(""));
        assertThat(form.getVariableLengthSampleDtos()[0].getName(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[0].getDate(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[0].getStatus(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[0].getStatus2(), is(""));
        assertFalse(form.getVariableLengthSampleDtos()[0].getRegistered());
        assertNull(form.getVariableLengthSampleDtos()[0].getEstimate());
        assertNull(form.getVariableLengthSampleDtos()[0].getScore());

        assertThat(form.getVariableLengthSampleDtos()[1].getFreeText(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserId(), is("1234567895"));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds().length, is(1));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds()[0], is(""));
        assertThat(form.getVariableLengthSampleDtos()[1].getName(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[1].getDate(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[1].getStatus(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[1].getStatus2(), is(""));
        assertTrue(form.getVariableLengthSampleDtos()[1].getRegistered());
        assertNull(form.getVariableLengthSampleDtos()[1].getEstimate());
        assertNull(form.getVariableLengthSampleDtos()[1].getScore());

        assertThat(form.getVariableLengthSampleDtos()[2].getFreeText(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserId(), is("1234567896"));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds().length, is(1));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds()[0], is(""));
        assertThat(form.getVariableLengthSampleDtos()[2].getName(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[2].getDate(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[2].getStatus(), is(""));
        assertThat(form.getVariableLengthSampleDtos()[2].getStatus2(), is(""));
        assertFalse(form.getVariableLengthSampleDtos()[2].getRegistered());
        assertNull(form.getVariableLengthSampleDtos()[2].getEstimate());
        assertNull(form.getVariableLengthSampleDtos()[2].getScore());
    }

    /**
     * 全てのパラメータが正常値の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testAllValidValuesUsingDomain() {
        testAllValidValues(SampleForm.class);
    }

    /**
     * 全てのパラメータが正常値の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testAllValidValuesUnusedDomain() {
        testAllValidValues(UnusedDomainSampleForm.class);
    }

    private static void testAllValidValues(Class<? extends TestForm> formClass) {
        ValidationContext<? extends TestForm> context = validate(formClass,
                                                         "sample.freeText", "フリーテキストのテスト",
                                                         "sample.userId", "1234567890",
                                                         "sample.userIds", "a123456789,b123456789,c123456789",
                                                         "sample.name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.date", "2014-12-31",
                                                         "sample.status", "05",
                                                         "sample.status2", "04",
                                                         "sample.registered", "true",
                                                         "sample.estimate", "123.45",
                                                         "sample.score", "89",

                                                         "sample.sampleDto.freeText", "フリーテキストのテスト",
                                                         "sample.sampleDto.userId", "1234567890",
                                                         "sample.sampleDto.userIds", "a123456789,b123456789,c123456789",
                                                         "sample.sampleDto.name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.sampleDto.date", "2014-12-31",
                                                         "sample.sampleDto.status", "05",
                                                         "sample.sampleDto.status2", "04",
                                                         "sample.sampleDto.registered", "true",
                                                         "sample.sampleDto.estimate", "123.45",
                                                         "sample.sampleDto.score", "89",

                                                         "sample.fixedLengthSampleDtos[0].freeText", "フリーテキストのテスト",
                                                         "sample.fixedLengthSampleDtos[0].userId", "1234567890",
                                                         "sample.fixedLengthSampleDtos[0].userIds", "a123456789,b123456789,c123456789",
                                                         "sample.fixedLengthSampleDtos[0].name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.fixedLengthSampleDtos[0].date", "2014-12-31",
                                                         "sample.fixedLengthSampleDtos[0].status", "05",
                                                         "sample.fixedLengthSampleDtos[0].status2", "04",
                                                         "sample.fixedLengthSampleDtos[0].registered", "true",
                                                         "sample.fixedLengthSampleDtos[0].estimate", "123.45",
                                                         "sample.fixedLengthSampleDtos[0].score", "89",

                                                         "sample.fixedLengthSampleDtos[1].freeText", "フリーテキストのテスト",
                                                         "sample.fixedLengthSampleDtos[1].userId", "1234567890",
                                                         "sample.fixedLengthSampleDtos[1].userIds", "a123456789,b123456789,c123456789",
                                                         "sample.fixedLengthSampleDtos[1].name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.fixedLengthSampleDtos[1].date", "2014-12-31",
                                                         "sample.fixedLengthSampleDtos[1].status", "05",
                                                         "sample.fixedLengthSampleDtos[1].status2", "04",
                                                         "sample.fixedLengthSampleDtos[1].registered", "true",
                                                         "sample.fixedLengthSampleDtos[1].estimate", "123.45",
                                                         "sample.fixedLengthSampleDtos[1].score", "89",

                                                         "sample.variableLengthSampleDtos[0].freeText", "フリーテキストのテスト",
                                                         "sample.variableLengthSampleDtos[0].userId", "1234567890",
                                                         "sample.variableLengthSampleDtos[0].userIds", "a123456789,b123456789,c123456789",
                                                         "sample.variableLengthSampleDtos[0].name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.variableLengthSampleDtos[0].date", "2014-12-31",
                                                         "sample.variableLengthSampleDtos[0].status", "05",
                                                         "sample.variableLengthSampleDtos[0].status2", "04",
                                                         "sample.variableLengthSampleDtos[0].registered", "true",
                                                         "sample.variableLengthSampleDtos[0].estimate", "123.45",
                                                         "sample.variableLengthSampleDtos[0].score", "89",

                                                         "sample.variableLengthSampleDtos[1].freeText", "フリーテキストのテスト",
                                                         "sample.variableLengthSampleDtos[1].userId", "1234567890",
                                                         "sample.variableLengthSampleDtos[1].userIds", "a123456789,b123456789,c123456789",
                                                         "sample.variableLengthSampleDtos[1].name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.variableLengthSampleDtos[1].date", "2014-12-31",
                                                         "sample.variableLengthSampleDtos[1].status", "05",
                                                         "sample.variableLengthSampleDtos[1].status2", "04",
                                                         "sample.variableLengthSampleDtos[1].registered", "true",
                                                         "sample.variableLengthSampleDtos[1].estimate", "123.45",
                                                         "sample.variableLengthSampleDtos[1].score", "89",

                                                         "sample.variableLengthSampleDtos[2].freeText", "フリーテキストのテスト",
                                                         "sample.variableLengthSampleDtos[2].userId", "1234567890",
                                                         "sample.variableLengthSampleDtos[2].userIds", "a123456789,b123456789,c123456789",
                                                         "sample.variableLengthSampleDtos[2].name", "abcdefghijabcdefghijabcdefghijabcdefghij",
                                                         "sample.variableLengthSampleDtos[2].date", "2014-12-31",
                                                         "sample.variableLengthSampleDtos[2].status", "05",
                                                         "sample.variableLengthSampleDtos[2].status2", "04",
                                                         "sample.variableLengthSampleDtos[2].registered", "true",
                                                         "sample.variableLengthSampleDtos[2].estimate", "123.45",
                                                         "sample.variableLengthSampleDtos[2].score", "89");
        assertTrue(context.isValid());
        TestForm form = context.createObject();
        assertThat(form.getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getUserId(), is("1234567890"));
        assertThat(form.getUserIds().length, is(3));
        assertThat(form.getUserIds()[0], is("a123456789"));
        assertThat(form.getUserIds()[1], is("b123456789"));
        assertThat(form.getUserIds()[2], is("c123456789"));
        assertThat(form.getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getDate(), is("2014-12-31"));
        assertThat(form.getStatus(), is("05"));
        assertThat(form.getStatus2(), is("04"));
        assertTrue(form.getRegistered());
        assertThat(form.getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getScore(), is(Integer.valueOf(89)));

        assertThat(form.getSampleDto().getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getSampleDto().getUserId(), is("1234567890"));
        assertThat(form.getSampleDto().getUserIds().length, is(3));
        assertThat(form.getSampleDto().getUserIds()[0], is("a123456789"));
        assertThat(form.getSampleDto().getUserIds()[1], is("b123456789"));
        assertThat(form.getSampleDto().getUserIds()[2], is("c123456789"));
        assertThat(form.getSampleDto().getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getSampleDto().getDate(), is("2014-12-31"));
        assertThat(form.getSampleDto().getStatus(), is("05"));
        assertThat(form.getSampleDto().getStatus2(), is("04"));
        assertTrue(form.getSampleDto().getRegistered());
        assertThat(form.getSampleDto().getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getSampleDto().getScore(), is(Integer.valueOf(89)));

        assertThat(form.getFixedLengthSampleDtos()[0].getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserId(), is("1234567890"));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds().length, is(3));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds()[0], is("a123456789"));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds()[1], is("b123456789"));
        assertThat(form.getFixedLengthSampleDtos()[0].getUserIds()[2], is("c123456789"));
        assertThat(form.getFixedLengthSampleDtos()[0].getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getFixedLengthSampleDtos()[0].getDate(), is("2014-12-31"));
        assertThat(form.getFixedLengthSampleDtos()[0].getStatus(), is("05"));
        assertThat(form.getFixedLengthSampleDtos()[0].getStatus2(), is("04"));
        assertTrue(form.getFixedLengthSampleDtos()[0].getRegistered());
        assertThat(form.getFixedLengthSampleDtos()[0].getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getFixedLengthSampleDtos()[0].getScore(), is(Integer.valueOf(89)));

        assertThat(form.getFixedLengthSampleDtos()[1].getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserId(), is("1234567890"));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds().length, is(3));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds()[0], is("a123456789"));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds()[1], is("b123456789"));
        assertThat(form.getFixedLengthSampleDtos()[1].getUserIds()[2], is("c123456789"));
        assertThat(form.getFixedLengthSampleDtos()[1].getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getFixedLengthSampleDtos()[1].getDate(), is("2014-12-31"));
        assertThat(form.getFixedLengthSampleDtos()[1].getStatus(), is("05"));
        assertThat(form.getFixedLengthSampleDtos()[1].getStatus2(), is("04"));
        assertTrue(form.getFixedLengthSampleDtos()[1].getRegistered());
        assertThat(form.getFixedLengthSampleDtos()[1].getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getFixedLengthSampleDtos()[1].getScore(), is(Integer.valueOf(89)));

        assertThat(form.getVariableLengthSampleDtos()[0].getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserId(), is("1234567890"));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds().length, is(3));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds()[0], is("a123456789"));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds()[1], is("b123456789"));
        assertThat(form.getVariableLengthSampleDtos()[0].getUserIds()[2], is("c123456789"));
        assertThat(form.getVariableLengthSampleDtos()[0].getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getVariableLengthSampleDtos()[0].getDate(), is("2014-12-31"));
        assertThat(form.getVariableLengthSampleDtos()[0].getStatus(), is("05"));
        assertThat(form.getVariableLengthSampleDtos()[0].getStatus2(), is("04"));
        assertTrue(form.getVariableLengthSampleDtos()[0].getRegistered());
        assertThat(form.getVariableLengthSampleDtos()[0].getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getVariableLengthSampleDtos()[0].getScore(), is(Integer.valueOf(89)));

        assertThat(form.getVariableLengthSampleDtos()[1].getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserId(), is("1234567890"));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds().length, is(3));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds()[0], is("a123456789"));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds()[1], is("b123456789"));
        assertThat(form.getVariableLengthSampleDtos()[1].getUserIds()[2], is("c123456789"));
        assertThat(form.getVariableLengthSampleDtos()[1].getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getVariableLengthSampleDtos()[1].getDate(), is("2014-12-31"));
        assertThat(form.getVariableLengthSampleDtos()[1].getStatus(), is("05"));
        assertThat(form.getVariableLengthSampleDtos()[1].getStatus2(), is("04"));
        assertTrue(form.getVariableLengthSampleDtos()[1].getRegistered());
        assertThat(form.getVariableLengthSampleDtos()[1].getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getVariableLengthSampleDtos()[1].getScore(), is(Integer.valueOf(89)));

        assertThat(form.getVariableLengthSampleDtos()[2].getFreeText(), is("フリーテキストのテスト"));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserId(), is("1234567890"));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds().length, is(3));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds()[0], is("a123456789"));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds()[1], is("b123456789"));
        assertThat(form.getVariableLengthSampleDtos()[2].getUserIds()[2], is("c123456789"));
        assertThat(form.getVariableLengthSampleDtos()[2].getName(), is("abcdefghijabcdefghijabcdefghijabcdefghij"));
        assertThat(form.getVariableLengthSampleDtos()[2].getDate(), is("2014-12-31"));
        assertThat(form.getVariableLengthSampleDtos()[2].getStatus(), is("05"));
        assertThat(form.getVariableLengthSampleDtos()[2].getStatus2(), is("04"));
        assertTrue(form.getVariableLengthSampleDtos()[2].getRegistered());
        assertThat(form.getVariableLengthSampleDtos()[2].getEstimate(), is(BigDecimal.valueOf(123.45)));
        assertThat(form.getVariableLengthSampleDtos()[2].getScore(), is(Integer.valueOf(89)));
    }

    /**
     * 全てのパラメータが異常値の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testAllInvalidValuesUsingDomain() {
        testAllInvalidValues(SampleForm.class);
    }

    /**
     * 全てのパラメータが異常値の場合、ドメイン定義にしたがいバリデーションが行われること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testAllInvalidValuesUnusedDomain() {
        testAllInvalidValues(UnusedDomainSampleForm.class);
    }

    private static void testAllInvalidValues(Class<? extends TestForm> formClass) {
        ValidationContext<? extends TestForm> context = validate(formClass,
                                                         "sample.freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.userId", "123456789", // M013
                                                         "sample.userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.date", "2014/12/31", // M002
                                                         "sample.status", "00", // M014
                                                         "sample.status2", "02", // M014
                                                         "sample.registered", "", // M006
                                                         "sample.estimate", "123456.45", // M004
                                                         "sample.score", "101", // M009

                                                         "sample.sampleDto.freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.sampleDto.userId", "123456789", // M013
                                                         "sample.sampleDto.userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.sampleDto.name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.sampleDto.date", "2014/12/31", // M002
                                                         "sample.sampleDto.status", "00", // M014
                                                         "sample.sampleDto.status2", "02", // M014
                                                         "sample.sampleDto.registered", "", // M006
                                                         "sample.sampleDto.estimate", "123456.45", // M004
                                                         "sample.sampleDto.score", "101", // M009

                                                         "sample.fixedLengthSampleDtos[0].freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.fixedLengthSampleDtos[0].userId", "123456789", // M013
                                                         "sample.fixedLengthSampleDtos[0].userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.fixedLengthSampleDtos[0].name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.fixedLengthSampleDtos[0].date", "2014/12/31", // M002
                                                         "sample.fixedLengthSampleDtos[0].status", "00", // M014
                                                         "sample.fixedLengthSampleDtos[0].status2", "02", // M014
                                                         "sample.fixedLengthSampleDtos[0].registered", "", // M006
                                                         "sample.fixedLengthSampleDtos[0].estimate", "123456.45", // M004
                                                         "sample.fixedLengthSampleDtos[0].score", "101", // M009

                                                         "sample.fixedLengthSampleDtos[1].freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.fixedLengthSampleDtos[1].userId", "123456789", // M013
                                                         "sample.fixedLengthSampleDtos[1].userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.fixedLengthSampleDtos[1].name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.fixedLengthSampleDtos[1].date", "2014/12/31", // M002
                                                         "sample.fixedLengthSampleDtos[1].status", "00", // M014
                                                         "sample.fixedLengthSampleDtos[1].status2", "02", // M014
                                                         "sample.fixedLengthSampleDtos[1].registered", "", // M006
                                                         "sample.fixedLengthSampleDtos[1].estimate", "123456.45", // M004
                                                         "sample.fixedLengthSampleDtos[1].score", "101", // M009

                                                         "sample.variableLengthSampleDtos[0].freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.variableLengthSampleDtos[0].userId", "123456789", // M013
                                                         "sample.variableLengthSampleDtos[0].userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.variableLengthSampleDtos[0].name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.variableLengthSampleDtos[0].date", "2014/12/31", // M002
                                                         "sample.variableLengthSampleDtos[0].status", "00", // M014
                                                         "sample.variableLengthSampleDtos[0].status2", "02", // M014
                                                         "sample.variableLengthSampleDtos[0].registered", "", // M006
                                                         "sample.variableLengthSampleDtos[0].estimate", "123456.45", // M004
                                                         "sample.variableLengthSampleDtos[0].score", "101", // M009

                                                         "sample.variableLengthSampleDtos[1].freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.variableLengthSampleDtos[1].userId", "123456789", // M013
                                                         "sample.variableLengthSampleDtos[1].userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.variableLengthSampleDtos[1].name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.variableLengthSampleDtos[1].date", "2014/12/31", // M002
                                                         "sample.variableLengthSampleDtos[1].status", "00", // M014
                                                         "sample.variableLengthSampleDtos[1].status2", "02", // M014
                                                         "sample.variableLengthSampleDtos[1].registered", "", // M006
                                                         "sample.variableLengthSampleDtos[1].estimate", "123456.45", // M004
                                                         "sample.variableLengthSampleDtos[1].score", "101", // M009

                                                         "sample.variableLengthSampleDtos[2].freeText", "フリーテキストのテスト", // always valid value
                                                         "sample.variableLengthSampleDtos[2].userId", "123456789", // M013
                                                         "sample.variableLengthSampleDtos[2].userIds", "a123456789,b1234567890,c123456789", // M013
                                                         "sample.variableLengthSampleDtos[2].name", "abcdefghijabcdefghijabcdefghijabcdefgh99", // M015
                                                         "sample.variableLengthSampleDtos[2].date", "2014/12/31", // M002
                                                         "sample.variableLengthSampleDtos[2].status", "00", // M014
                                                         "sample.variableLengthSampleDtos[2].status2", "02", // M014
                                                         "sample.variableLengthSampleDtos[2].registered", "", // M006
                                                         "sample.variableLengthSampleDtos[2].estimate", "123456.45", // M004
                                                         "sample.variableLengthSampleDtos[2].score", "101"); // M009
        assertFalse(context.isValid());
        assertMessages(context.getMessages(),"sample.userId", "M013",
                                             "sample.userIds", "M013",
                                             "sample.name", "M015",
                                             "sample.registered", "M006",
                                             "sample.estimate", "M004",
                                             "sample.score", "M009",

                                             "sample.sampleDto.userId", "M013",
                                             "sample.sampleDto.userIds", "M013",
                                             "sample.sampleDto.name", "M015",
                                             "sample.sampleDto.registered", "M006",
                                             "sample.sampleDto.estimate", "M004",
                                             "sample.sampleDto.score", "M009",

                                             "sample.fixedLengthSampleDtos[0].userId", "M013",
                                             "sample.fixedLengthSampleDtos[0].userIds", "M013",
                                             "sample.fixedLengthSampleDtos[0].name", "M015",
                                             "sample.fixedLengthSampleDtos[0].registered", "M006",
                                             "sample.fixedLengthSampleDtos[0].estimate", "M004",
                                             "sample.fixedLengthSampleDtos[0].score", "M009",

                                             "sample.fixedLengthSampleDtos[1].userId", "M013",
                                             "sample.fixedLengthSampleDtos[1].userIds", "M013",
                                             "sample.fixedLengthSampleDtos[1].name", "M015",
                                             "sample.fixedLengthSampleDtos[1].registered", "M006",
                                             "sample.fixedLengthSampleDtos[1].estimate", "M004",
                                             "sample.fixedLengthSampleDtos[1].score", "M009",

                                             "sample.variableLengthSampleDtos[0].userId", "M013",
                                             "sample.variableLengthSampleDtos[0].userIds", "M013",
                                             "sample.variableLengthSampleDtos[0].name", "M015",
                                             "sample.variableLengthSampleDtos[0].registered", "M006",
                                             "sample.variableLengthSampleDtos[0].estimate", "M004",
                                             "sample.variableLengthSampleDtos[0].score", "M009",

                                             "sample.variableLengthSampleDtos[1].userId", "M013",
                                             "sample.variableLengthSampleDtos[1].userIds", "M013",
                                             "sample.variableLengthSampleDtos[1].name", "M015",
                                             "sample.variableLengthSampleDtos[1].registered", "M006",
                                             "sample.variableLengthSampleDtos[1].estimate", "M004",
                                             "sample.variableLengthSampleDtos[1].score", "M009",

                                             "sample.variableLengthSampleDtos[2].userId", "M013",
                                             "sample.variableLengthSampleDtos[2].userIds", "M013",
                                             "sample.variableLengthSampleDtos[2].name", "M015",
                                             "sample.variableLengthSampleDtos[2].registered", "M006",
                                             "sample.variableLengthSampleDtos[2].estimate", "M004",
                                             "sample.variableLengthSampleDtos[2].score", "M009");
    }

    /**
     * {@link DomainValidator}を拡張することで、明示的なバリデーション呼び出しに対応できること。
     * ドメインアノテーションを使用した場合。
     */
    @Test
    public void testCustomDirectCallableDomainValidatorUsingDomain() {
        testCustomDirectCallableDomainValidator(SampleForm.class);
    }

    /**
     * {@link DomainValidator}を拡張することで、明示的なバリデーション呼び出しに対応できること。
     * ドメインアノテーションを使用しない場合。
     */
    @Test
    public void testCustomDirectCallableDomainValidatorUnusedDomain() {
        testCustomDirectCallableDomainValidator(UnusedDomainSampleForm.class);
    }

    private static void testCustomDirectCallableDomainValidator(Class<? extends TestForm> formClass) {

        DirectCallableDomainValidator domainValidator = repositoryResource.getComponent("domainValidator2");
        domainValidator.initialize();
        ValidationManager validationManager = repositoryResource.getComponent("validationManager");
        List<Validator> validators = new ArrayList<Validator>();
        validators.add((Validator)repositoryResource.getComponent("requiredValidator"));
        validators.add((Validator)repositoryResource.getComponent("numberRangeValidator"));
        validators.add((Validator)repositoryResource.getComponent("lengthValidator"));
        validators.add((Validator)repositoryResource.getComponent("systemCharValidator"));
        validators.add(domainValidator);
        validationManager.setValidators(validators);
        validationManager.initialize();
        repositoryResource.addComponent("validationManager.validators", validators);
        ValidationContext<? extends TestForm> context;

        // valid
        context = ValidationUtil.validateAndConvertRequest("sample", formClass, toParams("sample.freeText", "03"), "testDirectCallable");
        assertTrue(context.isValid());
        assertThat(context.createObject().getFreeText(), is("03"));

        // invalid
        context = ValidationUtil.validateAndConvertRequest("sample", formClass, toParams("sample.freeText", ""), "testDirectCallable");
        assertFalse(context.isValid());
        assertMessages(context.getMessages(),"sample.freeText", "M007");
    }

    private static ValidationContext<? extends TestForm> validate(Class<? extends TestForm> formClass, String... keyValues) {
        return  ValidationUtil.validateAndConvertRequest("sample", formClass, toParams(keyValues), "test");
    }

    private static Map<String, String[]> toParams(String... keyValues) {

        List<String> allKeys = new ArrayList<String>(
                                    Arrays.asList("sample.freeText",
                                                  "sample.userId",
                                                  "sample.userIds",
                                                  "sample.name",
                                                  "sample.date",
                                                  "sample.status",
                                                  "sample.status2",
                                                  "sample.registered",
                                                  "sample.estimate",
                                                  "sample.score",

                                                  "sample.sampleDto.freeText",
                                                  "sample.sampleDto.userId",
                                                  "sample.sampleDto.userIds",
                                                  "sample.sampleDto.name",
                                                  "sample.sampleDto.date",
                                                  "sample.sampleDto.status",
                                                  "sample.sampleDto.status2",
                                                  "sample.sampleDto.registered",
                                                  "sample.sampleDto.estimate",
                                                  "sample.sampleDto.score",

                                                  "sample.fixedLengthSampleDtos[0].freeText",
                                                  "sample.fixedLengthSampleDtos[0].userId",
                                                  "sample.fixedLengthSampleDtos[0].userIds",
                                                  "sample.fixedLengthSampleDtos[0].name",
                                                  "sample.fixedLengthSampleDtos[0].date",
                                                  "sample.fixedLengthSampleDtos[0].status",
                                                  "sample.fixedLengthSampleDtos[0].status2",
                                                  "sample.fixedLengthSampleDtos[0].registered",
                                                  "sample.fixedLengthSampleDtos[0].estimate",
                                                  "sample.fixedLengthSampleDtos[0].score",

                                                  "sample.fixedLengthSampleDtos[1].freeText",
                                                  "sample.fixedLengthSampleDtos[1].userId",
                                                  "sample.fixedLengthSampleDtos[1].userIds",
                                                  "sample.fixedLengthSampleDtos[1].name",
                                                  "sample.fixedLengthSampleDtos[1].date",
                                                  "sample.fixedLengthSampleDtos[1].status",
                                                  "sample.fixedLengthSampleDtos[1].status2",
                                                  "sample.fixedLengthSampleDtos[1].registered",
                                                  "sample.fixedLengthSampleDtos[1].estimate",
                                                  "sample.fixedLengthSampleDtos[1].score",

                                                  "sample.variableLengthSampleDtos[0].freeText",
                                                  "sample.variableLengthSampleDtos[0].userId",
                                                  "sample.variableLengthSampleDtos[0].userIds",
                                                  "sample.variableLengthSampleDtos[0].name",
                                                  "sample.variableLengthSampleDtos[0].date",
                                                  "sample.variableLengthSampleDtos[0].status",
                                                  "sample.variableLengthSampleDtos[0].status2",
                                                  "sample.variableLengthSampleDtos[0].registered",
                                                  "sample.variableLengthSampleDtos[0].estimate",
                                                  "sample.variableLengthSampleDtos[0].score",

                                                  "sample.variableLengthSampleDtos[1].freeText",
                                                  "sample.variableLengthSampleDtos[1].userId",
                                                  "sample.variableLengthSampleDtos[1].userIds",
                                                  "sample.variableLengthSampleDtos[1].name",
                                                  "sample.variableLengthSampleDtos[1].date",
                                                  "sample.variableLengthSampleDtos[1].status",
                                                  "sample.variableLengthSampleDtos[1].status2",
                                                  "sample.variableLengthSampleDtos[1].registered",
                                                  "sample.variableLengthSampleDtos[1].estimate",
                                                  "sample.variableLengthSampleDtos[1].score",

                                                  "sample.variableLengthSampleDtos[2].freeText",
                                                  "sample.variableLengthSampleDtos[2].userId",
                                                  "sample.variableLengthSampleDtos[2].userIds",
                                                  "sample.variableLengthSampleDtos[2].name",
                                                  "sample.variableLengthSampleDtos[2].date",
                                                  "sample.variableLengthSampleDtos[2].status",
                                                  "sample.variableLengthSampleDtos[2].status2",
                                                  "sample.variableLengthSampleDtos[2].registered",
                                                  "sample.variableLengthSampleDtos[2].estimate",
                                                  "sample.variableLengthSampleDtos[2].score"));

        // set specified parameters
        Map<String, String[]> params = new HashMap<String, String[]>();
        for (int i = 0; i < keyValues.length; i += 2) {
            params.put(keyValues[i], keyValues[i + 1].split(","));
            allKeys.remove(keyValues[i]);
        }

        // set blank for unspecified parameters
        for (String key : allKeys) {
            params.put(key, new String[] {""});
        }

        // set sizeKey. always 3
        params.put("sample.sampleDtosSize", new String[] {"3"});

        return params;
    }

    private static void assertMessages(List<Message> messages, String... keyMessageIds) {
        assertThat("message size", messages.size(), is(keyMessageIds.length / 2));
        for (int i = 0; i < keyMessageIds.length; i += 2) {
            ValidationResultMessage message = find(messages, keyMessageIds[i]);
            if (message == null) {
                fail("ValidationResultMessage was not found. propertyName = [" + keyMessageIds[i] + "]");
            }
            assertThat("propertyName = [" + keyMessageIds[i] + "]", message.getMessageId(), is(keyMessageIds[i + 1]));
        }
    }

    private static ValidationResultMessage find(List<Message> messages, String propertyName) {
        for (Message message : messages) {
            ValidationResultMessage validationResultMessage = (ValidationResultMessage) message;
            if (validationResultMessage.getPropertyName().equals(propertyName)) {
                return validationResultMessage;
            }
        }
        return null;
    }
}
