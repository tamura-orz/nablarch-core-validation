package nablarch.core.validation.validator.unicode;

import nablarch.core.repository.SimpleLoader;
import nablarch.core.repository.SystemRepository;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * {@link SystemCharValidator}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class SystemCharValidatorTest {

    /** テスト対象 */
    private SystemCharValidator target = new SystemCharValidator();

    /** 制御コードを除くASCII */
    private static RangedCharsetDef asciiWoCC = new RangedCharsetDef();

    /** カナ */
    private static test.core.validation.validator.unicode.BlockNameCharsetDef kana = new test.core.validation.validator.unicode.BlockNameCharsetDef();

    /** CJK拡張B（サロゲートペア確認用） */
    private static test.core.validation.validator.unicode.BlockNameCharsetDef cjkExtensionB = new test.core.validation.validator.unicode.BlockNameCharsetDef();

    @BeforeClass
    public static void setUpClass() {
        SystemRepository.clear();
        asciiWoCC.setStartCodePoint("U+0020");
        asciiWoCC.setEndCodePoint("U+007E");
        kana.setBlockNames(Arrays.asList("KATAKANA", "HIRAGANA"));
        cjkExtensionB.setBlockNames(Arrays.asList("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B"));
    }

    /**
     * バリデーション対象文字列の各文字が、許可指定された文字集合に含まれている場合、
     * バリデーションが成功すること。
     */
    @Test
    public void testSingleCharsetDef() {
        // ASCII文字集合をシステムリポジトリに登録
        register("ascii", asciiWoCC);

        SystemChar anon = get("ascii", false);
        assertThat(target.isValid(anon, "01ABC"), is(true));
        assertThat(target.isValid(anon, "あ"), is(false));
    }

    /** アノテーションの改行コード許容設定が不許可の場合、改行コードが許容されないこと */
    @Test
    public void testLineSeparatorNotAllowed() {
        // ASCII文字集合をシステムリポジトリに登録
        register("ascii", asciiWoCC);
        SystemChar anon = get("ascii", false);
        assertThat(target.isValid(anon, "\n"), is(false));
        assertThat(target.isValid(anon, "\r"), is(false));
    }

    /** アノテーションの改行コード許容設定が許可の場合、改行コードが許容されること */
    @Test
    public void testLineSeparator() {
        // ASCII文字集合をシステムリポジトリに登録
        register("ascii", asciiWoCC);

        SystemChar anon = get("ascii", true);
        assertThat(target.isValid(anon, "01\rA\nBC\r\n"), is(true));     // ASCIIと改行コード
        assertThat(target.isValid(anon, "あ"), is(false));  // ASCIIでない
    }

    /**
     * 複数の文字集合を組み合わせた場合、
     * バリデーション対象文字列の各文字が、いずれかの文字集合に含まれていれば
     * バリデーションが成功すること。
     */
    @Test
    public void testComposite() {
        // ASCIIとカナをシステムリポジトリに登録
        register("ascii_and_kana", asciiWoCC, kana);

        SystemChar anon = get("ascii_and_kana", false);
        assertThat(target.isValid(anon, "01アあ"), is(true));
        assertThat(target.isValid(anon, "川"), is(false));  // ASCIIでもカナでもない
    }


    /**
     * サロゲートペアを許容するよう指定した場合、
     * バリデーション対象文字にサロゲートペアが含まれていた場合に、
     * 非許容と判定されないこと。
     */
    @Test
    public void testSurrogateArrow() {
        // カナとCJK拡張Bをシステムリポジトリに登録
        register("kana_and_surrogates", kana, cjkExtensionB);
        target.setAllowSurrogatePair(true);

        SystemChar anon = get("kana_and_surrogates", false);
        assertThat(target.isValid(anon, "かな\uD867\uDE3D"), is(true));
        assertThat(target.isValid(anon, "a"), is(false));
    }

    /**
     * サロゲートペアを許容するよう指定した場合、
     * バリデーション対象文字にサロゲートペアが含まれていた場合に、
     * 非許容と判定されること。
     */
    @Test
    public void testSurrogateNotArrowed() {
        // カナとCJK拡張Bをシステムリポジトリに登録
        register("kana_and_surrogates", kana, cjkExtensionB);
        target.setAllowSurrogatePair(false);

        SystemChar anon = get("kana_and_surrogates", false);
        assertThat(target.isValid(anon, "\uD867\uDE3D"), is(false));
    }

    /**
     * 指定された名前の文字集合定義がシステムリポジトリに存在しない場合、
     * 例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCharsetDefNotRegisteredInSystemRepository() {
        SystemChar anon = get("invalidCharsetDefinitionName", false);
        target.isValid(anon, "just a place holder.");
    }

    /**
     * アノテーションの許容文字集合指定がデフォルト設定の場合、
     * デフォルトの許容文字集合定義でバリデーションが実施されること。
     */
    @Test
    public void testUsingDefaultCharsetDef() {
        // ASCII文字集合をデフォルトに設定
        target.setDefaultCharsetDef(asciiWoCC);
        SystemChar anon = get("", false); // デフォルトの許容文字集合を指定（空文字がデフォルト値）
        assertThat(target.isValid(anon, "01ABC"), is(true));
        assertThat(target.isValid(anon, "あ"), is(false));
    }

    /** アノテーションクラスとして、{@link SystemChar}が返却されること。 */
    @Test
    public void testGetAnnotationClass() {
        assertEquals(SystemChar.class, target.getAnnotationClass());
    }

    /**
     * {@link CharsetDef}に設定したメッセージIDが取得できること。
     */
    @Test
    public void testGetMessageIdFromCharsetDef() {

        register("dummy", new LiteralCharsetDef());
        SystemChar anon = get("dummy", false);
        // メッセージIDが設定されていない場合は、アノテーションのメッセージIDが返却される。
        assertThat(target.getMessageIdFromAnnotation(anon), is("M001"));

        // CharsetDefにメッセージIDが設定されていてもアノテーションが優先されること。
        CompositeCharsetDef compo = new CompositeCharsetDef();
        compo.setMessageId("メッセージID");
        register("dummy", compo);
        assertThat(target.getMessageIdFromAnnotation(anon), is("M001"));

        // アノテーションにメッセージIDが設定されていない場合
        String messageId = target.getMessageIdFromAnnotation(new SystemChar() {
            public boolean allowLineSeparator() {
                return false;
            }

            public String charsetDef() {
                return "dummy";
            }

            public String messageId() {
                return null;
            }

            public Class<? extends Annotation> annotationType() {
                return null;
            }
        });
        assertThat(messageId, is("メッセージID"));
    }

    /** アノテーションからメッセージIDが取得できること。 */
    @Test
    public void testGetMessageIdFromAnnotation() {
        register("dummy", new LiteralCharsetDef());
        SystemChar anon = get("dummy", false);
        assertThat(target.getMessageIdFromAnnotation(anon), is("M001"));
    }

    @Test
    public void createAnnotation() {
        SystemCharValidator validator = new SystemCharValidator();
        SystemChar annotation = validator.createAnnotation(new HashMap<String, Object>());
        assertThat(annotation, is(instanceOf(annotation.annotationType())));
        assertThat(annotation.charsetDef(), is(""));
        assertThat(annotation.allowLineSeparator(), is(false));
        assertThat(annotation.messageId(), is(""));

        annotation = validator.createAnnotation(new HashMap<String, Object>(){{
            put("charsetDef", "a");
            put("allowLineSeparator", true);
            put("messageId", "b");
        }});
        assertThat(annotation, is(instanceOf(annotation.annotationType())));
        assertThat(annotation.charsetDef(), is("a"));
        assertThat(annotation.allowLineSeparator(), is(true));
        assertThat(annotation.messageId(), is("b"));
    }

    /**
     * 許容文字集合定義をシステムリポジトリに登録する。
     *
     * @param name 名前
     * @param def  許容文字集合定義
     */
    private void register(String name, CharsetDef... def) {
        CompositeCharsetDef compo = new CompositeCharsetDef();
        compo.setCharsetDefList(Arrays.asList(def));
        register(name, compo);
    }

    /**
     * 許容文字集合定義をシステムリポジトリに登録する。
     *
     * @param name 名前
     * @param def  許容文字集合定義
     */
    private void register(String name, CharsetDef def) {
        new SimpleLoader().add(name, def).register();
    }

    /**
     * アノテーションを取得する。
     *
     * @param charsetDefName     許容文字集合定義名称
     * @param allowLineSeparator 改行コードを許容するか
     * @return アノテーションインスタンス
     */
    private SystemChar get(final String charsetDefName, final boolean allowLineSeparator) {
        return new SystemChar() {
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            public String messageId() {
                return "M001";
            }

            public String charsetDef() {
                return charsetDefName;
            }

            public boolean allowLineSeparator() {
                return allowLineSeparator;
            }
        };
    }
}

