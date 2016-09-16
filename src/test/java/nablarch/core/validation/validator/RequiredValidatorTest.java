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

import static org.junit.Assert.*;


public class RequiredValidatorTest {

    private RequiredValidator testee;
    private MockStringResourceHolder resource;
    private ValidationContext<TestTarget> context;

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}は必ず入力してください。", "EN", "{0} is required." },
        { "MSG00002", "ja", "テストメッセージ01。", "EN", "test message 01." },
        { "PROP0001", "ja", "プロパティ1", "EN", "property1" }, };


    @Before
    public void setUp() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("nablarch/core/validation/convertor-test-base.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new RequiredValidator();
        testee.setMessageId("MSG00001");
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        context = new ValidationContext<TestTarget>(
                "", TestTarget.class, new ReflectionFormCreator(),
                params, "");

    }

    private Required required = new Required() {

        public Class<? extends Annotation> annotationType() {
            return Required.class;
        }

        public String messageId() {
            return "";
        }
    };

    @Test
    public void testValidateSuccessString() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, "12345"));
    }

    @Test
    public void testValidateSuccessNumber() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, Integer.valueOf("12345")));
    }

    @Test
    public void testValidateFailString() {

        assertFalse(testee.validate(context, "param", "PROP0001", required, ""));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は必ず入力してください。", context.getMessages().get(0).formatMessage());
    }


    @Test
    public void testValidateSuccessStringArray() {

        assertTrue(testee.validate(context, "param", "PROP0001", required, new String[] {"12345"}));
    }

    @Test
    public void testValidateFailStringArray() {
        Required required = new Required() {

            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            public String messageId() {
                return "MSG00002";
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", required, new String[] {} ));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValidateFailWithAnnotationMessageId() {
        Required required = new Required() {
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            public String messageId() {
                return "MSG00002";
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", required, ""));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testValueIsNull() {
        Required required = new Required() {
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            public String messageId() {
                return "MSG00002";
            }
        };
        assertFalse(testee.validate(context, "param", "PROP0001", required, null));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testAnnotationMap() {
        assertFalse(testee.validate(context, "param", "PROP0001",
                new HashMap<String, Object>() {{
                    put("messageId", "MSG00002");
                }},
                null));
        assertFalse(testee.validate(context, "param", "PROP0001", required, null));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("テストメッセージ01。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testAnnotationMapDefaultMessage() {
        assertFalse(testee.validate(context, "param", "PROP0001",
                new HashMap<String, Object>(), null));
        assertFalse(testee.validate(context, "param", "PROP0001", required, null));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は必ず入力してください。", context.getMessages().get(0).formatMessage());
    }

    @Test
    public void testGetAnnotationClass() {
    	assertEquals(Required.class, testee.getAnnotationClass());
    }
}
