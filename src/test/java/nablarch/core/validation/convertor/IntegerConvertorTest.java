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

public class IntegerConvertorTest {

    private static IntegerConvertor testee;
    private static MockStringResourceHolder resource;

    @ClassRule
    public static SystemRepositoryResource repo = new SystemRepositoryResource("nablarch/core/validation/convertor-test-base.xml");

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}は整数で入力してください。", "en", "please input {0} as integer." },
        { "MSG00002", "ja", "{0}は正の整数で入力してください。", "en", "please input {0} as positive integer." },
        { "PROP0001", "ja", "プロパティ1", "en", "property1" }, };

    private static final Digits maxLengthDigits = new Digits() {

        public int integer() {
            return 9;
        }

        public int fraction() {
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
    };


    @BeforeClass
    public static void setUpClass() {
        resource = repo.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
    }

    /**
     * 各テストケース毎の事前準備
     */
    @Before
    public void setUp() {
        testee = new IntegerConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");
        // デフォルト動作としてnullは許可しない。
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
            return 0;
        }
        
        public boolean commaSeparated() {
            return true;
        }
    };

    /**
     * {@link IntegerConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
     * nullを許可しない場合のテスト。
     */
    @Test
    public void testIsConvertible() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列のテスト。
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
                new String[]{"+1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{String.valueOf(Long.MAX_VALUE)}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[] {"１"}, digits));

        assertEquals(7, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.", context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // Stringのテスト。
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "10", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10.", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "+1", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                String.valueOf(Long.MAX_VALUE), digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "２", digits));

        assertEquals(5, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.", context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // 数値型のテスト
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(10), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(-10), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(123456789), maxLengthDigits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Long(123456789), maxLengthDigits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new BigDecimal("123456789"), maxLengthDigits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new Long(1234567890), maxLengthDigits));

        //**********************************************************************
        // nullのテスト
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertFalse(testee.isConvertible(context, "param", "PROP0001", null, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("property1", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testIsConvertibleForI18N() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"decimal{###}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列のテスト。
        //**********************************************************************
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"1,000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"."}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{","}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"+1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{String.valueOf(Long.MAX_VALUE)}, digits));
        assertEquals(6, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.", context.getMessages().get(0).formatMessage());

        params.put("param_nablarch_formatSpec", new String[]{"decimal{###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // Stringのテスト。
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "10", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "1.000", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10,", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "+1", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                String.valueOf(Long.MAX_VALUE), digits));

        assertEquals(4, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.", context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // nullのテスト
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertFalse(testee.isConvertible(context, "param", "PROP0001", null, digits));

        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("property1", context.getMessages().get(0).formatMessage());

        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"integer{###|es}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"10"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"1,000"}, digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new String[]{"-10"}, digits));
    }

    /**
     * {@link IntegerConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
     * nullを許可する場合のテスト。
     */
    @Test
    public void testIsConvertibleAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new IntegerConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //**********************************************************************
        // String配列のテスト。
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
                new String[]{"+1"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new String[]{String.valueOf(Long.MAX_VALUE)}, digits));
        assertEquals(4, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // Stringのテスト。
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "10", digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                "-10", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-10.", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "-", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                "+1", digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                String.valueOf(Long.MAX_VALUE), digits));

        assertEquals(4, context.getMessages().size());
        ThreadContext.setLanguage(Locale.ENGLISH);
        assertEquals("please input PROP0001 as integer.",
                context.getMessages().get(0).formatMessage());

        //**********************************************************************
        // 数値型のテスト
        //**********************************************************************
        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(10), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(-10), digits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Integer(123456789), maxLengthDigits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new Long(123456789), maxLengthDigits));
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                new BigDecimal("123456789"), maxLengthDigits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001",
                new Long(1234567890), maxLengthDigits));

        //**********************************************************************
        // nullのテスト
        //**********************************************************************
        assertTrue(testee.isConvertible(context, "param", "PROP0001",
                null, digits));

    }

    @Test
    public void testGetTargetClass() {

        assertEquals(Integer.class, testee.getTargetClass());
    }

    /**
     * {@link IntegerConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * nullを許可しない場合のテスト。
     */
    @Test
    public void testisConvert() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        Integer i = (Integer) testee.convert(context, "param", new String[]{"10"}, digits);
        assertEquals(Integer.valueOf(10), i);

        Integer i2 = (Integer) testee.convert(context, "param", new Integer(10), digits);
        assertEquals(Integer.valueOf(10), i2);

        Integer i3 = (Integer) testee.convert(context, "param", new Integer(Integer.MAX_VALUE), digits);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), i3);

    }

    /**
     * {@link IntegerConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
     * nullを許可する場合のテスト。
     */
    @Test
    public void testisConvertAllowNullValue() {

        // nullを許可する。(デフォルト動作)
        testee = new IntegerConvertor();
        testee.setInvalidDigitsIntegerMessageId("MSG00001");
        testee.setMultiInputMessageId("PROP0001");

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        Integer i = (Integer) testee.convert(context, "param", new String[]{"10"}, digits);
        assertEquals(Integer.valueOf(10), i);

        Integer i2 = (Integer) testee.convert(context, "param", new Integer(10), digits);
        assertEquals(Integer.valueOf(10), i2);

        Integer i3 = (Integer) testee.convert(context, "param", new Integer(Integer.MAX_VALUE), digits);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), i3);

        Integer i4 = (Integer) testee.convert(context, "param", null, digits);
        assertNull(i4);

    }

    @Test
    public void testisConvertibleWithFraction() {

        Map<String, String[]> params = new HashMap<String, String[]>();
        
        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        //***********************************************************************
        // 小数部を指定した場合
        //***********************************************************************
        try {
            assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10."}, new Digits() {
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

                public String messageId() {
                    return "";
                }

                public Class<? extends Annotation> annotationType() {
                    return Digits.class;
                }
            }));
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Fraction value was specified."));
        }

        //***********************************************************************
        // 整数部の桁数に10桁以上を指定した場合
        //***********************************************************************
        try {
            assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"-10."}, new Digits() {
                public int integer() {
                    return 10;
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
            assertThat(e.getMessage(), is("length was invalid. integer length must be less than or equal to 9 digit. specified value:10"));
        }
    }

    @Test
    public void testisConvertibleWithoutComma() {

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
                return 9;
            }
            
            public int fraction() {
                return 0;
            }
            
            public boolean commaSeparated() {
                return false;
            }
        };

        
        assertTrue(testee.isConvertible(context, "param", "PROP0001", new String[]{"1010"}, digits));
        assertFalse(testee.isConvertible(context, "param", "PROP0001", new String[]{"1,010"}, digits));
        assertEquals("please input PROP0001 as integer.", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testConvert() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

        assertEquals(100, testee.convert(context, "param", new String[]{"100"}, digits));
        assertEquals(10, testee.convert(context, "param", new String[]{"10"}, digits));
        assertEquals(10000, testee.convert(context, "param", new String[]{"10,000"}, digits));
        assertNull(testee.convert(context, "param", new String[]{""}, digits));
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

        assertEquals(100, testee.convert(context, "param", new String[]{"100"}, digits));
        assertEquals(10, testee.convert(context, "param", new String[]{"10"}, digits));
        assertEquals(10000, testee.convert(context, "param", new String[]{"10.000"}, digits));
        assertNull(testee.convert(context, "param", new String[]{""}, digits));
    }

    /**
     * {@link IntegerConvertor#isConvertible(ValidationContext, String, Object, Object, Annotation)}のテスト。
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
     * {@link IntegerConvertor#convert(ValidationContext, String, Object, Annotation)}のテスト。
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
        assertEquals(new Integer("10"), testee.convert(context, "param", "   \0\r\n\t10   \r\n\t\0", null));

        // Stringを指定した場合に、全角スペースがトリムされないためBigDecimalへの変換に失敗し、nullが返却されることの確認
        assertNull(testee.convert(context, "param", "　　　10　　　", null));
    }
}
