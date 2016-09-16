package nablarch.core.validation;

import nablarch.core.validation.ValidationUtilTest.User;
import nablarch.core.validation.convertor.RegexFormat;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class EntityValidationDefinitionTest {

    @Test
    public void testConstructor() throws Throwable {
        FormValidationDefinition def = new FormValidationDefinition(TestEntity.class);
        
        PropertyValidationDefinition testDef = def.getPropertyValidationDefinition("test");
        PropertyValidationDefinition test2Def = def.getPropertyValidationDefinition("test2");


        String messageId1 = testDef.getMessageId();
        RegexFormat format = (RegexFormat) testDef.getConvertorFormatAnnotation();
        Required required1 = (Required) testDef.getValidatorAnnotations().get(0);
        Length length1 = (Length) testDef.getValidatorAnnotations().get(1);
        NumberRange range = (NumberRange) testDef.getValidatorAnnotations().get(2);

        String messageId2 = test2Def.getMessageId();
        Required required2 = (Required) test2Def.getValidatorAnnotations().get(0);
        Length length2 = (Length) test2Def.getValidatorAnnotations().get(1);

        assertEquals("message01", messageId1);
        assertTrue(required1 != null);
        assertEquals(10, length1.max());
        assertEquals("test", format.value());
        assertEquals(0.0, range.min());
        assertEquals(10.0, range.max());

        assertEquals("message02", messageId2);
        assertTrue(required2 != null);
        assertEquals(20, length2.max());
        
        assertEquals(2, def.getPropertyValidationDefinitions().size());
        
        try {
            def.getPropertyValidationDefinitions().put("test", null);
            fail("例外が発生するはず");
        } catch (Exception e) {
            
        }

        List<Method> insert = def.getValidateForMethods("insert");
        assertThat(insert.size(), is(2));
        assertThat(insert.contains(TestEntity.class.getMethod("validateFor1", ValidationContext.class)), is(true));
        assertThat(insert.contains(TestEntity.class.getMethod("validateFor2", ValidationContext.class)), is(true));


        List<Method> update = def.getValidateForMethods("update");
        assertThat(update.size(), is(1));
        assertThat(update.contains(TestEntity.class.getMethod("validateFor1", ValidationContext.class)), is(true));
    }

    @Test
    public void testConstructorNotStaticValidateForMethod() throws Throwable {
        try {
            FormValidationDefinition def = new FormValidationDefinition(ValidateForIsNotStaticEntity.class);
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {
        }
    }
        
    @Test
    public void testConstructorInvalidParameterValidateForMethod() throws Throwable {
        try {
        	new FormValidationDefinition(ValidateForParameterLengthUnmatchEntity.class);
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {
        }
//        try {
//            new EntityValidationDefinition(ValidateForParameterLengthAndTypeUnmatchEntity.class);
//            fail("例外が発生するはず");
//        } catch (IllegalArgumentException e) {
//        }
        try {
            new FormValidationDefinition(ValidateForParameterValueTypeUnmatchEntity.class);
            fail("例外が発生するはず");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetValidateForMethodsMethodNotFound() {
        FormValidationDefinition def = new FormValidationDefinition(TestEntity.class);
        try {
            def.getValidateForMethods("invalidMethodName");
        } catch (IllegalArgumentException e) {
            // OK 
        }
    }

    @Test
    public void testGetPropertyValidationDefinitionPropertyNotFound() {
        FormValidationDefinition def = new FormValidationDefinition(TestEntity.class);
        try {
            def.getPropertyValidationDefinition("invalidPropertyName");
        } catch (IllegalArgumentException e) {
            // OK 
        }
    }
    
    public static class TestEntity {
        @PropertyName(messageId="message01")
        @Required
        @Length(max=10)
        @RegexFormat("test")
        @NumberRange(min=0, max=10)
        public void setTest(Long l) {
            
        }

        @PropertyName(messageId="message02")
        @Required
        @Length(max=20)
        public void setTest2(Long l) {
            
        }

        @ValidateFor({"insert", "update"})
        public static void validateFor1(ValidationContext<User> context) {
        }
        @ValidateFor("insert")
        public static void validateFor2(ValidationContext<User> context) {
        }
    }

    public static class ValidateForIsNotStaticEntity {
        // staticではないvalidateForメソッド
        @ValidateFor("insert")
        public void validateFor1(ValidationContext<User> context) {
        }
    }

    public static class ValidateForParameterLengthIsNotValidationContextEntity {
        // 引数が不正
        @ValidateFor("insert")
        public static void validateFor1() {
        }
    }

    public static class ValidateForParameterLengthUnmatchEntity {
        // 引数が不正
        @ValidateFor("update")
        public static void validateFor2() {
        }
    }

//    public static class ValidateForParameterLengthAndTypeUnmatchEntity {
//        // 引数が不正
//        @ValidateFor("update")
//        public static void validateFor2(String value, String value2) {
//        }
//    }

    public static class ValidateForParameterValueTypeUnmatchEntity {
        // 引数が不正
        @ValidateFor("update")
        public static void validateFor2(String value) {
        }
    }
}
