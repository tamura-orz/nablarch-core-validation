package nablarch.core.validation.validator.unicode;

import nablarch.core.repository.SimpleLoader;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.validator.AsciiCharacterChecker;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Arrays;

/**
 * @author T.Kawasaki
 */
public class PerformanceTest {

    @Rule
    public final TestName testName = new TestName();

    private static final int LOOP_CNT = 50 * 20 * 10;  // 50 リクエスト * 20項目 * 全体の1/10

    private static final int CHAR_CNT = 100; // 一回あたり100字

    private static final String zzz = StringUtil.repeat("Z", CHAR_CNT);

    private static final String halfKana = StringUtil.repeat("ｱ", CHAR_CNT);

    private static test.core.validation.validator.unicode.BlockNameCharsetDef tenBlocks = new test.core.validation.validator.unicode.BlockNameCharsetDef();

    @BeforeClass
    public static void setUp() {
        tenBlocks.setBlockNames(
                Arrays.asList(
                        "AEGEAN_NUMBERS",
                        "ALPHABETIC_PRESENTATION_FORMS",
                        "ARABIC",
                        "ARABIC_PRESENTATION_FORMS_A",
                        "ARABIC_PRESENTATION_FORMS_B",
                        "ARMENIAN",
                        "ARROWS",
                        "BASIC_LATIN",
                        "BENGALI",
                        "BLOCK_ELEMENTS",
                        "BOPOMOFO",
                        "BOPOMOFO_EXTENDED",
                        "BOX_DRAWING",
                        "BRAILLE_PATTERNS",
                        "BUHID",
                        "BYZANTINE_MUSICAL_SYMBOLS",
                        "CHEROKEE",
                        "CJK_COMPATIBILITY",
                        "CJK_COMPATIBILITY_FORMS",
                        "CJK_COMPATIBILITY_IDEOGRAPHS",
                        "CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT",
                        "CJK_RADICALS_SUPPLEMENT",
                        "CJK_SYMBOLS_AND_PUNCTUATION",
                        "CJK_UNIFIED_IDEOGRAPHS",
                        "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A",
                        "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B",
                        "COMBINING_DIACRITICAL_MARKS",
                        "COMBINING_HALF_MARKS",
                        "COMBINING_MARKS_FOR_SYMBOLS",
                        "CONTROL_PICTURES",
                        "CURRENCY_SYMBOLS",
                        "CYPRIOT_SYLLABARY",
                        "CYRILLIC",
                        "CYRILLIC_SUPPLEMENTARY",
                        "DESERET",
                        "DEVANAGARI",
                        "DINGBATS",
                        "ENCLOSED_ALPHANUMERICS",
                        "ENCLOSED_CJK_LETTERS_AND_MONTHS",
                        "ETHIOPIC",
                        "GENERAL_PUNCTUATION",
                        "GEOMETRIC_SHAPES",
                        "GEORGIAN",
                        "GOTHIC",
                        "GREEK",
                        "GREEK_EXTENDED",
                        "GUJARATI",
                        "GURMUKHI",
                        "HANGUL_COMPATIBILITY_JAMO",
                        "HANGUL_JAMO",
                        "HANGUL_SYLLABLES",
                        "HANUNOO",
                        "HEBREW",
                        "HIRAGANA",
                        "IDEOGRAPHIC_DESCRIPTION_CHARACTERS",
                        "IPA_EXTENSIONS",
                        "KANBUN",
                        "KANGXI_RADICALS",
                        "KANNADA",
                        "KATAKANA",
                        "KATAKANA_PHONETIC_EXTENSIONS",
                        "KHMER",
                        "KHMER_SYMBOLS",
                        "LAO",
                        "LATIN_1_SUPPLEMENT",
                        "LATIN_EXTENDED_A",
                        "LATIN_EXTENDED_ADDITIONAL",
                        "LATIN_EXTENDED_B",
                        "LETTERLIKE_SYMBOLS",
                        "LIMBU",
                        "LINEAR_B_IDEOGRAMS",
                        "LINEAR_B_SYLLABARY",
                        "MALAYALAM",
                        "MATHEMATICAL_ALPHANUMERIC_SYMBOLS",
                        "MATHEMATICAL_OPERATORS",
                        "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A",
                        "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B",
                        "MISCELLANEOUS_SYMBOLS",
                        "MISCELLANEOUS_SYMBOLS_AND_ARROWS",
                        "MISCELLANEOUS_TECHNICAL",
                        "MONGOLIAN",
                        "MUSICAL_SYMBOLS",
                        "MYANMAR",
                        "NUMBER_FORMS",
                        "OGHAM",
                        "OLD_ITALIC",
                        "OPTICAL_CHARACTER_RECOGNITION",
                        "ORIYA",
                        "OSMANYA",
                        "PHONETIC_EXTENSIONS",
                        "PRIVATE_USE_AREA",
                        "RUNIC",
                        "SHAVIAN",
                        "SINHALA",
                        "SMALL_FORM_VARIANTS",
                        "SPACING_MODIFIER_LETTERS",
                        "SPECIALS",
                        "SUPERSCRIPTS_AND_SUBSCRIPTS",
                        "SUPPLEMENTAL_ARROWS_A",
                        "SUPPLEMENTAL_ARROWS_B",
                        "SUPPLEMENTAL_MATHEMATICAL_OPERATORS",
                        "SUPPLEMENTARY_PRIVATE_USE_AREA_A",
                        "SUPPLEMENTARY_PRIVATE_USE_AREA_B",
                        "SYRIAC",
                        "TAGALOG",
                        "TAGBANWA",
                        "TAGS",
                        "TAI_LE",
                        "TAI_XUAN_JING_SYMBOLS",
                        "TAMIL",
                        "TELUGU",
                        "THAANA",
                        "THAI",
                        "TIBETAN",
                        "UGARITIC",
                        "UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS",
                        "VARIATION_SELECTORS",
                        "VARIATION_SELECTORS_SUPPLEMENT",
                        "YI_RADICALS",
                        "YI_SYLLABLES",
                        "YIJING_HEXAGRAM_SYMBOLS",
                        "HALFWIDTH_AND_FULLWIDTH_FORMS"
                )
        );
    }


    @Test
    public void BlockNameCharsetDef一つで合致するパターン() {
        test.core.validation.validator.unicode.BlockNameCharsetDef def = new test.core.validation.validator.unicode.BlockNameCharsetDef();
        def.setBlockNames(Arrays.asList("BASIC_LATIN"));
        new SimpleLoader().add("basicLatin", def).register();
        go("basicLatin", zzz, LOOP_CNT);
    }

    @Test
    public void BlockNameCharsetDef一つで合致するパターンリポジトリ登録なし() {
        test.core.validation.validator.unicode.BlockNameCharsetDef def = new test.core.validation.validator.unicode.BlockNameCharsetDef();
        def.setBlockNames(Arrays.asList("BASIC_LATIN"));
        go(def, zzz, LOOP_CNT);
    }

    @Ignore
    @Test
    public void BlockNameCharsetDefを連結して最後のブロックに合致する() {
        go(tenBlocks, halfKana, LOOP_CNT);
    }

    @Test
    public void BlockNameCharsetDefを連結して最後のブロックに合致する_キャッシュあり() {
        CachingCharsetDef cache = new CachingCharsetDef();
        cache.setCharsetDef(tenBlocks);
        go(cache, halfKana, LOOP_CNT);
    }

    @Test
    public void RangedNameCharsetDef一つで合致するパターンリポジトリ登録なし() {
        RangedCharsetDef def = new RangedCharsetDef();
        def.setStartCodePoint("U+0000");
        def.setEndCodePoint("U+00FF");
        go(def, zzz, LOOP_CNT);
    }

    @Test
    public void LiteralCharsetDefで合致するパターン() {
        LiteralCharsetDef def = new LiteralCharsetDef();
        def.setAllowedCharacters("ABCDEFGHJKLMNOPQRSTUVWXYZ");
        go(def, zzz, LOOP_CNT);
    }


    @Test
    public void 参考にAsciiCharacterCheckerで() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP_CNT; i++) {    // 10man
            AsciiCharacterChecker.checkAlnumCharOnly(zzz);
        }
        printResult(start);
    }


    private void go(CharsetDef def, String str, int cnt) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < cnt; i++) {
            CharsetDefValidationUtil.isValid(def, str);
        }
        printResult(start);
    }

    private void go(String defName, String str, int cnt) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < cnt; i++) {
            CharsetDefValidationUtil.isValid(defName, str);
        }
        printResult(start);
    }

    private void printResult(long start) {
        System.out.println(testName.getMethodName() + '\t' + (System.currentTimeMillis() - start));
    }
}
