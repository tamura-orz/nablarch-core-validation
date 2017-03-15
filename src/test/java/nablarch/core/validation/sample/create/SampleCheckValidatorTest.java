package nablarch.core.validation.sample.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.cache.BasicStaticDataCache;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationManager;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.convertor.TestTarget;
import nablarch.core.validation.creator.ReflectionFormCreator;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class SampleCheckValidatorTest {

    private SampleCheckValidator testee;

    private ValidationContext<TestTarget> context;

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "nablarch/core/validation/sample/create/sample-validator.xml");

    private static final String[][] MESSAGES = {
            {"MSG00051", "ja", "{0}は \"0001\", \"0002\", \"0003\" のいずれかで入力してください。"},
            {"MSG00052", "ja", "{0}は \"0002\", \"0003\" のいずれかで入力してください。"},
            {"PROP0001", "ja", "プロパティ1"},};


    @Before
    public void setUp() {

        MockStringResourceHolder mock = repositoryResource.getComponent("stringResourceHolder");
        mock.setMessages(MESSAGES);

        testee = new SampleCheckValidator();
        testee.setAllow0001MessageId("MSG00051");
        testee.setDeny0001MessageId("MSG00052");
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[] {"10"});

        context = new ValidationContext<TestTarget>("", TestTarget.class, new ReflectionFormCreator(), params, "");
    }

    private SampleCheck allow0001 = new SampleCheck() {
        public Class<? extends Annotation> annotationType() {
            return SampleCheck.class;
        }

        public boolean allow0001() {
            return true;
        }
    };

    private SampleCheck deny0001 = new SampleCheck() {
        public Class<? extends Annotation> annotationType() {
            return SampleCheck.class;
        }

        public boolean allow0001() {
            return false;
        }
    };

    @Test
    public void testValidateSuccess() {

        assertTrue(testee.validate(context, "param", "PROP0001", deny0001, "0002"));
        assertTrue(testee.validate(context, "param", "PROP0001", deny0001, "0003"));
    }

    @Test
    public void testValidateFail() {

        assertFalse(testee.validate(context, "param", "PROP0001", deny0001, "0001"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は \"0002\", \"0003\" のいずれかで入力してください。", context.getMessages()
                                                                            .get(0)
                                                                            .formatMessage());
    }

    @Test
    public void testValidateAllow0001() {


        assertTrue(testee.validate(context, "param", "PROP0001", allow0001, "0001"));
        assertTrue(testee.validate(context, "param", "PROP0001", allow0001, "0002"));
        assertTrue(testee.validate(context, "param", "PROP0001", allow0001, "0003"));
    }

    @Test
    public void testValidateAllow0001Fail() {

        assertFalse(testee.validate(context, "param", "PROP0001", allow0001, "0004"));
        ThreadContext.setLanguage(Locale.JAPANESE);
        assertEquals("PROP0001は \"0001\", \"0002\", \"0003\" のいずれかで入力してください。", context.getMessages()
                                                                                      .get(0)
                                                                                      .formatMessage());
    }

    @Test
    public void testWithManager() {
        ValidationManager manager = (ValidationManager) SystemRepository.getObject("validationManager");
        BasicStaticDataCache stringResourceCache = (BasicStaticDataCache) SystemRepository.getObject(
                "stringResourceCache");
        BasicStaticDataCache formDefinitionCache = (BasicStaticDataCache) SystemRepository.getObject(
                "validationManager.formDefinitionCache");

        formDefinitionCache.initialize();
        manager.initialize();

        {
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("param1", new String[] {"0002"});
            params.put("param2", new String[] {"0001"});

            ValidationContext<TestEntity> result = ValidationUtil.validateAndConvertRequest("", TestEntity.class,
                    params, "validateAll");
            assertTrue(result.isValid());
        }
        {
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put("param1", new String[] {"0001"});
            params.put("param2", new String[] {"0001"});

            ValidationContext<TestEntity> result = ValidationUtil.validateAndConvertRequest("", TestEntity.class,
                    params, "validateAll");
            assertFalse(result.isValid());

        }
    }

    public static class TestEntity {

        private String param1;

        private String param2;

        public TestEntity(Map<String, Object> params) {

        }

        public String getParam1() {
            return param1;
        }

        @SampleCheck(allow0001 = false)
        public void setParam1(String param1) {
            this.param1 = param1;
        }

        public String getParam2() {
            return param2;
        }

        @SampleCheck(allow0001 = true)
        public void setParam2(String param2) {
            this.param2 = param2;
        }


        @ValidateFor("validateAll")
        public static void validateAll(ValidationContext<TestEntity> context) {
            ValidationUtil.validateWithout(context, new String[0]);
        }


    }
}
