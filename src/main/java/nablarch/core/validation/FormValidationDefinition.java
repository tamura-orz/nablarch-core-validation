package nablarch.core.validation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.core.util.ObjectUtil;

/**
 * フォームに紐付けられたバリデーションの設定を保持するクラス。
 * 
 * @author Koichi Asano
 *
 */
public class FormValidationDefinition {

    /**
     * フォームクラス。
     */
    private Class<?> formClass;

    /**
     * ValidateForアノテーションが付けられたメソッドのリストを保持するMap。
     */
    private final Map<String, List<Method>> validateForMethods;

    /**
     * プロパティ定義のMap。
     */
    private final Map<String, PropertyValidationDefinition> propertyDefinitions;

    /**
     * コンストラクタ。
     * 
     * @param formClass バリデーション対象のフォーム
     */
    public FormValidationDefinition(Class<?> formClass) {
        this.formClass = formClass;
        
        propertyDefinitions = getPropertyDefinitions(formClass);

        validateForMethods = getValidateForMethods(formClass);
    }

    /**
     * クラスからvalidateForアノテーションのついたメソッドを取得する。
     * @param formClass 取得元のクラス 
     * @return validateForアノテーションの値をキー、validateForアノテーションのついたメソッド
     *                     を値とするMap
     */
    private Map<String, List<Method>> getValidateForMethods(Class<?> formClass) {
        Map<String, List<Method>> methods = new HashMap<String, List<Method>>();
        for (Method method : formClass.getMethods()) {
            addValidateForMethod(methods, method);
        }
        return Collections.unmodifiableMap(methods);
    }

    /**
     * クラスからプロパティの定義を取得する。
     * @param formClass 取得元のクラス
     * @return プロパティ名をキー、プロパティ定義を値に持つMap
     */
    private Map<String, PropertyValidationDefinition> getPropertyDefinitions(Class<?> formClass) {
        Map<String, PropertyValidationDefinition> props = new HashMap<String, PropertyValidationDefinition>();
        for (Method method : ObjectUtil.getSetterMethods(formClass)) {

            PropertyValidationDefinition definition = createPropertyValidationDefinition(formClass, method);
            props.put(ObjectUtil.getPropertyNameFromSetter(method), definition);
        }
        return Collections.unmodifiableMap(props);
    }

    /**
     * PropertyValidationDefinition を作成する。
     * @param formClass 対象のクラス
     * @param method 作成対象のメソッド
     * @return 作成したPropertyValidationDefinition
     */
    private PropertyValidationDefinition createPropertyValidationDefinition(Class<?> formClass, Method method) {
        
        Class<?> superclass = formClass.getSuperclass();
        if (superclass == Object.class) {
            return new PropertyValidationDefinition(formClass, method, null);
        }

        Method superClassMethod = ObjectUtil.findMatchMethod(superclass, method.getName(), method.getParameterTypes());
        if (superClassMethod != null) {
            // オーバライドしたメソッドの定義を作成
            PropertyValidationDefinition superClassPropertyDef = createPropertyValidationDefinition(superClassMethod.getDeclaringClass(), superClassMethod);
         // オーバライドしたメソッドの定義を加味して定義を作成
            return new PropertyValidationDefinition(formClass, method, superClassPropertyDef);
        } else {
            return new PropertyValidationDefinition(formClass, method, null);
        }
    }

    /**
     * ValidateForアノテーションがついたメソッドをvalidateForMethodsに追加する。<br/>
     * ValidateForアノテーションがついてなければなにもしない。
     * @param map 追加するMap
     * @param method 追加対象のメソッド
     */
    private void addValidateForMethod(Map<String, List<Method>> map, Method method) {
        ValidateFor validateForAnnotation = method.getAnnotation(ValidateFor.class);
        if (validateForAnnotation != null) {
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("ValidateFor method was not static. "
                        + "class name = " + formClass.getName()
                        + ", method name = " + method.getName());
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1
                    || params[0] != ValidationContext.class) {
                throw new IllegalArgumentException("ValidateFor method signature was not valid. "
                        + "class name = " + formClass.getName()
                        + ", method name = " + method.getName());
            }
            for (String name : validateForAnnotation.value()) {
                List<Method> methods = map.get(name);
                if (methods == null) {
                    methods = new ArrayList<Method>();
                    map.put(name, methods);
                }
                methods.add(method);
            }
        }
    }

    /**
     * ValidateForアノテーションのついたメソッドをValidateForのvalueに指定したメソッド名を元に取得する。
     * 
     * @param methodName ValidateForのvalueに指定したメソッド名
     * @return ValidateForアノテーションのついたメソッド
     */
    public List<Method> getValidateForMethods(String methodName) {
        if (!validateForMethods.containsKey(methodName)) {
            throw new IllegalArgumentException("Couldn't find method. "
                    + "class name = " + formClass.getName()
                    + ", method name = " + methodName);
        }
        return validateForMethods.get(methodName);
    }

    /**
     * プロパティ名にマッチしたPropertyValidationDefinitionを取得する。
     * 
     * @param propertyName プロパティ名
     * @return プロパティ名にマッチしたPropertyValidationDefinition
     */
    public PropertyValidationDefinition getPropertyValidationDefinition(String propertyName) {
        if (!propertyDefinitions.containsKey(propertyName)) {
            throw new IllegalArgumentException("Couldn't find property. "
                    + "class name = " + formClass.getName()
                    + ", property name = " + propertyName);
        }

        return propertyDefinitions.get(propertyName);
    }

    /**
     * フォームのプロパティ定義を全て取得する。<br/>
     * 取得したMapは変更できない。
     * 
     * @return フォームのプロパティ定義のMap
     */
    public Map<String, PropertyValidationDefinition> getPropertyValidationDefinitions() {
        return propertyDefinitions;
    }
}
