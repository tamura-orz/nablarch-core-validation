package nablarch.core.validation.validator.unicode;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author T.Kawasaki
 */
public class RangedCharsetDefTest {

    /** テスト対象 */
    private RangedCharsetDef target = new RangedCharsetDef();

    /**
     * {@link RangedCharsetDef#contains(int)}のテスト。
     * 開始位置から終了位置の範囲内にあるコードポイントが指定され場合は
     * 真が返却されること。
     */
    @Test
    public void testVariation() {
        target.setStartCodePoint("U+0020"); // 開始位置
        target.setEndCodePoint("U+007F");   // 終了位置

        assertThat(target.contains(0x1F), is(false)); // 範囲外（小さい）
        assertThat(target.contains(0x20), is(true));  // 範囲内
        assertThat(target.contains(0x7F), is(true));  // 範囲内
        assertThat(target.contains(0x80), is(false)); // 範囲外（大きい）
    }

    /** 開始、終了は同じでもよいこと。(単一のコードポイントのみを許容する) */
    @Test
    public void testSetSameCodePoint() {
        target.setStartCodePoint("U+0020");
        target.setEndCodePoint("U+0020");

        assertThat(target.contains(0x1F), is(false)); // 範囲外（小さい）
        assertThat(target.contains(0x20), is(true));  // 範囲内
        assertThat(target.contains(0x21), is(false)); // 範囲外（大きい）
    }

    /**
     * {@link Character#isValidCodePoint(int)}に
     * 合致しないコードポイントを指定した場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetStartCodePointInvalid() {
        target.setStartCodePoint("U+110000");
    }

    /** 開始位置に小さすぎるコードポイントを指定した場合、例外が発生すること */
    @Test(expected = IllegalArgumentException.class)
    public void testSetStartCodePointTooSmall() {
        target.setStartCodePoint("U+123");
    }

    /** 開始位置に大きすぎるコードポイントを指定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetStartCodePointTooBig() {
        target.setStartCodePoint("U+123456789");
    }

    /**
     * {@link Character#isValidCodePoint(int)}に
     * 合致しないコードポイントを指定した場合、例外が発生すること。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetEndCodePointInvalid() {
        target.setEndCodePoint("U+110000");
    }

    /** 終了位置に小さすぎるコードポイントを指定した場合、例外が発生すること */
    @Test(expected = IllegalArgumentException.class)
    public void testSetEndCodePointTooSmall() {
        target.setEndCodePoint("U+123");
    }

    /** 終了位置に大きすぎるコードポイントを指定した場合、例外が発生すること。 */
    @Test(expected = IllegalArgumentException.class)
    public void testSetEndCodePointTooBig() {
        target.setEndCodePoint("U+123456789");
    }

    /** 開始、終了の大小関係が逆転している場合例外が発生すること */
    @Test(expected = IllegalStateException.class)
    public void testSetInvalidCodePoints() {
        target.setStartCodePoint("U+0020");
        target.setEndCodePoint("U+001F"); // smaller
    }

    @Test
    public void testMessageId() {
        target.setMessageId("id");
        assertThat(target.getMessageId(), is("id"));
    }
}
