package nablarch.core.validation.convertor;

import nablarch.core.ThreadContext;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.creator.ReflectionFormCreator;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LongConvertorTest {

    private static LongConvertor testee;
    private static MockStringResourceHolder resource;

    @ClassRule
    public static SystemRepositoryResource repo = new SystemRepositoryResource("nablarch/core/validation/convertor-test-base.xml");

    private static final String[][] MESSAGES = {
            {"MSG00001", "ja", "{0}は整数で入力してください。", "en",
                    "please input {0} as integer."},
            {"MSG00002", "ja", "{0}は正の整数で入力してください。", "en",
                    "please input {0} as positive integer."},
            {"PROP0001", "ja", "プロパティ1", "en", "property1"},};


    @BeforeClass
    public static void setUpClass() {
        resource = repo.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
    }

    @Before
    public void setUp() {
        testee = new LongConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");
        // デフォルトとしてnullは許可しない。
        testee.setAllowNullValue(false);
    }

    private Digits digits = new Digits() {

        public Class<? extends Annotation> annotationType() {
            return Digits.class;
        }

        public String messageId() {
            return "";
        }

        public int integer() {
            return 18;
        }

        public int fraction() {
            return 0;
        }

        public boolean commaSeparated() {
            return true;
        }
    };

    @Test
    public void testIsConvertible() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列
        //**********************************************************************
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{","}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"１２３４"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"+10"}, digits));

        assertEquals(6, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // String
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "1", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10.", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                ".", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                ",", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "４３２１", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "+10", digits));

        assertEquals(6, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // 数値
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                1, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                -1234567890L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                12345678901L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                123456789012345678L, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                1234567890123456789L, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // null
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                null, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("property1",
                context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testIsConvertibleForI18N() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"decimal{###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列
        //**********************************************************************
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10.000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10,"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-"}, digits));

        assertEquals(2, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // String
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "1", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10,", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));

        assertEquals(2, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // 数値
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                1, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                -1234567890L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                12345678901L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                123456789012345678L, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                1234567890123456789L, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // null
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                null, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("property1",
                context.getMessages().get(0).formatMessage());
    }

    /**
     * {@link LongConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}。
     *
     * nullを許可する場合のテスト
     */
    @Test
    public void testIsConvertibleAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new LongConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列
        //**********************************************************************
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-"}, digits));

        assertEquals(2, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // String
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "1", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10.", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));

        assertEquals(2, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // 数値
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                1, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                -1234567890L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                12345678901L, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                123456789012345678L, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                1234567890123456789L, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // null
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                null, digits));
    }

    @Test
    public void testIsConvertiblePlusChar() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"+10"}, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testIsConvertibleWithFraction() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        Digits digits = new Digits() {

            public Class<? extends Annotation> annotationType() {
                return Digits.class;
            }

            public String messageId() {
                return "";
            }

            public int integer() {
                return 5;
            }

            public int fraction() {
                // 小数点以下は指定不可。
                return 1;
            }

            public boolean commaSeparated() {
                return true;
            }
        };


        try {
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"-10."}, digits));
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {

        }

        //***********************************************************************
        // 整数部の桁数に10桁以上を指定した場合
        //***********************************************************************
        try {
            assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10."}, new Digits() {
                public int integer() {
                    return 19;
                }

                public int fraction() {
                    // 小数点以下は指定不可。
                    return 0;
                }

                public boolean commaSeparated() {
                    return true;
                }

                public String messageId() {
                    return "";
                }

                public Class<? extends Annotation> annotationType() {
                    return Digits.class;
                }
            }));
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("length was invalid. integer length must be less than or equal to 18 digit. specified value:19"));
        }
    }

    @Test
    public void testConvert() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertEquals(10L, testee.convert(context, "param", new String[]{"10"},
                digits));
        assertEquals(10000L, testee.convert(context, "param", new String[]{"10,000"}, digits));
        assertNull(testee.convert(context, "param", new String[]{""}, digits));

        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1234567890L, testee.convert(context, "param", 1234567890L, digits));
        assertEquals(123456789012345678L, testee.convert(context, "param", 123456789012345678L, digits));
        assertEquals(-123456789012345678L, testee.convert(context, "param", -123456789012345678L, digits));
        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1L, testee.convert(context, "param", new BigDecimal("1"), digits));
        assertEquals(-12345L, testee.convert(context, "param", new BigDecimal("-12345"), digits));
    }

    @Test
    public void testConvertForI18N() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"decimal{###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertEquals(10L, testee.convert(context, "param", new String[]{"10"},
                digits));
        assertEquals(10000L, testee.convert(context, "param", new String[]{"10.000"}, digits));
        assertNull(testee.convert(context, "param", new String[]{""}, digits));

        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1234567890L, testee.convert(context, "param", 1234567890L, digits));
        assertEquals(123456789012345678L, testee.convert(context, "param", 123456789012345678L, digits));
        assertEquals(-123456789012345678L, testee.convert(context, "param", -123456789012345678L, digits));
        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1L, testee.convert(context, "param", new BigDecimal("1"), digits));
        assertEquals(-12345L, testee.convert(context, "param", new BigDecimal("-12345"), digits));
    }

    /**
     * {@link LongConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     *
     * nullを許可する場合
     */
    @Test
    public void testConvertAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new LongConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertEquals(10L, testee.convert(context, "param", new String[]{"10"},
                digits));
        assertEquals(10000L, testee.convert(context, "param", new String[]{"10,000"}, digits));
        assertNull(testee.convert(context, "param", new String[]{""}, digits));

        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1234567890L, testee.convert(context, "param", 1234567890L, digits));
        assertEquals(123456789012345678L, testee.convert(context, "param", 123456789012345678L, digits));
        assertEquals(-123456789012345678L, testee.convert(context, "param", -123456789012345678L, digits));
        assertEquals(1L, testee.convert(context, "param", 1L, digits));
        assertEquals(1L, testee.convert(context, "param", new BigDecimal("1"), digits));
        assertEquals(-12345L, testee.convert(context, "param", new BigDecimal("-12345"), digits));
        assertNull(testee.convert(context, "param", null, digits));
    }


    /**
     * {@link LongConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
     * <p/>
     * 変換可否判定の際にトリム機能が正しく動作することの確認。
     */
    @Test
    public void testIsConvertibleWithTrim() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        // Stringを指定した場合に、u0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされisConvertibleの結果がtrueになることの確認
        assertTrue(testee.isConvertible(context, "param", "表示", "   \0\r\n\t10   \r\n\t\0", digits));

        // Stringを指定した場合に、全角スペースがトリムされず、isConvertibleの結果がfalseになることの確認
        assertFalse(testee.isConvertible(context, "param", "表示", "　　　10　　　", digits));
    }
    
    /**
     * {@link LongConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * <p/>
     * 変換の際にトリム機能が正しく動作することの確認。
     */
    @Test
    public void testConvertWithTrim() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        // Stringを指定した場合に、u0020以下の文字（半角スペース、改行コード、タブ、null文字など）がトリムされることの確認
        assertEquals(new Long("10"), testee.convert(context, "param", "   \0\r\n\t10   \r\n\t\0", null));

        // Stringを指定した場合に、全角スペースがトリムされないためBigDecimalへの変換に失敗し、nullが返却されることの確認
        assertNull(testee.convert(context, "param", "　　　10　　　", null));
    }
    
}
