package nablarch.core.validation.domain;

import java.lang.annotation.Annotation;
import java.util.List;

import nablarch.core.util.annotation.Published;

/**
 * ドメイン定義を表すインタフェース。
 * 
 * 1つのコンバータ定義と複数のバリデーション定義を持つことができる。
 * 
 * @author kawasima
 * @author Kiyohito Itoh
 */
@Published(tag = "architect")
public interface DomainDefinition {

    /**
     * ドメイン定義に指定されたコンバータのアノテーションを取得する。
     * @return ドメイン定義に指定されたコンバータのアノテーション。コンバータのアノテーションが指定されていない場合はnull
     */
    Annotation getConvertorAnnotation();

    /**
     * ドメイン定義に指定されたバリデータのアノテーションを取得する。
     * @return ドメイン定義に指定されたバリデータのアノテーション
     */
    List<Annotation> getValidatorAnnotations();
}
