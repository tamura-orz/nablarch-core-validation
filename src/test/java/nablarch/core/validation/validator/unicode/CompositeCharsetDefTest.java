package nablarch.core.validation.validator.unicode;

import org.junit.Test;

import java.util.Arrays;

import static nablarch.core.validation.validator.unicode.UnicodeTestUtil.codePointOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CompositeCharsetDef}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class CompositeCharsetDefTest {

    /**
     * 複数の許容文字集合いずれかで許容される文字であれば、
     * 許容文字と判定されること。
     */
    @Test
    public void testContains() {
        LiteralCharsetDef literal = new LiteralCharsetDef();
        literal.setAllowedCharacters("ACE");
        
        RangedCharsetDef ranged = new RangedCharsetDef();
        ranged.setStartCodePoint("U+0031");  // 0
        ranged.setEndCodePoint("U+0032");    // 1
        
        CompositeCharsetDef target = new CompositeCharsetDef();
        target.setCharsetDefList(Arrays.asList(literal, ranged));
        
        assertThat(target.contains(codePointOf('A')), is(true));
        assertThat(target.contains(codePointOf('B')), is(false));
        assertThat(target.contains(codePointOf('C')), is(true));
        assertThat(target.contains(codePointOf('D')), is(false));
        assertThat(target.contains(codePointOf('E')), is(true));
        assertThat(target.contains(codePointOf('F')), is(false));
        assertThat(target.contains(codePointOf('0')), is(false));
        assertThat(target.contains(codePointOf('1')), is(true));
        assertThat(target.contains(codePointOf('2')), is(true));
        assertThat(target.contains(codePointOf('3')), is(false));
    }

    @Test
    public void testMessageId() {
        CompositeCharsetDef compositeCharsetDef = new CompositeCharsetDef();
        compositeCharsetDef.setMessageId("message");
        assertThat(compositeCharsetDef.getMessageId(), is("message"));
    }
}

