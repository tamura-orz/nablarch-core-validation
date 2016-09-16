package nablarch.core.validation.validator;

import nablarch.core.ThreadContext;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.convertor.TestTarget;
import nablarch.core.validation.creator.ReflectionFormCreator;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;


public class NumberRangeValidatorTest {

    private NumberRangeValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}は{2}以下で入力してください。", "EN", "{0} cannot be greater than {2}." },
        { "MSG00002", "ja", "{0}は{1}以上{2}以下で入力してください。", "EN", "{0} is not in the range {1} through {2}." },
        { "MSG00003", "ja", "{0}は{1}以上で入力してください。", "EN", "{0} cannot be lesser than {2}." },
        { "MSG00004", "ja", "テストメッセージ01", "EN", "test message 01" },
        { "PROP0001", "ja", "プロパティ1", "EN", "property1" }, };


    @Before
    public void setUpClass() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("nablarch/core/validation/convertor-test-base.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new NumberRangeValidator();
        testee.setMaxMessageId("MSG00001");
        testee.setMaxAndMinMessageId("MSG00002");
        testee.setMinMessageId("MSG00003");


        Map<String, String[]> params = new HashMap<String, String[]>();
        
        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

    }

    private NumberRange range = new NumberRange() {
        
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        public double min() {
            return 10.0;
        }
        
        public String messageId() {
            return "";
        }
        
        public double max() {
            return 20.1;
        }
    };

    @Test
    public void testValidateSuccess() {

        
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10l));        
        assertTrue(testee.validate(context, "param", "PROP0001", range, 11l));       
        assertTrue(testee.validate(context, "param", "PROP0001", range, 20l));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10));
        assertTrue(testee.validate(context, "param", "PROP0001", range, new BigDecimal(11)));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10.1));
        assertTrue(testee.validate(context, "param", "PROP0001", range, 10.1f));
        
    }

    @Test
    public void testValidateGreaterWithMin() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20.1以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinInteger() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20.1以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinBigDecimal() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, new BigDecimal(21)));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20.1以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinDouble() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 20.2));
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20.1以下で入力してください。", context.getMessages().get(0).formatMessage());

        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("max", 20.1);
        }}, 20.2));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterWithMinFloat() {

        assertFalse(testee.validate(context, "param", "PROP0001", range, 20.2f));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上20.1以下で入力してください。", context.getMessages().get(0).formatMessage());
    }


    private NumberRange lesserRange = new NumberRange() {
        
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        public double min() {
            return 10;
        }
        
        public String messageId() {
            return "";
        }
        
        public double max() {
            return Double.POSITIVE_INFINITY;
        }
    };

    @Test
    public void testValidateLesser() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserInteger() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserBigDecimal() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, new BigDecimal(9l)));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserDouble() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9.9));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLesserFloat() {

        assertFalse(testee.validate(context, "param", "PROP0001", lesserRange, 9.9f));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10以上で入力してください。", context.getMessages().get(0).formatMessage());


        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("min", 10.1);
        }}, 9.9f));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateNull() {
    	// NULLはrequiredではない場合にありえる。

        assertTrue(testee.validate(context, "param", "PROP0001", lesserRange, null));    
    }

    @Test
    public void testValidateGreater() {
        NumberRange range = new NumberRange() {
            
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            public double min() {
                return Double.NEGATIVE_INFINITY;
            }
            
            public String messageId() {
                return "";
            }
            
            public double max() {
                return 20;
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は20以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    private NumberRange range03 = new NumberRange() {
        
        public Class<? extends Annotation> annotationType() {
            return NumberRange.class;
        }
        
        public double min() {
            return 10;
        }
        
        public String messageId() {
        	return "MSG00004";
        }
        
        public double max() {
            return 20;
        }
    };

    @Test
    public void testValidateLesserAnnotationMessage() {

        assertFalse(testee.validate(context, "param", "PROP0001", range03, 9l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateGreaterAnnotationMessage() {
        NumberRange range = new NumberRange() {
            
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            public double min() {
                return Long.MIN_VALUE;
            }
            
            public String messageId() {
                return "MSG00004";
            }
            
            public double max() {
                return 20;
            }
        };

        assertFalse(testee.validate(context, "param", "PROP0001", range, 21l));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01", context.getMessages().get(0).formatMessage());

        assertFalse(testee.validate(context, "param", "PROP0001", new HashMap<String, Object>() {{
            put("min", 0D);
            put("max", 20D);
            put("messageId", "MSG00004");
        }}, 211));
        assertEquals(2, context.getMessages().size());
        assertEquals(context.getMessages().get(0).formatMessage(), context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateMaxNotSpecified() {
        NumberRange range = new NumberRange() {
            
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }
            
            public double min() {
                return Double.NEGATIVE_INFINITY;
            }
            
            public String messageId() {
                return "MSG00004";
            }
            
            public double max() {
                return Double.POSITIVE_INFINITY;
            }
        };

        assertTrue(testee.validate(context, "param", "PROP0001", range, 21l));    
    }

    @Test
    public void testGetAnnotationClass() {
    	assertEquals(NumberRange.class, testee.getAnnotationClass());
    }

}
