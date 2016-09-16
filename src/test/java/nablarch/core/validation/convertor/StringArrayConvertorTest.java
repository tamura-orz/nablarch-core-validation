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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StringArrayConvertorTest {
    private static StringArrayConvertor testee;

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
        testee = new StringArrayConvertor();
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

            
            //　配列長違いは全てOK
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"10", "20", "30"}, null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{"10"}, null));
            assertTrue(testee.isConvertible(context, "param", "PROP0001",
                    new String[]{}, null));

            // nullは許可
            assertTrue(testee.isConvertible(context, "param", "PROP0001", null,
                    null));
        }


        {
        	// 引数の型が String[]以外の場合
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class,
                    new ReflectionFormCreator(),
                    params, "");
        	// ここに Integer が設定されるのは、(今のところ)プログラムバグのみ。
            try {
            	testee.isConvertible(context, "param", "PROP0001", Integer.valueOf(1),
                    null);
            	fail("例外が発生するはず。");
            } catch (IllegalArgumentException e) {
            	
            }
        }
    }

}
