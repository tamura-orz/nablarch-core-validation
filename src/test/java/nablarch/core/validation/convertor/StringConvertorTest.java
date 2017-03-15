package nablarch.core.validation.convertor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ConversionFormat;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.creator.ReflectionFormCreator;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * {@link StringConvertor}のテストクラス。
 */
@SuppressWarnings("UnusedLabel")
public class StringConvertorTest {

    private StringConvertor testee;

    private static MockStringResourceHolder resource;

    @ClassRule
    public static SystemRepositoryResource repo = new SystemRepositoryResource("nablarch/core/validation/convertor-test-base.xml");

    private static final String[][] MESSAGES = {
            {"MSG00001", "ja", "{0}が正しくありません。", "en", "value of {0} is not valid."},
            {"PROP0001", "ja", "プロパティ1", "en", "property1"},
            {"invalid.zero", "ja", "ゼロじゃありません1", "en", "invalid!!!"},
            {"MSG00002", "ja", "{0}がフォーマット通りではありません。", "en", "value if input is not well-formatted."}
    };

    @BeforeClass
    public static void setUpClass() {
        resource = repo.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
    }

    @Before
    public void setUp() {
        testee = new StringConvertor();
        testee.setConversionFailedMessageId("MSG00001");
        // デフォルト設定としてnullを許可しない
        testee.setAllowNullValue(false);
    }


    /**
     * {@link StringConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテストケース。
     */
    @Test
    public void testIsConvertible() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        正常に変換されるケース: {
            System.out.println("params = " + params);
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"10"}, null));

        }

        変換されないケース: {

            //******************************************************************
            // サイズ0の配列はNG
            //******************************************************************
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{}, null));

            assertEquals(1, context.getMessages().size());
            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("value of PROP0001 is not valid.",
                    context.getMessages().get(0).formatMessage());

            //******************************************************************
            // サイズ2の配列はNG
            //******************************************************************
            context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"", ""}, null));

            assertEquals(1, context.getMessages().size());
            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("value of PROP0001 is not valid.",
                    context.getMessages().get(0).formatMessage());
        }

        nullを指定した場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertFalse(testee.isConvertible(context, "param", "PROP0001", null,
                    null));

            assertEquals(1, context.getMessages().size());
            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("value of PROP0001 is not valid.",
                    context.getMessages().get(0).formatMessage());
        }

        文字列を指定した場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertTrue("文字列は変換できるのでtrueが返却される。", testee.isConvertible(context, "param", "PROP0001", "文字列", null));
            assertFalse("nullは変換できないのでfalse", testee.isConvertible(context, "param", "文字列項目", null, null));

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals(1, context.getMessages().size());
            assertEquals("文字列項目が正しくありません。", context.getMessages().get(0)
                    .formatMessage());
        }

        数値を指定した場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertFalse("intは変換できないので、falseが返却される。", testee.isConvertible(context, "param", "文字列項目", 100, null));
            assertFalse("intは変換できないので、falseが返却される。", testee.isConvertible(context, "param", "文字列項目", null, null));

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals(2, context.getMessages().size());
            assertEquals("文字列項目が正しくありません。", context.getMessages().get(0).formatMessage());
        }

        ネストするコンバータを使用する場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            List<ExtendedStringConvertor> extendedStringConvertorList = new ArrayList<ExtendedStringConvertor>();
            final ExtendedStringConvertor convertor = new ZeroConvertor();
            extendedStringConvertorList.add(convertor);
            testee.setExtendedStringConvertors(extendedStringConvertorList);
            
            Zero dateStringAnnotation = new Zero() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Zero.class;
                }
            };
            assertThat(testee.isConvertible(context, "param", "zero", "ゼロ", dateStringAnnotation), is(true));
            assertThat(testee.isConvertible(context, "param", "zero", new String[] {"ゼロ"}, dateStringAnnotation), is(true));
            assertThat(testee.isConvertible(context, "param", "zero", new String[] {"0"}, dateStringAnnotation), is(false));
            assertThat(context.getMessages().get(0).getMessageId(), is("invalid.zero"));
        }
    }

    /**
     * {@link StringConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテストケース。
     * nullを許可する設定とした場合。
     */
    @Test
    public void testIsConvertibleAllowNullValue() {

        // デフォルト設定のnullを許可する設定でテストを実施
        testee = new StringConvertor();
        testee.setConversionFailedMessageId("MSG00001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        正常に変換されるケース: {
            System.out.println("params = " + params);
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"10"}, null));

        }

        変換されないケース: {

            //******************************************************************
            // サイズ0の配列はNG
            //******************************************************************
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{}, null));

            assertEquals(1, context.getMessages().size());
            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("value of PROP0001 is not valid.",
                    context.getMessages().get(0).formatMessage());

            //******************************************************************
            // サイズ2の配列はNG
            //******************************************************************
            context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"", ""}, null));

            assertEquals(1, context.getMessages().size());
            ThreadContext.setLanguage(Locale.ENGLISH);
            assertEquals("value of PROP0001 is not valid.",
                    context.getMessages().get(0).formatMessage());
        }

        nullを指定した場合: {

            // nullを許可しているのでOK

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertTrue(testee.isConvertible(context, "param", "PROP0001", null,
                    null));

        }

        文字列を指定した場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertTrue("文字列は変換できるのでtrueが返却される。", testee.isConvertible(context, "param", "PROP0001", "文字列", null));
        }

        数値を指定した場合: {

            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
            assertFalse("intは変換できないので、falseが返却される。", testee.isConvertible(context, "param", "文字列項目", 100, null));

            ThreadContext.setLanguage(Locale.JAPANESE);
            assertEquals(1, context.getMessages().size());
            assertEquals("文字列項目が正しくありません。", context.getMessages().get(0).formatMessage());
        }

    }


    /**
     * {@link StringConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     */
    @Test
    public void testConvert() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class,
                new ReflectionFormCreator(),
                params, "");

        assertEquals("String配列はOK", "日本語", testee.convert(context, "param", new String[]{"日本語"}, null));
        assertEquals("StringはOK", "文字列", testee.convert(context, "param", "文字列", null));

        // ネストするコンバータを使用する場合。
        List<ExtendedStringConvertor> extendedStringConvertorList = new ArrayList<ExtendedStringConvertor>();
        extendedStringConvertorList.add(new ZeroConvertor());
        testee.setExtendedStringConvertors(extendedStringConvertorList);
        final Zero zero = new Zero() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Zero.class;
            }
        };

        assertEquals("0", testee.convert(context, "param", "ゼロ", zero));
        assertEquals("0", testee.convert(context, "param", new String[]{"ゼロ"}, zero));

        try {
            Digits digits = new Digits() {
                @Override
                public int integer() {

                    return 0;
                }

                @Override
                public int fraction() {

                    return 0;
                }

                @Override
                public boolean commaSeparated() {

                    return false;
                }

                @Override
                public String messageId() {

                    return null;
                }

                @Override
                public Class<? extends Annotation> annotationType() {

                    return null;
                }
            };
            testee.convert(context, "param", "", digits);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("convertor related to null isn't specified.", e.getMessage());
        }
    }

    /**
     * {@link StringConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * nullを許可する設定とした場合。
     */
    @Test
    public void testConvertAllowNullValue() {

        // デフォルト設定のnullを許可する設定でテストを実施
        testee = new StringConvertor();
        testee.setConversionFailedMessageId("MSG00001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class,
                new ReflectionFormCreator(),
                params, "");

        assertEquals("String配列はOK", "日本語", testee.convert(context, "param", new String[]{"日本語"},
                null));
        assertEquals("StringはOK", "文字列", testee.convert(context, "param", "文字列", null));
        assertNull("nullはOK", testee.convert(context, "param", null, null));
    }
    @Test
    public void testTargetClass() {

        assertEquals(String.class, testee.getTargetClass());
    }


    /**
     * {@link StringConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
     * <p/>
     * 変換可否判定の際にトリム機能が正しく動作することの確認。
     */
    @Test
    public void testIsConvertibleWithTrim() {

        testee = new StringConvertor();
        
        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class,
                new ReflectionFormCreator(),
                params, "");

        // ネストするコンバータを使用してトリム動作を確認
        List<ExtendedStringConvertor> extendedStringConvertorList = new ArrayList<ExtendedStringConvertor>();
        extendedStringConvertorList.add(new ZeroConvertor());
        testee.setExtendedStringConvertors(extendedStringConvertorList);
        final Zero zero = new Zero() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Zero.class;
            }
        };

        // デフォルト設定でu0020以下の文字（文字列前後の半角スペース、改行コード、タブ、null文字）がトリムされないことの確認
        assertFalse(testee.isConvertible(context, "param", "表示", new String[]{"   \r\n\t\0 ゼロ    \t\r\n\0"}, zero));
        assertFalse(testee.isConvertible(context, "param", "表示", "   \r\n\t\0 ゼロ    \t\r\n\0", zero));

        // trimAllを設定した場合に、u0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされることの確認
        testee.setTrimPolicy("trimAll");
        assertTrue(testee.isConvertible(context, "param", "表示", new String[]{"   \r\n\t\0 ゼロ    \t\r\n\0"}, zero));
        assertTrue(testee.isConvertible(context, "param", "表示", "   \r\n\t\0 ゼロ    \t\r\n\0", zero));

        // noTrimを設定した場合にu0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされないことの確認
        testee.setTrimPolicy("noTrim");
        assertFalse(testee.isConvertible(context, "param", "表示", new String[]{"   \r\n\t\0 ゼロ    \t\r\n\0"}, zero));
        assertFalse(testee.isConvertible(context, "param", "表示", "   \r\n\t\0 ゼロ    \t\r\n\0", zero));

        // trimAllを設定した場合でも、全角スペースはトリムされないことの確認
        testee.setTrimPolicy("trimAll");
        assertFalse(testee.isConvertible(context, "param", "表示", new String[]{"　　　ゼロ　　　"}, zero));
        assertFalse(testee.isConvertible(context, "param", "表示", "　　　ゼロ　　　", zero));
    }
    
    
    /**
     * {@link StringConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * <p/>
     * 変換の際にトリム機能が正しく動作することの確認。
     */
    @Test
    public void testConvertWithTrim() {

        testee = new StringConvertor();
        
        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class,
                new ReflectionFormCreator(),
                params, "");
        
        // デフォルト設定でu0020以下の文字（文字列前後の半角スペース、改行コード、タブ、null文字）がトリムされないことの確認
        assertEquals("String配列OK", "   \r\n\t\0日本語    \t\r\n\0", testee.convert(context, "param", new String[]{"   \r\n\t\0日本語    \t\r\n\0"}, null));
        assertEquals("StringはOK", "   \r\n\t\0文字列    \t\r\n\0", testee.convert(context, "param", "   \r\n\t\0文字列    \t\r\n\0", null));

        // trimAllを設定した場合に、u0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされることの確認
        testee.setTrimPolicy("trimAll");
        assertEquals("String配列OK", "日本語", testee.convert(context, "param", new String[]{"   \r\n\t\0日本語    \t\r\n\0"}, null));
        assertEquals("StringOK", "文字列", testee.convert(context, "param", "   \r\n\t\0文字列    \t\r\n\0", null));

        // noTrimを設定した場合にu0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされないことの確認
        testee.setTrimPolicy("noTrim");
        assertEquals("String配列OK", "   \r\n\t\0日本語    \t\r\n\0", testee.convert(context, "param", new String[]{"   \r\n\t\0日本語    \t\r\n\0"}, null));
        assertEquals("StringOK", "   \r\n\t\0文字列    \t\r\n\0", testee.convert(context, "param", "   \r\n\t\0文字列    \t\r\n\0", null));

        // trimAllを設定した場合でも、全角スペースはトリムされないことの確認
        testee.setTrimPolicy("trimAll");
        assertEquals("String配列OK", "　　　日本語　　　", testee.convert(context, "param", new String[]{"　　　日本語　　　"}, null));
        assertEquals("StringOK", "　　　文字列　　　", testee.convert(context, "param", "　　　文字列　　　", null));
    }

    /**
     * {@link StringConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * <p/>
     * 不正なトリムポリシー名を設定した場合、例外がスローされるテスト。
     */
    @Test
    public void testInvalidTrimPolicyName() {
        
        // null
        try {
            testee.setTrimPolicy(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid property value was specified. 'trimPolicy' property must not be empty. supported trim policy name=[\"trimAll\", \"noTrim\"]."));
        }

        // 空文字
        try {
            testee.setTrimPolicy("");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid property value was specified. 'trimPolicy' property must not be empty. supported trim policy name=[\"trimAll\", \"noTrim\"]."));
        }
        
        // 不正な値
        try {
            testee.setTrimPolicy("abc");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("invalid property value was specified. 'abc' was not supported trim policy name. supported trim policy name=[\"trimAll\", \"noTrim\"]."));
        }
    }

    /**
     * {@link StringConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * <p/>
     * 設定ファイルからトリムポリシー名が設定できることのテスト。
     */
    @Test
    public void testConfiguration() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class,
                new ReflectionFormCreator(),
                params, "");
        
        ////////////////////////////////
        //   noTrimでリポジトリ構築   //
        ////////////////////////////////
        File file1 = new File("src/test/resources/nablarch/core/validation/convertor/StringConvertorTest1.xml");
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(file1.toURI().toString());
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);
        StringConvertor configurationConvertor = SystemRepository.get("stringConvertor");

        // noTrimを設定した場合にu0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされないことの確認
        testee.setTrimPolicy("noTrim");
        assertEquals("String配列OK", "   \r\n\t\0日本語    \t\r\n\0", configurationConvertor.convert(context, "param", new String[]{"   \r\n\t\0日本語    \t\r\n\0"}, null));
        assertEquals("StringOK", "   \r\n\t\0文字列    \t\r\n\0", configurationConvertor.convert(context, "param", "   \r\n\t\0文字列    \t\r\n\0", null));

        
        ////////////////////////////////
        //   trimAllでリポジトリ構築   //
        ////////////////////////////////
        File file2 = new File("src/test/resources/nablarch/core/validation/convertor/StringConvertorTest2.xml");
        loader = new XmlComponentDefinitionLoader(file2.toURI().toString());
        container = new DiContainer(loader);
        SystemRepository.load(container);
        configurationConvertor = SystemRepository.get("stringConvertor");

        // trimAllを設定した場合に、u0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされることの確認
        testee.setTrimPolicy("trimAll");
        assertEquals("String配列OK", "日本語", configurationConvertor.convert(context, "param", new String[]{"   \r\n\t\0日本語    \t\r\n\0"}, null));
        assertEquals("StringOK", "文字列", configurationConvertor.convert(context, "param", "   \r\n\t\0文字列    \t\r\n\0", null));
    }

    private static class ZeroConvertor implements ExtendedStringConvertor {

        @Override
        public Class<? extends Annotation> getTargetAnnotation() {
            return Zero.class;
        }

        @Override
        public Class<?> getTargetClass() {
            return String.class;
        }

        @Override
        public <T> boolean isConvertible(final ValidationContext<T> context, final String propertyName,
                final Object propertyDisplayName,
                final Object value, final Annotation format) {

            final boolean result = value != null && value.toString()
                                                     .equals("ゼロ");
            if (!result) {
                context.addResultMessage(propertyName, "invalid.zero");
            }
            return result;
        }

        @Override
        public <T> Object convert(final ValidationContext<T> context, final String propertyName,
                final Object value, final Annotation format) {
            return "0";
        }
    }
    
    @ConversionFormat
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Zero {

    }
}
