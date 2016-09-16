package nablarch.core.validation.validator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AsciiCharacterCheckerTest {

    @Test
    public void testCheckAsciiCharOnly() {
        assertTrue(AsciiCharacterChecker.checkAsciiCharOnly(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));

        assertFalse(AsciiCharacterChecker.checkAsciiCharOnly(" ｱ!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));
        assertFalse(AsciiCharacterChecker.checkAsciiCharOnly(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPｱQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));
        assertFalse(AsciiCharacterChecker.checkAsciiCharOnly(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ｱ"));

    }
    @Test
    public void testCheckAlphaCharOnly() {
        assertTrue(AsciiCharacterChecker.checkAlphaCharOnly("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));

        assertFalse(AsciiCharacterChecker.checkAlphaCharOnly("0ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));
        assertFalse(AsciiCharacterChecker.checkAlphaCharOnly("ABCDEFGHIJKLMNOPQRSTUVWX0YZabcdefghijklmnopqrstuvwxyz"));
        assertFalse(AsciiCharacterChecker.checkAlphaCharOnly("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0"));
    }

    @Test
    public void testCheckNumberCharOnly() {
        assertTrue(AsciiCharacterChecker.checkNumberCharOnly("0123456789"));

        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("A0123456789"));
        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("012345A6789"));
        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("0123456789A"));
    }

    @Test
    public void testCheckAlnumCharOnly() {
        assertTrue(AsciiCharacterChecker.checkAlnumCharOnly("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));

        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("-ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));
        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("ABCDEFGHIJKLMNOPQRSTUVWXYZabc-defghijklmnopqrstuvwxyz0123456789"));
        assertFalse(AsciiCharacterChecker.checkNumberCharOnly("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-"));
    }

}
