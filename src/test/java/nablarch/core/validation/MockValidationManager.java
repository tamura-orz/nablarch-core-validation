package nablarch.core.validation;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class MockValidationManager extends ValidationManager {

    private ValidationContext context;
    
    public ValidationContext getValidationContext() {
        return context;
    }
    
    @SuppressWarnings("unchecked")
    public <T> ValidationContext<T> validateAndConvert(String prefix, Class<T> targetClass, Map<String, ?> params, String validateFor) {
        context = super.validateAndConvert(prefix, targetClass, params, validateFor);
        return context;
    }
}
