package nablarch.core.validation.convertor;

import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.creator.ReflectionFormCreator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link BooleanConvertor}のテストを行います。
 * 
 * @author TIS
 */
public class BooleanConvertorTest {
    private static BooleanConvertor testee;

    private static MockStringResourceHolder resource;

    private static final String[][] MESSAGES = {
            {"MSG00001", "ja", "{0}が正しくありません。", "en", "value of {0} is not valid."},
            {"PROP0001", "ja", "プロパティ1", "en", "property1"},};



    @BeforeClass
    public static void setUpClass() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/validation/convertor-test-base.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        resource = container.getComponentByType(MockStringResourceHolder.class);
        resource.setMessages(MESSAGES);
        testee = new BooleanConvertor();
        testee.setConversionFailedMessageId("MSG00001");
    }


    @Test
    public void testIsConvertible() {

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        {
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            
            //　true/falseに変換できるものは全てOK
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "true", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "TRUE", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "TruE", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "false", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "FALSE", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    "FalsE", null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    true, null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    false, null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"true"}, null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"false"}, null));
            
            // nullは不許可
            testee.setAllowNullValue(false);
            assertFalse(testee.isConvertible(context, "param", "PROP0001", null,
                    null));
            
            // nullは許可(デフォルト動作)
            testee = new BooleanConvertor();
            testee.setConversionFailedMessageId("MSG00001");
            assertTrue(testee.isConvertible(context, "param", "PROP0001", null,
                    null));
            
            // 空の文字列配列はNG
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{}, null));
            
            // 文字列配列の２要素目以降は見れない
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"", "true"}, null));
            
            // true/false以外の文字列ははNG
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    "hoge", null));
            assertFalse(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"hoge"}, null));
        }            
    }

    @Test
    public void testConvert() {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("param", new String[]{"10"});

        {
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");

            
            //　true/falseに変換できるものは全てOK
            assertEquals(Boolean.class, testee.getTargetClass());
            assertTrue((Boolean)testee.convert(context, "param", "true", null));
            assertFalse((Boolean)testee.convert(context, "param", "false", null));
            assertTrue((Boolean)testee.convert(context, "param", new String[]{"true"}, null));
            assertFalse((Boolean)testee.convert(context, "param", new String[]{"false"}, null));
            
            // 変換できないものは全てfalse
            assertFalse((Boolean)testee.convert(context, "param", "hoge", null));
            assertFalse((Boolean)testee.convert(context, "param", new String[]{"hoge"}, null));
            assertFalse((Boolean)testee.convert(context, "param", null, null));
        }
    }
}
