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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class LengthValidatorTest {

    private LengthValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}は{2}文字以下で入力してください。", "EN", "{0} cannot be longer than {2}." },
        { "MSG00002", "ja", "{0}は{1}文字以上{2}文字以下で入力してください。", "EN", "{0} is not in the range {1} through {2}." },
        { "MSG00003", "ja", "テストメッセージ01。", "EN", "test message 01." },
        { "MSG00004", "ja", "入力値が不正です。", "EN", "input value is invalid." },
        { "MSG00005", "ja", "{0}は{1}文字で入力してください。", "EN", "{0}'s length cannot be length {1}." },
        { "PROP0001", "ja", "プロパティ1", "EN", "property1" }, };


    @Before
    public void setUp() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("nablarch/core/validation/convertor-test-base.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new LengthValidator();
        testee.setMaxMessageId("MSG00001");
        testee.setMaxAndMinMessageId("MSG00002");
        testee.setFixLengthMessageId("MSG00005");
        Map<String, String[]> params = new HashMap<String, String[]>();
        
        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

    }

    private Length length = new Length() {

        public Class<? extends Annotation> annotationType() {
            return Length.class;
        }

        public int min() {
            return 5;
        }

        public String messageId() {
            return "";
        }

        public int max() {
            return 10;
        }
    };

    @Test
    public void testValidateSuccess() {

        assertTrue(testee.validate(context, "param", "PROP0001", length, "12345"));        
        assertTrue(testee.validate(context, "param", "PROP0001", length, "1234567"));       
        assertTrue(testee.validate(context, "param", "PROP0001", length, "1234567890"));        
    }
    @Test
    public void testValidateLonger() {
        Length length = new Length() {

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            public int min() {
                return 0;
            }

            public String messageId() {
                return "";
            }

            public int max() {
                return 10;
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", length, "12345678901"));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10文字以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLongerWithMin() {

        assertFalse(testee.validate(context, "param", "PROP0001", length, "12345678901"));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は5文字以上10文字以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateShorter() {

        assertFalse(testee.validate(context, "param", "PROP0001", length, "1234"));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は5文字以上10文字以下で入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateLengthZero() {

        Length length = new Length() {

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            public int min() {
                return 8;
            }

            public String messageId() {
                return "";
            }

            public int max() {
                return 10;
            }
        };
        // min を指定していても、長さ0の文字列は許可する
        assertTrue(testee.validate(context, "param", "PROP0001", length, ""));    
    }

    @Test
    public void testValidateMulti() {

        assertTrue(testee.validate(context, "param", "PROP0001", length, new String[] {"12345", "0123456789"}));    
        assertFalse(testee.validate(context, "param", "PROP0001", length, new String[] {"12345", "01234567890", "1234"}));    
        
        assertEquals(1, context.getMessages().size());
    }

    @Test
    public void testValidateLongerAnnotationMessage() {
        Length length = new Length() {

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            public int min() {
                return 0;
            }

            public String messageId() {
                return "MSG00003";
            }

            public int max() {
                return 10;
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", length, "12345678901"));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateFixedLengthMessage() {
        Length length = new Length() {

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            public int min() {
                return 10;
            }

            public String messageId() {
                return "";
            }

            public int max() {
                return 10;
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", length, "12345678901"));    
        
        assertEquals(1, context.getMessages().size());
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は10文字で入力してください。", context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateMaxIsNotSet() {
        Length length = new Length() {

            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            public int min() {
                return 0;
            }

            public String messageId() {
                return "MSG00003";
            }

            public int max() {
                return 0;
            }
        };
        // MAXおよびMINが0の場合は全てtrue(カバレッジ以外に意味はない。)
        assertTrue(testee.validate(context, "param", "PROP0001", length, "12345678901"));    
        
    }

    @Test
    public void testCreateAnnotation() {
        LengthValidator sut = new LengthValidator();
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("max", 10);
        Length lengthAnnotation = sut.createAnnotation(param);

        assertThat(lengthAnnotation, is(instanceOf(lengthAnnotation.annotationType())));
        assertThat("minはデフォルト値の0となる", lengthAnnotation.min(), is(0));
        assertThat(lengthAnnotation.max(), is(10));
        assertThat(lengthAnnotation.messageId(), is(""));

        param.put("min", -1);
        lengthAnnotation = sut.createAnnotation(param);
        assertThat("minはマイナス値の0となる", lengthAnnotation.min(), is(0));

        param.put("min", 5);
        param.put("messageId", "id");
        lengthAnnotation = sut.createAnnotation(param);
        assertThat("minはデフォルト値の0となる", lengthAnnotation.min(), is(5));
        assertThat(lengthAnnotation.max(), is(10));
        assertThat(lengthAnnotation.messageId(), is("id"));

        param.put("max", null);
        lengthAnnotation = sut.createAnnotation(param);
        try {
            lengthAnnotation.max();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testGetAnnotationClass() {
    	assertEquals(Length.class, testee.getAnnotationClass());
    }
}
