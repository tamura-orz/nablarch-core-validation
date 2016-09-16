package nablarch.core.validation;

import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.convertor.RegexFormat;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class PropertyValidationDefinitionTest {

    @Test
    public void testConstructorWithPropertyName() throws Throwable {
        PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity.class, TestEntity.class.getMethod("setProp1", Long.class), null);
        
        assertEquals(3, def.getValidatorAnnotations().size());
        
        String messageId = def.getMessageId();
        RegexFormat format = (RegexFormat) def.getConvertorFormatAnnotation();
        Required required = (Required) def.getValidatorAnnotations().get(0);
        Length length = (Length) def.getValidatorAnnotations().get(1);
        NumberRange range = (NumberRange) def.getValidatorAnnotations().get(2);

        assertEquals("message02", messageId);
        assertTrue(required != null);
        assertEquals(10, length.max());
        assertEquals("test", format.value());
        assertEquals(0.0, range.min());
        assertEquals(10.0, range.max());
        
    }


    @Test
    public void testConstructor() throws Throwable {
        { 
            PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity.class, TestEntity.class.getMethod("setProp2", Long.class), null);
            
            assertEquals(3, def.getValidatorAnnotations().size());
            
            String messageId = def.getMessageId();
            RegexFormat format = (RegexFormat) def.getConvertorFormatAnnotation();
            Required required = (Required) def.getValidatorAnnotations().get(0);
            Length length = (Length) def.getValidatorAnnotations().get(1);
            NumberRange range = (NumberRange) def.getValidatorAnnotations().get(2);
    
            assertNull(messageId);
            assertTrue(required != null);
            assertEquals(10, length.max());
            assertEquals("test", format.value());
            assertEquals(0.0, range.min());
            assertEquals(10.0, range.max());
        }        
        

        { 
            PropertyValidationDefinition parentDef = new PropertyValidationDefinition(TestEntity.class, TestEntity.class.getMethod("setBd1", BigDecimal.class), null);
            PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity2.class, TestEntity2.class.getMethod("setBd1", BigDecimal.class), parentDef);

            List<Annotation> validatorAnnotations = def.getValidatorAnnotations();
            
            assertEquals(1, validatorAnnotations.size());
            NumberRange numberRange = (NumberRange) validatorAnnotations.get(0);
            Digits digits = (Digits) def.getConvertorFormatAnnotation();
            
            assertEquals(-10.0d, numberRange.min(), 0.01d);
            assertEquals(200.0d, numberRange.max(), 0.01d);
            
            assertEquals(1, digits.integer());
            assertEquals(1, digits.fraction());
        }
        


        { 
            PropertyValidationDefinition parentDef = new PropertyValidationDefinition(TestEntity.class, TestEntity.class.getMethod("setOverrideBd", BigDecimal.class), null);
            PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity2.class, TestEntity2.class.getMethod("setOverrideBd", BigDecimal.class), parentDef);
            
            List<Annotation> validatorAnnotations = def.getValidatorAnnotations();
            
            assertEquals(1, validatorAnnotations.size());
            NumberRange numberRange = (NumberRange) validatorAnnotations.get(0);
            Digits digits = (Digits) def.getConvertorFormatAnnotation();
            
            assertEquals(-20.0d, numberRange.min(), 0.01d);
            assertEquals(10.0d, numberRange.max(), 0.01d);
            
            assertEquals(2, digits.integer());
            assertEquals(2, digits.fraction());
        }

        { 
            PropertyValidationDefinition parentDef = new PropertyValidationDefinition(TestEntity.class, TestEntity.class.getMethod("setOverrideBd2", BigDecimal.class), null);
            PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity2.class, TestEntity2.class.getMethod("setOverrideBd2", BigDecimal.class), parentDef);
            List<Annotation> validatorAnnotations = def.getValidatorAnnotations();
            
            assertEquals(1, validatorAnnotations.size());
            NumberRange numberRange = (NumberRange) validatorAnnotations.get(0);
            Digits digits = (Digits) def.getConvertorFormatAnnotation();
            
            assertEquals(-30.0d, numberRange.min(), 0.01d);
            assertEquals(20.0d, numberRange.max(), 0.01d);
            
            assertEquals(4, digits.integer());
            assertEquals(4, digits.fraction());
        }

        {
            // 親定義が渡されない場合も一応テスト。
            PropertyValidationDefinition def = new PropertyValidationDefinition(TestEntity2.class, TestEntity2.class.getMethod("setOverrideBd2", BigDecimal.class), null);
            List<Annotation> validatorAnnotations = def.getValidatorAnnotations();
            
            assertEquals(1, validatorAnnotations.size());
            NumberRange numberRange = (NumberRange) validatorAnnotations.get(0);
            Digits digits = (Digits) def.getConvertorFormatAnnotation();
            
            assertEquals(-30.0d, numberRange.min(), 0.01d);
            assertEquals(20.0d, numberRange.max(), 0.01d);
            
            assertEquals(4, digits.integer());
            assertEquals(4, digits.fraction());
        }
    }

    public static class TestEntity {
        @PropertyName(messageId="message02")
        @Required
        @Length(max=10)
        @RegexFormat("test")
        @NumberRange(min=0, max=10)
        public void setProp1(Long l) {
            
        }

        @Required
        @Length(max=10)
        @RegexFormat("test")
        @NumberRange(min=0, max=10)
        public void setProp2(Long l) {
            
        }

        @NumberRange(min=-10, max=200)
        @Digits(integer=1, fraction=1)
        public void setBd1(BigDecimal bd) {
            
        }
        

        @NumberRange(min=-10, max=200)
        @Digits(integer=2, fraction=2)
        public void setOverrideBd(BigDecimal bd) {
            
        }
        @NumberRange(min=-10, max=200)
        @Digits(integer=3, fraction=3)
        public void setOverrideBd2(BigDecimal bd) {
            
        }
    }

    public static class TestEntity2 extends TestEntity {
        @Override
        public void setBd1(BigDecimal bd) {
            super.setBd1(bd);
        }

        @Override
        @NumberRange(min=-20, max=10)
        public void setOverrideBd(BigDecimal bd) {
            super.setOverrideBd(bd);
        }
        @Override
        @NumberRange(min=-30, max=20)
        @Digits(integer=4, fraction=4)
        public void setOverrideBd2(BigDecimal bd) {
            super.setOverrideBd2(bd);
        }
    }
}
