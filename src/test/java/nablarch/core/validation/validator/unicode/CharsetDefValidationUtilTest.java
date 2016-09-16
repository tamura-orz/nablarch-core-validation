package nablarch.core.validation.validator.unicode;

import nablarch.core.repository.SimpleLoader;
import nablarch.test.NablarchTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static nablarch.core.validation.validator.unicode.CharsetDefValidationUtil.isValid;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CharsetDefValidationUtil}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class CharsetDefValidationUtilTest {

    /** 制御文字以外のASCII */
    private static RangedCharsetDef asciiWoCC = new RangedCharsetDef();

    /** ひらがな、カタカナ */
    private static test.core.validation.validator.unicode.BlockNameCharsetDef kana = new test.core.validation.validator.unicode.BlockNameCharsetDef();

    /** CJK拡張B */
    private static test.core.validation.validator.unicode.BlockNameCharsetDef cjkExtensionB = new test.core.validation.validator.unicode.BlockNameCharsetDef();

    /** 前準備 */
    @BeforeClass
    public static void setUpClass() {
        // ASCII
        asciiWoCC.setStartCodePoint("U+0020");
        asciiWoCC.setEndCodePoint("U+007F");
        // カナ
        kana.setBlockNames(Arrays.asList("KATAKANA", "HIRAGANA"));
        // サロゲートペア用のCJK拡張B
        cjkExtensionB.setBlockNames(Arrays.asList("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B"));
    }

    /**
     * バリデーション対象文字列の各文字が、許可指定された文字集合に含まれている場合、
     * バリデーションが成功すること。
     */
    @Test
    public void testSingleCharsetDef() {
        assertThat(isValid(asciiWoCC, "01ABC"), is(true));
        assertThat(isValid(asciiWoCC, "あ"), is(false));
    }

    /** 改行コード不許可の場合、改行コードが許容されないこと */
    @Test
    public void testLineSeparatorNotAllowed() {
        assertThat(isValid(asciiWoCC, "\n"), is(false));  // デフォルト不許可
        assertThat(isValid(asciiWoCC, "\n"), is(false));
        assertThat(isValid(asciiWoCC, "\n", false), is(false));
        assertThat(isValid(asciiWoCC, "\r", false), is(false));
    }

    /** 改行コード許容の場合、改行コードが許容されること */
    @Test
    public void testLineSeparator() {
        assertThat(isValid(asciiWoCC, "01\rA\nBC\r\n", true), is(true));
    }

    /**
     * 複数の文字集合を組み合わせた場合、
     * バリデーション対象文字列の各文字が、いずれかの文字集合に含まれていれば
     * バリデーションが成功すること。
     */
    @Test
    public void testComposite() {
        CharsetDef def = composite(asciiWoCC, kana);
        assertThat(isValid(def, "01アあ"), is(true));
        assertThat(isValid(def, "川"), is(false));  // ASCIIでもカナでもない
    }

    /**
     * サロゲートペアを許容するよう指定した場合、
     * バリデーション対象文字にサロゲートペアが含まれていた場合に、
     * 非許容と判定されないこと。
     */
    @Test
    public void testSurrogateArrow() {
        CharsetDef def = composite(kana, cjkExtensionB);
        assertThat(isValid(def, "かな\uD867\uDE3D\r\n", true, true),
                   is(true));
        assertThat(isValid(def, "a", true, true), is(false));
    }
    /**
     * サロゲートペアを許容するよう指定した場合、
     * バリデーション対象文字にサロゲートペアが含まれていた場合に、
     * 非許容と判定されること。
     */
    @Test
    public void testSurrogateNotArrowed() {
        CharsetDef def = composite(kana, cjkExtensionB);
        assertThat(isValid(def, "\uD867\uDE3D"), is(false));   // デフォルトはfalse
        assertThat(isValid(def, "\uD867\uDE3D", false, false), is(false));
    }

    /**
     * 許容文字集合定義の名称でバリデーションができること。
     */
    @Test
    public void testLookUp() {
        CharsetDef def = composite(asciiWoCC, cjkExtensionB);
        // システムリポジトリに登録
        String name = "myDef";
        new SimpleLoader().add(name, def).register();
        // 前述のケースから抜粋してテスト
        assertThat(isValid(name, "01ABC"), is(true));
        assertThat(isValid(name, "あ"), is(false));   // 範囲外
        assertThat(isValid(name,  "01\rA\nBC\r\n", true), is(true));          // 改行コードを許容
        assertThat(isValid(name,  "01\rA\nBC\r\n", false), is(false));        // 改行コードを拒否
        assertThat(isValid(name, "\uD867\uDE3D"), is(false));                 // サロゲートペアを拒否
        assertThat(isValid(name, "\uD867\uDE3D", false, false), is(false));   // サロゲートペアを許容
    }

    /** コンストラクタ呼び出しテスト（カバレッジ対策） */
    @Test
    public void testConstructor() {
        NablarchTestUtils.invokePrivateDefaultConstructor(CharsetDefValidationUtil.class);

    }

    private CharsetDef composite(CharsetDef... defs) {
        CompositeCharsetDef compo = new CompositeCharsetDef();
        compo.setCharsetDefList(Arrays.asList(defs));
        return compo;
    }


}
