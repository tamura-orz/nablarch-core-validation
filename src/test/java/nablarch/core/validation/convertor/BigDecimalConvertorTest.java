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

public class BigDecimalConvertorTest {

    private BigDecimalConvertor testee;
    private static MockStringResourceHolder resource;

    @ClassRule
    public static SystemRepositoryResource repo = new SystemRepositoryResource("nablarch/core/validation/convertor-test-base.xml");

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}の入力が不正です。", "en", "input value of {0} was invalid." },
        { "MSG00002", "ja", "{0}には整数部{1}桁、小数部{2}桁の数値を入力してください。", "en", "please input {0} <{1} digits>.<{2} digits>." },
        { "MSG00003", "ja", "{0}には{1}桁の数値を入力してください。", "en", "please input {0} <{1} digits>." },
        { "MSG00004", "ja", "{0}は正数値で入力してください。", "en", "please input {0} as integer." },
        { "MSG00005", "ja", "テストメッセージ", "en", "test message." },
        { "PROP0001", "ja", "プロパティ1", "en", "property1" }, };


    @BeforeClass
    public static void setUpClass() {
        resource = repo.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
    }

    @Before
    public void setUp() {
        testee = new BigDecimalConvertor();
        testee.setMultiInputMessageId("MSG00001");
        testee.setInvalidDigitsFractionMessageId("MSG00002");
        testee.setInvalidDigitsIntegerMessageId("MSG00003");

        // デフォルト動作としてnullは許可しない
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
            return 5;
        }

        public int fraction() {
            return 2;
        }

        public boolean commaSeparated() {
            return false;
        }
    };

    @Test
    public void testIsConvertible() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{".01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"-.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10.0"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"+10.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10.01"}, digits));

        // 配列以外の場合
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10.01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", ".01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-.01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10.0", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "+10.01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-10.01", digits));

        // Integerを指定した場合。
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new Integer("10"), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new Integer("-10"), digits));

        // nullを指定した場合
        assertFalse(testee.isConvertible(context, "param", "PROP0001", null,
                digits));
    }

    @Test
    public void testIsConvertibleForI18N() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"decimal{###.###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10,01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{",01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"-,01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10,0"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"+10,01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10,01"}, digits));

        // 配列以外の場合
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10,01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", ",01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-,01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10,0", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "+10,01", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-10,01", digits));

        // Integerを指定した場合。
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new Integer("10"), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new Integer("-10"), digits));

        // nullを指定した場合
        assertFalse(testee.isConvertible(context, "param", "PROP0001", null, digits));
        
        // 記号のみの場合
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{","}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"+"}, digits));
    }


    /**
     * 文字列に変換した時、0.0000000が"0E-7"といった指数形式に変換されると、
     * ^[\+-]?[0-9]{0,2}(\.[0-9]{1,7})?$
     * のパターンにマッチせず、NumberConvertorSupport#isPatternMatched(Digits, DecimalFormatSymbols, Object)
     * で必ずfalseになる。
     */
    @Test
    public void testConvertToStringBigDecimal() {
        assertThat("0E-7とかだとパターンにマッチしない！",
                   testee.convertToString(new BigDecimal("0.0000000")),
                   is("0.0000000"));
    }


    @Test
    public void testIsConvertibleBigDecimal() {

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("param", new BigDecimal("0.0000000"));

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new BigDecimal("0.0000000"), new Digits() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public int integer() {
                return 2;
            }

            @Override
            public int fraction() {
                return 7;
            }

            @Override
            public boolean commaSeparated() {
                return false;
            }

            @Override
            public String messageId() {
                return null;
            }
        }));
    }

    /**
     * {@link BigDecimalConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
     *
     * nullを許可する場合のケース
     */
    @Test
    public void testIsConvertibleAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new BigDecimalConvertor();
        testee.setMultiInputMessageId("MSG00001");
        testee.setInvalidDigitsFractionMessageId("MSG00002");
        testee.setInvalidDigitsIntegerMessageId("MSG00003");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{".01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10.0"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"+10.01"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10.01"}, digits));

        // 配列以外の場合
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10.01",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", ".01",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-.01",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "10.0",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "+10.01",
                digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", "-10.01",
                digits));

        // Integerを指定した場合。
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer("10"), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer("-10"), digits));

        // nullを指定した場合
        assertTrue(testee.isConvertible(context, "param", "PROP0001", null,
                digits));
    }

    @Test
    public void testIsConvertibleFail() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10.001"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"a"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"..1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{".-1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{","}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"+"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"１"}, digits));

        assertEquals(9, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 <5 digits>.<2 digits>.", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testIsConvertibleFailCommaSeparated() {

        digits = new Digits() {
            
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
                return 2;
            }

            public boolean commaSeparated() {
                return true;
            }
        };
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10.001"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"a"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"..1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{".-1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{","}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-,"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"+,"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{",,"}, digits));

        assertEquals(10, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 <5 digits>.<2 digits>.", context.getMessages().get(0).formatMessage());
    }



    @Test
    public void testIsConvertibleMultiInput() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10", "11"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"10", "11"}, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("input value of PROP0001 was invalid.", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testIsConvertibleWithDigits5And2() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        Digits digits = new Digits() {

            public boolean commaSeparated() {
                return true;
            }

            public int fraction() {
                return 2;
            }

            public int integer() {
                return 5;
            }

            public String messageId() {
                return "MSG00005";
            }

            public Class<? extends Annotation> annotationType() {
                return Digits.class;
            }

        };

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10,000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10000.00"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10,000.01"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"10.001"}, digits));
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("test message.", context.getMessages().get(0).formatMessage());

    }

    @Test
    public void testIsConvertibleWithDigits7And0() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        Digits digits = new Digits() {

            public boolean commaSeparated() {
                return true;
            }

            public int integer() {
                return 7;
            }

            public int fraction() {
                return 0;
            }

            public String messageId() {
                return "MSG00005";
            }

            public Class<? extends Annotation> annotationType() {
                return Digits.class;
            }

        };

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"10,000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"100,000"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"100,0000"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"100,000."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"10.001"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{",,"}, digits));
        assertEquals(4, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);

        assertEquals("test message.", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testInputIsEmpty() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{""});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{""}, digits));
    }

    @Test
    public void testInputIsNull() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{""});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001", null, digits));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001の入力が不正です。", context.getMessages().get(0).formatMessage());

    }


    @Test
    public void testInputIsMulti() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{""});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[] {"01", "02"}, digits));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001の入力が不正です。", context.getMessages().get(0).formatMessage());

    }

    @Test
    public void testInputAnnotationIsNotAllowed() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        Annotation annotation = new Annotation() {

            public Class<? extends Annotation> annotationType() {
                return Annotation.class;
            }
        };

        try {
            assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{ "11" }, annotation));
            fail("例外が発生するはず");
        } catch(IllegalArgumentException e) {

        }
    }
    @Test
    public void testConvert() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", new String[]{"10"}, null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", new String[]{"10,000"}, null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", new String[]{".01"}, null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", new String[]{"0.01"}, null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", new String[]{"+10"}, null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", new String[]{"+10,000"}, null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", new String[]{"+.01"}, null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", new String[]{"+0.01"}, null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", new String[]{"-10"}, null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", new String[]{"-10,000"}, null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", new String[]{"-.01"}, null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", new String[]{"-0.01"}, null));
        assertNull(testee.convert(context, "param", new String[]{"-0.01", ""}, null));

        //**********************************************************************
        // Stringを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", "10", null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", "10,000", null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", ".01", null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", "0.01", null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", "+10", null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", "+10,000", null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", "+.01", null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", "+0.01", null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", "-10", null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", "-10,000", null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", "-.01", null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", "-0.01", null));

        //**********************************************************************
        // Integerを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"),
                testee.convert(context, "param", new Integer("10"), null));
        assertEquals(new BigDecimal("10000"),
                testee.convert(context, "param", new Integer("10000"), null));
        assertEquals(new BigDecimal("-10"),
                testee.convert(context, "param", new Integer("-10"), null));
        assertEquals(new BigDecimal("-10000"),
                testee.convert(context, "param", new Integer("-10000"), null));

        //**********************************************************************
        // Longを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"),
                testee.convert(context, "param", new Long("10"), null));
        assertEquals(new BigDecimal("10000"),
                testee.convert(context, "param", new Long("10000"), null));
        assertEquals(new BigDecimal("-10"),
                testee.convert(context, "param", new Long("-10"), null));
        assertEquals(new BigDecimal("-10000"),
                testee.convert(context, "param", new Long("-10000"), null));

        //**********************************************************************
        // BigDecimalを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", new BigDecimal("10"), null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", new BigDecimal("10000"), null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", new BigDecimal(".01"), null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", new BigDecimal("0.01"), null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", new BigDecimal("+10"), null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", new BigDecimal("+10000"), null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", new BigDecimal("+.01"), null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", new BigDecimal("+0.01"), null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", new BigDecimal("-10"), null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", new BigDecimal("-10000"), null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", new BigDecimal("-.01"), null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", new BigDecimal("-0.01"), null));
    }

    @Test
    public void testConvertForI18N() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"decimal{###.###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", new String[]{"10"}, null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", new String[]{"10.000"}, null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", new String[]{",01"}, null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", new String[]{"0,01"}, null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", new String[]{"+10"}, null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", new String[]{"+10.000"}, null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", new String[]{"+,01"}, null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", new String[]{"+0,01"}, null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", new String[]{"-10"}, null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", new String[]{"-10.000"}, null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", new String[]{"-,01"}, null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", new String[]{"-0,01"}, null));

        //**********************************************************************
        // Stringを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", "10", null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", "10.000", null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", ",01", null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", "0,01", null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", "+10", null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", "+10.000", null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", "+,01", null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", "+0,01", null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", "-10", null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", "-10.000", null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", "-,01", null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", "-0,01", null));
    }

    /**
     * {@link BigDecimalConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     *
     * nullを許可するケース
     */
    @Test
    public void testConvertAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new BigDecimalConvertor();
        testee.setMultiInputMessageId("MSG00001");
        testee.setInvalidDigitsFractionMessageId("MSG00002");
        testee.setInvalidDigitsIntegerMessageId("MSG00003");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", new String[]{"10"},
                null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", 
                new String[]{"10,000"}, null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", new String[]{".01"},
                null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", 
                new String[]{"0.01"}, null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", new String[]{"+10"},
                null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", 
                new String[]{"+10,000"}, null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", 
                new String[]{"+.01"}, null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", 
                new String[]{"+0.01"}, null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", new String[]{"-10"},
                null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", 
                new String[]{"-10,000"}, null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", 
                new String[]{"-.01"}, null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", 
                new String[]{"-0.01"}, null));

        //**********************************************************************
        // Stringを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", "10", null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", "10,000", null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", ".01", null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", "0.01", null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", "+10", null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", "+10,000", null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", "+.01", null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", "+0.01", null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", "-10", null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", "-10,000", null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", "-.01", null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", "-0.01", null));

        //**********************************************************************
        // Integerを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"),
                testee.convert(context, "param", new Integer("10"), null));
        assertEquals(new BigDecimal("10000"),
                testee.convert(context, "param", new Integer("10000"), null));
        assertEquals(new BigDecimal("-10"),
                testee.convert(context, "param", new Integer("-10"), null));
        assertEquals(new BigDecimal("-10000"),
                testee.convert(context, "param", new Integer("-10000"), null));

        //**********************************************************************
        // Longを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"),
                testee.convert(context, "param", new Long("10"), null));
        assertEquals(new BigDecimal("10000"),
                testee.convert(context, "param", new Long("10000"), null));
        assertEquals(new BigDecimal("-10"),
                testee.convert(context, "param", new Long("-10"), null));
        assertEquals(new BigDecimal("-10000"),
                testee.convert(context, "param", new Long("-10000"), null));

        //**********************************************************************
        // BigDecimalを指定
        //**********************************************************************
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", new BigDecimal("10"),
                null));
        assertEquals(new BigDecimal("10000"), testee.convert(context, "param", new BigDecimal(
                "10000"), null));
        assertEquals(new BigDecimal(".01"), testee.convert(context, "param", new BigDecimal(
                ".01"), null));
        assertEquals(new BigDecimal("0.01"), testee.convert(context, "param", new BigDecimal(
                "0.01"), null));
        assertEquals(new BigDecimal("+10"), testee.convert(context, "param", new BigDecimal(
                "+10"), null));
        assertEquals(new BigDecimal("+10000"), testee.convert(context, "param", new BigDecimal(
                "+10000"), null));
        assertEquals(new BigDecimal("+.01"), testee.convert(context, "param", new BigDecimal(
                "+.01"), null));
        assertEquals(new BigDecimal("+0.01"), testee.convert(context, "param", new BigDecimal(
                "+0.01"), null));
        assertEquals(new BigDecimal("-10"), testee.convert(context, "param", new BigDecimal(
                "-10"), null));
        assertEquals(new BigDecimal("-10000"), testee.convert(context, "param", new BigDecimal(
                "-10000"), null));
        assertEquals(new BigDecimal("-.01"), testee.convert(context, "param", new BigDecimal(
                "-.01"), null));
        assertEquals(new BigDecimal("-0.01"), testee.convert(context, "param", new BigDecimal(
                "-0.01"), null));

        assertNull(testee.convert(context, "param", null, null));
    }


    /**
     * {@link BigDecimalConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
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
        assertFalse(testee.isConvertible(context, "param", "表示", "   \0\r\n\t+   \r\n\t\0", digits));

        // Stringを指定した場合に、全角スペースがトリムされず、isConvertibleの結果がfalseになることの確認
        assertFalse(testee.isConvertible(context, "param", "表示", "　　　10　　　", digits));
    }
    
    /**
     * {@link BigDecimalConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
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

        // Stringを指定した場合に、'\u0020'以下のコードの文字（半角スペース、改行コード、タブ、null文字など）がトリムされることの確認
        assertEquals(new BigDecimal("10"), testee.convert(context, "param", "   \0\r\n\t10   \r\n\t\0", null));

        // Stringを指定した場合に、全角スペースがトリムされないためBigDecimalへの変換に失敗し、nullが返却されることの確認
        assertNull(testee.convert(context, "param", "　　　10　　　", null));
    }

}
